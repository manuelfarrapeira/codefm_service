CREATE TABLE IF NOT EXISTS class_subject_exercise_student_document
(
    id                             INT AUTO_INCREMENT PRIMARY KEY,
    class_subject_exercise_student INT          NOT NULL,
    document                       VARCHAR(255) NOT NULL,
    description                    TEXT         NULL,
    CONSTRAINT class_subject_exercise_student_document_student_fk
        FOREIGN KEY (class_subject_exercise_student) REFERENCES class_subject_exercise_student (id)
);

