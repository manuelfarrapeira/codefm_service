package org.web.codefm.domain.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MimeTypeEnumTest {

    @Nested
    class FindMimeType {

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
        void when_extension_is_valid_expect_mime_type(final String extension, final String expectedMimeType) {
            assertThat(MimeTypeEnum.findMimeType(extension)).isEqualTo(expectedMimeType);
        }

        @ParameterizedTest
        @CsvSource({"PDF", "Pdf", "DOCX", "Png"})
        void when_extension_has_different_case_expect_non_default_mime_type(final String extension) {
            assertThat(MimeTypeEnum.findMimeType(extension)).isNotEqualTo("application/octet-stream");
        }

        @ParameterizedTest
        @CsvSource({"exe", "bat", "sh", "zip", "rar", "mp4", "avi"})
        void when_extension_is_not_allowed_expect_octet_stream(final String extension) {
            assertThat(MimeTypeEnum.findMimeType(extension)).isEqualTo("application/octet-stream");
        }

        @ParameterizedTest
        @CsvSource({", application/octet-stream", "'', application/octet-stream"})
        void when_extension_is_null_or_empty_expect_octet_stream(final String extension,
                                                                 final String expectedMimeType) {
            assertThat(MimeTypeEnum.findMimeType(extension)).isEqualTo(expectedMimeType);
        }
    }

    @Nested
    class IsAllowedExtension {

        @ParameterizedTest
        @CsvSource({"pdf", "doc", "docx", "xls", "xlsx", "jpeg", "jpg", "png"})
        void when_extension_is_valid_expect_true(final String extension) {
            assertThat(MimeTypeEnum.isAllowedExtension(extension)).isTrue();
        }

        @ParameterizedTest
        @CsvSource({"PDF", "Pdf", "DOCX", "Png"})
        void when_extension_has_different_case_expect_true(final String extension) {
            assertThat(MimeTypeEnum.isAllowedExtension(extension)).isTrue();
        }

        @ParameterizedTest
        @CsvSource({"exe", "bat", "sh", "zip", "rar", "mp4", "avi"})
        void when_extension_is_not_allowed_expect_false(final String extension) {
            assertThat(MimeTypeEnum.isAllowedExtension(extension)).isFalse();
        }

        @ParameterizedTest
        @CsvSource({", false", "'', false"})
        void when_extension_is_null_or_empty_expect_false(final String extension, final boolean expected) {
            assertThat(MimeTypeEnum.isAllowedExtension(extension)).isEqualTo(expected);
        }
    }

    @Nested
    class Values {

        @Test
        void when_enum_values_are_requested_expect_eight_values() {
            assertThat(MimeTypeEnum.values()).hasSize(8);
        }
    }

    @Nested
    class GetExtension {

        @Test
        void when_extension_is_requested_expect_value() {
            assertThat(MimeTypeEnum.PDF.getExtension()).isEqualTo("pdf");
            assertThat(MimeTypeEnum.DOCX.getExtension()).isEqualTo("docx");
        }
    }

    @Nested
    class GetMimeType {

        @Test
        void when_mime_type_is_requested_expect_value() {
            assertThat(MimeTypeEnum.PDF.getMimeType()).isEqualTo("application/pdf");
            assertThat(MimeTypeEnum.JPEG.getMimeType()).isEqualTo("image/jpeg");
        }

        @Test
        void when_jpeg_and_jpg_are_compared_expect_same_mime_type() {
            assertThat(MimeTypeEnum.JPEG.getMimeType()).isEqualTo(MimeTypeEnum.JPG.getMimeType());
        }
    }
}
