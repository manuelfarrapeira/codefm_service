package org.web.codefm.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.exception.teachernotebook.StudentAbsenceValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentAbsenceRequestMapperTest {

	@InjectMocks
	private StudentAbsenceRequestMapper mapper;

	@Mock
	private MessageSource messageSource;

	@Mock
	private SessionUser sessionUser;

	@BeforeEach
	void setUp() {
		when(sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
		when(messageSource.getMessage(eq(MessageKeys.ABSENCE_VALIDATION_DATE_REQUIRED), eq(null), any(Locale.class)))
				.thenReturn("Date is required.");
		when(messageSource.getMessage(eq(MessageKeys.ABSENCE_VALIDATION_DATE_INVALID), eq(null), any(Locale.class)))
				.thenReturn("Invalid date format.");
	}

	@Test
	void parseDate_shouldReturnLocalDate_whenDateIsValid() {
		LocalDate result = mapper.parseDate("15/03/2026");

		assertEquals(LocalDate.of(2026, 3, 15), result);
	}

	@Test
	void parseDate_shouldParseFirstDayOfYear() {
		LocalDate result = mapper.parseDate("01/01/2025");

		assertEquals(LocalDate.of(2025, 1, 1), result);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"   "})
	void parseDate_shouldThrowValidationException_whenDateIsNullOrBlank(String dateString) {
		StudentAbsenceValidationException exception = assertThrows(StudentAbsenceValidationException.class,
				() -> mapper.parseDate(dateString));

		assertFalse(exception.getErrors().isEmpty());
		assertEquals("date", exception.getErrors().get(0).getParam());
	}

	@ParameterizedTest
	@ValueSource(strings = {"2026-03-15", "32/03/2026", "15/13/2026", "not-a-date", "15-03-2026"})
	void parseDate_shouldThrowValidationException_whenDateFormatIsInvalid(String invalidDate) {
		StudentAbsenceValidationException exception = assertThrows(StudentAbsenceValidationException.class,
				() -> mapper.parseDate(invalidDate));

		assertFalse(exception.getErrors().isEmpty());
		assertEquals("date", exception.getErrors().get(0).getParam());
	}
}
