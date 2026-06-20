CREATE TABLE IF NOT EXISTS skills
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    description   VARCHAR(200) NOT NULL,
    id_teacher    INT          NOT NULL,
    deletion_date DATE         NULL
);
