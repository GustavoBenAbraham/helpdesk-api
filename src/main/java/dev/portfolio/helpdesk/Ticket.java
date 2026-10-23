package dev.portfolio.helpdesk;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id UUID id = UUID.randomUUID();
    @Version long version;
    @Column(nullable = false, length = 120) String title;
    @Column(nullable = false, length = 4000) String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false) Priority priority;
    @Enumerated(EnumType.STRING) @Column(nullable = false) Status status = Status.OPEN;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "owner_id") Account owner;
    @Column(nullable = false) Instant createdAt = Instant.now();
    @Column(nullable = false) Instant updatedAt = createdAt;
    protected Ticket() {}
    Ticket(String title, String description, Priority priority, Account owner) {
        this.title = title; this.description = description; this.priority = priority; this.owner = owner;
    }
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
    public enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
}
