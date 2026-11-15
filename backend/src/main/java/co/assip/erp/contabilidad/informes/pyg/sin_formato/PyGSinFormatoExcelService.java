
package co.assip.erp.contabilidad.informes.pyg.sin_formato;

import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualRespuestaDTO.FirmanteDTO;
import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualRespuestaDTO.FirmasDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Exporta el Estado de Excedentes o Pérdidas sin formato a Excel.
 * Conserva los importes contables como valores numéricos.
 */
@Service
public class PyGSinFormatoExcelService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final String NOMBRE_COOPERATIVA =
            "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE";

    /**
     * Genera el informe mensual con sus cuentas, resumen y firmas.
     * Recibe resultados previamente calculados y autorizados.
     */
    public byte[] generar(
            PyGSinFormatoService.ResultadoPyG resultado,
            String codigoAgencia,
            String nombreAgencia,
            FirmasDTO firmas
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado PyG es obligatorio."
            );
        }

        try (
                XSSFWorkbook libro = new XSSFWorkbook();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            Sheet hoja = libro.createSheet("Excedentes y perdidas");

            CellStyle titulo = crearEstilo(
                    libro, true, (short) 14, null, false
            );
            CellStyle subtitulo = crearEstilo(
                    libro, true, (short) 11, null, false
            );
            CellStyle texto = crearEstilo(
                    libro, false, (short) 10, null, false
            );
            CellStyle negrita = crearEstilo(
                    libro, true, (short) 10, null, false
            );
            CellStyle numero = crearEstilo(
                    libro, false, (short) 10, null, true
            );
            CellStyle numeroNegrita = crearEstilo(
                    libro, true, (short) 10, null, true
            );

            short azul = IndexedColors.PALE_BLUE.getIndex();
            short gris = IndexedColors.GREY_25_PERCENT.getIndex();

            CellStyle cabecera = crearEstilo(
                    libro, true, (short) 10, azul, false
            );
            CellStyle grupo = crearEstilo(
                    libro, true, (short) 10, gris, false
            );
            CellStyle grupoNumero = crearEstilo(
                    libro, true, (short) 10, gris, true
            );
            CellStyle resultadoTexto = crearEstilo(
                    libro, true, (short) 10, azul, false
            );
            CellStyle resultadoNumero = crearEstilo(
                    libro, true, (short) 10, azul, true
            );

            int fila = 0;

            fila = agregarTitulo(
                    hoja, fila, NOMBRE_COOPERATIVA, titulo
            );
            fila = agregarTitulo(
                    hoja, fila,
                    "ESTADO DE EXCEDENTES O PERDIDAS - SIN FORMATO",
                    subtitulo
            );

            fila = agregarTitulo(
                    hoja, fila,
                    "Agencia: " + texto(codigoAgencia)
                            + " - " + texto(nombreAgencia),
                    negrita
            );

            String mes = Month.of(resultado.mes())
                    .getDisplayName(TextStyle.FULL, COLOMBIA)
                    .toUpperCase(COLOMBIA);

            fila = agregarTitulo(
                    hoja, fila,
                    "Período: " + mes + " DE " + resultado.ano(),
                    texto
            );

            fila = agregarTitulo(
                    hoja, fila,
                    "Generado: " + LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    ),
                    texto
            );

            fila++;

            int filaCabecera = fila;

            Row encabezado = hoja.createRow(fila++);
            crearCeldaTexto(encabezado, 0, "Código", cabecera);
            crearCeldaTexto(encabezado, 1, "Cuenta contable", cabecera);
            crearCeldaTexto(encabezado, 2, "Débitos", cabecera);
            crearCeldaTexto(encabezado, 3, "Créditos", cabecera);
            crearCeldaTexto(encabezado, 4, "Saldo", cabecera);

            if (resultado.cuentas() != null) {
                for (PyGSinFormatoService.CuentaPyG cuenta
                        : resultado.cuentas()) {

                    Row registro = hoja.createRow(fila++);

                    boolean agrupadora = cuenta.nivelCuenta() <= 2;
                    CellStyle estiloTexto = agrupadora ? grupo : texto;
                    CellStyle estiloNumero = agrupadora
                            ? grupoNumero
                            : numero;

                    crearCeldaTexto(
                            registro, 0, cuenta.codigoCuenta(), estiloTexto
                    );

                    String sangria = "  ".repeat(
                            Math.max(0, cuenta.nivelCuenta() - 1)
                    );

                    crearCeldaTexto(
                            registro, 1,
                            sangria + texto(cuenta.nombreCuenta()),
                            estiloTexto
                    );

                    crearCeldaNumero(
                            registro, 2, cuenta.debitos(), estiloNumero
                    );
                    crearCeldaNumero(
                            registro, 3, cuenta.creditos(), estiloNumero
                    );
                    crearCeldaNumero(
                            registro, 4, cuenta.saldo(), estiloNumero
                    );
                }
            }

            int ultimaFilaDetalle = Math.max(filaCabecera, fila - 1);

            fila += 2;

            fila = agregarTitulo(
                    hoja, fila, "RESUMEN DEL PERÍODO", subtitulo
            );

            fila = agregarResumen(
                    hoja, fila,
                    "TOTAL INGRESOS",
                    resultado.totalIngresos(),
                    negrita, numeroNegrita
            );

            fila = agregarResumen(
                    hoja, fila,
                    "TOTAL COSTOS Y GASTOS",
                    resultado.totalCostosGastos(),
                    negrita, numeroNegrita
            );

            boolean excedente =
                    resultado.excedentePerdida().signum() >= 0;

            fila = agregarResumen(
                    hoja, fila,
                    excedente
                            ? "EXCEDENTE DEL PERÍODO"
                            : "PÉRDIDA DEL PERÍODO",
                    resultado.excedentePerdida(),
                    resultadoTexto, resultadoNumero
            );

            if (firmas != null) {
                fila += 3;
                fila = agregarFirmante(
                        hoja, fila, firmas.firmante1(),
                        negrita, texto
                );
                fila = agregarFirmante(
                        hoja, fila, firmas.firmante2(),
                        negrita, texto
                );
                agregarFirmante(
                        hoja, fila, firmas.firmante3(),
                        negrita, texto
                );
            }

            configurarHoja(
                    hoja, filaCabecera, ultimaFilaDetalle
            );

            libro.write(salida);
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el Excel de Excedentes o Pérdidas.",
                    e
            );
        }
    }

    /**
     * Crea un estilo reutilizable con formato monetario colombiano.
     * No convierte los importes en cadenas.
     */
    private CellStyle crearEstilo(
            Workbook libro,
            boolean negrita,
            short tamano,
            Short fondo,
            boolean numerico
    ) {
        CellStyle estilo = libro.createCellStyle();

        org.apache.poi.ss.usermodel.Font fuente =
                libro.createFont();
        fuente.setFontName("Arial");
        fuente.setFontHeightInPoints(tamano);
        fuente.setBold(negrita);
        estilo.setFont(fuente);

        estilo.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        if (fondo != null) {
            estilo.setFillForegroundColor(fondo);
            estilo.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );
        }

        if (numerico) {
            estilo.setAlignment(
                    HorizontalAlignment.RIGHT
            );
            estilo.setDataFormat(
                    libro.createDataFormat().getFormat(
                            "#,##0.00;[Red](#,##0.00)"
                    )
            );
        } else {
            estilo.setAlignment(
                    HorizontalAlignment.LEFT
            );
        }

        return estilo;
    }

    /** Agrega una línea de identificación del informe. */
    private int agregarTitulo(
            Sheet hoja,
            int indice,
            String contenido,
            CellStyle estilo
    ) {
        Row fila = hoja.createRow(indice);
        crearCeldaTexto(fila, 0, contenido, estilo);
        hoja.addMergedRegion(
                new CellRangeAddress(indice, indice, 0, 4)
        );
        return indice + 1;
    }

    /** Agrega un concepto y su importe al resumen financiero. */
    private int agregarResumen(
            Sheet hoja,
            int indice,
            String concepto,
            BigDecimal importe,
            CellStyle estiloTexto,
            CellStyle estiloNumero
    ) {
        Row fila = hoja.createRow(indice);
        crearCeldaTexto(
                fila, 1, concepto, estiloTexto
        );
        crearCeldaNumero(
                fila, 4, importe, estiloNumero
        );
        return indice + 1;
    }

    /**
     * Agrega un firmante configurado.
     * Omite registros sin nombre y conserva la tarjeta profesional.
     */
    private int agregarFirmante(
            Sheet hoja,
            int indice,
            FirmanteDTO firmante,
            CellStyle negrita,
            CellStyle texto
    ) {
        if (firmante == null
                || firmante.nombre() == null
                || firmante.nombre().isBlank()) {
            return indice;
        }

        indice = agregarTitulo(
                hoja, indice,
                "________________________________",
                texto
        );

        indice = agregarTitulo(
                hoja, indice,
                firmante.nombre(),
                negrita
        );

        String cargo = texto(firmante.titulo());

        if (firmante.tarjetaProfesional() != null
                && !firmante.tarjetaProfesional().isBlank()) {
            cargo += " - T.P. "
                    + firmante.tarjetaProfesional();
        }

        indice = agregarTitulo(
                hoja, indice, cargo, texto
        );

        return indice + 2;
    }

    /** Escribe una celda de texto con su estilo. */
    private void crearCeldaTexto(
            Row fila,
            int columna,
            String contenido,
            CellStyle estilo
    ) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(texto(contenido));
        celda.setCellStyle(estilo);
    }

    /** Escribe un importe como celda numérica real. */
    private void crearCeldaNumero(
            Row fila,
            int columna,
            BigDecimal valor,
            CellStyle estilo
    ) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(
                valor == null ? 0.0 : valor.doubleValue()
        );
        celda.setCellStyle(estilo);
    }

    /** Configura anchos, filtros, inmovilización e impresión. */
    private void configurarHoja(
            Sheet hoja,
            int filaCabecera,
            int ultimaFilaDetalle
    ) {
        hoja.setColumnWidth(0, 17 * 256);
        hoja.setColumnWidth(1, 58 * 256);
        hoja.setColumnWidth(2, 23 * 256);
        hoja.setColumnWidth(3, 23 * 256);
        hoja.setColumnWidth(4, 23 * 256);

        hoja.createFreezePane(2, filaCabecera + 1);

        hoja.setAutoFilter(
                new CellRangeAddress(
                        filaCabecera,
                        Math.max(filaCabecera, ultimaFilaDetalle),
                        0,
                        4
                )
        );

        hoja.setFitToPage(true);

        PrintSetup impresion = hoja.getPrintSetup();
        impresion.setLandscape(true);
        impresion.setPaperSize(
                PrintSetup.A4_PAPERSIZE
        );
        impresion.setFitWidth((short) 1);
        impresion.setFitHeight((short) 0);

        hoja.setRepeatingRows(
                new CellRangeAddress(
                        filaCabecera, filaCabecera, 0, 4
                )
        );

        hoja.setMargin(
                Sheet.LeftMargin, 0.30
        );
        hoja.setMargin(
                Sheet.RightMargin, 0.30
        );
        hoja.setMargin(
                Sheet.TopMargin, 0.50
        );
        hoja.setMargin(
                Sheet.BottomMargin, 0.50
        );

        Footer pie = hoja.getFooter();
        pie.setLeft("Estado de Excedentes o Pérdidas");
        pie.setCenter("Página &P de &N");
        pie.setRight("ASSIP ERP");
    }

    /** Evita valores nulos en las descripciones. */
    private String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
