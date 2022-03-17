drop table if exists assignment CASCADE;

create table assignment
(
    id          bigint NOT NULL AUTO_INCREMENT,
    name        varchar(255),
    type        varchar(255),
    description varchar(255),
    deadline    date,
    course_id   bigint,
    primary key (id)
);

alter table assignment
    add constraint FKbt7x8mvmfgjbjbtxpv65go0ab
        foreign key (course_id)
            references course (id);