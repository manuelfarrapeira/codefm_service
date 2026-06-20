package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentJPARepository;
import org.web.codefm.infrastructure.mapper.StudentMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StudentRepositoryImpl implements StudentRepository {

    private final StudentJPARepository studentJPARepository;
    private final StudentMapper studentMapper;
    private final CacheEvictionService cacheEvictionService;

    @Override
    public Student save(Student student) {
        StudentEntity studentEntity = studentMapper.toEntity(student);
        StudentEntity savedEntity = studentJPARepository.save(studentEntity);
        this.cacheEvictionService.evictByTeacher(CacheName.STUDENTS_BY_TEACHER);
        return studentMapper.toModel(savedEntity);
    }

    @Override
    public Optional<Student> findByIdAndTeacherIdAndDeletionDateIsNull(Integer id, Integer teacherId) {
        return studentJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId)
                .map(studentMapper::toModel);
    }

    @Override
    public Student update(Student student) {
        StudentEntity studentEntity = studentMapper.toEntity(student);
        StudentEntity updatedEntity = studentJPARepository.save(studentEntity);
        this.cacheEvictionService.evictByTeacher(CacheName.STUDENTS_BY_TEACHER);
        return studentMapper.toModel(updatedEntity);
    }

    @Override
    public Student softDelete(Integer id, Integer teacherId) {
        StudentEntity studentEntity = studentJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found or not owned by teacher or already deleted."));

        studentEntity.setDeletionDate(LocalDate.now());
        StudentEntity updatedEntity = studentJPARepository.save(studentEntity);
        this.cacheEvictionService.evictByTeacher(CacheName.STUDENTS_BY_TEACHER);
        return studentMapper.toModel(updatedEntity);
    }

    @Override
    public List<Student> searchStudents(Integer teacherId, Integer id, String name, String surnames) {
        return studentMapper.toModelList(
                studentJPARepository.searchStudents(teacherId, id, name, surnames)
        );
    }

    @Override
    @Cacheable(value = CacheName.STUDENTS_BY_TEACHER, key = "#teacherId")
    public List<Student> findAllByTeacherId(Integer teacherId) {
        return studentMapper.toModelList(
                studentJPARepository.findAllByTeacherIdAndDeletionDateIsNull(teacherId)
        );
    }

    @Override
    public List<Student> findByIdsAndTeacherIdAndDeletionDateIsNull(List<Integer> ids, Integer teacherId) {
        return this.studentMapper.toModelList(
                this.studentJPARepository.findByIdInAndTeacherIdAndDeletionDateIsNull(ids, teacherId)
        );
    }

}
