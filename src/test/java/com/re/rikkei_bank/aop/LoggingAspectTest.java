package com.re.rikkei_bank.aop;

import com.re.rikkei_bank.exception.CustomException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        lenient().when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.getName()).thenReturn("testMethod");
        lenient().when(proceedingJoinPoint.getTarget()).thenReturn(new Object());
    }

    @Test
    void logExecutionTime_Success() throws Throwable {
        when(proceedingJoinPoint.proceed()).thenReturn("Success Result");

        Object result = loggingAspect.logExecutionTime(proceedingJoinPoint);

        assertEquals("Success Result", result);
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    @Test
    void logExecutionTime_Exception() throws Throwable {
        when(proceedingJoinPoint.proceed()).thenThrow(new CustomException("Error", HttpStatus.BAD_REQUEST));

        assertThrows(CustomException.class, () -> loggingAspect.logExecutionTime(proceedingJoinPoint));
        verify(proceedingJoinPoint, times(1)).proceed();
    }

    @Test
    void logAfterReturning_DoesNotThrow() {
        loggingAspect.logAfterReturning(proceedingJoinPoint, "Result");
    }

    @Test
    void logAfterThrowing_CustomException() {
        CustomException ex = new CustomException("Business Error", HttpStatus.BAD_REQUEST);
        loggingAspect.logAfterThrowing(proceedingJoinPoint, ex);
    }

    @Test
    void logAfterThrowing_ValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getMessage()).thenReturn("Validation Error");
        loggingAspect.logAfterThrowing(proceedingJoinPoint, ex);
    }

    @Test
    void logAfterThrowing_SystemException() {
        RuntimeException ex = new RuntimeException("System Error");
        loggingAspect.logAfterThrowing(proceedingJoinPoint, ex);
    }
}
