package dev.portfolio.helpdesk;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Page;
public final class ApiModels {
    private ApiModels() {}
    public record Registration(@NotBlank @Email @Size(max=254) String email,
        @NotBlank @Size(min=10,max=72) String password, @NotBlank @Size(max=80) String name) {}
    public record AccountView(UUID id, String email, String name, Account.Role role) {
        static AccountView of(Account a) { return new AccountView(a.id,a.email,a.name,a.role); }
    }
    public record NewTicket(@NotBlank @Size(max=120) String title,
        @NotBlank @Size(max=4000) String description, @NotNull Ticket.Priority priority) {}
    public record StatusChange(@NotNull Ticket.Status status) {}
    public record Comment(@NotBlank @Size(max=1000) String message) {}
    public record TicketView(UUID id, String title, String description, Ticket.Priority priority,
        Ticket.Status status, UUID ownerId, Instant createdAt, Instant updatedAt, long version) {
        static TicketView of(Ticket t) { return new TicketView(t.id,t.title,t.description,t.priority,t.status,t.owner.getId(),t.createdAt,t.updatedAt,t.version); }
    }
    public record EventView(UUID id, String actor, String message, Instant createdAt) {
        static EventView of(TicketEvent e) { return new EventView(e.id,e.actor,e.message,e.createdAt); }
    }
    public record PageView<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        static <T> PageView<T> of(Page<T> p) { return new PageView<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages()); }
    }
}
