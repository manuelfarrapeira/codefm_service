package org.web.codefm.domain.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class for file name operations.
 * Provides methods to extract file extensions and base names.
 */
@UtilityClass
public class FileNameUtil {

    private static final String SANITIZE_PATTERN = "[^a-zA-Z0-9._-]";
    private static final String SANITIZE_REPLACEMENT = "_";

    /**
     * Extracts the file extension from a filename in lowercase.
     *
     * @param filename The original filename
     * @return The file extension in lowercase, or empty string if none found or filename is null
     */
    public static String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        final int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Extracts the base name from a filename, sanitizing non-alphanumeric characters.
     *
     * @param filename The original filename
     * @return The sanitized base name, or empty string if filename is null
     */
    public static String extractBaseName(String filename) {
        if (filename == null) {
            return "";
        }
        final int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex).replaceAll(SANITIZE_PATTERN, SANITIZE_REPLACEMENT);
        }
        return filename.replaceAll(SANITIZE_PATTERN, SANITIZE_REPLACEMENT);
    }
}

