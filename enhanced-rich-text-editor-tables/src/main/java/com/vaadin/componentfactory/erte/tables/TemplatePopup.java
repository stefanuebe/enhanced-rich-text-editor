package com.vaadin.componentfactory.erte.tables;

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
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.vaadin.componentfactory.erte.tables.TemplateContants.P_COLOR;
import static com.vaadin.componentfactory.erte.tables.TemplateContants.TABLE;

public class TemplatePopup extends ToolbarPopup {

    private final VerticalLayout layout;
    private final ComboBox<String> templateField;
    private final Binder<JsonObject> binder;
    private JsonObject templates = Json.createObject();

    public TemplatePopup(ToolbarSwitch referencedSwitch) {
        super(referencedSwitch);

        binder = new Binder<>();
        templateField = new ComboBox<>();

        layout = new VerticalLayout(templateField);

        layout.add(createSettingsSection("Tabelle"));
//        layout.add(new HorizontalLayout(
//                createSettingsSizeField("Feste Breite"),
//                createSettingsSizeField("Min. Breite"),
//                createSettingsSizeField("Max. Breite")));
        layout.add(new HorizontalLayout(createSettingsColorField("Textfarbe",
                rules -> {
                    String s1 = Optional.ofNullable(rules.getObject(TABLE)).map(declaration -> declaration.getString(P_COLOR)).orElse(null);
                    return s1;
                },
                (rules, s) -> {
                    JsonObject declaration = rules.getObject(TABLE);
                    if (declaration == null) {
                        declaration = Json.createObject();
                        rules.put(TABLE, declaration);
                    }

                    declaration.put(P_COLOR, s); // TODO validate
                }),
                createSettingsColorField("Hintergrundfarbe", jsonObject1 -> null, (jsonObject1, s3) -> {
        }), createSettingsBorderField("Rahmen", jsonObject -> null, (jsonObject, s2) -> {
        })));

//        layout.add(createSettingsSection("Spalte"));
//        layout.add(new HorizontalLayout(
//                createSettingsSizeField("Feste Breite"),
//                createSettingsSizeField("Min. Breite"),
//                createSettingsSizeField("Max. Breite")));
//        layout.add(new HorizontalLayout(
//                createSettingsColorField("Textfarbe"),
//                createSettingsColorField("Hintergrundfarbe"),
//                createSettingsBorderField("Rahmen")));
//
//        layout.add(createSettingsSection("Zelle"));
//        layout.add(new HorizontalLayout(
//                createSettingsColorField("Textfarbe"),
//                createSettingsColorField("Hintergrundfarbe"),
//                createSettingsBorderField("Rahmen")));

        templateField.addValueChangeListener(event -> {
            String value = StringUtils.trimToNull(event.getValue());
            if (value != null && this.templates.hasKey(value)) {
                binder.setBean(this.templates.getObject(value));
            } else {
                binder.removeBean();
            }

            fireEvent(new TemplateSelectedEvent(this, event.isFromClient(), value));
        });

        binder.addStatusChangeListener(event -> {
            System.out.println("state changed");
        });

        binder.addValueChangeListener(event -> {
            System.out.println("value changed");
            if (binder.isValid()) {
                fireEvent(new TemplatesChangedEvent(this, event.isFromClient()));
            }
        });

        setFocusTrap(true);
        setRestoreFocusOnClose(true);
        setFocusOnOpenTarget(templateField);

        add(layout);
    }

    public void setTemplates(JsonObject templates) {
        this.templates = clone(templates);

        List<String> keys = new ArrayList<>(templates.keys().length);
        for (String key : templates.keys()) {
            if (!TemplateContants.PATTERN_TEMPLATE_NAME.matcher(key).matches()) {
                throw new IllegalArgumentException("Invalid template name: " + key);
            }
            keys.add(key);
        }

        templateField.setItems(keys);
        templateField.setItemLabelGenerator(item -> this.templates.getObject(item).getString("name"));

    }

    private static JsonObject clone(JsonObject objectToClone) {
        return Json.parse(objectToClone.toJson());
    }

    private static Component createSettingsSection(String label) {
        H5 h5 = new H5(label);
        h5.getStyle().set("margin-top", "var(--lumo-space-s)");
        return h5;
    }

    private TextField createSettingsBorderField(String label, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = createSettingsTextField(label, getter, setter);
        field.setTooltipText("Expects a valid css border definition, e.g. 1px solid black");
        return field;
    }

    private TextField createSettingsColorField(String label, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = createSettingsTextField(label, getter, setter);
        field.setTooltipText("Expects a valid css color definition, e.g. #123456 or red");
        return field;
    }

    private TextField createSettingsTextField(String label, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = new TextField(label);
        field.setClearButtonVisible(true);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        binder.forField(field).bind(getter, setter);

        return field;
    }

    private IntegerField createSettingsSizeField(String label) {
        IntegerField field = createSettingsIntField(label);
        field.setTooltipText("Expects a positive integer value. Will be interpreted with the unit 'rem' (~ global font size)");

        return field;
    }
    private IntegerField createSettingsIntField(String label) {
        IntegerField field = new IntegerField(label);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.setWidth("6rem");
        field.setStepButtonsVisible(true);
        return field;
    }

    public JsonObject getTemplates() {
        return clone(templates);
    }

    public void setActiveTemplate(@Nullable String template) {
        templateField.setValue(template);
    }

    public Optional<String> getActiveTemplate() {
        return templateField.getOptionalValue();
    }

    public Registration addTemplatesChangedListener(ComponentEventListener<TemplatesChangedEvent> listener) {
        return addListener(TemplatesChangedEvent.class, listener);
    }

    public Registration addTemplateSelectedListener(ComponentEventListener<TemplateSelectedEvent> listener) {
        return addListener(TemplateSelectedEvent.class, listener);
    }

    public static class TemplateSelectedEvent extends ComponentEvent<TemplatePopup> {
        private final String template;

        public TemplateSelectedEvent(TemplatePopup source, boolean fromClient, String template) {
            super(source, fromClient);
            this.template = template;
        }

        public String getTemplate() {
            return template;
        }
    }

    public static class TemplatesChangedEvent extends ComponentEvent<TemplatePopup> {
        public TemplatesChangedEvent(TemplatePopup source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}