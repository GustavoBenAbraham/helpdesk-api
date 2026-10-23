package dev.portfolio.helpdesk;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "ticket_events")
public class TicketEvent {
    @Id UUID id = UUID.randomUUID();
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ticket_id") Ticket ticket;
    @Column(nullable = false) String actor;
    @Column(nullable = false, length = 1000) String message;
    @Column(nullable = false) Instant createdAt = Instant.now();
    protected TicketEvent() {}
    TicketEvent(Ticket ticket, String actor, String message) { this.ticket = ticket; this.actor = actor; this.message = message; }
}
