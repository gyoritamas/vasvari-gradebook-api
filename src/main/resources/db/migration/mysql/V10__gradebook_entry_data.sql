insert into subject_students
    (subject_id, students_id)
values (1, 1),
       (1, 2),
       (2, 1),
       (2, 2);

insert into gradebook_entry
    (student_id, subject_id, assignment_id, grade)
values (1, 2, 1, 4),
       (1, 2, 2, 4),
       (2, 2, 1, 4),
       (2, 2, 2, 5);
