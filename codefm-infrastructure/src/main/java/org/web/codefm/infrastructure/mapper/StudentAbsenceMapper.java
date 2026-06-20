package org.web.codefm.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentAbsenceEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentAbsenceMapper {

	@Mapping(target = "studentId", ignore = true)
	@Mapping(target = "classId", ignore = true)
	@Mapping(target = "studentName", ignore = true)
	@Mapping(target = "studentSurnames", ignore = true)
	@Mapping(target = "subjectName", ignore = true)
	StudentAbsence toModel(StudentAbsenceEntity entity);

	List<StudentAbsence> toModelList(List<StudentAbsenceEntity> entities);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "studentClassId", source = "studentClassId")
	@Mapping(target = "subjectId", source = "subjectId")
	@Mapping(target = "absenceDate", source = "absenceDate")
	StudentAbsenceEntity toEntity(StudentAbsence absence);

	List<StudentAbsenceEntity> toEntityList(List<StudentAbsence> absences);
}
