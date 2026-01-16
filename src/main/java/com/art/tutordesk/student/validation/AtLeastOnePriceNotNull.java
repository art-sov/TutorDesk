package com.art.tutordesk.student.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AtLeastOnePriceNotNullValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOnePriceNotNull {
    String message() default "At least one of individual or group price must be provided.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
