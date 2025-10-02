package org.web.codefm.infrastructure.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.infrastructure.consul.reactor.ConsulReactorExecutorExample;
import org.web.codefm.infrastructure.executor.ConcurrentCallExecutor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactorExecutorExampleRepositoryImplTest {

    @Mock
    private ConcurrentCallExecutor concurrentCallReactorExecutor;

    @Mock
    private ConsulReactorExecutorExample consulReactorExecutorExample;


    @Test
    @DisplayName("should return a list of strings when a list of integers is provided")
    void shouldReturnListOfStringsWhenListOfIntegersIsProvided() {

        when(consulReactorExecutorExample.getPartitionLimit()).thenReturn(10);
        when(consulReactorExecutorExample.getBlockTimeout()).thenReturn(Long.valueOf("100000"));
        when(consulReactorExecutorExample.getFlatMapConcurrency()).thenReturn(10);
        when(consulReactorExecutorExample.getRetries()).thenReturn(10);

        ReactorExecutorExampleRepositoryImpl reactorExecutorExampleRepository = new ReactorExecutorExampleRepositoryImpl(consulReactorExecutorExample);

        final List<Integer> ids = IntStream.rangeClosed(1, 20)
                .boxed()
                .collect(Collectors.toList());

        final List<String> result = reactorExecutorExampleRepository.getResult(ids);

        assertEquals(20, result.size());
    }
}
