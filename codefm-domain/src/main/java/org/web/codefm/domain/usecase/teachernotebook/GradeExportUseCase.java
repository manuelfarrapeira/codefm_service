package org.web.codefm.domain.usecase.teachernotebook;

/**
 * Interface that defines grade export operations for teachers.
 * Handles generation of Excel files with student grades for a class.
 */
public interface GradeExportUseCase {

    /**
     * Exports all student grades for a class as an Excel file.
     * Generates one sheet per subject that has at least one grade.
     *
     * @param classId The unique identifier of the class
     * @return Byte array containing the Excel file (.xlsx)
     */
    byte[] exportGradesByClassId(Integer classId);
}

