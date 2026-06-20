CREATE TABLE IF NOT EXISTS schools
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    teacher_id    INT          NOT NULL,
    name          VARCHAR(200) NOT NULL,
    town          VARCHAR(200) NULL,
    tlf INT NULL,
    deletion_date DATE         NULL
);

