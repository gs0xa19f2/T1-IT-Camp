package com.weyland.starter.synthetichumancorestarter.audit;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class WeylandAuditAspectTest {

    @Test
    void testAuditAspectCallsAuditService() throws Throwable {
        AuditService auditService = Mockito.mock(AuditService.class);
        WeylandAuditAspect aspect = new WeylandAuditAspect(auditService);

        ProceedingJoinPointStub joinPoint = new ProceedingJoinPointStub("result");

        aspect.around(joinPoint);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(auditService).audit(captor.capture());
        String auditMsg = captor.getValue();
        assertTrue(auditMsg.contains("Method:"));
        assertTrue(auditMsg.contains("Result: result"));
    }
}