package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.web.codefm.api.TeacherNoteBookSavedStudentGroupsApi;
import org.web.codefm.api.mapper.SavedStudentGroupDTOMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;
import org.web.codefm.domain.usecase.teachernotebook.SavedStudentGroupUseCase;
import org.web.codefm.model.SavedStudentGroupBulkUpdateItemDTO;
import org.web.codefm.model.SavedStudentGroupDTO;
import org.web.codefm.model.SavedStudentGroupRequestDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateSavedStudentGroups implements TeacherNoteBookSavedStudentGroupsApi {

    private final SavedStudentGroupUseCase savedStudentGroupUseCase;
    private final SavedStudentGroupDTOMapper savedStudentGroupDTOMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SavedStudentGroupDTO>> getSavedStudentGroups(Integer classId, String acceptLanguage) {
        final List<SavedStudentGroup> groups = this.savedStudentGroupUseCase.getSavedGroupsByClassId(classId);
        return ResponseEntity.ok(this.savedStudentGroupDTOMapper.toDTOList(groups));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SavedStudentGroupDTO>> createSavedStudentGroups(
            Integer classId,
            List<SavedStudentGroupRequestDTO> savedStudentGroupRequestDTO,
            String acceptLanguage) {
        final List<SavedStudentGroup> groups = savedStudentGroupRequestDTO.stream()
                .map(this::toDomain)
                .toList();
        final List<SavedStudentGroup> created = this.savedStudentGroupUseCase.createSavedGroups(classId, groups);
        return new ResponseEntity<>(this.savedStudentGroupDTOMapper.toDTOList(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<SavedStudentGroupDTO>> updateAllSavedStudentGroups(
            Integer classId,
            List<SavedStudentGroupBulkUpdateItemDTO> savedStudentGroupBulkUpdateItemDTO,
            String acceptLanguage) {
        final List<SavedStudentGroup> groups = savedStudentGroupBulkUpdateItemDTO.stream()
                .map(this::toDomainForBulkUpdate)
                .toList();
        final List<SavedStudentGroup> updated = this.savedStudentGroupUseCase.updateAllSavedGroups(classId, groups);
        return ResponseEntity.ok(this.savedStudentGroupDTOMapper.toDTOList(updated));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteSavedStudentGroupsByClassId(Integer classId, String acceptLanguage) {
        this.savedStudentGroupUseCase.softDeleteSavedGroupsByClassId(classId);
        return ResponseEntity.noContent().build();
    }

    private SavedStudentGroup toDomain(SavedStudentGroupRequestDTO dto) {
        final List<SavedStudentGroupMember> members = dto.getStudentIds() == null
                ? List.of()
                : dto.getStudentIds().stream()
                .map(id -> SavedStudentGroupMember.builder().studentId(id).build())
                .toList();
        return SavedStudentGroup.builder()
                .name(dto.getName())
                .members(members)
                .build();
    }

    private SavedStudentGroup toDomainForBulkUpdate(SavedStudentGroupBulkUpdateItemDTO dto) {
        final List<SavedStudentGroupMember> members = dto.getStudentIds() == null
                ? List.of()
                : dto.getStudentIds().stream()
                .map(id -> SavedStudentGroupMember.builder().studentId(id).build())
                .toList();
        return SavedStudentGroup.builder()
                .id(dto.getId())
                .name(dto.getName())
                .members(members)
                .build();
    }
}
