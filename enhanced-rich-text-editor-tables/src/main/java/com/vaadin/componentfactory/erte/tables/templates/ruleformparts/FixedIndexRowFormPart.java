package com.vaadin.componentfactory.erte.tables.templates.ruleformparts;

public class FixedIndexRowFormPart extends AbstractRowFormPart {
    private final String index;
    private final boolean fromBottom;

    public FixedIndexRowFormPart(String title, String index) {
        this(title, index, false);
    }

    public FixedIndexRowFormPart(String title, String index, boolean fromBottom) {
        super(title);
        this.index = index;
        this.fromBottom = fromBottom;
    }

    @Override
    protected String getSelectedIndex() {
        return index;
    }

    @Override
    protected boolean isIndexFromBottom() {
        return fromBottom;
    }
}