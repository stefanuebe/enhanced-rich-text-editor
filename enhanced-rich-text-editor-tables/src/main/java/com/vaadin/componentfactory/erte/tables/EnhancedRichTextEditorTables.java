package com.vaadin.componentfactory.erte.tables;

import com.vaadin.componentfactory.EnhancedRichTextEditor;
import com.vaadin.componentfactory.erte.tables.templates.TemplateSelectedEvent;
import com.vaadin.componentfactory.erte.tables.templates.TemplatesChangedEvent;
import com.vaadin.componentfactory.erte.tables.templates.TemplateDialog;
import com.vaadin.componentfactory.erte.tables.templates.TemplateParser;
import com.vaadin.componentfactory.toolbar.ToolbarPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSelectPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSwitch;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import javax.annotation.Nullable;
import java.util.Objects;

@NpmPackage(value = "quill-delta", version = "5.1.0")
@JsModule("./src/erte-table/connector.js")
@CssImport(value = "./src/erte-table/css/erte-shadow.css", themeFor = "vcf-enhanced-rich-text-editor")
@CssImport(value = "./src/erte-table/css/toolbar.css")
public class EnhancedRichTextEditorTables {

    private static final String SCRIPTS = "window.Vaadin.Flow._enhanced_rich_text_editor.";
    private static final String SCRIPTS_TABLE = "window.Vaadin.Flow._enhanced_rich_text_editor.tables.";

    private final EnhancedRichTextEditor rte;
    private TemplateDialog stylesPopup;

    public EnhancedRichTextEditorTables(EnhancedRichTextEditor rte) {
        this.rte = rte;

        Element element = rte.getElement();
        element.addEventListener("table-selected", event -> {
                    JsonObject eventData = event.getEventData();
                    fireEvent(new TableSelectedEvent(
                            this,
                            true,
                            eventData.getBoolean("event.detail.selected"),
                            eventData.getBoolean("event.detail.cellSelectionActive"),
                            eventData.hasKey("event.detail.template") ? eventData.getString("event.detail.template") : null
                    ));
                })
                .addEventData("event.detail.selected")
                .addEventData("event.detail.cellSelectionActive")
                .addEventData("event.detail.template");

        element.addEventListener("table-cell-changed", event -> {
                    JsonObject eventData = event.getEventData();
                    fireEvent(new TableCellChangedEvent(
                            this,
                            true,
                            toInteger(eventData, "event.detail.rowIndex"),
                            toInteger(eventData, "event.detail.colIndex"),
                            toInteger(eventData, "event.detail.oldRowIndex"),
                            toInteger(eventData, "event.detail.oldColIndex")
                    ));
                })
                .addEventData("event.detail.rowIndex")
                .addEventData("event.detail.oldRowIndex")
                .addEventData("event.detail.colIndex")
                .addEventData("event.detail.oldColIndex");
    }

    private Integer toInteger(JsonObject object, String key) {
        return object.hasKey(key) ? Double.valueOf(object.getNumber(key)).intValue() : null;
    }

    public static EnhancedRichTextEditorTables enable(EnhancedRichTextEditor rte) {
        EnhancedRichTextEditorTables tables = new EnhancedRichTextEditorTables(rte);
        tables.initToolbarTable();

        return tables;
    }

