package org.web.codefm.infrastructure.executor;

public interface ReactorExecutorPropertyProvider {

    Integer getFlatMapConcurrency();

    Integer getRetries();

    Long getBlockTimeout();
}
