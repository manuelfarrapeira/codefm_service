package org.web.codefm.infrastructure.teachernotebook;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroupMember;
import org.web.codefm.domain.repository.teachernotebook.SavedStudentGroupRepository;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentGroupEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentGroupMemberEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentGroupJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentGroupMemberJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentJPARepository;
import org.web.codefm.infrastructure.mapper.SavedStudentGroupMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class SavedStudentGroupRepositoryImpl implements SavedStudentGroupRepository {

    private final StudentGroupJPARepository studentGroupJPARepository;
    private final StudentGroupMemberJPARepository studentGroupMemberJPARepository;
    private final StudentJPARepository studentJPARepository;
    private final SavedStudentGroupMapper savedStudentGroupMapper;
    private final JdbcTemplate jdbcTemplate;

    public SavedStudentGroupRepositoryImpl(
            StudentGroupJPARepository studentGroupJPARepository,
            StudentGroupMemberJPARepository studentGroupMemberJPARepository,
            StudentJPARepository studentJPARepository,
            SavedStudentGroupMapper savedStudentGroupMapper,
            @Qualifier("teacherNotebookJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.studentGroupJPARepository = studentGroupJPARepository;
        this.studentGroupMemberJPARepository = studentGroupMemberJPARepository;
        this.studentJPARepository = studentJPARepository;
        this.savedStudentGroupMapper = savedStudentGroupMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SavedStudentGroup> findByClassIdWithMembers(Integer classId) {
        final List<StudentGroupEntity> groupEntities = this.studentGroupJPARepository.findByClassIdAndDeletionDateIsNull(classId);
        if (groupEntities.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Integer> groupIds = groupEntities.stream().map(StudentGroupEntity::getId).toList();
        final List<StudentGroupMemberEntity> memberEntities = this.studentGroupMemberJPARepository.findByStudentGroupIdIn(groupIds);

        final Set<Integer> studentIds = memberEntities.stream().map(StudentGroupMemberEntity::getStudentId).collect(Collectors.toSet());
        final Map<Integer, String[]> studentNameMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            this.studentJPARepository.findAllById(studentIds).forEach(s ->
                    studentNameMap.put(s.getId(), new String[]{s.getName(), s.getSurnames()}));
        }

        final Map<Integer, List<SavedStudentGroupMember>> membersByGroupId = memberEntities.stream()
                .collect(Collectors.groupingBy(StudentGroupMemberEntity::getStudentGroupId,
                        Collectors.mapping(m -> {
                            final String[] names = studentNameMap.getOrDefault(m.getStudentId(), new String[]{"", ""});
                            return SavedStudentGroupMember.builder()
                                    .id(m.getId())
                                    .studentGroupId(m.getStudentGroupId())
                                    .studentId(m.getStudentId())
                                    .studentName(names[0])
                                    .studentSurnames(names[1])
                                    .build();
                        }, Collectors.toList())));

        return groupEntities.stream().map(entity -> {
            final SavedStudentGroup group = this.savedStudentGroupMapper.toModel(entity);
            group.setMembers(membersByGroupId.getOrDefault(entity.getId(), Collections.emptyList()));
            return group;
        }).toList();
    }

    @Override
    public Optional<SavedStudentGroup> findByIdAndTeacherId(Integer groupId, Integer teacherId) {
        return this.studentGroupJPARepository.findByIdAndTeacherId(groupId, teacherId)
                .map(this.savedStudentGroupMapper::toModel);
    }

    @Override
    public SavedStudentGroup save(SavedStudentGroup group) {
        final StudentGroupEntity entity = this.savedStudentGroupMapper.toEntity(group);
        final StudentGroupEntity saved = this.studentGroupJPARepository.save(entity);
        return this.savedStudentGroupMapper.toModel(saved);
    }

    @Override
    public List<SavedStudentGroup> saveAll(List<SavedStudentGroup> groups) {
        final List<StudentGroupEntity> entities = groups.stream()
                .map(this.savedStudentGroupMapper::toEntity)
                .toList();
        return this.studentGroupJPARepository.saveAll(entities).stream()
                .map(this.savedStudentGroupMapper::toModel)
                .toList();
    }

    @Override
    public void updateName(Integer id, String name) {
        this.studentGroupJPARepository.updateNameById(id, name);
    }

    @Override
    public void saveMembers(List<SavedStudentGroupMember> members) {
        this.jdbcTemplate.batchUpdate(
                "INSERT INTO student_group_members (student_group_id, student_id) VALUES (?, ?)",
                members.stream()
                        .map(m -> new Object[]{m.getStudentGroupId(), m.getStudentId()})
                        .toList());
    }

    @Override
    public void softDeleteById(Integer groupId) {
        this.studentGroupJPARepository.softDeleteById(groupId);
    }

    @Override
    public void hardDeleteMembersByGroupId(Integer groupId) {
        this.studentGroupMemberJPARepository.deleteByStudentGroupId(groupId);
    }

    @Override
    public void softDeleteByClassId(Integer classId) {
        this.studentGroupJPARepository.softDeleteByClassId(classId);
    }

    @Override
    public void hardDeleteMembersByGroupIds(List<Integer> groupIds) {
        if (!groupIds.isEmpty()) {
            this.studentGroupMemberJPARepository.deleteByStudentGroupIdIn(groupIds);
        }
    }

    @Override
    public List<Integer> findActiveIdsByClassId(Integer classId) {
        return this.studentGroupJPARepository.findActiveIdsByClassId(classId);
    }
}