    public void initToolbarTable() {
        // insert new table
        IntegerField rows = createTableInsertNumberField("Zeilen", "Anzahl Zeilen für die zu erstellende Tabelle.");

        IntegerField cols = createTableInsertNumberField("Spalten", "Anzahl Spalten für die zu erstellende Tabelle.");

        Button add = new Button(VaadinIcon.PLUS.create(), event -> insertTableAtCurrentPosition(rows.getValue(), cols.getValue()));
        add.setTooltipText("Tabelle einfügen");

        ToolbarSwitch insertButton = new ToolbarSwitch(VaadinIcon.TABLE, VaadinIcon.PLUS);
        insertButton.setTooltipText("Neue Tabelle einfügen");

        ToolbarPopup insertPopup = ToolbarPopup.horizontal(insertButton, rows, new Span("x"), cols, add);
        insertPopup.setFocusOnOpenTarget(rows);
        add.addClickListener(event -> insertPopup.setOpened(false));

        ToolbarSwitch settingsButton = new ToolbarSwitch(VaadinIcon.TABLE, VaadinIcon.TOOLS);
        settingsButton.setEnabled(false);

        ToolbarSelectPopup selectPopup = new ToolbarSelectPopup(settingsButton);
        selectPopup.addItem("Zeile oberhalb einfügen", event -> executeTableRowAction("append-row-above"));
        selectPopup.addItem("Zeile unterhalb einfügen", event -> executeTableRowAction("append-row-below"));
        selectPopup.addItem("Zeile entfernen", event -> executeTableRowAction("remove-row"));
        selectPopup.add(new Hr());
        selectPopup.addItem("Spalte links einfügen", event -> executeTableColumnAction("append-col-before"));
        selectPopup.addItem("Spalte rechts einfügen", event -> executeTableColumnAction("append-col-after"));
        selectPopup.addItem("Spalte entfernen", event -> executeTableColumnAction("remove-col"));

        selectPopup.add(new Hr());
        MenuItem mergeCells = selectPopup.addItem("Zellen zusammenführen", event -> executeTableAction( "merge-selection"));

        selectPopup.addItem("Zellen trennen", event -> executeTableAction("split-cell"));

        selectPopup.add(new Hr());
        selectPopup.addItem("Tabelle entfernen", event -> executeTableAction("remove-table"));

        ToolbarSwitch stylesButton = new ToolbarSwitch(VaadinIcon.TABLE, VaadinIcon.EYE);
        stylesButton.setEnabled(false);
        stylesPopup = new TemplateDialog(stylesButton);

        addTableSelectedListener(event -> {
            insertButton.setEnabled(!event.isSelected());
            settingsButton.setEnabled(event.isSelected());
            stylesButton.setEnabled(event.isSelected());

            boolean cellSelectionActive = event.isCellSelectionActive();
            mergeCells.setEnabled(cellSelectionActive);

            // update the styles popup with the selected table's template
            stylesPopup.setActiveTemplate(event.getTemplate());
            stylesPopup.setCurrentPartsEnabled(!cellSelectionActive);
        });

        addTableCellChangedListener(event -> {
            if (event.getRowIndex() != null) {
                stylesPopup.setSelectedRow(event.getRowIndex());
            }
            if (event.getColIndex() != null) {
                stylesPopup.setSelectedColumn(event.getColIndex());
            }
        });

        stylesPopup.setTemplateSelectedCallback((template, fromClient) -> setTemplateForCurrentTable(template, fromClient));
        stylesPopup.setTemplatesChangedCallback((templates, fromClient) -> {
            try {
                String string = TemplateParser.parse(templates);
                setClientSideStyles(string);

                fireEvent(new TemplatesChangedEvent(this, true, templates, string));

            } catch (Exception e) {
                // TODO add error handler or smth.
                e.printStackTrace();

                Notification
                        .show("Could not parse changes from template popup. Please check your inputs and inform the admin.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        rte.addCustomToolbarComponents(insertButton, settingsButton, stylesButton);
    }

    /**
     * Sets the style templates to be used for this instance. These templates will be converted to css and
     * applied to the client side to modify the tables' appearance.
     * @param templates templates json object.
     */
    public void setTemplates(JsonObject templates) {
        if (stylesPopup != null) {
            stylesPopup.setTemplates(templates);
        }
        String cssString = TemplateParser.parse(templates);
        setClientSideStyles(cssString);
        fireEvent(new TemplatesChangedEvent(this, false, templates, cssString));
    }

    /**
     * Returns the current templates. Only available when the style popup has been activated.
     * @return templates json object or null
     */
    public JsonObject getTemplates() {
        return stylesPopup != null ? stylesPopup.getTemplates() : null;
    }

    /**
     * Returns the current templates as a css string. Only available when the style popup has been activated.
     * @return css string or null
     */
    public String getTemplatesAsCssString() {
        JsonObject templates = getTemplates();
        return templates != null ? TemplateParser.parse(templates) : null;
    }

    /**
     * Interpretes the given string as css (without any additional parsing or escaping!) and sets it
     * as the client side style for tables in the RTE. This will override any internal set styles and might
     * be overriden itself, when the styles popup is used together with templates.
     * @param cssString css string
     */
    public void setClientSideStyles(String cssString) {
        rte.getElement().executeJs(SCRIPTS_TABLE + "_setStyles(this, $0)", cssString);
    }

    public void insertTableAtCurrentPosition(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Zeile und Spalte müssen jeweils größer 0 sein!");
        }

        rte.getElement().executeJs(SCRIPTS_TABLE+ "insert(this, $0, $1)", rows, cols);
    }

    public void setTemplateForCurrentTable(@Nullable String template) {
        setTemplateForCurrentTable(template, false);
    }

    private void setTemplateForCurrentTable(@Nullable String template, boolean fromClient) {
        rte.getElement().executeJs(SCRIPTS_TABLE + "setTemplate(this, $0)", template);

        fireEvent(new TemplateSelectedEvent(this, fromClient, template));
    }

    public void executeTableAction(String action) {
        rte.getElement().executeJs(SCRIPTS_TABLE+ "action(this, $0)", action);
    }

    public void executeTableRowAction(String action) {
        executeTableAction(action);
        if (action.contains("remove")) {
            stylesPopup.updateRowIndexesOnRemove();
        } else {
            stylesPopup.updateRowIndexesOnAdd(action.contains("above"));
        }
    }

    public void executeTableColumnAction(String action) {
        executeTableAction(action);
        if (action.contains("remove")) {
            stylesPopup.updateColIndexesOnRemove();
        } else {
            stylesPopup.updateColIndexesOnAdd(action.contains("before"));
        }
    }

    private IntegerField createTableInsertNumberField(String placeholder, String tooltip) {
        IntegerField field = new IntegerField();
        field.setValue(1);
        field.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                event.getSource().setValue(1);
            }
        });

        field.setMin(1);
        field.setMax(10);
        field.setAutoselect(true);

        field.setStepButtonsVisible(true);
        field.setPlaceholder(placeholder);
        field.setTooltipText(tooltip);

        return field;
    }

    private void fireEvent(EnhancedRichTextEditorTablesComponentEvent event) {
        ComponentUtil.fireEvent(rte, event);
    }

    private <T extends ComponentEvent<EnhancedRichTextEditor>> Registration addListener(Class<T> type, ComponentEventListener<T> listener) {
        return ComponentUtil.addListener(rte, type, listener);
    }

    public Registration addTemplatesChangedListener(ComponentEventListener<TemplatesChangedEvent> listener) {
        return addListener(TemplatesChangedEvent.class, listener);
    }

    public Registration addTemplateSelectedListener(ComponentEventListener<TemplateSelectedEvent> listener) {
        return addListener(TemplateSelectedEvent.class, listener);
    }

    public Registration addTableSelectedListener(ComponentEventListener<TableSelectedEvent> listener) {
        return addListener(TableSelectedEvent.class, listener);
    }

    public Registration addTableCellChangedListener(ComponentEventListener<TableCellChangedEvent> listener) {
        return addListener(TableCellChangedEvent.class, listener);
    }

    public EnhancedRichTextEditor getRte() {
        return rte;
    }
}