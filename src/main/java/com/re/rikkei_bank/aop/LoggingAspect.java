package com.re.rikkei_bank.aop;

import com.re.rikkei_bank.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.re.rikkei_bank.service.impl..*)")
    public void servicePointcut() {
    }

    private String getRequestUrl() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getMethod() + " " + request.getRequestURI();
        }
        return "N/A";
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getName();
        }
        return "Anonymous/System";
    }

    @Around("servicePointcut()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String requestUrl = getRequestUrl();
        String user = getCurrentUser();

        log.info("START - [User: {}] [URL: {}] Executing: {}.{}", user, requestUrl, className, methodName);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        stopWatch.stop();
        log.info("END - [User: {}] {}.{} executed in {} ms", user, className, methodName, stopWatch.getTotalTimeMillis());

        return result;
    }

    @AfterReturning(pointcut = "servicePointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.info("SUCCESS - {}.{} completed successfully.", className, methodName);
    }

    @AfterThrowing(pointcut = "servicePointcut()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String requestUrl = getRequestUrl();
        String user = getCurrentUser();

        if (ex instanceof CustomException) {
            log.error("BUSINESS ERROR - [User: {}] [URL: {}] {}.{} -> {}", user, requestUrl, className, methodName, ex.getMessage());
        } else if (ex instanceof MethodArgumentNotValidException) {
            log.error("VALIDATION ERROR - [User: {}] [URL: {}] {}.{} -> {}", user, requestUrl, className, methodName, ex.getMessage());
        } else {
            log.error("SYSTEM ERROR - [User: {}] [URL: {}] {}.{} -> {}", user, requestUrl, className, methodName, ex.getMessage(), ex);
        }
    }
}
