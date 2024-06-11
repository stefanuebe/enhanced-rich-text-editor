package com.vaadin.componentfactory.erte.tables;

import com.vaadin.componentfactory.erte.tables.ruleformparts.CurrentColFormPart;
import com.vaadin.componentfactory.erte.tables.ruleformparts.CurrentRowFormPart;
import com.vaadin.componentfactory.erte.tables.ruleformparts.RuleFormPart;
import com.vaadin.componentfactory.erte.tables.ruleformparts.TableFormPart;
import com.vaadin.componentfactory.toolbar.ToolbarPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSwitch;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import elemental.json.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.DECLARATIONS;

public class TemplatePopup extends ToolbarPopup {

    private final VerticalLayout layout;
    private final ComboBox<String> templateField;
    private JsonObject currentTemplate;
    private JsonObject templates = Json.createObject();
    private TableFormPart tableFormPart;
    private CurrentRowFormPart currentRowFormPart;
    private CurrentColFormPart currentColFormPart;

    private final List<RuleFormPart> parts = new ArrayList<>();

    public TemplatePopup(ToolbarSwitch referencedSwitch) {
        super(referencedSwitch);
        setCloseOnClick(false);

        templateField = new ComboBox<>();
        applyOverlayPopupCloseWorkaround(templateField);

        layout = new VerticalLayout(templateField);
        layout.setSpacing(false);
        tableFormPart = addPart(new TableFormPart());
        currentRowFormPart = addPart(new CurrentRowFormPart());
        currentColFormPart = addPart(new CurrentColFormPart());
        add(layout);

        templateField.addValueChangeListener(event -> {
            String value = StringUtils.trimToNull(event.getValue());
            if (value != null && this.templates.hasKey(value)) {
                currentTemplate = this.templates.getObject(value);
            } else {
                currentTemplate = null;
            }

            if (currentTemplate != null) {
                parts.forEach(ruleFormPart -> ruleFormPart.readTemplate(currentTemplate));
            }

            fireEvent(new TemplateSelectedEvent(this, event.isFromClient(), value));
        });
        setFocusOnOpenTarget(templateField);


        // Workaround for https://github.com/vaadin-component-factory/vcf-popup/issues/16
        getElement().executeJs("this.addEventListener('popup-open-changed', e => {" +
                               "    if(!e.detail.value && this.__stayOpen) {" +
                               "        this.opened = true;" +
                               "        delete this.__stayOpen;" +
                               "    }" +
                               "});");
    }

    private <T extends RuleFormPart> T addPart(T part) {
        part.addValueChangeListener(event -> fireEvent(new TemplatesChangedEvent(this, event.isFromClient())));
        layout.add(part);
        parts.add(part);
        return part;
    }

    public JsonObject getTemplates() {
        JsonObject clone = clone(templates);
        removeEmptyChildren(clone);
        return clone;
    }

    private static JsonObject clone(JsonObject objectToClone) {
        return Json.parse(objectToClone.toJson());
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
        currentRowFormPart.setSelectedRow(row); // maybe combine these two calls when form part stores the template
        currentRowFormPart.readTemplate(currentTemplate);
    }

    public void setSelectedColumn(int col) {
        currentColFormPart.setSelectedColumn(col); // maybe combine these two calls when form part stores the template
        currentColFormPart.readTemplate(currentTemplate);
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