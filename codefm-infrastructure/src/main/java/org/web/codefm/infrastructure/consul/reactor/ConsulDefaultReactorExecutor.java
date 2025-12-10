package org.web.codefm.infrastructure.consul.reactor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class ConsulDefaultReactorExecutor extends ConsulSpecificReactorExecutorAbstract {

    public ConsulDefaultReactorExecutor(@Value("${reactor.executor.default.partition-limit:}") final Integer partitionLimit,
                                        @Value("${reactor.executor.default.retry:}") final Integer retries,
                                        @Value("${reactor.executor.default.concurrency:}") final Integer flatMapConcurrency,
                                        @Value("${reactor.executor.default.timeout:}") final Long blockTimeout) {
        super(partitionLimit, retries, flatMapConcurrency, blockTimeout);
    }
}
