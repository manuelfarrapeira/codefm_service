package org.web.codefm.infrastructure.executor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.infrastructure.executor.ChunkLimitListPartitioningStrategy.ChunkLimitListPartitioningStrategyBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChunkLimitListPartitioningStrategyTest {

    @Mock
    ListPartitioningPropertyProvider listPartitioningPropertyProvider;

    @Test
    void whenListPartitioningPropertyProviderIsNull_ThrowsException() {

        final ChunkLimitListPartitioningStrategyBuilder builder = ChunkLimitListPartitioningStrategy.builder();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.build());

    }

    @Test
    void whenPartitionLimitIsNull_ThrowsException() {

        final ChunkLimitListPartitioningStrategyBuilder builder =
                ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(this.listPartitioningPropertyProvider);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.build());

    }

    @Test
    void whenPartitionLimitIsNotHigherThanZero_ThrowsException() {

        final ChunkLimitListPartitioningStrategyBuilder builder =
                ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(this.listPartitioningPropertyProvider);
        when(this.listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(0);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.build());

    }

    @Test
    void whenListToPartitionIsNull_ThrowsException() {

        final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(this.listPartitioningPropertyProvider);
        when(this.listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(2);
        final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> chunkLimitListPartitioningStrategy.split(null));
    }

    @ParameterizedTest
    @MethodSource("provideListToPartition")
    void whenCompleteChunksIsNotHigherThanZero_ReturnedListHasTheOriginalList(final List<Integer> listToPartition,
                                                                              final Integer expectedSize) {

        final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(this.listPartitioningPropertyProvider);
        final Integer partitionLimit = 2;

        when(this.listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(partitionLimit);
        final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

        final List<List<Integer>> listList = chunkLimitListPartitioningStrategy.split(listToPartition);

        assertNotNull(listList);
        assertEquals(expectedSize, listList.size());

        if (expectedSize > 0) {
            assertEquals(listToPartition, listList.get(0));
        }

    }

    static Stream<Arguments> provideListToPartition() {
        return Stream.of(
                Arguments.of(List.of(1), 1),
                Arguments.of(Collections.emptyList(), 0));
    }

    @Test
    void whenReminderIsZero_ReturnedListsHasPartitionLimitSize() {

        final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(this.listPartitioningPropertyProvider);
        final Integer partitionLimit = 2;
        final List<Integer> listToPartition = List.of(1, 2, 3, 4, 5, 6);

        when(this.listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(partitionLimit);
        final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

        final List<List<Integer>> listList = chunkLimitListPartitioningStrategy.split(listToPartition);

        assertNotNull(listList);
        assertEquals(listToPartition.size() / partitionLimit, listList.size());

        for (final List<Integer> partialList : listList) {
            assertEquals(partitionLimit, partialList.size());
        }
    }

    @Test
    void whenReminderIsHigherThanZeroButLessOrEqualThanFactorNextChunk_LastReturnedListsHasPartitionLimitPlusRemainderSize() {

        final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(this.listPartitioningPropertyProvider);
        final Integer partitionLimit = 3;
        final List<Integer> listToPartition = List.of(1, 2, 3, 4);

        when(this.listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(partitionLimit);
        final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

        final List<List<Integer>> listList = chunkLimitListPartitioningStrategy.split(listToPartition);

        assertNotNull(listList);
        assertEquals(listToPartition.size() / partitionLimit, listList.size());
        assertEquals(listToPartition, listList.get(0));
    }

    @Test
    void whenReminderIsHigherThanZeroAndHigherFactorNextChunk_LastReturnedListsHasRemainderSize() {

        final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(this.listPartitioningPropertyProvider);
        final Integer partitionLimit = 3;
        final List<Integer> listToPartition = List.of(1, 2, 3, 4, 5);

        when(this.listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(partitionLimit);
        final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

        final List<List<Integer>> listList = chunkLimitListPartitioningStrategy.split(listToPartition);

        assertNotNull(listList);
        assertEquals(listToPartition.size() / partitionLimit + 1, listList.size());
        // last list has remainder size
        assertEquals(listList.size() % partitionLimit, listList.get(listList.size() - 1).size());
    }
}
