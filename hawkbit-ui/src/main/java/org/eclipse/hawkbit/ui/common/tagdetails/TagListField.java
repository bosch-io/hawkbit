/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.tagdetails;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.tagdetails.TagPanelLayout.TagAssignmentListener;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUITagButtonStyle;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;

/**
 * A panel that shows the assigned tags. A click on a tag unsassigns the tag
 * from the {@link Target} or {@link DistributionSet}.
 */
public class TagListField extends CssLayout {

    private static final long serialVersionUID = 1L;

    // TODO: check if the order is preserved as before
    private final transient Map<ProxyTag, Button> tagButtons = new TreeMap<>(
            Comparator.comparing(ProxyTag::getName, String.CASE_INSENSITIVE_ORDER));
    private final transient Set<TagAssignmentListener> listeners = Sets.newConcurrentHashSet();
    private final VaadinMessageSource i18n;
    private final boolean readOnlyMode;

    /**
     * Constructor.
     * 
     * @param i18n
     * @param readOnlyMode
     *            if <code>true</code> no unassignment can be done
     */
    TagListField(final VaadinMessageSource i18n, final boolean readOnlyMode) {
        this.i18n = i18n;
        this.readOnlyMode = readOnlyMode;
        setSizeFull();
    }

    /**
     * Initializes the Tag panel with all assigned tags.
     * 
     * @param assignedTags
     *            assigned tags
     */
    void initializeAssignedTags(final List<ProxyTag> assignedTags) {
        removeAllComponents();

        assignedTags.forEach(tag -> {
            final Button tagButton = createTagButton(tag);
            tagButtons.put(tag, tagButton);
        });

        addTagButtonsAsComponents();
    }

    /**
     * Adds a tag
     * 
     * @param tagName
     * @param tagColor
     */
    void addTag(final ProxyTag tagData) {
        if (!tagButtons.containsKey(tagData)) {
            removeAllComponents();

            final Button tagButton = createTagButton(tagData);
            tagButtons.put(tagData, tagButton);

            addTagButtonsAsComponents();
        }
    }

    private void addTagButtonsAsComponents() {
        tagButtons.values().forEach(this::addComponent);
    }

    private Button createTagButton(final ProxyTag tagData) {
        final Button tagButton = SPUIComponentProvider.getButton(
                UIComponentIdProvider.ASSIGNED_TAG_ID_PREFIX + "." + tagData.getId(), tagData.getName(),
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_CLICK_TO_REMOVE), null, false, null,
                SPUITagButtonStyle.class);
        tagButton.addStyleName(SPUIStyleDefinitions.TAG_BUTTON_WITH_BACKGROUND);
        tagButton.addStyleName(SPUIDefinitions.TEXT_STYLE + " " + SPUIStyleDefinitions.DETAILS_LAYOUT_STYLE);
        tagButton.setEnabled(!readOnlyMode);

        tagButton.setCaption(new ProxyFontIcon(VaadinIcons.CIRCLE, "", "", tagData.getColour()).getHtml() + " "
                + tagData.getName().concat(" ×"));
        tagButton.setCaptionAsHtml(true);

        tagButton.addClickListener(e -> removeTagAssignment(tagData));

        return tagButton;
    }

    private void removeTagAssignment(final ProxyTag tagData) {
        removeTag(tagData);
        notifyListenersTagAssignmentRemoved(tagData);
    }

    /**
     * Removes a tag from the field.
     * 
     * @param tagData
     */
    void removeTag(final ProxyTag tagData) {
        final Button button = tagButtons.get(tagData);
        if (button != null) {
            tagButtons.remove(tagData);
            removeComponent(button);
        }
    }

    /**
     * Removes a tag from the field.
     * 
     * @param tagData
     */
    void removeTag(final Long tagId) {
        tagButtons.keySet().stream().filter(tagData -> tagData.getId().equals(tagId)).findAny()
                .ifPresent(this::removeTag);
    }

    /**
     * Removes all tags from the field.
     */
    void removeAllTags() {
        removeAllComponents();
        tagButtons.clear();
    }

    /**
     * Registers a {@link TagAssignmentListener}.
     * 
     * @param listener
     *            the listener to register
     */
    void addTagAssignmentListener(final TagAssignmentListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a {@link TagAssignmentListener}.
     * 
     * @param listener
     *            the listener to remove
     */
    void removeTagAssignmentListener(final TagAssignmentListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersTagAssignmentRemoved(final ProxyTag tagData) {
        listeners.forEach(listener -> listener.unassignTag(tagData));
    }

    /**
     * Returns all assigned tags shown in the field.
     * 
     * @return a {@link List} with tags
     */
    List<ProxyTag> getTags() {
        return Lists.newArrayList(tagButtons.keySet());
    }
}
