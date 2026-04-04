package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.enums.MimeTypeEnum;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentDocumentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentDocumentUploadException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.GroupAssignmentDocumentRepository;
import org.web.codefm.domain.repository.teachernotebook.GroupAssignmentRepository;
import org.web.codefm.domain.repository.teachernotebook.SavedStudentGroupRepository;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentDocumentService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.domain.util.FileNameUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupAssignmentDocumentServiceImpl implements GroupAssignmentDocumentService {

    private final GroupAssignmentDocumentRepository groupAssignmentDocumentRepository;
    private final GroupAssignmentRepository groupAssignmentRepository;
    private final SavedStudentGroupRepository savedStudentGroupRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Value("${group.documents.directory:./data/group-documents}")
    private String documentsDirectory;

    private static final long MAX_DOC_SIZE = 2L * 1024 * 1024;

    @Override
    public GroupAssignmentDocument uploadDocument(Integer assignmentId, Integer groupId, MultipartFile file,
                                                  String description, Boolean groupDocument) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        final GroupAssignment assignment = this.findAssignmentOrThrow(assignmentId, teacherId, locale);

        final boolean isGroupDoc = Boolean.TRUE.equals(groupDocument);

        if (isGroupDoc) {
            this.validateGroupBelongsToClass(groupId, assignment.getClassId(), teacherId, locale);
        }

        byte[] fileBytes;
        String originalFilename;
        try {
            fileBytes = file != null && !file.isEmpty() ? file.getBytes() : new byte[0];
            originalFilename = file != null ? file.getOriginalFilename() : "";
        } catch (IOException e) {
            throw new GroupAssignmentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_UPLOAD_ERROR, null, this.sessionUser.getLocale()), e);
        }

        if (fileBytes.length == 0) {
            throw new GroupAssignmentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_EMPTY, null, this.sessionUser.getLocale()));
        }

        if (fileBytes.length > MAX_DOC_SIZE) {
            throw new GroupAssignmentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_SIZE_EXCEEDED, null, this.sessionUser.getLocale()));
        }

        final String extension = FileNameUtil.extractExtension(originalFilename);
        if (!MimeTypeEnum.isAllowedExtension(extension)) {
            throw new GroupAssignmentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_INVALID_EXTENSION, null, this.sessionUser.getLocale()));
        }

        final Integer effectiveGroupId = isGroupDoc ? groupId : null;

        try {
            final Path directory = Paths.get(this.documentsDirectory);
            final String baseName = FileNameUtil.extractBaseName(originalFilename);
            final String uuid = UUID.randomUUID().toString().substring(0, 8);
            final String diskFilename = assignmentId + "_" + baseName + "_" + uuid + "." + extension;
            final Path filePath = directory.resolve(diskFilename);
            Files.write(filePath, fileBytes);

            final GroupAssignmentDocument documentToSave = GroupAssignmentDocument.builder()
                    .groupAssignmentId(assignmentId)
                    .groupId(effectiveGroupId)
                    .document(diskFilename)
                    .description(description)
                    .groupDocument(isGroupDoc)
                    .build();

            return this.groupAssignmentDocumentRepository.save(documentToSave);
        } catch (IOException e) {
            log.error("Error saving group assignment document", e);
            throw new GroupAssignmentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_UPLOAD_ERROR, null, this.sessionUser.getLocale()), e);
        }
    }

    @Override
    public byte[] downloadDocument(Integer assignmentId, Integer documentId) {
        final Integer teacherId = this.getTeacherId();
        this.validateAssignmentOwnership(assignmentId, teacherId);

        final GroupAssignmentDocument document = this.groupAssignmentDocumentRepository.findById(documentId)
                .filter(doc -> doc.getGroupAssignmentId().equals(assignmentId))
                .orElseThrow(() -> new GroupAssignmentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())));

        try {
            final Path filePath = Paths.get(this.documentsDirectory).resolve(document.getDocument());
            if (!Files.exists(filePath)) {
                throw new GroupAssignmentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale()));
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error reading group assignment document", e);
            throw new GroupAssignmentDocumentNotFoundException(
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale()));
        }
    }

    @Override
    public String getDocumentFilename(Integer assignmentId, Integer documentId) {
        final Integer teacherId = this.getTeacherId();
        this.validateAssignmentOwnership(assignmentId, teacherId);

        final GroupAssignmentDocument document = this.groupAssignmentDocumentRepository.findById(documentId)
                .filter(doc -> doc.getGroupAssignmentId().equals(assignmentId))
                .orElseThrow(() -> new GroupAssignmentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())));

        return document.getDocument();
    }

    @Override
    @Transactional
    public void deleteDocument(Integer assignmentId, Integer documentId) {
        final Integer teacherId = this.getTeacherId();
        this.validateAssignmentOwnership(assignmentId, teacherId);

        final GroupAssignmentDocument document = this.groupAssignmentDocumentRepository.findById(documentId)
                .filter(doc -> doc.getGroupAssignmentId().equals(assignmentId))
                .orElseThrow(() -> new GroupAssignmentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())));

        this.groupAssignmentDocumentRepository.deleteById(documentId);
        this.deleteFileFromDisk(document.getDocument());
    }

    @Override
    @Transactional
    public void deleteDocumentsByGroupAssignmentId(Integer assignmentId) {
        final List<GroupAssignmentDocument> documents = this.groupAssignmentDocumentRepository.findByAssignmentId(assignmentId);
        this.groupAssignmentDocumentRepository.deleteByGroupAssignmentId(assignmentId);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByGroupAssignmentIds(List<Integer> assignmentIds) {
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            return;
        }
        final List<GroupAssignmentDocument> documents = this.groupAssignmentDocumentRepository.findByGroupAssignmentIds(assignmentIds);
        this.groupAssignmentDocumentRepository.deleteByGroupAssignmentIds(assignmentIds);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByGroupId(Integer groupId) {
        final List<GroupAssignmentDocument> documents = this.groupAssignmentDocumentRepository.findByGroupIds(List.of(groupId));
        this.groupAssignmentDocumentRepository.deleteByGroupId(groupId);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByGroupIds(List<Integer> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return;
        }
        final List<GroupAssignmentDocument> documents = this.groupAssignmentDocumentRepository.findByGroupIds(groupIds);
        this.groupAssignmentDocumentRepository.deleteByGroupIds(groupIds);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    private GroupAssignment findAssignmentOrThrow(Integer assignmentId, Integer teacherId, Locale locale) {
        return this.groupAssignmentRepository.findByIdAndTeacherId(assignmentId, teacherId)
                .orElseThrow(() -> new GroupAssignmentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND, null, locale)));
    }

    private void validateAssignmentOwnership(Integer assignmentId, Integer teacherId) {
        this.findAssignmentOrThrow(assignmentId, teacherId, this.sessionUser.getLocale());
    }

    private void validateGroupBelongsToClass(Integer groupId, Integer classId, Integer teacherId, Locale locale) {
        final List<ErrorMessage> errors = new ArrayList<>();
        final Optional<SavedStudentGroup> group = this.savedStudentGroupRepository.findByIdAndTeacherId(groupId, teacherId);
        if (group.isEmpty()) {
            errors.add(new ErrorMessage("groupId",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_FOUND, null, locale)));
        } else if (!group.get().getClassId().equals(classId)) {
            errors.add(new ErrorMessage("groupId",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_IN_CLASS, null, locale)));
        }
        if (!errors.isEmpty()) {
            throw new GroupAssignmentValidationException(errors);
        }
    }

    private void deleteFileFromDisk(String filename) {
        try {
            final Path filePath = Paths.get(this.documentsDirectory).resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new GroupAssignmentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_DOCUMENT_DELETE_ERROR, null, this.sessionUser.getLocale()), e);
        }
    }


    private Integer getTeacherId() {
        return this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
    }
}

