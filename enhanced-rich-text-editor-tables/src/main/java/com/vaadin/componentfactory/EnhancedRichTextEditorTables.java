package com.vaadin.componentfactory;

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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;

import java.util.Objects;

@NpmPackage(value = "quill-delta", version = "5.1.0")
@JsModule("./src/erte-table/connector.js")
public class EnhancedRichTextEditorTables {

    private static final String SCRIPTS = "window.Vaadin.Flow._enhanced_rich_text_editor.";
    private static final String SCRIPTS_TABLE = "window.Vaadin.Flow._enhanced_rich_text_editor.tables.";

    private final EnhancedRichTextEditor rte;
    private TemplatesPopup stylesPopup;

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
        IntegerField rows = createTableInsertNumberField(
                rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertRows, "Rows"),
                rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertRowsTooltip, "Amount of rows for the new table")
        );

        IntegerField cols = createTableInsertNumberField(
                rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertCols, "Columns"),
                rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertCols, "Amount of columns for the new table")
        );

        Button add = new Button(VaadinIcon.PLUS.create(), event -> tableInsertNew(rows.getValue(), cols.getValue()));
        add.setTooltipText(rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertAddButtonTooltip, "Add new table"));

        ToolbarSwitch insertButton = new ToolbarSwitch(VaadinIcon.TABLE, VaadinIcon.PLUS);
        insertButton.setTooltipText(rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertSwitchTooltip, "Show/Hide the \"add new table\" elements"));

        ToolbarPopup insertPopup = ToolbarPopup.horizontal(insertButton, rows, new Span("x"), cols, add);
        insertPopup.setFocusOnOpenTarget(rows);
        add.addClickListener(event -> insertPopup.setOpened(false));

        ToolbarSwitch settingsButton = new ToolbarSwitch(VaadinIcon.TABLE, VaadinIcon.TOOLS);
        settingsButton.setEnabled(false);

        ToolbarSelectPopup selectPopup = new ToolbarSelectPopup(settingsButton);
        selectPopup.addItem("Add row above", event -> tableAction("append-row-above"));
        selectPopup.addItem("Add row below", event -> tableAction("append-row-below"));
        selectPopup.addItem("Remove row", event -> tableAction("remove-row"));
        selectPopup.add(new Hr());
        selectPopup.addItem("Add col before", event -> tableAction("append-col-before"));
        selectPopup.addItem("Add col after", event -> tableAction("append-col-after"));
        selectPopup.addItem("Remove col", event -> tableAction("remove-col"));

        selectPopup.add(new Hr());
        MenuItem mergeCells = selectPopup.addItem("Merge Cells", event -> tableAction( "merge-selection"));

        selectPopup.addItem("Split Cells", event -> tableAction("split-cell"));

        selectPopup.add(new Hr());
        selectPopup.addItem("Remove table", event -> tableAction("remove-table"));

        ToolbarSwitch stylesButton = new ToolbarSwitch(VaadinIcon.TABLE, VaadinIcon.EYE);
        stylesButton.setEnabled(false);
        stylesPopup = new TemplatesPopup(stylesButton);

        ComponentUtil.addListener(rte, TableSelectedEvent.class, event -> {
            insertButton.setEnabled(!event.isSelected());
            settingsButton.setEnabled(event.isSelected());
            stylesButton.setEnabled(event.isSelected());

            boolean cellSelectionActive = event.isCellSelectionActive();
            insertButton.setEnabled(!cellSelectionActive);
            mergeCells.setEnabled(cellSelectionActive);

            stylesPopup.setActiveTemplateName(event.getTemplate());

        });



        rte.addCustomToolbarComponents(insertButton, settingsButton, stylesButton);
    }

    public void setTemplates(JsonObject jsonObject) {
        // TODO allow setting of templates without enabled styles popup
        Objects.requireNonNull(stylesPopup).setTemplates(jsonObject);
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

    public void tableInsertNew(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and cols must be greater 0");
        }

        rte.getElement().executeJs(SCRIPTS_TABLE+ "insert(this, $0, $1)", rows, cols);
    }

    public void tableAction(String action) {
        rte.getElement().executeJs(SCRIPTS_TABLE+ "action(this, $0)", action);
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



    public Registration addTemplatesChangedListener(ComponentEventListener<TemplatesPopup.TemplatesChangedEvent> listener) {
        return stylesPopup.addTemplatesChangedListener(listener);
    }

    public Registration addTemplateSelectedListener(ComponentEventListener<TemplatesPopup.TemplateSelecteEvent> listener) {
        return stylesPopup.addTemplateSelectedListener(listener);
    }
}