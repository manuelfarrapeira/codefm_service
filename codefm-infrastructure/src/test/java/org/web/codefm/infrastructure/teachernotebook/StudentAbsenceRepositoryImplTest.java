package org.web.codefm.infrastructure.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheEvictionService;
import org.web.codefm.infrastructure.cache.teachernotebook.CacheName;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentAbsenceEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentClassEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.StudentEntity;
import org.web.codefm.infrastructure.entity.mariadb.teachernotebook.SubjectEntity;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentAbsenceJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentClassJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.StudentJPARepository;
import org.web.codefm.infrastructure.jpa.teachernotebook.SubjectJPARepository;
import org.web.codefm.infrastructure.mapper.StudentAbsenceMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentAbsenceRepositoryImplTest {

    private StudentAbsenceRepositoryImpl studentAbsenceRepository;

    @Mock
    private StudentAbsenceJPARepository studentAbsenceJPARepository;

    @Mock
    private StudentClassJPARepository studentClassJPARepository;

    @Mock
    private StudentJPARepository studentJPARepository;

    @Mock
    private SubjectJPARepository subjectJPARepository;

    @Mock
    private StudentAbsenceMapper studentAbsenceMapper;

    @Mock
    private CacheEvictionService cacheEvictionService;

    private static final Integer STUDENT_CLASS_ID = 50;
    private static final Integer STUDENT_ID = 10;
    private static final Integer CLASS_ID = 1;
    private static final Integer SUBJECT_ID = 20;

    @BeforeEach
    void beforeEach() {
        this.studentAbsenceRepository = new StudentAbsenceRepositoryImpl(this.studentAbsenceJPARepository,
                this.studentClassJPARepository, this.studentJPARepository, this.subjectJPARepository,
                this.studentAbsenceMapper, this.cacheEvictionService);
    }

    @Nested
    class FindByStudentClassId {

        @Test
        void when_absences_exist_expect_enriched_absences_returned() {
            final LocalDate date = LocalDate.of(2025, 1, 15);
            final StudentAbsenceEntity entity = new StudentAbsenceEntity(1, STUDENT_CLASS_ID, SUBJECT_ID, date);
            final StudentAbsence absence = StudentAbsence.builder().id(1).studentClassId(STUDENT_CLASS_ID)
                    .subjectId(SUBJECT_ID).absenceDate(date).build();
            final StudentClassEntity studentClassEntity = new StudentClassEntity();
            studentClassEntity.setId(STUDENT_CLASS_ID);
            studentClassEntity.setStudentId(STUDENT_ID);
            studentClassEntity.setClassId(CLASS_ID);
            final StudentEntity studentEntity = new StudentEntity();
            studentEntity.setId(STUDENT_ID);
            studentEntity.setName("Juan");
            studentEntity.setSurnames("García López");
            final SubjectEntity subjectEntity = new SubjectEntity();
            subjectEntity.setId(SUBJECT_ID);
            subjectEntity.setName("Matemáticas");
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository.findByStudentClassId(STUDENT_CLASS_ID))
                    .thenReturn(List.of(entity));
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceMapper.toModelList(List.of(entity)))
                    .thenReturn(List.of(absence));
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository.findAllById(any()))
                    .thenReturn(List.of(studentClassEntity));
            when(StudentAbsenceRepositoryImplTest.this.studentJPARepository.findAllById(any()))
                    .thenReturn(List.of(studentEntity));
            when(StudentAbsenceRepositoryImplTest.this.subjectJPARepository.findAllById(any()))
                    .thenReturn(List.of(subjectEntity));

            final List<StudentAbsence> result = StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository
                    .findByStudentClassId(STUDENT_CLASS_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(STUDENT_ID);
            assertThat(result.get(0).getClassId()).isEqualTo(CLASS_ID);
            assertThat(result.get(0).getStudentName()).isEqualTo("Juan");
            assertThat(result.get(0).getStudentSurnames()).isEqualTo("García López");
            assertThat(result.get(0).getSubjectName()).isEqualTo("Matemáticas");
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).findByStudentClassId(
                    STUDENT_CLASS_ID);
        }
    }

    @Nested
    class FindByClassId {

        @Test
        void when_absences_exist_expect_enriched_absences_returned() {
            final LocalDate date = LocalDate.of(2025, 4, 5);
            final StudentAbsenceEntity entity = new StudentAbsenceEntity(3, STUDENT_CLASS_ID, SUBJECT_ID, date);
            final StudentAbsence absence = StudentAbsence.builder().id(3).studentClassId(STUDENT_CLASS_ID)
                    .subjectId(SUBJECT_ID).absenceDate(date).build();
            final StudentClassEntity studentClassEntity = new StudentClassEntity();
            studentClassEntity.setId(STUDENT_CLASS_ID);
            studentClassEntity.setStudentId(STUDENT_ID);
            studentClassEntity.setClassId(CLASS_ID);
            final StudentEntity studentEntity = new StudentEntity();
            studentEntity.setId(STUDENT_ID);
            studentEntity.setName("Pedro");
            studentEntity.setSurnames("López Díaz");
            final SubjectEntity subjectEntity = new SubjectEntity();
            subjectEntity.setId(SUBJECT_ID);
            subjectEntity.setName("Ciencias");
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository.findByClassId(CLASS_ID))
                    .thenReturn(List.of(entity));
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceMapper.toModelList(List.of(entity)))
                    .thenReturn(List.of(absence));
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository.findAllById(any()))
                    .thenReturn(List.of(studentClassEntity));
            when(StudentAbsenceRepositoryImplTest.this.studentJPARepository.findAllById(any()))
                    .thenReturn(List.of(studentEntity));
            when(StudentAbsenceRepositoryImplTest.this.subjectJPARepository.findAllById(any()))
                    .thenReturn(List.of(subjectEntity));

            final List<StudentAbsence> result = StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository
                    .findByClassId(CLASS_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(STUDENT_ID);
            assertThat(result.get(0).getClassId()).isEqualTo(CLASS_ID);
            assertThat(result.get(0).getStudentName()).isEqualTo("Pedro");
            assertThat(result.get(0).getStudentSurnames()).isEqualTo("López Díaz");
            assertThat(result.get(0).getSubjectName()).isEqualTo("Ciencias");
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).findByClassId(CLASS_ID);
        }
    }

    @Nested
    class FindByClassIdAndDate {

        @Test
        void when_absences_exist_expect_enriched_absences_returned() {
            final LocalDate date = LocalDate.of(2025, 3, 10);
            final StudentAbsenceEntity entity = new StudentAbsenceEntity(2, STUDENT_CLASS_ID, SUBJECT_ID, date);
            final StudentAbsence absence = StudentAbsence.builder().id(2).studentClassId(STUDENT_CLASS_ID)
                    .subjectId(SUBJECT_ID).absenceDate(date).build();
            final StudentClassEntity studentClassEntity = new StudentClassEntity();
            studentClassEntity.setId(STUDENT_CLASS_ID);
            studentClassEntity.setStudentId(STUDENT_ID);
            studentClassEntity.setClassId(CLASS_ID);
            final StudentEntity studentEntity = new StudentEntity();
            studentEntity.setId(STUDENT_ID);
            studentEntity.setName("Ana");
            studentEntity.setSurnames("Martínez Ruiz");
            final SubjectEntity subjectEntity = new SubjectEntity();
            subjectEntity.setId(SUBJECT_ID);
            subjectEntity.setName("Lengua");
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository.findByClassIdAndAbsenceDate(CLASS_ID,
                    date)).thenReturn(List.of(entity));
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceMapper.toModelList(List.of(entity)))
                    .thenReturn(List.of(absence));
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository.findAllById(any()))
                    .thenReturn(List.of(studentClassEntity));
            when(StudentAbsenceRepositoryImplTest.this.studentJPARepository.findAllById(any()))
                    .thenReturn(List.of(studentEntity));
            when(StudentAbsenceRepositoryImplTest.this.subjectJPARepository.findAllById(any()))
                    .thenReturn(List.of(subjectEntity));

            final List<StudentAbsence> result = StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository
                    .findByClassIdAndDate(CLASS_ID, date);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStudentId()).isEqualTo(STUDENT_ID);
            assertThat(result.get(0).getClassId()).isEqualTo(CLASS_ID);
            assertThat(result.get(0).getStudentName()).isEqualTo("Ana");
            assertThat(result.get(0).getStudentSurnames()).isEqualTo("Martínez Ruiz");
            assertThat(result.get(0).getSubjectName()).isEqualTo("Lengua");
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).findByClassIdAndAbsenceDate(
                    CLASS_ID, date);
        }
    }

    @Nested
    class FindByIdAndTeacherId {

        @Test
        void when_absence_exists_expect_absence_returned() {
            final Integer id = 1;
            final Integer teacherId = 5;
            final LocalDate absenceDate = LocalDate.of(2025, 2, 1);
            final StudentAbsenceEntity entity = new StudentAbsenceEntity(id, STUDENT_CLASS_ID, SUBJECT_ID,
                    absenceDate);
            final StudentAbsence absence = StudentAbsence.builder().id(id).studentClassId(STUDENT_CLASS_ID)
                    .subjectId(SUBJECT_ID).absenceDate(absenceDate).build();
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository.findByIdAndTeacherId(id, teacherId))
                    .thenReturn(Optional.of(entity));
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceMapper.toModel(entity)).thenReturn(absence);

            final Optional<StudentAbsence> result = StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository
                    .findByIdAndTeacherId(id, teacherId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).findByIdAndTeacherId(id,
                    teacherId);
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceMapper).toModel(entity);
        }

        @Test
        void when_absence_does_not_exist_expect_empty_optional_returned() {
            final Integer id = 999;
            final Integer teacherId = 5;
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository.findByIdAndTeacherId(id, teacherId))
                    .thenReturn(Optional.empty());

            final Optional<StudentAbsence> result = StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository
                    .findByIdAndTeacherId(id, teacherId);

            assertThat(result).isNotPresent();
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).findByIdAndTeacherId(id,
                    teacherId);
            verifyNoInteractions(StudentAbsenceRepositoryImplTest.this.studentAbsenceMapper);
        }
    }

    @Nested
    class SaveAll {

        @Test
        void when_absences_are_saved_expect_enriched_absences_returned() {
            final LocalDate date = LocalDate.of(2025, 4, 5);
            final StudentAbsence inputAbsence = StudentAbsence.builder().studentClassId(STUDENT_CLASS_ID)
                    .subjectId(SUBJECT_ID).absenceDate(date).build();
            final StudentAbsenceEntity inputEntity = new StudentAbsenceEntity(null, STUDENT_CLASS_ID, SUBJECT_ID, date);
            final StudentAbsenceEntity savedEntity = new StudentAbsenceEntity(3, STUDENT_CLASS_ID, SUBJECT_ID, date);
            final StudentAbsence mappedAbsence = StudentAbsence.builder().id(3).studentClassId(STUDENT_CLASS_ID)
                    .subjectId(SUBJECT_ID).absenceDate(date).build();
            final StudentClassEntity studentClassEntity = new StudentClassEntity();
            studentClassEntity.setId(STUDENT_CLASS_ID);
            studentClassEntity.setStudentId(STUDENT_ID);
            studentClassEntity.setClassId(CLASS_ID);
            final StudentEntity studentEntity = new StudentEntity();
            studentEntity.setId(STUDENT_ID);
            studentEntity.setName("Pedro");
            studentEntity.setSurnames("López Sánchez");
            final SubjectEntity subjectEntity = new SubjectEntity();
            subjectEntity.setId(SUBJECT_ID);
            subjectEntity.setName("Ciencias");
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceMapper.toEntityList(List.of(inputAbsence)))
                    .thenReturn(List.of(inputEntity));
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository.saveAll(List.of(inputEntity)))
                    .thenReturn(List.of(savedEntity));
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceMapper.toModelList(List.of(savedEntity)))
                    .thenReturn(List.of(mappedAbsence));
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository.findAllById(any()))
                    .thenReturn(List.of(studentClassEntity));
            when(StudentAbsenceRepositoryImplTest.this.studentJPARepository.findAllById(any()))
                    .thenReturn(List.of(studentEntity));
            when(StudentAbsenceRepositoryImplTest.this.subjectJPARepository.findAllById(any()))
                    .thenReturn(List.of(subjectEntity));
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository
                    .findDistinctClassIdsByStudentClassIds(List.of(STUDENT_CLASS_ID))).thenReturn(List.of(CLASS_ID));

            final List<StudentAbsence> result = StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository
                    .saveAll(List.of(inputAbsence));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(3);
            assertThat(result.get(0).getStudentId()).isEqualTo(STUDENT_ID);
            assertThat(result.get(0).getClassId()).isEqualTo(CLASS_ID);
            assertThat(result.get(0).getStudentName()).isEqualTo("Pedro");
            assertThat(result.get(0).getStudentSurnames()).isEqualTo("López Sánchez");
            assertThat(result.get(0).getSubjectName()).isEqualTo("Ciencias");
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).saveAll(List.of(inputEntity));
            verify(StudentAbsenceRepositoryImplTest.this.cacheEvictionService).evict(
                    CacheName.STUDENT_ABSENCES_BY_CLASS, CLASS_ID);
        }
    }

    @Nested
    class DeleteById {

        @Test
        void when_absence_exists_expect_cache_evicted_and_absence_deleted() {
            final Integer id = 1;
            final StudentAbsenceEntity entity = new StudentAbsenceEntity(id, STUDENT_CLASS_ID, SUBJECT_ID,
                    LocalDate.now());
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository.findById(id))
                    .thenReturn(Optional.of(entity));
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository
                    .findDistinctClassIdsByStudentClassIds(List.of(STUDENT_CLASS_ID))).thenReturn(List.of(CLASS_ID));

            StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository.deleteById(id);

            verify(StudentAbsenceRepositoryImplTest.this.cacheEvictionService).evict(
                    CacheName.STUDENT_ABSENCES_BY_CLASS, CLASS_ID);
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).deleteById(id);
        }
    }

    @Nested
    class DeleteByStudentClassIdAndDate {

        @Test
        void when_absence_is_deleted_expect_cache_evicted_and_delete_delegated() {
            final LocalDate date = LocalDate.of(2025, 5, 20);
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository
                    .findDistinctClassIdsByStudentClassIds(List.of(STUDENT_CLASS_ID))).thenReturn(List.of(CLASS_ID));

            StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository.deleteByStudentClassIdAndDate(
                    STUDENT_CLASS_ID, date);

            verify(StudentAbsenceRepositoryImplTest.this.cacheEvictionService).evict(
                    CacheName.STUDENT_ABSENCES_BY_CLASS, CLASS_ID);
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository)
                    .deleteByStudentClassIdAndAbsenceDate(STUDENT_CLASS_ID, date);
        }
    }

    @Nested
    class ExistsByStudentClassIdAndSubjectIdAndDate {

        @Test
        void when_absence_exists_expect_true_returned() {
            final LocalDate date = LocalDate.of(2025, 6, 1);
            when(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository
                    .existsByStudentClassIdAndSubjectIdAndAbsenceDate(STUDENT_CLASS_ID, SUBJECT_ID, date))
                    .thenReturn(true);

            final boolean result = StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository
                    .existsByStudentClassIdAndSubjectIdAndDate(STUDENT_CLASS_ID, SUBJECT_ID, date);

            assertThat(result).isTrue();
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository)
                    .existsByStudentClassIdAndSubjectIdAndAbsenceDate(STUDENT_CLASS_ID, SUBJECT_ID, date);
        }
    }

    @Nested
    class DeleteByStudentClassId {

        @Test
        void when_absences_are_deleted_expect_cache_evicted_and_delete_delegated() {
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository
                    .findDistinctClassIdsByStudentClassIds(List.of(STUDENT_CLASS_ID))).thenReturn(List.of(CLASS_ID));

            StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository.deleteByStudentClassId(STUDENT_CLASS_ID);

            verify(StudentAbsenceRepositoryImplTest.this.cacheEvictionService).evict(
                    CacheName.STUDENT_ABSENCES_BY_CLASS, CLASS_ID);
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).deleteByStudentClassId(
                    STUDENT_CLASS_ID);
        }
    }

    @Nested
    class HardDeleteByClassId {

        @Test
        void when_absences_are_hard_deleted_expect_cache_evicted_and_delete_delegated() {
            StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository.hardDeleteByClassId(CLASS_ID);

            verify(StudentAbsenceRepositoryImplTest.this.cacheEvictionService).evict(
                    CacheName.STUDENT_ABSENCES_BY_CLASS, CLASS_ID);
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).hardDeleteByClassId(CLASS_ID);
        }
    }

    @Nested
    class HardDeleteByStudentId {

        @Test
        void when_absences_are_hard_deleted_expect_cache_evicted_and_delete_delegated() {
            when(StudentAbsenceRepositoryImplTest.this.studentClassJPARepository.findDistinctClassIdsByStudentId(
                    STUDENT_ID)).thenReturn(List.of(CLASS_ID));

            StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository.hardDeleteByStudentId(STUDENT_ID);

            verify(StudentAbsenceRepositoryImplTest.this.cacheEvictionService).evict(
                    CacheName.STUDENT_ABSENCES_BY_CLASS, CLASS_ID);
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository).hardDeleteByStudentId(STUDENT_ID);
        }
    }

    @Nested
    class HardDeleteBySubjectClassId {

        @Test
        void when_absences_are_hard_deleted_expect_cache_evicted_and_delete_delegated() {
            final Integer subjectClassId = 99;
            when(StudentAbsenceRepositoryImplTest.this.subjectJPARepository.findDistinctClassIdBySubjectClassId(
                    subjectClassId)).thenReturn(Optional.of(CLASS_ID));

            StudentAbsenceRepositoryImplTest.this.studentAbsenceRepository.hardDeleteBySubjectClassId(subjectClassId);

            verify(StudentAbsenceRepositoryImplTest.this.cacheEvictionService).evict(
                    CacheName.STUDENT_ABSENCES_BY_CLASS, CLASS_ID);
            verify(StudentAbsenceRepositoryImplTest.this.studentAbsenceJPARepository)
                    .hardDeleteBySubjectClassId(subjectClassId);
        }
    }
}
