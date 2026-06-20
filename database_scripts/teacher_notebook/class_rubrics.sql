CREATE TABLE class_rubrics
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    id_class      INT NOT NULL,
    id_rubric     INT NOT NULL,
    deletion_date DATE,
    CONSTRAINT fk_class_rubrics_class FOREIGN KEY (id_class) REFERENCES classes (id),
    CONSTRAINT fk_class_rubrics_rubric FOREIGN KEY (id_rubric) REFERENCES skill_rubrics (id)
);

