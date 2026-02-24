package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.teachernotebook.ExerciseDocument;
import org.web.codefm.domain.enums.MimeTypeEnum;
import org.web.codefm.domain.exception.teachernotebook.ExerciseDocumentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseDocumentUploadException;
import org.web.codefm.domain.exception.teachernotebook.ExerciseNotFoundException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ExerciseDocumentRepository;
import org.web.codefm.domain.repository.teachernotebook.ExerciseRepository;
import org.web.codefm.domain.service.teachernotebook.ExerciseDocumentService;
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
public class ExerciseDocumentServiceImpl implements ExerciseDocumentService {

    private final ExerciseDocumentRepository exerciseDocumentRepository;
    private final ExerciseRepository exerciseRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Value("${exercise.documents.directory:./data/exercise-documents}")
    private String documentsDirectory;

    private static final long MAX_DOC_SIZE = 2L * 1024 * 1024;

    @Override
    public ExerciseDocument uploadDocument(Integer exerciseId, MultipartFile file, String description) {
        Integer teacherId = getTeacherId();

        exerciseRepository.findByIdAndTeacherId(exerciseId, teacherId)
                .orElseThrow(() -> new ExerciseNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_NOT_FOUND, null, sessionUser.getLocale())
                ));

        byte[] fileBytes;
        String originalFilename;
        try {
            fileBytes = file != null && !file.isEmpty() ? file.getBytes() : new byte[0];
            originalFilename = file != null ? file.getOriginalFilename() : "";
        } catch (IOException e) {
            throw new ExerciseDocumentUploadException(
                    messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_UPLOAD_ERROR, null, sessionUser.getLocale()), e
            );
        }

        if (fileBytes.length == 0) {
            throw new ExerciseDocumentUploadException(
                    messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_EMPTY, null, sessionUser.getLocale())
            );
        }

        if (fileBytes.length > MAX_DOC_SIZE) {
            throw new ExerciseDocumentUploadException(
                    messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_SIZE_EXCEEDED, null, sessionUser.getLocale())
            );
        }

        String extension = extractExtension(originalFilename);

        if (!MimeTypeEnum.isAllowedExtension(extension)) {
            throw new ExerciseDocumentUploadException(
                    messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_INVALID_EXTENSION, null, sessionUser.getLocale())
            );
        }

        try {
            Path directory = Paths.get(documentsDirectory);

            String baseName = extractBaseName(originalFilename);
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String diskFilename = exerciseId + "_" + baseName + "_" + uuid + "." + extension;

            Path filePath = directory.resolve(diskFilename);
            Files.write(filePath, fileBytes);

            ExerciseDocument documentToSave = ExerciseDocument.builder()
                    .exerciseId(exerciseId)
                    .document(diskFilename)
                    .description(description)
                    .build();

            return exerciseDocumentRepository.save(documentToSave);

        } catch (IOException e) {
            log.error("Error saving exercise document", e);
            throw new ExerciseDocumentUploadException(
                    messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_UPLOAD_ERROR, null, sessionUser.getLocale()), e
            );
        }
    }

    @Override
    public byte[] downloadDocument(Integer exerciseId, Integer documentId) {
        Integer teacherId = getTeacherId();

        exerciseRepository.findByIdAndTeacherId(exerciseId, teacherId)
                .orElseThrow(() -> new ExerciseNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_NOT_FOUND, null, sessionUser.getLocale())
                ));

        ExerciseDocument document = exerciseDocumentRepository.findById(documentId)
                .filter(doc -> doc.getExerciseId().equals(exerciseId))
                .orElseThrow(() -> new ExerciseDocumentNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_NOT_FOUND, null, sessionUser.getLocale())
                ));

        try {
            Path filePath = Paths.get(documentsDirectory).resolve(document.getDocument());
            if (!Files.exists(filePath)) {
                throw new ExerciseDocumentNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_NOT_FOUND, null, sessionUser.getLocale())
                );
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error reading exercise document", e);
            throw new ExerciseDocumentNotFoundException(
                    messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_NOT_FOUND, null, sessionUser.getLocale())
            );
        }
    }

    @Override
    public String getDocumentFilename(Integer exerciseId, Integer documentId) {
        Integer teacherId = getTeacherId();

        exerciseRepository.findByIdAndTeacherId(exerciseId, teacherId)
                .orElseThrow(() -> new ExerciseNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_NOT_FOUND, null, sessionUser.getLocale())
                ));

        ExerciseDocument document = exerciseDocumentRepository.findById(documentId)
                .filter(doc -> doc.getExerciseId().equals(exerciseId))
                .orElseThrow(() -> new ExerciseDocumentNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_NOT_FOUND, null, sessionUser.getLocale())
                ));

        return document.getDocument();
    }

    @Override
    public ExerciseDocument updateDescription(Integer exerciseId, Integer documentId, String description) {
        Integer teacherId = getTeacherId();

        exerciseRepository.findByIdAndTeacherId(exerciseId, teacherId)
                .orElseThrow(() -> new ExerciseNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_NOT_FOUND, null, sessionUser.getLocale())
                ));

        ExerciseDocument document = exerciseDocumentRepository.findById(documentId)
                .filter(doc -> doc.getExerciseId().equals(exerciseId))
                .orElseThrow(() -> new ExerciseDocumentNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_NOT_FOUND, null, sessionUser.getLocale())
                ));

        document.setDescription(description);
        return exerciseDocumentRepository.update(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Integer exerciseId, Integer documentId) {
        Integer teacherId = getTeacherId();

        exerciseRepository.findByIdAndTeacherId(exerciseId, teacherId)
                .orElseThrow(() -> new ExerciseNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_NOT_FOUND, null, sessionUser.getLocale())
                ));

        ExerciseDocument document = exerciseDocumentRepository.findById(documentId)
                .filter(doc -> doc.getExerciseId().equals(exerciseId))
                .orElseThrow(() -> new ExerciseDocumentNotFoundException(
                        messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_NOT_FOUND, null, sessionUser.getLocale())
                ));

        exerciseDocumentRepository.deleteById(documentId);
        deleteFileFromDisk(document.getDocument());
    }

    @Override
    @Transactional
    public void deleteDocumentsByExerciseId(Integer exerciseId) {
        List<ExerciseDocument> documents = exerciseDocumentRepository.findByExerciseId(exerciseId);
        exerciseDocumentRepository.deleteByExerciseId(exerciseId);
        documents.forEach(doc -> deleteFileFromDisk(doc.getDocument()));
    }

    @Override
    @Transactional
    public void deleteDocumentsByExerciseIds(List<Integer> exerciseIds) {
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return;
        }
        List<ExerciseDocument> documents = exerciseDocumentRepository.findByExerciseIds(exerciseIds);
        exerciseDocumentRepository.deleteByExerciseIds(exerciseIds);
        documents.forEach(doc -> deleteFileFromDisk(doc.getDocument()));
    }

    private void deleteFileFromDisk(String filename) {
        try {
            Path filePath = Paths.get(documentsDirectory).resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new ExerciseDocumentUploadException(
                    messageSource.getMessage(MessageKeys.EXERCISE_DOCUMENT_DELETE_ERROR, null, sessionUser.getLocale()), e
            );
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private String extractBaseName(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex).replaceAll("[^a-zA-Z0-9._-]", "_");
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private Integer getTeacherId() {
        return Integer.valueOf(
                sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName())
        );
    }
}

