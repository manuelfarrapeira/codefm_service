package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentDocumentService;
import org.web.codefm.domain.usecase.teachernotebook.GroupAssignmentDocumentUseCase;

@Service
@RequiredArgsConstructor
public class GroupAssignmentDocumentUseCaseImpl implements GroupAssignmentDocumentUseCase {

    private final GroupAssignmentDocumentService groupAssignmentDocumentService;

    @Override
    public GroupAssignmentDocument uploadAssignmentDocument(Integer assignmentId, MultipartFile file, String description) {
        return this.groupAssignmentDocumentService.uploadDocument(assignmentId, null, file, description, false);
    }

    @Override
    public GroupAssignmentDocument uploadGroupDocument(Integer assignmentId, Integer groupId, MultipartFile file, String description) {
        return this.groupAssignmentDocumentService.uploadDocument(assignmentId, groupId, file, description, true);
    }

    @Override
    public byte[] downloadDocument(Integer assignmentId, Integer documentId) {
        return this.groupAssignmentDocumentService.downloadDocument(assignmentId, documentId);
    }

    @Override
    public String getDocumentFilename(Integer assignmentId, Integer documentId) {
        return this.groupAssignmentDocumentService.getDocumentFilename(assignmentId, documentId);
    }

    @Override
    public void deleteDocument(Integer assignmentId, Integer documentId) {
        this.groupAssignmentDocumentService.deleteDocument(assignmentId, documentId);
    }
}

