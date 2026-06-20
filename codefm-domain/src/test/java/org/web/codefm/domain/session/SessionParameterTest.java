package org.web.codefm.domain.session;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionParameterTest {

    @Nested
    class GetClaimName {

        @Test
        void when_teacher_id_is_requested_expect_claim_name() {
            assertThat(SessionParameter.TEACHER_ID.getClaimName()).isEqualTo("teacher_id");
        }
    }

    @Nested
    class GetType {

        @Test
        void when_teacher_id_is_requested_expect_integer_type() {
            assertThat(SessionParameter.TEACHER_ID.getType()).isEqualTo(Integer.class);
        }
    }

    @Nested
    class Values {

        @Test
        void when_enum_values_are_requested_expect_single_value() {
            assertThat(SessionParameter.values()).hasSize(1);
        }
    }
}
