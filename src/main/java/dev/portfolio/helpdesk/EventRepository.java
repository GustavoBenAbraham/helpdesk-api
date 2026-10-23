package dev.portfolio.helpdesk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.util.UUID;
public interface EventRepository extends JpaRepository<TicketEvent, UUID> {
    Page<TicketEvent> findByTicketId(UUID ticketId, Pageable page);
}
