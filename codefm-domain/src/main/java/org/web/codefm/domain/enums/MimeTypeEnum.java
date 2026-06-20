package org.web.codefm.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MimeTypeEnum {

    PDF("pdf", "application/pdf"),
    DOC("doc", "application/msword"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    JPEG("jpeg", "image/jpeg"),
    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png");

    private final String extension;
    private final String mimeType;

    public static String findMimeType(String extension) {
        for (MimeTypeEnum value : values()) {
            if (value.getExtension().equalsIgnoreCase(extension)) {
                return value.getMimeType();
            }
        }
        return "application/octet-stream";
    }

    public static boolean isAllowedExtension(String extension) {
        for (MimeTypeEnum value : values()) {
            if (value.getExtension().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}

