CREATE TABLE IF NOT EXISTS group_assignments
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    class_id      INT          NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT         NULL,
    quarter       INT          NOT NULL,
    deletion_date DATE         NULL,
    CONSTRAINT group_assignments_classes_id_fk
        FOREIGN KEY (class_id) REFERENCES classes (id)
);

CREATE TABLE IF NOT EXISTS group_assignment_grades
(
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    group_assignment_id INT           NOT NULL,
    group_id            INT           NOT NULL,
    grade               DECIMAL(4, 2) NOT NULL,
    deletion_date       DATE          NULL,
    CONSTRAINT group_assignment_grades_assignment_fk
        FOREIGN KEY (group_assignment_id) REFERENCES group_assignments (id),
    CONSTRAINT group_assignment_grades_group_fk
        FOREIGN KEY (group_id) REFERENCES student_groups (id)
);

CREATE TABLE IF NOT EXISTS group_assignment_documents
(
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    group_assignment_id INT          NOT NULL,
    group_id            INT          NULL,
    document            VARCHAR(255) NOT NULL,
    description         TEXT         NULL,
    group_document      BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT group_assignment_documents_assignment_fk
        FOREIGN KEY (group_assignment_id) REFERENCES group_assignments (id),
    CONSTRAINT group_assignment_documents_group_fk
        FOREIGN KEY (group_id) REFERENCES student_groups (id)
);

