
package co.assip.erp.contabilidad.informes.pyg.comparado;

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
 * Exporta el Estado de Excedentes o Pérdidas con formato comparado.
 * Conserva los importes como números y las firmas institucionales opcionales.
 */
@Service
public class PyGComparadoExcelService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final short AZUL = IndexedColors.DARK_BLUE.getIndex();
    private static final short GRIS = IndexedColors.GREY_25_PERCENT.getIndex();
    private static final short CELESTE = IndexedColors.PALE_BLUE.getIndex();
    private static final String FORMATO_NUMERO =
            "#,##0.00;[Red](#,##0.00)";
    private static final String FORMATO_PORCENTAJE =
            "#,##0.00\"%\";[Red](#,##0.00\"%\")";

    /**
     * Genera un libro Excel a partir del resultado autorizado.
     * No consulta ni recalcula movimientos contables.
     */
    public byte[] generar(
            PyGComparadoService.ResultadoPyG resultado,
            String codigoAgencia,
            String nombreAgencia,
            FirmasDTO firmas
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado del PyG comparado es obligatorio."
            );
        }

        try (
                XSSFWorkbook libro = new XSSFWorkbook();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            Sheet hoja = libro.createSheet("PyG comparado");

            CellStyle titulo = estilo(
                    libro, true, (short) 14, null, null
            );
            CellStyle subtitulo = estilo(
                    libro, true, (short) 11, null, null
            );
            CellStyle texto = estilo(
                    libro, false, (short) 10, null, null
            );
            CellStyle negrita = estilo(
                    libro, true, (short) 10, null, null
            );
            CellStyle numero = estilo(
                    libro, false, (short) 10, null, FORMATO_NUMERO
            );
            CellStyle numeroNegrita = estilo(
                    libro, true, (short) 10, null, FORMATO_NUMERO
            );
            CellStyle porcentaje = estilo(
                    libro, false, (short) 10, null, FORMATO_PORCENTAJE
            );
            CellStyle cabecera = estilo(
                    libro, true, (short) 10, AZUL, null
            );
            CellStyle grupoTexto = estilo(
                    libro, true, (short) 10, GRIS, null
            );
            CellStyle grupoNumero = estilo(
                    libro, true, (short) 10, GRIS, FORMATO_NUMERO
            );
            CellStyle grupoPorcentaje = estilo(
                    libro, true, (short) 10, GRIS, FORMATO_PORCENTAJE
            );
            CellStyle totalTexto = estilo(
                    libro, true, (short) 10, CELESTE, null
            );
            CellStyle totalNumero = estilo(
                    libro, true, (short) 10, CELESTE, FORMATO_NUMERO
            );
            CellStyle totalPorcentaje = estilo(
                    libro, true, (short) 10, CELESTE, FORMATO_PORCENTAJE
            );

            String periodo1 = periodo(
                    resultado.anoPeriodo1(), resultado.mesPeriodo1()
            );
            String periodo2 = periodo(
                    resultado.anoPeriodo2(), resultado.mesPeriodo2()
            );

            int fila = 0;

            fila = agregarTitulo(
                    hoja, fila,
                    "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE",
                    titulo
            );
            fila = agregarTitulo(
                    hoja, fila,
                    "ESTADO DE EXCEDENTES O PERDIDAS - COMPARADO",
                    subtitulo
            );
            fila = agregarTitulo(
                    hoja, fila,
                    "Agencia: " + texto(codigoAgencia)
                            + " - " + texto(nombreAgencia),
                    negrita
            );
            fila = agregarTitulo(
                    hoja, fila,
                    "Período 1: " + periodo1
                            + "  |  Período 2: " + periodo2,
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

            String[] columnas = {
                    "Código",
                    "Concepto",
                    periodo1,
                    periodo2,
                    "Variación $",
                    "Variación %"
            };

            for (int columna = 0; columna < columnas.length; columna++) {
                crearCeldaTexto(
                        encabezado, columna, columnas[columna], cabecera
                );
            }

            if (resultado.filas() != null) {
                for (PyGComparadoService.FilaPyG concepto
                        : resultado.filas()) {

                    Row registro = hoja.createRow(fila++);

                    boolean resultadoFinal =
                            "RESULTADO".equals(concepto.tipo());
                    boolean total =
                            "TOTAL".equals(concepto.tipo());
                    boolean grupo =
                            "GRUPO".equals(concepto.tipo());

                    CellStyle estiloTexto;
                    CellStyle estiloNumero;
                    CellStyle estiloPorcentaje;

                    if (resultadoFinal || total) {
                        estiloTexto = totalTexto;
                        estiloNumero = totalNumero;
                        estiloPorcentaje = totalPorcentaje;
                    } else if (grupo) {
                        estiloTexto = grupoTexto;
                        estiloNumero = grupoNumero;
                        estiloPorcentaje = grupoPorcentaje;
                    } else {
                        estiloTexto = texto;
                        estiloNumero = numero;
                        estiloPorcentaje = porcentaje;
                    }

                    crearCeldaTexto(
                            registro, 0, concepto.codigo(), estiloTexto
                    );
                    crearCeldaTexto(
                            registro, 1, concepto.nombre(), estiloTexto
                    );
                    crearCeldaNumero(
                            registro, 2, concepto.periodo1(), estiloNumero
                    );
                    crearCeldaNumero(
                            registro, 3, concepto.periodo2(), estiloNumero
                    );
                    crearCeldaNumero(
                            registro, 4, concepto.variacion(), estiloNumero
                    );
                    crearCeldaNumero(
                            registro, 5, concepto.porcentaje(),
                            estiloPorcentaje
                    );

                    if (resultadoFinal) {
                        registro.setHeightInPoints(24);
                    }
                }
            }

            int ultimaFilaDetalle = Math.max(
                    filaCabecera, fila - 1
            );

            if (firmas != null) {
                fila += 3;
                fila = agregarFirmante(
                        hoja, fila, firmas.firmante1(), negrita, texto
                );
                fila = agregarFirmante(
                        hoja, fila, firmas.firmante2(), negrita, texto
                );
                agregarFirmante(
                        hoja, fila, firmas.firmante3(), negrita, texto
                );
            }

            configurarHoja(
                    hoja, filaCabecera, ultimaFilaDetalle
            );

            libro.write(salida);
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el Excel del PyG comparado.",
                    e
            );
        }
    }

    /**
     * Crea estilos reutilizables para textos, importes y porcentajes.
     * Los porcentajes recibidos ya están expresados en unidades porcentuales.
     */
    private CellStyle estilo(
            Workbook libro,
            boolean negrita,
            short tamano,
            Short fondo,
            String formato
    ) {
        CellStyle estilo = libro.createCellStyle();

        Font fuente = libro.createFont();
        fuente.setFontName("Arial");
        fuente.setFontHeightInPoints(tamano);
        fuente.setBold(negrita);

        if (fondo != null && fondo == AZUL) {
            fuente.setColor(IndexedColors.WHITE.getIndex());
        }

        estilo.setFont(fuente);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);

        if (fondo != null) {
            estilo.setFillForegroundColor(fondo);
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        if (formato != null) {
            estilo.setDataFormat(
                    libro.createDataFormat().getFormat(formato)
            );
            estilo.setAlignment(HorizontalAlignment.RIGHT);
        } else {
            estilo.setAlignment(HorizontalAlignment.LEFT);
        }

        return estilo;
    }

    /** Inserta un renglón institucional ocupando las seis columnas. */
    private int agregarTitulo(
            Sheet hoja,
            int indice,
            String contenido,
            CellStyle estilo
    ) {
        Row registro = hoja.createRow(indice);
        crearCeldaTexto(registro, 0, contenido, estilo);

        hoja.addMergedRegion(
                new CellRangeAddress(indice, indice, 0, 5)
        );

        return indice + 1;
    }

    /** Escribe los códigos y conceptos como texto. */
    private void crearCeldaTexto(
            Row registro,
            int columna,
            String contenido,
            CellStyle estilo
    ) {
        Cell celda = registro.createCell(columna);
        celda.setCellValue(texto(contenido));
        celda.setCellStyle(estilo);
    }

    /**
     * Escribe valores numéricos reales.
     * Un porcentaje indefinido permanece vacío, no se reemplaza por cero.
     */
    private void crearCeldaNumero(
            Row registro,
            int columna,
            BigDecimal valor,
            CellStyle estilo
    ) {
        Cell celda = registro.createCell(columna);

        if (valor != null) {
            celda.setCellValue(valor.doubleValue());
        }

        celda.setCellStyle(estilo);
    }

    /**
     * Incorpora los datos del firmante configurado para la agencia.
     * No genera nombres, cargos ni tarjetas profesionales ficticios.
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
            cargo += " - T.P. " + firmante.tarjetaProfesional();
        }

        indice = agregarTitulo(
                hoja, indice, cargo, texto
        );

        return indice + 2;
    }

    /**
     * Configura navegación, filtros e impresión horizontal.
     * Repite los encabezados al imprimir varias páginas.
     */
    private void configurarHoja(
            Sheet hoja,
            int filaCabecera,
            int ultimaFilaDetalle
    ) {
        hoja.setColumnWidth(0, 14 * 256);
        hoja.setColumnWidth(1, 54 * 256);
        hoja.setColumnWidth(2, 23 * 256);
        hoja.setColumnWidth(3, 23 * 256);
        hoja.setColumnWidth(4, 23 * 256);
        hoja.setColumnWidth(5, 18 * 256);

        hoja.createFreezePane(2, filaCabecera + 1);

        hoja.setAutoFilter(
                new CellRangeAddress(
                        filaCabecera,
                        Math.max(filaCabecera, ultimaFilaDetalle),
                        0,
                        5
                )
        );

        hoja.setFitToPage(true);

        PrintSetup impresion = hoja.getPrintSetup();
        impresion.setLandscape(true);
        impresion.setPaperSize(PrintSetup.A4_PAPERSIZE);
        impresion.setFitWidth((short) 1);
        impresion.setFitHeight((short) 0);

        hoja.setRepeatingRows(
                new CellRangeAddress(
                        filaCabecera, filaCabecera, 0, 5
                )
        );

        hoja.setMargin(Sheet.LeftMargin, 0.30);
        hoja.setMargin(Sheet.RightMargin, 0.30);
        hoja.setMargin(Sheet.TopMargin, 0.50);
        hoja.setMargin(Sheet.BottomMargin, 0.50);

        Footer pie = hoja.getFooter();
        pie.setLeft("Estado de Excedentes o Pérdidas - Comparado");
        pie.setCenter("Página &P de &N");
        pie.setRight("&D &T");

        hoja.setDisplayGridlines(false);
    }

    /** Formatea cada período respetando el mes y año seleccionados. */
    private String periodo(int ano, int mes) {
        return Month.of(mes)
                .getDisplayName(TextStyle.FULL, COLOMBIA)
                .toUpperCase(COLOMBIA)
                + " " + ano;
    }

    /** Normaliza los campos de texto opcionales. */
    private String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
