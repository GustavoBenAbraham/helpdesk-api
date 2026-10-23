package dev.portfolio.helpdesk;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import static dev.portfolio.helpdesk.ApiModels.*;

@Service
@Transactional
public class TicketService {
    private final TicketRepository tickets;
    private final AccountRepository accounts;
    private final EventRepository events;
    public TicketService(TicketRepository tickets, AccountRepository accounts, EventRepository events) { this.tickets=tickets; this.accounts=accounts; this.events=events; }
    private Account actor(String email) { return accounts.findByEmail(email).orElseThrow(); }
    private Ticket accessible(UUID id, Account actor) {
        Ticket t = tickets.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Chamado não encontrado"));
        if(actor.role != Account.Role.AGENT && !t.owner.getId().equals(actor.id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Chamado não encontrado");
        return t;
    }
    public TicketView create(NewTicket input, String email) {
        Account a = actor(email);
        Ticket t = tickets.save(new Ticket(input.title(),input.description(),input.priority(),a));
        events.save(new TicketEvent(t,a.email,"Chamado criado"));
        return TicketView.of(t);
    }
    @Transactional(readOnly=true)
    public PageView<TicketView> list(String email, Ticket.Status status, Ticket.Priority priority, int page, int size) {
        Account a = actor(email);
        Specification<Ticket> spec = (root,q,cb) -> cb.conjunction();
        if(a.role != Account.Role.AGENT) spec = spec.and((r,q,cb) -> cb.equal(r.get("owner").get("id"),a.id));
        if(status != null) spec = spec.and((r,q,cb) -> cb.equal(r.get("status"),status));
        if(priority != null) spec = spec.and((r,q,cb) -> cb.equal(r.get("priority"),priority));
        return PageView.of(tickets.findAll(spec,PageRequest.of(page,size,Sort.by(Sort.Order.desc("createdAt"),Sort.Order.desc("id")))).map(TicketView::of));
    }
    @Transactional(readOnly=true)
    public TicketView get(UUID id, String email) { return TicketView.of(accessible(id,actor(email))); }
    public TicketView transition(UUID id, Ticket.Status next, String email) {
        Account a = actor(email);
        if(a.role != Account.Role.AGENT) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Apenas agentes alteram status");
        Ticket t = accessible(id,a);
        boolean allowed = switch(t.status) {
            case OPEN -> next == Ticket.Status.IN_PROGRESS;
            case IN_PROGRESS -> next == Ticket.Status.RESOLVED;
            case RESOLVED -> next == Ticket.Status.CLOSED || next == Ticket.Status.IN_PROGRESS;
            case CLOSED -> false;
        };
        if(!allowed) throw new ResponseStatusException(HttpStatus.CONFLICT,"Transição de status inválida: " + t.status + " -> " + next);
        events.save(new TicketEvent(t,a.email,"Status: " + t.status + " -> " + next));
        t.status = next; t.updatedAt = Instant.now();
        tickets.flush();
        return TicketView.of(t);
    }
    public EventView comment(UUID id, String message, String email) {
        Account a = actor(email); Ticket t = accessible(id,a);
        if(t.status == Ticket.Status.CLOSED) throw new ResponseStatusException(HttpStatus.CONFLICT,"Chamado encerrado não aceita comentários");
        t.updatedAt = Instant.now();
        return EventView.of(events.save(new TicketEvent(t,a.email,message)));
    }
    @Transactional(readOnly=true)
    public PageView<EventView> history(UUID id, String email, int page, int size) {
        accessible(id,actor(email));
        return PageView.of(events.findByTicketId(id,PageRequest.of(page,size,Sort.by("createdAt","id"))).map(EventView::of));
    }
}
