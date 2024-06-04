package com.vaadin.componentfactory;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class CssRuleElement {
    private final String tagName;
    private final Map<String, Object> declarations = new LinkedHashMap<>();

    protected CssRuleElement(String tagName) {
        this.tagName = tagName;
    }

    public String toCss() {
        String declarationString = getCssDeclarations();

        return getCssSelector() + " {\n" + declarationString + "}\n\n";
    }

    protected String getCssDeclarations() {
        String declarationString = "";
        for (Map.Entry<String, Object> entry : declarations.entrySet()) {
            declarationString += entry.getKey() + ": ";

            Object value = entry.getValue();
            if (value instanceof String) {
                declarationString += "\"" + entry.getValue() + "\"";
            } else {
                declarationString += entry.getValue();
            }

            declarationString += "\n";
        }
        return declarationString;
    }

    protected String getCssSelector() {
        return tagName;
    }

    public void readDeclarationsFromCSs(String css) {
        int start = css.indexOf("{");
        int end = css.indexOf("}", start);

        declarations.clear();

        String[] declarationsArrays = css.substring(start + 1, end).replaceAll("\n", "").split(";");
        for (String declaration : declarationsArrays) {
            String[] split = declaration.split(":");
            if (split.length != 2) {
                throw new IllegalStateException("Invalid declaration " + declaration);
            }

//            declarations.put(split[0]);


        }

    }


}