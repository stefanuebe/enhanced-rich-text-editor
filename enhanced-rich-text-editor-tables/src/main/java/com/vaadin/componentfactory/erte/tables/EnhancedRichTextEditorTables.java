package com.vaadin.componentfactory.erte.tables;

import com.vaadin.componentfactory.EnhancedRichTextEditor;
import com.vaadin.componentfactory.toolbar.ToolbarPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSelectPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSwitch;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
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
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
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

    private final List<SerializableConsumer<TemplateSelectedEvent>> templateSelectedListeners = new LinkedList<>();
    private final List<SerializableConsumer<TemplatesChangedEvent>> templateChangedListeners = new LinkedList<>();

    public EnhancedRichTextEditorTables(EnhancedRichTextEditor rte) {
        this.rte = rte;
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

        ComponentUtil.addListener(rte, TableSelectedEvent.class, event -> {
            insertButton.setEnabled(!event.isSelected());
            settingsButton.setEnabled(event.isSelected());
            stylesButton.setEnabled(event.isSelected());

            boolean cellSelectionActive = event.isCellSelectionActive();
            mergeCells.setEnabled(cellSelectionActive);

            // update the styles popup with the selected table's template
            stylesPopup.setActiveTemplate(event.getTemplate());
            if (event.getRowIndex() != null) {
                stylesPopup.setSelectedRow(event.getRowIndex());
            }
            if (event.getColIndex() != null) {
                stylesPopup.setSelectedColumn(event.getColIndex());
            }
            stylesPopup.setCurrentPartsEnabled(!cellSelectionActive);
        });

        stylesPopup.setTemplateSelectedCallback((template, fromClient) -> setTemplateForCurrentTable(template, fromClient));
        stylesPopup.setTemplatesChangedCallback((templates, fromClient) -> {
            try {
                String string = TemplateParser.parse(templates);
                setClientSideStyles(string);

                templateChangedListeners.forEach(c -> c.accept(new TemplatesChangedEvent(this, true, templates, string)));


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

    public void setTemplates(JsonObject jsonObject) {
        // TODO allow setting of templates without enabled styles popup
        Objects.requireNonNull(stylesPopup).setTemplates(jsonObject);
        String cssString = TemplateParser.parse(jsonObject);
        setClientSideStyles(cssString);
        templateChangedListeners.forEach(c -> c.accept(new TemplatesChangedEvent(this, false, jsonObject, cssString)));
    }

    public JsonObject getTemplates() {
        return stylesPopup.getTemplates();
    }

    public String getTemplatesAsCssString() {
        return TemplateParser.parse(getTemplates());
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

        templateSelectedListeners.forEach(c -> c.accept(new TemplateSelectedEvent(this, fromClient, template)));
    }

    public void executeTableAction(String action) {
        rte.getElement().executeJs(SCRIPTS_TABLE+ "action(this, $0)", action);
    }

    public void executeTableRowAction(String action) {
        executeTableAction(action);
        Notification.show("TBD check for row update");
    }

    public void executeTableColumnAction(String action) {
        executeTableAction(action);
        Notification.show("TBD check for col update");
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

    public Registration addTemplatesChangedListener(SerializableConsumer<TemplatesChangedEvent> listener) {
        return Registration.addAndRemove(templateChangedListeners, listener);
    }

    public Registration addTemplateSelectedListener(SerializableConsumer<TemplateSelectedEvent> listener) {
        return Registration.addAndRemove(templateSelectedListeners, listener);
    }

    public static class TemplateSelectedEvent extends EventObject {
        private final boolean fromClient;
        private final String template;

        public TemplateSelectedEvent(EnhancedRichTextEditorTables source, boolean fromClient, String template) {
            super(source);
            this.fromClient = fromClient;
            this.template = template;
        }

        public String getTemplate() {
            return template;
        }

        public boolean isFromClient() {
            return fromClient;
        }

        public EnhancedRichTextEditorTables getSource() {
            return (EnhancedRichTextEditorTables) super.getSource();
        }
    }

    public static class TemplatesChangedEvent extends EventObject {
        private final boolean fromClient;
        private final JsonObject templates;
        private final String cssString;

        public TemplatesChangedEvent(EnhancedRichTextEditorTables source, boolean fromClient, JsonObject templates, String cssString) {
            super(source);
            this.fromClient = fromClient;
            this.templates = templates;
            this.cssString = cssString;
        }

        public EnhancedRichTextEditorTables getSource() {
            return (EnhancedRichTextEditorTables) super.getSource();
        }

        /**
         * Checks if this event originated from the client side.
         *
         * @return <code>true</code> if the event originated from the client side,
         *         <code>false</code> otherwise
         */
        public boolean isFromClient() {
            return fromClient;
        }

        public JsonObject getTemplates() {
            return templates;
        }

        public String getCssString() {
            return cssString;
        }
    }
}