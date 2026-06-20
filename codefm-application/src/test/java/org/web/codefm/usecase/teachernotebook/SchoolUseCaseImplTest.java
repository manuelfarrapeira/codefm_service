package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.SchoolService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolUseCaseImplTest {

    private SchoolUseCaseImpl schoolUseCase;

    @Mock
    private SchoolService schoolService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @Mock
    private SessionUser sessionUser;

    @BeforeEach
    void beforeEach() {
        schoolUseCase = new SchoolUseCaseImpl(schoolService, cascadeSoftDeleteService, sessionUser);
        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(1);
    }

    @Nested
    class GetSchoolsByTeacher {

        @Test
        void when_schools_have_classes_expect_sorted_by_max_school_year_desc() {
            final Integer teacherId = 1;

            final Class s1c1 = Class.builder().id(1).schoolId(1).name("Class A").schoolYear("23/24").build();
            final Class s1c2 = Class.builder().id(2).schoolId(1).name("Class B").schoolYear("25/26").build();
            final Class s1c3 = Class.builder().id(3).schoolId(1).name("Class C").schoolYear("24/25").build();
            final School school1 = School.builder()
                    .id(1).teacherId(teacherId).name("School A (Max 25/26)").town("Town A").tlf(123456)
                    .classes(Arrays.asList(s1c1, s1c2, s1c3)).build();

            final Class s2c1 = Class.builder().id(4).schoolId(2).name("Class D").schoolYear("22/23").build();
            final Class s2c2 = Class.builder().id(5).schoolId(2).name("Class E").schoolYear("24/25").build();
            final School school2 = School.builder()
                    .id(2).teacherId(teacherId).name("School B (Max 24/25)").town("Town B").tlf(789012)
                    .classes(Arrays.asList(s2c1, s2c2)).build();

            final Class s3c1 = Class.builder().id(6).schoolId(3).name("Class F").schoolYear("26/27").build();
            final Class s3c2 = Class.builder().id(7).schoolId(3).name("Class G").schoolYear("25/26").build();
            final School school3 = School.builder()
                    .id(3).teacherId(teacherId).name("School C (Max 26/27)").town("Town C").tlf(345678)
                    .classes(Arrays.asList(s3c1, s3c2)).build();

            final School school4 = School.builder()
                    .id(4).teacherId(teacherId).name("School D (No Classes)").town("Town D").tlf(901234)
                    .classes(Collections.emptyList()).build();

            final Class s5c1 = Class.builder().id(8).schoolId(5).name("Class H").schoolYear("INVALID").build();
            final Class s5c2 = Class.builder().id(9).schoolId(5).name("Class I").schoolYear("21/22").build();
            final School school5 = School.builder()
                    .id(5).teacherId(teacherId).name("School E (Max 21/22, Invalid)").town("Town E").tlf(567890)
                    .classes(Arrays.asList(s5c1, s5c2)).build();

            when(schoolService.getSchoolsByTeacherId(teacherId))
                    .thenReturn(Arrays.asList(school2, school4, school1, school5, school3));

            final List<School> result = schoolUseCase.getSchoolsByTeacher();

            assertThat(result).isNotNull().hasSize(5);
            verify(schoolService, times(1)).getSchoolsByTeacherId(teacherId);

            assertThat(result.get(0).getName()).isEqualTo("School C (Max 26/27)");
            assertThat(result.get(1).getName()).isEqualTo("School A (Max 25/26)");
            assertThat(result.get(2).getName()).isEqualTo("School B (Max 24/25)");
            assertThat(result.get(3).getName()).isEqualTo("School E (Max 21/22, Invalid)");
            assertThat(result.get(4).getName()).isEqualTo("School D (No Classes)");

            assertThat(result.get(0).getClasses().get(0).getSchoolYear()).isEqualTo("26/27");
            assertThat(result.get(0).getClasses().get(1).getSchoolYear()).isEqualTo("25/26");
            assertThat(result.get(1).getClasses().get(0).getSchoolYear()).isEqualTo("25/26");
            assertThat(result.get(1).getClasses().get(1).getSchoolYear()).isEqualTo("24/25");
            assertThat(result.get(1).getClasses().get(2).getSchoolYear()).isEqualTo("23/24");
            assertThat(result.get(2).getClasses().get(0).getSchoolYear()).isEqualTo("24/25");
            assertThat(result.get(2).getClasses().get(1).getSchoolYear()).isEqualTo("22/23");
            assertThat(result.get(3).getClasses().get(0).getSchoolYear()).isEqualTo("21/22");
            assertThat(result.get(3).getClasses().get(1).getSchoolYear()).isEqualTo("INVALID");
            assertThat(result.get(4).getClasses()).isEmpty();
        }

        @Test
        void when_school_has_null_classes_expect_handled_gracefully() {
            final Integer teacherId = 1;
            final School schoolWithNullClasses = School.builder()
                    .id(1).teacherId(teacherId).name("School A (Null Classes)").town("Town A").tlf(123456)
                    .classes(null).build();

            when(schoolService.getSchoolsByTeacherId(teacherId))
                    .thenReturn(Collections.singletonList(schoolWithNullClasses));

            final List<School> result = schoolUseCase.getSchoolsByTeacher();

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.get(0).getClasses()).isNull();
            verify(schoolService, times(1)).getSchoolsByTeacherId(teacherId);
        }
    }

    @Nested
    class CreateSchool {

        @Test
        void when_creating_school_expect_teacher_id_set() {
            final Integer teacherId = 1;
            final School schoolToCreate = School.builder().name("New School").town("New Town").build();

            when(schoolService.createSchool(any(School.class))).thenAnswer(invocation -> invocation.getArgument(0));

            schoolUseCase.createSchool(schoolToCreate);

            final ArgumentCaptor<School> schoolCaptor = ArgumentCaptor.forClass(School.class);
            verify(schoolService, times(1)).createSchool(schoolCaptor.capture());

            assertThat(schoolCaptor.getValue().getTeacherId()).isEqualTo(teacherId);
            assertThat(schoolCaptor.getValue().getName()).isEqualTo("New School");
        }
    }

    @Nested
    class SoftDeleteSchool {

        @Test
        void when_deleting_school_expect_cascade_before_service() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;

            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfSchool(schoolId);
            doNothing().when(schoolService).softDeleteSchool(schoolId, teacherId);

            schoolUseCase.softDeleteSchool(schoolId);

            verify(sessionUser, times(1)).getParameter(SessionParameter.TEACHER_ID);
            verify(cascadeSoftDeleteService, times(1)).cascadeDeleteChildrenOfSchool(schoolId);
            verify(schoolService, times(1)).softDeleteSchool(schoolId, teacherId);
        }
    }

    @Nested
    class UpdateSchool {

        @Test
        void when_updating_school_expect_teacher_id_from_session() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final School schoolToUpdate = School.builder()
                    .name("Updated School Name").town("Updated Town").tlf(987654321).build();
            final School updatedSchool = School.builder()
                    .id(schoolId).teacherId(teacherId).name("Updated School Name").town("Updated Town").tlf(987654321).build();

            when(schoolService.updateSchool(eq(schoolId), any(School.class), eq(teacherId))).thenReturn(updatedSchool);

            final School result = schoolUseCase.updateSchool(schoolId, schoolToUpdate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(schoolId);
            assertThat(result.getName()).isEqualTo("Updated School Name");
            assertThat(result.getTown()).isEqualTo("Updated Town");
            assertThat(result.getTlf()).isEqualTo(987654321);
            verify(sessionUser, times(1)).getParameter(SessionParameter.TEACHER_ID);
            verify(schoolService, times(1)).updateSchool(eq(schoolId), any(School.class), eq(teacherId));
        }
    }
}
