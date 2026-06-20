package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.GroupAssignmentService;
import org.web.codefm.domain.usecase.teachernotebook.GroupAssignmentUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupAssignmentUseCaseImpl implements GroupAssignmentUseCase {

    private final GroupAssignmentService groupAssignmentService;
    private final CascadeSoftDeleteService cascadeSoftDeleteService;

    @Override
    public List<GroupAssignment> getAssignmentsByClassId(Integer classId) {
        return this.groupAssignmentService.getAssignmentsByClassId(classId);
    }

    @Override
    public GroupAssignment createAssignment(Integer classId, GroupAssignment assignment) {
        return this.groupAssignmentService.createAssignment(classId, assignment);
    }

    @Override
    public GroupAssignment updateAssignment(Integer assignmentId, GroupAssignment assignment) {
        return this.groupAssignmentService.updateAssignment(assignmentId, assignment);
    }

    @Override
    @Transactional
    public void softDeleteAssignment(Integer assignmentId) {
        this.cascadeSoftDeleteService.cascadeDeleteChildrenOfGroupAssignment(assignmentId);
        this.groupAssignmentService.softDeleteAssignment(assignmentId);
    }

    @Override
    public List<GroupAssignmentGrade> getGradesByAssignmentId(Integer assignmentId) {
        return this.groupAssignmentService.getGradesByAssignmentId(assignmentId);
    }

    @Override
    public GroupAssignmentGrade createOrUpdateGrade(Integer assignmentId, Integer groupId, Double grade) {
        return this.groupAssignmentService.createOrUpdateGrade(assignmentId, groupId, grade);
    }

    @Override
    public void deleteGrade(Integer assignmentId, Integer groupId) {
        this.groupAssignmentService.deleteGrade(assignmentId, groupId);
    }
}

