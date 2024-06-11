package com.vaadin.componentfactory.erte.tables;

import com.vaadin.componentfactory.EnhancedRichTextEditor;
import com.vaadin.componentfactory.EnhancedRichTextEditor.RichTextEditorI18n;
import com.vaadin.componentfactory.toolbar.ToolbarPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSelectPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSwitch;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import javax.annotation.Nullable;
import java.util.Objects;

@NpmPackage(value = "quill-delta", version = "5.1.0")
@JsModule("./src/erte-table/connector.js")
public class EnhancedRichTextEditorTables {

    private static final String SCRIPTS = "window.Vaadin.Flow._enhanced_rich_text_editor.";
    private static final String SCRIPTS_TABLE = "window.Vaadin.Flow._enhanced_rich_text_editor.tables.";

    private final EnhancedRichTextEditor rte;
    private TemplatePopup stylesPopup;

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
        stylesPopup = new TemplatePopup(stylesButton);

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
        });

        stylesPopup.addTemplateSelectedListener(event -> setTemplateForCurrentTable(event.getTemplate()));
        stylesPopup.addTemplatesChangedListener(event -> {
            try {
                JsonObject templates = event.getSource().getTemplates();
                String string = TemplateParser.parse(templates);
                setClientSideStyles(string);
            } catch (Exception e) {
                // TODO add error handler or smth.
                Notification
                        .show("Could not parse changes from template popup. Please check your inputs and inform the admin")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        rte.addCustomToolbarComponents(insertButton, settingsButton, stylesButton);
    }

    public void setTemplates(JsonObject jsonObject) {
        // TODO allow setting of templates without enabled styles popup
        Objects.requireNonNull(stylesPopup).setTemplates(jsonObject);

        setClientSideStyles(TemplateParser.parse(jsonObject));

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
        rte.getElement().executeJs(SCRIPTS_TABLE + "setTemplate(this, $0)", template);
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

    public Registration addTemplatesChangedListener(ComponentEventListener<TemplatePopup.TemplatesChangedEvent> listener) {
        return stylesPopup.addTemplatesChangedListener(listener);
    }

    public Registration addTemplateSelectedListener(ComponentEventListener<TemplatePopup.TemplateSelectedEvent> listener) {
        return stylesPopup.addTemplateSelectedListener(listener);
    }
}