package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.api.TeacherNoteBookStudentsApi;
import org.web.codefm.api.mapper.StudentDTOMapper;
import org.web.codefm.api.mapper.StudentRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.usecase.teachernotebook.StudentUseCase;
import org.web.codefm.model.StudentDTO;
import org.web.codefm.model.StudentRequestDTO;
import org.web.codefm.model.UploadStudentPhoto200Response;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateStudents implements TeacherNoteBookStudentsApi {

    private final StudentUseCase studentUseCase;
    private final StudentDTOMapper studentDTOMapper;
    private final StudentRequestMapper studentRequestMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<StudentDTO> createStudent(StudentRequestDTO studentRequestDTO, String acceptLanguage) {
        Student createdStudent = studentUseCase.createStudent(studentRequestMapper.toDomain(studentRequestDTO));
        return new ResponseEntity<>(studentDTOMapper.toDTO(createdStudent), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<StudentDTO> updateStudent(Integer id, StudentRequestDTO studentRequestDTO, String acceptLanguage) {
        Student updatedStudent = studentUseCase.updateStudent(id, studentRequestMapper.toDomain(studentRequestDTO));
        return ResponseEntity.ok(studentDTOMapper.toDTO(updatedStudent));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteStudent(Integer id, String acceptLanguage) {
        studentUseCase.softDeleteStudent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Logged
    @Override
    @Locale(3)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<StudentDTO>> searchStudents(Integer id, String name, String surnames, String acceptLanguage) {
        List<Student> students = studentUseCase.searchStudents(id, name, surnames);
        return ResponseEntity.ok(studentDTOMapper.toDTOList(students));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<UploadStudentPhoto200Response> uploadStudentPhoto(Integer id, String acceptLanguage, MultipartFile file) {
        String photoPath = studentUseCase.uploadStudentPhoto(id, file);
        UploadStudentPhoto200Response response = new UploadStudentPhoto200Response();
        response.setPhotoPath(photoPath);
        return ResponseEntity.ok(response);
    }
}
