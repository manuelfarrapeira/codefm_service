CREATE TABLE IF NOT EXISTS skill_rubrics
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    id_skill      INT          NOT NULL,
    deletion_date DATE         NULL,
    CONSTRAINT fk_skill_rubric_skill FOREIGN KEY (id_skill) REFERENCES skills (id)
);

CREATE TABLE IF NOT EXISTS skill_rubric_criteria
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    description   TEXT NOT NULL,
    id_rubric     INT  NOT NULL,
    grade_start   INT  NOT NULL,
    grade_end     INT  NOT NULL,
    deletion_date DATE NULL,
    CONSTRAINT fk_rubric_criteria_rubric FOREIGN KEY (id_rubric) REFERENCES skill_rubrics (id),
    CONSTRAINT chk_grade_start_range CHECK (grade_start >= 0 AND grade_start <= 10),
    CONSTRAINT chk_grade_end_range CHECK (grade_end >= 0 AND grade_end <= 10),
    CONSTRAINT chk_grade_start_le_end CHECK (grade_start <= grade_end)
);

