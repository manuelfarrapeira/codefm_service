package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookClassesApi;
import org.web.codefm.api.mapper.ClassDTOMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.usecase.teachernotebook.ClassUseCase;
import org.web.codefm.model.ClassDTO;
import org.web.codefm.model.ClassRequestDTO;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateClasses implements TeacherNoteBookClassesApi {

    private final ClassUseCase classUseCase;
    private final ClassDTOMapper classDTOMapper;

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassDTO>> classes(Integer schoolId) {
        List<Class> classes = classUseCase.getClassesBySchoolId(schoolId);
        return ResponseEntity.ok(classDTOMapper.toDTOList(classes));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassDTO> createClass(Integer schoolId, ClassRequestDTO classRequestDTO, String acceptLanguage) {
        Class clazz = classDTOMapper.toDomain(classRequestDTO);
        clazz.setSchoolId(schoolId);
        return new ResponseEntity<>(classDTOMapper.toDTO(classUseCase.createClass(clazz)), HttpStatus.CREATED);
    }
}

