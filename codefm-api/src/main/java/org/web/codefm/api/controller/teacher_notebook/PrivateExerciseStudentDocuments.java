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
import org.web.codefm.api.TeacherNoteBookExerciseStudentDocumentsApi;
import org.web.codefm.api.mapper.ExerciseStudentDocumentDTOMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.domain.enums.MimeTypeEnum;
import org.web.codefm.domain.usecase.teachernotebook.ExerciseStudentDocumentUseCase;
import org.web.codefm.model.ExerciseStudentDocumentDTO;
import org.web.codefm.model.ExerciseStudentDocumentUpdateRequestDTO;

@RestController
@RequiredArgsConstructor
public class PrivateExerciseStudentDocuments implements TeacherNoteBookExerciseStudentDocumentsApi {

    private final ExerciseStudentDocumentUseCase exerciseStudentDocumentUseCase;
    private final ExerciseStudentDocumentDTOMapper exerciseStudentDocumentDTOMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ExerciseStudentDocumentDTO> uploadGradeDocument(
            Integer gradeId, String acceptLanguage, MultipartFile file, String description) {
        final ExerciseStudentDocument created = this.exerciseStudentDocumentUseCase.uploadDocument(gradeId, file, description);
        return new ResponseEntity<>(this.exerciseStudentDocumentDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Resource> downloadGradeDocument(
            Integer gradeId, Integer documentId, String acceptLanguage) {
        final byte[] fileBytes = this.exerciseStudentDocumentUseCase.downloadDocument(gradeId, documentId);
        final String filename = this.exerciseStudentDocumentUseCase.getDocumentFilename(gradeId, documentId);

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
    public ResponseEntity<ExerciseStudentDocumentDTO> updateGradeDocumentDescription(
            Integer gradeId, Integer documentId, ExerciseStudentDocumentUpdateRequestDTO body, String acceptLanguage) {
        final ExerciseStudentDocument updated = this.exerciseStudentDocumentUseCase.updateDescription(gradeId, documentId, body.getDescription());
        return ResponseEntity.ok(this.exerciseStudentDocumentDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteGradeDocument(
            Integer gradeId, Integer documentId, String acceptLanguage) {
        this.exerciseStudentDocumentUseCase.deleteDocument(gradeId, documentId);
        return ResponseEntity.noContent().build();
    }
}

