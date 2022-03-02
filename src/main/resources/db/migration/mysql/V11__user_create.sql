drop table if exists user CASCADE;

create table user
(
    id       bigint NOT NULL AUTO_INCREMENT,
    password varchar(255),
    role     varchar(255),
    username varchar(255),
    primary key (id)
)