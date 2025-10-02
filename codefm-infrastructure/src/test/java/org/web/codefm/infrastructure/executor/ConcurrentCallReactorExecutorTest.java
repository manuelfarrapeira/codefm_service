package org.web.codefm.infrastructure.executor;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcurrentCallReactorExecutorTest {

    final Scheduler scheduler = reactor.core.scheduler.Schedulers.boundedElastic();

    @Mock
    ReactorExecutorPropertyProvider reactorExecutorPropertyProvider;

    @Mock
    ListPartitioningStrategy listPartitioningStrategy;

    @Test
    void whenReactorExecutorPropertyProviderIsNull_ThrowException() {

        final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                ConcurrentCallReactorExecutor.builder().scheduler(this.scheduler).listPartitioningStrategy(this.listPartitioningStrategy);

        assertThrows(IllegalArgumentException.class,
                () -> concurrentCallReactorExecutorBuilder.build());

    }

    @Test
    void whenListPartitioningStrategyIsNull_ThrowException() {

        final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                ConcurrentCallReactorExecutor.builder().scheduler(this.scheduler).reactorExecutorPropertyProvider(
                        this.reactorExecutorPropertyProvider);

        assertThrows(IllegalArgumentException.class,
                () -> concurrentCallReactorExecutorBuilder.build());

    }

    @Test
    void whenSchedulerIsNull_ThrowException() {

        final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(this.listPartitioningStrategy)
                        .reactorExecutorPropertyProvider(this.reactorExecutorPropertyProvider);

        assertThrows(IllegalArgumentException.class,
                () -> concurrentCallReactorExecutorBuilder.build());

    }

    @ParameterizedTest
    @MethodSource("provideFlatMapConcurrency")
    void whenConcurrencyIsNotHigherThanZero_ThrowException(final Integer flatMapConcurrency) {

        final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(this.listPartitioningStrategy)
                        .reactorExecutorPropertyProvider(this.reactorExecutorPropertyProvider).scheduler(this.scheduler);
        when(this.reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(flatMapConcurrency);

        assertThrows(IllegalArgumentException.class,
                () -> concurrentCallReactorExecutorBuilder.build());

    }

    @ParameterizedTest
    @MethodSource("provideBlockTimeout")
    void whenBlockTimeoutIsNotHigherThanZero_ThrowException(final Long blockTimeout) {

        final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(this.listPartitioningStrategy)
                        .reactorExecutorPropertyProvider(this.reactorExecutorPropertyProvider).scheduler(this.scheduler);
        when(this.reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
        when(this.reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(blockTimeout);

        assertThrows(IllegalArgumentException.class,
                () -> concurrentCallReactorExecutorBuilder.build());

    }

    @Test
    void whenRetriesIsNull_ThrowException() {

        final ConcurrentCallReactorExecutorBuilder concurrentCallReactorExecutorBuilder =
                ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(this.listPartitioningStrategy)
                        .reactorExecutorPropertyProvider(this.reactorExecutorPropertyProvider).scheduler(this.scheduler);

        when(this.reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
        when(this.reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(3000L);
        when(this.reactorExecutorPropertyProvider.getRetries()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> concurrentCallReactorExecutorBuilder
                        .build());

    }

    @Test
    void whenRetryIsZeroAndNoException_ResultIsOk() {

        final FunctionClassTest functionClassTestSpy = spy(new FunctionClassTest());
        final List<Integer> listToPartition1 = List.of(1, 2);
        final List<Integer> listToPartition2 = List.of(3, 4);
        final List<Integer> listToPartition = Stream.concat(listToPartition1.stream(), listToPartition2.stream())
                .collect(Collectors.toList());
        final List<List<Integer>> splitList = new ArrayList<>();
        splitList.add(listToPartition1);
        splitList.add(listToPartition2);

        when(this.reactorExecutorPropertyProvider.getRetries()).thenReturn(0);
        when(this.reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
        when(this.reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(3000L);

        when(this.listPartitioningStrategy.split(listToPartition)).thenReturn(splitList);

        final ConcurrentCallReactorExecutor concurrentCallReactorExecutor =
                ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(this.listPartitioningStrategy)
                        .reactorExecutorPropertyProvider(this.reactorExecutorPropertyProvider).scheduler(this.scheduler)
                        .build();

        final List<Integer> totalResult = concurrentCallReactorExecutor.execute(listToPartition, l -> functionClassTestSpy.run(l));

        assertNotNull(totalResult);
        assertEquals(listToPartition.size(), totalResult.size());

        Collections.sort(listToPartition);
        Collections.sort(totalResult);
        assertEquals(listToPartition, totalResult);

        verify(functionClassTestSpy, times(2)).run(anyList());
    }

    @Test
    void whenRetryIsTwoAndException_ThreeCalls() {

        final FunctionClassExceptionTest functionClassExceptionTestSpy = spy(new FunctionClassExceptionTest());
        final List<Integer> listToPartition = List.of(1, 2);
        final List<List<Integer>> splitList = new ArrayList<>();
        splitList.add(listToPartition);

        when(this.reactorExecutorPropertyProvider.getRetries()).thenReturn(2);
        when(this.reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
        when(this.reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(3000L);

        when(this.listPartitioningStrategy.split(listToPartition)).thenReturn(splitList);

        final ConcurrentCallReactorExecutor concurrentCallReactorExecutor =
                ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(this.listPartitioningStrategy)
                        .reactorExecutorPropertyProvider(this.reactorExecutorPropertyProvider).scheduler(this.scheduler)
                        .build();

        assertThrows(NumberFormatException.class,
                () -> concurrentCallReactorExecutor.execute(listToPartition, l -> functionClassExceptionTestSpy.run(l)));

        verify(functionClassExceptionTestSpy, times(3)).run(anyList());
    }

    @Test
    void whenBlockTimeoutIsReached_TimeoutExceptionThrown() {

        final FunctionClassSleepTest functionClassSleepTestSpy = spy(new FunctionClassSleepTest());
        final List<Integer> listToPartition = List.of(1, 2);
        final List<List<Integer>> splitList = new ArrayList<>();
        splitList.add(listToPartition);

        when(this.reactorExecutorPropertyProvider.getRetries()).thenReturn(0);
        when(this.reactorExecutorPropertyProvider.getFlatMapConcurrency()).thenReturn(2);
        when(this.reactorExecutorPropertyProvider.getBlockTimeout()).thenReturn(1000L);

        when(this.listPartitioningStrategy.split(listToPartition)).thenReturn(splitList);

        final ConcurrentCallReactorExecutor concurrentCallReactorExecutor =
                ConcurrentCallReactorExecutor.builder().listPartitioningStrategy(this.listPartitioningStrategy)
                        .reactorExecutorPropertyProvider(this.reactorExecutorPropertyProvider).scheduler(this.scheduler)
                        .build();

        final IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> concurrentCallReactorExecutor.execute(listToPartition, l -> functionClassSleepTestSpy.run(l)));

        assertTrue(illegalStateException.getMessage().contains("Timeout on blocking read"));
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
