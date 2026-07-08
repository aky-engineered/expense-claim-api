--liquibase formatted sql
--changeset aqif:001-create-schema

CREATE TABLE users
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE expense_claims
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id      BIGINT         NOT NULL REFERENCES users (id),
    description      VARCHAR(255)   NOT NULL,
    amount           DECIMAL(10, 2) NOT NULL,
    date             DATE           NOT NULL,
    category         VARCHAR(20)    NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(500),
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_log
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    claim_id     BIGINT    NOT NULL REFERENCES expense_claims (id),
    action       VARCHAR(50),
    performed_by BIGINT    NOT NULL REFERENCE users(id),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details      VARCHAR(500)
);
)