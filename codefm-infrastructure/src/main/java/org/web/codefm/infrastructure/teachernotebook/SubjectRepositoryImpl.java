package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.Subject;
import org.web.codefm.domain.repository.teachernotebook.SubjectRepository;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.SubjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SubjectRepositoryImpl implements SubjectRepository {

    private final SubjectJPARepository subjectJPARepository;
    private final SubjectMapper subjectMapper;
    private final SubjectClassJPARepository subjectClassJPARepository;
    private final CacheEvictionService cacheEvictionService;

    @Override
    public List<Subject> findByTeacherId(Integer teacherId) {
        return subjectMapper.toModelList(subjectJPARepository.findByTeacherId(teacherId));
    }

    @Override
    public Subject save(Subject subject) {
        SubjectEntity subjectEntity = subjectMapper.toEntity(subject);
        SubjectEntity savedEntity = subjectJPARepository.save(subjectEntity);
        this.evictSubjectClassesCache(savedEntity.getId());
        this.cacheEvictionService.evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        return subjectMapper.toModel(savedEntity);
    }

    @Override
    public Optional<Subject> findById(Integer subjectId) {
        return subjectJPARepository.findByIdAndDeletionDateIsNull(subjectId)
                .map(subjectMapper::toModel);
    }

    @Override
    public Optional<Subject> findByIdAndTeacherId(Integer subjectId, Integer teacherId) {
        return subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId)
                .map(subjectMapper::toModel);
    }

    @Override
    public Subject softDeleteSubject(Integer subjectId, Integer teacherId) {
        List<Integer> classIds = subjectClassJPARepository.findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(subjectId);
        SubjectEntity subjectEntity = subjectJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(subjectId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found or not owned by teacher or already deleted."));

        subjectEntity.setDeletionDate(LocalDate.now());
        SubjectEntity updatedEntity = subjectJPARepository.save(subjectEntity);
        classIds.forEach(classId -> this.cacheEvictionService.evict(CacheName.SUBJECT_CLASSES_BY_CLASS, classId));
        this.cacheEvictionService.evictByTeacher(CacheName.CLASSES_WITH_SUBJECTS_BY_TEACHER);
        return subjectMapper.toModel(updatedEntity);
    }

    private void evictSubjectClassesCache(Integer subjectId) {
        List<Integer> classIds = subjectClassJPARepository.findDistinctClassIdsBySubjectIdAndDeletionDateIsNull(subjectId);
        classIds.forEach(classId -> this.cacheEvictionService.evict(CacheName.SUBJECT_CLASSES_BY_CLASS, classId));
    }
}
