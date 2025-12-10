package org.web.codefm.infrastructure.consul.reactor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConsulSpecificReactorExecutorAbstractTest {

    static class DummyExecutor extends ConsulSpecificReactorExecutorAbstract {
        DummyExecutor(Integer partitionLimit, Integer retries, Integer flatMapConcurrency, Long blockTimeout, ConsulSpecificReactorExecutorAbstract def) {
            super(partitionLimit, retries, flatMapConcurrency, blockTimeout, def);
        }

        DummyExecutor(Integer partitionLimit, Integer retries, Integer flatMapConcurrency, Long blockTimeout) {
            super(partitionLimit, retries, flatMapConcurrency, blockTimeout);
        }
    }

    @Test
    @DisplayName("Returns own values when all properties are set")
    void returnsOwnValuesWhenPropertiesSet() {
        DummyExecutor exec = new DummyExecutor(10, 2, 5, 1000L);
        assertEquals(10, exec.getPartitionLimit());
        assertEquals(2, exec.getRetries());
        assertEquals(5, exec.getFlatMapConcurrency());
        assertEquals(1000L, exec.getBlockTimeout());
    }

    @Test
    @DisplayName("Falls back to default when own value is null")
    void fallsBackToDefaultWhenOwnValueIsNull() {
        DummyExecutor def = new DummyExecutor(20, 3, 7, 2000L);
        DummyExecutor exec = new DummyExecutor(null, null, null, null, def);
        assertEquals(20, exec.getPartitionLimit());
        assertEquals(3, exec.getRetries());
        assertEquals(7, exec.getFlatMapConcurrency());
        assertEquals(2000L, exec.getBlockTimeout());
    }

    @Test
    @DisplayName("Returns null when value and default are null")
    void returnsNullWhenValueAndDefaultAreNull() {
        DummyExecutor exec = new DummyExecutor(null, null, null, null, null);
        assertNull(exec.getPartitionLimit());
        assertNull(exec.getRetries());
        assertNull(exec.getFlatMapConcurrency());
        assertNull(exec.getBlockTimeout());
    }

    @Test
    @DisplayName("Returns own value even if default is present")
    void returnsOwnValueEvenIfDefaultPresent() {
        DummyExecutor def = new DummyExecutor(30, 4, 8, 3000L);
        DummyExecutor exec = new DummyExecutor(15, 5, 9, 1500L, def);
        assertEquals(15, exec.getPartitionLimit());
        assertEquals(5, exec.getRetries());
        assertEquals(9, exec.getFlatMapConcurrency());
        assertEquals(1500L, exec.getBlockTimeout());
    }
}
