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

    public TableSelectedEvent(
            EnhancedRichTextEditor source,
            boolean fromClient,
            @EventData("event.detail.selected") boolean selected,
            @EventData("event.detail.cellSelectionActive") boolean cellSelectionActive,
            @EventData("event.detail.template") String template
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