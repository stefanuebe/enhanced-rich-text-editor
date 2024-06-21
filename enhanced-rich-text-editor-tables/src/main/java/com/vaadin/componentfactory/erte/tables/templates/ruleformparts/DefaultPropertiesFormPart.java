package com.vaadin.componentfactory.erte.tables.templates.ruleformparts;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import elemental.json.JsonObject;

public abstract class DefaultPropertiesFormPart extends RuleFormPart {

    private final boolean hasHeight;
    private final boolean hasWidth;

    public DefaultPropertiesFormPart(boolean hasHeight, boolean hasWidth) {
        this.hasHeight = hasHeight;
        this.hasWidth = hasWidth;
    }

    @Override
    void initForm(Binder<JsonObject> binder) {
        HorizontalLayout row = createRow(
                createTextColorField(),
                createBackgroundColorField(),
                createBorderField()
        );
        if (hasWidth) {
            row.add(createWidthField());
        }
        if (hasHeight) {
            row.add(createHeightField());
        }

        add(row);
    }
}