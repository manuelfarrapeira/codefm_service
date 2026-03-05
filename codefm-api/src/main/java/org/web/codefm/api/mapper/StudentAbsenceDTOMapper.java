package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.model.StudentAbsenceDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentAbsenceDTOMapper {

	DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Mapping(target = "absenceDate", expression = "java(formatDate(absence.getAbsenceDate()))")
	StudentAbsenceDTO toDTO(StudentAbsence absence);

	List<StudentAbsenceDTO> toDTOList(List<StudentAbsence> absences);

	default String formatDate(LocalDate date) {
		return date != null ? date.format(DATE_FORMATTER) : null;
	}
}
