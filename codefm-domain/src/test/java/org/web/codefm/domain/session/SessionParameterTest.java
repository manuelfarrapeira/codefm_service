package org.web.codefm.domain.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionParameterTest {

    @Test
    void testTeacherIdClaimName() {
        assertEquals("teacher_id", SessionParameter.TEACHER_ID.getClaimName());
    }

    @Test
    void testAllEnumValues() {
        assertEquals(1, SessionParameter.values().length, "Expected 1 enum value");
    }
}
