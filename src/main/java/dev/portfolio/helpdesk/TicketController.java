package dev.portfolio.helpdesk;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.util.UUID;
import static dev.portfolio.helpdesk.ApiModels.*;
@RestController
@RequestMapping("/api/tickets")
@Validated
public class TicketController {
    private final TicketService service;
    public TicketController(TicketService service) { this.service=service; }
    @PostMapping public ResponseEntity<TicketView> create(@Valid @RequestBody NewTicket input, Authentication a) {
        TicketView t = service.create(input,a.getName());
        return ResponseEntity.created(URI.create("/api/tickets/"+t.id())).body(t);
    }
    @GetMapping public PageView<TicketView> list(Authentication a, @RequestParam(required=false) Ticket.Status status,
        @RequestParam(required=false) Ticket.Priority priority, @RequestParam(defaultValue="0") @Min(0) int page,
        @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return service.list(a.getName(),status,priority,page,size); }
    @GetMapping("/{id}") public TicketView get(@PathVariable UUID id, Authentication a) { return service.get(id,a.getName()); }
    @PatchMapping("/{id}/status") public TicketView transition(@PathVariable UUID id, @Valid @RequestBody StatusChange input, Authentication a) { return service.transition(id,input.status(),a.getName()); }
    @PostMapping("/{id}/comments") @ResponseStatus(HttpStatus.CREATED)
    public EventView comment(@PathVariable UUID id, @Valid @RequestBody Comment input, Authentication a) { return service.comment(id,input.message(),a.getName()); }
    @GetMapping("/{id}/history") public PageView<EventView> history(@PathVariable UUID id, Authentication a,
        @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) { return service.history(id,a.getName(),page,size); }
}
