package com.vaadin.componentfactory.erte.tables.ruleformparts;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.COLUMNS;

public class CurrentColFormPart extends AbstractColFormPart {
    private int selectedCol;

    public CurrentColFormPart() {
        super("Aktuelle Spalte");
    }

    @Override
    protected int getSelectedIndex() {
        return selectedCol;
    }

    @Override
    protected String getKey() {
        return COLUMNS;
    }

    public void setSelectedColumn(int column) {
        if (column < 0) {
            throw new IllegalArgumentException("Column must not be negative");
        }

        if (this.selectedCol != column) {
            this.selectedCol = column;
        }
    }
}