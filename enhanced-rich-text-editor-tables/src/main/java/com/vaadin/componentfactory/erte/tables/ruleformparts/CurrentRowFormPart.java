package com.vaadin.componentfactory.erte.tables.ruleformparts;

public class CurrentRowFormPart extends AbstractRowFormPart {
    private int selectedRow;

    @Override
    protected String getSelectedIndex() {
        return String.valueOf(selectedRow + 1);
    }

    public void setSelectedRow(int row) {
        if (row < 0) {
            throw new IllegalArgumentException("Row must not be negative");
        }

        if (this.selectedRow != row) {
            this.selectedRow = row;
        }
    }

    public int getSelectedRow() {
        return selectedRow;
    }
}