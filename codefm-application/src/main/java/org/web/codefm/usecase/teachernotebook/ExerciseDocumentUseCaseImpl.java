package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
import org.web.codefm.domain.usecase.teachernotebook.ExerciseDocumentUseCase;

@Service
@RequiredArgsConstructor
public class ExerciseDocumentUseCaseImpl implements ExerciseDocumentUseCase {

    private final ExerciseDocumentService exerciseDocumentService;

    @Override
    public ExerciseDocument uploadDocument(Integer exerciseId, MultipartFile file, String description) {
        return exerciseDocumentService.uploadDocument(exerciseId, file, description);
    }

    @Override
    public byte[] downloadDocument(Integer exerciseId, Integer documentId) {
        return exerciseDocumentService.downloadDocument(exerciseId, documentId);
    }

    @Override
    public String getDocumentFilename(Integer exerciseId, Integer documentId) {
        return exerciseDocumentService.getDocumentFilename(exerciseId, documentId);
    }

    @Override
    public ExerciseDocument updateDescription(Integer exerciseId, Integer documentId, String description) {
        return exerciseDocumentService.updateDescription(exerciseId, documentId, description);
    }

    @Override
    public void deleteDocument(Integer exerciseId, Integer documentId) {
        exerciseDocumentService.deleteDocument(exerciseId, documentId);
    }
}

