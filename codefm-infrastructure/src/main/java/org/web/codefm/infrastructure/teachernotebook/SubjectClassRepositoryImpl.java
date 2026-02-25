package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.ClassWithSubjects;
import org.web.codefm.domain.entity.teachernotebook.SubjectClass;
import org.web.codefm.domain.entity.teachernotebook.SubjectClassDetail;
import org.web.codefm.domain.repository.teachernotebook.SubjectClassRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;
import org.web.codefm.infrastructure.mapper.SubjectClassMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SubjectClassRepositoryImpl implements SubjectClassRepository {

    private final SubjectClassJPARepository subjectClassJPARepository;
    private final SubjectJPARepository subjectJPARepository;
    private final ClassJPARepository classJPARepository;
    private final SubjectClassMapper subjectClassMapper;
    private final ClassMapper classMapper;

    @Override
    public List<SubjectClassDetail> findSubjectsByClassId(Integer classId) {
        List<SubjectClassEntity> subjectClassEntities = subjectClassJPARepository.findByClassIdAndDeletionDateIsNull(classId);

        if (subjectClassEntities.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> subjectIds = subjectClassEntities.stream()
                .map(SubjectClassEntity::getSubjectId)
                .toList();

        Map<Integer, SubjectEntity> subjectMap = subjectJPARepository.findAllById(subjectIds).stream()
                .filter(s -> s.getDeletionDate() == null)
                .collect(Collectors.toMap(SubjectEntity::getId, Function.identity()));

        return subjectClassEntities.stream()
                .filter(sc -> subjectMap.containsKey(sc.getSubjectId()))
                .map(sc -> {
                    SubjectEntity subject = subjectMap.get(sc.getSubjectId());
                    return SubjectClassDetail.builder()
                            .subjectClassId(sc.getId())
                            .subjectId(subject.getId())
                            .subjectName(subject.getName())
                            .build();
                })
                .toList();
    }

    @Override
    public List<SubjectClass> saveAll(List<SubjectClass> subjectClasses) {
        List<SubjectClassEntity> entities = subjectClassMapper.toEntityList(subjectClasses);
        List<SubjectClassEntity> savedEntities = subjectClassJPARepository.saveAll(entities);
        return subjectClassMapper.toModelList(savedEntities);
    }

    @Override
    @Transactional
    public void softDeleteAll(Integer classId, List<Integer> subjectIds) {
        subjectClassJPARepository.softDeleteByClassIdAndSubjectIds(classId, subjectIds);
    }

    @Override
    public boolean existsBySubjectIdAndClassIdAndDeletionDateIsNull(Integer subjectId, Integer classId) {
        return subjectClassJPARepository.findBySubjectIdAndClassIdAndDeletionDateIsNull(subjectId, classId).isPresent();
    }

    @Override
    public List<ClassWithSubjects> findAllClassesWithSubjectsByTeacherId(Integer teacherId) {
        List<Integer> classIds = subjectClassJPARepository.findClassIdsByTeacherId(teacherId);

        if (classIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ClassEntity> classEntities = classJPARepository.findAllById(classIds).stream()
                .filter(c -> c.getDeletionDate() == null)
                .toList();

        Map<Integer, List<SubjectClassDetail>> subjectsByClassId = classIds.stream()
                .collect(Collectors.toMap(
                        classId -> classId,
                        this::findSubjectsByClassId
                ));

        return classEntities.stream()
                .map(classEntity -> {
                    Class classData = classMapper.toModel(classEntity);
                    List<SubjectClassDetail> subjects = subjectsByClassId.getOrDefault(classEntity.getId(), new ArrayList<>());
                    return ClassWithSubjects.builder()
                            .classData(classData)
                            .subjects(subjects)
                            .build();
                })
                .toList();
    }

    @Override
    public void softDeleteByClassId(Integer classId) {
        subjectClassJPARepository.softDeleteByClassId(classId);
    }

    @Override
    public void softDeleteBySubjectId(Integer subjectId) {
        subjectClassJPARepository.softDeleteBySubjectId(subjectId);
    }

    @Override
    public List<Integer> findActiveIdsByClassId(Integer classId) {
        return subjectClassJPARepository.findIdsByClassIdAndDeletionDateIsNull(classId);
    }

    @Override
    public List<Integer> findActiveIdsBySubjectId(Integer subjectId) {
        return subjectClassJPARepository.findIdsBySubjectIdAndDeletionDateIsNull(subjectId);
    }

    @Override
    public Optional<Integer> findIdBySubjectIdAndClassId(Integer subjectId, Integer classId) {
        return subjectClassJPARepository.findBySubjectIdAndClassIdAndDeletionDateIsNull(subjectId, classId)
                .map(SubjectClassEntity::getId);
    }

    @Override
    public Optional<SubjectClass> findById(Integer id) {
        return subjectClassJPARepository.findById(id)
                .map(subjectClassMapper::toModel);
    }
}
