package com.vaadin.componentfactory.erte.tables;

import com.vaadin.componentfactory.toolbar.ToolbarPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSwitch;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import elemental.json.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.*;

public class TemplatePopup extends ToolbarPopup {

    private final VerticalLayout layout;
    private final ComboBox<String> templateField;
    private final Binder<JsonObject> tableBinder;
    private final Binder<JsonObject> rowBinder;
    private JsonObject currentTemplate;
    private JsonObject templates = Json.createObject();
    private int selectedRow;

    public TemplatePopup(ToolbarSwitch referencedSwitch) {
        super(referencedSwitch);

        tableBinder = new Binder<>();
        rowBinder = new Binder<>();

        templateField = new ComboBox<>();
        applyOverlayPopupCloseWorkaround(templateField);

        layout = new VerticalLayout(templateField);
        layout.setSpacing(false);
        setCloseOnClick(false);

        initTableForm();
        initRowForm();

        templateField.addValueChangeListener(event -> {
            String value = StringUtils.trimToNull(event.getValue());
            if (value != null && this.templates.hasKey(value)) {
                currentTemplate = this.templates.getObject(value);
            } else {
                currentTemplate = null;
            }

            if (currentTemplate != null) {
                JsonObject table = currentTemplate.getObject(TABLE);
                if (table == null) {
                    table = Json.createObject();
                }
                tableBinder.setBean(table);
            }

            fireEvent(new TemplateSelectedEvent(this, event.isFromClient(), value));
        });
        setFocusOnOpenTarget(templateField);

        add(layout);

        // Workaround for https://github.com/vaadin-component-factory/vcf-popup/issues/16
        getElement().executeJs("this.addEventListener('popup-open-changed', e => {" +
                               "    if(!e.detail.value && this.__stayOpen) {" +
                               "        this.opened = true;" +
                               "        delete this.__stayOpen;" +
                               "    }" +
                               "});");
    }

    private void initTableForm() {
        layout.add(createSettingsSection("Tabelle"));
        layout.add(new HorizontalLayout(createSettingsColorField("T Textfarbe", tableBinder, getter(P_COLOR), setter(P_COLOR))));

        tableBinder.addValueChangeListener(event -> {
            if (tableBinder.isValid()) {
                fireEvent(new TemplatesChangedEvent(this, event.isFromClient()));
            }
        });
    }

    private static ValueProvider<JsonObject, String> getter(String key) {
        return jsonObject -> jsonObject.hasKey(key) ? jsonObject.getString(key) : null;
    }

    private static Setter<JsonObject, String> setter(String key) {
        return (jsonObject1, s) -> jsonObject1.put(key, s);
    }

    private void initRowForm() {
        layout.add(createSettingsSection("Aktuelle Zeile"));
        HorizontalLayout rowLayout = new HorizontalLayout(
                createSettingsColorField("Z Textfarbe", rowBinder, getter(P_COLOR), setter(P_COLOR)),
                createSettingsColorField("Z Hintergrundfarbe", rowBinder, getter(P_BACKGROUND), setter(P_BACKGROUND)));

        rowLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.add(rowLayout);

        rowBinder.addValueChangeListener(event -> {
            if (rowBinder.isValid()) {
                fireEvent(new TemplatesChangedEvent(this, event.isFromClient()));
            }
        });
    }

    public void setTemplates(JsonObject templates) {
        this.templates = clone(templates);

        List<String> keys = new ArrayList<>(templates.keys().length);
        for (String key : templates.keys()) {
            if (!TemplateConstants.PATTERN_TEMPLATE_NAME.matcher(key).matches()) {
                throw new IllegalArgumentException("Invalid template name: " + key);
            }
            keys.add(key);
        }

        templateField.setItems(keys);
        templateField.setItemLabelGenerator(item -> this.templates.getObject(item).getString("name"));
    }

    public void setSelectedRow(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("Row must not be negative");
        }

