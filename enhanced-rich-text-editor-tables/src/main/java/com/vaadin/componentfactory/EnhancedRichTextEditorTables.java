package com.vaadin.componentfactory;

import com.vaadin.componentfactory.EnhancedRichTextEditor.RichTextEditorI18n;
import com.vaadin.componentfactory.toolbar.ToolbarPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSelectPopup;
import com.vaadin.componentfactory.toolbar.ToolbarSwitch;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.List;

@NpmPackage(value = "quill-delta", version = "5.1.0")
@JsModule("./src/erte-table/connector.js")
public class EnhancedRichTextEditorTables {

    private static final String SCRIPTS = "window.Vaadin.Flow._enhanced_rich_text_editor.";
    private static final String SCRIPTS_TABLE = "window.Vaadin.Flow._enhanced_rich_text_editor.tables.";

    public static void init() {
        UI.getCurrent().getElement().executeJs(SCRIPTS + "registerModule()");
    }

    public static void initToolbarTable(EnhancedRichTextEditor rte) {
        // insert new table
        IntegerField rows = createTableInsertNumberField(
                rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertRows, "Rows"),
                rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertRowsTooltip, "Amount of rows for the new table")
        );

        IntegerField cols = createTableInsertNumberField(
                rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertCols, "Columns"),
                rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertCols, "Amount of columns for the new table")
        );

        Button add = new Button(VaadinIcon.PLUS.create(), event -> tableInsertNew(rte, rows.getValue(), cols.getValue()));
        add.setTooltipText(rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertAddButtonTooltip, "Add new table"));

        HorizontalLayout insertLayout = new HorizontalLayout();
        insertLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        ToolbarSwitch insertButton = new ToolbarSwitch(VaadinIcon.TABLE, VaadinIcon.PLUS);
        insertButton.setTooltipText(rte.getI18nOrDefault(RichTextEditorI18n::getTableInsertSwitchTooltip, "Show/Hide the \"add new table\" elements"));

        ToolbarPopup insertPopup = ToolbarPopup.horizontal(insertButton, rows, new Span("x"), cols, add);
        insertPopup.setFocusOnOpenTarget(rows);
        add.addClickListener(event -> insertPopup.setOpened(false));

        ToolbarSwitch settingsButton = new ToolbarSwitch(VaadinIcon.TABLE, VaadinIcon.ASTERISK);
        settingsButton.setEnabled(false);

        ToolbarSelectPopup selectPopup = new ToolbarSelectPopup(settingsButton);
        selectPopup.addItem("Add row above", event -> tableAction(rte,"append-row-above"));
        selectPopup.addItem("Add row below", event -> tableAction(rte,"append-row-below"));

        List<MenuItem> cellActions = List.of(
                selectPopup.addItem("Merge Cells", event -> tableAction(rte,"merge-selection"))
        );

        selectPopup.addItem("Split Cells", event -> tableAction(rte,"split-cell"));

        ComponentUtil.addListener(rte, TableSelectedEvent.class, event -> {
            settingsButton.setEnabled(event.isSelected());
            boolean cellSelectionActive = event.isCellSelectionActive();
            insertButton.setEnabled(!cellSelectionActive);
            cellActions.forEach(menuItem -> menuItem.setEnabled(cellSelectionActive));
            Notification.show("Template: " + event.getTemplate());
        });

        rte.addCustomToolbarComponents(insertButton, settingsButton);
    }

    public static void tableInsertNew(EnhancedRichTextEditor rte, int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Rows and cols must be greater 0");
        }

        rte.getElement().executeJs(SCRIPTS_TABLE+ "insert($0, $1, $2)", rte, rows, cols);
    }

    public static void tableAction(EnhancedRichTextEditor rte, String action) {
        rte.getElement().executeJs(SCRIPTS_TABLE+ "action($0, $1)", rte, action);
    }

    private static IntegerField createTableInsertNumberField(String placeholder, String tooltip) {
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
}