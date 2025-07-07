package com.example.pbc.logs;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class Logging {
    private static final Logger log = LoggerFactory.getLogger(Logging.class);

    @Around("execution(* com.example.pbc.service.*.*(..)) || execution(* com.example.pbc.rest_controller.*.*(..)) && !execution(* com.example.pbc.work_databased.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        log.info("{} выполнен за {} мс", joinPoint.getSignature(), executionTime);
        return result;
    }
}