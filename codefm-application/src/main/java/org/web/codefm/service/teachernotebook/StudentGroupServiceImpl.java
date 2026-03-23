package org.web.codefm.service.teachernotebook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.enums.StudentShape;
import org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException;
import org.web.codefm.domain.exception.teachernotebook.StudentGroupValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.repository.teachernotebook.ClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentClassRepository;
import org.web.codefm.domain.repository.teachernotebook.StudentRepository;
import org.web.codefm.domain.service.teachernotebook.StudentGroupService;
import org.web.codefm.domain.session.SessionParameter;
import org.web.codefm.domain.session.SessionUser;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentGroupServiceImpl implements StudentGroupService {

    private final ClassRepository classRepository;
    private final StudentClassRepository studentClassRepository;
    private final StudentRepository studentRepository;
    private final MessageSource messageSource;
    private final SessionUser sessionUser;

    @Override
    public List<List<Integer>> generateGroups(Integer classId, Boolean prioritizeShapeDiversity) {
        final Integer teacherId = this.sessionUser.getParameter(SessionParameter.TEACHER_ID);
        final Locale locale = this.sessionUser.getLocale();

        this.validateClassOwnership(classId, teacherId, locale);

        final List<Integer> studentIds = this.studentClassRepository.findActiveStudentIdsByClassId(classId);
        final List<Student> students = this.studentRepository.findByIdsAndTeacherIdAndDeletionDateIsNull(studentIds, teacherId);

        this.validateAllStudentsHaveShape(students, locale);
        this.validateStudentCount(students.size(), locale);

        final boolean shapePriority = prioritizeShapeDiversity == null || prioritizeShapeDiversity;
        return this.buildGroups(students, shapePriority);
    }

    private void validateClassOwnership(Integer classId, Integer teacherId, Locale locale) {
        this.classRepository.findByIdAndTeacherIdAndDeletionDateIsNull(classId, teacherId)
                .orElseThrow(() -> new ClassNotFoundException(
                        this.messageSource.getMessage(MessageKeys.CLASS_NOT_FOUND, null, locale)));
    }

    private void validateAllStudentsHaveShape(List<Student> students, Locale locale) {
        final List<String> studentsWithoutShape = students.stream()
                .filter(s -> s.getShape() == null || s.getShape().trim().isEmpty())
                .map(s -> s.getName() + " " + s.getSurnames()).toList();

        if (!studentsWithoutShape.isEmpty()) {
            final String names = String.join(", ", studentsWithoutShape);
            final String message = this.messageSource.getMessage(
                    MessageKeys.STUDENT_GROUP_MISSING_SHAPE, new Object[]{names}, locale);
            throw new StudentGroupValidationException(
                    List.of(new ErrorMessage("shape", message)));
        }
    }

    private void validateStudentCount(int count, Locale locale) {
        if (count < 3) {
            final String message = this.messageSource.getMessage(
                    MessageKeys.STUDENT_GROUP_MIN_STUDENTS, null, locale);
            throw new StudentGroupValidationException(
                    List.of(new ErrorMessage("classId", message)));
        }

        if (count == 5) {
            final String message = this.messageSource.getMessage(
                    MessageKeys.STUDENT_GROUP_IMPOSSIBLE_COUNT, new Object[]{count}, locale);
            throw new StudentGroupValidationException(
                    List.of(new ErrorMessage("classId", message)));
        }
    }

    private List<List<Integer>> buildGroups(List<Student> students, boolean prioritizeShapeDiversity) {
        final int n = students.size();
        final List<Integer> groupSizes = this.calculateGroupSizes(n);
        final int numGroups = groupSizes.size();

        final List<Student> circles = new ArrayList<>();
        final List<Student> squares = new ArrayList<>();
        final List<Student> triangles = new ArrayList<>();

        for (Student student : students) {
            final StudentShape shape = StudentShape.valueOf(student.getShape());
            switch (shape) {
                case CIRCLE:
                    circles.add(student);
                    break;
                case SQUARE:
                    squares.add(student);
                    break;
                case TRIANGLE:
                    triangles.add(student);
                    break;
                default:
                    break;
            }
        }

        final List<List<Integer>> groups = new ArrayList<>();
        for (int i = 0; i < numGroups; i++) {
            groups.add(new ArrayList<>());
        }

        final List<Student> extraCircles = new ArrayList<>();
        for (int i = 0; i < circles.size(); i++) {
            if (i < numGroups) {
                groups.get(i).add(circles.get(i).getId());
            } else {
                extraCircles.add(circles.get(i));
            }
        }

        final List<Student> remainingPool = this.buildBalancedPool(squares, triangles, extraCircles, prioritizeShapeDiversity);

        int groupIndex = 0;
        for (Student student : remainingPool) {
            while (groupIndex < numGroups && groups.get(groupIndex).size() >= groupSizes.get(groupIndex)) {
                groupIndex++;
            }
            if (groupIndex >= numGroups) {
                break;
            }
            groups.get(groupIndex).add(student.getId());
        }

        return groups;
    }

    private List<Student> buildBalancedPool(List<Student> squares, List<Student> triangles,
                                            List<Student> extraCircles, boolean prioritizeShapeDiversity) {
        final List<Student> allRemaining = new ArrayList<>();
        allRemaining.addAll(squares);
        allRemaining.addAll(triangles);
        allRemaining.addAll(extraCircles);

        if (prioritizeShapeDiversity) {
            return this.buildPoolShapeThenGender(allRemaining);
        }
        return this.buildPoolGenderThenShape(allRemaining);
    }

    private List<Student> buildPoolShapeThenGender(List<Student> allRemaining) {
        final List<Student> males = new ArrayList<>();
        final List<Student> females = new ArrayList<>();

        for (Student student : allRemaining) {
            if ("M".equals(student.getGender())) {
                males.add(student);
            } else {
                females.add(student);
            }
        }

        this.sortByShapeDiversity(males);
        this.sortByShapeDiversity(females);

        final List<Student> balancedPool = new ArrayList<>();
        int mIdx = 0;
        int fIdx = 0;

        while (mIdx < males.size() || fIdx < females.size()) {
            if (mIdx < males.size()) {
                balancedPool.add(males.get(mIdx++));
            }
            if (fIdx < females.size()) {
                balancedPool.add(females.get(fIdx++));
            }
        }

        return balancedPool;
    }

    private List<Student> buildPoolGenderThenShape(List<Student> allRemaining) {
        final Map<String, List<Student>> byShape = new LinkedHashMap<>();
        byShape.put(StudentShape.SQUARE.name(), new ArrayList<>());
        byShape.put(StudentShape.TRIANGLE.name(), new ArrayList<>());
        byShape.put(StudentShape.CIRCLE.name(), new ArrayList<>());

        for (Student student : allRemaining) {
            byShape.getOrDefault(student.getShape(), byShape.get(StudentShape.CIRCLE.name())).add(student);
        }

        for (List<Student> shapeList : byShape.values()) {
            this.sortByGenderDiversity(shapeList);
        }

        final List<Student> balancedPool = new ArrayList<>();
        boolean added = true;
        while (added) {
            added = false;
            for (List<Student> shapeList : byShape.values()) {
                if (!shapeList.isEmpty()) {
                    balancedPool.add(shapeList.remove(0));
                    added = true;
                }
            }
        }

        return balancedPool;
    }

    private void sortByGenderDiversity(List<Student> students) {
        final List<Student> males = new ArrayList<>();
        final List<Student> females = new ArrayList<>();

        for (Student student : students) {
            if ("M".equals(student.getGender())) {
                males.add(student);
            } else {
                females.add(student);
            }
        }

        students.clear();
        int mIdx = 0;
        int fIdx = 0;
        while (mIdx < males.size() || fIdx < females.size()) {
            if (mIdx < males.size()) {
                students.add(males.get(mIdx++));
            }
            if (fIdx < females.size()) {
                students.add(females.get(fIdx++));
            }
        }
    }

    private void sortByShapeDiversity(List<Student> students) {
        final Map<String, List<Student>> byShape = new LinkedHashMap<>();
        byShape.put(StudentShape.SQUARE.name(), new ArrayList<>());
        byShape.put(StudentShape.TRIANGLE.name(), new ArrayList<>());
        byShape.put(StudentShape.CIRCLE.name(), new ArrayList<>());

        for (Student student : students) {
            byShape.getOrDefault(student.getShape(), byShape.get(StudentShape.CIRCLE.name())).add(student);
        }

        students.clear();
        boolean added = true;
        while (added) {
            added = false;
            for (List<Student> shapeList : byShape.values()) {
                if (!shapeList.isEmpty()) {
                    students.add(shapeList.remove(0));
                    added = true;
                }
            }
        }
    }

    private List<Integer> calculateGroupSizes(int n) {
        final List<Integer> sizes = new ArrayList<>();
        final int remainder = n % 4;

        int groupsOf4;
        int groupsOf3;

        switch (remainder) {
            case 0:
                groupsOf4 = n / 4;
                groupsOf3 = 0;
                break;
            case 3:
                groupsOf4 = n / 4;
                groupsOf3 = 1;
                break;
            case 2:
                groupsOf4 = n / 4 - 1;
                groupsOf3 = 2;
                break;
            case 1:
                groupsOf4 = n / 4 - 2;
                groupsOf3 = 3;
                break;
            default:
                groupsOf4 = 0;
                groupsOf3 = 0;
                break;
        }

        for (int i = 0; i < groupsOf4; i++) {
            sizes.add(4);
        }
        for (int i = 0; i < groupsOf3; i++) {
            sizes.add(3);
        }

        return sizes;
    }
}