        if (this.selectedRow != row) {
            Notification.show("row changed to " + row);
            this.selectedRow = row;

            JsonArray array = currentTemplate.getArray(ROWS);
            String index = String.valueOf(row + 1);
            JsonObject rowObject = searchForIndexedObject(array, index, false); // css nth child are 1 based
            JsonObject rowDeclarations;
            if (rowObject == null) {
                rowObject = Json.createObject();
                rowObject.put(INDEX, index);
                rowDeclarations = Json.createObject();
                rowObject.put(DECLARATIONS, rowDeclarations);
            } else {
                rowDeclarations = rowObject.getObject(DECLARATIONS);
            }
            rowBinder.setBean(rowDeclarations); // null automatically clears the binder
        }
    }

    private JsonObject searchForIndexedObject(JsonArray array, String index, boolean last) {
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JsonObject object = array.getObject(i);
                if ((!last || (object.hasKey(LAST) && object.getBoolean(LAST))) && index.equals(object.getString(INDEX))) {
                    return object;
                }
            }
        }

        return null;
    }



    private static JsonObject clone(JsonObject objectToClone) {
        return Json.parse(objectToClone.toJson());
    }

    private static Component createSettingsSection(String label) {
        H5 h5 = new H5(label);
        h5.getStyle().set("margin-top", "var(--lumo-space-s)");
        return h5;
    }

    private TextField createSettingsBorderField(String label, Binder<JsonObject> binder, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = createSettingsTextField(label, binder, getter, setter);
        field.setTooltipText("Expects a valid css border definition, e.g. 1px solid black");
        return field;
    }

    private TextField createSettingsColorField(String label, Binder<JsonObject> binder, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = createSettingsTextField(label, binder, getter, setter);
        field.setTooltipText("Expects a valid css color definition, e.g. #123456 or red");
        return field;
    }

    private TextField createSettingsTextField(String label, Binder<JsonObject> binder, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = new TextField(label);
        field.setClearButtonVisible(true);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        binder.forField(field).bind(getter, setter);

        return field;
    }

    private IntegerField createSettingsSizeField(String label, Binder<JsonObject> binder) {
        IntegerField field = createSettingsIntField(label, binder);
        field.setTooltipText("Expects a positive integer value. Will be interpreted with the unit 'rem' (~ global font size)");

        return field;
    }

    private IntegerField createSettingsIntField(String label, Binder<JsonObject> binder) {
        IntegerField field = new IntegerField(label);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.setWidth("6rem");
        field.setStepButtonsVisible(true);
        return field;
    }

    public JsonObject getTemplates() {
        JsonObject clone = clone(templates);
        removeEmptyChildren(clone);
        return clone;
    }

    private void removeEmptyChildren(JsonObject container) {
        List<String> keys = Arrays.asList(container.keys());
        for (String key : keys) {
            JsonValue value = container.get(key);
            if (value instanceof JsonArray) {
                JsonArray array = (JsonArray) value;

                for (int i = array.length() - 1; i >= 0; i--) {
                    JsonObject arrayChild = array.getObject(i);
                    if (arrayChild.hasKey(DECLARATIONS)) {
                        JsonObject declarations = arrayChild.getObject(DECLARATIONS);
                        removeEmptyChildren(declarations);
                        if (declarations.keys().length == 0) {
                            array.remove(i);
                        }
                    }
                }

                if (array.length() == 0) {
                    container.remove(key);
                }
            } else if (value instanceof JsonObject) {
                removeEmptyChildren((JsonObject) value);
                if (((JsonObject) value).keys().length == 0) {
                    container.remove(key);
                }
            } else if (value != null) {
                JsonType type = value.getType();
                if (type == JsonType.STRING && StringUtils.trimToNull(value.asString()) == null) {
                    container.remove(key);
                } else if (type == JsonType.NULL) {
                    container.remove(key);
                }

                // TODO implement other types if needed (but atM we just have Strings)
            }
        }
    }

    public void setActiveTemplate(@Nullable String template) {
        templateField.setValue(template);
    }

    public Optional<String> getActiveTemplate() {
        return templateField.getOptionalValue();
    }

    private void applyOverlayPopupCloseWorkaround(Component component) {
        component.getElement().executeJs("this.addEventListener('opened-changed', e => {" +
                                         "if(e.detail.value) {  " +
                                         "    $0.__stayOpen = true;" +
                                         "}" +
                                         "});", this);
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