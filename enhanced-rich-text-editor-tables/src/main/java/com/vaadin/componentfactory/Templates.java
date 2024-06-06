package com.vaadin.componentfactory;

import elemental.json.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class Templates {
    public static final String P_BACKGROUND = "bgColor";
    public static final String P_COLOR = "color";
    public static final String P_WIDTH = "width";
    public static final String P_MIN_WIDTH = "minWidth";
    public static final String P_MAX_WIDTH = "maxWidth";
    public static final String P_BORDER = "border";

    public static final String TABLE = "table";
    public static final String ROWS = "rows";
    public static final String CELLS = "cells";
    public static final String COLUMNS = "cols";

    public static final String CELL_X = "x";
    public static final String CELL_Y = "y";
    public static final String INDEX = "index";
    public static final String LAST = "last";
    public static final String DECLARATIONS = "declarations";

    public static final Pattern TEMPLATE_NAME = Pattern.compile("[A-Za-z][A-Za-z0-9]*");

    private static final Map<String, Set<String>> ALLOWED_PROPERTIES;

    static {
        ALLOWED_PROPERTIES = new HashMap<>();
        ALLOWED_PROPERTIES.put(TABLE, Set.of(P_BACKGROUND, P_COLOR, P_WIDTH, P_MIN_WIDTH, P_MAX_WIDTH, P_BORDER));
        ALLOWED_PROPERTIES.put(ROWS, Set.of(P_BACKGROUND, P_COLOR, P_BORDER));
        ALLOWED_PROPERTIES.put(COLUMNS, Set.of(P_BACKGROUND, P_COLOR, P_WIDTH, P_MIN_WIDTH, P_MAX_WIDTH, P_BORDER));
        ALLOWED_PROPERTIES.put(CELLS, Set.of(P_BACKGROUND, P_COLOR, P_BORDER));
    }

    public static final class CssBuilder {
        private final JsonObject templates;
        private StringBuilder builder;
        private String currentTemplateName;

        public CssBuilder(String json) {
            this(Json.parse(json));
        }

        public CssBuilder(JsonObject templates) {
            this.templates = templates;
        }

        public String toCss() {
            builder = new StringBuilder();
            for (String templateName : templates.keys()) {
                currentTemplateName = templateName;

                JsonObject rules = templates.getObject(templateName);

                for (String rulesKey : rules.keys()) {
                    JsonValue rulesContent = rules.get(rulesKey);
                    switch (rulesKey) {
                        case TABLE:
                            appendTableSelectorPart();
                            parseDeclarations((JsonObject) rulesContent); // table contains is css declarations directly
                            break;
                        case ROWS:
                            parseRows((JsonArray) rulesContent);
                            break;
                        case COLUMNS:
                            parseCols((JsonArray) rulesContent);
                            break;
                        case CELLS:
                            parseCellsArray((JsonArray) rulesContent);
                            break;
                        default:
                            throw new IllegalStateException("Unsupported rules key: " + rulesKey);
                    }


                }


            }
            return builder.toString().trim();
        }

        private void parseRows(JsonArray rowsConfigArray) {
            parseRowsColsArray(rowsConfigArray, " > tr");
        }

        private void parseCols(JsonArray colsConfigArray) {
            parseRowsColsArray(colsConfigArray, " > tr > td");
        }

        private void parseRowsColsArray(JsonArray rowsColsConfigArray, String cssSelector) {

            for (int i = 0; i < rowsColsConfigArray.length(); i++) {
                JsonObject rowsColsConfig = rowsColsConfigArray.getObject(i);

                appendTableSelectorPart();
                builder.append(cssSelector);
                appendIndex(rowsColsConfig);

                parseDeclarations(rowsColsConfig.getObject(DECLARATIONS));
            }
        }

        private void parseCellsArray(JsonArray cellsArray) {

            for (int i = 0; i < cellsArray.length(); i++) {
                JsonObject cellConfig = cellsArray.getObject(i);

                appendTableSelectorPart();
                builder.append(" > ").append("tr");
                appendXY(CELL_X, cellConfig);
                builder.append(" > ").append("td");
                appendXY(CELL_Y, cellConfig);

                parseDeclarations(cellConfig.getObject(DECLARATIONS));
            }
        }

        private void parseDeclarations(JsonObject declarations) {
            builder.append(" {\n");
            for (String property : declarations.keys()) {
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
            switch(propertyKey) {
                case P_BACKGROUND: return "background-color";
                case P_COLOR: return "color";
                case P_WIDTH: return "width";
                case P_MIN_WIDTH: return "min-width";
                case P_MAX_WIDTH: return "max-width";
                case P_BORDER: return "border";
                default: throw new IllegalStateException("Unsupported property key: " + propertyKey);
            }
        }
    }
}