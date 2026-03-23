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
	public static final String STUDENT_VALIDATION_GENDER_REQUIRED = "student.validation.gender.required";
	public static final String STUDENT_VALIDATION_GENDER_INVALID = "student.validation.gender.invalid";
	public static final String STUDENT_VALIDATION_SHAPE_INVALID = "student.validation.shape.invalid";
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
	public static final String SCHEDULE_VALIDATION_SUBJECT_NOT_IN_CLASS = "schedule.validation.subject.not.in.class";

	public static final String SUBJECT_CLASS_ALREADY_EXISTS = "subjectclass.already.exists";
	public static final String SUBJECT_CLASS_SUBJECT_IDS_REQUIRED = "subjectclass.validation.subjectIds.required";
	public static final String SUBJECT_CLASS_SUBJECT_NOT_OWNED = "subjectclass.validation.subject.not.owned";
	public static final String SUBJECT_CLASS_NOT_FOUND = "subjectclass.not.found";

	public static final String EXERCISE_NOT_FOUND = "exercise.not.found";
	public static final String EXERCISE_VALIDATION_TITLE_REQUIRED = "exercise.validation.title.required";
	public static final String EXERCISE_VALIDATION_QUARTER_REQUIRED = "exercise.validation.quarter.required";
	public static final String EXERCISE_VALIDATION_QUARTER_INVALID = "exercise.validation.quarter.invalid";
	public static final String EXERCISE_VALIDATION_SUBJECT_CLASS_NOT_FOUND = "exercise.validation.subjectClass.not.found";
	public static final String EXERCISE_VALIDATION_PERCENTAGE_GRADE_REQUIRED = "exercise.validation.percentageGrade.required";
	public static final String EXERCISE_VALIDATION_PERCENTAGE_GRADE_INVALID = "exercise.validation.percentageGrade.invalid";
	public static final String EXERCISE_VALIDATION_PERCENTAGE_GRADE_SUM_EXCEEDED = "exercise.validation.percentageGrade.sum.exceeded";
	public static final String EXERCISE_VALIDATION_MAX_GRADE_REQUIRED = "exercise.validation.maxGrade.required";
	public static final String EXERCISE_VALIDATION_MAX_GRADE_INVALID = "exercise.validation.maxGrade.invalid";

	public static final String EXERCISE_DOCUMENT_NOT_FOUND = "exercise.document.not.found";
	public static final String EXERCISE_DOCUMENT_EMPTY = "exercise.document.empty";
	public static final String EXERCISE_DOCUMENT_UPLOAD_ERROR = "exercise.document.upload.error";
	public static final String EXERCISE_DOCUMENT_DELETE_ERROR = "exercise.document.delete.error";
	public static final String EXERCISE_DOCUMENT_INVALID_EXTENSION = "exercise.document.invalid.extension";
	public static final String EXERCISE_DOCUMENT_SIZE_EXCEEDED = "exercise.document.size.exceeded";

	public static final String EXERCISE_STUDENT_DOCUMENT_NOT_FOUND = "exercise.student.document.not.found";
	public static final String EXERCISE_STUDENT_DOCUMENT_EMPTY = "exercise.student.document.empty";
	public static final String EXERCISE_STUDENT_DOCUMENT_UPLOAD_ERROR = "exercise.student.document.upload.error";
	public static final String EXERCISE_STUDENT_DOCUMENT_DELETE_ERROR = "exercise.student.document.delete.error";
	public static final String EXERCISE_STUDENT_DOCUMENT_INVALID_EXTENSION = "exercise.student.document.invalid.extension";
	public static final String EXERCISE_STUDENT_DOCUMENT_SIZE_EXCEEDED = "exercise.student.document.size.exceeded";

	public static final String EXERCISE_STUDENT_GRADE_NOT_FOUND = "exercise.student.grade.not.found";
	public static final String EXERCISE_STUDENT_GRADE_REQUIRED = "exercise.student.grade.required";
	public static final String EXERCISE_STUDENT_GRADE_EXCEEDS_MAX = "exercise.student.grade.exceeds.max";
	public static final String EXERCISE_STUDENT_GRADE_STUDENT_REQUIRED = "exercise.student.grade.student.required";
	public static final String EXERCISE_STUDENT_GRADE_STUDENT_NOT_FOUND = "exercise.student.grade.student.not.found";
	public static final String EXERCISE_STUDENT_GRADE_STUDENT_NOT_IN_CLASS = "exercise.student.grade.student.not.in.class";
	public static final String EXERCISE_STUDENT_GRADE_ALREADY_EXISTS = "exercise.student.grade.already.exists";
	public static final String EXERCISE_STUDENT_GRADE_EXERCISE_NOT_FOUND = "exercise.student.grade.exercise.not.found";

	public static final String CALENDAR_ALERT_NOT_FOUND = "calendar.alert.not.found";
	public static final String CALENDAR_ALERT_VALIDATION_TITLE_REQUIRED = "calendar.alert.validation.title.required";
	public static final String CALENDAR_ALERT_VALIDATION_TITLE_MAX_LENGTH = "calendar.alert.validation.title.maxLength";
	public static final String CALENDAR_ALERT_VALIDATION_DATE_REQUIRED = "calendar.alert.validation.date.required";
	public static final String CALENDAR_ALERT_VALIDATION_END_TIME_WITHOUT_START_TIME = "calendar.alert.validation.endTime.withoutStartTime";
	public static final String CALENDAR_ALERT_VALIDATION_END_TIME_BEFORE_START_TIME = "calendar.alert.validation.endTime.beforeStartTime";
	public static final String CALENDAR_ALERT_VALIDATION_MONTH_INVALID = "calendar.alert.validation.month.invalid";
	public static final String CALENDAR_ALERT_VALIDATION_YEAR_INVALID = "calendar.alert.validation.year.invalid";

	public static final String GRADE_EXPORT_STUDENT = "grade.export.student";
	public static final String GRADE_EXPORT_QUARTER = "grade.export.quarter";
	public static final String GRADE_EXPORT_QUARTER_GRADE = "grade.export.quarter.grade";
	public static final String GRADE_EXPORT_FINAL_GRADE = "grade.export.final.grade";
	public static final String GRADE_EXPORT_MAX_GRADE = "grade.export.max.grade";
	public static final String GRADE_EXPORT_PERCENTAGE = "grade.export.percentage";
	public static final String GRADE_EXPORT_ERROR = "grade.export.error";

	public static final String ABSENCE_NOT_FOUND = "absence.not.found";
	public static final String ABSENCE_VALIDATION_STUDENT_REQUIRED = "absence.validation.student.required";
	public static final String ABSENCE_VALIDATION_DATE_REQUIRED = "absence.validation.date.required";
	public static final String ABSENCE_VALIDATION_DATE_INVALID = "absence.validation.date.invalid";
	public static final String ABSENCE_VALIDATION_CLASS_NOT_FOUND = "absence.validation.class.not.found";
	public static final String ABSENCE_VALIDATION_STUDENT_NOT_FOUND = "absence.validation.student.not.found";
	public static final String ABSENCE_VALIDATION_SUBJECT_NOT_FOUND = "absence.validation.subject.not.found";
	public static final String ABSENCE_VALIDATION_STUDENT_NOT_IN_CLASS = "absence.validation.student.not.in.class";
	public static final String ABSENCE_VALIDATION_SUBJECT_NOT_IN_CLASS = "absence.validation.subject.not.in.class";
	public static final String ABSENCE_VALIDATION_SUBJECT_NOT_SCHEDULED_ON_DAY = "absence.validation.subject.not.scheduled.on.day";
	public static final String ABSENCE_VALIDATION_NO_SUBJECTS_SCHEDULED = "absence.validation.no.subjects.scheduled";

	public static final String SKILL_VALIDATION_TITLE_REQUIRED = "skill.validation.title.required";
	public static final String SKILL_VALIDATION_TITLE_MIN_LENGTH = "skill.validation.title.minLength";
	public static final String SKILL_VALIDATION_DESCRIPTION_REQUIRED = "skill.validation.description.required";
	public static final String SKILL_VALIDATION_DESCRIPTION_MIN_LENGTH = "skill.validation.description.minLength";
	public static final String SKILL_NOT_FOUND = "skill.not.found";
	public static final String SKILL_FORBIDDEN = "skill.forbidden";

	public static final String SKILL_RUBRIC_NOT_FOUND = "skill.rubric.not.found";
	public static final String SKILL_RUBRIC_CRITERIA_NOT_FOUND = "skill.rubric.criteria.not.found";
	public static final String SKILL_RUBRIC_VALIDATION_TITLE_REQUIRED = "skill.rubric.validation.title.required";
	public static final String SKILL_RUBRIC_VALIDATION_CRITERIA_DESCRIPTION_REQUIRED = "skill.rubric.validation.criteria.description.required";
	public static final String SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_START_REQUIRED = "skill.rubric.validation.criteria.gradeStart.required";
	public static final String SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_END_REQUIRED = "skill.rubric.validation.criteria.gradeEnd.required";
	public static final String SKILL_RUBRIC_VALIDATION_CRITERIA_GRADE_RANGE_INVALID = "skill.rubric.validation.criteria.gradeRange.invalid";
	public static final String SKILL_RUBRIC_VALIDATION_CRITERIA_OVERLAP = "skill.rubric.validation.criteria.overlap";

    public static final String CLASS_RUBRIC_NOT_FOUND = "class.rubric.not.found";
    public static final String CLASS_RUBRIC_VALIDATION_CLASS_NOT_FOUND = "class.rubric.validation.class.not.found";
    public static final String CLASS_RUBRIC_VALIDATION_RUBRIC_NOT_FOUND = "class.rubric.validation.rubric.not.found";
    public static final String CLASS_RUBRIC_ALREADY_EXISTS = "class.rubric.already.exists";

    public static final String STUDENT_CLASS_RUBRIC_CRITERIA_NOT_FOUND = "student.class.rubric.criteria.not.found";
    public static final String STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_STUDENT_NOT_FOUND = "student.class.rubric.criteria.validation.student.not.found";
    public static final String STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_STUDENT_NOT_IN_CLASS = "student.class.rubric.criteria.validation.student.not.in.class";
    public static final String STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_CRITERION_NOT_FOUND = "student.class.rubric.criteria.validation.criterion.not.found";
    public static final String STUDENT_CLASS_RUBRIC_CRITERIA_VALIDATION_CRITERION_NOT_IN_RUBRIC = "student.class.rubric.criteria.validation.criterion.not.in.rubric";
    public static final String STUDENT_CLASS_RUBRIC_CRITERIA_ALREADY_EXISTS = "student.class.rubric.criteria.already.exists";

    public static final String STUDENT_GROUP_MIN_STUDENTS = "student.group.min.students";
    public static final String STUDENT_GROUP_IMPOSSIBLE_COUNT = "student.group.impossible.count";
    public static final String STUDENT_GROUP_MISSING_SHAPE = "student.group.missing.shape";

    public static final String SHAPE_NAME_SQUARE = "shape.name.square";
    public static final String SHAPE_NAME_CIRCLE = "shape.name.circle";
    public static final String SHAPE_NAME_TRIANGLE = "shape.name.triangle";

}
