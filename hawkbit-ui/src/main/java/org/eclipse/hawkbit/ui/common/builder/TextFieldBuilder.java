/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;

import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Textfield builder.
 *
 */
public class TextFieldBuilder extends AbstractTextFieldBuilder<TextFieldBuilder, TextField> {

    /**
     * Constructor.
     * 
     * @param maxLengthAllowed
     *            as mandatory field
     */
    public TextFieldBuilder(final int maxLengthAllowed) {
        super(maxLengthAllowed);
        styleName(ValoTheme.TEXTAREA_TINY);
    }

    /**
     * Create a search text field.
     * 
     * @param textChangeListener
     *            listener when text is changed.
     * @return the textfield
     */
    public TextField createSearchField(final ValueChangeListener<String> textChangeListener) {
        final TextField textField = style(SPUIDefinitions.FILTER_BOX).styleName("text-style").buildTextComponent();
        textField.setWidth(100.0F, Unit.PERCENTAGE);
        textField.addValueChangeListener(textChangeListener);
        textField.setValueChangeMode(ValueChangeMode.LAZY);
        // 1 seconds timeout.
        textField.setValueChangeTimeout(1000);
        return textField;
    }

    @Override
    protected TextField createTextComponent() {
        final TextField textField = new TextField();
        textField.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        return textField;
    }

}
