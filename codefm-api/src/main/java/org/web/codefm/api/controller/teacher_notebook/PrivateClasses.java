package org.web.codefm.api.controller.teacher_notebook;

import java.util.List;

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

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteClass(Integer classId, String acceptLanguage) {
        classUseCase.softDeleteClass(classId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

  @Logged
  @Override
  @Locale(2)
  @PreAuthorize("hasRole('TEACHER')")
  public ResponseEntity<ClassDTO> updateClass(Integer classId, ClassRequestDTO classRequestDTO, String acceptLanguage) {
    Class updatedClass = classUseCase.updateClass(classId, classDTOMapper.toDomain(classRequestDTO));
    return ResponseEntity.ok(classDTOMapper.toDTO(updatedClass));
  }
}

