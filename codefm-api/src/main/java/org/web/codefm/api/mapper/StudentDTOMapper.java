package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.model.StudentClassDTO;
import org.web.codefm.model.StudentDTO;
import org.web.codefm.model.StudentSchoolDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class StudentDTOMapper {

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    protected StudentClassRepository studentClassRepository;

    @Autowired
    protected ClassRepository classRepository;

    @Autowired
    protected SchoolRepository schoolRepository;

    @Mapping(target = "dateOfBirth", expression = "java(formatDate(student.getDateOfBirth()))")
    @Mapping(target = "classList", expression = "java(resolveStudentSchools(student.getId()))")
    public abstract StudentDTO toDTO(Student student);

    public abstract List<StudentDTO> toDTOList(List<Student> students);

    protected String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    protected List<StudentSchoolDTO> resolveStudentSchools(Integer studentId) {
        final List<Integer> classIds = this.studentClassRepository.findClassIdsByStudentId(studentId);

        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<Integer, List<String>> schoolClassesMap = new LinkedHashMap<>();

        for (final Integer classId : classIds) {
            final Optional<Class> clazz = this.classRepository.findById(classId);
            if (clazz.isPresent()) {
                final Integer schoolId = clazz.get().getSchoolId();
                final String className = clazz.get().getName();

                schoolClassesMap.computeIfAbsent(schoolId, k -> new ArrayList<>()).add(className);
            }
        }

        return schoolClassesMap.entrySet().stream()
                .map(entry -> {
                    final Integer schoolId = entry.getKey();
                    final List<String> classNames = entry.getValue();

                    final Optional<School> school = this.schoolRepository.findById(schoolId);
                    final String schoolName = school.map(School::getName).orElse("Unknown");

                    final StudentSchoolDTO schoolDTO = new StudentSchoolDTO();
                    schoolDTO.setSchoolName(schoolName);
                    schoolDTO.setClasses(
                            classNames.stream()
                                    .map(className -> {
                                        final StudentClassDTO classDTO = new StudentClassDTO();
                                        classDTO.setClassName(className);
                                        return classDTO;
                                    })
                                    .collect(Collectors.toList())
                    );

                    return schoolDTO;
                })
                .collect(Collectors.toList());
    }
}

