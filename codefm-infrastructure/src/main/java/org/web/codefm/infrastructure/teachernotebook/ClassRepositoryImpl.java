package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ClassRepositoryImpl implements ClassRepository {

    private final ClassJPARepository classJPARepository;
    private final ClassMapper classMapper;
    private final CacheEvictionService cacheEvictionService;

    @Override
    public List<Class> findActiveClassesBySchoolIdAndTeacherId(Integer schoolId, Integer teacherId) {
        return classMapper.toModelList(
                classJPARepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)
        );
    }

    @Override
    public Class save(Class clazz) {
        ClassEntity savedEntity = classJPARepository.save(classMapper.toEntity(clazz));
        this.cacheEvictionService.evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        return classMapper.toModel(savedEntity);
    }

    @Override
    public Optional<Class> findById(Integer classId) {
        return classJPARepository.findByIdAndDeletionDateIsNull(classId)
                .map(classMapper::toModel);
    }

    @Override
    public Optional<Class> findByIdAndTeacherIdAndDeletionDateIsNull(Integer classId, Integer teacherId) {
        return classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .map(classMapper::toModel);
    }

    @Override
    public Class softDeleteClass(Integer classId, Integer teacherId) {
        ClassEntity classEntity = classJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found or not owned by teacher or already deleted."));

        classEntity.setDeletionDate(LocalDate.now());
        ClassEntity updatedEntity = classJPARepository.save(classEntity);
        this.cacheEvictionService.evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        return classMapper.toModel(updatedEntity);
    }

    @Override
    public List<Integer> findActiveIdsBySchoolId(Integer schoolId) {
        return classJPARepository.findIdsBySchoolIdAndDeletionDateIsNull(schoolId);
    }

    @Override
    public void softDeleteBySchoolId(Integer schoolId) {
        classJPARepository.softDeleteBySchoolId(schoolId);
        this.cacheEvictionService.evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
    }
}
