package com.vaadin.componentfactory.erte.tables.ruleformparts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import elemental.json.JsonObject;

public abstract class RuleFormPart extends VerticalLayout {
    private final Binder<JsonObject> binder;

    public RuleFormPart() {
        this.binder = new Binder<>();

        setPadding(false);
        setSpacing(false);

        addAttachListener(event -> {
            initForm (binder);
            event.unregisterListener();
        });
        addClassName("form-part");
    }

    abstract void initForm(Binder<JsonObject> binder);

    protected TextField createBorderField(String label, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = createTextField(label, getter, setter);
        field.setTooltipText("Expects a valid css border definition, e.g. 1px solid black");
        return field;
    }

    protected TextField createColorField(String label, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = createTextField(label, getter, setter);
        field.setTooltipText("Expects a valid css color definition, e.g. #123456 or red");
        return field;
    }

    protected TextField createTextField(String label, ValueProvider<JsonObject, String> getter, Setter<JsonObject, String> setter) {
        TextField field = new TextField(label);
        field.setClearButtonVisible(true);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        binder.forField(field).bind(getter, setter);

        return field;
    }

    protected IntegerField createSizeField(String label) {
        IntegerField field = createIntegerField(label);
        field.setTooltipText("Expects a positive integer value. Will be interpreted with the unit 'rem' (~ global font size)");

        return field;
    }

    protected IntegerField createIntegerField(String label) {
        IntegerField field = new IntegerField(label);
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.setWidth("6rem");
        field.setStepButtonsVisible(true);
        return field;
    }

    protected static Component createPartTitle(String label) {
        Span section = new Span(label);
        section.getStyle().set("font-weight", "bold").set("margin-top","0.8rem");
        return section;
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
            if (binder.isValid()) {
                listener.valueChanged(event);
            }
        });
    }
}