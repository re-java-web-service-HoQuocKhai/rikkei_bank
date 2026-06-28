package com.re.rikkei_bank.aop;

import com.re.rikkei_bank.dto.request.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.re.rikkei_bank.service.TransactionService.transfer(..))")
    public Object logTransfer(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        TransferRequest request = null;
        String username = null;
        
        for (Object arg : args) {
            if (arg instanceof TransferRequest) {
                request = (TransferRequest) arg;
            } else if (arg instanceof String) {
                username = (String) arg;
            }
        }

        if (request != null) {
            log.info("Starting transfer of amount {} from {} to {}. Initiated by user: {}", 
                    request.getAmount(), request.getFromAccountNumber(), request.getToAccountNumber(), username);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.info("Transfer SUCCESS in {} ms", stopWatch.getTotalTimeMillis());
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            log.error("Transfer FAILED after {} ms. Reason: {}", stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
}
