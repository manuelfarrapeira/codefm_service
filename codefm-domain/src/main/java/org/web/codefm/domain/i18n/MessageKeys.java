package org.web.codefm.domain.i18n;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageKeys {

    public static final String SCHOOL_VALIDATION_NAME_REQUIRED = "school.validation.name.required";
    public static final String SCHOOL_VALIDATION_TLF_INVALID = "school.validation.tlf.invalid";
    public static final String SCHOOL_NOT_FOUND = "school.not.found";
    public static final String SCHOOL_FORBIDDEN = "school.forbidden";

    public static final String CLASS_VALIDATION_NAME_REQUIRED = "class.validation.name.required";
    public static final String CLASS_VALIDATION_SCHOOL_YEAR_REQUIRED = "class.validation.schoolYear.required";
    public static final String CLASS_VALIDATION_SCHOOL_YEAR_FORMAT_INVALID = "class.validation.schoolYear.format.invalid";
    public static final String CLASS_VALIDATION_SCHOOL_YEAR_NOT_CONSECUTIVE = "class.validation.schoolYear.notConsecutive";
    public static final String CLASS_NOT_FOUND = "class.not.found";
    public static final String CLASS_FORBIDDEN = "class.forbidden";

    public static final String STUDENT_VALIDATION_NAME_REQUIRED = "student.validation.name.required";
    public static final String STUDENT_VALIDATION_NAME_MIN_LENGTH = "student.validation.name.minLength";
    public static final String STUDENT_VALIDATION_SURNAMES_REQUIRED = "student.validation.surnames.required";
    public static final String STUDENT_VALIDATION_SURNAMES_MIN_LENGTH = "student.validation.surnames.minLength";
    public static final String STUDENT_VALIDATION_DATE_FORMAT_INVALID = "student.validation.dateOfBirth.format.invalid";
    public static final String STUDENT_NOT_FOUND = "student.not.found";
    public static final String STUDENT_PHOTO_EMPTY = "student.photo.empty";
    public static final String STUDENT_PHOTO_UPLOAD_ERROR = "student.photo.upload.error";
    public static final String STUDENT_PHOTO_INVALID_EXTENSION = "student.photo.invalid.extension";
    public static final String STUDENT_PHOTO_SIZE_EXCEEDED = "student.photo.size.exceeded";
    public static final String STUDENT_PHOTO_NOT_FOUND = "student.photo.not.found";
    public static final String STUDENT_PHOTO_DELETE_ERROR = "student.photo.delete.error";
    public static final String STUDENT_SEARCH_NO_FILTERS = "student.search.no.filters";

    public static final String STUDENT_ALREADY_IN_CLASS = "student.already.in.class";
    public static final String STUDENT_NOT_IN_CLASS = "student.not.in.class";

    public static final String SUBJECT_VALIDATION_NAME_REQUIRED = "subject.validation.name.required";
    public static final String SUBJECT_NOT_FOUND = "subject.not.found";
    public static final String SUBJECT_FORBIDDEN = "subject.forbidden";

    public static final String SCHEDULE_NOT_FOUND = "schedule.not.found";
    public static final String SCHEDULE_VALIDATION_DAY_REQUIRED = "schedule.validation.day.required";
    public static final String SCHEDULE_VALIDATION_DAY_INVALID = "schedule.validation.day.invalid";
    public static final String SCHEDULE_VALIDATION_START_REQUIRED = "schedule.validation.start.required";
    public static final String SCHEDULE_VALIDATION_END_REQUIRED = "schedule.validation.end.required";
    public static final String SCHEDULE_VALIDATION_END_BEFORE_START = "schedule.validation.end.before.start";
    public static final String SCHEDULE_VALIDATION_SUBJECT_NOT_FOUND = "schedule.validation.subject.not.found";
    public static final String SCHEDULE_VALIDATION_CLASS_NOT_FOUND = "schedule.validation.class.not.found";
    public static final String SCHEDULE_VALIDATION_IDS_REQUIRED = "schedule.validation.ids.required";
    public static final String SCHEDULE_VALIDATION_IDS_NOT_OWNED = "schedule.validation.ids.not.owned";
    public static final String SCHEDULE_VALIDATION_ITEMS_REQUIRED = "schedule.validation.items.required";
    public static final String SCHEDULE_VALIDATION_TIME_OVERLAP = "schedule.validation.time.overlap";

}
