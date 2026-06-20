package org.web.codefm.usecase.teachernotebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.service.teachernotebook.StudentGroupService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentGroupUseCaseImplTest {

    private StudentGroupUseCaseImpl studentGroupUseCase;

    @Mock
    private StudentGroupService studentGroupService;

    @BeforeEach
    void beforeEach() {
        studentGroupUseCase = new StudentGroupUseCaseImpl(studentGroupService);
    }

    @Nested
    class GenerateGroups {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void when_generating_groups_expect_delegated_to_service(final Boolean prioritizeShapeDiversity) {
            final Integer classId = 1;
            final List<List<Integer>> expectedGroups = List.of(
                    List.of(1, 2, 3, 4),
                    List.of(5, 6, 7, 8));
            when(studentGroupService.generateGroups(classId, prioritizeShapeDiversity)).thenReturn(expectedGroups);

            final List<List<Integer>> result = studentGroupUseCase.generateGroups(classId, prioritizeShapeDiversity);

            assertThat(result).isEqualTo(expectedGroups);
            verify(studentGroupService).generateGroups(classId, prioritizeShapeDiversity);
        }
    }
}
