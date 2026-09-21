/** CSS independiente: no altera el expediente integral. */
export const ORIGINACION_COMPROMISOS_PRINT_STYLES = `

  /* =========================================================
     VARIABLES GENERALES
     ========================================================= */

  :root {
    --verde: #174b35;
    --borde: #b6c4bc;
    --claro: #f5f8f6;
  }

  * {
    box-sizing: border-box;
  }

  @page {
    size: letter portrait;
    margin: 10mm 12mm 10mm;
  }

  html,
  body {
    margin: 0;
    padding: 0;
  }

  body {
    font: 8.2px/1.28 Arial, Helvetica, sans-serif;
    color: #202b25;

    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }

  .compromisos {
    width: 100%;
  }


  /* =========================================================
     ENCABEZADO INSTITUCIONAL COOPVALLE
     ========================================================= */

  .cabecera {
    display: flex;
    align-items: center;
    gap: 12px;

    width: 100%;
    margin-bottom: 7px;
  }

  .cabecera__logo-contenedor {
    flex: 0 0 100px;
    width: 100px;

    display: flex;
    align-items: center;
    justify-content: center;
  }

  .cabecera__logo {
    display: block;

    width: 100%;
    max-height: 65px;

    object-fit: contain;
  }

  .cabecera__contenido {
    flex: 1 1 auto;
    min-width: 0;

    text-align: center;
  }

  .cabecera__empresa {
    font-size: 10px;
    font-weight: 700;
    line-height: 1.25;
  }

  .cabecera__nombre {
    font-size: 9px;
    font-weight: 700;
  }

  .cabecera__nit {
    font-size: 8px;
  }

  .cabecera__titulo {
    margin: 6px 0 2px;
    padding: 5px 6px;

    background: var(--verde);
    color: white;

    font-size: 10px;
    font-weight: 700;
    text-transform: uppercase;
  }

  .referencia {
    font-size: 7px;
    color: #55645c;
  }


  /* =========================================================
     DECLARACIÓN Y CLÁUSULAS
     ========================================================= */

  .declaracion {
    margin: 5px 0;
  }

  .clausulas {
    margin: 4px 0 7px;
    padding-left: 17px;

    text-align: justify;
  }

  .clausulas li {
    margin: 0 0 4px;
    padding-left: 2px;
  }


  /* =========================================================
     CUENTA DE AHORROS
     ========================================================= */

  .cuenta {
    border: 1px solid var(--borde);

    padding: 5px 7px;
    margin: 5px 0;
  }

  .cuenta strong {
    color: var(--verde);
  }


  /* =========================================================
     TÍTULOS DE SECCIONES
     ========================================================= */

  .seccion {
    margin: 7px 0 3px;
    padding: 4px 6px;

    background: var(--claro);
    border-bottom: 1px solid var(--verde);

    font-weight: 700;
    font-size: 8px;
  }


  /* =========================================================
     FIRMAS DE DEUDOR PRINCIPAL Y CODEUDORES
     ========================================================= */

  .firmas {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));

    column-gap: 14px;
    row-gap: 14px;

    margin: 12px 0 10px;
  }

  .firma {
    min-width: 0;

    break-inside: avoid;
    page-break-inside: avoid;
  }

  .firma__linea {
    height: 42px;
    border-bottom: 1px solid #66776c;
  }

  .firma__rol {
    margin-top: 3px;

    font-size: 7px;
    color: var(--verde);
    font-weight: 700;
  }

  .firma__nombre {
    font-size: 8px;
    font-weight: 700;

    overflow-wrap: anywhere;
  }

  .firma__documento {
    font-size: 7px;
    color: #4e5c54;
  }


  /* =========================================================
     TABLA DE INFORME DE CARTERA
     ========================================================= */

  .tabla {
    width: 100%;

    border-collapse: collapse;
    table-layout: fixed;

    margin: 3px 0 5px;
  }

  .tabla th,
  .tabla td {
    border: 1px solid var(--borde);

    padding: 4px 5px;

    overflow-wrap: anywhere;
  }

  .tabla th {
    background: var(--claro);

    font-size: 7.3px;
    text-align: left;
  }

  .tabla .numero {
    text-align: right;

    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }


  /* =========================================================
     INDICADORES FINANCIEROS DEL DEUDOR PRINCIPAL
     ========================================================= */

  .indicadores {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));

    border: 1px solid var(--borde);

    margin: 5px 0;
  }

  .indicador {
    padding: 4px 5px;
    min-width: 0;

    border-right: 1px solid var(--borde);
  }

  .indicador:last-child {
    border-right: 0;
  }

  .indicador__titulo {
    display: block;

    font-size: 6.8px;
    color: #4e5c54;

    text-transform: uppercase;
  }

  .indicador__valor {
    display: block;

    text-align: right;

    font-size: 8px;
    font-weight: 700;

    font-variant-numeric: tabular-nums;
  }


  /* =========================================================
     OBSERVACIONES
     ========================================================= */

  .observaciones {
    border: 1px solid var(--borde);

    margin-top: 5px;
    padding: 5px 6px;
  }

  .observaciones__titulo {
    font-weight: 700;
    color: var(--verde);
  }

  .observaciones__linea {
    height: 20px;
    border-bottom: 1px solid #c9d3cd;
  }


  /* =========================================================
     FIRMA INSTITUCIONAL
     ========================================================= */

  .jefe {
    margin: 28px auto 0;
    width: 43%;
    text-align: center;
  }

  .jefe__linea {
    border-bottom: 1px solid #66776c;
    height: 30px;
  }

  .jefe__linea {
    border-bottom: 1px solid #66776c;
    height: 16px;
  }

  .jefe__texto {
    margin-top: 4px;

    font-size: 8px;
    font-weight: 700;
  }


  /* =========================================================
     BOTÓN DE IMPRESIÓN
     ========================================================= */

  .acciones {
    padding: 10px;

    text-align: right;
    background: #eef2ef;
  }

  .acciones button {
    background: var(--verde);
    color: white;

    border: 0;
    padding: 8px 15px;

    border-radius: 4px;
    cursor: pointer;
  }


  /* =========================================================
     VISTA EN PANTALLA
     ========================================================= */

  @media screen {

    body {
      background: #e9eeea;
      padding: 12px;
    }

    .compromisos {
      width: 216mm;
      min-height: 279mm;

      margin: auto;
      padding: 10mm 12mm;

      background: white;
      box-shadow: 0 2px 14px #0002;
    }

    .acciones {
      max-width: 216mm;
      margin: 0 auto 10px;
    }

  }


  /* =========================================================
     IMPRESIÓN TAMAÑO CARTA
     ========================================================= */

  @media print {

    .acciones {
      display: none !important;
    }

    body {
      background: white;
      padding: 0;
    }

    .compromisos {
      width: 100%;
      min-height: auto;

      margin: 0;
      padding: 0;

      box-shadow: none;
    }

    .cabecera,
    .firmas,
    .firma,
    .indicadores,
    .observaciones,
    .jefe,
    .tabla tr {
      break-inside: avoid;
      page-break-inside: avoid;
    }

  }

`;
