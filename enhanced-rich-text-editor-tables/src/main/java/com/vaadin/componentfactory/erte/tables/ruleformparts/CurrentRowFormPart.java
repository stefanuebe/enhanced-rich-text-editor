package com.vaadin.componentfactory.erte.tables.ruleformparts;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.COLUMNS;
import static com.vaadin.componentfactory.erte.tables.TemplateConstants.ROWS;

public class CurrentRowFormPart extends AbstractRowFormPart {
    private int selectedRow;

    public CurrentRowFormPart() {
        super("Aktuelle Zeile");
    }

    @Override
    protected int getSelectedIndex() {
        return selectedRow;
    }

    @Override
    protected String getKey() {
        return ROWS;
    }

    public void setSelectedRow(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("Row must not be negative");
        }

        if (this.selectedRow != row) {
            this.selectedRow = row;
        }
    }
}