package org.web.codefm.domain.service.teachernotebook;

/**
 * Service interface for grade export operations.
 * Handles the generation of Excel files with student grades.
 */
public interface GradeExportService {

    /**
     * Exports all student grades for a class as an Excel file.
     * Validates class ownership, retrieves students, exercises and grades,
     * and generates a workbook with one sheet per subject that has at least one grade.
     *
     * @param classId The unique identifier of the class
     * @return Byte array containing the Excel file (.xlsx)
     */
    byte[] exportGradesByClassId(Integer classId);
}

