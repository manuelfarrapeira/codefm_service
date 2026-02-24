package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.domain.repository.teachernotebook.ExerciseRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseDocumentJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseDocumentMapper;
import org.web.codefm.infrastructure.mapper.ExerciseMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ExerciseRepositoryImpl implements ExerciseRepository {

    private final ExerciseJPARepository exerciseJPARepository;
    private final SubjectClassJPARepository subjectClassJPARepository;
    private final SubjectJPARepository subjectJPARepository;
    private final ExerciseDocumentJPARepository exerciseDocumentJPARepository;
    private final ExerciseMapper exerciseMapper;
    private final ExerciseDocumentMapper exerciseDocumentMapper;

    @Override
    public List<Exercise> findByClassId(Integer classId) {
        List<SubjectClassEntity> subjectClassEntities = subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(classId);

        if (subjectClassEntities.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, Integer> subjectClassToSubjectId = subjectClassEntities.stream()
                .collect(Collectors.toMap(SubjectClassEntity::getId, SubjectClassEntity::getSubjectId));

        List<Integer> subjectClassIds = new ArrayList<>(subjectClassToSubjectId.keySet());

        List<Exercise> exercises = exerciseMapper.toModelList(
                exerciseJPARepository.findBySubjectClassIdInAndDeletionDateIsNull(subjectClassIds)
        );

        List<Integer> subjectIds = subjectClassEntities.stream()
                .map(SubjectClassEntity::getSubjectId)
                .distinct()
                .toList();

        Map<Integer, String> subjectNames = subjectJPARepository.findAllById(subjectIds).stream()
                .collect(Collectors.toMap(SubjectEntity::getId, SubjectEntity::getName));

        exercises.forEach(exercise -> {
            Integer subjectId = subjectClassToSubjectId.get(exercise.getSubjectClassId());
            exercise.setSubjectId(subjectId);
            exercise.setSubjectName(subjectNames.getOrDefault(subjectId, ""));
        });

        if (!exercises.isEmpty()) {
            List<Integer> exerciseIds = exercises.stream().map(Exercise::getId).toList();
            Map<Integer, List<ExerciseDocument>> documentsMap = exerciseDocumentMapper
                    .toModelList(exerciseDocumentJPARepository.findByExerciseIdIn(exerciseIds))
                    .stream()
                    .collect(Collectors.groupingBy(ExerciseDocument::getExerciseId));
            exercises.forEach(exercise ->
                    exercise.setDocuments(documentsMap.getOrDefault(exercise.getId(), List.of()))
            );
        }

        return exercises;
    }

    @Override
    public Optional<Exercise> findByIdAndTeacherId(Integer id, Integer teacherId) {
        return exerciseJPARepository.findByIdAndTeacherId(id, teacherId)
                .map(entity -> {
                    Exercise exercise = exerciseMapper.toModel(entity);
                    enrichWithSubjectData(exercise);
                    return exercise;
                });
    }

    @Override
    public Exercise save(Exercise exercise) {
        var entity = exerciseMapper.toEntity(exercise);
        var saved = exerciseJPARepository.save(entity);
        Exercise result = exerciseMapper.toModel(saved);
        enrichWithSubjectData(result);
        return result;
    }

    @Override
    public Exercise update(Exercise exercise) {
        return save(exercise);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) {
        exerciseJPARepository.softDeleteById(id);
    }

    @Override
    public boolean subjectClassBelongsToTeacher(Integer subjectClassId, Integer teacherId) {
        return exerciseJPARepository.subjectClassBelongsToTeacher(subjectClassId, teacherId);
    }

    @Override
    public void softDeleteBySubjectClassIds(List<Integer> subjectClassIds) {
        if (subjectClassIds != null && !subjectClassIds.isEmpty()) {
            exerciseJPARepository.softDeleteBySubjectClassIds(subjectClassIds);
        }
    }

    @Override
    public List<Integer> findActiveIdsBySubjectClassIds(List<Integer> subjectClassIds) {
        if (subjectClassIds == null || subjectClassIds.isEmpty()) {
            return List.of();
        }
        return exerciseJPARepository.findActiveIdsBySubjectClassIds(subjectClassIds);
    }

    private void enrichWithSubjectData(Exercise exercise) {
        var scOpt = subjectClassJPARepository.findById(exercise.getSubjectClassId());
        scOpt.ifPresent(sc -> {
            exercise.setSubjectId(sc.getSubjectId());
            subjectJPARepository.findById(sc.getSubjectId())
                    .ifPresent(subject -> exercise.setSubjectName(subject.getName()));
        });
        exercise.setDocuments(
                exerciseDocumentMapper.toModelList(
                        exerciseDocumentJPARepository.findByExerciseId(exercise.getId())
                )
        );
    }
}
