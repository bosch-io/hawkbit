package org.eclipse.hawkbit.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Audit {

    private static AuditContextProvider.AuditContext AUDIT_CONTEXT = new AuditContextProvider().getAuditContext();
    private Audit() {}

    public static void logMessage(String entity, String message, AuditLog.Level level) {
        logMessage(AUDIT_CONTEXT.tenant(), AUDIT_CONTEXT.username(), entity , message, level);
    }

    public static void logMessage(String tenant, String username, String entity, String message, AuditLog.Level level) {
        String logMessage = String.format("[%s] User: %s, Tenant: %s - %s", entity, username, tenant, message);
        Logger auditLogger = LoggerFactory.getLogger("AUDIT" + (entity != null ? ("-" + entity) : ""));
        switch (level) {
            case INFO:
                auditLogger.info(logMessage);
                break;
            case WARN:
                auditLogger.warn(logMessage);
                break;
            case ERROR:
                auditLogger.error(logMessage);
                break;
        }
    }
}
