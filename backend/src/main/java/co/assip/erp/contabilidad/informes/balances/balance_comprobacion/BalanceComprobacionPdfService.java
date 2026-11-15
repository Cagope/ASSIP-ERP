package co.assip.erp.contabilidad.informes.balances.balance_comprobacion;

import co.assip.erp.contabilidad.informes.balances.balance_comprobacion.dto.BalanceComprobacionFilaDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comprobacion.dto.BalanceComprobacionRespuestaDTO;
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
import java.util.List;
import java.util.Locale;

/** Genera el informe institucional en PDF a partir del balance ya autorizado y calculado. */
@Service
public class BalanceComprobacionPdfService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final Color VERDE = new Color(22, 94, 76);
    private static final Color GRIS = new Color(240, 243, 246);
    private static final Color TEXTO = new Color(35, 48, 59);
    private static final Font TITULO = new Font(Font.HELVETICA, 13, Font.BOLD, VERDE);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 9, Font.BOLD, TEXTO);
    private static final Font CABECERA = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);
    private static final Font NORMAL = new Font(Font.HELVETICA, 7, Font.NORMAL, TEXTO);
    private static final Font DESTACADO = new Font(Font.HELVETICA, 7, Font.BOLD, TEXTO);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Produce bytes PDF; el controlador debe invocar antes al servicio de consulta para validar permisos. */
    public byte[] generar(BalanceComprobacionRespuestaDTO balance) {
        if (balance == null) throw new IllegalArgumentException("El balance es obligatorio.");
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4.rotate(), 29, 29, 52, 38);
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();
            agregarEncabezado(documento, balance);
            PdfPTable tabla = crearTabla(balance.compararAnoAnterior());
            agregarFilas(tabla, balance.filas(), balance.compararAnoAnterior());
            documento.add(tabla);
            if (balance.incluirFirmas() && balance.firmas() != null) {
                agregarFirmas(documento, balance.firmas());
            }
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("No fue posible generar el PDF del balance de comprobación.", e);
        }
    }

    /** Presenta la identificación institucional, el alcance y el período del informe. */
    private void agregarEncabezado(Document documento, BalanceComprobacionRespuestaDTO b)
            throws DocumentException {
        Paragraph titulo = new Paragraph("COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);
        Paragraph informe = new Paragraph("BALANCE COMPARADO MENSUAL" +
                (b.compararAnoAnterior() ? " - COMPARATIVO INTERANUAL" : ""), SUBTITULO);
        informe.setAlignment(Element.ALIGN_CENTER);
        informe.setSpacingBefore(4);
        documento.add(informe);
        String mes = Month.of(b.mes()).getDisplayName(TextStyle.FULL, COLOMBIA);
        String periodo = mes.toUpperCase(COLOMBIA) + " " + b.ano();
        if (b.compararAnoAnterior()) periodo += " / " + mes.toUpperCase(COLOMBIA) + " " + b.anoComparativo();
        Paragraph datos = new Paragraph("Agencia: " + texto(b.codigoAgencia()) + " - " +
                texto(b.nombreAgencia()) + "     |     Período: " + periodo +
                "     |     Nivel: " + b.nivel() +
                "     |     " + (b.soloBalance() ? "Clases 1, 2 y 3" : "Todas las clases"), NORMAL);
        datos.setAlignment(Element.ALIGN_CENTER);
        datos.setSpacingBefore(5);
        datos.setSpacingAfter(12);
        documento.add(datos);
    }

    /** Define columnas diferenciadas para el balance tradicional y el comparativo. */
    private PdfPTable crearTabla(boolean comparativo) throws DocumentException {
        float[] anchos = comparativo
                ? new float[]{12, 38, 14, 14, 14, 12}
                : new float[]{12, 38, 15, 12, 12, 15};
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        String[] columnas = comparativo
                ? new String[]{"Código", "Cuenta", "Saldo año anterior", "Saldo actual", "Variación $", "Variación %"}
                : new String[]{"Código", "Cuenta", "Saldo anterior", "Débitos mes", "Créditos mes", "Saldo final"};
        for (String columna : columnas) {
            PdfPCell celda = new PdfPCell(new Phrase(columna, CABECERA));
            celda.setBackgroundColor(VERDE);
            celda.setPadding(6);
            celda.setHorizontalAlignment(columna.equals("Cuenta") || columna.equals("Código")
                    ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            celda.setBorderColor(Color.WHITE);
            tabla.addCell(celda);
        }
        return tabla;
    }

    /** Imprime los saldos ya consolidados; nunca suma cuentas hijas sobre sus agrupadoras. */
    private void agregarFilas(PdfPTable tabla, List<BalanceComprobacionFilaDTO> filas, boolean comparativo) {
        if (filas == null) return;
        int indice = 0;
        for (BalanceComprobacionFilaDTO f : filas) {
            boolean raiz = f.nivelCuenta() != null && f.nivelCuenta() == 1;
            Color fondo = raiz ? new Color(219, 235, 229) : indice % 2 == 0 ? Color.WHITE : GRIS;
            Font fuente = raiz ? DESTACADO : NORMAL;
            celda(tabla, texto(f.codigoCuenta()), fuente, fondo, Element.ALIGN_LEFT);
            String sangria = "  ".repeat(Math.max(0, Math.min(5, f.nivelCuenta() == null ? 0 : f.nivelCuenta() - 1)));
            celda(tabla, sangria + texto(f.nombreCuenta()), fuente, fondo, Element.ALIGN_LEFT);
            if (comparativo) {
                celda(tabla, moneda(f.saldoMismoMesAnoAnterior()), fuente, fondo, Element.ALIGN_RIGHT);
                celda(tabla, moneda(f.saldoFinal()), fuente, fondo, Element.ALIGN_RIGHT);
                celda(tabla, moneda(f.variacionValor()), fuente, fondo, Element.ALIGN_RIGHT);
                celda(tabla, porcentaje(f.variacionPorcentaje()), fuente, fondo, Element.ALIGN_RIGHT);
            } else {
                celda(tabla, moneda(f.saldoAnterior()), fuente, fondo, Element.ALIGN_RIGHT);
                celda(tabla, moneda(f.valorDebitoMes()), fuente, fondo, Element.ALIGN_RIGHT);
                celda(tabla, moneda(f.valorCreditoMes()), fuente, fondo, Element.ALIGN_RIGHT);
                celda(tabla, moneda(f.saldoFinal()), fuente, fondo, Element.ALIGN_RIGHT);
            }
            indice++;
        }
    }

    private void celda(PdfPTable tabla, String valor, Font fuente, Color fondo, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(valor, fuente));
        celda.setPadding(4);
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(alineacion);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(new Color(222, 229, 232));
        tabla.addCell(celda);
    }

    /** Incluye únicamente las firmas solicitadas y configuradas para la agencia del informe. */
    private void agregarFirmas(Document documento, BalanceComprobacionRespuestaDTO.FirmasDTO firmas)
            throws DocumentException {
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(35);
        var firmantes = java.util.Arrays.asList(firmas.firmante1(), firmas.firmante2(), firmas.firmante3());
        for (var f : firmantes) {
            PdfPCell celda = new PdfPCell();
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setPadding(6);
            if (f != null && f.nombre() != null && !f.nombre().isBlank()) {
                celda.addElement(new Paragraph("________________________________", NORMAL));
                celda.addElement(new Paragraph(f.nombre(), DESTACADO));
                celda.addElement(new Paragraph(texto(f.titulo()), NORMAL));
                if (f.tarjetaProfesional() != null && !f.tarjetaProfesional().isBlank()) {
                    celda.addElement(new Paragraph("T. P. " + f.tarjetaProfesional().trim(), NORMAL));
                }
            }
            tabla.addCell(celda);
        }
        documento.add(tabla);
    }

    private String texto(String valor) { return valor == null ? "" : valor.trim(); }

    private String moneda(BigDecimal valor) {
        if (valor == null) return "—";
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(COLOMBIA);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat("#,##0.00;(#,##0.00)", simbolos);
        return formato.format(valor);
    }

    private String porcentaje(BigDecimal valor) {
        return valor == null ? "N/A" : moneda(valor) + " %";
    }

    /** Numera cada página y señala la fecha de generación del documento. */
    private static class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            com.lowagie.text.pdf.ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                    new Phrase("Generado: " + LocalDateTime.now().format(FECHA), NORMAL),
                    documento.left(), 20, 0);
            com.lowagie.text.pdf.ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), NORMAL),
                    documento.right(), 20, 0);
        }
    }
}
