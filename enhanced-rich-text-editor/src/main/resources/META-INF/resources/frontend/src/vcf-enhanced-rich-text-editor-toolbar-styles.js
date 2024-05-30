import './vcf-enhanced-rich-text-editor-icons.js';
const documentContainer = document.createElement('template');

documentContainer.innerHTML = `
  <dom-module id="vcf-enhanced-rich-text-editor-toolbar-styles">
    <template>
      <style include="vcf-enhanced-rich-text-editor-icons">
        [part="toolbar"] {
          display: flex;
          flex-wrap: wrap;
          flex-shrink: 0;
        }

        [part~="toolbar-button"],
        ::slotted([part~="toolbar-button"]) {
          width: 2em;
          height: 2em;
          margin: 0;
          padding: 0;
          font: inherit;
          line-height: 1;
          text-transform: none;
          background: transparent;
          border: none;
          position: relative;
        }

        ::slotted([part~="toolbar-button"]) {
          width: auto;
          height: var(--lumo-size-m);
          border-radius: var(--lumo-border-radius);
          color: var(--lumo-contrast-80pct);
          margin: 2px 1px;
          cursor: default;
          transition: background-color 100ms, color 100ms;
          padding: 0 var(--lumo-space-s);
        }

        ::slotted([part~="toolbar-button"]:hover) {
          background-color: var(--lumo-contrast-5pct);
          color: var(--lumo-contrast);
          box-shadow: none;
        }

        [part~="toolbar-button"]:hover,
        ::slotted([part~="toolbar-button"]:hover) {
          outline: none;
        }

        [part~="toolbar-button"]::before,
        ::slotted([part~="toolbar-button"]::before) {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
        }

        [part~="toolbar-button-table-icon"]::before,
        [part~="toolbar-button-undo-icon"]::before,
        [part~="toolbar-button-redo-icon"]::before,
        [part~="toolbar-button-list-ordered-icon"]::before,
        [part~="toolbar-button-list-bullet.icon"]::before,       
        [part~="toolbar-button-image-icon"]::before,
        [part~="toolbar-button-link-icon"]::before,
        [part~="toolbar-button-clean-icon"]::before {
          font-family: "vaadin-rte-icons", sans-serif;
        }    

        [part~="toolbar-button-align-justify-icon"], 
        [part~="toolbar-button-align-left-icon"],
        [part~="toolbar-button-align-center-icon"],
        [part~="toolbar-button-align-right-icon"],
        [part~="toolbar-button-deindent-icon"], 
        [part~="toolbar-button-indent-icon"],
        [part~="toolbar-button-readonly-icon"],
        [part~="toolbar-button-vaadin-icon"]{
          --rte-extra-icons-stroke-color: var(--lumo-contrast-80pct);
        }

        [part~="toolbar-group"] {
          display: flex;
          margin: 0 0.5em;
        }

        [part~="toolbar-button-bold-icon"]::before {
          content: "B";
          font-weight: 700;
        }

        [part~="toolbar-button-italic-icon"]::before {
          content: "I";
          font-style: italic;
        }

        [part~="toolbar-button-underline-icon"]::before {
          content: "U";
          text-decoration: underline;
        }

        [part~="toolbar-button-strike-icon"]::before {
          content: "T";
          text-decoration: line-through;
        }

        [part~="toolbar-button-h1-icon"]::before {
          content: "H1";
          font-size: 1.25em;
        }

        [part~="toolbar-button-h2-icon"]::before {
          content: "H2";
          font-size: 1em;
        }

        [part~="toolbar-button-h3-icon"]::before {
          content: "H3";
          font-size: 0.875em;
        }

        [part~="toolbar-button-h1-icon"]::before,
        [part~="toolbar-button-h2-icon"]::before,
        [part~="toolbar-button-h3-icon"]::before {
          letter-spacing: -0.05em;
        }

        [part~="toolbar-button-subscript-icon"]::before,
        [part~="toolbar-button-superscript-icon"]::before {
          content: "X";
        }

        [part~="toolbar-button-subscript-icon"]::after,
        [part~="toolbar-button-superscript-icon"]::after {
          content: "2";
          position: absolute;
          top: 50%;
          left: 65%;
          font-size: 0.625em;
        }

        [part~="toolbar-button-superscript-icon"]::after {
          top: 20%;
        }

        [part~="toolbar-button-blockquote-icon"]::before {
          content: "”";
          font-size: 2em;
          height: 0.6em;
        }

        [part~="toolbar-button-code-block-icon"]::before {
          content: "</>";
          font-size: 0.875em;
        }
        
        [part~="toolbar-button"][part~="toolbar-button-placeholder-display"] {
          width: auto;
          min-width: var(--lumo-size-l);
          padding: 0 var(--lumo-space-xs);
          font-size: 0.875em;
        }

        /* taken from snow theme. necessary for any dropdowns*/

         .ql-picker:not([class~="icon"]) {
          color: #444;
          display: inline-block;
          float: left;
          font-size: 14px;
          font-weight: 500;
          height: 24px;
          position: relative;
          vertical-align: middle;
        }
        
        .ql-picker:not([class~="icon"]) .ql-picker-label {
          cursor: pointer;
          display: inline-block;
          height: 100%;
          padding-left: 8px;
          padding-right: 2px;
          position: relative;
          width: 100%;
        }
        .ql-picker:not([class~="icon"])  .ql-picker-label::before {
          display: inline-block;
          line-height: 22px;
        }
        
        .ql-picker.icon {
            width: var(--lumo-size-m, 2em);
            height: var(--lumo-size-m, 2em);
            display: flex;
            flex-direction: column;
            justify-content: center;
            position: relative;
        }
        
        .ql-picker.icon .ql-picker-label {
          cursor: pointer;
          display: inline;
          align-self: center;
        }
        
        .ql-picker-options {
          background-color: #fff;
          display: none;
          min-width: 100%;
          padding: 4px 8px;
          position: absolute;
          white-space: nowrap;
        }
         .ql-picker-options .ql-picker-item {
          cursor: pointer;
          display: block;
          padding-bottom: 5px;
          padding-top: 5px;
        }
        
         .ql-picker.ql-expanded .ql-picker-label {
          color: #ccc;
          z-index: 2;
        }
         .ql-picker.ql-expanded .ql-picker-label .ql-fill {
          fill: #ccc;
        }
         .ql-picker.ql-expanded .ql-picker-label .ql-stroke {
          stroke: #ccc;
        }
         .ql-picker.ql-expanded .ql-picker-options {
          display: block;
          margin-top: -1px;
          top: 100%;
          z-index: 1;
        }
   
         .ql-picker:not(.ql-color-picker):not(.ql-icon-picker) svg {
          position: absolute;
          margin-top: -9px;
          right: 0;
          top: 50%;
          width: 18px;
        }
         .ql-picker.ql-header .ql-picker-label[data-label]:not([data-label=''])::before,
         .ql-picker.ql-font .ql-picker-label[data-label]:not([data-label=''])::before,
         .ql-picker.ql-size .ql-picker-label[data-label]:not([data-label=''])::before,
         .ql-picker.ql-header .ql-picker-item[data-label]:not([data-label=''])::before,
         .ql-picker.ql-font .ql-picker-item[data-label]:not([data-label=''])::before,
         .ql-picker.ql-size .ql-picker-item[data-label]:not([data-label=''])::before {
          content: attr(data-label);
        }
        
        /* .ql-picker.ql-font {*/
        /*  width: 108px;*/
        /*}*/
        /* .ql-picker.ql-font .ql-picker-label::before,*/
        /* .ql-picker.ql-font .ql-picker-item::before {*/
        /*  content: 'Sans Serif';*/
        /*}*/
        /* .ql-picker.ql-font .ql-picker-label[data-value=serif]::before,*/
        /* .ql-picker.ql-font .ql-picker-item[data-value=serif]::before {*/
        /*  content: 'Serif';*/
        /*}*/
        /* .ql-picker.ql-font .ql-picker-label[data-value=monospace]::before,*/
        /* .ql-picker.ql-font .ql-picker-item[data-value=monospace]::before {*/
        /*  content: 'Monospace';*/
        /*}*/
        /* .ql-picker.ql-font .ql-picker-item[data-value=serif]::before {*/
        /*  font-family: Georgia, Times New Roman, serif;*/
        /*}*/
        /* .ql-picker.ql-font .ql-picker-item[data-value=monospace]::before {*/
        /*  font-family: Monaco, Courier New, monospace;*/
        /*}*/
        
         .ql-picker.ql-size {
          width: 98px;
        }
         .ql-picker.ql-size .ql-picker-label::before,
         .ql-picker.ql-size .ql-picker-item::before {
          content: 'Normal';
        }
         .ql-picker.ql-size .ql-picker-label[data-value=small]::before,
         .ql-picker.ql-size .ql-picker-item[data-value=small]::before {
          content: 'Small';
        }
         .ql-picker.ql-size .ql-picker-label[data-value=large]::before,
         .ql-picker.ql-size .ql-picker-item[data-value=large]::before {
          content: 'Large';
        }
         .ql-picker.ql-size .ql-picker-label[data-value=huge]::before,
         .ql-picker.ql-size .ql-picker-item[data-value=huge]::before {
          content: 'Huge';
        }
         .ql-picker.ql-size .ql-picker-item[data-value=small]::before {
          font-size: 10px;
        }
         .ql-picker.ql-size .ql-picker-item[data-value=large]::before {
          font-size: 18px;
        }
         .ql-picker.ql-size .ql-picker-item[data-value=huge]::before {
          font-size: 32px;
        }
      
        .ql-toolbar {
          font-family: 'Helvetica Neue', 'Helvetica', 'Arial', sans-serif;
          padding: 8px;
        }
        
        .ql-toolbar .ql-picker-label {
          border: 1px solid transparent;
        }
        
        .ql-toolbar .ql-picker-options {
          border: 1px solid transparent;
          box-shadow: rgba(0,0,0,0.2) 0 2px 8px;
        }
        
        .ql-toolbar .ql-picker.ql-expanded .ql-picker-label {
          border-color: #ccc;
        }
        .ql-toolbar .ql-picker.ql-expanded .ql-picker-options {
          border-color: #ccc;
        }
        
        

        
      </style>
    </template>
  </dom-module>
`;

document.head.appendChild(documentContainer.content);
