package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GroupAssignmentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupAssignmentServiceImpl implements GroupAssignmentService {

    private static final int QUARTER_MIN = 1;
    private static final int QUARTER_MAX = 3;
    private static final double GRADE_MIN = 0.0;
    private static final double GRADE_MAX = 10.0;

    private final GroupAssignmentRepository groupAssignmentRepository;
    private final GroupAssignmentGradeRepository groupAssignmentGradeRepository;
    private final GroupAssignmentDocumentRepository groupAssignmentDocumentRepository;
    private final ClassRepository classRepository;
    private final SavedStudentGroupRepository savedStudentGroupRepository;
    private final SessionUser sessionUser;
    private final MessageSource messageSource;

    @Override
    public List<GroupAssignment> getAssignmentsByClassId(Integer classId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.validateClassOwnership(classId, teacherId, locale);
        final List<GroupAssignment> assignments = this.groupAssignmentRepository.findByClassId(classId);
        this.enrichAssignmentsWithDocuments(assignments);
        return assignments;
    }

    @Override
    public GroupAssignment createAssignment(Integer classId, GroupAssignment assignment) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.validateClassOwnership(classId, teacherId, locale);

        final List<ErrorMessage> errors = new ArrayList<>();
        this.validateAssignment(assignment, errors, locale);
        if (!errors.isEmpty()) {
            throw new GroupAssignmentValidationException(errors);
        }

        assignment.setClassId(classId);
        return this.groupAssignmentRepository.save(assignment);
    }

    @Override
    public GroupAssignment updateAssignment(Integer assignmentId, GroupAssignment assignment) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final GroupAssignment existing = this.findAssignmentOrThrow(assignmentId, teacherId, locale);

        final List<ErrorMessage> errors = new ArrayList<>();
        this.validateAssignment(assignment, errors, locale);
        if (!errors.isEmpty()) {
            throw new GroupAssignmentValidationException(errors);
        }

        existing.setTitle(assignment.getTitle());
        existing.setDescription(assignment.getDescription());
        existing.setQuarter(assignment.getQuarter());
        return this.groupAssignmentRepository.save(existing);
    }

    @Override
    public void softDeleteAssignment(Integer assignmentId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.findAssignmentOrThrow(assignmentId, teacherId, locale);
        this.groupAssignmentRepository.softDeleteById(assignmentId);
    }

    @Override
    public List<GroupAssignmentGrade> getGradesByAssignmentId(Integer assignmentId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.findAssignmentOrThrow(assignmentId, teacherId, locale);
        final List<GroupAssignmentGrade> grades = this.groupAssignmentGradeRepository.findByAssignmentId(assignmentId);
        this.enrichGradesWithDocuments(assignmentId, grades);
        return grades;
    }

    @Override
    public GroupAssignmentGrade createOrUpdateGrade(Integer assignmentId, Integer groupId, Double grade) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        final GroupAssignment assignment = this.findAssignmentOrThrow(assignmentId, teacherId, locale);

        final List<ErrorMessage> errors = new ArrayList<>();
        this.validateGradeValue(grade, errors, locale);
        this.validateGroupBelongsToClass(groupId, assignment.getClassId(), teacherId, errors, locale);
        if (!errors.isEmpty()) {
            throw new GroupAssignmentValidationException(errors);
        }

        final Optional<GroupAssignmentGrade> existingGrade =
                this.groupAssignmentGradeRepository.findByAssignmentIdAndGroupId(assignmentId, groupId);

        if (existingGrade.isPresent()) {
            final GroupAssignmentGrade gradeToUpdate = existingGrade.get();
            gradeToUpdate.setGrade(grade);
            return this.groupAssignmentGradeRepository.update(gradeToUpdate);
        }

        final GroupAssignmentGrade newGrade = GroupAssignmentGrade.builder()
                .groupAssignmentId(assignmentId)
                .groupId(groupId)
                .grade(grade)
                .build();
        return this.groupAssignmentGradeRepository.save(newGrade);
    }

    @Override
    public void deleteGrade(Integer assignmentId, Integer groupId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();
        this.findAssignmentOrThrow(assignmentId, teacherId, locale);

        final GroupAssignmentGrade existing = this.groupAssignmentGradeRepository
                .findByAssignmentIdAndGroupId(assignmentId, groupId)
                .orElseThrow(() -> new GroupAssignmentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND, null, locale)));
        this.groupAssignmentGradeRepository.softDeleteById(existing.getId());
    }

    private void validateAssignment(GroupAssignment assignment, List<ErrorMessage> errors, Locale locale) {
        if (assignment.getTitle() == null || assignment.getTitle().trim().isEmpty()) {
            errors.add(new ErrorMessage("title",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_TITLE_REQUIRED, null, locale)));
        }

        if (assignment.getQuarter() == null) {
            errors.add(new ErrorMessage("quarter",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_QUARTER_REQUIRED, null, locale)));
        } else if (assignment.getQuarter() < QUARTER_MIN || assignment.getQuarter() > QUARTER_MAX) {
            errors.add(new ErrorMessage("quarter",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_VALIDATION_QUARTER_INVALID, null, locale)));
        }
    }

    private void validateGradeValue(Double grade, List<ErrorMessage> errors, Locale locale) {
        if (grade == null) {
            errors.add(new ErrorMessage("grade",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_GRADE_REQUIRED, null, locale)));
        } else if (grade < GRADE_MIN || grade > GRADE_MAX) {
            errors.add(new ErrorMessage("grade",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_GRADE_INVALID, null, locale)));
        }
    }

    private void validateGroupBelongsToClass(Integer groupId, Integer classId, Integer teacherId,
                                             List<ErrorMessage> errors, Locale locale) {
        final Optional<SavedStudentGroup> group = this.savedStudentGroupRepository.findByIdAndTeacherId(groupId, teacherId);
        if (group.isEmpty()) {
            errors.add(new ErrorMessage("groupId",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_FOUND, null, locale)));
            return;
        }
        if (!group.get().getClassId().equals(classId)) {
            errors.add(new ErrorMessage("groupId",
                    this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_GROUP_NOT_IN_CLASS, null, locale)));
        }
    }

    private void validateClassOwnership(Integer classId, Integer teacherId, Locale locale) {
        this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, locale)));
    }

    private GroupAssignment findAssignmentOrThrow(Integer assignmentId, Integer teacherId, Locale locale) {
        return this.groupAssignmentRepository.findByIdAndTeacherId(assignmentId, teacherId)
                .orElseThrow(() -> new GroupAssignmentNotFoundException(
                        this.messageSource.getMessage(MessageKeys.GROUP_ASSIGNMENT_NOT_FOUND, null, locale)));
    }

    private Integer getTeacherId() {
        return this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
    }

    private void enrichAssignmentsWithDocuments(List<GroupAssignment> assignments) {
        if (assignments.isEmpty()) {
            return;
        }
        final List<Integer> assignmentIds = assignments.stream()
                .map(GroupAssignment::getId)
                .collect(Collectors.toList());
        final List<GroupAssignmentDocument> allDocuments = this.groupAssignmentDocumentRepository
                .findByGroupAssignmentIds(assignmentIds);
        final Map<Integer, List<GroupAssignmentDocument>> documentsByAssignment = allDocuments.stream()
                .collect(Collectors.groupingBy(GroupAssignmentDocument::getGroupAssignmentId));
        assignments.forEach(assignment ->
                assignment.setDocuments(documentsByAssignment.getOrDefault(assignment.getId(), Collections.emptyList())));
    }

    private void enrichGradesWithDocuments(Integer assignmentId, List<GroupAssignmentGrade> grades) {
        if (grades.isEmpty()) {
            return;
        }
        final List<GroupAssignmentDocument> allDocuments = this.groupAssignmentDocumentRepository
                .findByAssignmentId(assignmentId);
        final Map<Integer, List<GroupAssignmentDocument>> documentsByGroup = allDocuments.stream()
                .filter(doc -> doc.getGroupId() != null)
                .collect(Collectors.groupingBy(GroupAssignmentDocument::getGroupId));
        grades.forEach(grade ->
                grade.setDocuments(documentsByGroup.getOrDefault(grade.getGroupId(), Collections.emptyList())));
    }
}

