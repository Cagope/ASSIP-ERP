
package co.assip.erp.contabilidad.informes.pyg.individual;

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
 * Exporta el Estado de Excedentes o Pérdidas con formato individual.
 * Conserva importes numéricos, jerarquía contable y resultados del período.
 * El archivo Excel no incorpora firmas institucionales.
 */
@Service
public class PyGIndividualExcelService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final short AZUL = IndexedColors.DARK_BLUE.getIndex();
    private static final short GRIS = IndexedColors.GREY_25_PERCENT.getIndex();
    private static final short CELESTE = IndexedColors.PALE_BLUE.getIndex();
    private static final String FORMATO_NUMERO =
            "#,##0.00;[Red](#,##0.00)";

    /**
     * Genera el libro Excel a partir del resultado autorizado.
     * No consulta movimientos, no recalcula saldos y no incluye firmas.
     */
    public byte[] generar(
            PyGIndividualService.ResultadoPyG resultado,
            String codigoAgencia,
            String nombreAgencia
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado del PyG individual es obligatorio."
            );
        }

        try (
                XSSFWorkbook libro = new XSSFWorkbook();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            Sheet hoja = libro.createSheet("PyG individual");
            hoja.setDisplayGridlines(false);

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
            CellStyle cabecera = crearEstilo(
                    libro, true, (short) 10, AZUL, false
            );
            CellStyle cabeceraNumero = crearEstilo(
                    libro, true, (short) 10, AZUL, true
            );
            CellStyle grupoTexto = crearEstilo(
                    libro, true, (short) 10, GRIS, false
            );
            CellStyle grupoNumero = crearEstilo(
                    libro, true, (short) 10, GRIS, true
            );
            CellStyle totalTexto = crearEstilo(
                    libro, true, (short) 10, CELESTE, false
            );
            CellStyle totalNumero = crearEstilo(
                    libro, true, (short) 10, CELESTE, true
            );
            CellStyle resultadoTexto = crearEstilo(
                    libro, true, (short) 11, CELESTE, false
            );
            CellStyle resultadoNumero = crearEstilo(
                    libro, true, (short) 11, CELESTE, true
            );

            String nombrePeriodo = periodo(
                    resultado.ano(), resultado.mes()
            );

            int indice = 0;

            indice = agregarTitulo(
                    hoja,
                    indice,
                    "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE",
                    titulo
            );
            indice = agregarTitulo(
                    hoja,
                    indice,
                    "ESTADO DE EXCEDENTES O PERDIDAS - INDIVIDUAL",
                    subtitulo
            );
            indice = agregarTitulo(
                    hoja,
                    indice,
                    "Agencia: " + texto(codigoAgencia)
                            + " - " + texto(nombreAgencia),
                    negrita
            );
            indice = agregarTitulo(
                    hoja,
                    indice,
                    "Período: " + nombrePeriodo,
                    texto
            );
            indice = agregarTitulo(
                    hoja,
                    indice,
                    resultado.incluirDetalle()
                            ? "Presentación: Con detalle de subcuentas"
                            : "Presentación: Sin detalle de subcuentas",
                    texto
            );
            indice = agregarTitulo(
                    hoja,
                    indice,
                    "Generado: " + LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    ),
                    texto
            );

            indice++;

            int filaCabecera = indice;
            Row encabezado = hoja.createRow(indice++);
            encabezado.setHeightInPoints(25);

            crearCeldaTexto(
                    encabezado, 0, "Código", cabecera
            );
            crearCeldaTexto(
                    encabezado, 1, "Concepto", cabecera
            );
            crearCeldaTexto(
                    encabezado, 2, nombrePeriodo, cabeceraNumero
            );

            if (resultado.filas() != null) {
                for (PyGIndividualService.FilaPyG concepto
                        : resultado.filas()) {
                    Row registro = hoja.createRow(indice++);

                    boolean esGrupo =
                            "GRUPO".equals(concepto.tipo());
                    boolean esTotal =
                            "TOTAL".equals(concepto.tipo());
                    boolean esResultado =
                            "RESULTADO".equals(concepto.tipo());

                    CellStyle estiloTexto;
                    CellStyle estiloNumero;

                    if (esResultado) {
                        estiloTexto = resultadoTexto;
                        estiloNumero = resultadoNumero;
                        registro.setHeightInPoints(26);
                    } else if (esTotal) {
                        estiloTexto = totalTexto;
                        estiloNumero = totalNumero;
                        registro.setHeightInPoints(23);
                    } else if (esGrupo) {
                        estiloTexto = grupoTexto;
                        estiloNumero = grupoNumero;
                        registro.setHeightInPoints(22);
                    } else {
                        estiloTexto = texto;
                        estiloNumero = numero;
                    }

                    crearCeldaTexto(
                            registro,
                            0,
                            concepto.codigo(),
                            estiloTexto
                    );

                    String sangria = "CUENTA".equals(concepto.tipo())
                            ? "  ".repeat(
                            Math.max(0, concepto.nivel() - 2)
                    )
                            : "";

                    crearCeldaTexto(
                            registro,
                            1,
                            sangria + texto(concepto.nombre()),
                            estiloTexto
                    );

                    crearCeldaNumero(
                            registro,
                            2,
                            concepto.valor(),
                            estiloNumero
                    );
                }
            }

            int ultimaFilaDetalle = Math.max(
                    filaCabecera, indice - 1
            );

            configurarHoja(
                    hoja,
                    filaCabecera,
                    ultimaFilaDetalle
            );

            libro.write(salida);
            return salida.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el Excel del PyG individual.",
                    e
            );
        }
    }

    /**
     * Crea estilos reutilizables para textos e importes.
     * Los importes permanecen numéricos y alineados a la derecha.
     */
    private CellStyle crearEstilo(
            Workbook libro,
            boolean negrita,
            short tamano,
            Short fondo,
            boolean numerico
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

        if (numerico) {
            estilo.setAlignment(HorizontalAlignment.RIGHT);
            estilo.setDataFormat(
                    libro.createDataFormat().getFormat(
                            FORMATO_NUMERO
                    )
            );
        } else {
            estilo.setAlignment(HorizontalAlignment.LEFT);
        }

        return estilo;
    }

    /**
     * Agrega un renglón institucional sobre las tres columnas.
     * Mantiene separados encabezados y datos financieros.
     */
    private int agregarTitulo(
            Sheet hoja,
            int indice,
            String contenido,
            CellStyle estilo
    ) {
        Row fila = hoja.createRow(indice);
        fila.setHeightInPoints(22);

        crearCeldaTexto(
                fila, 0, contenido, estilo
        );

        hoja.addMergedRegion(
                new CellRangeAddress(
                        indice, indice, 0, 2
                )
        );

        return indice + 1;
    }

    /** Escribe códigos y descripciones como texto. */
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

    /**
     * Escribe importes como valores numéricos reales.
     * Permite operaciones y fórmulas posteriores en Excel.
     */
    private void crearCeldaNumero(
            Row fila,
            int columna,
            BigDecimal importe,
            CellStyle estilo
    ) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(
                importe == null ? 0.0 : importe.doubleValue()
        );
        celda.setCellStyle(estilo);
    }

    /**
     * Configura anchos, navegación, filtros e impresión.
     * Repite la cabecera y ajusta el informe a una página de ancho.
     */
    private void configurarHoja(
            Sheet hoja,
            int filaCabecera,
            int ultimaFilaDetalle
    ) {
        hoja.setColumnWidth(0, 18 * 256);
        hoja.setColumnWidth(1, 75 * 256);
        hoja.setColumnWidth(2, 25 * 256);

        hoja.createFreezePane(2, filaCabecera + 1);

        hoja.setAutoFilter(
                new CellRangeAddress(
                        filaCabecera,
                        Math.max(filaCabecera, ultimaFilaDetalle),
                        0,
                        2
                )
        );

        hoja.setFitToPage(true);

        PrintSetup impresion = hoja.getPrintSetup();
        impresion.setLandscape(false);
        impresion.setPaperSize(PrintSetup.A4_PAPERSIZE);
        impresion.setFitWidth((short) 1);
        impresion.setFitHeight((short) 0);

        hoja.setRepeatingRows(
                new CellRangeAddress(
                        filaCabecera, filaCabecera, 0, 2
                )
        );

        hoja.setMargin(Sheet.LeftMargin, 0.30);
        hoja.setMargin(Sheet.RightMargin, 0.30);
        hoja.setMargin(Sheet.TopMargin, 0.50);
        hoja.setMargin(Sheet.BottomMargin, 0.50);

        Footer pie = hoja.getFooter();
        pie.setLeft(
                "Estado de Excedentes o Pérdidas - Individual"
        );
        pie.setCenter("Página &P de &N");
        pie.setRight("ASSIP ERP");
    }

    /** Obtiene el nombre del mes y año del informe. */
    private String periodo(int ano, int mes) {
        return Month.of(mes)
                .getDisplayName(TextStyle.FULL, COLOMBIA)
                .toUpperCase(COLOMBIA)
                + " " + ano;
    }

    /** Normaliza textos opcionales sin generar valores ficticios. */
    private String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
