/**
 * Copyright (c) 2022 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.model;

import org.eclipse.hawkbit.repository.model.AutoConfirmationStatus;
import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.model.Target;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "sp_target_conf_status")
public class JpaAutoConfirmationStatus extends AbstractJpaTenantAwareBaseEntity implements AutoConfirmationStatus {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "fk_target_auto_conf"))
    private JpaTarget target;

    @Column(name = "initiator", length = USERNAME_FIELD_LENGTH)
    @Size(max = NamedEntity.DESCRIPTION_MAX_SIZE)
    private String initiator;

    @Column(name = "remark", length = NamedEntity.DESCRIPTION_MAX_SIZE)
    @Size(max = NamedEntity.DESCRIPTION_MAX_SIZE)
    private String remark;

    /**
     * Default constructor needed for JPA entities.
     */
    public JpaAutoConfirmationStatus() {
        // Default constructor needed for JPA entities.
    }

    public JpaAutoConfirmationStatus(final String initiator, final String remark, final Target target) {
        this.target = (JpaTarget) target;
        this.initiator = initiator;
        this.remark = remark;
    }

    @Override
    public Target getTarget() {
        return target;
    }

    @Override
    public String getInitiator() {
        return initiator;
    }

    @Override
    public long getActivatedAt() {
        return getCreatedAt();
    }

    @Override
    public String getRemark() {
        return remark;
    }

    public void setRemark(final String remark) {
        this.remark = remark;
    }

    @Override
    public String constructActionMessage() {
        final String remarkMessage = StringUtils.hasText(remark) ? remark : "n/a";
        return String.format("Assignment automatically confirmed by initiator ''%s'' %nRemark: %s", initiator,
                remarkMessage);
    }

    @Override
    public String toString() {
        return "AutoConfirmationStatus [target=" + target.getControllerId() + ", initiator=" + initiator
                + ", activatedAt=" + getCreatedAt() + ", remark=" + remark + "]";
    }

}
