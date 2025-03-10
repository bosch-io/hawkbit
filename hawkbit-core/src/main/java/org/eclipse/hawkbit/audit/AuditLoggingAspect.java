package org.eclipse.hawkbit.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@Component
public class AuditLoggingAspect {
    AuditContextProvider.AuditContext auditContext = new AuditContextProvider().getAuditContext();

    /**
     * Around advice that applies to methods annotated with @AuditLog.
     * It logs the request and, if logResponse is true, the response as well.
     */
    @Around("@annotation(auditLog)")
    public Object handleAuditLogging(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {

        String logUUID = UUID.randomUUID().toString();
        logAudit(logUUID, joinPoint, auditLog);

        Object result = joinPoint.proceed();

        if (auditLog.logResponse()) {
            logAuditWithResponse(logUUID, joinPoint, auditLog, result);
        }
        return result;
    }

    /**
     * Logs the request details including method name, parameters, user, and tenant.
     */
    private void logAudit(String logUUID, JoinPoint joinPoint, AuditLog auditLog) {
        String methodName = joinPoint.getSignature().getName();
        String params = getParamsToLog(joinPoint, auditLog);
        String username = auditContext.username();
        String tenant = auditContext.tenant();

        String requestLog = String.format(
            "[Log UUID: %s], Method: %s - Message: %s - Parameters: %s",
            logUUID, methodName, auditLog.message(), params
        );
        Audit.logMessage(tenant, username, auditLog.entity(), requestLog, auditLog.level());
    }

    /**
     * Logs both the request details and the response.
     */
    private void logAuditWithResponse(String logUUID, JoinPoint joinPoint, AuditLog auditLog, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String params = getParamsToLog(joinPoint, auditLog);
        String username = auditContext.username();
        String tenant = auditContext.tenant();

        String logMessage = String.format(
            "[Log UUID: %s], Method: %s - Message: %s - Parameters: %s - Response: %s",
            logUUID, methodName, auditLog.message(), params,
            result != null ? result.toString() : "null"
        );
        Audit.logMessage(tenant, username, auditLog.entity(), logMessage, auditLog.level());
    }

    private String getParamsToLog(JoinPoint joinPoint, AuditLog auditLog) {
        Object[] args = joinPoint.getArgs();
        String[] includeParams = auditLog.includeParams();

        if (includeParams.length == 0) {
            return Arrays.deepToString(args);
        } else {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = methodSignature.getParameterNames();
            Map<String, Object> paramMap = IntStream.range(0, paramNames.length)
                .boxed()
                .collect(Collectors.toMap(i -> paramNames[i], i -> args[i]));

            return Arrays.stream(includeParams)
                .filter(paramMap::containsKey)
                .map(name -> name + "=" + paramMap.get(name))
                .collect(Collectors.joining(", "));
        }
    }
}