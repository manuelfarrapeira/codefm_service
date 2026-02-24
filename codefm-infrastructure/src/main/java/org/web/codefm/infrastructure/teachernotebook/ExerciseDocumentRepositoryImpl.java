package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.domain.repository.teachernotebook.ExerciseDocumentRepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ExerciseDocumentJPARepository;
import org.web.codefm.infrastructure.mapper.ExerciseDocumentMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExerciseDocumentRepositoryImpl implements ExerciseDocumentRepository {

    private final ExerciseDocumentJPARepository exerciseDocumentJPARepository;
    private final ExerciseDocumentMapper exerciseDocumentMapper;

    @Override
    public ExerciseDocument save(ExerciseDocument exerciseDocument) {
        var entity = exerciseDocumentMapper.toEntity(exerciseDocument);
        var saved = exerciseDocumentJPARepository.save(entity);
        return exerciseDocumentMapper.toModel(saved);
    }

    @Override
    public ExerciseDocument update(ExerciseDocument exerciseDocument) {
        var entity = exerciseDocumentMapper.toEntity(exerciseDocument);
        var updated = exerciseDocumentJPARepository.save(entity);
        return exerciseDocumentMapper.toModel(updated);
    }

    @Override
    public List<ExerciseDocument> findByExerciseId(Integer exerciseId) {
        return exerciseDocumentMapper.toModelList(
                exerciseDocumentJPARepository.findByExerciseId(exerciseId)
        );
    }

    @Override
    public Optional<ExerciseDocument> findById(Integer id) {
        return exerciseDocumentJPARepository.findById(id)
                .map(exerciseDocumentMapper::toModel);
    }

    @Override
    public void deleteById(Integer id) {
        exerciseDocumentJPARepository.deleteById(id);
    }

    @Override
    public void deleteByExerciseId(Integer exerciseId) {
        exerciseDocumentJPARepository.deleteByExerciseId(exerciseId);
    }

    @Override
    public void deleteByExerciseIds(List<Integer> exerciseIds) {
        if (exerciseIds != null && !exerciseIds.isEmpty()) {
            exerciseDocumentJPARepository.deleteByExerciseIdIn(exerciseIds);
        }
    }

    @Override
    public List<ExerciseDocument> findByExerciseIds(List<Integer> exerciseIds) {
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return List.of();
        }
        return exerciseDocumentMapper.toModelList(
                exerciseDocumentJPARepository.findByExerciseIdIn(exerciseIds)
        );
    }
}

