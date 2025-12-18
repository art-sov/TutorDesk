package com.art.tutordesk.error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private Model model;

    @Test
    void handleException_shouldAddErrorMessageToModelAndReturnErrorView() {
        Exception exception = new RuntimeException("Test exception message");
        String expectedViewName = "error/custom-error";
        String expectedErrorMessage = "Oops! Something went wrong. Please try again later or contact support.";

        String actualViewName = globalExceptionHandler.handleException(exception, model);

        assertEquals(expectedViewName, actualViewName);
        verify(model).addAttribute(eq("errorMessage"), eq(expectedErrorMessage));
    }
}
