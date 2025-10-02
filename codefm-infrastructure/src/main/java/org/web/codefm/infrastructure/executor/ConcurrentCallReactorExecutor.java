package org.web.codefm.infrastructure.executor;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class ConcurrentCallReactorExecutor implements ConcurrentCallExecutor {

    private final Scheduler scheduler;

    private final ReactorExecutorPropertyProvider reactorExecutorPropertyProvider;

    private final ListPartitioningStrategy listPartitioningStrategy;

    @Builder
    public ConcurrentCallReactorExecutor(final ReactorExecutorPropertyProvider reactorExecutorPropertyProvider,
                                         final ListPartitioningStrategy listPartitioningStrategy, final Scheduler scheduler) {

        Assert.notNull(reactorExecutorPropertyProvider, "reactorExecutorPropertyProvider must not be null");
        Assert.notNull(listPartitioningStrategy, "listPartitioningStrategy must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");

        Assert.isTrue(reactorExecutorPropertyProvider.getFlatMapConcurrency() > 0, "concurrency must be higher than zero");
        Assert.isTrue(reactorExecutorPropertyProvider.getBlockTimeout() > 0L, "timeout must be higher than zero");
        Assert.notNull(reactorExecutorPropertyProvider.getRetries(), "retries must not be null");

        this.reactorExecutorPropertyProvider = reactorExecutorPropertyProvider;
        this.listPartitioningStrategy = listPartitioningStrategy;
        this.scheduler = scheduler;
    }

    /**
     * Executes a function call concurrently on partitions of the provided list and combines the results.
     *
     * @param listToPartition    the list to be partitioned and processed
     * @param functionCallAndMap the function to be applied to each partition, returning a list of results
     * @param <T>                the type of elements in the input list
     * @param <R>                the type of elements in the result list
     * @return a combined list of results from all partitions
     */
    @Override
    public <T, R> List<R> execute(final List<T> listToPartition, final Function<List<T>, List<R>> functionCallAndMap) {

        final String callContextId = this.getCallContext();

        log.info("callContextId: {}, retries: {}, flatMapConcurrency: {}, blockTimeout: {}", callContextId,
                this.reactorExecutorPropertyProvider.getRetries(),
                this.reactorExecutorPropertyProvider.getFlatMapConcurrency(),
                this.reactorExecutorPropertyProvider.getBlockTimeout());

        final List<R> combinedResult =
                Flux.fromIterable(this.listPartitioningStrategy.split(listToPartition)).publishOn(this.scheduler).flatMap(partialList -> {

                            final Mono<List<R>> innerResult = Mono.fromCallable(() -> {
                                List<R> partialResult = null;
                                try {
                                    partialResult = functionCallAndMap.apply(partialList);
                                    log.info("Partial call ended, callContextId: {}, partial result list size: {}", callContextId,
                                            Optional.ofNullable(partialResult).map(List::size).orElse(null));
                                } catch (final Throwable throwable) {
                                    log.error(String.format("Unexpected exception in partial call, callContextId: %s", callContextId), throwable);
                                    throw throwable;
                                }
                                return partialResult;

                            }).retry(this.reactorExecutorPropertyProvider.getRetries());

                            return innerResult.subscribeOn(this.scheduler).flatMapIterable(Function.identity());

                        }, this.reactorExecutorPropertyProvider.getFlatMapConcurrency()).collectList()
                        .block(Duration.ofMillis(this.reactorExecutorPropertyProvider.getBlockTimeout()));

        log.info("Combined result, callContextId: {}, size: {}", callContextId,
                Optional.ofNullable(combinedResult).map(List::size).orElse(null));

        return combinedResult;
    }

    private String getCallContext() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[3];
        String className = element.getClassName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        return simpleClassName + "." + element.getMethodName();
    }


}
