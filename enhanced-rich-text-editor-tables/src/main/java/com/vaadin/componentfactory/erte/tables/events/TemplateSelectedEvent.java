package com.vaadin.componentfactory.erte.tables.events;

import com.vaadin.componentfactory.erte.tables.EnhancedRichTextEditorTables;
import com.vaadin.componentfactory.erte.tables.EnhancedRichTextEditorTablesComponentEvent;

public class TemplateSelectedEvent extends EnhancedRichTextEditorTablesComponentEvent {
    private final String template;

    public TemplateSelectedEvent(EnhancedRichTextEditorTables source, boolean fromClient, String template) {
        super(source, fromClient);
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}