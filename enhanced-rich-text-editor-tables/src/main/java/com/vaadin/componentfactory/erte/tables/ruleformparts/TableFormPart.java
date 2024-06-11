package com.vaadin.componentfactory.erte.tables.ruleformparts;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import elemental.json.Json;
import elemental.json.JsonObject;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.P_COLOR;
import static com.vaadin.componentfactory.erte.tables.TemplateConstants.TABLE;

public class TableFormPart extends RuleFormPart {
    @Override
    void initForm(Binder<JsonObject> binder) {
        add(createPartTitle("Tabelle"));
        add(new HorizontalLayout(createColorField("T Textfarbe", getter(P_COLOR), setter(P_COLOR))));
    }

    @Override
    public void readTemplate(JsonObject template, Binder<JsonObject> binder) {
        JsonObject table = template.getObject(TABLE);
        if (table == null) {
            table = Json.createObject();
        }
        binder.setBean(table);
    }
}