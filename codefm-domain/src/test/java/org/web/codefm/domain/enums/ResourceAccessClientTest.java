package org.web.codefm.domain.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceAccessClientTest {

    @Nested
    class GetClientId {

        @Test
        void when_codefm_is_requested_expect_client_id() {
            assertThat(ResourceAccessClient.CODEFM.getClientId()).isEqualTo("codefm");
        }

        @Test
        void when_teacher_notebook_is_requested_expect_client_id() {
            assertThat(ResourceAccessClient.TEACHER_NOTEBOOK.getClientId()).isEqualTo("teacher_notebook");
        }
    }

    @Nested
    class Values {

        @Test
        void when_enum_values_are_requested_expect_two_values() {
            assertThat(ResourceAccessClient.values()).hasSize(2);
        }
    }
}
