package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.SavedStudentGroup;
import org.web.codefm.domain.service.teachernotebook.SavedStudentGroupService;
import org.web.codefm.domain.usecase.teachernotebook.SavedStudentGroupUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedStudentGroupUseCaseImpl implements SavedStudentGroupUseCase {

    private final SavedStudentGroupService savedStudentGroupService;

    @Override
    public List<SavedStudentGroup> getSavedGroupsByClassId(Integer classId) {
        return this.savedStudentGroupService.getSavedGroupsByClassId(classId);
    }

    @Override
    public List<SavedStudentGroup> createSavedGroups(Integer classId, List<SavedStudentGroup> groups) {
        return this.savedStudentGroupService.createSavedGroups(classId, groups);
    }

    @Override
    public List<SavedStudentGroup> updateAllSavedGroups(Integer classId, List<SavedStudentGroup> groups) {
        return this.savedStudentGroupService.updateAllSavedGroups(classId, groups);
    }


    @Override
    public void softDeleteSavedGroupsByClassId(Integer classId) {
        this.savedStudentGroupService.softDeleteSavedGroupsByClassId(classId);
    }
}
