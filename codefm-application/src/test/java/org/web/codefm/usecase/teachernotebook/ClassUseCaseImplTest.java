package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.service.teachernotebook.CascadeSoftDeleteService;
import org.web.codefm.domain.service.teachernotebook.ClassService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassUseCaseImplTest {

    private ClassUseCaseImpl classUseCase;

    @Mock
    private ClassService classService;

    @Mock
    private CascadeSoftDeleteService cascadeSoftDeleteService;

    @Mock
    private SessionUser sessionUser;

    @BeforeEach
    void beforeEach() {
        classUseCase = new ClassUseCaseImpl(classService, cascadeSoftDeleteService, sessionUser);
        lenient().when(sessionUser.getParameter(SessionParameter.TEACHER_ID)).thenReturn(1);
    }

    @Nested
    class GetClassesBySchoolId {

        @Test
        void when_classes_found_expect_sorted_desc() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final Class class1 = Class.builder().id(1).schoolId(schoolId).name("Math 2023").schoolYear("23/24").build();
            final Class class2 = Class.builder().id(2).schoolId(schoolId).name("Math 2024").schoolYear("24/25").build();
            final Class class3 = Class.builder().id(3).schoolId(schoolId).name("Math 2022").schoolYear("22/23").build();

            when(classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId))
                    .thenReturn(Arrays.asList(class1, class2, class3));

            final List<Class> result = classUseCase.getClassesBySchoolId(schoolId);

            assertThat(result).isNotNull().hasSize(3);
            assertThat(result.get(0).getSchoolYear()).isEqualTo("24/25");
            assertThat(result.get(1).getSchoolYear()).isEqualTo("23/24");
            assertThat(result.get(2).getSchoolYear()).isEqualTo("22/23");
            verify(classService, times(1)).getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
        }

        @Test
        void when_no_classes_found_expect_empty_list() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;

            when(classService.getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId)).thenReturn(new ArrayList<>());

            final List<Class> result = classUseCase.getClassesBySchoolId(schoolId);

            assertThat(result).isNotNull().isEmpty();
            verify(classService, times(1)).getActiveClassesBySchoolIdAndTeacherId(schoolId, teacherId);
        }
    }

    @Nested
    class CreateClass {

        @Test
        void when_creating_class_expect_class_created() {
            final Integer schoolId = 1;
            final Integer teacherId = 1;
            final Class classToCreate = Class.builder().schoolId(schoolId).name("Math Class").schoolYear("24/25").build();
            final Class createdClass = Class.builder().id(1).schoolId(schoolId).name("Math Class").schoolYear("24/25").build();

            when(classService.createClass(classToCreate, teacherId)).thenReturn(createdClass);

            final Class result = classUseCase.createClass(classToCreate);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Math Class");
            assertThat(result.getSchoolYear()).isEqualTo("24/25");
            verify(classService, times(1)).createClass(classToCreate, teacherId);
        }
    }

    @Nested
    class SoftDeleteClass {

        @Test
        void when_deleting_class_expect_cascade_before_service() {
            final Integer classId = 1;
            final Integer teacherId = 1;

            doNothing().when(cascadeSoftDeleteService).cascadeDeleteChildrenOfClass(classId);
            doNothing().when(classService).softDeleteClass(classId, teacherId);

            classUseCase.softDeleteClass(classId);

            verify(sessionUser, times(1)).getParameter(SessionParameter.TEACHER_ID);
            verify(cascadeSoftDeleteService, times(1)).cascadeDeleteChildrenOfClass(classId);
            verify(classService, times(1)).softDeleteClass(classId, teacherId);
        }
    }

    @Nested
    class UpdateClass {

        @Test
        void when_updating_class_expect_teacher_id_from_session() {
            final Integer classId = 1;
            final Integer teacherId = 1;
            final Class updateData = Class.builder().name("New Name").schoolYear("24/25").build();
            final Class updatedClass = Class.builder().id(classId).schoolId(10).name("New Name").schoolYear("24/25").build();

            when(classService.updateClass(classId, updateData, teacherId)).thenReturn(updatedClass);

            final Class result = classUseCase.updateClass(classId, updateData);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getSchoolYear()).isEqualTo("24/25");
            verify(sessionUser, times(1)).getParameter(SessionParameter.TEACHER_ID);
            verify(classService, times(1)).updateClass(classId, updateData, teacherId);
        }
    }
}

