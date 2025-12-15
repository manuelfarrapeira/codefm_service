package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SchoolJPARepository;
import org.web.codefm.infrastructure.mapper.SchoolMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SchoolRepositoryImpl implements SchoolRepository {

    private final SchoolJPARepository schoolJPARepository;
    private final SchoolMapper schoolMapper;

    @Override
    public List<School> findByTeacherId(Integer teacherId) {
        return schoolMapper.toModelList(schoolJPARepository.findByTeacherId(teacherId));
    }
}
