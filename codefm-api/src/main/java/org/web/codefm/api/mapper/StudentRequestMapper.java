package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.web.codefm.domain.entity.exception.ErrorMessage;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.domain.exception.teachernotebook.StudentValidationException;
import org.web.codefm.domain.i18n.MessageKeys;
import org.web.codefm.domain.session.SessionUser;
import org.web.codefm.model.StudentRequestDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class StudentRequestMapper {

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    protected SessionUser sessionUser;

    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photo", ignore = true)
    @Mapping(target = "deletionDate", ignore = true)
    @Mapping(target = "dateOfBirth", expression = "java(parseDate(dto.getDateOfBirth()))")
    public abstract Student toDomain(StudentRequestDTO dto);

    protected LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            List<ErrorMessage> errors = new ArrayList<>();
            String message = messageSource.getMessage(
                    MessageKeys.STUDENT_VALIDATION_DATE_FORMAT_INVALID,
                    null,
                    sessionUser.getLocale()
            );
            errors.add(new ErrorMessage("dateOfBirth", message));
            throw new StudentValidationException(errors);
        }
    }
}
