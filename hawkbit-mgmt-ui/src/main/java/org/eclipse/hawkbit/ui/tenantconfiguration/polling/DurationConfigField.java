/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.polling;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.tenantconfiguration.ConfigurationItem;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;

/**
 * The DurationConfigField consists of three vaadin fields. A {@link #Label}
 * {@link #DurationField} and a {@link #CheckBox}. The user can then enter a
 * duration in the DurationField or he can configure using the global duration
 * by changing the CheckBox.
 */
public class DurationConfigField extends GridLayout implements ConfigurationItem {

    private static final long serialVersionUID = 1L;

    private final List<ConfigurationItemChangeListener> configurationChangeListeners = new ArrayList<>();

    private final CheckBox checkBox = new CheckBox();
    private final DurationField durationField = new DurationField();
    private Duration globalDuration;

    private DurationConfigField() {
        super(2, 2);

        this.addStyleName("duration-config-field");
        this.setSpacing(true);
        this.setImmediate(true);
        this.setColumnExpandRatio(1, 1.0F);

        this.addComponent(checkBox, 0, 0);
        this.setComponentAlignment(checkBox, Alignment.MIDDLE_LEFT);

        this.addComponent(durationField, 1, 0);
        this.setComponentAlignment(durationField, Alignment.MIDDLE_LEFT);

        checkBox.addValueChangeListener(event -> checkBoxChange());
        durationField.addValueChangeListener(event -> notifyConfigurationChanged());
    }

    private void checkBoxChange() {
        durationField.setEnabled(checkBox.getValue());

        if (!checkBox.getValue()) {
            durationField.setDuration(globalDuration);
        }

        notifyConfigurationChanged();
    }

    /**
     * has to be called before using, see Builder Implementation.
     * 
     * @param tenantDuration
     *            tenant specific duration value
     * @param globalDuration
     *            duration value which is stored in the global configuration
     */
    private void init(final Duration globalDuration, final Duration tenantDuration) {
        this.globalDuration = globalDuration;
        this.setValue(tenantDuration);
    }

    private void setCheckBoxTooltip(final String label) {
        checkBox.setDescription(label);
    }

    private void setAllowedRange(final Duration minimumDuration, final Duration maximumDuration) {
        durationField.setMinimumDuration(minimumDuration);
        durationField.setMaximumDuration(maximumDuration);
    }

    /**
     * Set the value of the duration field
     * 
     * @param tenantDuration
     *            duration which will be set in to the duration field, when
     *            {@code null} the global configuration will be used.
     */
    public void setValue(final Duration tenantDuration) {
        if (tenantDuration == null) {
            // no tenant specific configuration
            checkBox.setValue(false);
            durationField.setDuration(globalDuration);
            durationField.setEnabled(false);
            return;
        }

        checkBox.setValue(true);
        durationField.setDuration(tenantDuration);
        durationField.setEnabled(true);
    }

    /**
     * @return the duration of the duration field or null, when the user has
     *         configured to use the global value.
     */
    public Duration getValue() {
        if (checkBox.getValue()) {
            return durationField.getDuration();
        }
        return null;
    }

    @Override
    public boolean isUserInputValid() {
        return !checkBox.getValue() || (durationField.isValid() && durationField.getValue() != null);
    }

    private void notifyConfigurationChanged() {
        configurationChangeListeners.forEach(listener -> listener.configurationHasChanged());
    }

    @Override
    public void addChangeListener(final ConfigurationItemChangeListener listener) {
        configurationChangeListeners.add(listener);
    }

    public static DurationConfigFieldBuilder builder() {
        return new DurationConfigFieldBuilder();
    }

    public static class DurationConfigFieldBuilder {
        private final DurationConfigField field;

        private Duration globalDuration = null;
        private Duration tenantDuration = null;

        private DurationConfigFieldBuilder() {
            field = new DurationConfigField();
        };

        public DurationConfigFieldBuilder checkBoxTooltip(final String label) {
            field.setCheckBoxTooltip(label);
            return this;
        }

        public DurationConfigFieldBuilder globalDuration(final Duration globalDuration) {
            this.globalDuration = globalDuration;
            return this;
        }

        public DurationConfigFieldBuilder caption(final String caption) {
            field.setCaption(caption);
            return this;
        }

        public DurationConfigFieldBuilder range(final Duration minDuration, final Duration maxDuration) {
            field.setAllowedRange(minDuration, maxDuration);
            return this;
        }

        public DurationConfigFieldBuilder tenantDuration(final Duration tenantDuration) {
            this.tenantDuration = tenantDuration;
            return this;
        }

        public DurationConfigField build() {
            if (globalDuration == null) {
                throw new IllegalStateException(
                        "Cannot build DurationConfigField without a value for global duration.");
            }

            field.init(globalDuration, tenantDuration);
            return field;
        }
    };
}
