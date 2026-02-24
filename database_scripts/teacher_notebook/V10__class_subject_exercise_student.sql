CREATE TABLE IF NOT EXISTS class_subject_exercise_student
(
    id            int auto_increment primary key,
    id_student    int  not null,
    id_exercise   int  not null,
    grade         int  not null,
    description   text null,
    deletion_date date null,
    constraint class_subject_exercise_student_class_subject_exercise_id_fk
        foreign key (id_exercise) references class_subject_exercise (id),
    constraint class_subject_exercise_student_students_id_fk
        foreign key (id_student) references students (id)
);

