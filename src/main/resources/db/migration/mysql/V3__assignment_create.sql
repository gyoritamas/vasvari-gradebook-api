drop table if exists assignment CASCADE;

create table assignment
(
    id          bigint NOT NULL AUTO_INCREMENT,
    name        varchar(255),
    type        varchar(255),
    description varchar(255),
    created_at  timestamp,
    primary key (id)
);