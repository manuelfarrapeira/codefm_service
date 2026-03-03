package org.web.codefm.service.teachernotebook;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.domain.entity.teachernotebook.ExerciseStudentGrade;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.ClassForbiddenException;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GradeExportServiceImplTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private StudentClassRepository studentClassRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseStudentGradeRepository exerciseStudentGradeRepository;

    @Mock
    private SessionUser sessionUser;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private GradeExportServiceImpl gradeExportService;

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 4;

    @BeforeEach
    void setUp() {
        when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object[] args = invocation.getArgument(1);
            if (key.contains("quarter.grade") && args != null && args.length > 0) return "Grade T" + args[0];
            if (key.contains("quarter") && args != null && args.length > 0) return "Quarter " + args[0];
            if (key.contains("final.grade")) return "Final Grade";
            if (key.contains("student")) return "Student";
            return key;
        });
    }

    @Test
    void exportGradesByClassId_shouldGenerateExcel_whenClassHasGrades() throws IOException {
        Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

        Student student1 = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
        Student student2 = Student.builder().id(2).name("Pedro").surnames("García").teacherId(TEACHER_ID).build();
        when(studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student1));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(2, TEACHER_ID)).thenReturn(Optional.of(student2));

        Exercise ex1 = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                .title("Examen T1").quarter(1).percentageGrade(60).maxGrade(10).build();
        Exercise ex2 = Exercise.builder().id(11).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                .title("Tarea T1").quarter(1).percentageGrade(40).maxGrade(5).build();
        when(exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(ex1, ex2));

        ExerciseStudentGrade g1 = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(7.5).build();
        ExerciseStudentGrade g2 = ExerciseStudentGrade.builder().studentId(1).exerciseId(11).grade(4.0).build();
        ExerciseStudentGrade g3 = ExerciseStudentGrade.builder().studentId(2).exerciseId(10).grade(3.0).build();
        when(exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(g1, g2, g3));

        byte[] result = gradeExportService.exportGradesByClassId(CLASS_ID);

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
            List<Sheet> sheets = wb.getSheets().collect(Collectors.toList());
            assertEquals(1, sheets.size());

            List<Row> rows = sheets.get(0).openStream().collect(Collectors.toList());
            Row headerRow = rows.get(1);
            assertEquals("Student", headerRow.getCellText(0));
            assertEquals("Examen T1", headerRow.getCellText(1));
            assertEquals("Tarea T1", headerRow.getCellText(2));
            assertEquals("Grade T1", headerRow.getCellText(3));
            assertEquals("Final Grade", headerRow.getCellText(4));

            Row dataRow1 = rows.get(2);
            assertEquals("García, Pedro", dataRow1.getCellText(0));
            assertEquals(3.0, dataRow1.getCell(1).asNumber().doubleValue());
            assertEquals("", dataRow1.getCellText(2));

            Row dataRow2 = rows.get(3);
            assertEquals("López, Ana", dataRow2.getCellText(0));
            assertEquals(7.5, dataRow2.getCell(1).asNumber().doubleValue());
            assertEquals(4.0, dataRow2.getCell(2).asNumber().doubleValue());

            assertEquals(1.8, dataRow1.getCell(3).asNumber().doubleValue(), 0.01);
            assertEquals(1.8, dataRow1.getCell(4).asNumber().doubleValue(), 0.01);
        }
    }

    @Test
    void exportGradesByClassId_shouldGenerateEmptyWorkbook_whenNoGradesExist() throws IOException {
        Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
        when(studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID))
                .thenReturn(Optional.of(Student.builder().id(1).name("Ana").surnames("López").build()));
        when(exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of());
        when(exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of());

        byte[] result = gradeExportService.exportGradesByClassId(CLASS_ID);

        assertNotNull(result);
        try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
            List<Sheet> sheets = wb.getSheets().collect(Collectors.toList());
            assertEquals(1, sheets.size());
            List<Row> rows = sheets.get(0).openStream().collect(Collectors.toList());
            assertEquals(0, rows.size());
        }
    }

    @Test
    void exportGradesByClassId_shouldThrowNotFoundException_whenClassNotExists() {
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

        assertThrows(ClassNotFoundException.class,
                () -> gradeExportService.exportGradesByClassId(CLASS_ID));
    }

    @Test
    void exportGradesByClassId_shouldThrowForbiddenException_whenClassNotOwnedByTeacher() {
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

        assertThrows(ClassForbiddenException.class,
                () -> gradeExportService.exportGradesByClassId(CLASS_ID));
    }

    @Test
    void exportGradesByClassId_shouldOnlyCreateSheetsForSubjectsWithGrades() throws IOException {
        Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

        Student student = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
        when(studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student));

        Exercise exMath = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                .title("Examen").quarter(1).percentageGrade(100).maxGrade(10).build();
        Exercise exLang = Exercise.builder().id(20).subjectClassId(200).subjectId(6).subjectName("Lengua")
                .title("Dictado").quarter(1).percentageGrade(100).maxGrade(10).build();
        when(exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(exMath, exLang));

        ExerciseStudentGrade grade = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(8.0).build();
        when(exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(grade));

        byte[] result = gradeExportService.exportGradesByClassId(CLASS_ID);

        try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
            List<Sheet> sheets = wb.getSheets().collect(Collectors.toList());
            assertEquals(1, sheets.size());
            assertEquals("Matemáticas", sheets.get(0).getName());
        }
    }

    @Test
    void exportGradesByClassId_shouldSortStudentsBySurnames() throws IOException {
        Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

        Student s1 = Student.builder().id(1).name("Zoe").surnames("Abad").teacherId(TEACHER_ID).build();
        Student s2 = Student.builder().id(2).name("Ana").surnames("Zapata").teacherId(TEACHER_ID).build();
        Student s3 = Student.builder().id(3).name("Luis").surnames("Martín").teacherId(TEACHER_ID).build();
        when(studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2, 3));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(s1));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(2, TEACHER_ID)).thenReturn(Optional.of(s2));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(3, TEACHER_ID)).thenReturn(Optional.of(s3));

        Exercise ex = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                .title("Examen").quarter(1).percentageGrade(100).maxGrade(10).build();
        when(exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(ex));

        ExerciseStudentGrade g1 = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(5.0).build();
        ExerciseStudentGrade g2 = ExerciseStudentGrade.builder().studentId(2).exerciseId(10).grade(6.0).build();
        ExerciseStudentGrade g3 = ExerciseStudentGrade.builder().studentId(3).exerciseId(10).grade(7.0).build();
        when(exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(g1, g2, g3));

        byte[] result = gradeExportService.exportGradesByClassId(CLASS_ID);

        try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
            List<Row> rows = wb.getSheets().findFirst().get().openStream().collect(Collectors.toList());
            assertEquals("Abad, Zoe", rows.get(2).getCellText(0));
            assertEquals("Martín, Luis", rows.get(3).getCellText(0));
            assertEquals("Zapata, Ana", rows.get(4).getCellText(0));
        }
    }

    @Test
    void exportGradesByClassId_shouldHandleMultipleQuarters() throws IOException {
        Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
        when(classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
        when(classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

        Student student = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
        when(studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
        when(studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student));

        Exercise exQ1 = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                .title("Examen T1").quarter(1).percentageGrade(100).maxGrade(10).build();
        Exercise exQ2 = Exercise.builder().id(11).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                .title("Examen T2").quarter(2).percentageGrade(100).maxGrade(10).build();
        when(exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(exQ1, exQ2));

        ExerciseStudentGrade g1 = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(8.0).build();
        ExerciseStudentGrade g2 = ExerciseStudentGrade.builder().studentId(1).exerciseId(11).grade(6.0).build();
        when(exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(g1, g2));

        byte[] result = gradeExportService.exportGradesByClassId(CLASS_ID);

        try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
            List<Row> rows = wb.getSheets().findFirst().get().openStream().collect(Collectors.toList());
            Row headerRow = rows.get(1);

            assertEquals("Examen T1", headerRow.getCellText(1));
            assertEquals("Grade T1", headerRow.getCellText(2));
            assertEquals("Examen T2", headerRow.getCellText(3));
            assertEquals("Grade T2", headerRow.getCellText(4));
            assertEquals("Final Grade", headerRow.getCellText(5));

            Row dataRow = rows.get(2);
            assertEquals(7.0, dataRow.getCell(5).asNumber().doubleValue(), 0.01);
        }
    }
}

