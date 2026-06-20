package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.domain.service.teachernotebook.StudentAbsenceService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentAbsenceUseCaseImplTest {

    private StudentAbsenceUseCaseImpl studentAbsenceUseCase;

    @Mock
    private StudentAbsenceService studentAbsenceService;

    @BeforeEach
    void beforeEach() {
        studentAbsenceUseCase = new StudentAbsenceUseCaseImpl(studentAbsenceService);
    }

    @Nested
    class CreateAbsences {

        @Test
        void when_creating_absences_expect_delegated_to_service() {
            final Integer classId = 1;
            final Integer studentId = 2;
            final Integer subjectId = 3;
            final LocalDate date = LocalDate.of(2025, 3, 15);
            final List<StudentAbsence> expected = List.of(StudentAbsence.builder().id(1).build());
            when(studentAbsenceService.createAbsences(classId, studentId, subjectId, date)).thenReturn(expected);

            final List<StudentAbsence> result = studentAbsenceUseCase.createAbsences(classId, studentId, subjectId, date);

            assertThat(result).isEqualTo(expected);
            verify(studentAbsenceService).createAbsences(classId, studentId, subjectId, date);
        }
    }

    @Nested
    class GetAbsences {

        @Test
        void when_fetching_absences_expect_delegated_to_service() {
            final Integer classId = 1;
            final Integer studentId = 2;
            final LocalDate date = LocalDate.of(2025, 3, 15);
            final List<StudentAbsence> expected = List.of(StudentAbsence.builder().id(1).build());
            when(studentAbsenceService.getAbsences(classId, studentId, date)).thenReturn(expected);

            final List<StudentAbsence> result = studentAbsenceUseCase.getAbsences(classId, studentId, date);

            assertThat(result).isEqualTo(expected);
            verify(studentAbsenceService).getAbsences(classId, studentId, date);
        }
    }

    @Nested
    class DeleteAbsence {

        @Test
        void when_deleting_absence_expect_delegated_to_service() {
            final Integer id = 1;

            studentAbsenceUseCase.deleteAbsence(id);

            verify(studentAbsenceService).deleteAbsence(id);
        }
    }

    @Nested
    class DeleteAbsencesByStudentAndDate {

        @Test
        void when_deleting_absences_expect_delegated_to_service() {
            final Integer classId = 1;
            final Integer studentId = 2;
            final LocalDate date = LocalDate.of(2025, 3, 15);

            studentAbsenceUseCase.deleteAbsencesByStudentAndDate(classId, studentId, date);

            verify(studentAbsenceService).deleteAbsencesByStudentAndDate(classId, studentId, date);
        }
    }
}
