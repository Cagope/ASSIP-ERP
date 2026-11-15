
package co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual;

import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualFilaDTO;
import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualRespuestaDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class BalanceDetalladoMensualPdfService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    private static final Color AZUL = new Color(23, 59, 92);
    private static final Color AZUL_CABECERA = new Color(220, 232, 243);
    private static final Color AZUL_DESTACADO = new Color(225, 237, 247);
    private static final Color BORDE = new Color(213, 224, 235);
    private static final Color TEXTO = new Color(35, 48, 59);

    private static final Font TITULO =
            new Font(Font.HELVETICA, 13, Font.BOLD, AZUL);
    private static final Font SUBTITULO =
            new Font(Font.HELVETICA, 9, Font.BOLD, AZUL);
    private static final Font CABECERA =
            new Font(Font.HELVETICA, 7, Font.BOLD, AZUL);
    private static final Font NORMAL =
            new Font(Font.HELVETICA, 7, Font.NORMAL, TEXTO);
    private static final Font DESTACADO =
            new Font(Font.HELVETICA, 7, Font.BOLD, AZUL);
    private static final Font DESCRIPCION =
            new Font(Font.HELVETICA, 7, Font.NORMAL, TEXTO);

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Genera el PDF según la modalidad mensual o comparativa. */
    public byte[] generar(BalanceDetalladoMensualRespuestaDTO balance) {
        if (balance == null) {
            throw new IllegalArgumentException("El balance es obligatorio.");
        }

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(
                    PageSize.A4.rotate(), 29, 29, 52, 38
            );

            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();

            agregarEncabezado(documento, balance);
            agregarResumen(documento, balance);
            agregarTituloSeccion(documento, "DETALLE CONTABLE");

            PdfPTable tabla = crearTabla(balance);
            agregarFilas(
                    tabla,
                    balance.filas(),
                    balance.compararAnoAnterior()
            );
            documento.add(tabla);

            if (balance.incluirFirmas() && balance.firmas() != null) {
                agregarFirmas(documento, balance.firmas());
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el PDF del balance detallado mensual.",
                    e
            );
        }
    }

    /** Identifica la cooperativa, agencia, período y modalidad. */
    private void agregarEncabezado(
            Document documento,
            BalanceDetalladoMensualRespuestaDTO b
    ) throws DocumentException {
        Paragraph titulo = new Paragraph(
                "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE",
                TITULO
        );
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph informe = new Paragraph(
                "BALANCE DETALLADO MENSUAL"
                        + (b.compararAnoAnterior()
                        ? " - COMPARATIVO INTERANUAL"
                        : ""),
                SUBTITULO
        );
        informe.setAlignment(Element.ALIGN_CENTER);
        informe.setSpacingBefore(4);
        documento.add(informe);

        String mes = nombreMes(b.mes());
        String periodo = mes + " " + b.ano();

        if (b.compararAnoAnterior()) {
            periodo += " / " + mes + " " + b.anoComparativo();
        }

        Paragraph datos = new Paragraph(
                "Agencia: " + texto(b.codigoAgencia())
                        + " - " + texto(b.nombreAgencia())
                        + "     |     Período: " + periodo
                        + "     |     Nivel: " + b.nivel()
                        + "     |     "
                        + (b.soloBalance()
                        ? "Clases 1, 2 y 3"
                        : "Todas las clases"),
                NORMAL
        );
        datos.setAlignment(Element.ALIGN_CENTER);
        datos.setSpacingBefore(5);
        datos.setSpacingAfter(16);
        documento.add(datos);
    }

    /** Presenta los indicadores y movimientos sin alterar sus cálculos. */
    private void agregarResumen(
            Document documento,
            BalanceDetalladoMensualRespuestaDTO balance
    ) throws DocumentException {
        var indicadores = balance.indicadoresFinancieros();

        if (indicadores != null) {
            agregarTituloSeccion(documento, "INDICADORES FINANCIEROS");
            agregarDescripcion(
                    documento,
                    "Valores del mes y acumulados del período contable seleccionado."
            );

            PdfPTable tabla = tablaResumen(
                    new String[]{"Concepto", "Del mes", "Acumulado"}
            );

            filaIndicador(tabla, "Activo", indicadores.activo(), false);
            filaIndicador(tabla, "Pasivo", indicadores.pasivo(), false);
            filaIndicador(tabla, "Patrimonio", indicadores.patrimonio(), false);
            filaIndicador(tabla, "Ingresos", indicadores.ingresos(), false);
            filaIndicador(
                    tabla, "Costos y gastos",
                    indicadores.costosGastos(), false
            );
            filaIndicador(
                    tabla, "Ganancias o pérdidas",
                    indicadores.gananciasPerdidas(), true
            );

            documento.add(tabla);
        }

        var resumen = balance.resumenFinanciero();

        if (resumen != null) {
            agregarTituloSeccion(documento, "RESUMEN FINANCIERO");
            agregarDescripcion(
                    documento,
                    "Acumulados y movimientos de las cuentas contables operables."
            );

            PdfPTable tabla = tablaResumen(
                    new String[]{"Concepto", "Débitos", "Créditos"}
            );

            filaMovimiento(
                    tabla, "Acumulado anterior",
                    resumen.acumuladoAnterior(), false
            );
            filaMovimiento(
                    tabla, "Movimientos del mes",
                    resumen.movimientosMes(), false
            );
            filaMovimiento(
                    tabla, "Acumulado final",
                    resumen.acumuladoFinal(), true
            );

            filaDiferencia(
                    tabla, "Diferencia del mes",
                    resumen.diferenciaMes()
            );
            filaDiferencia(
                    tabla, "Diferencia acumulada",
                    resumen.diferenciaAcumulada()
            );

            documento.add(tabla);
        }
    }

    /** Agrega un título de sección compatible con OpenPDF. */
    private void agregarTituloSeccion(
            Document documento,
            String titulo
    ) throws DocumentException {
        Paragraph parrafo = new Paragraph(titulo, SUBTITULO);
        parrafo.setSpacingBefore(15);
        parrafo.setSpacingAfter(5);
        documento.add(parrafo);
    }

    /** Agrega la descripción de una sección financiera. */
    private void agregarDescripcion(
            Document documento,
            String descripcion
    ) throws DocumentException {
        Paragraph parrafo = new Paragraph(descripcion, DESCRIPCION);
        parrafo.setSpacingAfter(8);
        documento.add(parrafo);
    }

    /** Construye las tablas de indicadores y resumen financiero. */
    private PdfPTable tablaResumen(String[] titulos)
            throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{40, 30, 30});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);

        for (int n = 0; n < titulos.length; n++) {
            PdfPCell celda = new PdfPCell(
                    new Phrase(titulos[n], CABECERA)
            );
            celda.setBackgroundColor(AZUL_CABECERA);
            celda.setPadding(7);
            celda.setBorder(Rectangle.BOTTOM);
            celda.setBorderColor(BORDE);
            celda.setHorizontalAlignment(
                    n == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT
            );
            tabla.addCell(celda);
        }

        return tabla;
    }

    /** Agrega un indicador financiero con sus dos importes. */
    private void filaIndicador(
            PdfPTable tabla,
            String concepto,
            BalanceDetalladoMensualRespuestaDTO.IndicadorDTO indicador,
            boolean destacar
    ) {
        Color fondo = destacar ? AZUL_DESTACADO : Color.WHITE;
        Font fuente = destacar ? DESTACADO : NORMAL;

        celda(tabla, concepto, fuente, fondo, Element.ALIGN_LEFT);
        celda(
                tabla,
                indicador == null ? "—" : moneda(indicador.valorMes()),
                fuente, fondo, Element.ALIGN_RIGHT
        );
        celda(
                tabla,
                indicador == null ? "—" : moneda(indicador.valorAcumulado()),
                fuente, fondo, Element.ALIGN_RIGHT
        );
    }

    /** Agrega los débitos y créditos del resumen financiero. */
    private void filaMovimiento(
            PdfPTable tabla,
            String concepto,
            BalanceDetalladoMensualRespuestaDTO.MovimientoResumenDTO movimiento,
            boolean destacar
    ) {
        Color fondo = destacar ? AZUL_DESTACADO : Color.WHITE;
        Font fuente = destacar ? DESTACADO : NORMAL;

        celda(tabla, concepto, fuente, fondo, Element.ALIGN_LEFT);
        celda(
                tabla,
                movimiento == null ? "—" : moneda(movimiento.debitos()),
                fuente, fondo, Element.ALIGN_RIGHT
        );
        celda(
                tabla,
                movimiento == null ? "—" : moneda(movimiento.creditos()),
                fuente, fondo, Element.ALIGN_RIGHT
        );
    }

    /** Presenta la diferencia contable calculada por el servicio. */
    private void filaDiferencia(
            PdfPTable tabla,
            String concepto,
            BigDecimal diferencia
    ) {
        celda(
                tabla, concepto, NORMAL,
                Color.WHITE, Element.ALIGN_LEFT
        );
        celda(
                tabla, moneda(diferencia), NORMAL,
                Color.WHITE, Element.ALIGN_RIGHT
        );
        celda(
                tabla, "—", NORMAL,
                Color.WHITE, Element.ALIGN_RIGHT
        );
    }

    /**
     * Construye el cuerpo del balance.
     * Mensual: código, cuenta y saldo final.
     * Comparativo: código, cuenta, dos saldos y dos variaciones.
     */
    private PdfPTable crearTabla(
            BalanceDetalladoMensualRespuestaDTO balance
    ) throws DocumentException {
        boolean comparativo = balance.compararAnoAnterior();

        float[] anchos = comparativo
                ? new float[]{12, 36, 15, 15, 13, 9}
                : new float[]{14, 61, 25};

        String mes = nombreMes(balance.mes());

        String[] columnas = comparativo
                ? new String[]{
                "Código",
                "Cuenta contable",
                "Saldo " + mes + " " + balance.anoComparativo(),
                "Saldo " + mes + " " + balance.ano(),
                "Variación $",
                "Variación %"
        }
                : new String[]{
                "Código",
                "Cuenta contable",
                "Saldo " + mes + " " + balance.ano()
        };

        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        tabla.setSplitRows(true);

        for (int n = 0; n < columnas.length; n++) {
            PdfPCell celda = new PdfPCell(
                    new Phrase(columnas[n], CABECERA)
            );
            celda.setBackgroundColor(AZUL_CABECERA);
            celda.setPadding(7);
            celda.setBorder(Rectangle.BOTTOM);
            celda.setBorderColor(BORDE);
            celda.setHorizontalAlignment(
                    n < 2 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT
            );
            celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
            tabla.addCell(celda);
        }

        return tabla;
    }

    /**
     * Presenta cada cuenta según su nivel.
     * Solo las cuentas de clase llevan fondo; las agrupadoras
     * se distinguen por negrita y las operables permanecen blancas.
     */
    private void agregarFilas(
            PdfPTable tabla,
            List<BalanceDetalladoMensualFilaDTO> filas,
            boolean comparativo
    ) {
        if (filas == null) {
            return;
        }

        for (BalanceDetalladoMensualFilaDTO fila : filas) {
            int nivel = fila.nivelCuenta() == null
                    ? 1
                    : fila.nivelCuenta();

            boolean clase = nivel == 1;
            boolean agrupadora = !fila.cuentaOperable();

            Font fuente = clase || agrupadora ? DESTACADO : NORMAL;
            Color fondo = clase ? AZUL_DESTACADO : Color.WHITE;

            celda(
                    tabla,
                    texto(fila.codigoCuenta()),
                    fuente,
                    fondo,
                    Element.ALIGN_LEFT
            );

            String sangria = "  ".repeat(
                    Math.max(0, Math.min(5, nivel - 1))
            );

            celda(
                    tabla,
                    sangria + texto(fila.nombreCuenta()),
                    fuente,
                    fondo,
                    Element.ALIGN_LEFT
            );

            if (comparativo) {
                celda(
                        tabla,
                        moneda(fila.saldoMismoMesAnoAnterior()),
                        fuente,
                        fondo,
                        Element.ALIGN_RIGHT
                );
                celda(
                        tabla,
                        moneda(fila.saldoFinal()),
                        fuente,
                        fondo,
                        Element.ALIGN_RIGHT
                );
                celda(
                        tabla,
                        moneda(fila.variacionValor()),
                        fuente,
                        fondo,
                        Element.ALIGN_RIGHT
                );
                celda(
                        tabla,
                        porcentaje(fila.variacionPorcentaje()),
                        fuente,
                        fondo,
                        Element.ALIGN_RIGHT
                );
            } else {
                celda(
                        tabla,
                        moneda(fila.saldoFinal()),
                        fuente,
                        fondo,
                        Element.ALIGN_RIGHT
                );
            }
        }
    }

    /** Agrega una celda con bordes discretos y alineación uniforme. */
    private void celda(
            PdfPTable tabla,
            String valor,
            Font fuente,
            Color fondo,
            int alineacion
    ) {
        PdfPCell celda = new PdfPCell(new Phrase(valor, fuente));
        celda.setPadding(5);
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(BORDE);
        tabla.addCell(celda);
    }

    /** Conserva los firmantes configurados para el informe. */
    private void agregarFirmas(
            Document documento,
            BalanceDetalladoMensualRespuestaDTO.FirmasDTO firmas
    ) throws DocumentException {
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(35);

        var firmantes = Arrays.asList(
                firmas.firmante1(),
                firmas.firmante2(),
                firmas.firmante3()
        );

        for (var firmante : firmantes) {
            PdfPCell celda = new PdfPCell();
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setPadding(6);

            if (firmante != null
                    && firmante.nombre() != null
                    && !firmante.nombre().isBlank()) {
                celda.addElement(
                        new Paragraph(
                                "________________________________",
                                NORMAL
                        )
                );
                celda.addElement(
                        new Paragraph(firmante.nombre(), DESTACADO)
                );
                celda.addElement(
                        new Paragraph(texto(firmante.titulo()), NORMAL)
                );

                if (firmante.tarjetaProfesional() != null
                        && !firmante.tarjetaProfesional().isBlank()) {
                    celda.addElement(
                            new Paragraph(
                                    "T. P. "
                                            + firmante.tarjetaProfesional().trim(),
                                    NORMAL
                            )
                    );
                }
            }

            tabla.addCell(celda);
        }

        documento.add(tabla);
    }

    /** Obtiene el nombre del mes para los encabezados del balance. */
    private String nombreMes(int mes) {
        return Month.of(mes)
                .getDisplayName(TextStyle.FULL, COLOMBIA)
                .toUpperCase(COLOMBIA);
    }

    /** Normaliza los textos opcionales. */
    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /** Formatea importes con separadores colombianos. */
    private String moneda(BigDecimal valor) {
        if (valor == null) {
            return "—";
        }

        DecimalFormatSymbols simbolos =
                new DecimalFormatSymbols(COLOMBIA);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        DecimalFormat formato = new DecimalFormat(
                "#,##0.00;(#,##0.00)",
                simbolos
        );

        return formato.format(valor);
    }

    /** Formatea las variaciones porcentuales. */
    private String porcentaje(BigDecimal valor) {
        return valor == null ? "N/A" : moneda(valor) + " %";
    }

    /** Agrega fecha de generación y numeración de páginas. */
    private static class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_LEFT,
                    new Phrase(
                            "Generado: "
                                    + LocalDateTime.now().format(FECHA),
                            NORMAL
                    ),
                    documento.left(),
                    20,
                    0
            );

            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            "Página " + writer.getPageNumber(),
                            NORMAL
                    ),
                    documento.right(),
                    20,
                    0
            );
        }
    }
}
