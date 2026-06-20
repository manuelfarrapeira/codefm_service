package org.web.codefm.domain.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum defining session parameters that can be extracted from JWT claims.
 * Each parameter includes its claim name and expected Java type for automatic conversion.
 */
@Getter
@RequiredArgsConstructor
public enum SessionParameter {

    TEACHER_ID("teacher_id", Integer.class);

    private final String claimName;
    private final Class<?> type;
}
