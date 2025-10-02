package org.web.codefm.infrastructure.consul.reactor;

import org.web.codefm.infrastructure.executor.ListPartitioningPropertyProvider;
import org.web.codefm.infrastructure.executor.ReactorExecutorPropertyProvider;

import java.util.Optional;
import java.util.function.Function;

public abstract class ConsulSpecificReactorExecutorAbstract implements ReactorExecutorPropertyProvider,
        ListPartitioningPropertyProvider {

    protected final Integer partitionLimit;

    protected final Integer retries;

    protected final Integer flatMapConcurrency;

    protected final Long blockTimeout;

    protected final Optional<ConsulSpecificReactorExecutorAbstract> opConfigToolSpecificReactorExecutorAbstractDefault;

    protected ConsulSpecificReactorExecutorAbstract(final Integer partitionLimit, final Integer retries, final Integer flatMapConcurrency,
                                                    final Long blockTimeout, final ConsulSpecificReactorExecutorAbstract configToolSpecificReactorExecutorAbstractDefault) {
        this.partitionLimit = partitionLimit;
        this.retries = retries;
        this.flatMapConcurrency = flatMapConcurrency;
        this.blockTimeout = blockTimeout;
        this.opConfigToolSpecificReactorExecutorAbstractDefault = Optional.ofNullable(configToolSpecificReactorExecutorAbstractDefault);
    }

    protected ConsulSpecificReactorExecutorAbstract(final Integer partitionLimit, final Integer retries, final Integer flatMapConcurrency,
                                                    final Long blockTimeout) {
        this.partitionLimit = partitionLimit;
        this.retries = retries;
        this.flatMapConcurrency = flatMapConcurrency;
        this.blockTimeout = blockTimeout;
        this.opConfigToolSpecificReactorExecutorAbstractDefault = Optional.empty();
    }

    @Override
    public Integer getFlatMapConcurrency() {
        return this.getValue(this.flatMapConcurrency, op -> op.get().getFlatMapConcurrency());
    }

    @Override
    public Integer getRetries() {
        return this.getValue(this.retries, op -> op.get().getRetries());

    }

    @Override
    public Long getBlockTimeout() {
        return this.getValue(this.blockTimeout, op -> op.get().getBlockTimeout());

    }

    @Override
    public Integer getPartitionLimit() {
        return this.getValue(this.partitionLimit, op -> op.get().getPartitionLimit());
    }

    private <T> T getValue(final T value, final Function<Optional<ConsulSpecificReactorExecutorAbstract>, T> defaultFunction) {
        if (value == null) {
            return this.opConfigToolSpecificReactorExecutorAbstractDefault.isPresent()
                    ? defaultFunction.apply(this.opConfigToolSpecificReactorExecutorAbstractDefault)
                    : null;
        } else {
            return value;
        }
    }
}
