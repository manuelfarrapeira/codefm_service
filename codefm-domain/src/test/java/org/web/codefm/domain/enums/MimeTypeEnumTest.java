package org.web.codefm.domain.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MimeTypeEnumTest {

    @ParameterizedTest
    @CsvSource({
            "pdf, application/pdf",
            "doc, application/msword",
            "docx, application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xls, application/vnd.ms-excel",
            "xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "jpeg, image/jpeg",
            "jpg, image/jpeg",
            "png, image/png"
    })
    void findMimeType_shouldReturnCorrectMimeType_whenExtensionIsValid(String extension, String expectedMimeType) {
        assertEquals(expectedMimeType, MimeTypeEnum.findMimeType(extension));
    }

    @ParameterizedTest
    @CsvSource({"PDF", "Pdf", "DOCX", "Png"})
    void findMimeType_shouldReturnCorrectMimeType_whenExtensionHasDifferentCase(String extension) {
        assertNotEquals("application/octet-stream", MimeTypeEnum.findMimeType(extension));
    }

    @ParameterizedTest
    @CsvSource({"exe", "bat", "sh", "zip", "rar", "mp4", "avi"})
    void findMimeType_shouldReturnOctetStream_whenExtensionIsNotAllowed(String extension) {
        assertEquals("application/octet-stream", MimeTypeEnum.findMimeType(extension));
    }

    @Test
    void findMimeType_shouldReturnOctetStream_whenExtensionIsEmpty() {
        assertEquals("application/octet-stream", MimeTypeEnum.findMimeType(""));
    }

    @Test
    void findMimeType_shouldReturnOctetStream_whenExtensionIsNull() {
        assertEquals("application/octet-stream", MimeTypeEnum.findMimeType(null));
    }

    @ParameterizedTest
    @CsvSource({"pdf", "doc", "docx", "xls", "xlsx", "jpeg", "jpg", "png"})
    void isAllowedExtension_shouldReturnTrue_whenExtensionIsValid(String extension) {
        assertTrue(MimeTypeEnum.isAllowedExtension(extension));
    }

    @ParameterizedTest
    @CsvSource({"PDF", "Pdf", "DOCX", "Png"})
    void isAllowedExtension_shouldReturnTrue_whenExtensionHasDifferentCase(String extension) {
        assertTrue(MimeTypeEnum.isAllowedExtension(extension));
    }

    @ParameterizedTest
    @CsvSource({"exe", "bat", "sh", "zip", "rar", "mp4", "avi"})
    void isAllowedExtension_shouldReturnFalse_whenExtensionIsNotAllowed(String extension) {
        assertFalse(MimeTypeEnum.isAllowedExtension(extension));
    }

    @Test
    void isAllowedExtension_shouldReturnFalse_whenExtensionIsEmpty() {
        assertFalse(MimeTypeEnum.isAllowedExtension(""));
    }

    @Test
    void isAllowedExtension_shouldReturnFalse_whenExtensionIsNull() {
        assertFalse(MimeTypeEnum.isAllowedExtension(null));
    }

    @Test
    void enumValues_shouldHaveCorrectCount() {
        assertEquals(8, MimeTypeEnum.values().length);
    }

    @Test
    void getExtension_shouldReturnCorrectExtension() {
        assertEquals("pdf", MimeTypeEnum.PDF.getExtension());
        assertEquals("docx", MimeTypeEnum.DOCX.getExtension());
    }

    @Test
    void getMimeType_shouldReturnCorrectMimeType() {
        assertEquals("application/pdf", MimeTypeEnum.PDF.getMimeType());
        assertEquals("image/jpeg", MimeTypeEnum.JPEG.getMimeType());
    }

    @Test
    void jpegAndJpg_shouldHaveSameMimeType() {
        assertEquals(MimeTypeEnum.JPEG.getMimeType(), MimeTypeEnum.JPG.getMimeType());
    }
}

