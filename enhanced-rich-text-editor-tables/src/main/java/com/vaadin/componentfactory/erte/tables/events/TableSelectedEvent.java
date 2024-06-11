package com.vaadin.componentfactory.erte.tables.events;

import com.vaadin.componentfactory.erte.tables.EnhancedRichTextEditorTables;
import com.vaadin.componentfactory.erte.tables.EnhancedRichTextEditorTablesComponentEvent;
import org.apache.commons.lang3.StringUtils;

public class TableSelectedEvent extends EnhancedRichTextEditorTablesComponentEvent {
    private final boolean selected;

    private final boolean cellSelectionActive;
    private final String template;

    public TableSelectedEvent(
            EnhancedRichTextEditorTables source,
            boolean fromClient,
            boolean selected,
            boolean cellSelectionActive,
            String template
    ) {
        super(source, fromClient);

        if (StringUtils.isNotBlank(template) && !template.matches("[A-Za-z0-9\\-]+")) {
            throw new IllegalArgumentException("Illegal template name: " + template);
        }

        this.selected = selected;
        this.cellSelectionActive = cellSelectionActive;
        this.template = StringUtils.trimToNull(template);
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isCellSelectionActive() {
        return cellSelectionActive;
    }

    /**
     * The template of the current selected table or null, if none is selected or the table has no template
     * assigned.
     *
     * @return template
     */
    public String getTemplate() {
        return template;
    }

}