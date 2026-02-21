CREATE TABLE IF NOT EXISTS class_subject_exercise
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    id_subject_class INT          NOT NULL,
    title            VARCHAR(100) NOT NULL,
    description      TEXT         NULL,
    quarter          INT          NOT NULL,
    percentage_grade INT          NOT NULL,
    max_grade        INT          NOT NULL,
    deletion_date    DATE         NULL,
    CONSTRAINT class_subject_exercise_subjerct_classes_id_fk
        FOREIGN KEY (id_subject_class) REFERENCES subjerct_classes (id)
);

