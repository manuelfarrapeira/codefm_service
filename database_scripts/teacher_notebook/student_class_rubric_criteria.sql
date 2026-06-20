CREATE TABLE student_class_rubric_criteria
(
    id              INT AUTO_INCREMENT PRIMARY KEY,
    id_class_rubric INT NOT NULL,
    id_student      INT NOT NULL,
    id_criterion    INT NOT NULL,
    deletion_date   DATE,
    CONSTRAINT fk_student_class_rubric_criteria_class_rubric FOREIGN KEY (id_class_rubric) REFERENCES class_rubrics (id),
    CONSTRAINT fk_student_class_rubric_criteria_student FOREIGN KEY (id_student) REFERENCES students (id),
    CONSTRAINT fk_student_class_rubric_criteria_criterion FOREIGN KEY (id_criterion) REFERENCES skill_rubric_criteria (id)
);

