CREATE TABLE IF NOT EXISTS class_subject_exercise_document
(
    id                     INT AUTO_INCREMENT PRIMARY KEY,
    class_subject_exercise INT          NOT NULL,
    document               VARCHAR(255) NOT NULL,
    description            TEXT         NULL,
    CONSTRAINT class_subject_exercise_document_class_subject_exercise_id_fk
        FOREIGN KEY (class_subject_exercise) REFERENCES class_subject_exercise (id)
);

