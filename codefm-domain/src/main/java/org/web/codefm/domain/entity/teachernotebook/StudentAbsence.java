package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class StudentAbsence {
	private Integer id;
	private Integer studentClassId;
	private Integer studentId;
	private String studentName;
	private String studentSurnames;
	private Integer classId;
	private Integer subjectId;
	private String subjectName;
	private LocalDate absenceDate;
}
