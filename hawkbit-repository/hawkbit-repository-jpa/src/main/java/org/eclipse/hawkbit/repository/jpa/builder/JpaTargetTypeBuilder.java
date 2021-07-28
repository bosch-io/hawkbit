package org.eclipse.hawkbit.repository.jpa.builder;

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.TargetTypeManagement;
import org.eclipse.hawkbit.repository.builder.GenericTargetTypeUpdate;
import org.eclipse.hawkbit.repository.builder.TargetTypeBuilder;
import org.eclipse.hawkbit.repository.builder.TargetTypeCreate;
import org.eclipse.hawkbit.repository.builder.TargetTypeUpdate;

public class JpaTargetTypeBuilder implements TargetTypeBuilder {
    private final DistributionSetTypeManagement distributionSetTypeManagement;

    public JpaTargetTypeBuilder(DistributionSetTypeManagement distributionSetTypeManagement) {
        this.distributionSetTypeManagement = distributionSetTypeManagement;
    }

    @Override
    public TargetTypeUpdate update(long id) {
        return new GenericTargetTypeUpdate(id);
    }

    @Override
    public TargetTypeCreate create() {
        return new JpaTargetTypeCreate(distributionSetTypeManagement);
    }
}
