package com.vaadin.componentfactory.erte.tables.ruleformparts;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import elemental.json.JsonObject;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.P_BACKGROUND;
import static com.vaadin.componentfactory.erte.tables.TemplateConstants.P_COLOR;

public abstract class AbstractColFormPart extends AbstractIndexedFormPart {
    private final String title;

    public AbstractColFormPart(String title) {
        this.title = title;
    }

    @Override
    void initForm(Binder<JsonObject> binder) {
        add(createPartTitle(title));
        HorizontalLayout rowLayout = new HorizontalLayout(
                createColorField("S Textfarbe", getter(P_COLOR), setter(P_COLOR)),
                createColorField("S Hintergrundfarbe", getter(P_BACKGROUND), setter(P_BACKGROUND)));

        rowLayout.setAlignItems(Alignment.BASELINE);
        add(rowLayout);
    }
}