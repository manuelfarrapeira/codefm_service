CREATE TABLE IF NOT EXISTS subjects
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50) NOT NULL,
    id_teacher    INT         NOT NULL,
    deletion_date DATE        NULL
);

