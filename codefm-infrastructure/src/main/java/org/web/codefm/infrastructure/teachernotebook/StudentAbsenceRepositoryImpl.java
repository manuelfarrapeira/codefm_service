package org.web.codefm.infrastructure.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.entity.teachernotebook.StudentAbsence;
import org.web.codefm.domain.repository.teachernotebook.StudentAbsenceRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StudentAbsenceRepositoryImpl implements StudentAbsenceRepository {

	private final StudentAbsenceJPARepository studentAbsenceJPARepository;
	private final StudentClassJPARepository studentClassJPARepository;
	private final StudentJPARepository studentJPARepository;
	private final SubjectJPARepository subjectJPARepository;
	private final StudentAbsenceMapper studentAbsenceMapper;

	@Override
	public List<StudentAbsence> findByStudentClassId(Integer studentClassId) {
		final List<StudentAbsenceEntity> entities = this.studentAbsenceJPARepository
				.findByStudentClassId(studentClassId);
		final List<StudentAbsence> absences = this.studentAbsenceMapper.toModelList(entities);
		this.enrichWithDetails(absences);
		return absences;
	}

	@Override
	public List<StudentAbsence> findByStudentClassIdAndDate(Integer studentClassId, LocalDate date) {
		final List<StudentAbsenceEntity> entities = this.studentAbsenceJPARepository
				.findByStudentClassIdAndAbsenceDate(studentClassId, date);
		final List<StudentAbsence> absences = this.studentAbsenceMapper.toModelList(entities);
		this.enrichWithDetails(absences);
		return absences;
	}

	@Override
	public List<StudentAbsence> findByClassIdAndDate(Integer classId, LocalDate date) {
		final List<StudentAbsenceEntity> entities = this.studentAbsenceJPARepository
				.findByClassIdAndAbsenceDate(classId, date);
		final List<StudentAbsence> absences = this.studentAbsenceMapper.toModelList(entities);
		this.enrichWithDetails(absences);
		return absences;
	}

	@Override
	public Optional<StudentAbsence> findByIdAndTeacherId(Integer id, Integer teacherId) {
		return this.studentAbsenceJPARepository.findByIdAndTeacherId(id, teacherId)
				.map(this.studentAbsenceMapper::toModel);
	}

	@Override
	public List<StudentAbsence> saveAll(List<StudentAbsence> absences) {
		final List<StudentAbsenceEntity> entities = this.studentAbsenceMapper.toEntityList(absences);
		final List<StudentAbsenceEntity> saved = this.studentAbsenceJPARepository.saveAll(entities);
		final List<StudentAbsence> result = this.studentAbsenceMapper.toModelList(saved);
		this.enrichWithDetails(result);
		return result;
	}

	@Override
	public void deleteById(Integer id) {
		this.studentAbsenceJPARepository.deleteById(id);
	}

	@Override
	public void deleteByStudentClassIdAndDate(Integer studentClassId, LocalDate date) {
		this.studentAbsenceJPARepository.deleteByStudentClassIdAndAbsenceDate(studentClassId, date);
	}

	@Override
	public boolean existsByStudentClassIdAndSubjectIdAndDate(Integer studentClassId, Integer subjectId,
			LocalDate date) {
		return this.studentAbsenceJPARepository.existsByStudentClassIdAndSubjectIdAndAbsenceDate(studentClassId,
				subjectId, date);
	}

	@Override
	public void deleteByStudentClassId(Integer studentClassId) {
		this.studentAbsenceJPARepository.deleteByStudentClassId(studentClassId);
	}

	@Override
	public void hardDeleteByClassId(Integer classId) {
		this.studentAbsenceJPARepository.hardDeleteByClassId(classId);
	}

	@Override
	public void hardDeleteByStudentId(Integer studentId) {
		this.studentAbsenceJPARepository.hardDeleteByStudentId(studentId);
	}

	@Override
	public void hardDeleteBySubjectClassId(Integer subjectClassId) {
		this.studentAbsenceJPARepository.hardDeleteBySubjectClassId(subjectClassId);
	}

	private void enrichWithDetails(List<StudentAbsence> absences) {
		if (absences.isEmpty()) {
			return;
		}

		final Set<Integer> studentClassIds = absences.stream().map(StudentAbsence::getStudentClassId)
				.collect(Collectors.toSet());
		final Map<Integer, StudentClassEntity> studentClassMap = this.studentClassJPARepository
				.findAllById(studentClassIds).stream()
				.collect(Collectors.toMap(StudentClassEntity::getId, Function.identity()));

		final Set<Integer> studentIds = studentClassMap.values().stream().map(StudentClassEntity::getStudentId)
				.collect(Collectors.toSet());
		final Set<Integer> subjectIds = absences.stream().map(StudentAbsence::getSubjectId).collect(Collectors.toSet());

		final Map<Integer, StudentEntity> studentsMap = this.studentJPARepository.findAllById(studentIds).stream()
				.collect(Collectors.toMap(StudentEntity::getId, Function.identity()));
		final Map<Integer, SubjectEntity> subjectsMap = this.subjectJPARepository.findAllById(subjectIds).stream()
				.collect(Collectors.toMap(SubjectEntity::getId, Function.identity()));

		absences.forEach(absence -> {
			final StudentClassEntity sc = studentClassMap.get(absence.getStudentClassId());
			if (sc != null) {
				absence.setStudentId(sc.getStudentId());
				absence.setClassId(sc.getClassId());
				final StudentEntity student = studentsMap.get(sc.getStudentId());
				if (student != null) {
					absence.setStudentName(student.getName());
					absence.setStudentSurnames(student.getSurnames());
				}
			}
			final SubjectEntity subject = subjectsMap.get(absence.getSubjectId());
			if (subject != null) {
				absence.setSubjectName(subject.getName());
			}
		});
	}
}
