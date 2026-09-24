// =========================================================
// ASSIP ERP
// ORIGINACIÓN DE CARTERA
// EXPEDIENTE INTEGRAL DE SOLICITUD Y OTORGAMIENTO
//
// Estilos exclusivos para impresión física y PDF.
//
// Formato:
// - Tamaño carta.
// - Orientación vertical.
// - Márgenes institucionales.
// - Tablas financieras.
// - Espacios para firmas y huellas.
// - Saltos de página controlados.
// =========================================================

export const ORIGINACION_EXPEDIENTE_PRINT_STYLES = `

  /* =======================================================
     VARIABLES INSTITUCIONALES
     ======================================================= */

  :root {

    --color-primario: #174b35;
    --color-primario-oscuro: #103b2a;
    --color-secundario: #d5a329;

    --color-texto: #222222;
    --color-texto-suave: #555555;

    --color-borde: #b8c0bb;
    --color-borde-fuerte: #78867e;

    --color-fondo: #f2f5f3;
    --color-fondo-suave: #f8faf8;

    --color-blanco: #ffffff;

  }


  /* =======================================================
     CONFIGURACIÓN DE PÁGINA
     ======================================================= */

  @page {

    size: letter portrait;

    margin: 13mm 12mm 15mm 12mm;

  }


  /* =======================================================
     CONFIGURACIÓN GENERAL
     ======================================================= */

  * {

    box-sizing: border-box;

  }

  html {

    margin: 0;
    padding: 0;

    background: #ffffff;

  }

  body {

    margin: 0;
    padding: 0;

    background: #ffffff;

    color: var(--color-texto);

    font-family: Arial, Helvetica, sans-serif;

    font-size: 9px;

    line-height: 1.35;

    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;

  }

  h1,
  h2,
  h3,
  h4,
  p {

    margin-top: 0;

  }

  p {

    margin-bottom: 6px;

  }

  strong {

    font-weight: 700;

  }

  .expediente {

    width: 100%;
    margin: 0 auto;

  }


  /* =======================================================
     ENCABEZADO INSTITUCIONAL COOPVALLE
     ======================================================= */

  .expediente-header {

    display: flex;
    align-items: center;
    gap: 18px;

    margin-bottom: 20px;
    padding: 5px 0 12px;

    border-bottom: none;

    break-inside: avoid;
    page-break-inside: avoid;

  }

  .expediente-header__logo-contenedor {

    width: 105px;
    flex: 0 0 105px;

    display: flex;
    align-items: center;
    justify-content: center;

  }

  .expediente-header__logo {

    display: block;

    width: 95px;
    max-height: 75px;

    object-fit: contain;

  }

  .expediente-header__empresa {

    flex: 1;
    min-width: 0;

    text-align: left;

  }

  .expediente-header__nombre {

    margin: 0 0 3px;

    color: #047857;

    font-size: 12px;
    font-weight: 700;

    text-transform: uppercase;

  }

  .expediente-header__nit {

    margin: 0 0 10px;

    color: #475569;

    font-size: 8px;

  }

  .expediente-header__documento {

    margin: 0 0 4px;

    color: #047857;

    font-size: 16px;
    font-weight: 700;

    line-height: 1.2;

    text-transform: uppercase;

  }

  .expediente-header__referencia {

    color: #475569;

    font-size: 8px;

  }


  /* =======================================================
     TÍTULO PRINCIPAL
     ======================================================= */

  .expediente-titulo {

    margin: 10px 0 8px;

    padding: 7px 10px;

    background: var(--color-primario);

    color: #ffffff;

    text-align: center;

    font-size: 12px;

    font-weight: 700;

    text-transform: uppercase;

  }

  .expediente-subtitulo {

    margin: 4px 0 10px;

    color: var(--color-texto-suave);

    text-align: center;

    font-size: 9px;

  }


  /* =======================================================
     INFORMACIÓN GENERAL DE LA SOLICITUD
     ======================================================= */

  .expediente-identificacion {

    display: grid;

    grid-template-columns: repeat(4, minmax(0, 1fr));

    gap: 6px;

    margin-bottom: 10px;

  }

  .expediente-identificacion__item {

    min-width: 0;

    padding: 5px 6px;

    border: 1px solid var(--color-borde);

    background: var(--color-fondo-suave);

  }

  .expediente-identificacion__label {

    display: block;

    margin-bottom: 3px;

    color: var(--color-texto-suave);

    font-size: 7.5px;

    font-weight: 700;

    text-transform: uppercase;

  }

  .expediente-identificacion__valor {

    display: block;

    overflow-wrap: anywhere;

    font-size: 9px;

    font-weight: 700;

  }


  /* =======================================================
     SECCIONES
     ======================================================= */

  .expediente-seccion {

    margin-bottom: 12px;

  }

  .expediente-seccion__titulo {

    margin: 10px 0 7px;

    padding: 6px 8px;

    border-left: 4px solid var(--color-secundario);

    background: var(--color-primario);

    color: #ffffff;

    font-size: 10px;

    font-weight: 700;

    text-transform: uppercase;

    break-after: avoid;

    page-break-after: avoid;

  }

  .expediente-seccion__subtitulo {

    margin: 8px 0 5px;

    padding: 5px 7px;

    border-bottom: 1px solid var(--color-primario);

    background: var(--color-fondo);

    color: var(--color-primario-oscuro);

    font-size: 9px;

    font-weight: 700;

    break-after: avoid;

    page-break-after: avoid;

  }

  .expediente-seccion__descripcion {

    margin-bottom: 7px;

    color: var(--color-texto-suave);

    font-size: 8px;

  }


  /* =======================================================
     CUADRÍCULAS DE INFORMACIÓN
     ======================================================= */

  /* Un solo cuadro por bloque: sin tarjetas independientes. */
  .expediente-grid {

    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0;
    margin-bottom: 8px;

    border: 1px solid var(--color-borde);
    background: var(--color-blanco);

  }

  .expediente-grid--3 {

    grid-template-columns: repeat(3, minmax(0, 1fr));

  }

  .expediente-grid--4 {

    grid-template-columns: repeat(4, minmax(0, 1fr));

  }

  .expediente-grid--1 {

    grid-template-columns: minmax(0, 1fr);

  }

  .expediente-campo {

    min-width: 0;
    padding: 5px 7px;
    border: 0;
    border-bottom: 1px solid var(--color-borde);
    background: transparent;

  }

  /* Separador central de la cuadrícula de dos columnas. */
  .expediente-grid:not(.expediente-grid--1):not(.expediente-grid--3):not(.expediente-grid--4)
    > .expediente-campo:nth-child(odd) {

    border-right: 1px solid var(--color-borde);

  }

  /* La última fila no necesita línea inferior: ya existe el borde exterior. */
  .expediente-grid:not(.expediente-grid--1):not(.expediente-grid--3):not(.expediente-grid--4)
    > .expediente-campo:nth-last-child(-n + 2) {

    border-bottom: 0;

  }

  .expediente-grid--1 > .expediente-campo:last-child,
  .expediente-grid--3 > .expediente-campo:nth-last-child(-n + 3),
  .expediente-grid--4 > .expediente-campo:nth-last-child(-n + 4) {

    border-bottom: 0;

  }

  .expediente-grid--3 > .expediente-campo:not(:nth-child(3n)),
  .expediente-grid--4 > .expediente-campo:not(:nth-child(4n)) {

    border-right: 1px solid var(--color-borde);

  }

  /* =========================================================
     CAMPOS DOCUMENTALES
     Etiqueta y valor en la misma línea
     ========================================================= */

  .expediente-campo__label {
    display: inline;
    font-size: 9px;
    font-weight: 500;
    color: #244c3c;
    text-transform: uppercase;
    line-height: 1.4;
  }

  .expediente-campo__label::after {
    content: ': ';
  }

  .expediente-campo__valor {
    display: inline;
    margin-left: 4px;
    font-size: 10px;
    font-weight: 700;
    color: #17212b;
    line-height: 1.4;
    overflow-wrap: anywhere;
  }

  /* =========================================================
     VALORES NUMÉRICOS EN CAMPOS DOCUMENTALES
     Requiere clase expediente-campo--numerico desde campos().
     No se aplica a fechas, documentos ni teléfonos.
     ========================================================= */

  .expediente-campo--numerico {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 8px;
  }

  .expediente-campo--numerico > .expediente-campo__label {
    flex: 1 1 auto;
    min-width: 0;
    margin: 0;
  }

  .expediente-campo--numerico > .expediente-campo__valor {
    flex: 0 0 auto;
    margin: 0 0 0 auto;
    text-align: right;
    white-space: nowrap;
    font-variant-numeric: tabular-nums;
  }

  .expediente-campo--destacado {

    background: var(--color-fondo-suave);

  }


  /* =======================================================
     TABLAS GENERALES
     ======================================================= */

  .expediente-tabla {

    width: 100%;

    margin: 5px 0 9px;

    border-collapse: collapse;

    table-layout: fixed;

    font-size: 8.5px;

  }

  .expediente-tabla thead {

    display: table-header-group;

  }

  .expediente-tabla tfoot {

    display: table-footer-group;

  }

  .expediente-tabla th {

    padding: 5px 6px;

    border: 1px solid var(--color-borde-fuerte);

    background: var(--color-fondo);

    color: var(--color-primario-oscuro);

    font-weight: 700;

    text-align: left;

    vertical-align: middle;

    overflow-wrap: anywhere;

  }

  .expediente-tabla td {

    padding: 5px 6px;

    border: 1px solid var(--color-borde);

    vertical-align: top;

    overflow-wrap: anywhere;

  }

  .expediente-tabla tbody tr:nth-child(even) {

    background: var(--color-fondo-suave);

  }

  .expediente-tabla tr {

    break-inside: avoid;

    page-break-inside: avoid;

  }

  .expediente-tabla__numero {

    text-align: right !important;

    white-space: nowrap;

    font-variant-numeric: tabular-nums;

  }

  .expediente-tabla__centro {

    text-align: center !important;

  }

  .expediente-tabla__total td {

    background: var(--color-fondo);

    color: var(--color-primario-oscuro);

    font-weight: 700;

    border-top: 1.5px solid var(--color-primario);

  }

  .expediente-tabla__subtotal td {

    background: var(--color-fondo-suave);

    font-weight: 700;

  }

  .expediente-tabla__observacion {

    color: var(--color-texto-suave);

    font-size: 8px;

  }


  /* =======================================================
     INGRESOS Y EGRESOS
     ======================================================= */

  .expediente-financiero {

    margin-bottom: 10px;

  }

  .expediente-financiero__titulo {

    margin: 8px 0 5px;

    padding: 5px 7px;

    background: var(--color-fondo);

    border-left: 3px solid var(--color-primario);

    color: var(--color-primario-oscuro);

    font-size: 9px;

    font-weight: 700;

  }

  .expediente-financiero__total {

    padding: 6px 8px;

    border: 1px solid var(--color-primario);

    background: var(--color-fondo);

    color: var(--color-primario-oscuro);

    text-align: right;

    font-size: 10px;

    font-weight: 700;

  }

  .expediente-financiero__resumen {

    display: grid;

    grid-template-columns: repeat(2, minmax(0, 1fr));

    gap: 8px;

    margin-top: 8px;

  }

  .expediente-financiero__indicador {

    padding: 7px;

    border: 1px solid var(--color-borde);

    background: var(--color-fondo-suave);

  }

  .expediente-financiero__indicador-label {

    display: block;

    margin-bottom: 4px;

    font-size: 8px;

    color: var(--color-texto-suave);

  }

    .expediente-financiero__indicador-valor {

      display: block;

      font-size: 11px;

      font-weight: 700;

      color: var(--color-primario-oscuro);

      text-align: right;

    }


    /* =======================================================
       INFORMACIÓN FINANCIERA EN DOS COLUMNAS
       ======================================================= */

    .expediente-financiero-columnas {
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
      gap: 8px;
      align-items: start;
      margin-top: 6px;
    }

    .expediente-financiero-columna {
      min-width: 0;
    }

    .expediente-financiero-columna
    .expediente-seccion__subtitulo {
      margin-top: 0;
    }

    .expediente-financiero-columna
    .expediente-tabla {
      width: 100%;
      margin-top: 5px;
      table-layout: fixed;
    }

    .expediente-financiero-columna
    .expediente-tabla th,
    .expediente-financiero-columna
    .expediente-tabla td {
      padding: 5px 7px;
    }

    .expediente-financiero-columna
    .expediente-tabla th:first-child,
    .expediente-financiero-columna
    .expediente-tabla td:first-child {
      width: 65%;
    }

    .expediente-financiero-columna
    .expediente-tabla th:last-child,
    .expediente-financiero-columna
    .expediente-tabla td:last-child {
      width: 35%;
    }


    /* =======================================================
       DESCRIPCIONES FINANCIERAS COMPACTAS
       ======================================================= */

    .expediente-financiero-descripciones {
      display: grid;
      grid-template-columns: minmax(0, 1fr);
      gap: 0;

      width: 100%;
      margin: 6px 0 8px;

      border: 1px solid var(--color-borde);
      background: var(--color-blanco);
    }

    .expediente-financiero-descripcion {
      display: block;

      min-width: 0;
      width: 100%;

      padding: 5px 7px;

      line-height: 1.35;
      overflow-wrap: anywhere;

      break-inside: avoid;
      page-break-inside: avoid;
    }

    .expediente-financiero-descripcion
    + .expediente-financiero-descripcion {
      border-left: 0;
      border-top: 1px solid var(--color-borde);
    }

    .expediente-financiero-descripcion
    .expediente-campo__label {
      display: inline;

      margin: 0;

      font-size: 9px;
      font-weight: 500;
      line-height: 1.35;
    }

    .expediente-financiero-descripcion
    .expediente-campo__valor {
      display: inline;

      margin: 0 0 0 4px;

      font-size: 9px;
      font-weight: 700;
      line-height: 1.35;

      white-space: normal;
      overflow-wrap: anywhere;
    }

  /* =======================================================
     PATRIMONIO DECLARADO
     MISMO DISEÑO DE INGRESOS Y EGRESOS
     ======================================================= */

  .expediente-patrimonio-columnas {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
    align-items: start;
    margin-bottom: 8px;
  }

  .expediente-patrimonio-columna {
    min-width: 0;
  }

  .expediente-patrimonio-columna .expediente-tabla {
    width: 100%;
    margin: 5px 0 9px;
    table-layout: fixed;
  }

  .expediente-tabla--patrimonio th:first-child,
  .expediente-tabla--patrimonio td:first-child {
    width: 65%;
  }

  .expediente-tabla--patrimonio th:last-child,
  .expediente-tabla--patrimonio td:last-child {
    width: 35%;
  }

  .expediente-tabla--patrimonio th,
  .expediente-tabla--patrimonio td {
    padding: 5px 7px;
  }

    /* =======================================================
       RESULTADOS DEL MODELO DE OTORGAMIENTO
       ======================================================= */

  .expediente-indicadores {

    display: grid;

    grid-template-columns: repeat(3, minmax(0, 1fr));

    gap: 7px;

    margin-bottom: 9px;

  }

  .expediente-indicador {

    padding: 7px;

    border: 1px solid var(--color-borde);

    background: var(--color-fondo-suave);

    text-align: center;

  }

  .expediente-indicador__titulo {

    display: block;

    margin-bottom: 5px;

    color: var(--color-texto-suave);

    font-size: 8px;

    font-weight: 700;

  }

  .expediente-indicador__valor {

    display: block;

    color: var(--color-primario-oscuro);

    font-size: 12px;

    font-weight: 700;

  }

  .expediente-indicador__detalle {

    display: block;

    margin-top: 4px;

    color: var(--color-texto-suave);

    font-size: 7.5px;

  }


  /* =======================================================
     DEUDORES Y CODEUDORES
     ======================================================= */

  .expediente-persona {

    margin-bottom: 13px;

  }

  .expediente-persona__encabezado {

    margin: 8px 0 7px;

    padding: 6px 8px;

    border: 1px solid var(--color-primario);

    background: var(--color-fondo);

    color: var(--color-primario-oscuro);

    font-size: 10px;

    font-weight: 700;

    break-after: avoid;

    page-break-after: avoid;

  }

  .expediente-persona__tipo {

    display: inline-block;

    margin-right: 7px;

    color: var(--color-primario);

    font-size: 8px;

    font-weight: 700;

    text-transform: uppercase;

  }


  /* =======================================================
     BIENES Y GARANTÍAS
     ======================================================= */

  .expediente-bien {

    margin-bottom: 8px;

    border: 1px solid var(--color-borde);

  }

  .expediente-bien__titulo {

    padding: 5px 7px;

    background: var(--color-fondo);

    color: var(--color-primario-oscuro);

    font-size: 9px;

    font-weight: 700;

  }

  .expediente-bien__contenido {

    padding: 7px;

  }


  /* =======================================================
     CENTRALES DE RIESGO
     ======================================================= */

  .expediente-central__resumen {

    margin-bottom: 8px;

    padding: 7px;

    border: 1px solid var(--color-borde);

    background: var(--color-fondo-suave);

  }

  .expediente-central__fecha {

    color: var(--color-texto-suave);

    font-size: 8px;

  }


  /* =======================================================
     APROBACIONES
     ======================================================= */

  .expediente-aprobacion {

    margin-bottom: 11px;

    border: 1px solid var(--color-borde);

  }

  .expediente-aprobacion__titulo {

    padding: 6px 8px;

    background: var(--color-primario);

    color: #ffffff;

    font-size: 9px;

    font-weight: 700;

    text-transform: uppercase;

  }

  .expediente-aprobacion__contenido {

    padding: 8px;

  }

  .expediente-aprobacion__concepto {

    margin-top: 7px;

    padding: 7px;

    border: 1px solid var(--color-borde);

    min-height: 35px;

    white-space: pre-wrap;

    overflow-wrap: anywhere;

  }


  /* =======================================================
     DECLARACIONES Y AUTORIZACIONES
     ======================================================= */

  .expediente-declaracion {

    margin-bottom: 8px;

    font-size: 8.5px;

    line-height: 1.5;

    text-align: justify;

  }

  .expediente-declaracion__numero {

    font-weight: 700;

    color: var(--color-primario-oscuro);

  }

  .expediente-autorizaciones {

    margin-top: 10px;

  }

  .expediente-autorizaciones__titulo {

    margin-bottom: 6px;

    font-size: 9px;

    font-weight: 700;

  }


  /* =======================================================
     FIRMAS Y HUELLAS
     ======================================================= */

  .expediente-firmas {

    margin-top: 18px;

  }

  .expediente-firmas__titulo {

    margin-bottom: 12px;

    padding: 6px 8px;

    border-bottom: 2px solid var(--color-primario);

    color: var(--color-primario-oscuro);

    font-size: 10px;

    font-weight: 700;

  }

  .expediente-firmas__grid {

    display: grid;

    grid-template-columns: repeat(2, minmax(0, 1fr));

    gap: 18px 15px;

  }

  .expediente-firma {

    min-height: 120px;

    padding: 8px;

    border: 1px solid var(--color-borde);

    break-inside: avoid;

    page-break-inside: avoid;

  }

  .expediente-firma__tipo {

    margin-bottom: 6px;

    font-size: 8px;

    font-weight: 700;

    text-transform: uppercase;

  }

  .expediente-firma__espacios {

    display: flex;

    align-items: flex-end;

    gap: 10px;

    min-height: 66px;

  }

  .expediente-firma__firma {

    flex: 1;

    min-width: 0;

    padding-top: 35px;

    border-bottom: 1px solid #222222;

  }

  .expediente-firma__huella {

    width: 65px;

    height: 65px;

    flex-shrink: 0;

    border: 1px solid #555555;

    display: flex;

    align-items: flex-end;

    justify-content: center;

    padding-bottom: 3px;

    color: var(--color-texto-suave);

    font-size: 7px;

  }

  .expediente-firma__nombre {

    margin-top: 7px;

    font-size: 8px;

    font-weight: 700;

    overflow-wrap: anywhere;

  }

  .expediente-firma__documento {

    margin-top: 3px;

    color: var(--color-texto-suave);

    font-size: 8px;

  }

  .expediente-firma__cargo {

    margin-top: 3px;

    color: var(--color-texto-suave);

    font-size: 8px;

  }


  /* =======================================================
     OBSERVACIONES Y ESPACIOS MANUSCRITOS
     ======================================================= */

  .expediente-observaciones {

    min-height: 50px;

    margin: 7px 0 10px;

    padding: 7px;

    border: 1px solid var(--color-borde);

    white-space: pre-wrap;

    overflow-wrap: anywhere;

  }

  .expediente-observaciones--amplias {

    min-height: 90px;

  }

  .expediente-lineas {

    margin: 12px 0;

  }

  .expediente-linea {

    height: 22px;

    border-bottom: 1px solid var(--color-borde);

  }


  /* =======================================================
     MENSAJES INFORMATIVOS
     ======================================================= */

  .expediente-nota {

    margin: 7px 0;

    padding: 7px 8px;

    border-left: 3px solid var(--color-secundario);

    background: var(--color-fondo-suave);

    color: var(--color-texto-suave);

    font-size: 8px;

  }

  .expediente-sin-datos {

    padding: 8px;

    border: 1px dashed var(--color-borde);

    color: var(--color-texto-suave);

    font-size: 8px;

    font-style: italic;

    text-align: center;

  }


  /* =======================================================
     PIE DE DOCUMENTO
     ======================================================= */

  .expediente-footer {

    margin-top: 14px;

    padding-top: 6px;

    border-top: 1px solid var(--color-borde);

    color: var(--color-texto-suave);

    font-size: 7.5px;

    text-align: center;

  }

  .expediente-footer__referencia {

    margin-top: 3px;

    font-size: 7px;

  }


  /* =======================================================
     UTILIDADES
     ======================================================= */

  .texto-derecha {

    text-align: right !important;

  }

  .texto-centro {

    text-align: center !important;

  }

  .texto-izquierda {

    text-align: left !important;

  }

  .texto-negrita {

    font-weight: 700 !important;

  }

  .texto-suave {

    color: var(--color-texto-suave) !important;

  }

  .texto-pequeno {

    font-size: 8px !important;

  }

  .texto-monetario {

    text-align: right !important;

    white-space: nowrap;

    font-variant-numeric: tabular-nums;

  }

  .ancho-completo {

    width: 100%;

  }

  .margen-superior {

    margin-top: 10px;

  }

  .margen-inferior {

    margin-bottom: 10px;

  }

  .sin-margen {

    margin: 0 !important;

  }


  /* =======================================================
     CONTROL DE SALTOS DE PÁGINA
     ======================================================= */

  .salto-pagina {

    break-before: page;

    page-break-before: always;

  }

  .salto-pagina-despues {

    break-after: page;

    page-break-after: always;

  }

  .evitar-salto {

    break-inside: avoid;

    page-break-inside: avoid;

  }

  .mantener-con-siguiente {

    break-after: avoid;

    page-break-after: avoid;

  }


  /* =======================================================
     VISUALIZACIÓN EN PANTALLA
     ======================================================= */

  @media screen {

    body {

      background: #e8ece9;

      padding: 20px;

    }

    .expediente {

      max-width: 216mm;

      min-height: 279mm;

      padding: 13mm 12mm 15mm;

      background: #ffffff;

      box-shadow: 0 3px 18px rgba(0, 0, 0, 0.12);

    }

    .expediente-print-actions {

      max-width: 216mm;

      margin: 0 auto 15px;

      display: flex;

      justify-content: flex-end;

      gap: 8px;

    }

    .expediente-print-actions button {

      padding: 9px 16px;

      border: 0;

      border-radius: 5px;

      background: var(--color-primario);

      color: #ffffff;

      font-family: Arial, Helvetica, sans-serif;

      font-size: 12px;

      font-weight: 700;

      cursor: pointer;

    }

    .expediente-print-actions button:hover {

      background: var(--color-primario-oscuro);

    }

  }


  /* =======================================================
     IMPRESIÓN FÍSICA / GUARDAR COMO PDF
     ======================================================= */

  @media print {

    html,
    body {

      width: auto;

      min-height: auto;

      margin: 0 !important;

      padding: 0 !important;

      background: #ffffff !important;

    }

    .expediente {

      width: 100%;

      max-width: none;

      min-height: auto;

      margin: 0;

      padding: 0;

      box-shadow: none;

    }

    .expediente-print-actions,
    .no-imprimir {

      display: none !important;

    }

    .expediente-header {

      break-inside: avoid;

      page-break-inside: avoid;

    }

    .expediente-identificacion__item,
    .expediente-campo,
    .expediente-indicador,
    .expediente-firma {

      break-inside: avoid;

      page-break-inside: avoid;

    }

    .expediente-tabla {

      break-inside: auto;

      page-break-inside: auto;

    }

    .expediente-tabla thead {

      display: table-header-group;

    }

    .expediente-tabla tfoot {

      display: table-footer-group;

    }

    .expediente-tabla tr {

      break-inside: avoid;

      page-break-inside: avoid;

    }

    .expediente-seccion__titulo,
    .expediente-seccion__subtitulo {

      break-after: avoid;

      page-break-after: avoid;

    }

    a {

      color: inherit;

      text-decoration: none;

    }

  }


  /* Perfil de riesgo: reproducción imprimible del medidor del análisis. */
  .expediente-riesgo {
    margin: 14px 0 8px;
    border: 1px solid #cbd5e1;
    background: #fff;
    break-inside: avoid;
    page-break-inside: avoid;
  }
  .expediente-riesgo__titulo {
    padding: 7px 10px;
    background: #f3f4f6;
    border-bottom: 1px solid #cbd5e1;
    text-align: center;
    font-weight: 800;
    font-size: 11px;
  }
  .expediente-riesgo__contenido {
    display: grid;
    grid-template-columns: 1fr 1.1fr;
  }
  .expediente-riesgo__datos { border-right: 1px solid #cbd5e1; }
  .expediente-riesgo__fila {
    display: grid;
    grid-template-columns: 35% 65%;
    min-height: 42px;
    border-bottom: 1px solid #cbd5e1;
  }
  .expediente-riesgo__fila:last-child { border-bottom: 0; }
  .expediente-riesgo__fila span {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 6px;
    background: #f3f4f6;
    border-right: 1px solid #cbd5e1;
    font-weight: 700;
    font-size: 9px;
    text-align: right;
  }
  .expediente-riesgo__fila strong {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 6px;
    text-align: center;
    font-size: 10px;
    overflow-wrap: anywhere;
  }
  .expediente-riesgo__perfil { background: #f0fdf4; font-size: 15px !important; }
  .expediente-riesgo--bajo { color: #059669; }
  .expediente-riesgo--medio { color: #a16207; }
  .expediente-riesgo--alto { color: #b91c1c; }
  .expediente-riesgo__medidor {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
    padding: 12px;
  }
  .expediente-riesgo__barra {
    position: relative;
    height: 48px;
    margin: 7px 7px 0;
    background: linear-gradient(to right,
      #00aa55 0%, #00aa55 20%,
      #92d050 20%, #92d050 40%,
      #ffff00 40%, #ffff00 60%,
      #ffc000 60%, #ffc000 80%,
      #c00000 80%, #c00000 100%);
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .expediente-riesgo__marcador {
    position: absolute;
    top: -7px;
    bottom: -7px;
    width: 10px;
    background: #111;
    transform: translateX(-50%);
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .expediente-riesgo__valor {
    margin-top: 8px;
    font-size: 30px;
    font-weight: 800;
    line-height: 1.1;
    text-align: right;
  }

`;
