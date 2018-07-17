/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common;

import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueContext;
import com.vaadin.data.validator.StringLengthValidator;

/**
 * Assures that the entered text does not contain only whitespaces. At least one
 * character has to be entered. Leading and trailing whitespaces are allowed as
 * they will be trimmed by the repository.
 */
public class EmptyStringValidator extends StringLengthValidator {

    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_KEY = "validator.textfield.min.length";

    /**
     * Constructor for EmptyStringValidator
     * 
     * @param i18n
     *            {@link VaadinMessageSource}
     * @param maxLength
     *            max length of the textfield
     */
    public EmptyStringValidator(final VaadinMessageSource i18n, final int maxLength) {
        super(i18n.getMessage(MESSAGE_KEY, maxLength), 1, maxLength);
    }

    @Override
    public ValidationResult apply(final String value, final ValueContext context) {
        // TODO MR check ValueContext
        return super.apply(value != null ? value.trim() : null, new ValueContext());
    }

}
