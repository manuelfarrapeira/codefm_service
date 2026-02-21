package org.web.codefm.api.controller.teacher_notebook;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookSchoolsApi;
import org.web.codefm.api.mapper.SchoolDTOMapper;
import org.web.codefm.api.mapper.SchoolRequestMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.usecase.teachernotebook.SchoolUseCase;
import org.web.codefm.model.SchoolDTO;
import org.web.codefm.model.SchoolRequestDTO;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateSchools implements TeacherNoteBookSchoolsApi {

    private final SchoolUseCase schoolUseCase;
    private final SchoolDTOMapper schoolDTOMapper;
    private final SchoolRequestMapper schoolRequestMapper;

    @Logged
    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SchoolDTO>> schools() {
      return ResponseEntity.ok(schoolDTOMapper.toDTOList(schoolUseCase.getSchoolsByTeacher()));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SchoolDTO> createSchool(SchoolRequestDTO createSchoolRequestDTO, String acceptLanguage) {

      School createdSchool = schoolUseCase.createSchool(schoolRequestMapper.toDomain(createSchoolRequestDTO));

        return new ResponseEntity<>(schoolDTOMapper.toDTO(createdSchool), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteSchool(Integer id, String acceptLanguage) {
        schoolUseCase.softDeleteSchool(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SchoolDTO> updateSchool(Integer id, SchoolRequestDTO updateSchoolRequestDTO, String acceptLanguage) {
      School updatedSchool = schoolUseCase.updateSchool(id, schoolRequestMapper.toDomain(updateSchoolRequestDTO));
        return ResponseEntity.ok(schoolDTOMapper.toDTO(updatedSchool));
    }


}
