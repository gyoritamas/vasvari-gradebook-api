drop table if exists assignment CASCADE;

create table assignment
(
    id          bigint NOT NULL AUTO_INCREMENT,
    name        varchar(255),
    type        varchar(255),
    description varchar(255),
    deadline    date,
    subject_id   bigint,
    primary key (id)
);

alter table assignment
    add constraint assignment_subject_constraint
        foreign key (subject_id)
            references subject (id);