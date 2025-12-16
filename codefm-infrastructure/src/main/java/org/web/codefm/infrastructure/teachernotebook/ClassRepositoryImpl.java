package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ClassRepositoryImpl implements ClassRepository {

    private final ClassJPARepository classJPARepository;
    private final ClassMapper classMapper;

    @Override
    public List<Class> findActiveClassesBySchoolIdAndTeacherId(Integer schoolId, Integer teacherId) {
        return classMapper.toModelList(
                classJPARepository.findActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)
        );
    }
}

