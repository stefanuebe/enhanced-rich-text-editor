package com.vaadin.componentfactory;

import com.vaadin.componentfactory.toolbar.ToolbarPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSwitch;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TemplatesPopup extends ToolbarPopup {

    private final VerticalLayout layout;
    private final ComboBox<String> templateField;
    private final Binder<JsonObject> binder;
    private JsonObject templates = Json.createObject();

    public TemplatesPopup(ToolbarSwitch referencedSwitch) {
        super(referencedSwitch);

        binder = new Binder<>();

        templateField = new ComboBox<>();

        layout = new VerticalLayout(templateField);

        layout.add(createSettingsSection("Tabelle"));
        layout.add(new HorizontalLayout(
                createSettingsIntField("Feste Breite"),
                createSettingsIntField("Min. Breite"),
                createSettingsIntField("Max. Breite")));
        layout.add(new HorizontalLayout(
                createSettingsTextField("Hintergrundfarbe"),
                createSettingsTextField("Rahmen")));

        layout.add(createSettingsSection("Spalte"));
        layout.add(new HorizontalLayout(
                createSettingsIntField("Feste Breite"),
                createSettingsIntField("Min. Breite"),
                createSettingsIntField("Max. Breite")));
        layout.add(new HorizontalLayout(
                createSettingsTextField("Hintergrundfarbe"),
                createSettingsTextField("Rahmen")));

        layout.add(createSettingsSection("Zelle"));
        layout.add(new HorizontalLayout(
                createSettingsTextField("Hintergrundfarbe"),
                createSettingsTextField("Rahmen")));

        add(layout);
    }

    public void setTemplates(JsonObject templates) {
        List<String> keys = new ArrayList<>(templates.keys().length);
        for (String key : templates.keys()) {
            if (!Templates.TEMPLATE_NAME.matcher(key).matches()) {
                throw new IllegalArgumentException("Invalid template name: " + key);
            }
            keys.add(key);
        }

        this.templates = clone(templates);
        templateField.setItems(keys);

        String value = templateField.getValue();
        if (value != null) {
            if (this.templates.hasKey(value)) {
                // update form with up to date state, if necessary
                JsonObject object = this.templates.getObject(value);
                if (object != null) {
                    binder.readBean(object);
                }
            } else {
                // current template is unknown to given list, so clear the form
                templateField.clear();
            }
        }
    }

    private static JsonObject clone(JsonObject objectToClone) {
        return Json.parse(objectToClone.toJson());
    }

    private static Component createSettingsSection(String label) {
        H5 h5 = new H5(label);
        h5.getStyle().set("margin-top", "var(--lumo-space-s)");
        return h5;
    }

    private static TextField createSettingsTextField(String label) {
        TextField field = new TextField(label);
        field.setClearButtonVisible(true);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        return field;
    }

    private static IntegerField createSettingsIntField(String label) {
        IntegerField field = new IntegerField(label);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.setWidth("6rem");
        field.setStepButtonsVisible(true);
        return field;
    }

    public JsonObject getTemplates() {
        return clone(templates);
    }

    public void setActiveTemplateName(@Nullable String template) {
        templateField.setValue(template);
    }

    public Optional<String> getActiveTemplateName() {
        return templateField.getOptionalValue();
    }

    public Registration addTemplatesChangedListener(ComponentEventListener<TemplatesChangedEvent> listener) {
        return addListener(TemplatesChangedEvent.class, listener);
    }

    public Registration addTemplateSelectedListener(ComponentEventListener<TemplateSelecteEvent> listener) {
        return addListener(TemplateSelecteEvent.class, listener);
    }

    public static class TemplateSelecteEvent extends ComponentEvent<TemplatesPopup> {
        private final String template;

        public TemplateSelecteEvent(TemplatesPopup source, boolean fromClient, String template) {
            super(source, fromClient);
            this.template = template;
        }

        public String getTemplate() {
            return template;
        }
    }

    public static class TemplatesChangedEvent extends ComponentEvent<TemplatesPopup> {
        public TemplatesChangedEvent(TemplatesPopup source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}