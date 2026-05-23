package org.web.codefm.api.mapper;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.StudentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.model.StudentRequestDTO;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudentRequestMapperTest {

    @Spy
    @InjectMocks
    private StudentRequestMapperImpl mapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SessionUser sessionUser;

    @BeforeEach
    void beforeEach() {
        when(this.sessionUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(this.messageSource.getMessage(eq(MessageKeys.STUDENT_VALIDATION_DATE_FORMAT_INVALID), eq(null), any(Locale.class)))
                .thenReturn("Date of birth must be in format dd/MM/yyyy.");
    }

    @Nested
    class ToDomain {

        @Test
        void when_date_is_valid_expect_mapped_student() {
            final StudentRequestDTO dto = new StudentRequestDTO();
            dto.setName("Juan");
            dto.setSurnames("García López");
            dto.setDateOfBirth("15/03/2010");
            dto.setAdditionalInfo("Test info");
            dto.setShape("CIRCLE");

            final Student result = StudentRequestMapperTest.this.mapper.toDomain(dto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(2010, 3, 15));
            assertThat(result.getAdditionalInfo()).isEqualTo("Test info");
            assertThat(result.getShape()).isEqualTo("CIRCLE");
            assertThat(result.getId()).isNull();
            assertThat(result.getPhoto()).isNull();
            assertThat(result.getDeletionDate()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void when_date_is_null_or_empty_expect_null_date(final String dateOfBirth) {
            final StudentRequestDTO dto = new StudentRequestDTO();
            dto.setName("Juan");
            dto.setSurnames("García López");
            dto.setDateOfBirth(dateOfBirth);

            final Student result = StudentRequestMapperTest.this.mapper.toDomain(dto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Juan");
            assertThat(result.getSurnames()).isEqualTo("García López");
            assertThat(result.getDateOfBirth()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"15-03-2010", "2010/03/15", "invalid-date", "32/03/2010", "15/13/2010"})
        void when_date_is_invalid_expect_validation_exception(final String invalidDate) {
            final StudentRequestDTO dto = new StudentRequestDTO();
            dto.setName("Juan");
            dto.setSurnames("García López");
            dto.setDateOfBirth(invalidDate);
            final ThrowingCallable action = () -> StudentRequestMapperTest.this.mapper.toDomain(dto);

            assertThatThrownBy(action)
                    .isInstanceOf(StudentValidationException.class)
                    .satisfies(throwable -> assertThat(((StudentValidationException) throwable).getErrors())
                            .isNotEmpty()
                            .first()
                            .satisfies(error -> assertThat(error.getParam()).isEqualTo("dateOfBirth")));
        }
    }
}
