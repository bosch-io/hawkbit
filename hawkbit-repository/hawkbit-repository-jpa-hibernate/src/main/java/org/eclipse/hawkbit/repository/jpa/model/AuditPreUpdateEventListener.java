package org.eclipse.hawkbit.repository.jpa.model;

import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditPreUpdateEventListener implements PreUpdateEventListener {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Object entity = event.getEntity();
        auditLogger.info("UPDATE: Entity {} with state {}", entity.getClass().getSimpleName(), entity);
        // Returning false to indicate that the insert should proceed
        return false;
    }
}
