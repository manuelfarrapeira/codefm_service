package org.web.codefm.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.web.codefm.domain.exception.UserNotFound;
import org.web.codefm.domain.exception.teachernotebook.*;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ExceptionStatusEnum {

    USER_NOT_FOUND(UserNotFound.class, HttpStatus.NOT_FOUND),
    VALIDATION_ERROR(SchoolValidationException.class, HttpStatus.BAD_REQUEST),
    CLASS_VALIDATION_ERROR(ClassValidationException.class, HttpStatus.BAD_REQUEST),
    STUDENT_VALIDATION_ERROR(StudentValidationException.class, HttpStatus.BAD_REQUEST),
    STUDENT_SEARCH_VALIDATION(StudentSearchValidationException.class, HttpStatus.BAD_REQUEST),
    SCHOOL_NOT_FOUND(SchoolNotFoundException.class, HttpStatus.NOT_FOUND),
    SCHOOL_FORBIDDEN(SchoolForbiddenException.class, HttpStatus.FORBIDDEN),
    CLASS_NOT_FOUND(ClassNotFoundException.class, HttpStatus.NOT_FOUND),
    CLASS_FORBIDDEN(ClassForbiddenException.class, HttpStatus.FORBIDDEN),
    STUDENT_NOT_FOUND(StudentNotFoundException.class, HttpStatus.NOT_FOUND),
    STUDENT_PHOTO_UPLOAD_ERROR(StudentPhotoUploadException.class, HttpStatus.INTERNAL_SERVER_ERROR),
    STUDENT_PHOTO_NOT_FOUND(StudentPhotoNotFoundException.class, HttpStatus.NOT_FOUND),
    STUDENT_PHOTO_DELETE_ERROR(StudentPhotoDeleteException.class, HttpStatus.INTERNAL_SERVER_ERROR),
    STUDENT_CLASS_VALIDATION_ERROR(StudentClassValidationException.class, HttpStatus.BAD_REQUEST),
    STUDENT_CLASS_NOT_FOUND(StudentClassNotFoundException.class, HttpStatus.NOT_FOUND),
    SUBJECT_VALIDATION_ERROR(SubjectValidationException.class, HttpStatus.BAD_REQUEST),
    SUBJECT_NOT_FOUND(SubjectNotFoundException.class, HttpStatus.NOT_FOUND),
    SUBJECT_FORBIDDEN(SubjectForbiddenException.class, HttpStatus.FORBIDDEN),
    SCHEDULE_NOT_FOUND(ScheduleNotFoundException.class, HttpStatus.NOT_FOUND),
    SCHEDULE_VALIDATION_ERROR(ScheduleValidationException.class, HttpStatus.BAD_REQUEST),
    SUBJECT_CLASS_VALIDATION_ERROR(SubjectClassValidationException.class, HttpStatus.BAD_REQUEST),
    SUBJECT_CLASS_DUPLICATE(SubjectClassDuplicateException.class, HttpStatus.CONFLICT),
    EXERCISE_NOT_FOUND(ExerciseNotFoundException.class, HttpStatus.NOT_FOUND),
    EXERCISE_VALIDATION_ERROR(ExerciseValidationException.class, HttpStatus.BAD_REQUEST);

    private final Class<?> exceptionClazz;

    private final HttpStatus status;

    public static <T extends Throwable> ExceptionStatusEnum getExceptionEnum(final Class<T> obj) {
        return Arrays.stream(ExceptionStatusEnum.values())
                .filter(ex -> (obj.equals(ex.getExceptionClazz()))).findFirst().orElse(null);
    }

}
