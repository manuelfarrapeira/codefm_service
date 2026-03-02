CREATE TABLE IF NOT EXISTS calendar_alerts
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    teacher_id  INT          NOT NULL,
    date        DATE         NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    start_time  TIME         NULL,
    end_time    TIME         NULL
);

