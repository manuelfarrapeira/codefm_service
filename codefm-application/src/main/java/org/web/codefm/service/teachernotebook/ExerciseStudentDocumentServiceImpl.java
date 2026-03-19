package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentDocument;
import org.web.codefm.domain.enums.MimeTypeEnum;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentDocumentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentDocumentUploadException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseStudentGradeNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ExerciseStudentDocumentRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseStudentGradeRepository;
import org.web.codefm.domain.service.teachernotebook.ExerciseStudentDocumentService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseStudentDocumentServiceImpl implements ExerciseStudentDocumentService {

    private final ExerciseStudentDocumentRepository exerciseStudentDocumentRepository;
    private final ExerciseStudentGradeRepository exerciseStudentGradeRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Value("${exercise.student-documents.directory:./data/exercise-student-document}")
    private String documentsDirectory;

    private static final long MAX_DOC_SIZE = 2L * 1024 * 1024;

    @Override
    public ExerciseStudentDocument uploadDocument(Integer gradeId, MultipartFile file, String description) {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

        this.exerciseStudentGradeRepository.findByIdAndTeacherId(gradeId, teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        final byte[] fileBytes;
        final String originalFilename;
        try {
            fileBytes = file != null && !file.isEmpty() ? file.getBytes() : new byte[0];
            originalFilename = file != null ? file.getOriginalFilename() : "";
        } catch (IOException e) {
            throw new ExerciseStudentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_UPLOAD_ERROR, null, this.sessionUser.getLocale()), e
            );
        }

        if (fileBytes.length == 0) {
            throw new ExerciseStudentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_EMPTY, null, this.sessionUser.getLocale())
            );
        }

        if (fileBytes.length > MAX_DOC_SIZE) {
            throw new ExerciseStudentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_SIZE_EXCEEDED, null, this.sessionUser.getLocale())
            );
        }

        final String extension = this.extractExtension(originalFilename);

        if (!MimeTypeEnum.isAllowedExtension(extension)) {
            throw new ExerciseStudentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_INVALID_EXTENSION, null, this.sessionUser.getLocale())
            );
        }

        try {
            final Path directory = Paths.get(this.documentsDirectory);
            final String baseName = this.extractBaseName(originalFilename);
            final String uuid = UUID.randomUUID().toString().substring(0, 8);
            final String diskFilename = gradeId + "_" + baseName + "_" + uuid + "." + extension;
            final Path filePath = directory.resolve(diskFilename);
            Files.write(filePath, fileBytes);

            final ExerciseStudentDocument documentToSave = ExerciseStudentDocument.builder()
                    .gradeId(gradeId)
                    .document(diskFilename)
                    .description(description)
                    .build();

            return this.exerciseStudentDocumentRepository.save(documentToSave);

        } catch (IOException e) {
            log.error("Error saving exercise student document", e);
            throw new ExerciseStudentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_UPLOAD_ERROR, null, this.sessionUser.getLocale()), e
            );
        }
    }

    @Override
    public byte[] downloadDocument(Integer gradeId, Integer documentId) {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

        this.exerciseStudentGradeRepository.findByIdAndTeacherId(gradeId, teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        final ExerciseStudentDocument document = this.exerciseStudentDocumentRepository.findById(documentId)
                .filter(doc -> doc.getGradeId().equals(gradeId))
                .orElseThrow(() -> new ExerciseStudentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        try {
            final Path filePath = Paths.get(this.documentsDirectory).resolve(document.getDocument());
            if (!Files.exists(filePath)) {
                throw new ExerciseStudentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())
                );
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error reading exercise student document", e);
            throw new ExerciseStudentDocumentNotFoundException(
                    this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())
            );
        }
    }

    @Override
    public String getDocumentFilename(Integer gradeId, Integer documentId) {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

        this.exerciseStudentGradeRepository.findByIdAndTeacherId(gradeId, teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        final ExerciseStudentDocument document = this.exerciseStudentDocumentRepository.findById(documentId)
                .filter(doc -> doc.getGradeId().equals(gradeId))
                .orElseThrow(() -> new ExerciseStudentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        return document.getDocument();
    }

    @Override
    public ExerciseStudentDocument updateDescription(Integer gradeId, Integer documentId, String description) {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

        this.exerciseStudentGradeRepository.findByIdAndTeacherId(gradeId, teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        final ExerciseStudentDocument document = this.exerciseStudentDocumentRepository.findById(documentId)
                .filter(doc -> doc.getGradeId().equals(gradeId))
                .orElseThrow(() -> new ExerciseStudentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        document.setDescription(description);
        return this.exerciseStudentDocumentRepository.update(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Integer gradeId, Integer documentId) {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);

        this.exerciseStudentGradeRepository.findByIdAndTeacherId(gradeId, teacherId)
                .orElseThrow(() -> new ExerciseStudentGradeNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_GRADE_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        final ExerciseStudentDocument document = this.exerciseStudentDocumentRepository.findById(documentId)
                .filter(doc -> doc.getGradeId().equals(gradeId))
                .orElseThrow(() -> new ExerciseStudentDocumentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_NOT_FOUND, null, this.sessionUser.getLocale())
                ));

        this.exerciseStudentDocumentRepository.deleteById(documentId);
        this.deleteFileFromDisk(document.getDocument());
    }

    @Override
    @Transactional
    public void deleteDocumentsByGradeId(Integer gradeId) {
        final List<ExerciseStudentDocument> documents = this.exerciseStudentDocumentRepository.findByGradeId(gradeId);
        this.exerciseStudentDocumentRepository.deleteByGradeId(gradeId);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByGradeIds(List<Integer> gradeIds) {
        if (gradeIds == null || gradeIds.isEmpty()) {
            return;
        }
        final List<ExerciseStudentDocument> documents = this.exerciseStudentDocumentRepository.findByGradeIds(gradeIds);
        this.exerciseStudentDocumentRepository.deleteByGradeIds(gradeIds);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByExerciseId(Integer exerciseId) {
        final List<ExerciseStudentDocument> documents = this.exerciseStudentDocumentRepository.findByExerciseId(exerciseId);
        this.exerciseStudentDocumentRepository.deleteByExerciseId(exerciseId);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByExerciseIds(List<Integer> exerciseIds) {
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return;
        }
        final List<ExerciseStudentDocument> documents = this.exerciseStudentDocumentRepository.findByExerciseIds(exerciseIds);
        this.exerciseStudentDocumentRepository.deleteByExerciseIds(exerciseIds);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByStudentId(Integer studentId) {
        final List<ExerciseStudentDocument> documents = this.exerciseStudentDocumentRepository.findByStudentId(studentId);
        this.exerciseStudentDocumentRepository.deleteByStudentId(studentId);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByStudentIdAndClassId(Integer studentId, Integer classId) {
        final List<ExerciseStudentDocument> documents = this.exerciseStudentDocumentRepository.findByStudentIdAndClassId(studentId, classId);
        this.exerciseStudentDocumentRepository.deleteByStudentIdAndClassId(studentId, classId);
        documents.forEach(doc -> this.deleteFileFromDisk(doc.getDocument()));
    }

    private void deleteFileFromDisk(String filename) {
        try {
            final Path filePath = Paths.get(this.documentsDirectory).resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new ExerciseStudentDocumentUploadException(
                    this.messageSource.getMessage(MessageKeys.EXERCISE_STUDENT_DOCUMENT_DELETE_ERROR, null, this.sessionUser.getLocale()), e
            );
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        final int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private String extractBaseName(String filename) {
        if (filename == null) {
            return "";
        }
        final int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex).replaceAll("[^a-zA-Z0-9._-]", "_");
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

