package org.web.codefm.infrastructure.jpa.teachernotebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ExerciseDocumentEntity;

import java.util.List;

@Repository
public interface ExerciseDocumentJPARepository extends JpaRepository<ExerciseDocumentEntity, Integer> {

    List<ExerciseDocumentEntity> findByExerciseId(Integer exerciseId);

    List<ExerciseDocumentEntity> findByExerciseIdIn(List<Integer> exerciseIds);

    @Modifying
    @Transactional
    void deleteByExerciseId(Integer exerciseId);

    @Modifying
    @Transactional
    void deleteByExerciseIdIn(List<Integer> exerciseIds);
}

