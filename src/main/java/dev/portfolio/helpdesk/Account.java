package dev.portfolio.helpdesk;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id UUID id = UUID.randomUUID();
    @Column(nullable = false, unique = true, length = 254) String email;
    @Column(nullable = false) String passwordHash;
    @Column(nullable = false, length = 80) String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) Role role = Role.CUSTOMER;
    protected Account() {}
    public UUID getId() { return id; }
    Account(String email, String passwordHash, String name, Role role) {
        this.email = email; this.passwordHash = passwordHash; this.name = name; this.role = role;
    }
    public enum Role { CUSTOMER, AGENT }
}
