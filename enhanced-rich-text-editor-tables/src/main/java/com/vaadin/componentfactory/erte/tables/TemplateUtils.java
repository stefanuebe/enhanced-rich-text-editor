package com.vaadin.componentfactory.erte.tables;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.INDEX;
import static com.vaadin.componentfactory.erte.tables.TemplateConstants.FROM_BOTTOM;

public class TemplateUtils {
    public static JsonObject searchForIndexedObject(JsonArray array, String index, boolean indexFromBottom) {
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JsonObject object = array.getObject(i);
                if ((!indexFromBottom || (object.hasKey(FROM_BOTTOM) && object.getBoolean(FROM_BOTTOM))) && index.equals(object.getString(INDEX))) {
                    return object;
                }
            }
        }

        return null;
    }
}