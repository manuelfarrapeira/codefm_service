package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SchoolJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;
import org.web.codefm.infrastructure.mapper.SchoolMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SchoolRepositoryImpl implements SchoolRepository {

    private final SchoolJPARepository schoolJPARepository;
    private final ClassJPARepository classJPARepository;
    private final SchoolMapper schoolMapper;
    private final ClassMapper classMapper;

    @Override
    public List<School> findByTeacherId(Integer teacherId) {
        List<School> schools = schoolMapper.toModelList(schoolJPARepository.findByTeacherId(teacherId));

        schools.forEach(school -> school.setClasses(
                classMapper.toModelList(
                        classJPARepository.findActiveClassesBySchoolIdAndTeacherId(school.getId(), teacherId)
                )
        ));

        return schools;
    }

    @Override
    public School save(School school) {
        SchoolEntity schoolEntity = schoolMapper.toEntity(school);
        SchoolEntity savedEntity = schoolJPARepository.save(schoolEntity);
        return schoolMapper.toModel(savedEntity);
    }

    @Override
    public Optional<School> findById(Integer schoolId) {
        return schoolJPARepository.findByIdAndDeletionDateIsNull(schoolId)
                .map(schoolMapper::toModel);
    }

    @Override
    public School softDeleteSchool(Integer schoolId, Integer teacherId) {
        SchoolEntity schoolEntity = schoolJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("School not found or not owned by teacher or already deleted."));

        schoolEntity.setDeletionDate(LocalDate.now());
        SchoolEntity updatedEntity = schoolJPARepository.save(schoolEntity);
        return schoolMapper.toModel(updatedEntity);
    }
}
