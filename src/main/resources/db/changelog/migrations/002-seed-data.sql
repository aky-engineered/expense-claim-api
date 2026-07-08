--liquibase formatted sql
--changeset aqif:002-seed-data

INSERT INTO users (username, password, role)
VALUES ('john.smith', '$2a$12$WtQ2XFXQjRGV9Xqtu4U0H.U8XTykVMT1R6UpSmt6VioOoheKH0Q9e', 'EMPLOYEE'),
       ('jane.doe', '$2a$12$J0mvEih5yjYrxjn7QwiAb.QVdEFchIr0dc9eSh5J4p/ipNZ3PXqCe', 'EMPLOYEE'),
       ('mike.approver', '$2a$12$NQKuVxwW8lFgpo77L1kh1OYGiww.l7AOgCkm9/xaQ812pe9shG6hO', 'APPROVER');

