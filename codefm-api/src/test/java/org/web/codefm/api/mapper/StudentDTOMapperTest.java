package org.web.codefm.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web.codefm.domain.entity.teachernotebook.Student;
import org.web.codefm.model.StudentDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentDTOMapperTest {

    private final StudentDTOMapper mapper = new StudentDTOMapperImpl();

    @Test
    void toDTO_shouldMapCorrectly_whenAllFieldsArePresent() {
        // Given
        Student student = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .additionalInfo("Test info")
                .photo("1.jpg")
                .shape("SQUARE")
                .build();

        // When
        StudentDTO result = mapper.toDTO(student);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Juan", result.getName());
        assertEquals("García López", result.getSurnames());
        assertEquals("15/03/2010", result.getDateOfBirth());
        assertEquals("Test info", result.getAdditionalInfo());
        assertEquals("1.jpg", result.getPhoto());
        assertEquals("SQUARE", result.getShape());
    }

    @Test
    void toDTO_shouldMapCorrectly_whenDateIsNull() {
        // Given
        Student student = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(null)
                .build();

        // When
        StudentDTO result = mapper.toDTO(student);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Juan", result.getName());
        assertEquals("García López", result.getSurnames());
        assertNull(result.getDateOfBirth());
    }

    @Test
    void toDTO_shouldFormatDateCorrectly_withSingleDigitDay() {
        // Given
        Student student = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 5)) // Day 5
                .build();

        // When
        StudentDTO result = mapper.toDTO(student);

        // Then
        assertNotNull(result);
        assertEquals("05/03/2010", result.getDateOfBirth());
    }

    @Test
    void toDTO_shouldFormatDateCorrectly_withSingleDigitMonth() {
        // Given
        Student student = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 1, 15)) // Month 1
                .build();

        // When
        StudentDTO result = mapper.toDTO(student);

        // Then
        assertNotNull(result);
        assertEquals("15/01/2010", result.getDateOfBirth());
    }

    @Test
    void toDTO_shouldMapCorrectly_whenOptionalFieldsAreNull() {
        // Given
        Student student = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .additionalInfo(null)
                .photo(null)
                .build();

        // When
        StudentDTO result = mapper.toDTO(student);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Juan", result.getName());
        assertEquals("García López", result.getSurnames());
        assertNull(result.getAdditionalInfo());
        assertNull(result.getPhoto());
    }

    @Test
    void toDTOList_shouldMapListCorrectly() {
        // Given
        Student student1 = Student.builder()
                .id(1)
                .name("Juan")
                .surnames("García López")
                .dateOfBirth(LocalDate.of(2010, 3, 15))
                .build();

        Student student2 = Student.builder()
                .id(2)
                .name("María")
                .surnames("Pérez Sánchez")
                .dateOfBirth(LocalDate.of(2011, 5, 20))
                .build();

        List<Student> students = Arrays.asList(student1, student2);

        // When
        List<StudentDTO> result = mapper.toDTOList(students);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        StudentDTO dto1 = result.get(0);
        assertEquals(1, dto1.getId());
        assertEquals("Juan", dto1.getName());
        assertEquals("15/03/2010", dto1.getDateOfBirth());

        StudentDTO dto2 = result.get(1);
        assertEquals(2, dto2.getId());
        assertEquals("María", dto2.getName());
        assertEquals("20/05/2011", dto2.getDateOfBirth());
    }

    @Test
    void toDTOList_shouldReturnEmptyList_whenInputIsEmpty() {
        // Given
        List<Student> students = Arrays.asList();

        // When
        List<StudentDTO> result = mapper.toDTOList(students);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

