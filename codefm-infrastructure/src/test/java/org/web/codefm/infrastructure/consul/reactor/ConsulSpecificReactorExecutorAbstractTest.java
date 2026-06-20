package org.web.codefm.infrastructure.consul.reactor;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsulSpecificReactorExecutorAbstractTest {

    static class DummyExecutor extends ConsulSpecificReactorExecutorAbstract {
        DummyExecutor(Integer partitionLimit, Integer retries, Integer flatMapConcurrency, Long blockTimeout, ConsulSpecificReactorExecutorAbstract def) {
            super(partitionLimit, retries, flatMapConcurrency, blockTimeout, def);
        }

        DummyExecutor(Integer partitionLimit, Integer retries, Integer flatMapConcurrency, Long blockTimeout) {
            super(partitionLimit, retries, flatMapConcurrency, blockTimeout);
        }
    }

    @Nested
    class GetProperties {

        @Test
        void when_all_properties_set_expect_own_values_returned() {
            final DummyExecutor exec = new DummyExecutor(10, 2, 5, 1000L);

            assertThat(exec.getPartitionLimit()).isEqualTo(10);
            assertThat(exec.getRetries()).isEqualTo(2);
            assertThat(exec.getFlatMapConcurrency()).isEqualTo(5);
            assertThat(exec.getBlockTimeout()).isEqualTo(1000L);
        }

        @Test
        void when_own_value_is_null_expect_default_returned() {
            final DummyExecutor def = new DummyExecutor(20, 3, 7, 2000L);
            final DummyExecutor exec = new DummyExecutor(null, null, null, null, def);

            assertThat(exec.getPartitionLimit()).isEqualTo(20);
            assertThat(exec.getRetries()).isEqualTo(3);
            assertThat(exec.getFlatMapConcurrency()).isEqualTo(7);
            assertThat(exec.getBlockTimeout()).isEqualTo(2000L);
        }

        @Test
        void when_both_null_expect_null_returned() {
            final DummyExecutor exec = new DummyExecutor(null, null, null, null, null);

            assertThat(exec.getPartitionLimit()).isNull();
            assertThat(exec.getRetries()).isNull();
            assertThat(exec.getFlatMapConcurrency()).isNull();
            assertThat(exec.getBlockTimeout()).isNull();
        }

        @Test
        void when_own_value_set_expect_own_value_returned_despite_default() {
            final DummyExecutor def = new DummyExecutor(30, 4, 8, 3000L);
            final DummyExecutor exec = new DummyExecutor(15, 5, 9, 1500L, def);

            assertThat(exec.getPartitionLimit()).isEqualTo(15);
            assertThat(exec.getRetries()).isEqualTo(5);
            assertThat(exec.getFlatMapConcurrency()).isEqualTo(9);
            assertThat(exec.getBlockTimeout()).isEqualTo(1500L);
        }
    }
}
