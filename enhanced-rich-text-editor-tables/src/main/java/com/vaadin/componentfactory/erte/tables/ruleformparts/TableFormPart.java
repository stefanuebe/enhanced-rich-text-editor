package com.vaadin.componentfactory.erte.tables.ruleformparts;

import com.vaadin.flow.data.binder.Binder;
import elemental.json.Json;
import elemental.json.JsonObject;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.TABLE;

public class TableFormPart extends RuleFormPart {
    @Override
    void initForm(Binder<JsonObject> binder) {
        add(createRow(
                createWidthField(),
                createTextColorField(),
                createBackgroundColorField(),
                createBorderField()
        ));
    }

    @Override
    public void readTemplate(JsonObject template, Binder<JsonObject> binder) {
        JsonObject table = template.getObject(TABLE);
        if (table == null) {
            table = Json.createObject();
            template.put(TABLE, table);
        }
        binder.setBean(table);
    }
}