package org.web.codefm.domain.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class FileNameUtilTest {

    @Nested
    class ExtractExtension {

        @ParameterizedTest
        @CsvSource({
                "document.pdf, pdf",
                "image.PNG, png",
                "archive.tar.gz, gz",
                "file.JPEG, jpeg",
                "photo.JpG, jpg"
        })
        void when_filename_has_extension_expect_lowercase_extension(final String filename,
                                                                    final String expectedExtension) {
            assertThat(FileNameUtil.extractExtension(filename)).isEqualTo(expectedExtension);
        }

        @ParameterizedTest
        @NullSource
        void when_filename_is_null_expect_empty_value(final String filename) {
            assertThat(FileNameUtil.extractExtension(filename)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"noextension", ".hiddenfile", ""})
        void when_filename_has_no_valid_extension_expect_empty_value(final String filename) {
            assertThat(FileNameUtil.extractExtension(filename)).isEmpty();
        }
    }

    @Nested
    class ExtractBaseName {

        @ParameterizedTest
        @CsvSource({
                "document.pdf, document",
                "my-file.txt, my-file",
                "archive.tar.gz, archive.tar",
                "file_name.jpg, file_name"
        })
        void when_filename_has_extension_expect_sanitized_base_name(final String filename,
                                                                    final String expectedBaseName) {
            assertThat(FileNameUtil.extractBaseName(filename)).isEqualTo(expectedBaseName);
        }

        @Test
        void when_filename_has_special_characters_expect_sanitized_base_name() {
            assertThat(FileNameUtil.extractBaseName("my file name.pdf")).isEqualTo("my_file_name");
        }

        @ParameterizedTest
        @NullSource
        void when_filename_is_null_expect_empty_value(final String filename) {
            assertThat(FileNameUtil.extractBaseName(filename)).isEmpty();
        }

        @Test
        void when_filename_has_no_extension_expect_sanitized_base_name() {
            assertThat(FileNameUtil.extractBaseName("no extension here")).isEqualTo("no_extension_here");
        }
    }
}
