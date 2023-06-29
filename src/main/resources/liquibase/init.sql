--liquibase formatted sql

--changeset dbogda:1
CREATE TABLE notification_tasks(
    id BIGINT generated by default as identity PRIMARY KEY,
    message TEXT NOT NULL,
    chat_id BIGINT NOT NULL,
    notification_date_time TIMESTAMP NOT NULL
);