drop table if exists gradebook_entry CASCADE;

create table gradebook_entry
(
    id            bigint NOT NULL AUTO_INCREMENT,
    grade         integer,
    assignment_id bigint,
    course_id     bigint,
    student_id    bigint,
    primary key (id)
);

alter table gradebook_entry
    add constraint FKanp74jram41jnx5x8dkaf4km7
        foreign key (assignment_id)
            references assignment (id);

alter table gradebook_entry
    add constraint FK1r53dp4himt9xaw8xgoxwky8
        foreign key (course_id)
            references course (id);

alter table gradebook_entry
    add constraint FK335bhocwlnems95irjffts7b8
        foreign key (student_id)
            references student (id);