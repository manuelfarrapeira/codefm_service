package org.web.codefm.domain.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum defining session parameters that can be extracted from JWT claims.
 */
@Getter
@RequiredArgsConstructor
public enum SessionParameter {

    TEACHER_ID("teacher_id");

    private final String claimName;
}
