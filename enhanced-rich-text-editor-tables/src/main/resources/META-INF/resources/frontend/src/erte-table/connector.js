import '../vendor/vaadin-quill.js';
import TableModule from "./index";
import TableTrick from "./js/TableTrick.js";

const old = window.Vaadin.Flow._enhanced_rich_text_editor ?? {}
window.Vaadin.Flow._enhanced_rich_text_editor = {
    ...old,

    registerModule() {
        if (!window.Quill) {
            throw new Error("Quill not found. Registered?");
        }

        if (!window.Quill.__tablesRegistered) {
            window.Quill.register('modules/table', TableModule);
            window.Quill.__tablesRegistered = true;
        }
    },

    extendRteOptions(config) {
        const optionsCopy = {
            ...config,
        }

        optionsCopy.modules = {
            ...optionsCopy.modules,
            table: true,
            keyboard: {
                ...optionsCopy.modules?.keyboard,
                bindings: TableModule.keyBindings
            }
        }

        return optionsCopy;
    },

    tables: {
        insert(rte, rows, cols) {
            this._assureFocus(rte);
            TableTrick.table_handler(`newtable_${rows}_${cols}`, rte._editor);
        },

        action(rte, action) {
            this._assureFocus(rte);
            TableTrick.table_handler(action, rte._editor);
        },

        _assureFocus(rte) {
            if (!rte._editor.hasFocus()) {
                rte._editor.focus();
            }
        }
    }

}