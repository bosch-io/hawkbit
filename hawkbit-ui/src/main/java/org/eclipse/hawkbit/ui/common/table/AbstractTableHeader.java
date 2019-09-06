/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.table;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilderV7;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilderV7;
import org.eclipse.hawkbit.ui.components.SPUIButton;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Parent class for table header.
 */
public abstract class AbstractTableHeader extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    protected VaadinMessageSource i18n;

    protected SpPermissionChecker permChecker;

    protected transient EventBus.UIEventBus eventBus;

    protected HorizontalLayout titleFilterIconsLayout;

    private Label headerCaption;

    private TextField searchField;

    private SPUIButton searchResetIcon;

    private Button showFilterButtonLayout;

    private Button addIcon;

    private SPUIButton maxMinIcon;

    private final ManagementUIState managementUIState;

    private final ManageDistUIState manageDistUIstate;

    private final ArtifactUploadState artifactUploadState;

    protected AbstractTableHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final ManagementUIState managementUIState,
            final ManageDistUIState manageDistUIstate, final ArtifactUploadState artifactUploadState) {
        this.i18n = i18n;
        this.permChecker = permChecker;
        this.eventBus = eventBus;
        this.managementUIState = managementUIState;
        this.manageDistUIstate = manageDistUIstate;
        this.artifactUploadState = artifactUploadState;
        createComponents();
        buildLayout();
        restoreState();
        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }
    }

    /**
     * Subscribes the view to the eventBus. Method has to be overriden (return
     * false) if the view does not contain any listener to avoid Vaadin blowing
     * up our logs with warnings.
     */
    protected boolean doSubscribeToEventBus() {
        return true;
    }

    private void createComponents() {
        headerCaption = createHeaderCaption();
        searchField = new TextFieldBuilderV7(64).id(getSearchBoxId())
                .createSearchField(event -> searchBy(event.getText()));

        searchResetIcon = createSearchResetIcon();

        addIcon = createAddIcon();

        showFilterButtonLayout = createShowFilterButtonLayout();
        // Not visible by default.
        showFilterButtonLayout.setVisible(false);

        maxMinIcon = createMaxMinIcon();

        final String onLoadSearchBoxValue = onLoadSearchBoxValue();

        if (onLoadSearchBoxValue != null && onLoadSearchBoxValue.length() > 0) {
            openSearchTextField();
            searchField.setValue(onLoadSearchBoxValue);
        }

    }

    private void restoreState() {

        final String onLoadSearchBoxValue = onLoadSearchBoxValue();
        if (StringUtils.hasText(onLoadSearchBoxValue)) {
            openSearchTextField();
            searchField.setValue(onLoadSearchBoxValue.trim());
        }

        if (onLoadIsTableMaximized()) {
            /**
             * If table is maximized display the minimize icon.
             */
            showMinIcon();
            hideAddIcon();
        }

        if (onLoadIsShowFilterButtonDisplayed()) {
            /**
             * Show filter button will be displayed when filter button layout is
             * closed.
             */
            setFilterButtonsIconVisible(true);
        }
    }

    private void hideAddIcon() {
        addIcon.setVisible(false);
    }

    private void showAddIcon() {
        addIcon.setVisible(true);
    }

    private void buildLayout() {
        titleFilterIconsLayout = createHeaderFilterIconLayout();

        titleFilterIconsLayout.addComponents(headerCaption, searchField, searchResetIcon, showFilterButtonLayout);
        titleFilterIconsLayout.setComponentAlignment(headerCaption, Alignment.TOP_LEFT);
        titleFilterIconsLayout.setComponentAlignment(searchField, Alignment.TOP_RIGHT);
        titleFilterIconsLayout.setComponentAlignment(searchResetIcon, Alignment.TOP_RIGHT);
        titleFilterIconsLayout.setComponentAlignment(showFilterButtonLayout, Alignment.TOP_RIGHT);
        if (hasCreatePermission() && isAddNewItemAllowed()) {
            titleFilterIconsLayout.addComponent(addIcon);
            titleFilterIconsLayout.setComponentAlignment(addIcon, Alignment.TOP_RIGHT);
        }
        titleFilterIconsLayout.addComponent(maxMinIcon);
        titleFilterIconsLayout.setComponentAlignment(maxMinIcon, Alignment.TOP_RIGHT);
        titleFilterIconsLayout.setExpandRatio(headerCaption, 0.4F);
        titleFilterIconsLayout.setExpandRatio(searchField, 0.6F);

        addComponent(titleFilterIconsLayout);

        addStyleName("bordered-layout");
        addStyleName("no-border-bottom");
    }

    private Label createHeaderCaption() {
        return new LabelBuilderV7().name(getHeaderCaption()).buildCaptionLabel();
    }

    private SPUIButton createSearchResetIcon() {
        final SPUIButton button = (SPUIButton) SPUIComponentProvider.getButton(getSearchRestIconId(), "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_SEARCH), null, false, FontAwesome.SEARCH,
                SPUIButtonStyleNoBorder.class);
        button.addClickListener(event -> onSearchResetClick());
        button.setData(Boolean.FALSE);
        return button;
    }

    private Button createAddIcon() {
        final Button button = SPUIComponentProvider.getButton(getAddIconId(), "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_ADD), null, false, FontAwesome.PLUS,
                SPUIButtonStyleNoBorder.class);
        button.addClickListener(this::addNewItem);
        return button;
    }

    private Button createShowFilterButtonLayout() {
        final Button button = SPUIComponentProvider.getButton(getShowFilterButtonLayoutId(), null,
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_SHOW_TAGS), null, false, FontAwesome.TAGS,
                SPUIButtonStyleNoBorder.class);
        button.setVisible(false);
        button.addClickListener(event -> showFilterButtonsIconClicked());
        return button;
    }

    private SPUIButton createMaxMinIcon() {
        final SPUIButton button = (SPUIButton) SPUIComponentProvider.getButton(getMaxMinIconId(), "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_MAXIMIZE), null, false, FontAwesome.EXPAND,
                SPUIButtonStyleNoBorder.class);
        button.addClickListener(event -> maxMinButtonClicked());
        return button;
    }

    private void onSearchResetClick() {
        final Boolean flag = isSearchFieldOpen();
        if (flag == null || Boolean.FALSE.equals(flag)) {
            // Clicked on search Icon
            openSearchTextField();
        } else {
            // Clicked on rest icon
            closeSearchTextField();
        }
    }

    protected Boolean isSearchFieldOpen() {
        return (Boolean) searchResetIcon.getData();
    }

    private void openSearchTextField() {
        searchResetIcon.addStyleName(SPUIDefinitions.FILTER_RESET_ICON);
        searchResetIcon.toggleIcon(FontAwesome.TIMES);
        searchResetIcon.setData(Boolean.TRUE);
        searchResetIcon.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_RESET));
        searchField.removeStyleName("filter-box-hide");
        searchField.focus();
    }

    private void closeSearchTextField() {
        searchField.setValue("");
        searchField.addStyleName("filter-box-hide");
        searchResetIcon.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_SEARCH));
        searchResetIcon.removeStyleName(SPUIDefinitions.FILTER_RESET_ICON);
        searchResetIcon.toggleIcon(FontAwesome.SEARCH);
        searchResetIcon.setData(Boolean.FALSE);
        resetSearchText();
    }

    private void maxMinButtonClicked() {
        final Boolean flag = (Boolean) maxMinIcon.getData();
        if (flag == null || Boolean.FALSE.equals(flag)) {
            // Clicked on max Icon
            maximizedTableView();
        } else {
            // Clicked on min icon
            minimizeTableView();
        }
    }

    private void maximizedTableView() {
        showMinIcon();
        hideAddIcon();
        maximizeTable();
    }

    private void minimizeTableView() {
        showMaxIcon();
        showAddIcon();
        minimizeTable();
    }

    private void showMinIcon() {
        maxMinIcon.toggleIcon(FontAwesome.COMPRESS);
        maxMinIcon.setData(Boolean.TRUE);
        maxMinIcon.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_MINIMIZE));
    }

    private void showMaxIcon() {
        maxMinIcon.toggleIcon(FontAwesome.EXPAND);
        maxMinIcon.setData(Boolean.FALSE);
        maxMinIcon.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_MAXIMIZE));
    }

    private static HorizontalLayout createHeaderFilterIconLayout() {
        final HorizontalLayout titleFilterIconsLayout = new HorizontalLayout();
        titleFilterIconsLayout.addStyleName(SPUIStyleDefinitions.WIDGET_TITLE);
        titleFilterIconsLayout.setSpacing(false);
        titleFilterIconsLayout.setMargin(false);
        titleFilterIconsLayout.setSizeFull();
        return titleFilterIconsLayout;
    }

    private void showFilterButtonsIconClicked() {
        /* Remove the show filter Buttons button */
        setFilterButtonsIconVisible(false);
        /* Show the filter buttons layout */
        showFilterButtonsLayout();
    }

    protected void setFilterButtonsIconVisible(final boolean isVisible) {
        showFilterButtonLayout.setVisible(isVisible);
    }

    /**
     * Resets search text and closed search field when complex filters are
     * applied.
     */
    protected void resetSearch() {
        closeSearchTextField();
    }

    /**
     * Once user switches to custom filters search functionality is re-enabled.
     */
    protected void disableSearch() {
        searchResetIcon.setEnabled(false);
    }

    /**
     * Once user switches to simple filters search functionality is re-enabled.
     */
    protected void reEnableSearch() {
        searchResetIcon.setEnabled(true);
    }

    /**
     * Checks if the creation of a new item is allowed. Default is true.
     * 
     * @return true if the creation of a new item is allowed, otherwise returns
     *         false.
     */
    protected abstract Boolean isAddNewItemAllowed();

    /**
     * Get the title of the table.
     * 
     * @return title as String.
     */
    protected abstract String getHeaderCaption();

    /**
     * get Id of search text field.
     * 
     * @return Id of the text field.
     */
    protected abstract String getSearchBoxId();

    /**
     * Get search reset Icon Id.
     * 
     * @return Id of search reset icon.
     */
    protected abstract String getSearchRestIconId();

    /**
     * Get Id of add Icon.
     * 
     * @return String of add Icon.
     */
    protected abstract String getAddIconId();

    /**
     * Get search box on load text value.
     * 
     * @return value of search box.
     */
    protected abstract String onLoadSearchBoxValue();

    /**
     * Check if logged in user has create permission..
     * 
     * @return true of user has create permission, otherwise return false.
     */
    protected abstract boolean hasCreatePermission();

    /**
     * Get Id of the show filter buttons layout.
     * 
     * @return Id of the show filter buttons Icon.
     */
    protected abstract String getShowFilterButtonLayoutId();

    /**
     * Show the filter buttons layout logic. This method will be called when
     * show filter buttons Icon is clicked displayed on the header.
     */
    protected abstract void showFilterButtonsLayout();

    /**
     * This method will be called when user resets the search text means on
     * click of (X) icon.
     */
    protected abstract void resetSearchText();

    /**
     * Get the Id of min/max button for the table.
     * 
     * @return Id of min/max button.
     */
    protected abstract String getMaxMinIconId();

    /**
     * Called when table is maximized.
     */
    public abstract void maximizeTable();

    /**
     * Called when table is minimized.
     */
    public abstract void minimizeTable();

    /**
     * Get the max/min icon state on load.
     * 
     * @return true if table should be maximized.
     */
    public abstract Boolean onLoadIsTableMaximized();

    /**
     * On load show filter button is displayed.
     * 
     * @return true if requires to delete, otherwise false.
     */
    public abstract Boolean onLoadIsShowFilterButtonDisplayed();

    protected abstract void searchBy(String newSearchText);

    protected abstract void addNewItem(final Button.ClickEvent event);

    protected ManagementUIState getManagementUIState() {
        return managementUIState;
    }

    protected ManageDistUIState getManageDistUIstate() {
        return manageDistUIstate;
    }

    protected ArtifactUploadState getArtifactUploadState() {
        return artifactUploadState;
    }

}
