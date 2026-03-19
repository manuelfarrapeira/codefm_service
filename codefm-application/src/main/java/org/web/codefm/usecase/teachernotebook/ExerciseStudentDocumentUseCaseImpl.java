package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentDocumentService;
import org.web.codefm.domain.usecase.teachernotebook.ExerciseStudentDocumentUseCase;

@Service
@RequiredArgsConstructor
public class ExerciseStudentDocumentUseCaseImpl implements ExerciseStudentDocumentUseCase {

    private final ExerciseStudentDocumentService exerciseStudentDocumentService;

    @Override
    public ExerciseStudentDocument uploadDocument(Integer gradeId, MultipartFile file, String description) {
        return this.exerciseStudentDocumentService.uploadDocument(gradeId, file, description);
    }

    @Override
    public byte[] downloadDocument(Integer gradeId, Integer documentId) {
        return this.exerciseStudentDocumentService.downloadDocument(gradeId, documentId);
    }

    @Override
    public String getDocumentFilename(Integer gradeId, Integer documentId) {
        return this.exerciseStudentDocumentService.getDocumentFilename(gradeId, documentId);
    }

    @Override
    public ExerciseStudentDocument updateDescription(Integer gradeId, Integer documentId, String description) {
        return this.exerciseStudentDocumentService.updateDescription(gradeId, documentId, description);
    }

    @Override
    public void deleteDocument(Integer gradeId, Integer documentId) {
        this.exerciseStudentDocumentService.deleteDocument(gradeId, documentId);
    }
}

