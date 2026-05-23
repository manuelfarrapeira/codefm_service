package org.web.codefm.infrastructure.executor;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.infrastructure.executor.ConcurrentCallReactorExecutor.ConcurrentCallReactorExecutorBuilder;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcurrentCallReactorExecutorTest {

    final Scheduler scheduler = reactor.core.scheduler.Schedulers.boundedElastic();

    @Mock
    ReactorExecutorPropertyProvider reactorExecutorPropertyProvider;

    @Mock
    ListPartitioningStrategy listPartitioningStrategy;

    @Nested
    class Build {

        @Test
        void when_property_provider_is_null_expect_exception() {
            final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                    ConcurrentCallReactorExecutor.builder().scheduler(scheduler).listPartitioningStrategy(listPartitioningStrategy);
            final ThrowingCallable callable = concurrentCallReactorExecutorBuilder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void when_list_partitioning_strategy_is_null_expect_exception() {
            final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                    ConcurrentCallReactorExecutor.builder().scheduler(scheduler)
                            .reactorExecutorPropertyProvider(reactorExecutorPropertyProvider);
            final ThrowingCallable callable = concurrentCallReactorExecutorBuilder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void when_scheduler_is_null_expect_exception() {
            final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                    ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(listPartitioningStrategy)
                            .reactorExecutorPropertyProvider(reactorExecutorPropertyProvider);
            final ThrowingCallable callable = concurrentCallReactorExecutorBuilder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.infrastructure.executor.ConcurrentCallReactorExecutorTest#provideFlatMapConcurrency")
        void when_concurrency_is_not_higher_than_zero_expect_exception(final Integer flatMapConcurrency) {
            final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                    ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(listPartitioningStrategy)
                            .reactorExecutorPropertyProvider(reactorExecutorPropertyProvider).scheduler(scheduler);
            when(reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(flatMapConcurrency);
            final ThrowingCallable callable = concurrentCallReactorExecutorBuilder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @MethodSource("org.web.codefm.infrastructure.executor.ConcurrentCallReactorExecutorTest#provideBlockTimeout")
        void when_block_timeout_is_not_higher_than_zero_expect_exception(final Long blockTimeout) {
            final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                    ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(listPartitioningStrategy)
                            .reactorExecutorPropertyProvider(reactorExecutorPropertyProvider).scheduler(scheduler);
            when(reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
            when(reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(blockTimeout);
            final ThrowingCallable callable = concurrentCallReactorExecutorBuilder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void when_retries_is_null_expect_exception() {
            final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                    ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(listPartitioningStrategy)
                            .reactorExecutorPropertyProvider(reactorExecutorPropertyProvider).scheduler(scheduler);
            when(reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
            when(reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(3000L);
            when(reactorExecutorPropertyProvider.getRetries()).thenReturn(null);
            final ThrowingCallable callable = concurrentCallReactorExecutorBuilder::build;

            assertThatThrownBy(callable).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class Execute {

        @Test
        void when_retry_is_zero_and_no_exception_expect_result() {
            final FunctionClassTest functionClassTestSpy = spy(new FunctionClassTest());
            final List<Integer> listToPartition1 = List.of(1, 2);
            final List<Integer> listToPartition2 = List.of(3, 4);
            final List<Integer> listToPartition = Stream.concat(listToPartition1.stream(), listToPartition2.stream())
                    .collect(Collectors.toList());
            final List<List<Integer>> splitList = new ArrayList<>();
            splitList.add(listToPartition1);
            splitList.add(listToPartition2);
            when(reactorExecutorPropertyProvider.getRetries()).thenReturn(0);
            when(reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
            when(reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(3000L);
            when(listPartitioningStrategy.split(listToPartition)).thenReturn(splitList);
            final ConcurrentCallReactorExecutor concurrentCallReactorExecutor =
                    ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(listPartitioningStrategy)
                            .reactorExecutorPropertyProvider(reactorExecutorPropertyProvider).scheduler(scheduler)
                            .build();

            final List<Integer> totalResult = concurrentCallReactorExecutor.execute(listToPartition, functionClassTestSpy::run);

            assertThat(totalResult).isNotNull().hasSize(listToPartition.size());
            Collections.sort(listToPartition);
            Collections.sort(totalResult);
            assertThat(totalResult).isEqualTo(listToPartition);
            verify(functionClassTestSpy, times(2)).run(anyList());
        }

        @Test
        void when_retry_is_two_and_exception_expect_three_calls_and_exception() {
            final FunctionClassExceptionTest functionClassExceptionTestSpy = spy(new FunctionClassExceptionTest());
            final List<Integer> listToPartition = List.of(1, 2);
            final List<List<Integer>> splitList = new ArrayList<>();
            splitList.add(listToPartition);
            when(reactorExecutorPropertyProvider.getRetries()).thenReturn(2);
            when(reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
            when(reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(3000L);
            when(listPartitioningStrategy.split(listToPartition)).thenReturn(splitList);
            final ConcurrentCallReactorExecutor concurrentCallReactorExecutor =
                    ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(listPartitioningStrategy)
                            .reactorExecutorPropertyProvider(reactorExecutorPropertyProvider).scheduler(scheduler)
                            .build();
            final ThrowingCallable callable = () -> concurrentCallReactorExecutor.execute(listToPartition, functionClassExceptionTestSpy::run);

            assertThatThrownBy(callable).isInstanceOf(NumberFormatException.class);
            verify(functionClassExceptionTestSpy, times(3)).run(anyList());
        }

        @Test
        void when_block_timeout_is_reached_expect_timeout_exception_thrown() {
            final FunctionClassSleepTest functionClassSleepTestSpy = spy(new FunctionClassSleepTest());
            final List<Integer> listToPartition = List.of(1, 2);
            final List<List<Integer>> splitList = new ArrayList<>();
            splitList.add(listToPartition);
            when(reactorExecutorPropertyProvider.getRetries()).thenReturn(0);
            when(reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
            when(reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(1000L);
            when(listPartitioningStrategy.split(listToPartition)).thenReturn(splitList);
            final ConcurrentCallReactorExecutor concurrentCallReactorExecutor =
                    ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(listPartitioningStrategy)
                            .reactorExecutorPropertyProvider(reactorExecutorPropertyProvider).scheduler(scheduler)
                            .build();
            final ThrowingCallable callable = () -> concurrentCallReactorExecutor.execute(listToPartition, functionClassSleepTestSpy::run);

            assertThatThrownBy(callable)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Timeout on blocking read");
        }
    }

    static Stream<Arguments> provideFlatMapConcurrency() {
        return Stream.of(
                Arguments.of(0),
                Arguments.of(-1));
    }

    static Stream<Arguments> provideBlockTimeout() {
        return Stream.of(
                Arguments.of(0L),
                Arguments.of(-1L));
    }

    @Slf4j
    static class FunctionClassTest {

        public List<Integer> run(final List<Integer> listIds) {
            log.info("FunctionClassTest run: {}", listIds);
            return listIds;
        }
    }

    @Slf4j
    static class FunctionClassSleepTest {

        public List<Integer> run(final List<Integer> listIds) {
            log.info("FunctionClassTest run: {}", listIds);
            Awaitility.waitAtMost(3, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS).until(() -> true);
            return listIds;
        }
    }

    @Slf4j
    static class FunctionClassExceptionTest {

        public List<Integer> run(final List<Integer> listIds) {
            throw new NumberFormatException("FunctionClassExceptionTest run - Unexpected exception");
        }
    }
}
