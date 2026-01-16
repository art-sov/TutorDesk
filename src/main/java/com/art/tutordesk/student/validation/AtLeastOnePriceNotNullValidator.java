package com.art.tutordesk.student.validation;

import com.art.tutordesk.student.StudentDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOnePriceNotNullValidator implements ConstraintValidator<AtLeastOnePriceNotNull, StudentDto> {

    @Override
    public void initialize(AtLeastOnePriceNotNull constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(StudentDto studentDto, ConstraintValidatorContext context) {
        if (studentDto == null) {
            return true; // Let other validations handle null StudentDto
        }
        return studentDto.getPriceIndividual() != null || studentDto.getPriceGroup() != null;
    }
}
