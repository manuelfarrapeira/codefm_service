package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.service.teachernotebook.StudentGroupService;
import org.web.codefm.domain.usecase.teachernotebook.StudentGroupUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentGroupUseCaseImpl implements StudentGroupUseCase {

    private final StudentGroupService studentGroupService;

    @Override
    public List<List<Integer>> generateGroups(Integer classId, Boolean prioritizeShapeDiversity) {
        return this.studentGroupService.generateGroups(classId, prioritizeShapeDiversity);
    }
}
