package org.web.codefm.infrastructure.examples;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web.codefm.domain.repository.ReactorExecutorExampleRepository;
import org.web.codefm.infrastructure.consul.reactor.ConsulReactorExecutorExample;
import org.web.codefm.infrastructure.executor.ChunkLimitListPartitioningStrategy;
import org.web.codefm.infrastructure.executor.ConcurrentCallExecutor;
import org.web.codefm.infrastructure.executor.ConcurrentCallReactorExecutor;
import org.web.codefm.infrastructure.executor.ListPartitioningStrategy;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Repository
public class ReactorExecutorExampleRepositoryImpl implements ReactorExecutorExampleRepository {

    private final ConsulReactorExecutorExample consulReactorExecutorExample;

    private final ListPartitioningStrategy chunkLimitListPartitioningStrategy;

    private final ConcurrentCallExecutor concurrentCallReactorExecutor;

    public ReactorExecutorExampleRepositoryImpl(final ConsulReactorExecutorExample consulReactorExecutorExample) {

        this.consulReactorExecutorExample = consulReactorExecutorExample;

        this.chunkLimitListPartitioningStrategy = ChunkLimitListPartitioningStrategy.builder().listPartitioningPropertyProvider(this.consulReactorExecutorExample).build();

        this.concurrentCallReactorExecutor = ConcurrentCallReactorExecutor.builder().reactorExecutorPropertyProvider(this.consulReactorExecutorExample)
                .listPartitioningStrategy(this.chunkLimitListPartitioningStrategy)
                .scheduler(Schedulers.boundedElastic())
                .build();

    }

    @Override
    public List<String> getResult(final List<Integer> ids) {
        return this.concurrentCallReactorExecutor.execute(ids, this::transformToString);
    }


    private List<String> transformToString(final List<Integer> ids) {

        log.info("Procesed chunk of size: {}", ids.size());

        return ids.stream().map(String::valueOf).toList();
    }

}
