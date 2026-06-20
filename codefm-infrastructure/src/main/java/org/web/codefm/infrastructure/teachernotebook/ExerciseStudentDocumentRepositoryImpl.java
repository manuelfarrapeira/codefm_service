package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.domain.repository.teachernotebook.ExerciseStudentDocumentRepository;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseStudentDocumentJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseStudentGradeJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseStudentDocumentMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExerciseStudentDocumentRepositoryImpl implements ExerciseStudentDocumentRepository {


    private final ExerciseStudentDocumentJPARepository exerciseStudentDocumentJPARepository;
    private final ExerciseStudentDocumentMapper exerciseStudentDocumentMapper;
    private final ExerciseStudentGradeJPARepository exerciseStudentGradeJPARepository;
    private final ExerciseJPARepository exerciseJPARepository;
    private final CacheEvictionService cacheEvictionService;

    @Override
    public ExerciseStudentDocument save(ExerciseStudentDocument document) {
        final var entity = this.exerciseStudentDocumentMapper.toEntity(document);
        final var saved = this.exerciseStudentDocumentJPARepository.save(entity);
        this.evictCacheForGrades(List.of(document.getGradeId()));
        return this.exerciseStudentDocumentMapper.toModel(saved);
    }

    @Override
    public ExerciseStudentDocument update(ExerciseStudentDocument document) {
        final var entity = this.exerciseStudentDocumentMapper.toEntity(document);
        final var updated = this.exerciseStudentDocumentJPARepository.save(entity);
        this.evictCacheForGrades(List.of(document.getGradeId()));
        return this.exerciseStudentDocumentMapper.toModel(updated);
    }

    @Override
    public List<ExerciseStudentDocument> findByGradeId(Integer gradeId) {
        return this.exerciseStudentDocumentMapper.toModelList(
                this.exerciseStudentDocumentJPARepository.findByGradeId(gradeId)
        );
    }

    @Override
    public Optional<ExerciseStudentDocument> findById(Integer id) {
        return this.exerciseStudentDocumentJPARepository.findById(id)
                .map(this.exerciseStudentDocumentMapper::toModel);
    }

    @Override
    public void deleteById(Integer id) {
        this.exerciseStudentDocumentJPARepository.findById(id)
                .ifPresent(entity -> this.evictCacheForGrades(List.of(entity.getGradeId())));
        this.exerciseStudentDocumentJPARepository.deleteById(id);
    }

    @Override
    public void deleteByGradeId(Integer gradeId) {
        this.evictCacheForGrades(List.of(gradeId));
        this.exerciseStudentDocumentJPARepository.deleteByGradeId(gradeId);
    }

    @Override
    public void deleteByGradeIds(List<Integer> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return;
        }
        this.evictCacheForGrades(gradeIds);
        this.exerciseStudentDocumentJPARepository.deleteByGradeIdIn(gradeIds);
    }

    @Override
    public List<ExerciseStudentDocument> findByGradeIds(List<Integer> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return List.of();
        }
        return this.exerciseStudentDocumentMapper.toModelList(
                this.exerciseStudentDocumentJPARepository.findByGradeIdIn(gradeIds)
        );
    }

    @Override
    public List<ExerciseStudentDocument> findByExerciseId(Integer exerciseId) {
        return this.exerciseStudentDocumentMapper.toModelList(
                this.exerciseStudentDocumentJPARepository.findByExerciseId(exerciseId)
        );
    }

    @Override
    public List<ExerciseStudentDocument> findByExerciseIds(List<Integer> exerciseIds) {
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return List.of();
        }
        return this.exerciseStudentDocumentMapper.toModelList(
                this.exerciseStudentDocumentJPARepository.findByExerciseIdIn(exerciseIds)
        );
    }

    @Override
    public void deleteByExerciseId(Integer exerciseId) {
        this.evictCacheForExercises(List.of(exerciseId));
        this.exerciseStudentDocumentJPARepository.deleteByExerciseId(exerciseId);
    }

    @Override
    public void deleteByExerciseIds(List<Integer> exerciseIds) {
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return;
        }
        this.evictCacheForExercises(exerciseIds);
        this.exerciseStudentDocumentJPARepository.deleteByExerciseIdIn(exerciseIds);
    }

    @Override
    public List<ExerciseStudentDocument> findByStudentId(Integer studentId) {
        return this.exerciseStudentDocumentMapper.toModelList(
                this.exerciseStudentDocumentJPARepository.findByStudentId(studentId)
        );
    }

    @Override
    public void deleteByStudentId(Integer studentId) {
        final List<Integer> classIds = this.exerciseStudentGradeJPARepository
                .findDistinctClassIdsByStudentId(studentId);
        this.exerciseStudentDocumentJPARepository.deleteByStudentId(studentId);
        classIds.forEach(classId -> this.cacheEvictionService.evict(CacheName.EXERCISE_STUDENT_GRADES_BY_CLASS, classId));
    }

    @Override
    public List<ExerciseStudentDocument> findByStudentIdAndClassId(Integer studentId, Integer classId) {
        return this.exerciseStudentDocumentMapper.toModelList(
                this.exerciseStudentDocumentJPARepository.findByStudentIdAndClassId(studentId, classId)
        );
    }

    @Override
    public void deleteByStudentIdAndClassId(Integer studentId, Integer classId) {
        this.exerciseStudentDocumentJPARepository.deleteByStudentIdAndClassId(studentId, classId);
        this.cacheEvictionService.evict(CacheName.EXERCISE_STUDENT_GRADES_BY_CLASS, classId);
    }

    private void evictCacheForGrades(List<Integer> gradeIds) {
        this.exerciseStudentGradeJPARepository.findDistinctClassIdsByGradeIds(gradeIds)
                .forEach(classId -> this.cacheEvictionService.evict(CacheName.EXERCISE_STUDENT_GRADES_BY_CLASS, classId));
    }

    private void evictCacheForExercises(List<Integer> exerciseIds) {
        this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(exerciseIds)
                .forEach(classId -> this.cacheEvictionService.evict(CacheName.EXERCISE_STUDENT_GRADES_BY_CLASS, classId));
    }
}
