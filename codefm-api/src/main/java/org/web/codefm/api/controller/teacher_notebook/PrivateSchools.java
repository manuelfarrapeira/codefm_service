package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookSchoolsApi;
import org.web.codefm.api.mapper.SchoolDTOMapper;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.School;
import org.web.codefm.domain.usecase.teachernotebook.SchoolUseCase;
import org.web.codefm.model.SchoolDTO;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateSchools implements TeacherNoteBookSchoolsApi {

    private final SchoolUseCase schoolUseCase;
    private final SchoolDTOMapper schoolDTOMapper;

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SchoolDTO>> schools() {
        List<School> schools = schoolUseCase.getSchoolsByTeacher();
        List<SchoolDTO> dtos = schoolDTOMapper.toDTOList(schools);
        return ResponseEntity.ok(dtos);
    }
}
