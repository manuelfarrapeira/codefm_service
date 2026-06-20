package org.web.codefm.usecase.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.service.teachernotebook.GradeExportService;
import org.web.codefm.domain.usecase.teachernotebook.GradeExportUseCase;

@Service
@RequiredArgsConstructor
public class GradeExportUseCaseImpl implements GradeExportUseCase {

    private final GradeExportService gradeExportService;

    @Override
    public byte[] exportGradesByClassId(Integer classId) {
        return gradeExportService.exportGradesByClassId(classId);
    }
}

