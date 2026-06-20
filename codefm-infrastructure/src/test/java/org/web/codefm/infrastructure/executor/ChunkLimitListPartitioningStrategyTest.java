package org.web.codefm.infrastructure.executor;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChunkLimitListPartitioningStrategyTest {

    @Mock
    ListPartitioningPropertyProvider listPartitioningPropertyProvider;

    @Nested
    class Build {

        @Test
        void when_list_partitioning_property_provider_is_null_expect_exception() {
            final ChunkLimitListPartitioningStrategyBuilder builder = ChunkLimitListPartitioningStrategy.builder();
            final ThrowingCallable callable = builder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void when_partition_limit_is_null_expect_exception() {
            final ChunkLimitListPartitioningStrategyBuilder builder =
                    ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(listPartitioningPropertyProvider);
            final ThrowingCallable callable = builder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void when_partition_limit_is_not_higher_than_zero_expect_exception() {
            final ChunkLimitListPartitioningStrategyBuilder builder =
                    ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(listPartitioningPropertyProvider);
            when(listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(0);
            final ThrowingCallable callable = builder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Split {

        @Test
        void when_list_to_partition_is_null_expect_exception() {
            final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                    ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(listPartitioningPropertyProvider);
            when(listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(2);
            final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();
            final ThrowingCallable callable = () -> chunkLimitListPartitioningStrategy.split(null);

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.infrastructure.executor.ChunkLimitListPartitioningStrategyTest#provideListToPartition")
        void when_complete_chunks_is_not_higher_than_zero_expect_original_list_returned(final List<Integer> listToPartition,
                                                                                        final Integer expectedSize) {
            final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                    ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(listPartitioningPropertyProvider);
            final Integer partitionLimit = 2;
            when(listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(partitionLimit);
            final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

            final List<List<Integer>> listList = chunkLimitListPartitioningStrategy.split(listToPartition);

            assertThat(listList).isNotNull().hasSize(expectedSize);
            if (expectedSize > 0) {
                assertThat(listList.get(0)).isEqualTo(listToPartition);
            }
        }

        @Test
        void when_remainder_is_zero_expect_lists_with_partition_limit_size() {
            final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                    ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(listPartitioningPropertyProvider);
            final Integer partitionLimit = 2;
            final List<Integer> listToPartition = List.of(1, 2, 3, 4, 5, 6);
            when(listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(partitionLimit);
            final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

            final List<List<Integer>> listList = chunkLimitListPartitioningStrategy.split(listToPartition);

            assertThat(listList).isNotNull().hasSize(listToPartition.size() / partitionLimit);
            for (final List<Integer> partialList : listList) {
                assertThat(partialList).hasSize(partitionLimit);
            }
        }

        @Test
        void when_remainder_is_higher_than_zero_but_not_higher_than_factor_next_chunk_expect_single_list_returned() {
            final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                    ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(listPartitioningPropertyProvider);
            final Integer partitionLimit = 3;
            final List<Integer> listToPartition = List.of(1, 2, 3, 4);
            when(listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(partitionLimit);
            final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

            final List<List<Integer>> listList = chunkLimitListPartitioningStrategy.split(listToPartition);

            assertThat(listList).isNotNull().hasSize(listToPartition.size() / partitionLimit);
            assertThat(listList.get(0)).isEqualTo(listToPartition);
        }

        @Test
        void when_remainder_is_higher_than_zero_and_higher_than_factor_next_chunk_expect_last_list_with_remainder_size() {
            final ChunkLimitListPartitioningStrategyBuilder chunkLimitListPartitioningStrategyBuilder =
                    ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(listPartitioningPropertyProvider);
            final Integer partitionLimit = 3;
            final List<Integer> listToPartition = List.of(1, 2, 3, 4, 5);
            when(listPartitioningPropertyProvider.getPartitionLimit()).thenReturn(partitionLimit);
            final ChunkLimitListPartitioningStrategy chunkLimitListPartitioningStrategy = chunkLimitListPartitioningStrategyBuilder.build();

            final List<List<Integer>> listList = chunkLimitListPartitioningStrategy.split(listToPartition);

            assertThat(listList).isNotNull().hasSize(listToPartition.size() / partitionLimit + 1);
            assertThat(listList.get(listList.size() - 1)).hasSize(listList.size() % partitionLimit);
        }
    }

    static Stream<Arguments> provideListToPartition() {
        return Stream.of(
                Arguments.of(List.of(1), 1),
                Arguments.of(Collections.emptyList(), 0));
    }
}
