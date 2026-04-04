package org.web.codefm.api.controller.teacher_notebook;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.api.TeacherNoteBookGroupAssignmentsApi;
import org.web.codefm.api.mapper.GroupAssignmentDTOMapper;
import org.web.codefm.api.mapper.GroupAssignmentDocumentDTOMapper;
import org.web.codefm.api.mapper.GroupAssignmentGradeDTOMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.domain.enums.MimeTypeEnum;
import org.web.codefm.domain.usecase.teachernotebook.GroupAssignmentDocumentUseCase;
import org.web.codefm.domain.usecase.teachernotebook.GroupAssignmentUseCase;
import org.web.codefm.model.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateGroupAssignments implements TeacherNoteBookGroupAssignmentsApi {

    private final GroupAssignmentUseCase groupAssignmentUseCase;
    private final GroupAssignmentDocumentUseCase groupAssignmentDocumentUseCase;
    private final GroupAssignmentDTOMapper groupAssignmentDTOMapper;
    private final GroupAssignmentGradeDTOMapper groupAssignmentGradeDTOMapper;
    private final GroupAssignmentDocumentDTOMapper groupAssignmentDocumentDTOMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<GroupAssignmentDTO>> getGroupAssignmentsByClass(Integer classId, String acceptLanguage) {
        final List<GroupAssignment> assignments = this.groupAssignmentUseCase.getAssignmentsByClassId(classId);
        return ResponseEntity.ok(this.groupAssignmentDTOMapper.toDTOList(assignments));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<GroupAssignmentDTO> createGroupAssignment(Integer classId, GroupAssignmentRequestDTO body, String acceptLanguage) {
        final GroupAssignment assignment = this.groupAssignmentDTOMapper.toDomain(body);
        final GroupAssignment created = this.groupAssignmentUseCase.createAssignment(classId, assignment);
        return new ResponseEntity<>(this.groupAssignmentDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<GroupAssignmentDTO> updateGroupAssignment(Integer assignmentId, GroupAssignmentRequestDTO body, String acceptLanguage) {
        final GroupAssignment assignment = this.groupAssignmentDTOMapper.toDomain(body);
        final GroupAssignment updated = this.groupAssignmentUseCase.updateAssignment(assignmentId, assignment);
        return ResponseEntity.ok(this.groupAssignmentDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteGroupAssignment(Integer assignmentId, String acceptLanguage) {
        this.groupAssignmentUseCase.softDeleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<GroupAssignmentGradeDTO>> getGroupAssignmentGrades(Integer assignmentId, String acceptLanguage) {
        final List<GroupAssignmentGrade> grades = this.groupAssignmentUseCase.getGradesByAssignmentId(assignmentId);
        return ResponseEntity.ok(this.groupAssignmentGradeDTOMapper.toDTOList(grades));
    }

    @Logged
    @Override
    @Locale(3)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<GroupAssignmentGradeDTO> upsertGroupAssignmentGrade(Integer assignmentId, Integer groupId, GroupAssignmentGradeRequestDTO body, String acceptLanguage) {
        final GroupAssignmentGrade grade = this.groupAssignmentUseCase.createOrUpdateGrade(assignmentId, groupId, body.getGrade());
        return ResponseEntity.ok(this.groupAssignmentGradeDTOMapper.toDTO(grade));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteGroupAssignmentGrade(Integer assignmentId, Integer groupId, String acceptLanguage) {
        this.groupAssignmentUseCase.deleteGrade(assignmentId, groupId);
        return ResponseEntity.noContent().build();
    }

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<GroupAssignmentDocumentDTO> uploadGroupAssignmentDocument(Integer assignmentId, String acceptLanguage, MultipartFile file, String description) {
        final GroupAssignmentDocument created = this.groupAssignmentDocumentUseCase.uploadAssignmentDocument(assignmentId, file, description);
        return new ResponseEntity<>(this.groupAssignmentDocumentDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<GroupAssignmentDocumentDTO> uploadGroupAssignmentGroupDocument(Integer assignmentId, Integer groupId, String acceptLanguage, MultipartFile file, String description) {
        final GroupAssignmentDocument created = this.groupAssignmentDocumentUseCase.uploadGroupDocument(assignmentId, groupId, file, description);
        return new ResponseEntity<>(this.groupAssignmentDocumentDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Resource> downloadGroupAssignmentDocument(Integer assignmentId, Integer documentId, String acceptLanguage) {
        final byte[] fileBytes = this.groupAssignmentDocumentUseCase.downloadDocument(assignmentId, documentId);
        final String filename = this.groupAssignmentDocumentUseCase.getDocumentFilename(assignmentId, documentId);

        final String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        final MediaType mediaType = MediaType.parseMediaType(MimeTypeEnum.findMimeType(extension));

        final ByteArrayResource resource = new ByteArrayResource(fileBytes);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(fileBytes.length)
                .body(resource);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteGroupAssignmentDocument(Integer assignmentId, Integer documentId, String acceptLanguage) {
        this.groupAssignmentDocumentUseCase.deleteDocument(assignmentId, documentId);
        return ResponseEntity.noContent().build();
    }
}

