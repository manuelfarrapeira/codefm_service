package org.web.codefm.service.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
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
import org.web.codefm.domain.exception.teachernotebook.GradeExportException;
import org.web.codefm.domain.repository.teachernotebook.*;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GradeExportServiceImplTest {

    private static final Integer TEACHER_ID = 1;
    private static final Integer CLASS_ID = 4;

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

    private GradeExportServiceImpl gradeExportService;

    @BeforeEach
    void beforeEach() {
        this.gradeExportService = new GradeExportServiceImpl(
                this.classRepository,
                this.studentClassRepository,
                this.studentRepository,
                this.exerciseRepository,
                this.exerciseStudentGradeRepository,
                this.sessionUser,
                this.messageSource
        );
        when(this.sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(TEACHER_ID);
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(this.messageSource.getMessage(anyString(), any(), any(Locale.class))).thenAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            final Object[] args = invocation.getArgument(1);
            if (key.contains("quarter.grade") && args != null && args.length > 0) {
                return "Grade T" + args[0];
            }
            if (key.contains("quarter") && args != null && args.length > 0) {
                return "Quarter " + args[0];
            }
            if (key.contains("final.grade")) {
                return "Final Grade";
            }
            if (key.contains("student")) {
                return "Student";
            }
            return key;
        });
    }

    @Nested
    class ExportGradesByClassId {

        @Test
        void when_class_has_grades_expect_generate_excel() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

            final Student student1 = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
            final Student student2 = Student.builder().id(2).name("Pedro").surnames("García").teacherId(TEACHER_ID).build();
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student1));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(2, TEACHER_ID)).thenReturn(Optional.of(student2));

            final Exercise ex1 = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen T1").quarter(1).percentageGrade(60).maxGrade(10).build();
            final Exercise ex2 = Exercise.builder().id(11).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Tarea T1").quarter(1).percentageGrade(40).maxGrade(5).build();
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(ex1, ex2));

            final ExerciseStudentGrade g1 = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(7.5).build();
            final ExerciseStudentGrade g2 = ExerciseStudentGrade.builder().studentId(1).exerciseId(11).grade(4.0).build();
            final ExerciseStudentGrade g3 = ExerciseStudentGrade.builder().studentId(2).exerciseId(10).grade(3.0).build();
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(g1, g2, g3));

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            assertThat(result).isNotEmpty();

            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Sheet> sheets = wb.getSheets().collect(Collectors.toList());
                assertThat(sheets).hasSize(1);

                final List<Row> rows = sheets.get(0).openStream().collect(Collectors.toList());
                final Row headerRow = rows.get(1);
                assertThat(headerRow.getCellText(0)).isEqualTo("Student");
                assertThat(headerRow.getCellText(1)).isEqualTo("Examen T1");
                assertThat(headerRow.getCellText(2)).isEqualTo("Tarea T1");
                assertThat(headerRow.getCellText(3)).isEqualTo("Grade T1");
                assertThat(headerRow.getCellText(4)).isEqualTo("Final Grade");

                final Row dataRow1 = rows.get(2);
                assertThat(dataRow1.getCellText(0)).isEqualTo("García, Pedro");
                assertThat(dataRow1.getCell(1).asNumber().doubleValue()).isEqualTo(3.0);
                assertThat(dataRow1.getCellText(2)).isEqualTo("");

                final Row dataRow2 = rows.get(3);
                assertThat(dataRow2.getCellText(0)).isEqualTo("López, Ana");
                assertThat(dataRow2.getCell(1).asNumber().doubleValue()).isEqualTo(7.5);
                assertThat(dataRow2.getCell(2).asNumber().doubleValue()).isEqualTo(4.0);

                assertThat(dataRow1.getCell(3).asNumber().doubleValue()).isCloseTo(1.8, within(0.01));
                assertThat(dataRow1.getCell(4).asNumber().doubleValue()).isCloseTo(1.8, within(0.01));
            }
        }

        @Test
        void when_no_grades_exist_expect_generate_empty_workbook() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID))
                    .thenReturn(Optional.of(Student.builder().id(1).name("Ana").surnames("López").build()));
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of());
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of());

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            assertThat(result).isNotNull();
            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Sheet> sheets = wb.getSheets().collect(Collectors.toList());
                assertThat(sheets).hasSize(1);
                final List<Row> rows = sheets.get(0).openStream().collect(Collectors.toList());
                assertThat(rows).isEmpty();
            }
        }

        @Test
        void when_class_does_not_exist_expect_throw_not_found_exception() {
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            assertThatThrownBy(action).isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        void when_class_is_not_owned_by_teacher_expect_throw_forbidden_exception() {
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(Class.builder().id(CLASS_ID).build()));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.empty());

            final ThrowingCallable action = () -> GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            assertThatThrownBy(action).isInstanceOf(ClassForbiddenException.class);
        }

        @Test
        void when_only_some_subjects_have_grades_expect_only_create_sheets_for_subjects_with_grades() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student));

            final Exercise exMath = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen").quarter(1).percentageGrade(100).maxGrade(10).build();
            final Exercise exLang = Exercise.builder().id(20).subjectClassId(200).subjectId(6).subjectName("Lengua")
                    .title("Dictado").quarter(1).percentageGrade(100).maxGrade(10).build();
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(exMath, exLang));

            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(8.0).build();
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(grade));

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Sheet> sheets = wb.getSheets().collect(Collectors.toList());
                assertThat(sheets).hasSize(1);
                assertThat(sheets.get(0).getName()).isEqualTo("Matemáticas");
            }
        }

        @Test
        void when_students_are_unsorted_expect_sort_students_by_surnames() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

            final Student s1 = Student.builder().id(1).name("Zoe").surnames("Abad").teacherId(TEACHER_ID).build();
            final Student s2 = Student.builder().id(2).name("Ana").surnames("Zapata").teacherId(TEACHER_ID).build();
            final Student s3 = Student.builder().id(3).name("Luis").surnames("Martín").teacherId(TEACHER_ID).build();
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2, 3));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(s1));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(2, TEACHER_ID)).thenReturn(Optional.of(s2));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(3, TEACHER_ID)).thenReturn(Optional.of(s3));

            final Exercise ex = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen").quarter(1).percentageGrade(100).maxGrade(10).build();
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(ex));

            final ExerciseStudentGrade g1 = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(5.0).build();
            final ExerciseStudentGrade g2 = ExerciseStudentGrade.builder().studentId(2).exerciseId(10).grade(6.0).build();
            final ExerciseStudentGrade g3 = ExerciseStudentGrade.builder().studentId(3).exerciseId(10).grade(7.0).build();
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(g1, g2, g3));

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Row> rows = wb.getSheets().findFirst().get().openStream().collect(Collectors.toList());
                assertThat(rows.get(2).getCellText(0)).isEqualTo("Abad, Zoe");
                assertThat(rows.get(3).getCellText(0)).isEqualTo("Martín, Luis");
                assertThat(rows.get(4).getCellText(0)).isEqualTo("Zapata, Ana");
            }
        }

        @Test
        void when_multiple_quarters_exist_expect_handle_multiple_quarters() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student));

            final Exercise exQ1 = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen T1").quarter(1).percentageGrade(100).maxGrade(10).build();
            final Exercise exQ2 = Exercise.builder().id(11).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen T2").quarter(2).percentageGrade(100).maxGrade(10).build();
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(exQ1, exQ2));

            final ExerciseStudentGrade g1 = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(8.0).build();
            final ExerciseStudentGrade g2 = ExerciseStudentGrade.builder().studentId(1).exerciseId(11).grade(6.0).build();
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(g1, g2));

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Row> rows = wb.getSheets().findFirst().get().openStream().collect(Collectors.toList());
                final Row headerRow = rows.get(1);

                assertThat(headerRow.getCellText(1)).isEqualTo("Examen T1");
                assertThat(headerRow.getCellText(2)).isEqualTo("Grade T1");
                assertThat(headerRow.getCellText(3)).isEqualTo("Examen T2");
                assertThat(headerRow.getCellText(4)).isEqualTo("Grade T2");
                assertThat(headerRow.getCellText(5)).isEqualTo("Final Grade");

                final Row dataRow = rows.get(2);
                assertThat(dataRow.getCell(5).asNumber().doubleValue()).isCloseTo(7.0, within(0.01));
            }
        }

        @Test
        void when_io_exception_occurs_expect_throw_grade_export_exception() {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of());
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of());
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of());

            try (MockedConstruction<Workbook> mocked = mockConstruction(Workbook.class,
                    (mock, context) -> {
                        final Worksheet mockWs = mock(Worksheet.class);
                        when(mock.newWorksheet(anyString())).thenReturn(mockWs);
                        doThrow(new IOException("IO error")).when(mock).finish();
                    })) {
                final ThrowingCallable action = () -> GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

                assertThatThrownBy(action).isInstanceOf(GradeExportException.class);
            }
        }

        @Test
        void when_grades_have_descriptions_expect_include_descriptions() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student));

            final Exercise ex = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen T1").quarter(1).percentageGrade(100).maxGrade(10).build();
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(ex));

            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder()
                    .studentId(1).exerciseId(10).grade(8.0).description("Buen trabajo").build();
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(grade));

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            assertThat(result).isNotEmpty();

            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Row> rows = wb.getSheets().findFirst().get().openStream().collect(Collectors.toList());
                assertThat(rows.get(2).getCell(1).asNumber().doubleValue()).isEqualTo(8.0);
            }
        }

        @Test
        void when_subject_name_exceeds_thirty_one_characters_expect_truncate_sheet_name() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student));

            final String longName = "Educación Física y Deportes Avanzados Extra";
            final Exercise ex = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName(longName)
                    .title("Examen").quarter(1).percentageGrade(100).maxGrade(10).build();
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(ex));

            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(8.0).build();
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(grade));

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Sheet> sheets = wb.getSheets().collect(Collectors.toList());
                assertThat(sheets).hasSize(1);
                assertThat(sheets.get(0).getName().length()).isLessThanOrEqualTo(31);
            }
        }

        @Test
        void when_student_has_no_grades_in_specific_quarter_expect_show_dash() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

            final Student student = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(student));

            final Exercise exQ1 = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen T1").quarter(1).percentageGrade(100).maxGrade(10).build();
            final Exercise exQ2 = Exercise.builder().id(11).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen T2").quarter(2).percentageGrade(100).maxGrade(10).build();
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(exQ1, exQ2));

            final ExerciseStudentGrade gradeQ1 = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(8.0).build();
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(gradeQ1));

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Row> rows = wb.getSheets().findFirst().get().openStream().collect(Collectors.toList());
                final Row dataRow = rows.get(2);
                assertThat(dataRow.getCellText(4)).isEqualTo("-");
            }
        }

        @Test
        void when_student_has_no_grades_at_all_expect_show_dash() throws IOException {
            final Class clazz = Class.builder().id(CLASS_ID).name("1A").build();
            when(GradeExportServiceImplTest.this.classRepository.findById(CLASS_ID)).thenReturn(Optional.of(clazz));
            when(GradeExportServiceImplTest.this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(CLASS_ID, TEACHER_ID)).thenReturn(Optional.of(clazz));

            final Student studentWithGrades = Student.builder().id(1).name("Ana").surnames("López").teacherId(TEACHER_ID).build();
            final Student studentWithoutGrades = Student.builder().id(2).name("Pedro").surnames("García").teacherId(TEACHER_ID).build();
            when(GradeExportServiceImplTest.this.studentClassRepository.findActiveStudentIdsByClassId(CLASS_ID)).thenReturn(List.of(1, 2));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(1, TEACHER_ID)).thenReturn(Optional.of(studentWithGrades));
            when(GradeExportServiceImplTest.this.studentRepository.findByIdAndTeacherIdAndDeletionDateIsNull(2, TEACHER_ID)).thenReturn(Optional.of(studentWithoutGrades));

            final Exercise ex = Exercise.builder().id(10).subjectClassId(100).subjectId(5).subjectName("Matemáticas")
                    .title("Examen T1").quarter(1).percentageGrade(100).maxGrade(10).build();
            when(GradeExportServiceImplTest.this.exerciseRepository.findByClassId(CLASS_ID)).thenReturn(List.of(ex));

            final ExerciseStudentGrade grade = ExerciseStudentGrade.builder().studentId(1).exerciseId(10).grade(8.0).build();
            when(GradeExportServiceImplTest.this.exerciseStudentGradeRepository.findByClassId(CLASS_ID)).thenReturn(List.of(grade));

            final byte[] result = GradeExportServiceImplTest.this.gradeExportService.exportGradesByClassId(CLASS_ID);

            try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(result))) {
                final List<Row> rows = wb.getSheets().findFirst().get().openStream().collect(Collectors.toList());
                final Row noGradesRow = rows.get(2);
                assertThat(noGradesRow.getCellText(0)).isEqualTo("García, Pedro");
                assertThat(noGradesRow.getCellText(2)).isEqualTo("-");
                assertThat(noGradesRow.getCellText(3)).isEqualTo("-");
            }
        }
    }

    @Nested
    class WriteGroupRow {

        @Test
        @SuppressWarnings("unchecked")
        void when_quarter_has_one_column_expect_style_single_column() throws Exception {
            final java.lang.Class<?> columnTypeClass = java.lang.Class.forName(
                    "org.web.codefm.service.teachernotebook.GradeExportServiceImpl$ColumnType");
            final java.lang.Class<?> columnInfoClass = java.lang.Class.forName(
                    "org.web.codefm.service.teachernotebook.GradeExportServiceImpl$ColumnInfo");

            final Object studentType = Enum.valueOf((java.lang.Class<Enum>) columnTypeClass, "STUDENT");
            final Object quarterGradeType = Enum.valueOf((java.lang.Class<Enum>) columnTypeClass, "QUARTER_GRADE");

            final Constructor<?> ctor = columnInfoClass.getDeclaredConstructor(
                    String.class, String.class, columnTypeClass, Integer.class, Integer.class, int.class, int.class);
            ctor.setAccessible(true);

            final Object studentCol = ctor.newInstance("Student", null, studentType, null, null, 0, 0);
            final Object singleQuarterCol = ctor.newInstance("Grade T1", null, quarterGradeType, 1, null, 0, 0);

            final List<?> columns = List.of(studentCol, singleQuarterCol);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                final Workbook workbook = new Workbook(out, "Test", "1.0");
                final Worksheet ws = workbook.newWorksheet("Test");

                final Method writeGroupRow = GradeExportServiceImpl.class.getDeclaredMethod(
                        "writeGroupRow", Worksheet.class, List.class, List.class, Locale.class);
                writeGroupRow.setAccessible(true);

                writeGroupRow.invoke(GradeExportServiceImplTest.this.gradeExportService, ws, columns, List.of(1), Locale.ENGLISH);

                ws.finish();
                workbook.finish();

                final byte[] result = out.toByteArray();
                assertThat(result).isNotEmpty();
            }
        }
    }
}

