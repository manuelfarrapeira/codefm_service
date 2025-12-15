package org.web.codefm.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceAccessClientTest {

    @Test
    void testCodefmClientId() {
        assertEquals("codefm", ResourceAccessClient.CODEFM.getClientId());
    }

    @Test
    void testTeacherNotebookClientId() {
        assertEquals("teacher_notebook", ResourceAccessClient.TEACHER_NOTEBOOK.getClientId());
    }

    @Test
    void testAllEnumValuesHaveClientIds() {
        assertEquals(2, ResourceAccessClient.values().length, "Expected 2 enum values");
    }
}
