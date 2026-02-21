CREATE TABLE IF NOT EXISTS subjerct_classes
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    id_subject    INT  NOT NULL,
    id_class      INT  NOT NULL,
    deletion_date DATE NULL,
    CONSTRAINT subjerct_classes_subjects_id_fk
        FOREIGN KEY (id_subject) REFERENCES subjects (id),
    CONSTRAINT subjerct_classes_classes_id_fk
        FOREIGN KEY (id_class) REFERENCES classes (id)
);

