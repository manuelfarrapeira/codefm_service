package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentGrade;
import org.web.codefm.domain.repository.teachernotebook.GroupAssignmentGradeRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.GroupAssignmentGradeEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentGradeJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentGradeMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupAssignmentGradeRepositoryImpl implements GroupAssignmentGradeRepository {

    private final GroupAssignmentGradeJPARepository groupAssignmentGradeJPARepository;
    private final GroupAssignmentGradeMapper groupAssignmentGradeMapper;

    @Override
    public List<GroupAssignmentGrade> findByAssignmentId(Integer assignmentId) {
        return this.groupAssignmentGradeMapper.toModelList(
                this.groupAssignmentGradeJPARepository.findByGroupAssignmentIdAndDeletionDateIsNull(assignmentId));
    }

    @Override
    public Optional<GroupAssignmentGrade> findByAssignmentIdAndGroupId(Integer assignmentId, Integer groupId) {
        return this.groupAssignmentGradeJPARepository
                .findByGroupAssignmentIdAndGroupIdAndDeletionDateIsNull(assignmentId, groupId)
                .map(this.groupAssignmentGradeMapper::toModel);
    }

    @Override
    public GroupAssignmentGrade save(GroupAssignmentGrade grade) {
        return this.groupAssignmentGradeMapper.toModel(
                this.groupAssignmentGradeJPARepository.save(this.groupAssignmentGradeMapper.toEntity(grade)));
    }

    @Override
    public GroupAssignmentGrade update(GroupAssignmentGrade grade) {
        final GroupAssignmentGradeEntity entity = this.groupAssignmentGradeMapper.toEntity(grade);
        return this.groupAssignmentGradeMapper.toModel(this.groupAssignmentGradeJPARepository.save(entity));
    }

    @Override
    public void softDeleteById(Integer gradeId) {
        this.groupAssignmentGradeJPARepository.softDeleteById(gradeId);
    }

    @Override
    public void softDeleteByGroupAssignmentId(Integer assignmentId) {
        this.groupAssignmentGradeJPARepository.softDeleteByGroupAssignmentId(assignmentId);
    }

    @Override
    public void softDeleteByGroupAssignmentIds(List<Integer> assignmentIds) {
        if (assignmentIds != null && !assignmentIds.isEmpty()) {
            this.groupAssignmentGradeJPARepository.softDeleteByGroupAssignmentIds(assignmentIds);
        }
    }

    @Override
    public void softDeleteByGroupId(Integer groupId) {
        this.groupAssignmentGradeJPARepository.softDeleteByGroupId(groupId);
    }

    @Override
    public void softDeleteByGroupIds(List<Integer> groupIds) {
        if (groupIds != null && !groupIds.isEmpty()) {
            this.groupAssignmentGradeJPARepository.softDeleteByGroupIds(groupIds);
        }
    }
}

