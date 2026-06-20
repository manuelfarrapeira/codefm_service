package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignment;
import org.web.codefm.domain.repository.teachernotebook.GroupAssignmentRepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupAssignmentRepositoryImpl implements GroupAssignmentRepository {

    private final GroupAssignmentJPARepository groupAssignmentJPARepository;
    private final GroupAssignmentMapper groupAssignmentMapper;

    @Override
    public List<GroupAssignment> findByClassId(Integer classId) {
        return this.groupAssignmentMapper.toModelList(
                this.groupAssignmentJPARepository.findByClassIdAndDeletionDateIsNull(classId));
    }

    @Override
    public Optional<GroupAssignment> findByIdAndTeacherId(Integer assignmentId, Integer teacherId) {
        return this.groupAssignmentJPARepository.findByIdAndTeacherId(assignmentId, teacherId)
                .map(this.groupAssignmentMapper::toModel);
    }

    @Override
    public GroupAssignment save(GroupAssignment groupAssignment) {
        return this.groupAssignmentMapper.toModel(
                this.groupAssignmentJPARepository.save(this.groupAssignmentMapper.toEntity(groupAssignment)));
    }

    @Override
    public void softDeleteById(Integer assignmentId) {
        this.groupAssignmentJPARepository.softDeleteById(assignmentId);
    }

    @Override
    public void softDeleteByClassId(Integer classId) {
        this.groupAssignmentJPARepository.softDeleteByClassId(classId);
    }

    @Override
    public List<Integer> findActiveIdsByClassId(Integer classId) {
        return this.groupAssignmentJPARepository.findActiveIdsByClassId(classId);
    }
}

