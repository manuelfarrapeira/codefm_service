package org.web.codefm.infrastructure.teachernotebook;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Class;
import org.web.codefm.domain.entity.teachernotebook.School;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.ClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SchoolEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.ClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SchoolJPARepository;
import org.web.codefm.infrastructure.mapper.ClassMapper;
import org.web.codefm.infrastructure.mapper.SchoolMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolRepositoryImplTest {

    private SchoolRepositoryImpl schoolRepository;

    @Mock
    private SchoolJPARepository schoolJPARepository;

    @Mock
    private ClassJPARepository classJPARepository;

    @Mock
    private SchoolMapper schoolMapper;

    @Mock
    private ClassMapper classMapper;

    @BeforeEach
    void beforeEach() {
        this.schoolRepository = new SchoolRepositoryImpl(this.schoolJPARepository, this.classJPARepository, this.schoolMapper,
                this.classMapper);
    }

    @Nested
    class FindByTeacherId {

        @Test
        void when_teacher_has_schools_expect_schools_returned() {
            final Integer teacherId = 101;
            final SchoolEntity schoolEntity1 = new SchoolEntity(1, teacherId, "School A", "Town A", 123456789, null);
            final SchoolEntity schoolEntity2 = new SchoolEntity(2, teacherId, "School B", "Town B", 987654321, null);
            final School school1 = School.builder().id(1).teacherId(teacherId).name("School A").build();
            final School school2 = School.builder().id(2).teacherId(teacherId).name("School B").build();
            final ClassEntity classEntity = new ClassEntity(10, 1, "1A", "24/25", null);
            final Class clazz = Class.builder().id(10).schoolId(1).name("1A").schoolYear("24/25").build();

            when(SchoolRepositoryImplTest.this.schoolJPARepository.findByTeacherId(teacherId)).thenReturn(Arrays.asList(schoolEntity1, schoolEntity2));
            when(SchoolRepositoryImplTest.this.schoolMapper.toModelList(Arrays.asList(schoolEntity1, schoolEntity2))).thenReturn(
                    Arrays.asList(school1, school2));
            when(SchoolRepositoryImplTest.this.classJPARepository.findActiveClassesBySchoolIdAndTeacherId(1, teacherId)).thenReturn(
                    List.of(classEntity));
            when(SchoolRepositoryImplTest.this.classJPARepository.findActiveClassesBySchoolIdAndTeacherId(2, teacherId)).thenReturn(
                    Collections.emptyList());
            when(SchoolRepositoryImplTest.this.classMapper.toModelList(List.of(classEntity))).thenReturn(List.of(clazz));
            when(SchoolRepositoryImplTest.this.classMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

            final List<School> result = SchoolRepositoryImplTest.this.schoolRepository.findByTeacherId(teacherId);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getClasses()).hasSize(1);
            assertThat(result.get(0).getClasses().get(0).getName()).isEqualTo("1A");
            assertThat(result.get(1).getClasses()).isEmpty();
            verify(SchoolRepositoryImplTest.this.classJPARepository).findActiveClassesBySchoolIdAndTeacherId(1, teacherId);
            verify(SchoolRepositoryImplTest.this.classJPARepository).findActiveClassesBySchoolIdAndTeacherId(2, teacherId);
        }

        @Test
        void when_teacher_has_no_schools_expect_empty_list_returned() {
            final Integer teacherId = 101;

            when(SchoolRepositoryImplTest.this.schoolJPARepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());
            when(SchoolRepositoryImplTest.this.schoolMapper.toModelList(Collections.emptyList())).thenReturn(Collections.emptyList());

            final List<School> result = SchoolRepositoryImplTest.this.schoolRepository.findByTeacherId(teacherId);

            assertThat(result).isNotNull().isEmpty();
            verify(SchoolRepositoryImplTest.this.classJPARepository, never()).findActiveClassesBySchoolIdAndTeacherId(any(), any());
        }
    }

    @Nested
    class Save {

        @Test
        void when_valid_data_expect_entity_saved() {
            final School schoolToSave = School.builder().name("New School").build();
            final SchoolEntity schoolEntity = new SchoolEntity();
            final SchoolEntity savedSchoolEntity = new SchoolEntity();
            final School savedSchool = School.builder().id(1).name("New School").build();

            when(SchoolRepositoryImplTest.this.schoolMapper.toEntity(schoolToSave)).thenReturn(schoolEntity);
            when(SchoolRepositoryImplTest.this.schoolJPARepository.save(schoolEntity)).thenReturn(savedSchoolEntity);
            when(SchoolRepositoryImplTest.this.schoolMapper.toModel(savedSchoolEntity)).thenReturn(savedSchool);

            final School result = SchoolRepositoryImplTest.this.schoolRepository.save(schoolToSave);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("New School");
            verify(SchoolRepositoryImplTest.this.schoolMapper, times(1)).toEntity(schoolToSave);
            verify(SchoolRepositoryImplTest.this.schoolJPARepository, times(1)).save(schoolEntity);
            verify(SchoolRepositoryImplTest.this.schoolMapper, times(1)).toModel(savedSchoolEntity);
        }
    }

    @Nested
    class FindById {

        @Test
        void when_school_exists_expect_school_returned() {
            final Integer schoolId = 1;
            final SchoolEntity schoolEntity = new SchoolEntity(schoolId, 101, "School A", "Town A", 123456789, null);
            final School expectedSchool = School.builder().id(schoolId).name("School A").build();

            when(SchoolRepositoryImplTest.this.schoolJPARepository.findByIdAndDeletionDateIsNull(schoolId)).thenReturn(Optional.of(schoolEntity));
            when(SchoolRepositoryImplTest.this.schoolMapper.toModel(schoolEntity)).thenReturn(expectedSchool);

            final Optional<School> result = SchoolRepositoryImplTest.this.schoolRepository.findById(schoolId);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedSchool);
            verify(SchoolRepositoryImplTest.this.schoolJPARepository, times(1)).findByIdAndDeletionDateIsNull(schoolId);
            verify(SchoolRepositoryImplTest.this.schoolMapper, times(1)).toModel(schoolEntity);
        }

        @Test
        void when_school_does_not_exist_expect_empty_optional_returned() {
            final Integer schoolId = 1;

            when(SchoolRepositoryImplTest.this.schoolJPARepository.findByIdAndDeletionDateIsNull(schoolId)).thenReturn(Optional.empty());

            final Optional<School> result = SchoolRepositoryImplTest.this.schoolRepository.findById(schoolId);

            assertThat(result).isNotPresent();
            verify(SchoolRepositoryImplTest.this.schoolJPARepository, times(1)).findByIdAndDeletionDateIsNull(schoolId);
            verify(SchoolRepositoryImplTest.this.schoolMapper, never()).toModel(any(SchoolEntity.class));
        }
    }

    @Nested
    class SoftDeleteSchool {

        @Test
        void when_school_exists_expect_deletion_date_set() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final SchoolEntity schoolEntity = new SchoolEntity(schoolId, teacherId, "School A", "Town A", 123456789, null);
            final School updatedSchool = School.builder().id(schoolId).teacherId(teacherId).name("School A")
                    .deletionDate(LocalDate.now()).build();

            when(SchoolRepositoryImplTest.this.schoolJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId)).thenReturn(
                    Optional.of(schoolEntity));
            when(SchoolRepositoryImplTest.this.schoolJPARepository.save(any(SchoolEntity.class))).thenReturn(schoolEntity);
            when(SchoolRepositoryImplTest.this.schoolMapper.toModel(any(SchoolEntity.class))).thenReturn(updatedSchool);

            final School result = SchoolRepositoryImplTest.this.schoolRepository.softDeleteSchool(schoolId, teacherId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(schoolId);
            assertThat(result.getTeacherId()).isEqualTo(teacherId);
            assertThat(result.getDeletionDate()).isNotNull();
            verify(SchoolRepositoryImplTest.this.schoolJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId);
            verify(SchoolRepositoryImplTest.this.schoolJPARepository, times(1)).save(schoolEntity);
            verify(SchoolRepositoryImplTest.this.schoolMapper, times(1)).toModel(schoolEntity);
        }

        @Test
        void when_school_not_found_expect_exception() {
            final Integer schoolId = 1;
            final Integer teacherId = 101;
            final ThrowingCallable callable = () -> SchoolRepositoryImplTest.this.schoolRepository.softDeleteSchool(schoolId, teacherId);

            when(SchoolRepositoryImplTest.this.schoolJPARepository.findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId)).thenReturn(
                    Optional.empty());

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
            verify(SchoolRepositoryImplTest.this.schoolJPARepository, times(1)).findByIdAndTeacherIdAndDeletionDateIsNull(schoolId, teacherId);
            verify(SchoolRepositoryImplTest.this.schoolJPARepository, never()).save(any(SchoolEntity.class));
            verify(SchoolRepositoryImplTest.this.schoolMapper, never()).toModel(any(SchoolEntity.class));
        }
    }
}
