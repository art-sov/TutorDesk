package com.art.tutordesk.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        log.error("Unhandled exception occurred", ex);
        model.addAttribute("errorMessage", "Oops! Something went wrong. Please try again later or contact support.");
        return "error/custom-error";
    }
}
