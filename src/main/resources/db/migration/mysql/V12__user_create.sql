drop table if exists user CASCADE;

create table user
(
    id       bigint NOT NULL AUTO_INCREMENT,
    username varchar(255),
    password varchar(255),
    role     varchar(255),
    enabled  boolean,
    primary key (id)
)