drop table if exists course CASCADE;
drop table if exists course_students CASCADE;

create table course
(
    id   bigint NOT NULL AUTO_INCREMENT,
    name varchar(255),
    primary key (id)
);

create table course_students
(
    course_id   bigint not null,
    students_id bigint not null,
    primary key (course_id, students_id)
);

alter table course_students
    add constraint FK98urt246twcpsnbpa4ejy1uj5
        foreign key (students_id)
            references student (id);

alter table course_students
    add constraint FK2ddw41wqqiilmi5oj69nwxbaa
        foreign key (course_id)
            references course (id);

