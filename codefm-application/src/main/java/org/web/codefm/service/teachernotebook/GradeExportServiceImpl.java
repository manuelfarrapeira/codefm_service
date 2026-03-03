package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.GradeExportException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.service.teachernotebook.GradeExportService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeExportServiceImpl implements GradeExportService {

    private static final String LIGHT_RED_HEX = "FFC7CE";
    private static final String LIGHT_GREEN_HEX = "C6EFCE";
    private static final String LIGHT_BLUE_HEX = "BDD7EE";
    private static final String ALIGN_CENTER = "center";
    private static final double FAILING_THRESHOLD_RATIO = 0.5;
    private static final double FAILING_FINAL_GRADE = 5.0;

    private final ClassRepository classRepository;
    private final StudentClassRepository studentClassRepository;
    private final StudentRepository studentRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseStudentGradeRepository exerciseStudentGradeRepository;
    private final SessionUser sessionUser;
    private final MessageSource messageSource;

    @Override
    public byte[] exportGradesByClassId(Integer classId) {
        Integer teacherId = getTeacherId();
        Locale locale = sessionUser.getLocale();

        validateClassOwnership(classId, teacherId, locale);

        List<Student> students = loadStudentsByClassId(classId, teacherId);
        List<Exercise> exercises = exerciseRepository.findByClassId(classId);
        List<ExerciseStudentGrade> grades = exerciseStudentGradeRepository.findByClassId(classId);

        Map<String, Double> gradeMap = buildGradeMap(grades);
        Map<String, String> descriptionMap = buildDescriptionMap(grades);
        Map<Integer, Map<Integer, List<Exercise>>> exercisesBySubjectAndQuarter = groupExercises(exercises);
        Set<Integer> subjectsWithGrades = findSubjectsWithGrades(exercises, gradeMap);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Workbook workbook = new Workbook(out, "CodeFM", "1.0");
            boolean hasSheets = false;

            for (Map.Entry<Integer, Map<Integer, List<Exercise>>> subjectEntry : exercisesBySubjectAndQuarter.entrySet()) {
                Integer subjectId = subjectEntry.getKey();
                if (!subjectsWithGrades.contains(subjectId)) {
                    continue;
                }

                Map<Integer, List<Exercise>> quarterMap = subjectEntry.getValue();
                String subjectName = getSubjectName(quarterMap);
                String sheetName = sanitizeSheetName(subjectName);

                Worksheet ws = workbook.newWorksheet(sheetName);
                buildSheet(ws, students, quarterMap, gradeMap, descriptionMap, locale);
                ws.finish();
                hasSheets = true;
            }

            if (!hasSheets) {
                String emptyLabel = messageSource.getMessage(MessageKeys.GRADE_EXPORT_STUDENT, null, locale);
                try (Worksheet ws = workbook.newWorksheet(emptyLabel)) {
                    ws.finish();
                }
            }

            workbook.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new GradeExportException(
                    messageSource.getMessage(MessageKeys.GRADE_EXPORT_ERROR, null, locale), e
            );
        }
    }

    private void buildSheet(Worksheet ws, List<Student> students,
                            Map<Integer, List<Exercise>> quarterMap, Map<String, Double> gradeMap,
                            Map<String, String> descriptionMap, Locale locale) {
        List<Integer> sortedQuarters = new TreeMap<>(quarterMap).keySet().stream().toList();
        List<ColumnInfo> columns = buildColumnLayout(sortedQuarters, quarterMap, locale);

        writeGroupRow(ws, columns, sortedQuarters, locale);
        writeHeaderRow(ws, columns);
        writeDataRows(ws, students, columns, gradeMap, descriptionMap);
        setColumnWidths(ws, columns);
    }

    private void writeGroupRow(Worksheet ws, List<ColumnInfo> columns, List<Integer> sortedQuarters,
                               Locale locale) {
        for (Integer quarter : sortedQuarters) {
            int[] colRange = findColumnRange(columns, quarter);
            int startCol = colRange[0];
            int endCol = colRange[1];

            if (startCol == -1) {
                continue;
            }

            if (startCol < endCol) {
                ws.range(0, startCol, 0, endCol).merge();
                ws.range(0, startCol, 0, endCol).style()
                        .bold()
                        .fontSize(12)
                        .horizontalAlignment(ALIGN_CENTER)
                        .fillColor(Color.LIGHT_CORNFLOWER_BLUE)
                        .borderStyle("thin")
                        .set();
            } else {
                ws.style(0, startCol)
                        .bold()
                        .fontSize(12)
                        .horizontalAlignment(ALIGN_CENTER)
                        .fillColor(Color.LIGHT_CORNFLOWER_BLUE)
                        .borderStyle("thin")
                        .set();
            }

            String quarterLabel = messageSource.getMessage(
                    MessageKeys.GRADE_EXPORT_QUARTER, new Object[]{quarter}, locale);
            ws.value(0, startCol, quarterLabel);
        }
    }

    private int[] findColumnRange(List<ColumnInfo> columns, Integer quarter) {
        int startCol = -1;
        int endCol = -1;

        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo col = columns.get(i);
            if (col.quarter != null && col.quarter.equals(quarter)) {
                if (startCol == -1) startCol = i;
                endCol = i;
            }
        }

        return new int[]{startCol, endCol};
    }

    private void writeHeaderRow(Worksheet ws, List<ColumnInfo> columns) {
        for (int i = 0; i < columns.size(); i++) {
            ws.value(1, i, columns.get(i).headerText);
            ws.style(1, i)
                    .bold()
                    .horizontalAlignment(ALIGN_CENTER)
                    .borderStyle("thin")
                    .set();

            String subHeader = columns.get(i).subHeaderText;
            if (subHeader != null) {
                ws.comment(1, i, subHeader);
            }
        }
    }

    private void writeDataRows(Worksheet ws, List<Student> students, List<ColumnInfo> columns,
                               Map<String, Double> gradeMap, Map<String, String> descriptionMap) {
        for (int rowIdx = 0; rowIdx < students.size(); rowIdx++) {
            Student student = students.get(rowIdx);
            int excelRow = rowIdx + 2;

            ws.value(excelRow, 0, student.getSurnames() + ", " + student.getName());
            ws.style(excelRow, 0).borderStyle("thin").set();

            for (int colIdx = 1; colIdx < columns.size(); colIdx++) {
                ColumnInfo col = columns.get(colIdx);

                if (col.type == ColumnType.EXERCISE) {
                    writeExerciseCell(ws, excelRow, colIdx, student, col, gradeMap, descriptionMap);
                } else if (col.type == ColumnType.QUARTER_GRADE) {
                    writeQuarterGradeCell(ws, excelRow, colIdx, columns, col.quarter, student, gradeMap);
                } else if (col.type == ColumnType.FINAL_GRADE) {
                    writeFinalGradeCell(ws, excelRow, colIdx, columns, student, gradeMap);
                }
            }
        }
    }

    private void writeExerciseCell(Worksheet ws, int row, int col, Student student,
                                   ColumnInfo colInfo, Map<String, Double> gradeMap,
                                   Map<String, String> descriptionMap) {
        String key = student.getId() + "_" + colInfo.exerciseId;
        Double grade = gradeMap.get(key);

        if (grade != null) {
            ws.value(row, col, grade);
            if (grade < colInfo.maxGrade * FAILING_THRESHOLD_RATIO) {
                ws.style(row, col).fillColor(LIGHT_RED_HEX).borderStyle("thin").set();
            } else {
                ws.style(row, col).borderStyle("thin").set();
            }
        } else {
            ws.style(row, col).borderStyle("thin").set();
        }

        String description = descriptionMap.get(key);
        if (description != null && !description.isBlank()) {
            ws.comment(row, col, description);
        }
    }

    private void writeQuarterGradeCell(Worksheet ws, int row, int col,
                                       List<ColumnInfo> columns, Integer quarter,
                                       Student student, Map<String, Double> gradeMap) {
        boolean hasAnyGrade = hasAnyGradeInQuarter(columns, quarter, student, gradeMap);
        String fillColor = LIGHT_GREEN_HEX;

        if (hasAnyGrade) {
            double calculatedGrade = calculateQuarterGrade(columns, quarter, student, gradeMap);
            ws.value(row, col, calculatedGrade);
            if (calculatedGrade < FAILING_FINAL_GRADE) {
                fillColor = LIGHT_RED_HEX;
            }
        } else {
            ws.value(row, col, "-");
        }

        ws.style(row, col).bold().horizontalAlignment(ALIGN_CENTER).fillColor(fillColor).borderStyle("thin").set();
    }

    private void writeFinalGradeCell(Worksheet ws, int row, int col,
                                     List<ColumnInfo> columns,
                                     Student student, Map<String, Double> gradeMap) {
        boolean hasAnyGrade = hasAnyGradeTotal(columns, student, gradeMap);
        String fillColor = LIGHT_BLUE_HEX;

        if (hasAnyGrade) {
            double calculatedFinal = calculateFinalGrade(columns, student, gradeMap);
            ws.value(row, col, calculatedFinal);
            if (calculatedFinal < FAILING_FINAL_GRADE) {
                fillColor = LIGHT_RED_HEX;
            }
        } else {
            ws.value(row, col, "-");
        }

        ws.style(row, col).bold().horizontalAlignment(ALIGN_CENTER).fillColor(fillColor).borderStyle("thin").set();
    }

    private double calculateQuarterGrade(List<ColumnInfo> columns, Integer quarter,
                                         Student student, Map<String, Double> gradeMap) {
        double total = 0.0;
        for (ColumnInfo col : columns) {
            if (col.type == ColumnType.EXERCISE && col.quarter.equals(quarter)) {
                String key = student.getId() + "_" + col.exerciseId;
                double grade = gradeMap.getOrDefault(key, 0.0);
                if (col.maxGrade > 0) {
                    total += (grade / col.maxGrade) * col.percentageGrade;
                }
            }
        }
        return total * 10.0 / 100.0;
    }

    private boolean hasAnyGradeInQuarter(List<ColumnInfo> columns, Integer quarter,
                                         Student student, Map<String, Double> gradeMap) {
        for (ColumnInfo col : columns) {
            if (col.type == ColumnType.EXERCISE && col.quarter.equals(quarter)) {
                String key = student.getId() + "_" + col.exerciseId;
                if (gradeMap.containsKey(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    private double calculateFinalGrade(List<ColumnInfo> columns, Student student,
                                       Map<String, Double> gradeMap) {
        double sum = 0.0;
        int count = 0;
        Set<Integer> quarters = new TreeSet<>();
        for (ColumnInfo col : columns) {
            // Check quarter presence via QUARTER_GRADE columns which act as markers for quarter existence
            if (col.type == ColumnType.QUARTER_GRADE && col.quarter != null) {
                quarters.add(col.quarter);
            }
        }
        for (Integer quarter : quarters) {
            if (hasAnyGradeInQuarter(columns, quarter, student, gradeMap)) {
                sum += calculateQuarterGrade(columns, quarter, student, gradeMap);
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }

    private boolean hasAnyGradeTotal(List<ColumnInfo> columns, Student student,
                                     Map<String, Double> gradeMap) {
        for (ColumnInfo col : columns) {
            if (col.type == ColumnType.EXERCISE) {
                String key = student.getId() + "_" + col.exerciseId;
                if (gradeMap.containsKey(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setColumnWidths(Worksheet ws, List<ColumnInfo> columns) {
        ws.width(0, 30);
        for (int i = 1; i < columns.size(); i++) {
            ws.width(i, 18);
        }
    }

    private List<ColumnInfo> buildColumnLayout(List<Integer> sortedQuarters,
                                               Map<Integer, List<Exercise>> quarterMap,
                                               Locale locale) {
        List<ColumnInfo> columns = new ArrayList<>();
        String studentHeader = messageSource.getMessage(MessageKeys.GRADE_EXPORT_STUDENT, null, locale);
        columns.add(new ColumnInfo(studentHeader, null, ColumnType.STUDENT, null, null, 0, 0));

        for (Integer quarter : sortedQuarters) {
            List<Exercise> quarterExercises = quarterMap.get(quarter);
            for (Exercise exercise : quarterExercises) {
                String maxGradeLine = messageSource.getMessage(
                        MessageKeys.GRADE_EXPORT_MAX_GRADE, new Object[]{exercise.getMaxGrade()}, locale);
                String percentageLine = messageSource.getMessage(
                        MessageKeys.GRADE_EXPORT_PERCENTAGE, new Object[]{exercise.getPercentageGrade()}, locale);
                String tooltip = maxGradeLine + "\n" + percentageLine;
                columns.add(new ColumnInfo(exercise.getTitle(), tooltip, ColumnType.EXERCISE, quarter,
                        exercise.getId(), exercise.getMaxGrade(), exercise.getPercentageGrade()));
            }

            String quarterGradeHeader = messageSource.getMessage(
                    MessageKeys.GRADE_EXPORT_QUARTER_GRADE, new Object[]{quarter}, locale);
            columns.add(new ColumnInfo(quarterGradeHeader, null, ColumnType.QUARTER_GRADE, quarter, null, 0, 0));
        }

        String finalGradeHeader = messageSource.getMessage(MessageKeys.GRADE_EXPORT_FINAL_GRADE, null, locale);
        columns.add(new ColumnInfo(finalGradeHeader, null, ColumnType.FINAL_GRADE, null, null, 0, 0));

        return columns;
    }

    private Map<String, Double> buildGradeMap(List<ExerciseStudentGrade> grades) {
        Map<String, Double> map = new HashMap<>();
        for (ExerciseStudentGrade grade : grades) {
            String key = grade.getStudentId() + "_" + grade.getExerciseId();
            map.put(key, grade.getGrade());
        }
        return map;
    }

    private Map<String, String> buildDescriptionMap(List<ExerciseStudentGrade> grades) {
        Map<String, String> map = new HashMap<>();
        for (ExerciseStudentGrade grade : grades) {
            if (grade.getDescription() != null && !grade.getDescription().isBlank()) {
                String key = grade.getStudentId() + "_" + grade.getExerciseId();
                map.put(key, grade.getDescription());
            }
        }
        return map;
    }

    private Map<Integer, Map<Integer, List<Exercise>>> groupExercises(List<Exercise> exercises) {
        Map<Integer, Map<Integer, List<Exercise>>> result = new TreeMap<>();
        for (Exercise exercise : exercises) {
            result
                    .computeIfAbsent(exercise.getSubjectId(), k -> new TreeMap<>())
                    .computeIfAbsent(exercise.getQuarter(), k -> new ArrayList<>())
                    .add(exercise);
        }
        return result;
    }

    private Set<Integer> findSubjectsWithGrades(List<Exercise> exercises, Map<String, Double> gradeMap) {
        Set<Integer> exerciseIdsWithGrades = gradeMap.keySet().stream()
                .map(key -> Integer.parseInt(key.split("_")[1]))
                .collect(Collectors.toSet());

        return exercises.stream()
                .filter(e -> exerciseIdsWithGrades.contains(e.getId()))
                .map(Exercise::getSubjectId)
                .collect(Collectors.toSet());
    }

    private String getSubjectName(Map<Integer, List<Exercise>> quarterMap) {
        return quarterMap.values().stream()
                .flatMap(Collection::stream)
                .findFirst()
                .map(Exercise::getSubjectName)
                .orElse("Unknown");
    }

    private List<Student> loadStudentsByClassId(Integer classId, Integer teacherId) {
        List<Integer> studentIds = studentClassRepository.findActiveStudentIdsByClassId(classId);
        List<Student> students = new ArrayList<>();

        for (Integer studentId : studentIds) {
            studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(studentId, teacherId)
                    .ifPresent(students::add);
        }

        students.sort(Comparator.comparing(Student::getSurnames, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Student::getName, String.CASE_INSENSITIVE_ORDER));

        return students;
    }

    private void validateClassOwnership(Integer classId, Integer teacherId, Locale locale) {
        classRepository.findById(classId)
                .orElseThrow(() -> new ClassNotFoundException(
                        messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, locale)));

        classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassForbiddenException(
                        messageSource.getMessage(MessageKeys.CLASS_FORBIDDEN, null, locale)));
    }

    private Integer getTeacherId() {
        return sessionUser.getParameter(SessionParameter.TEACHER_ID);
    }


    private String sanitizeSheetName(String name) {
        String sanitized = name.replaceAll("[\\\\/:*?\\[\\]]", "_");
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        return sanitized;
    }


    private enum ColumnType {
        STUDENT, EXERCISE, QUARTER_GRADE, FINAL_GRADE
    }

    private record ColumnInfo(
            String headerText,
            String subHeaderText,
            ColumnType type,
            Integer quarter,
            Integer exerciseId,
            int maxGrade,
            int percentageGrade
    ) {
    }
}

