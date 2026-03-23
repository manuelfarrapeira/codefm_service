package org.web.codefm.domain.usecase.teachernotebook;

import java.util.List;

/**
 * Interface that defines student group generation operations.
 * Handles the creation of balanced student groups for a given class.
 */
public interface StudentGroupUseCase {

    /**
     * Generates balanced groups of students for a given class.
     * Groups will have 3-4 students, balanced by gender and shape.
     * Priority 1 is always distributing one CIRCLE per group.
     * When prioritizeShapeDiversity is true, priority 2 is shape diversity and priority 3 is gender diversity.
     * When prioritizeShapeDiversity is false, priority 2 is gender diversity and priority 3 is shape diversity.
     *
     * @param classId                  The unique identifier of the class
     * @param prioritizeShapeDiversity When true, shape diversity takes priority over gender diversity; when false, gender diversity takes priority
     * @return List of groups, each group being a list of student IDs
     */
    List<List<Integer>> generateGroups(Integer classId, Boolean prioritizeShapeDiversity);
}
