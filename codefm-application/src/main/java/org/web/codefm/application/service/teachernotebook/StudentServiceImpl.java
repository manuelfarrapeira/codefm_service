package org.web.codefm.application.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.StudentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentPhotoUploadException;
import org.web.codefm.domain.exception.teachernotebook.StudentSearchValidationException;
import org.web.codefm.domain.exception.teachernotebook.StudentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.service.teachernotebook.StudentService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Value("${student.photos.directory:/app/data/student-photos}")
    private String photosDirectory;

    private static final int MIN_NAME_LENGTH = 3;
    private static final long MAX_PHOTO_SIZE = 500L * 1024; // 500KB in bytes
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    @Override
    public Student createStudent(Student student) {
        Integer teacherId = getTeacherId();
        validateStudent(student);
        student.setTeacherId(teacherId);
        return studentRepository.save(student);
    }

    @Override
    public Student updateStudent(Integer id, Student student) {
        Integer teacherId = getTeacherId();
        validateStudent(student);

        Student existingStudent = studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId)
                .orElseThrow(() -> new StudentNotFoundException(
                        messageSource.getMessage(MessageKeys.STUDENT_NOT_FOUND, null, sessionUser.getLocale())
                ));

        existingStudent.setName(student.getName());
        existingStudent.setSurnames(student.getSurnames());
        existingStudent.setDateOfBirth(student.getDateOfBirth());
        existingStudent.setAdditionalInfo(student.getAdditionalInfo());

        return studentRepository.update(existingStudent);
    }

    @Override
    public void softDeleteStudent(Integer id) {
        Integer teacherId = getTeacherId();

        Optional<Student> student = studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(id, teacherId);

        if (student.isEmpty()) {
            throw new StudentNotFoundException(
                    messageSource.getMessage(MessageKeys.STUDENT_NOT_FOUND, null, sessionUser.getLocale())
            );
        }

        studentRepository.softDelete(id, teacherId);
    }

    @Override
    public String saveStudentPhoto(Integer studentId, MultipartFile file) {
        Integer teacherId = getTeacherId();
        Student student = studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)
                .orElseThrow(() -> new StudentNotFoundException(
                        messageSource.getMessage(MessageKeys.STUDENT_NOT_FOUND, null, sessionUser.getLocale())
                ));

        byte[] photoBytes;
        String fileName;
        try {
            photoBytes = file != null && !file.isEmpty() ? file.getBytes() : new byte[0];
            fileName = file != null ? file.getOriginalFilename() : "";
        } catch (IOException e) {
            throw new StudentPhotoUploadException(
                    messageSource.getMessage(MessageKeys.STUDENT_PHOTO_UPLOAD_ERROR, null, sessionUser.getLocale()),
                    e
            );
        }

        if (photoBytes == null || photoBytes.length == 0) {
            throw new StudentPhotoUploadException(
                    messageSource.getMessage(MessageKeys.STUDENT_PHOTO_EMPTY, null, sessionUser.getLocale())
            );
        }

        if (photoBytes.length > MAX_PHOTO_SIZE) {
            throw new StudentPhotoUploadException(
                    messageSource.getMessage(MessageKeys.STUDENT_PHOTO_SIZE_EXCEEDED, null, sessionUser.getLocale())
            );
        }

        try {
            Path directory = Paths.get(photosDirectory);

            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex + 1).toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new StudentPhotoUploadException(
                        messageSource.getMessage(MessageKeys.STUDENT_PHOTO_INVALID_EXTENSION, null, sessionUser.getLocale())
                );
            }

            String photoFileName = studentId + "." + extension;
            Path photoPath = directory.resolve(photoFileName);
            Files.write(photoPath, photoBytes);

            student.setPhoto(photoFileName);
            studentRepository.update(student);

            return photoFileName;

        } catch (IOException e) {
            log.error("Error saving student photo", e);
            throw new StudentPhotoUploadException(
                    messageSource.getMessage(MessageKeys.STUDENT_PHOTO_UPLOAD_ERROR, null, sessionUser.getLocale()),
                    e
            );
        }
    }

    @Override
    public List<Student> searchStudents(Integer id, String name, String surnames) {
        Integer teacherId = getTeacherId();

        if (id == null && (name == null || name.trim().isEmpty()) &&
                (surnames == null || surnames.trim().isEmpty())) {
            String message = messageSource.getMessage(
                    MessageKeys.STUDENT_SEARCH_NO_FILTERS,
                    null,
                    sessionUser.getLocale()
            );
            throw new StudentSearchValidationException(message);
        }

        return studentRepository.searchStudents(teacherId, id, name, surnames);
    }

    private void validateStudent(Student student) {
        List<ErrorMessage> errors = new ArrayList<>();

        if (student.getName() == null || student.getName().trim().isEmpty()) {
            errors.add(new ErrorMessage("name",
                    messageSource.getMessage(MessageKeys.STUDENT_VALIDATION_NAME_REQUIRED, null, sessionUser.getLocale())
            ));
        } else if (student.getName().trim().length() < MIN_NAME_LENGTH) {
            errors.add(new ErrorMessage("name",
                    messageSource.getMessage(MessageKeys.STUDENT_VALIDATION_NAME_MIN_LENGTH, null, sessionUser.getLocale())
            ));
        }

        if (student.getSurnames() == null || student.getSurnames().trim().isEmpty()) {
            errors.add(new ErrorMessage("surnames",
                    messageSource.getMessage(MessageKeys.STUDENT_VALIDATION_SURNAMES_REQUIRED, null, sessionUser.getLocale())
            ));
        } else if (student.getSurnames().trim().length() < MIN_NAME_LENGTH) {
            errors.add(new ErrorMessage("surnames",
                    messageSource.getMessage(MessageKeys.STUDENT_VALIDATION_SURNAMES_MIN_LENGTH, null, sessionUser.getLocale())
            ));
        }


        if (!errors.isEmpty()) {
            throw new StudentValidationException(errors);
        }
    }

    private Integer getTeacherId() {
        return Integer.valueOf(
                sessionUser.getParameters().get(SessionParameter.TEACHER_ID.getClaimName())
        );
    }

    @Override
    public List<Student> getAllStudents() {
        Integer teacherId = getTeacherId();
        return studentRepository.findAllByTeacherId(teacherId);
    }
}
