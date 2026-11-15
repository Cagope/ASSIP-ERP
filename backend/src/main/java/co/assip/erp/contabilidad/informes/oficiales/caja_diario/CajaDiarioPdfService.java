package co.assip.erp.contabilidad.informes.oficiales.caja_diario;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Construye un informe carta con secciones diario y resumen, para papel interno u oficial. */
@Service
public class CajaDiarioPdfService {
    private static final Font NORMAL = new Font(Font.HELVETICA, 8);
    private static final Font BOLD = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static String numero(BigDecimal n) {
        var s = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        s.setGroupingSeparator('.'); s.setDecimalSeparator(',');
        var f = new DecimalFormat("#,##0.00;(#,##0.00)", s);
        return f.format(n);
    }
    private static PdfPCell celda(String texto, boolean negrita, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, negrita ? BOLD : NORMAL));
        c.setPadding(5); c.setBorder(Rectangle.BOTTOM); c.setBorderColor(new Color(220, 226, 233));
        c.setHorizontalAlignment(alineacion); return c;
    }
    private static PdfPTable tabla() throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{1.2f, 4.2f, 2f, 2f});
        t.setWidthPercentage(100);
        for (String h : new String[]{"Código", "Cuenta", "Débito", "Crédito"})
            t.addCell(celda(h, true, Element.ALIGN_LEFT));
        t.setHeaderRows(1); return t;
    }
    private static void titulo(Document d, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, new Font(Font.HELVETICA, 11, Font.BOLD));
        p.setAlignment(Element.ALIGN_CENTER); p.setSpacingAfter(10); d.add(p);
    }
    private static void renglon(PdfPTable t, String codigo, String nombre, BigDecimal debito, BigDecimal credito, boolean total) {
        t.addCell(celda(codigo, total, Element.ALIGN_LEFT));
        t.addCell(celda(nombre, total, Element.ALIGN_LEFT));
        t.addCell(celda(numero(debito), total, Element.ALIGN_RIGHT));
        t.addCell(celda(numero(credito), total, Element.ALIGN_RIGHT));
    }
    /** Evita datos institucionales en papel foliado preimpreso; no imprime folios ficticios. */
    public byte[] generar(CajaDiarioService.Resultado r, String formato) {
        boolean oficial = "OFICIAL".equalsIgnoreCase(formato);
        if (!oficial && !"INTERNO".equalsIgnoreCase(formato))
            throw new IllegalArgumentException("Formato PDF inválido: INTERNO u OFICIAL.");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document d = new Document(PageSize.LETTER, 48, 42, oficial ? 95 : 52, 50);
            PdfWriter.getInstance(d, out); d.open();
            if (!oficial) {
                titulo(d, "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE");
                titulo(d, "CONTABILIDAD - LIBRO CAJA DIARIO");
            } else titulo(d, "LIBRO CAJA DIARIO");
            d.add(new Paragraph("Agencia: " + r.codigoAgencia() + " - " + r.nombreAgencia(), NORMAL));
            d.add(new Paragraph("Período: " + r.desde().format(FECHA) + " al " + r.hasta().format(FECHA) + "  |  Nivel: " + r.nivel(), NORMAL));
            d.add(Chunk.NEWLINE);
            PdfPTable t = tabla(); LocalDate fecha = null;
            for (var fila : r.diario()) {
                if (!fila.fecha().equals(fecha)) {
                    if (fecha != null) {
                        var diaAnterior = fecha;
                        var total = r.totalesDias().stream().filter(x -> x.fecha().equals(diaAnterior)).findFirst().orElseThrow();
                        renglon(t, "", "TOTAL DEL DÍA " + fecha.format(FECHA), total.debito(), total.credito(), true);
                    }
                    fecha = fila.fecha();
                    PdfPCell dia = celda("FECHA: " + fecha.format(FECHA), true, Element.ALIGN_LEFT);
                    dia.setColspan(4); t.addCell(dia);
                }
                renglon(t, fila.codigo(), fila.nombre(), fila.debito(), fila.credito(), false);
            }
            if (fecha != null) {
                var ultima = fecha;
                var total = r.totalesDias().stream().filter(x -> x.fecha().equals(ultima)).findFirst().orElseThrow();
                renglon(t, "", "TOTAL DEL DÍA " + ultima.format(FECHA), total.debito(), total.credito(), true);
            }
            renglon(t, "", "TOTAL DEL INFORME", r.totalDebito(), r.totalCredito(), true);
            d.add(t);
            d.newPage(); titulo(d, "RESUMEN LIBRO CAJA DIARIO");
            d.add(new Paragraph("Agencia: " + r.codigoAgencia() + " - " + r.nombreAgencia(), NORMAL));
            d.add(new Paragraph("Período: " + r.desde().format(FECHA) + " al " + r.hasta().format(FECHA), NORMAL));
            d.add(Chunk.NEWLINE);
            PdfPTable resumen = tabla();
            for (var fila : r.resumen()) renglon(resumen, fila.codigo(), fila.nombre(), fila.debito(), fila.credito(), false);
            renglon(resumen, "", "TOTAL DEL INFORME", r.totalDebito(), r.totalCredito(), true);
            d.add(resumen);
            d.close(); return out.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("No fue posible generar el libro Caja Diario.", e); }
    }
}
