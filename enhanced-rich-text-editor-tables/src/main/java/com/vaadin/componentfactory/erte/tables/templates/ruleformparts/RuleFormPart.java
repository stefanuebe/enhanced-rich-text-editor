package com.vaadin.componentfactory.erte.tables.templates.ruleformparts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import static com.vaadin.componentfactory.erte.tables.templates.TemplateJsonConstants.*;

public abstract class RuleFormPart extends VerticalLayout {
    private final Binder<JsonObject> binder;

    public RuleFormPart() {
        this.binder = new Binder<>();

        setPadding(false);

        addAttachListener(event -> {
            initForm (binder);
            event.unregisterListener();
        });
        addClassName("form-part");
    }

    abstract void initForm(Binder<JsonObject> binder);

    protected TextField createBorderField(String label, String key) {
        TextField field = createTextField(label, key);
        field.setTooltipText("Expects a valid css border definition, e.g. 1px solid black");
        return field;
    }

    protected TextField createBorderField() {
        return createBorderField("Rahmen", P_BORDER);
    }

    protected TextField createTextColorField() {
        return createColorField("Textfarbe", P_COLOR);
    }

    protected TextField createBackgroundColorField() {
        return createColorField("Hintergrundfarbe", P_BACKGROUND);
    }

    protected TextField createColorField(String label, String key) {
        TextField field = createTextField(label, key);
        field.setTooltipText("Expects a valid css color definition, e.g. #123456 or red");
        return field;
    }

    protected TextField createTextField(String label, String key) {
        TextField field = new TextField(label);
        field.setClearButtonVisible(true);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        binder.forField(field).bind(getter(key), setter(key));

        return field;
    }

    protected IntegerField createSizeField(String label, String key) {
        IntegerField field = createIntegerField(label, key);
        field.setMin(1);
        field.setTooltipText("Expects a positive integer value. Will be interpreted with the unit 'rem' (~ global font size)");

        return field;
    }

    protected IntegerField createIntegerField(String label, String key) {
        IntegerField field = new IntegerField(label);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.setWidth("6rem");
        field.setStepButtonsVisible(true);
        binder.forField(field)
                .withValidator(i -> i == null || i >= 1, "Wert muss größer gleich 1 sein")
                .withConverter(
                        i -> i == null ? "" : i + "rem",
                        s -> StringUtils.trimToNull(s) != null ? Integer.valueOf(s.replace("rem", "")) : null
                )
                .bind(getter(key), setter(key));

        return field;
    }

    protected static Component createPartTitle(String label) {
        Span section = new Span(label);
        section.getStyle().set("font-weight", "bold").set("margin-top","0.8rem");
        return section;
    }

    protected static HorizontalLayout createRow(Component... components) {
        HorizontalLayout row = new HorizontalLayout(components);
        row.setAlignItems(Alignment.CENTER);
        row.getStyle().set("flex-wrap", "wrap");
        return row;
    }

    protected IntegerField createWidthField() {
        return createSizeField("Breite", P_WIDTH);
    }
    protected IntegerField createHeightField() {
        return createSizeField("Höhe", P_HEIGHT);
    }

    protected static ValueProvider<JsonObject, String> getter(String key) {
        return jsonObject -> jsonObject.hasKey(key) ? jsonObject.getString(key) : null;
    }

    protected static Setter<JsonObject, String> setter(String key) {
        return (jsonObject1, s) -> jsonObject1.put(key, s);
    }

    public void readTemplate(JsonObject template) {
        readTemplate(template, binder);
    }

    protected abstract void readTemplate(JsonObject template, Binder<JsonObject> binder);

    public void setBean(JsonObject object) {
        binder.setBean(object);
    }

    public JsonObject getBean() {
        return binder.getBean();
    }

    public Registration addValueChangeListener(HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<?>> listener) {
        return binder.addValueChangeListener(event -> {
            if (binder.validate().isOk()) {
                listener.valueChanged(event);
            }
        });
    }
}