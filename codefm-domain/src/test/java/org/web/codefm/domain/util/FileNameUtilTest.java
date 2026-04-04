package org.web.codefm.domain.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileNameUtilTest {

    @ParameterizedTest
    @CsvSource({
            "document.pdf, pdf",
            "image.PNG, png",
            "archive.tar.gz, gz",
            "file.JPEG, jpeg",
            "photo.JpG, jpg"
    })
    void extractExtension_shouldReturnLowercaseExtension(String filename, String expectedExtension) {
        assertEquals(expectedExtension, FileNameUtil.extractExtension(filename));
    }

    @ParameterizedTest
    @NullSource
    void extractExtension_shouldReturnEmpty_whenFilenameIsNull(String filename) {
        assertEquals("", FileNameUtil.extractExtension(filename));
    }

    @ParameterizedTest
    @ValueSource(strings = {"noextension", ".hiddenfile", ""})
    void extractExtension_shouldReturnEmpty_whenNoValidExtension(String filename) {
        assertEquals("", FileNameUtil.extractExtension(filename));
    }

    @ParameterizedTest
    @CsvSource({
            "document.pdf, document",
            "my-file.txt, my-file",
            "archive.tar.gz, archive.tar",
            "file_name.jpg, file_name"
    })
    void extractBaseName_shouldReturnSanitizedBaseName(String filename, String expectedBaseName) {
        assertEquals(expectedBaseName, FileNameUtil.extractBaseName(filename));
    }

    @Test
    void extractBaseName_shouldSanitizeSpecialCharacters() {
        assertEquals("my_file_name", FileNameUtil.extractBaseName("my file name.pdf"));
    }

    @ParameterizedTest
    @NullSource
    void extractBaseName_shouldReturnEmpty_whenFilenameIsNull(String filename) {
        assertEquals("", FileNameUtil.extractBaseName(filename));
    }

    @Test
    void extractBaseName_shouldSanitizeFilenameWithoutExtension() {
        assertEquals("no_extension_here", FileNameUtil.extractBaseName("no extension here"));
    }
}

