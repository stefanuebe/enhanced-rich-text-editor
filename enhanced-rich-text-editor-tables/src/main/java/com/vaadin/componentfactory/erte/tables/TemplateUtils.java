package com.vaadin.componentfactory.erte.tables;

import elemental.json.JsonArray;
import elemental.json.JsonObject;

import static com.vaadin.componentfactory.erte.tables.TemplateConstants.INDEX;
import static com.vaadin.componentfactory.erte.tables.TemplateConstants.LAST;

public class TemplateUtils {
    public static JsonObject searchForIndexedObject(JsonArray array, String index, boolean last) {
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JsonObject object = array.getObject(i);
                if ((!last || (object.hasKey(LAST) && object.getBoolean(LAST))) && index.equals(object.getString(INDEX))) {
                    return object;
                }
            }
        }

        return null;
    }
}