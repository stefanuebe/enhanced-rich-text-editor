package com.vaadin.componentfactory.erte.tables;

import com.vaadin.componentfactory.EnhancedRichTextEditor;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import org.apache.commons.lang3.StringUtils;

@DomEvent("table-selected")
public class TableSelectedEvent extends ComponentEvent<EnhancedRichTextEditor> {
    private final boolean selected;

    private final boolean cellSelectionActive;
    private final String template;
    private final Integer rowIndex;
    private final Integer colIndex;

    public TableSelectedEvent(
            EnhancedRichTextEditor source,
            boolean fromClient,
            @EventData("event.detail.selected") boolean selected,
            @EventData("event.detail.cellSelectionActive") boolean cellSelectionActive,
            @EventData("event.detail.template") String template,
            @EventData("event.detail.rowIndex") Integer rowIndex,
            @EventData("event.detail.colIndex") Integer colIndex
    ) {

        super(source, fromClient);

        if (StringUtils.isNotBlank(template) && !template.matches("[A-Za-z0-9\\-]+")) {
            throw new IllegalArgumentException("Illegal template name: " + template);
        }

        if (rowIndex != null && rowIndex < 0) {
            throw new IllegalArgumentException("Row index must not be negative");
        }
        if (colIndex != null && colIndex < 0) {
            throw new IllegalArgumentException("Col index must not be negative");
        }

        this.selected = selected;
        this.cellSelectionActive = cellSelectionActive;
        this.template = StringUtils.trimToNull(template);
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
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

    public Integer getColIndex() {
        return colIndex;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }
}