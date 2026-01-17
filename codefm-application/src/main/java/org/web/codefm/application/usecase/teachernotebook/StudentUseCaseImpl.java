package org.web.codefm.application.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.service.teachernotebook.StudentService;
import org.web.codefm.domain.usecase.teachernotebook.StudentUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentUseCaseImpl implements StudentUseCase {

    private final StudentService studentService;

    @Override
    public Student createStudent(Student student) {
        return studentService.createStudent(student);
    }

    @Override
    public Student updateStudent(Integer id, Student student) {
        return studentService.updateStudent(id, student);
    }

    @Override
    public void softDeleteStudent(Integer id) {
        studentService.softDeleteStudent(id);
    }

    @Override
    public String uploadStudentPhoto(Integer studentId, MultipartFile file) {
        return studentService.saveStudentPhoto(studentId, file);
    }

    @Override
    public List<Student> searchStudents(Integer id, String name, String surnames) {
        return studentService.searchStudents(id, name, surnames);
    }
}
