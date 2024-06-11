package com.vaadin.componentfactory.erte.tables;

import com.vaadin.componentfactory.erte.tables.ruleformparts.*;
import com.vaadin.componentfactory.toolbar.ToolbarDialog;
import com.vaadin.componentfactory.toolbar.ToolbarSwitch;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.DECLARATIONS;

public class TemplateDialog extends ToolbarDialog {

    private final VerticalLayout layout;
    private final ComboBox<String> templateField;
    private final FixedIndexRowFormPart headerRowFormPart;
    private final FixedIndexRowFormPart oddRowsFormPart;
    private final FixedIndexRowFormPart evenRowsFormPart;
    private final FixedIndexRowFormPart footerRowFormPart;
    private JsonObject currentTemplate;
    private JsonObject templates = Json.createObject();
    private TableFormPart tableFormPart;
    private CurrentRowFormPart currentRowFormPart;
    private CurrentColFormPart currentColFormPart;

    private final List<RuleFormPart> parts = new ArrayList<>();
    private SerializableBiConsumer<String, Boolean> templateSelectedCallback;
    private SerializableBiConsumer<JsonObject, Boolean> templatesChangedCallback;

    public TemplateDialog(ToolbarSwitch referencedSwitch) {
        super(referencedSwitch);

        setHeaderTitle("Formatvorlagen");
        Button button = new Button(VaadinIcon.CLOSE.create(), event -> close());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        getHeader().add(button);

        templateField = new ComboBox<>();

        layout = new VerticalLayout(templateField);
        layout.setSpacing(false);

        Details tableDetails = addPartDetails("Tabelle");
        tableFormPart = addPart(new TableFormPart(), tableDetails);

        Details currentRowDetails = addPartDetails("Aktuelle Zeile");
        currentRowFormPart = addPart(new CurrentRowFormPart(), currentRowDetails);

        Details currentColDetails = addPartDetails("Aktuelle Spalte");
        currentColFormPart = addPart(new CurrentColFormPart(), currentColDetails);

        Details fixedRowsDetails = addPartDetails("Spezielle Zeilen", false);
        headerRowFormPart = addPart(new FixedIndexRowFormPart("Kopfzeile", "1"), fixedRowsDetails);
        footerRowFormPart = addPart(new FixedIndexRowFormPart("Fußzeile", "1", true), fixedRowsDetails);
        evenRowsFormPart = addPart(new FixedIndexRowFormPart("Gerade Zeilen", "2n"), fixedRowsDetails);
        oddRowsFormPart = addPart(new FixedIndexRowFormPart("Ungerade Zeilen", "2n+1"), fixedRowsDetails);
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

            if(templateSelectedCallback != null) {
                templateSelectedCallback.accept(value, event.isFromClient());
            }
        });
        setFocusOnOpenTarget(templateField);

        addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                getElement().executeJs("const {left, top, width, height} = $0.getBoundingClientRect();" +
                                       "this.$.overlay.$.overlay.style.position = 'absolute';" +
                                       "this.$.overlay.$.overlay.style.left = left + width + 'px';",
                        getToolbarSwitch());
            }
        });


        // Workaround for https://github.com/vaadin-component-factory/vcf-popup/issues/16
//        getElement().executeJs("this.addEventListener('popup-open-changed', e => {" +
//                               "    if(!e.detail.value && this.__stayOpen) {" +
//                               "        this.opened = true;" +
//                               "        delete this.__stayOpen;" +
//                               "    }" +
//                               "});");
    }

    private Details addPartDetails(String title) {
        return addPartDetails(title, true);
    }

    private Details addPartDetails(String title, boolean initiallyOpened) {
        Details details = new Details(title);
        details.setOpened(initiallyOpened);
        layout.add(details);
        return details;
    }

    private <T extends RuleFormPart> T addPart(T part, Component container) {
        part.addValueChangeListener(event -> {
            if (templatesChangedCallback != null) {
                templatesChangedCallback.accept(getTemplates(), event.isFromClient());
            }
        });
        container.getElement().appendChild(part.getElement());
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

    /**
     * Allows to specifically enable / disable the "current selection" form parts. This might be used, if there
     * is a multiline selection or similar.
     * @param enabled enable or disable
     */
    public void setCurrentPartsEnabled(boolean enabled) {
        currentColFormPart.setEnabled(enabled);
        currentRowFormPart.setEnabled(enabled);
    }

    public void setActiveTemplate(@Nullable String template) {
        templateField.setValue(template);
    }

    public Optional<String> getActiveTemplate() {
        return templateField.getOptionalValue();
    }

    protected void setTemplateSelectedCallback(SerializableBiConsumer<String, Boolean> callback) {
        this.templateSelectedCallback = callback;
    }

    protected void setTemplatesChangedCallback(SerializableBiConsumer<JsonObject, Boolean> callback) {
        this.templatesChangedCallback = callback;
    }

//    private void applyOverlayPopupCloseWorkaround(Component component) {
//        component.getElement().executeJs("this.addEventListener('opened-changed', e => {" +
//                                         "if(e.detail.value) {  " +
//                                         "    $0.__stayOpen = true;" +
//                                         "}" +
//                                         "});", this);
//    }


}