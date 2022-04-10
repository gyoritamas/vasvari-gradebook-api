drop table if exists teacher CASCADE;

create table teacher
(
    id        bigint NOT NULL AUTO_INCREMENT,
    address   varchar(255),
    birthdate date,
    email     varchar(255),
    firstname varchar(255),
    lastname  varchar(255),
    phone     varchar(255),
    primary key (id)
);