
package co.assip.erp.contabilidad.informes.pyg.sin_formato;

import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualRespuestaDTO.FirmanteDTO;
import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualRespuestaDTO.FirmasDTO;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Genera el informe formal de Excedentes o Pérdidas sin formato.
 * Utiliza los importes ya calculados por el servicio contable.
 */
@Service
public class PyGSinFormatoPdfService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final Color AZUL = new Color(23, 59, 92);
    private static final Color FONDO = new Color(226, 237, 247);
    private static final Color BORDE = new Color(211, 223, 234);
    private static final Color TEXTO = new Color(39, 49, 60);

    private static final Font TITULO =
            new Font(Font.HELVETICA, 13, Font.BOLD, AZUL);
    private static final Font SUBTITULO =
            new Font(Font.HELVETICA, 10, Font.BOLD, AZUL);
    private static final Font NORMAL =
            new Font(Font.HELVETICA, 8, Font.NORMAL, TEXTO);
    private static final Font NEGRITA =
            new Font(Font.HELVETICA, 8, Font.BOLD, AZUL);
    private static final Font PEQUENA =
            new Font(Font.HELVETICA, 7, Font.NORMAL, TEXTO);

    /**
     * Genera el PDF para la agencia y el período consultados.
     * Las firmas son opcionales y deben provenir de la configuración autorizada.
     */
    public byte[] generar(
            PyGSinFormatoService.ResultadoPyG resultado,
            String codigoAgencia,
            String nombreAgencia,
            FirmasDTO firmas
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException("El resultado PyG es obligatorio.");
        }

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(
                    PageSize.A4, 35, 35, 65, 48
            );

            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            escritor.setPageEvent(new PiePagina());
            documento.open();

            agregarEncabezado(
                    documento, resultado, codigoAgencia, nombreAgencia
            );
            agregarDetalle(documento, resultado);
            agregarResumen(documento, resultado);

            if (firmas != null) {
                agregarFirmas(documento, firmas);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el PDF de Excedentes o Pérdidas.",
                    e
            );
        }
    }

    /** Identifica la cooperativa, agencia y período del informe. */
    private void agregarEncabezado(
            Document documento,
            PyGSinFormatoService.ResultadoPyG resultado,
            String codigoAgencia,
            String nombreAgencia
    ) throws DocumentException {
        Paragraph cooperativa = new Paragraph(
                "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE",
                TITULO
        );
        cooperativa.setAlignment(Element.ALIGN_CENTER);
        documento.add(cooperativa);

        Paragraph titulo = new Paragraph(
                "ESTADO DE EXCEDENTES O PERDIDAS - SIN FORMATO",
                SUBTITULO
        );
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(6);
        documento.add(titulo);

        String mes = Month.of(resultado.mes())
                .getDisplayName(TextStyle.FULL, COLOMBIA)
                .toUpperCase(COLOMBIA);

        Paragraph periodo = new Paragraph(
                "Agencia: " + texto(codigoAgencia)
                        + " - " + texto(nombreAgencia)
                        + "\nPeríodo: " + mes + " DE " + resultado.ano(),
                NORMAL
        );
        periodo.setAlignment(Element.ALIGN_CENTER);
        periodo.setSpacingBefore(8);
        periodo.setSpacingAfter(18);
        documento.add(periodo);
    }

    /** Presenta las cuentas contables respetando sus niveles. */
    private void agregarDetalle(
            Document documento,
            PyGSinFormatoService.ResultadoPyG resultado
    ) throws DocumentException {
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{13, 39, 16, 16, 16});
        tabla.setHeaderRows(1);
        tabla.setSpacingAfter(14);

        cabecera(tabla, "Código", Element.ALIGN_LEFT);
        cabecera(tabla, "Cuenta contable", Element.ALIGN_LEFT);
        cabecera(tabla, "Débitos", Element.ALIGN_RIGHT);
        cabecera(tabla, "Créditos", Element.ALIGN_RIGHT);
        cabecera(tabla, "Saldo", Element.ALIGN_RIGHT);

        if (resultado.cuentas() != null) {
            for (PyGSinFormatoService.CuentaPyG cuenta : resultado.cuentas()) {
                boolean destacada = cuenta.nivelCuenta() <= 2;
                Font fuente = destacada ? NEGRITA : NORMAL;
                Color fondo = cuenta.nivelCuenta() == 1 ? FONDO : Color.WHITE;

                celda(tabla, cuenta.codigoCuenta(), fuente,
                        Element.ALIGN_LEFT, fondo);
                celda(tabla,
                        "  ".repeat(Math.max(0, cuenta.nivelCuenta() - 1))
                                + texto(cuenta.nombreCuenta()),
                        fuente, Element.ALIGN_LEFT, fondo);
                celda(tabla, numero(cuenta.debitos()), fuente,
                        Element.ALIGN_RIGHT, fondo);
                celda(tabla, numero(cuenta.creditos()), fuente,
                        Element.ALIGN_RIGHT, fondo);
                celda(tabla, numero(cuenta.saldo()), fuente,
                        Element.ALIGN_RIGHT, fondo);
            }
        }

        documento.add(tabla);
    }

    /** Presenta ingresos, costos y gastos y el resultado del período. */
    private void agregarResumen(
            Document documento,
            PyGSinFormatoService.ResultadoPyG resultado
    ) throws DocumentException {
        Paragraph titulo = new Paragraph("RESUMEN DEL PERÍODO", SUBTITULO);
        titulo.setSpacingBefore(8);
        titulo.setSpacingAfter(8);
        documento.add(titulo);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(70);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setWidths(new float[]{65, 35});
        tabla.setSpacingAfter(18);

        resumen(tabla, "TOTAL INGRESOS",
                resultado.totalIngresos(), false);
        resumen(tabla, "TOTAL COSTOS Y GASTOS",
                resultado.totalCostosGastos(), false);

        String concepto = resultado.excedentePerdida().signum() >= 0
                ? "EXCEDENTE DEL PERÍODO"
                : "PÉRDIDA DEL PERÍODO";

        resumen(tabla, concepto, resultado.excedentePerdida(), true);
        documento.add(tabla);
    }

    /** Agrega hasta tres firmantes, sin generar firmas ficticias. */
    private void agregarFirmas(
            Document documento,
            FirmasDTO firmas
    ) throws DocumentException {
        FirmanteDTO[] disponibles = {
                firmas.firmante1(),
                firmas.firmante2(),
                firmas.firmante3()
        };

        int cantidad = 0;
        for (FirmanteDTO firmante : disponibles) {
            if (firmante != null && firmante.nombre() != null
                    && !firmante.nombre().isBlank()) {
                cantidad++;
            }
        }
        if (cantidad == 0) {
            return;
        }

        PdfPTable tabla = new PdfPTable(cantidad);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(35);
        tabla.setKeepTogether(true);

        for (FirmanteDTO firmante : disponibles) {
            if (firmante == null || firmante.nombre() == null
                    || firmante.nombre().isBlank()) {
                continue;
            }

            PdfPCell celda = new PdfPCell();
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(8);

            Paragraph linea = new Paragraph(
                    "____________________________", NORMAL
            );
            linea.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(linea);

            Paragraph nombre = new Paragraph(firmante.nombre(), NEGRITA);
            nombre.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(nombre);

            Paragraph cargo = new Paragraph(texto(firmante.titulo()), PEQUENA);
            cargo.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(cargo);

            if (firmante.tarjetaProfesional() != null
                    && !firmante.tarjetaProfesional().isBlank()) {
                Paragraph tarjeta = new Paragraph(
                        "T.P. " + firmante.tarjetaProfesional(),
                        PEQUENA
                );
                tarjeta.setAlignment(Element.ALIGN_CENTER);
                celda.addElement(tarjeta);
            }

            tabla.addCell(celda);
        }

        documento.add(tabla);
    }

    /** Construye una celda de cabecera repetible en cada página. */
    private void cabecera(
            PdfPTable tabla,
            String contenido,
            int alineacion
    ) {
        PdfPCell celda = new PdfPCell(new Phrase(contenido, NEGRITA));
        celda.setBackgroundColor(FONDO);
        celda.setBorderColor(BORDE);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(7);
        tabla.addCell(celda);
    }

    /** Construye una celda contable con alineación y formato uniforme. */
    private void celda(
            PdfPTable tabla,
            String contenido,
            Font fuente,
            int alineacion,
            Color fondo
    ) {
        PdfPCell celda = new PdfPCell(new Phrase(texto(contenido), fuente));
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(BORDE);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5);
        tabla.addCell(celda);
    }

    /** Agrega un concepto financiero al resumen del informe. */
    private void resumen(
            PdfPTable tabla,
            String concepto,
            BigDecimal valor,
            boolean destacado
    ) {
        Font fuente = destacado ? NEGRITA : NORMAL;
        Color fondo = destacado ? FONDO : Color.WHITE;
        celda(tabla, concepto, fuente, Element.ALIGN_LEFT, fondo);
        celda(tabla, numero(valor), fuente, Element.ALIGN_RIGHT, fondo);
    }

    /** Aplica formato colombiano a los valores monetarios. */
    private String numero(BigDecimal valor) {
        DecimalFormatSymbols simbolos =
                DecimalFormatSymbols.getInstance(COLOMBIA);
        DecimalFormat formato = new DecimalFormat(
                "#,##0.00;(#,##0.00)", simbolos
        );
        return formato.format(valor == null ? BigDecimal.ZERO : valor);
    }

    /** Evita valores nulos en las descripciones. */
    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    /** Identifica fecha de generación y número de página. */
    private static class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            String fecha = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );

            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    escritor.getDirectContent(),
                    Element.ALIGN_LEFT,
                    new Phrase("Generado: " + fecha, PEQUENA),
                    documento.left(),
                    documento.bottom() - 20,
                    0
            );

            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    escritor.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase("Página " + escritor.getPageNumber(), PEQUENA),
                    documento.right(),
                    documento.bottom() - 20,
                    0
            );
        }
    }
}
