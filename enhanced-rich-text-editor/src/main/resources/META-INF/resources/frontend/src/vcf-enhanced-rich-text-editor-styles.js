import './vcf-enhanced-rich-text-editor-content-styles.js';
import './vcf-enhanced-rich-text-editor-toolbar-styles.js';
import './vcf-enhanced-rich-text-editor-table-styles.js';
const documentContainer = document.createElement('template');

documentContainer.innerHTML = `
  <dom-module id="vcf-enhanced-rich-text-editor-styles">
    <template>
      <style include="vcf-enhanced-rich-text-editor-content-styles vcf-enhanced-rich-text-editor-toolbar-styles vcf-enhanced-rich-text-editor-table-styles">
        :host([readonly]) [part="toolbar"],
        :host([readonly]) ::slotted([part~="toolbar-button"]) {
          display: none;
        }

        :host([disabled]) {
          pointer-events: none;
          opacity: 0.5;
          -webkit-user-select: none;
          -moz-user-select: none;
          user-select: none;
        }

        :host([disabled]) [part~="toolbar-button"],
        :host([disabled]) ::slotted([part~="toolbar-button"]) {
          background-color: transparent;
        }
      </style>
    </template>
  </dom-module>
`;

document.head.appendChild(documentContainer.content);
