package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.StudentClass;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentClassJPARepository;
import org.web.codefm.infrastructure.mapper.StudentClassMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<Integer> findClassIdsByStudentId(Integer studentId) {
        return studentClassJPARepository.findClassIdsByStudentIdAndDeletionDateIsNull(studentId);
    }

    @Override
    public Map<Integer, List<Integer>> findClassIdsByTeacherId(Integer teacherId) {
        List<StudentClassEntity> entities = studentClassJPARepository.findAllByTeacherIdAndDeletionDateIsNull(teacherId);
        return entities.stream()
                .collect(Collectors.groupingBy(
                        StudentClassEntity::getStudentId,
                        Collectors.mapping(StudentClassEntity::getClassId, Collectors.toList())
                ));
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

    @Override
    public void softDeleteByClassId(Integer classId) {
        studentClassJPARepository.softDeleteByClassId(classId);
    }

    @Override
    public void softDeleteByStudentId(Integer studentId) {
        studentClassJPARepository.softDeleteByStudentId(studentId);
    }
}
