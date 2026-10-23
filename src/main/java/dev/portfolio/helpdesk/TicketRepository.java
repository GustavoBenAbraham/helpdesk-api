package dev.portfolio.helpdesk;
import org.springframework.data.jpa.repository.*;
import java.util.UUID;
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {}
