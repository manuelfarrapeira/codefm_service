package org.web.codefm.infrastructure.examples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.infrastructure.consul.reactor.ConsulReactorExecutorExample;
import org.web.codefm.infrastructure.executor.ConcurrentCallExecutor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactorExecutorExampleRepositoryImplTest {

    private ReactorExecutorExampleRepositoryImpl reactorExecutorExampleRepository;

    @Mock
    private ConcurrentCallExecutor concurrentCallReactorExecutor;

    @Mock
    private ConsulReactorExecutorExample consulReactorExecutorExample;

    @BeforeEach
    void beforeEach() {
        when(consulReactorExecutorExample.getPartitionLimit()).thenReturn(10);
        when(consulReactorExecutorExample.getBlockTimeout()).thenReturn(Long.valueOf("100000"));
        when(consulReactorExecutorExample.getFlatMapConcurrency()).thenReturn(10);
        when(consulReactorExecutorExample.getRetries()).thenReturn(10);
        this.reactorExecutorExampleRepository = new ReactorExecutorExampleRepositoryImpl(this.consulReactorExecutorExample);
    }

    @Nested
    class GetResult {

        @Test
        void when_integer_list_provided_expect_string_list_returned() {

            final List<Integer> ids = IntStream.rangeClosed(1, 20)
                    .boxed()
                    .collect(Collectors.toList());

            final List<String> result = reactorExecutorExampleRepository.getResult(ids);

            assertThat(result).hasSize(20);
        }
    }
}
