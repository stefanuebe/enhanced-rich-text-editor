package com.vaadin.componentfactory.erte.tables;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import java.util.Objects;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.*;

/**
 * Parses an Tables addon Css JSON Template and creates a css stylesheet out of it.
 */
public final class TemplateParser {
    private final JsonObject templates;
    private StringBuilder builder;
    private String currentTemplateName;

    public static String parse(JsonObject templates) {
        return new TemplateParser(templates).toCss();
    }

    public static String parse(String templateJson) {
        return new TemplateParser(Json.parse(templateJson)).toCss();
    }

    public TemplateParser(JsonObject templates) {
        this.templates = templates;
    }

    public String toCss() {
        builder = new StringBuilder();
        for (String templateName : templates.keys()) {
            if (!isValidTemplateName(templateName)) {
                throw new IllegalStateException(templateName + " is not a legal template name. It must match " + PATTERN_TEMPLATE_NAME.pattern());
            }

            currentTemplateName = templateName;

            JsonObject rules = templates.getObject(templateName);

            // check the rules for the different selectors
            // we could do that in a loop, but by doing it manually we can also
            // specify the order of css rules in the resulting sheet (which has an effect on the resulting look)
            if (rules.hasKey(TABLE)) {
                parseTable(rules.getObject(TABLE));
            }

            // cols before rows makes the rows the overriding style when styles "overlap"
            if (rules.hasKey(COLUMNS)) {
                parseCols(rules.getArray(COLUMNS));
            }

            if (rules.hasKey(ROWS)) {
                parseRows(rules.getArray(ROWS));
            }

            if (rules.hasKey(CELLS)) {
                parseCells(rules.getArray(CELLS));
            }
        }
        return builder.toString().trim();
    }

    private void parseTable(JsonObject rules) {
        appendTableSelectorPart();
        parseDeclarations(TABLE, rules); // table contains is css declarations directly
    }

    private void parseRows(JsonArray rowsConfigArray) {

        for (int i = 0; i < rowsConfigArray.length(); i++) {
            JsonObject rowsColsConfig = rowsConfigArray.getObject(i);

            appendTableSelectorPart();
            builder.append(" > tr");
            appendIndex(rowsColsConfig);
            builder.append(" > td"); // important to allow rows "override" columns

            parseDeclarations(ROWS, rowsColsConfig.getObject(DECLARATIONS));
        }
    }

    private void parseCols(JsonArray colsConfigArray) {

        for (int i = 0; i < colsConfigArray.length(); i++) {
            JsonObject rowsColsConfig = colsConfigArray.getObject(i);

            appendTableSelectorPart();
            builder.append(" > tr > td");
            appendIndex(rowsColsConfig);

            parseDeclarations(COLUMNS, rowsColsConfig.getObject(DECLARATIONS));
        }
    }

    private void parseCells(JsonArray cellsArray) {

        for (int i = 0; i < cellsArray.length(); i++) {
            JsonObject cellConfig = cellsArray.getObject(i);

            appendTableSelectorPart();
            builder.append(" > ").append("tr");
            appendXY(CELL_X, cellConfig);
            builder.append(" > ").append("td");
            appendXY(CELL_Y, cellConfig);

            parseDeclarations(CELLS, cellConfig.getObject(DECLARATIONS));
        }
    }

    private void parseDeclarations(String ruleKey, JsonObject declarations) {
        builder.append(" {\n");
        for (String property : declarations.keys()) {

            if (!ALLOWED_PROPERTIES.get(ruleKey).contains(property)) {
                throw new IllegalStateException("Unsupported property " + property + " for type " + ruleKey);
            }

            String value = Objects.requireNonNull(declarations.getString(property), "null properties are not allowed!");

            builder.append("    ")
                    .append(mapToCss(property))
                    .append(": ")
                    .append(value)
                    .append(";\n");
        }
        builder.append("}\n\n");
    }

    private void appendTableSelectorPart() {
        builder.append("table.").append(currentTemplateName);
    }

    private void appendIndex(JsonObject declarationDef) {
        if (declarationDef.hasKey(INDEX)) {
            String nth = declarationDef.hasKey(LAST) && declarationDef.getBoolean(LAST)
                    ? "nth-last-of-type"
                    : "nth-of-type";

            // since index can also be something like "2n + 1" (odd children) we interprete it as string
            builder.append(":")
                    .append(nth)
                    .append("(")
                    .append(declarationDef.getString(INDEX))
                    .append(")");
        }
    }

    private void appendXY(String key, JsonObject declarationDef) {
        if (declarationDef.hasKey(key)) {
            builder.append(":")
                    .append("nth-of-type")
                    .append("(")
                    .append((int) declarationDef.getNumber(key)) // we have concrete coordinates, so always number
                    .append(")");
        }
    }

    private static String mapToCss(String propertyKey) {
        switch (propertyKey) {
            case P_BACKGROUND:
                return "background-color";
            case P_COLOR:
                return "color";
            case P_WIDTH:
                return "width";
            case P_MIN_WIDTH:
                return "min-width";
            case P_MAX_WIDTH:
                return "max-width";
            case P_BORDER:
                return "border";
            default:
                throw new IllegalStateException("Unsupported property key: " + propertyKey);
        }
    }

    public static boolean isValidTemplateName(String templateName) {
        return PATTERN_TEMPLATE_NAME.asMatchPredicate().test(templateName);
    }

    // for later use maybe?
    public static boolean isValidPropertyValue(String property, String value) {
        if (value.contains(":") || value.contains(";") || value.contains("{") || value.contains("}")) {
            return false;
        }

//        switch (property) {
//            case P_BACKGROUND:
//            case P_COLOR:
//                return isValidColor(value);
//            case P_WIDTH:
//            case P_MIN_WIDTH:
//            case P_MAX_WIDTH:
//                return isValidSize(value);
//                case P_BORDER:
//        }
//
//        return false;

        return true;
    }

    // for later use maybe?
    private static boolean isValidSize(String value) {
        // TODO implement
        return true;
    }

    // for later use maybe?
    public static boolean isValidColor(String color) {
        return PATTERN_P_COLOR_1.asMatchPredicate().test(color)
               || PATTERN_P_COLOR_2.asMatchPredicate().test(color)
               || PATTERN_P_COLOR_3.asMatchPredicate().test(color)
               || PATTERN_P_COLOR_4.asMatchPredicate().test(color);
    }
}