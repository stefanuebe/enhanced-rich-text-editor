package com.vaadin.componentfactory.erte.tables.templates.ruleformparts;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import elemental.json.Json;
import elemental.json.JsonObject;

import static com.vaadin.componentfactory.erte.tables.templates.TemplateJsonConstants.*;

public class TableFormPart extends RuleFormPart {

    @Override
    public void readTemplate(JsonObject template, Binder<JsonObject> binder) {
        JsonObject table = template.getObject(TABLE);
        if (table == null) {
            table = Json.createObject();
            template.put(TABLE, table);
        }
        binder.setBean(table);
    }

    @Override
    void initForm(Binder<JsonObject> binder) {
        HorizontalLayout row = createRow(
                createTextColorField(),
                createBackgroundColorField(),
                createBorderField("Außenrahmen", P_BORDER),
                createBorderField("Zellenrahmen", P_BORDER_CELLS)
        );

        add(row);
    }
}