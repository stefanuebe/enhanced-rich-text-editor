package com.vaadin.componentfactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

public class ToolbarPopup extends Popup {
    private Registration focusOnOpenTargetRegistration;

    public static ToolbarPopup vertical(ToolbarSwitch toolbarSwitch, Component... components) {
        ToolbarPopup popup = new ToolbarPopup(toolbarSwitch);
        popup.add(new VerticalLayout(components));
        return popup;
    }

    public static ToolbarPopup horizontal(ToolbarSwitch toolbarSwitch, Component... components) {
        ToolbarPopup popup = new ToolbarPopup(toolbarSwitch);
        popup.add(new HorizontalLayout(components));
        return popup;
    }

    public ToolbarPopup(ToolbarSwitch referencedSwitch) {
        setTarget(referencedSwitch.getElement());
        setFocusTrap(true);
        setRestoreFocusOnClose(true);
        addPopupOpenChangedEventListener(event -> referencedSwitch.setActive(event.isOpened()));

        referencedSwitch.addAttachListener(event -> {
            event.getSource().getParent().orElseThrow(IllegalStateException::new).getElement().appendChild(getElement());
        });

        referencedSwitch.addDetachListener(event -> {
            getElement().removeFromParent();
        });
    }

    public void setFocusOnOpenTarget(Component component) {
        if (focusOnOpenTargetRegistration != null) {
            focusOnOpenTargetRegistration.remove();
        }

        focusOnOpenTargetRegistration = addPopupOpenChangedEventListener(event -> {
            if (event.isOpened()) {
                component.getElement().callJsFunction("focus");
            }
        });

    }
}