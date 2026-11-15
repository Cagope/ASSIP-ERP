
package co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual;

import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualFilaDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualRespuestaDTO;
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
import java.util.List;
import java.util.Locale;

/** Genera el PDF institucional del balance entre dos períodos seleccionados. */
@Service
public class BalanceComparadoMensualPdfService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final Color VERDE = new Color(22, 94, 76);
    private static final Color GRIS = new Color(240, 243, 246);
    private static final Color TEXTO = new Color(35, 48, 59);
    private static final Font TITULO = new Font(Font.HELVETICA, 13, Font.BOLD, VERDE);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 9, Font.BOLD, TEXTO);
    private static final Font CABECERA = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);
    private static final Font NORMAL = new Font(Font.HELVETICA, 7, Font.NORMAL, TEXTO);
    private static final Font DESTACADO = new Font(Font.HELVETICA, 7, Font.BOLD, TEXTO);
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Exporta el resultado ya calculado y autorizado por el servicio de consulta. */
    public byte[] generar(BalanceComparadoMensualRespuestaDTO balance) {
        if (balance == null) {
            throw new IllegalArgumentException("El balance es obligatorio.");
        }
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4.rotate(), 29, 29, 52, 38);
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();
            agregarEncabezado(documento, balance);
            PdfPTable tabla = crearTabla(balance);
            agregarFilas(tabla, balance.filas());
            agregarTotales(tabla, balance.totales());
            documento.add(tabla);
            if (balance.incluirFirmas() && balance.firmas() != null) {
                agregarFirmas(documento, balance.firmas());
            }
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el PDF del balance comparado mensual.", e);
        }
    }

    /** Presenta agencia, nivel y los dos períodos independientes. */
    private void agregarEncabezado(
            Document documento, BalanceComparadoMensualRespuestaDTO b)
            throws DocumentException {
        Paragraph titulo = new Paragraph(
                "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph informe = new Paragraph("BALANCE COMPARADO MENSUAL", SUBTITULO);
        informe.setAlignment(Element.ALIGN_CENTER);
        informe.setSpacingBefore(4);
        documento.add(informe);

        String periodo1 = periodo(b.anoPeriodo1(), b.mesPeriodo1());
        String periodo2 = periodo(b.anoPeriodo2(), b.mesPeriodo2());

        Paragraph datos = new Paragraph(
                "Agencia: " + texto(b.codigoAgencia()) + " - " +
                        texto(b.nombreAgencia()) +
                        "     |     Período 1: " + periodo1 +
                        "     |     Período 2: " + periodo2 +
                        "     |     Nivel: " + b.nivel() +
                        "     |     " + (b.soloBalance()
                        ? "Clases 1, 2 y 3" : "Todas las clases"), NORMAL);
        datos.setAlignment(Element.ALIGN_CENTER);
        datos.setSpacingBefore(5);
        datos.setSpacingAfter(12);
        documento.add(datos);
    }

    /** Construye únicamente las columnas de saldos y variaciones. */
    private PdfPTable crearTabla(BalanceComparadoMensualRespuestaDTO b)
            throws DocumentException {
        PdfPTable tabla = new PdfPTable(
                new float[]{12, 36, 15, 15, 14, 12});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);

        String[] columnas = {
                "Código",
                "Cuenta",
                periodo(b.anoPeriodo1(), b.mesPeriodo1()),
                periodo(b.anoPeriodo2(), b.mesPeriodo2()),
                "Variación $",
                "Variación %"
        };

        for (int i = 0; i < columnas.length; i++) {
            PdfPCell celda = new PdfPCell(new Phrase(columnas[i], CABECERA));
            celda.setBackgroundColor(VERDE);
            celda.setPadding(6);
            celda.setHorizontalAlignment(i < 2
                    ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            celda.setBorderColor(Color.WHITE);
            tabla.addCell(celda);
        }
        return tabla;
    }

    /** Presenta cada cuenta con los saldos previamente calculados. */
    private void agregarFilas(
            PdfPTable tabla, List<BalanceComparadoMensualFilaDTO> filas) {
        if (filas == null) return;
        int indice = 0;

        for (BalanceComparadoMensualFilaDTO f : filas) {
            boolean raiz = f.nivelCuenta() != null && f.nivelCuenta() == 1;
            Color fondo = raiz
                    ? new Color(219, 235, 229)
                    : indice % 2 == 0 ? Color.WHITE : GRIS;
            Font fuente = raiz ? DESTACADO : NORMAL;

            celda(tabla, texto(f.codigoCuenta()), fuente, fondo, Element.ALIGN_LEFT);

            int nivel = f.nivelCuenta() == null ? 1 : f.nivelCuenta();
            String sangria = "  ".repeat(Math.max(0, Math.min(5, nivel - 1)));
            celda(tabla, sangria + texto(f.nombreCuenta()),
                    fuente, fondo, Element.ALIGN_LEFT);

            celda(tabla, moneda(f.saldoPeriodo1()),
                    fuente, fondo, Element.ALIGN_RIGHT);
            celda(tabla, moneda(f.saldoPeriodo2()),
                    fuente, fondo, Element.ALIGN_RIGHT);
            celda(tabla, moneda(f.variacionValor()),
                    fuente, fondo, Element.ALIGN_RIGHT);
            celda(tabla, porcentaje(f.variacionPorcentaje()),
                    fuente, fondo, Element.ALIGN_RIGHT);
            indice++;
        }
    }

    /** Agrega los totales recibidos sin volver a sumar niveles contables. */
    private void agregarTotales(
            PdfPTable tabla, BalanceComparadoMensualRespuestaDTO.TotalesDTO totales) {
        if (totales == null) return;

        Color fondo = new Color(219, 235, 229);
        celda(tabla, "", DESTACADO, fondo, Element.ALIGN_LEFT);
        celda(tabla, "TOTALES", DESTACADO, fondo, Element.ALIGN_LEFT);
        celda(tabla, moneda(totales.saldoPeriodo1()),
                DESTACADO, fondo, Element.ALIGN_RIGHT);
        celda(tabla, moneda(totales.saldoPeriodo2()),
                DESTACADO, fondo, Element.ALIGN_RIGHT);
        celda(tabla, moneda(totales.variacionValor()),
                DESTACADO, fondo, Element.ALIGN_RIGHT);
        celda(tabla, porcentaje(totales.variacionPorcentaje()),
                DESTACADO, fondo, Element.ALIGN_RIGHT);
    }

    /** Aplica formato de presentación y alineación a cada celda. */
    private void celda(
            PdfPTable tabla, String valor, Font fuente,
            Color fondo, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(valor, fuente));
        celda.setPadding(4);
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(alineacion);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(new Color(222, 229, 232));
        tabla.addCell(celda);
    }

    /** Incluye las firmas configuradas cuando fueron solicitadas. */
    private void agregarFirmas(
            Document documento, BalanceComparadoMensualRespuestaDTO.FirmasDTO firmas)
            throws DocumentException {
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(35);

        var firmantes = java.util.Arrays.asList(
                firmas.firmante1(), firmas.firmante2(), firmas.firmante3());

        for (var f : firmantes) {
            PdfPCell celda = new PdfPCell();
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setPadding(6);

            if (f != null && f.nombre() != null && !f.nombre().isBlank()) {
                celda.addElement(new Paragraph(
                        "________________________________", NORMAL));
                celda.addElement(new Paragraph(f.nombre(), DESTACADO));
                celda.addElement(new Paragraph(texto(f.titulo()), NORMAL));

                if (f.tarjetaProfesional() != null
                        && !f.tarjetaProfesional().isBlank()) {
                    celda.addElement(new Paragraph(
                            "T. P. " + f.tarjetaProfesional().trim(), NORMAL));
                }
            }
            tabla.addCell(celda);
        }
        documento.add(tabla);
    }

    /** Formatea el nombre del mes y año seleccionados. */
    private String periodo(int ano, int mes) {
        return Month.of(mes)
                .getDisplayName(TextStyle.FULL, COLOMBIA)
                .toUpperCase(COLOMBIA) + " " + ano;
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    /** Formato monetario colombiano, con negativos entre paréntesis. */
    private String moneda(BigDecimal valor) {
        if (valor == null) return "—";
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(COLOMBIA);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat(
                "#,##0.00;(#,##0.00)", simbolos);
        return formato.format(valor);
    }

    private String porcentaje(BigDecimal valor) {
        return valor == null ? "N/A" : moneda(valor) + " %";
    }

    /** Numera las páginas y registra la fecha de generación. */
    private static class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_LEFT,
                    new Phrase("Generado: " +
                            LocalDateTime.now().format(FECHA), NORMAL),
                    documento.left(), 20, 0);

            com.lowagie.text.pdf.ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), NORMAL),
                    documento.right(), 20, 0);
        }
    }
}
