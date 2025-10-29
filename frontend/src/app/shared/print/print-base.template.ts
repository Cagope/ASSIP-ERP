/**
 * 🖨️ Plantilla base estándar de impresión ASSIP-ERP
 * Incluye encabezado institucional, dos líneas opcionales de detalle,
 * cuerpo dinámico y pie con fecha, usuario y paginación.
 */

export interface PrintOptions {
  empresa?: string;
  nit?: string;
  title?: string;
  detalle1?: string;   // primera línea opcional
  detalle2?: string;   // segunda línea opcional
  logoUrl?: string;
  user?: string;
  date?: string;
}

/**
 * Genera el HTML completo listo para imprimir en nueva ventana.
 * @param body Contenido HTML del informe (tabla, resumen, etc.)
 * @param opts Opciones del encabezado/pie institucional
 */
export function buildPrintHtml(body: string, opts: PrintOptions = {}): string {
  const {
    empresa = 'ERP ASSIP Solidaria y Financiera',
    nit = '900.123.456-7',
    title = 'INFORME',
    detalle1 = '',
    detalle2 = '',
    logoUrl = `${window.location.origin}/assets/LOGO_EMPRESA.png`,
    user = 'admin',
    date = new Date().toLocaleString(),
  } = opts;

  // 🔹 Construir encabezado
  const encabezado = `
    <table style="width:100%; border:none;">
      <tr>
        <td style="width:70px; vertical-align:top;">
          <img src="${logoUrl}" style="height:60px;" alt="logo" />
        </td>
        <td style="vertical-align:top; text-align:left;">
          <div style="font-weight:bold; font-size:16px;">${empresa}</div>
          <div style="font-size:12px;">NIT: ${nit}</div>
          <div style="font-size:14px; margin-top:10px; font-weight:bold; text-transform:uppercase;">
            ${title}
          </div>
          ${detalle1 ? `<div style="font-size:12px; margin-top:4px;">${detalle1}</div>` : ''}
          ${detalle2 ? `<div style="font-size:12px; margin-top:2px;">${detalle2}</div>` : ''}
        </td>
      </tr>
    </table>
    <hr style="margin:8px 0 12px 0; border:none; border-top:1px solid #999;">
  `;

  // 🔹 Construir pie de página
  const pie = `
    <footer>
      <div>Fecha de emisión: ${date} — Usuario: ${user}</div>
      <div id="page-num"></div>
    </footer>
  `;

  // 🔹 CSS y estructura general
  return `
<html>
  <head>
    <meta charset="utf-8">
    <title>${title}</title>
    <style>
      @page { margin: 15mm; }

      body {
        font-family: Arial, sans-serif;
        font-size: 11px;
        margin: 0;
        color: #000;
      }

      header {
        margin-bottom: 8px;
      }

      table {
        width: 100%;
        border-collapse: collapse;
        font-size: 11px;
      }

      th, td {
        padding: 4px 6px;
        text-align: left;
        vertical-align: top;
        border: none; /* 🚫 sin bordes visibles */
      }

      th {
        font-weight: bold;
        background: #f9f9f9;
        border-bottom: 1px solid #aaa; /* solo línea inferior del encabezado */
      }

      tbody tr {
        /* 🚫 sin borde entre registros */
      }

      footer {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 22px;
        border-top: 1px solid #ccc;
        font-size: 10px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 3px 18px;
        color: #333;
      }

      footer .page-num::after {
        counter-increment: page;
        content: "Página " counter(page) " / " counter(pages);
      }

      @media print {
        footer {
          position: fixed;
          bottom: 0;
        }
      }
    </style>



  </head>

  <body>
    <header>${encabezado}</header>
    <main>${body}</main>
    ${pie}

    <script>
      window.onload = function() {
        // 🧮 Calcular total de páginas aproximado (1 si solo una hoja)
        const totalPages = Math.max(1, Math.ceil(document.body.scrollHeight / window.innerHeight));
        const pageNumEl = document.getElementById('page-num');
        if (pageNumEl) pageNumEl.textContent = 'Página 1 / ' + totalPages;

        window.focus();
        setTimeout(() => window.print(), 500);
      };
    </script>
  </body>
</html>
`;
}

/**
 * 🧩 Función auxiliar para abrir e imprimir directamente
 */
export function printDirect(body: string, options: PrintOptions = {}): void {
  const html = buildPrintHtml(body, options);
  const win = window.open('', '_blank');
  if (!win) {
    alert('⚠️ Bloqueador de ventanas emergentes activo.');
    return;
  }
  win.document.open();
  win.document.write(html);
  win.document.close();
}
