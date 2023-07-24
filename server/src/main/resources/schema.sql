DROP TABLE IF EXISTS users, items, bookings, requests, comments;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT       NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name  VARCHAR(310)  NOT NULL,
    email VARCHAR(310) NOT NULL,
    CONSTRAINT user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT                      NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    description  VARCHAR(1000)                NOT NULL,
    requester_id BIGINT                      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS items
(
    id           BIGINT       NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name         VARCHAR(310)  NOT NULL,
    description  VARCHAR(1000) NOT NULL,
    is_available BOOLEAN      NOT NULL,
    owner_id     BIGINT       NOT NULL REFERENCES users (id),
    request_id   BIGINT       REFERENCES requests (id)
);

CREATE TABLE IF NOT EXISTS bookings
(
    id         BIGINT                   NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date   TIMESTAMP WITH TIME ZONE NOT NULL,
    item_id    BIGINT                   NOT NULL REFERENCES items (id),
    booker_id  BIGINT                   NOT NULL REFERENCES users (id),
    status     varchar(30)              NOT NULL
);

CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT                      NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text      VARCHAR(1000)                NOT NULL,
    item_id   BIGINT                      NOT NULL REFERENCES items (id),
    author_id BIGINT                      NOT NULL REFERENCES users (id),
    created   TIMESTAMP WITHOUT TIME ZONE NOT NULL
);