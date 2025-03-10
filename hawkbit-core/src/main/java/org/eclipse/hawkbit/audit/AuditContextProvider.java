package org.eclipse.hawkbit.audit;

import org.eclipse.hawkbit.tenancy.TenantAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditContextProvider {

    private final TenantAware.DefaultTenantResolver resolver = new TenantAware.DefaultTenantResolver();

    public AuditContext getAuditContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "system";
        String tenant = resolver.resolveTenant();
        return new AuditContext(tenant, username);
    }
    public record AuditContext(String tenant, String username) {
    }
}
