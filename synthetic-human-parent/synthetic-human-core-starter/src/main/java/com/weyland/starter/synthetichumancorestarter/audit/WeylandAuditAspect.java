package com.weyland.starter.synthetichumancorestarter.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class WeylandAuditAspect {

    private final AuditService auditService;

    @Autowired
    public WeylandAuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(com.weyland.starter.synthetichumancorestarter.audit.WeylandWatchingYou)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        Object result = null;
        Throwable throwable = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            throwable = t;
            throw t;
        } finally {
            StringBuilder sb = new StringBuilder();
            sb.append("Method: ").append(method).append("; ");
            sb.append("Params: ").append(Arrays.toString(args)).append("; ");
            sb.append("Result: ").append(result).append("; ");
            if (throwable != null) sb.append("Exception: ").append(throwable.getMessage());
            auditService.audit(sb.toString());
        }
    }
}