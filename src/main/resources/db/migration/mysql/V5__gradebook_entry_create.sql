drop table if exists gradebook_entry CASCADE;

create table gradebook_entry
(
    id            bigint NOT NULL AUTO_INCREMENT,
    grade         integer,
    assignment_id bigint,
    subject_id     bigint,
    student_id    bigint,
    primary key (id)
);

alter table gradebook_entry
    add constraint gradebook_entry_assignment_constraint
        foreign key (assignment_id)
            references assignment (id);

alter table gradebook_entry
    add constraint gradebook_entry_subject_constraint
        foreign key (subject_id)
            references subject (id);

alter table gradebook_entry
    add constraint gradebook_entry_student_constraint
        foreign key (student_id)
            references student (id);