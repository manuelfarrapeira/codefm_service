CREATE TABLE IF NOT EXISTS student_absences
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    student_class_id INT  NOT NULL,
    subject_id       INT  NOT NULL,
    absence_date     DATE NOT NULL,
    UNIQUE KEY uk_student_absence (student_class_id, subject_id, absence_date),
    CONSTRAINT fk_absence_student_class FOREIGN KEY (student_class_id) REFERENCES students_classes (id)
);
