package com.art.tutordesk.student.validation;

import com.art.tutordesk.student.StudentDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import com.art.tutordesk.payment.Currency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AtLeastOnePriceNotNullValidatorTest {

    private AtLeastOnePriceNotNullValidator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        validator = new AtLeastOnePriceNotNullValidator();
    }

    @Test
    void isValid_shouldReturnTrue_whenBothPricesAreNotNull() {
        StudentDto studentDto = new StudentDto(
                1L, "John", "Doe", "A2", 25,
                BigDecimal.TEN, BigDecimal.valueOf(20), Currency.USD, true, Map.of(Currency.USD, BigDecimal.valueOf(100))
        );
        assertTrue(validator.isValid(studentDto, constraintValidatorContext));
    }

    @Test
    void isValid_shouldReturnTrue_whenIndividualPriceIsNotNull() {
        StudentDto studentDto = new StudentDto(
                1L, "John", "Doe", "A2", 25,
                BigDecimal.TEN, null, Currency.USD, true, Map.of(Currency.USD, BigDecimal.valueOf(100))
        );
        assertTrue(validator.isValid(studentDto, constraintValidatorContext));
    }

    @Test
    void isValid_shouldReturnTrue_whenGroupPriceIsNotNull() {
        StudentDto studentDto = new StudentDto(
                1L, "John", "Doe", "A2", 25,
                null, BigDecimal.TEN, Currency.USD, true, Map.of(Currency.USD, BigDecimal.valueOf(100))
        );
        assertTrue(validator.isValid(studentDto, constraintValidatorContext));
    }

    @Test
    void isValid_shouldReturnFalse_whenBothPricesAreNull() {
        StudentDto studentDto = new StudentDto(
                1L, "John", "Doe", "A2", 25,
                null, null, Currency.USD, true, Map.of(Currency.USD, BigDecimal.valueOf(100))
        );
        assertFalse(validator.isValid(studentDto, constraintValidatorContext));
    }

    @Test
    void isValid_shouldReturnTrue_whenStudentDtoIsNull() {
        assertTrue(validator.isValid(null, constraintValidatorContext));
    }
}
