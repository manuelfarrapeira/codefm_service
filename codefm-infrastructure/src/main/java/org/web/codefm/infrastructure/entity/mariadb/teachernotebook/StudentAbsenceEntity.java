package org.web.codefm.infrastructure.entity.mariadb.teachernotebook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "student_absences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class StudentAbsenceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "student_class_id", nullable = false)
	private Integer studentClassId;

	@Column(name = "subject_id", nullable = false)
	private Integer subjectId;

	@Column(name = "absence_date", nullable = false)
	private LocalDate absenceDate;
}
