import '../vendor/vaadin-quill.js';
import TableModule from "./index";
import TableTrick from "./js/TableTrick.js";

(function () {
    if (!window.Quill) {
        throw new Error("Quill not found. Registered?");
    }

    if (!window.Quill.__tablesRegistered) {
        console.info("Register Quill Table Module for Enhanced Rich Text Editor");
        window.Quill.register('modules/table', TableModule);
        window.Quill.__tablesRegistered = true;
    }

    const old = window.Vaadin.Flow._enhanced_rich_text_editor ?? {}
    window.Vaadin.Flow._enhanced_rich_text_editor = {
        ...old,  // allow other potential addons to also extend the connector

        extendRteOptions(config) {
            // allow other potential addons to also extend the options
            const optionsCopy = old.extendRteOptions ? old.extendRteOptions(config) : {...config}

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
            },

            _setStyles(rte, styles) {
                let s = rte.shadowRoot.querySelector('#table-template-styles')
                if (!s) {
                    s = document.createElement('style');
                    s.id = 'table-template-styles';
                    rte.shadowRoot.append(s);
                }
                s.innerHTML = styles;
            },

            _getSelectedTable(rte) {
                return rte._editor.__selectedTable;
            },

            setTemplate(rte, template) {
                const classList = this._getSelectedTable(rte)?.classList;
                if (classList) {
                    classList.remove(...classList);
                    classList.add(template);
                }
            },
        }

    }
}());


