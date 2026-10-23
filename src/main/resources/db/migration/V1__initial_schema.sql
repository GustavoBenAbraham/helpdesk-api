CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(80) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER','AGENT'))
);
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED')),
    owner_id UUID NOT NULL REFERENCES accounts(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_tickets_owner_created ON tickets(owner_id, created_at);
CREATE INDEX idx_tickets_status_priority ON tickets(status, priority);
CREATE TABLE ticket_events (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets(id),
    actor VARCHAR(254) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_events_ticket_created ON ticket_events(ticket_id, created_at);
