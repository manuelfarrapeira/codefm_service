package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.repository.teachernotebook.SchoolRepository;
import org.web.codefm.domain.service.teachernotebook.SchoolService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;

    @Override
    public List<School> getSchoolsByTeacherId(Integer teacherId) {
        return schoolRepository.findByTeacherId(teacherId);
    }
}
