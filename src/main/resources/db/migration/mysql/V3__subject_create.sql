drop table if exists subject CASCADE;
drop table if exists subject_students CASCADE;

create table subject
(
    id         bigint NOT NULL AUTO_INCREMENT,
    name       varchar(255),
    teacher_id bigint,
    primary key (id)
);

create table subject_students
(
    subject_id   bigint not null,
    students_id bigint not null,
    primary key (subject_id, students_id)
);

alter table subject
    add constraint subject_teacher_constraint
        foreign key (teacher_id)
            references teacher (id);

alter table subject_students
    add constraint subject_students_students_constraint
        foreign key (students_id)
            references student (id);

alter table subject_students
    add constraint subject_students_subject_constraint
        foreign key (subject_id)
            references subject (id);

