CREATE TABLE IF NOT EXISTS student_groups
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    class_id      INT          NOT NULL,
    name          VARCHAR(200) NOT NULL,
    deletion_date DATE         NULL,
    CONSTRAINT student_groups_classes_id_fk
        FOREIGN KEY (class_id) REFERENCES classes (id)
);

CREATE TABLE IF NOT EXISTS student_group_members
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    student_group_id INT NOT NULL,
    student_id       INT NOT NULL,
    CONSTRAINT student_group_members_group_fk
        FOREIGN KEY (student_group_id) REFERENCES student_groups (id),
    CONSTRAINT student_group_members_student_fk
        FOREIGN KEY (student_id) REFERENCES students (id)
);
