package com.vaadin.componentfactory.erte.tables.ruleformparts;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.COLUMNS;

public class CurrentColFormPart extends AbstractColFormPart {
    private int selectedCol;

    @Override
    protected String getSelectedIndex() {
        return String.valueOf(selectedCol + 1);
    }


    public void setSelectedColumn(int column) {
        if (column < 0) {
            throw new IllegalArgumentException("Column must not be negative");
        }

        if (this.selectedCol != column) {
            this.selectedCol = column;
        }
    }

    public int getSelectedCol() {
        return selectedCol;
    }
}