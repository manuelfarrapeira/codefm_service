package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.GroupAssignmentDocument;
import org.web.codefm.domain.repository.teachernotebook.GroupAssignmentDocumentRepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.GroupAssignmentDocumentJPARepository;
import org.web.codefm.infrastructure.mapper.GroupAssignmentDocumentMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupAssignmentDocumentRepositoryImpl implements GroupAssignmentDocumentRepository {

    private final GroupAssignmentDocumentJPARepository groupAssignmentDocumentJPARepository;
    private final GroupAssignmentDocumentMapper groupAssignmentDocumentMapper;

    @Override
    public List<GroupAssignmentDocument> findByAssignmentId(Integer assignmentId) {
        return this.groupAssignmentDocumentMapper.toModelList(
                this.groupAssignmentDocumentJPARepository.findByGroupAssignmentId(assignmentId));
    }

    @Override
    public List<GroupAssignmentDocument> findByAssignmentIdAndGroupId(Integer assignmentId, Integer groupId) {
        return this.groupAssignmentDocumentMapper.toModelList(
                this.groupAssignmentDocumentJPARepository.findByGroupAssignmentIdAndGroupId(assignmentId, groupId));
    }

    @Override
    public Optional<GroupAssignmentDocument> findById(Integer id) {
        return this.groupAssignmentDocumentJPARepository.findById(id)
                .map(this.groupAssignmentDocumentMapper::toModel);
    }

    @Override
    public GroupAssignmentDocument save(GroupAssignmentDocument document) {
        return this.groupAssignmentDocumentMapper.toModel(
                this.groupAssignmentDocumentJPARepository.save(this.groupAssignmentDocumentMapper.toEntity(document)));
    }

    @Override
    public void deleteById(Integer id) {
        this.groupAssignmentDocumentJPARepository.deleteById(id);
    }

    @Override
    public void deleteByGroupAssignmentId(Integer assignmentId) {
        this.groupAssignmentDocumentJPARepository.deleteByGroupAssignmentId(assignmentId);
    }

    @Override
    public void deleteByGroupAssignmentIds(List<Integer> assignmentIds) {
        if (assignmentIds != null && !assignmentIds.isEmpty()) {
            this.groupAssignmentDocumentJPARepository.deleteByGroupAssignmentIds(assignmentIds);
        }
    }

    @Override
    public void deleteByGroupId(Integer groupId) {
        this.groupAssignmentDocumentJPARepository.deleteByGroupId(groupId);
    }

    @Override
    public void deleteByGroupIds(List<Integer> groupIds) {
        if (groupIds != null && !groupIds.isEmpty()) {
            this.groupAssignmentDocumentJPARepository.deleteByGroupIds(groupIds);
        }
    }

    @Override
    public List<GroupAssignmentDocument> findByGroupAssignmentIds(List<Integer> assignmentIds) {
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            return List.of();
        }
        return this.groupAssignmentDocumentMapper.toModelList(
                this.groupAssignmentDocumentJPARepository.findByGroupAssignmentIdIn(assignmentIds));
    }

    @Override
    public List<GroupAssignmentDocument> findByGroupIds(List<Integer> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return List.of();
        }
        return this.groupAssignmentDocumentMapper.toModelList(
                this.groupAssignmentDocumentJPARepository.findByGroupIdIn(groupIds));
    }
}

