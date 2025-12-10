
package org.web.codefm.infrastructure.consul.reactor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
public class ConsulReactorExecutorExample extends ConsulSpecificReactorExecutorAbstract {


    protected ConsulReactorExecutorExample(@Value("${reactor.executor.example.partition-limit:}") final Integer partitionLimit,
                                           @Value("${reactor.executor.example.retry:}") final Integer retries,
                                           @Value("${reactor.executor.example.concurrency:}") final Integer flatMapConcurrency,
                                           @Value("${reactor.executor.example.timeout:}") final Long blockTimeout,
                                           final ConsulDefaultReactorExecutor consulDefaultReactorExecutor) {
        super(partitionLimit, retries, flatMapConcurrency, blockTimeout, consulDefaultReactorExecutor);
    }
}
