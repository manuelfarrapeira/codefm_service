package org.web.codefm.domain.service.teachernotebook;

import java.util.List;

/**
 * Service interface for student group generation business logic.
 * Acts as an intermediary between the use case and repositories.
 */
public interface StudentGroupService {

    /**
     * Generates balanced groups of students for a given class.
     * Validates class ownership, shape assignment and student count.
     * Groups will have 3-4 students, balanced by gender and shape.
     * Priority 1 is always distributing one CIRCLE per group.
     * When prioritizeShapeDiversity is true, priority 2 is shape diversity and priority 3 is gender diversity.
     * When prioritizeShapeDiversity is false, priority 2 is gender diversity and priority 3 is shape diversity.
     *
     * @param classId                  The unique identifier of the class
     * @param prioritizeShapeDiversity When true, shape diversity takes priority over gender diversity; when false, gender diversity takes priority
     * @return List of groups, each group being a list of student IDs
     * @throws org.web.codefm.domain.exception.teachernotebook.ClassNotFoundException          if class not found or not owned
     * @throws org.web.codefm.domain.exception.teachernotebook.StudentGroupValidationException if validation fails
     */
    List<List<Integer>> generateGroups(Integer classId, Boolean prioritizeShapeDiversity);
}
