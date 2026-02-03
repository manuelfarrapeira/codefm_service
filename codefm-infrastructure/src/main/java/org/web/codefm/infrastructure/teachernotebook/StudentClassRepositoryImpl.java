package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentClassJPARepository;
import org.web.codefm.infrastructure.mapper.StudentClassMapper;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudentClassRepositoryImpl implements StudentClassRepository {

    private final StudentClassJPARepository studentClassJPARepository;
    private final StudentClassMapper studentClassMapper;

    @Override
    public Optional<StudentClass> findByClassIdAndStudentId(Integer classId, Integer studentId) {
        return studentClassJPARepository.findByClassIdAndStudentId(classId, studentId)
                .map(studentClassMapper::toModel);
    }

    @Override
    public StudentClass save(StudentClass studentClass) {
        StudentClassEntity entity = studentClassMapper.toEntity(studentClass);
        StudentClassEntity savedEntity = studentClassJPARepository.save(entity);
        return studentClassMapper.toModel(savedEntity);
    }

    @Override
    public StudentClass update(StudentClass studentClass) {
        StudentClassEntity entity = studentClassMapper.toEntity(studentClass);
        StudentClassEntity updatedEntity = studentClassJPARepository.save(entity);
        return studentClassMapper.toModel(updatedEntity);
    }

    @Override
    public void softDelete(Integer classId, Integer studentId) {
        StudentClassEntity entity = studentClassJPARepository.findByClassIdAndStudentId(classId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student-Class association not found"));
        entity.setDeletionDate(LocalDate.now());
        studentClassJPARepository.save(entity);
    }
}

