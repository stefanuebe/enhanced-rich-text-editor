package com.vaadin.componentfactory.erte.tables.ruleformparts;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import elemental.json.JsonObject;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.*;

public abstract class AbstractColFormPart extends AbstractIndexedFormPart {
    private final String title;

    public AbstractColFormPart() {
        this(null);
    }

    public AbstractColFormPart(String title) {
        super(false, true);
        this.title = title;
    }

    @Override
    void initForm(Binder<JsonObject> binder) {
        if (title != null) {
            add(createPartTitle(title));
        }

        super.initForm(binder);
    }


    @Override
    protected String getKey() {
        return COLUMNS;
    }
}