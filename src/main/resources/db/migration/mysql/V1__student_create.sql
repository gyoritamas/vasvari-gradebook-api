drop table if exists student CASCADE;

create table student
(
    id          bigint NOT NULL AUTO_INCREMENT,
    firstname   varchar(255),
    lastname    varchar(255),
    grade_level integer,
    email       varchar(255),
    address     varchar(255),
    phone       varchar(255),
    birthdate   date,
    primary key (id)
);
