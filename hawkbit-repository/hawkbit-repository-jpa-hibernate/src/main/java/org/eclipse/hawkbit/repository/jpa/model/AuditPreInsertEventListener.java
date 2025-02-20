package org.eclipse.hawkbit.repository.jpa.model;

import org.eclipse.hawkbit.repository.jpa.TenantIdentifier;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AuditPreInsertEventListener implements PreInsertEventListener {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private final TenantIdentifier tenantIdentifier;

    public AuditPreInsertEventListener(TenantIdentifier tenantIdentifier) {
        this.tenantIdentifier = tenantIdentifier;
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();
        auditLogger.info("Tenant {}: INSERT: Entity {} with state {}", tenantIdentifier.resolveCurrentTenantIdentifier(), entity.getClass().getSimpleName(), entity);
        // Returning false to indicate that the insert should proceed
        return false;
    }
}
