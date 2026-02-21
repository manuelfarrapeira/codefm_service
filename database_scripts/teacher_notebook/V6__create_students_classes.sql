CREATE TABLE IF NOT EXISTS students_classes
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    id_class      INT  NOT NULL,
    id_student    INT  NOT NULL,
    deletion_date DATE NULL,
    CONSTRAINT students_classes_classes_id_fk
        FOREIGN KEY (id_class) REFERENCES classes (id),
    CONSTRAINT students_classes_students_id_fk
        FOREIGN KEY (id_student) REFERENCES students (id)
);

