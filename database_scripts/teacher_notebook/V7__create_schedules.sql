CREATE TABLE IF NOT EXISTS schedules
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    class_id      INT  NOT NULL,
    subject_id    INT  NOT NULL,
    day           INT  NOT NULL,
    start         TIME NOT NULL,
    end           TIME NOT NULL,
    deletion_date DATE NULL,
    CONSTRAINT schedules_classes_id_fk
        FOREIGN KEY (class_id) REFERENCES classes (id),
    CONSTRAINT schedules_subjects_id_fk
        FOREIGN KEY (subject_id) REFERENCES subjects (id)
);

