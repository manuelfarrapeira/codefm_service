package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.model.StudentDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentDTOMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Mapping(target = "dateOfBirth", expression = "java(formatDate(student.getDateOfBirth()))")
    StudentDTO toDTO(Student student);

    List<StudentDTO> toDTOList(List<Student> students);

    default String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
}

