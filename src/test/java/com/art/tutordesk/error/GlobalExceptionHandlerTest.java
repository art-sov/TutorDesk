package com.art.tutordesk.error;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private Model model;
    @Mock
    private HttpServletResponse response;

    @Test
    void handleException_shouldAddErrorMessageToModelAndReturnErrorView() {
        Exception exception = new RuntimeException("Test exception message");
        String expectedViewName = "error/custom-error";
        String expectedErrorMessage = "Oops! Something went wrong. Please try again later or contact support.";

        when(response.isCommitted()).thenReturn(false);

        String actualViewName = globalExceptionHandler.handleException(exception, model, response);

        assertEquals(expectedViewName, actualViewName);
        verify(model).addAttribute(eq("errorMessage"), eq(expectedErrorMessage));
    }

    @Test
    void handleException_shouldReturnNull_whenResponseIsCommitted() {
        Exception exception = new RuntimeException("Test exception message");

        when(response.isCommitted()).thenReturn(true);

        String actualViewName = globalExceptionHandler.handleException(exception, model, response);

        assertEquals(null, actualViewName);
        verify(model, never()).addAttribute(any(), any());
    }
}
