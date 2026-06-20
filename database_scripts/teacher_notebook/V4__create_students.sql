CREATE TABLE IF NOT EXISTS students
(
    id             INT AUTO_INCREMENT PRIMARY KEY,
    teacher_id     INT          NOT NULL,
    name           VARCHAR(60)  NOT NULL,
    surnames       VARCHAR(100) NOT NULL,
    gender CHAR NOT NULL,
    date_of_birth  DATE         NULL,
    aditional_info TEXT         NULL,
    photo          VARCHAR(100) NULL,
    shape VARCHAR(20) NULL,
    deletion_date  DATE         NULL
);

