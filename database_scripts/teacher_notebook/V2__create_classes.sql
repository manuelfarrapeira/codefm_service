CREATE TABLE IF NOT EXISTS classes
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    school_id     INT          NOT NULL,
    name          VARCHAR(100) NOT NULL,
    school_year   VARCHAR(100) NOT NULL,
    deletion_date DATE         NULL,
    CONSTRAINT classes_schools_id_fk
        FOREIGN KEY (school_id) REFERENCES schools (id)
);

