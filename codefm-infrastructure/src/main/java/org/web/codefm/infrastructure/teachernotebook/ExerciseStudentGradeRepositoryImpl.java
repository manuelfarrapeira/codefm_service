package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.domain.repository.teachernotebook.ExerciseStudentGradeRepository;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.*;
import org.web.codefm.infrastructure.jpa.teachernotebook.*;
import org.web.codefm.infrastructure.mapper.ExerciseStudentDocumentMapper;
import org.web.codefm.infrastructure.mapper.ExerciseStudentGradeMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ExerciseStudentGradeRepositoryImpl implements ExerciseStudentGradeRepository {


    private final ExerciseStudentGradeJPARepository exerciseStudentGradeJPARepository;
    private final ExerciseJPARepository exerciseJPARepository;
    private final SubjectClassJPARepository subjectClassJPARepository;
    private final SubjectJPARepository subjectJPARepository;
    private final StudentJPARepository studentJPARepository;
    private final ExerciseStudentDocumentJPARepository exerciseStudentDocumentJPARepository;
    private final ExerciseStudentGradeMapper exerciseStudentGradeMapper;
    private final ExerciseStudentDocumentMapper exerciseStudentDocumentMapper;
    private final CacheEvictionService cacheEvictionService;

    @Override
    @Cacheable(value = CacheName.EXERCISE_STUDENT_GRADES_BY_CLASS, key = "#classId")
    public List<ExerciseStudentGrade> findByClassId(Integer classId) {
        List<Integer> exerciseIds = getActiveExerciseIdsByClassId(classId);
        if (exerciseIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ExerciseStudentGrade> grades = exerciseStudentGradeMapper.toModelList(
                exerciseStudentGradeJPARepository.findByExerciseIdInAndDeletionDateIsNull(exerciseIds)
        );

        return enrichGrades(grades).stream()
                .sorted(Comparator.comparing(ExerciseStudentGrade::getSubjectName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Override
    public List<ExerciseStudentGrade> findByClassIdAndStudentId(Integer classId, Integer studentId) {
        List<Integer> exerciseIds = getActiveExerciseIdsByClassId(classId);
        if (exerciseIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ExerciseStudentGrade> grades = exerciseStudentGradeMapper.toModelList(
                exerciseStudentGradeJPARepository.findByExerciseIdInAndStudentIdAndDeletionDateIsNull(exerciseIds, studentId)
        );

        return enrichGrades(grades).stream()
                .sorted(Comparator.comparing(ExerciseStudentGrade::getSubjectName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Override
    public Optional<ExerciseStudentGrade> findByIdAndTeacherId(Integer id, Integer teacherId) {
        return exerciseStudentGradeJPARepository.findByIdAndTeacherId(id, teacherId)
                .map(entity -> {
                    ExerciseStudentGrade grade = exerciseStudentGradeMapper.toModel(entity);
                    enrichSingleGrade(grade);
                    return grade;
                });
    }

    @Override
    public ExerciseStudentGrade save(ExerciseStudentGrade grade) {
        ExerciseStudentGradeEntity entity = exerciseStudentGradeMapper.toEntity(grade);
        ExerciseStudentGradeEntity saved = exerciseStudentGradeJPARepository.save(entity);
        ExerciseStudentGrade result = exerciseStudentGradeMapper.toModel(saved);
        enrichSingleGrade(result);
        this.evictCacheForExercises(List.of(grade.getExerciseId()));
        return result;
    }

    @Override
    public ExerciseStudentGrade update(ExerciseStudentGrade grade) {
        return save(grade);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) {
        this.exerciseStudentGradeJPARepository.findById(id)
                .ifPresent(entity -> this.evictCacheForExercises(List.of(entity.getExerciseId())));
        exerciseStudentGradeJPARepository.softDeleteById(id);
    }

    @Override
    public boolean existsByStudentIdAndExerciseIdAndDeletionDateIsNull(Integer studentId, Integer exerciseId) {
        return exerciseStudentGradeJPARepository.existsByStudentIdAndExerciseIdAndDeletionDateIsNull(studentId, exerciseId);
    }

    @Override
    public void softDeleteByExerciseIds(List<Integer> exerciseIds) {
        if (exerciseIds != null && !exerciseIds.isEmpty()) {
            this.evictCacheForExercises(exerciseIds);
            exerciseStudentGradeJPARepository.softDeleteByExerciseIds(exerciseIds);
        }
    }

    @Override
    public void softDeleteByStudentIdAndClassId(Integer studentId, Integer classId) {
        exerciseStudentGradeJPARepository.softDeleteByStudentIdAndClassId(studentId, classId);
        this.cacheEvictionService.evict(CacheName.EXERCISE_STUDENT_GRADES_BY_CLASS, classId);
    }

    @Override
    public void softDeleteByStudentId(Integer studentId) {
        final List<Integer> classIds = this.exerciseStudentGradeJPARepository
                .findDistinctClassIdsByStudentId(studentId);
        exerciseStudentGradeJPARepository.softDeleteByStudentId(studentId);
        classIds.forEach(classId -> this.cacheEvictionService.evict(CacheName.EXERCISE_STUDENT_GRADES_BY_CLASS, classId));
    }

    private List<Integer> getActiveExerciseIdsByClassId(Integer classId) {
        List<SubjectClassEntity> subjectClassEntities = subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(classId);
        if (subjectClassEntities.isEmpty()) {
            return List.of();
        }

        List<Integer> subjectClassIds = subjectClassEntities.stream()
                .map(SubjectClassEntity::getId)
                .toList();

        return exerciseJPARepository.findActiveIdsBySubjectClassIds(subjectClassIds);
    }

    private List<ExerciseStudentGrade> enrichGrades(List<ExerciseStudentGrade> grades) {
        if (grades.isEmpty()) {
            return grades;
        }

        List<Integer> exerciseIds = grades.stream().map(ExerciseStudentGrade::getExerciseId).distinct().toList();
        List<ExerciseEntity> exerciseEntities = exerciseJPARepository.findAllById(exerciseIds);
        Map<Integer, ExerciseEntity> exerciseMap = exerciseEntities.stream()
                .collect(Collectors.toMap(ExerciseEntity::getId, e -> e));

        List<Integer> subjectClassIds = exerciseEntities.stream()
                .map(ExerciseEntity::getSubjectClassId)
                .distinct()
                .toList();
        Map<Integer, SubjectClassEntity> scMap = subjectClassJPARepository.findAllById(subjectClassIds).stream()
                .collect(Collectors.toMap(SubjectClassEntity::getId, sc -> sc));

        List<Integer> subjectIds = scMap.values().stream()
                .map(SubjectClassEntity::getSubjectId)
                .distinct()
                .toList();
        Map<Integer, String> subjectNames = subjectJPARepository.findAllById(subjectIds).stream()
                .collect(Collectors.toMap(SubjectEntity::getId, SubjectEntity::getName));

        List<Integer> studentIds = grades.stream().map(ExerciseStudentGrade::getStudentId).distinct().toList();
        Map<Integer, StudentEntity> studentMap = studentJPARepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(StudentEntity::getId, s -> s));

        List<Integer> gradeIds = grades.stream().map(ExerciseStudentGrade::getId).toList();
        Map<Integer, List<ExerciseStudentDocument>> documentsMap = this.exerciseStudentDocumentMapper
                .toModelList(this.exerciseStudentDocumentJPARepository.findByGradeIdIn(gradeIds))
                .stream()
                .collect(Collectors.groupingBy(ExerciseStudentDocument::getGradeId));

        grades.forEach(grade -> {
            ExerciseEntity exercise = exerciseMap.get(grade.getExerciseId());
            if (exercise != null) {
                grade.setExerciseTitle(exercise.getTitle());
                grade.setQuarter(exercise.getQuarter());
                grade.setMaxGrade(exercise.getMaxGrade());
                grade.setPercentageGrade(exercise.getPercentageGrade());

                SubjectClassEntity sc = scMap.get(exercise.getSubjectClassId());
                if (sc != null) {
                    grade.setSubjectId(sc.getSubjectId());
                    grade.setSubjectName(subjectNames.getOrDefault(sc.getSubjectId(), ""));
                }
            }

            StudentEntity student = studentMap.get(grade.getStudentId());
            if (student != null) {
                grade.setStudentName(student.getName());
                grade.setStudentSurnames(student.getSurnames());
            }

            grade.setDocuments(documentsMap.getOrDefault(grade.getId(), List.of()));
        });

        return grades;
    }

    private void enrichSingleGrade(ExerciseStudentGrade grade) {
        exerciseJPARepository.findById(grade.getExerciseId()).ifPresent(exercise -> {
            grade.setExerciseTitle(exercise.getTitle());
            grade.setQuarter(exercise.getQuarter());
            grade.setMaxGrade(exercise.getMaxGrade());
            grade.setPercentageGrade(exercise.getPercentageGrade());

            subjectClassJPARepository.findById(exercise.getSubjectClassId()).ifPresent(sc -> {
                grade.setSubjectId(sc.getSubjectId());
                subjectJPARepository.findById(sc.getSubjectId())
                        .ifPresent(subject -> grade.setSubjectName(subject.getName()));
            });
        });

        studentJPARepository.findById(grade.getStudentId()).ifPresent(student -> {
            grade.setStudentName(student.getName());
            grade.setStudentSurnames(student.getSurnames());
        });

        grade.setDocuments(this.exerciseStudentDocumentMapper.toModelList(
                this.exerciseStudentDocumentJPARepository.findByGradeId(grade.getId())));
    }

    private void evictCacheForExercises(List<Integer> exerciseIds) {
        this.exerciseJPARepository.findDistinctClassIdsByExerciseIds(exerciseIds)
                .forEach(classId -> this.cacheEvictionService.evict(CacheName.EXERCISE_STUDENT_GRADES_BY_CLASS, classId));
    }
}
