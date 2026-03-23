package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.SavedStudentGroupValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.SavedStudentGroupRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.service.teachernotebook.SavedStudentGroupService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedStudentGroupServiceImpl implements SavedStudentGroupService {

    private static final String PARAM_STUDENT_IDS = "studentIds";

    private final SavedStudentGroupRepository savedStudentGroupRepository;
    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;
    private final SessionUser sessionUser;
    private final MessageSource messageSource;

    @Override
    public List<SavedStudentGroup> getSavedGroupsByClassId(Integer classId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        this.validateClassOwnership(classId, teacherId, locale);

        return this.savedStudentGroupRepository.findByClassIdWithMembers(classId);
    }

    @Override
    @Transactional
    public List<SavedStudentGroup> createSavedGroups(Integer classId, List<SavedStudentGroup> groups) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        this.validateClassOwnership(classId, teacherId, locale);

        if (!this.savedStudentGroupRepository.findActiveIdsByClassId(classId).isEmpty()) {
            final String message = this.messageSource.getMessage(
                    MessageKeys.SAVED_GROUP_CLASS_ALREADY_HAS_GROUPS, null, locale);
            throw new SavedStudentGroupValidationException(
                    List.of(new ErrorMessage("classId", message)));
        }

        final List<Integer> activeStudentIds = this.studentClassRepository.findActiveStudentIdsByClassId(classId);
        final List<Student> teacherStudents = this.studentRepository.findByIdsAndTeacherIdAndDeletionDateIsNull(
                activeStudentIds, teacherId);
        final Set<Integer> validStudentIds = teacherStudents.stream().map(Student::getId).collect(Collectors.toSet());
        final Map<Integer, String> studentNameMap = teacherStudents.stream()
                .collect(Collectors.toMap(Student::getId, s -> s.getName() + " " + s.getSurnames()));

        for (SavedStudentGroup group : groups) {
            this.validateGroup(group, validStudentIds, locale);
        }

        this.validateNoDuplicatesAcrossGroups(groups, studentNameMap, locale);
        this.validateAllStudentsAssigned(groups, validStudentIds, studentNameMap, locale);

        final List<SavedStudentGroup> groupsToCreate = groups.stream()
                .map(g -> SavedStudentGroup.builder()
                        .classId(classId)
                        .name(g.getName().trim())
                        .build())
                .toList();
        final List<SavedStudentGroup> savedGroups = this.savedStudentGroupRepository.saveAll(groupsToCreate);

        final List<SavedStudentGroupMember> allMembers = new ArrayList<>();
        for (int i = 0; i < savedGroups.size(); i++) {
            final Integer groupId = savedGroups.get(i).getId();
            groups.get(i).getMembers().forEach(m -> allMembers.add(
                    SavedStudentGroupMember.builder().studentGroupId(groupId).studentId(m.getStudentId()).build()));
        }
        this.savedStudentGroupRepository.saveMembers(allMembers);

        for (int i = 0; i < savedGroups.size(); i++) {
            final Integer groupId = savedGroups.get(i).getId();
            savedGroups.get(i).setMembers(groups.get(i).getMembers().stream()
                    .map(m -> SavedStudentGroupMember.builder().studentGroupId(groupId).studentId(m.getStudentId()).build())
                    .toList());
        }
        return savedGroups;
    }

    @Override
    @Transactional
    public List<SavedStudentGroup> updateAllSavedGroups(Integer classId, List<SavedStudentGroup> groups) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        this.validateClassOwnership(classId, teacherId, locale);

        final List<Integer> activeGroupIds = this.savedStudentGroupRepository.findActiveIdsByClassId(classId);
        this.validateGroupIdsForUpdate(groups, activeGroupIds, locale);

        final List<Integer> activeStudentIds = this.studentClassRepository.findActiveStudentIdsByClassId(classId);
        final List<Student> teacherStudents = this.studentRepository.findByIdsAndTeacherIdAndDeletionDateIsNull(
                activeStudentIds, teacherId);
        final Set<Integer> validStudentIds = teacherStudents.stream().map(Student::getId).collect(Collectors.toSet());
        final Map<Integer, String> studentNameMap = teacherStudents.stream()
                .collect(Collectors.toMap(Student::getId, s -> s.getName() + " " + s.getSurnames()));

        for (SavedStudentGroup group : groups) {
            this.validateGroup(group, validStudentIds, locale);
        }

        this.validateNoDuplicatesAcrossGroups(groups, studentNameMap, locale);
        this.validateAllStudentsAssigned(groups, validStudentIds, studentNameMap, locale);

        groups.forEach(g -> this.savedStudentGroupRepository.updateName(g.getId(), g.getName().trim()));

        final List<Integer> allGroupIds = groups.stream().map(SavedStudentGroup::getId).toList();
        this.savedStudentGroupRepository.hardDeleteMembersByGroupIds(allGroupIds);

        final List<SavedStudentGroupMember> allMembers = groups.stream()
                .flatMap(g -> g.getMembers().stream()
                        .map(m -> SavedStudentGroupMember.builder()
                                .studentGroupId(g.getId())
                                .studentId(m.getStudentId())
                                .build()))
                .toList();
        this.savedStudentGroupRepository.saveMembers(allMembers);

        return groups.stream()
                .map(g -> {
                    final SavedStudentGroup result = SavedStudentGroup.builder()
                            .id(g.getId())
                            .classId(classId)
                            .name(g.getName().trim())
                            .build();
                    result.setMembers(g.getMembers().stream()
                            .map(m -> SavedStudentGroupMember.builder()
                                    .studentGroupId(g.getId())
                                    .studentId(m.getStudentId())
                                    .build())
                            .toList());
                    return result;
                })
                .toList();
    }


    @Override
    @Transactional
    public void softDeleteSavedGroupsByClassId(Integer classId) {
        final Integer teacherId = this.getTeacherId();
        final Locale locale = this.sessionUser.getLocale();

        this.validateClassOwnership(classId, teacherId, locale);

        final List<Integer> groupIds = this.savedStudentGroupRepository.findActiveIdsByClassId(classId);
        this.savedStudentGroupRepository.hardDeleteMembersByGroupIds(groupIds);
        this.savedStudentGroupRepository.softDeleteByClassId(classId);
    }

    private void validateClassOwnership(Integer classId, Integer teacherId, Locale locale) {
        this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, locale)));
    }

    private void validateGroupIdsForUpdate(List<SavedStudentGroup> groups, List<Integer> activeGroupIds, Locale locale) {
        final List<ErrorMessage> errors = new ArrayList<>();

        for (SavedStudentGroup group : groups) {
            if (group.getId() == null) {
                final String message = this.messageSource.getMessage(
                        MessageKeys.SAVED_GROUP_VALIDATION_GROUP_ID_REQUIRED, null, locale);
                errors.add(new ErrorMessage("id", message));
            } else if (!activeGroupIds.contains(group.getId())) {
                final String message = this.messageSource.getMessage(
                        MessageKeys.SAVED_GROUP_VALIDATION_GROUP_NOT_FOUND,
                        new Object[]{group.getId()}, locale);
                errors.add(new ErrorMessage("id", message));
            }
        }

        if (!errors.isEmpty()) {
            throw new SavedStudentGroupValidationException(errors);
        }
    }

    private void validateGroup(SavedStudentGroup group, Set<Integer> validStudentIds, Locale locale) {
        final List<ErrorMessage> errors = new ArrayList<>();

        if (group.getName() == null || group.getName().trim().isEmpty()) {
            final String message = this.messageSource.getMessage(
                    MessageKeys.SAVED_GROUP_VALIDATION_NAME_REQUIRED, null, locale);
            errors.add(new ErrorMessage("name", message));
        }

        if (group.getMembers() == null || group.getMembers().isEmpty()) {
            final String message = this.messageSource.getMessage(
                    MessageKeys.SAVED_GROUP_VALIDATION_STUDENTS_REQUIRED, null, locale);
            errors.add(new ErrorMessage(PARAM_STUDENT_IDS, message));
        } else {
            for (SavedStudentGroupMember member : group.getMembers()) {
                if (!validStudentIds.contains(member.getStudentId())) {
                    final String message = this.messageSource.getMessage(
                            MessageKeys.SAVED_GROUP_VALIDATION_STUDENT_NOT_IN_CLASS,
                            new Object[]{member.getStudentId()}, locale);
                    errors.add(new ErrorMessage(PARAM_STUDENT_IDS, message));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new SavedStudentGroupValidationException(errors);
        }
    }

    private void validateNoDuplicatesAcrossGroups(List<SavedStudentGroup> groups, Map<Integer, String> studentNameMap, Locale locale) {
        final List<ErrorMessage> errors = new ArrayList<>();
        final Set<Integer> seen = new HashSet<>();

        for (SavedStudentGroup group : groups) {
            if (group.getMembers() == null) {
                continue;
            }
            for (SavedStudentGroupMember member : group.getMembers()) {
                if (!seen.add(member.getStudentId())) {
                    final String studentName = studentNameMap.getOrDefault(member.getStudentId(), String.valueOf(member.getStudentId()));
                    final String message = this.messageSource.getMessage(
                            MessageKeys.SAVED_GROUP_VALIDATION_STUDENT_DUPLICATE,
                            new Object[]{studentName}, locale);
                    errors.add(new ErrorMessage(PARAM_STUDENT_IDS, message));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new SavedStudentGroupValidationException(errors);
        }
    }

    private void validateAllStudentsAssigned(List<SavedStudentGroup> groups, Set<Integer> validStudentIds,
                                             Map<Integer, String> studentNameMap, Locale locale) {
        final Set<Integer> assignedStudentIds = groups.stream()
                .filter(g -> g.getMembers() != null)
                .flatMap(g -> g.getMembers().stream())
                .map(SavedStudentGroupMember::getStudentId)
                .collect(Collectors.toSet());

        final Set<Integer> missingStudentIds = new LinkedHashSet<>(validStudentIds);
        missingStudentIds.removeAll(assignedStudentIds);

        if (!missingStudentIds.isEmpty()) {
            final String missingNames = missingStudentIds.stream()
                    .map(id -> studentNameMap.getOrDefault(id, String.valueOf(id)))
                    .collect(Collectors.joining(", "));
            final String message = this.messageSource.getMessage(
                    MessageKeys.SAVED_GROUP_VALIDATION_STUDENTS_NOT_ALL_ASSIGNED,
                    new Object[]{missingNames}, locale);
            throw new SavedStudentGroupValidationException(
                    List.of(new ErrorMessage(PARAM_STUDENT_IDS, message)));
        }
    }

    private Integer getTeacherId() {
        return this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
    }
}
