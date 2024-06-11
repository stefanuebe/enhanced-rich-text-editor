package com.vaadin.componentfactory.erte.tables.ruleformparts;

import com.vaadin.flow.data.binder.Binder;
import elemental.json.JsonObject;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.ROWS;

public abstract class AbstractRowFormPart extends AbstractIndexedFormPart {
    private final String title;

    public AbstractRowFormPart() {
        this(null);
    }

    public AbstractRowFormPart(String title) {
        super(true, false);
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
        return ROWS;
    }
}