package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "calendar_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class CalendarAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;
}

