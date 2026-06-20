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
import org.web.codefm.api.TeacherNoteBookExerciseDocumentsApi;
import org.web.codefm.api.mapper.ExerciseDocumentDTOMapper;
import org.web.codefm.api.utils.Locale;
import org.web.codefm.api.utils.Logged;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.domain.enums.MimeTypeEnum;
import org.web.codefm.domain.usecase.teachernotebook.ExerciseDocumentUseCase;
import org.web.codefm.model.ExerciseDocumentDTO;
import org.web.codefm.model.ExerciseDocumentUpdateRequestDTO;

@RestController
@RequiredArgsConstructor
public class PrivateExerciseDocuments implements TeacherNoteBookExerciseDocumentsApi {

    private final ExerciseDocumentUseCase exerciseDocumentUseCase;
    private final ExerciseDocumentDTOMapper exerciseDocumentDTOMapper;

    @Logged
    @Override
    @Locale(1)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ExerciseDocumentDTO> uploadExerciseDocument(Integer exerciseId, String acceptLanguage, MultipartFile file, String description) {
        ExerciseDocument created = exerciseDocumentUseCase.uploadDocument(exerciseId, file, description);
        return new ResponseEntity<>(exerciseDocumentDTOMapper.toDTO(created), HttpStatus.CREATED);
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Resource> downloadExerciseDocument(Integer exerciseId, Integer documentId, String acceptLanguage) {
        byte[] fileBytes = exerciseDocumentUseCase.downloadDocument(exerciseId, documentId);
        String filename = exerciseDocumentUseCase.getDocumentFilename(exerciseId, documentId);

        String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        MediaType mediaType = MediaType.parseMediaType(MimeTypeEnum.findMimeType(extension));

        ByteArrayResource resource = new ByteArrayResource(fileBytes);

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
    public ResponseEntity<ExerciseDocumentDTO> updateExerciseDocumentDescription(Integer exerciseId, Integer documentId, ExerciseDocumentUpdateRequestDTO body, String acceptLanguage) {
        ExerciseDocument updated = exerciseDocumentUseCase.updateDescription(exerciseId, documentId, body.getDescription());
        return ResponseEntity.ok(exerciseDocumentDTOMapper.toDTO(updated));
    }

    @Logged
    @Override
    @Locale(2)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteExerciseDocument(Integer exerciseId, Integer documentId, String acceptLanguage) {
        exerciseDocumentUseCase.deleteDocument(exerciseId, documentId);
        return ResponseEntity.noContent().build();
    }
}
