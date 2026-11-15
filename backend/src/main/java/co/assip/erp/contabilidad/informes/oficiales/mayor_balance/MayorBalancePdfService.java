package co.assip.erp.contabilidad.informes.oficiales.mayor_balance;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Carta horizontal; el formato oficial reserva espacio para el encabezado preimpreso. */
@Service
public class MayorBalancePdfService {
    private static final Font NORMAL = new Font(Font.HELVETICA, 7);
    private static final Font NEGRITA = new Font(Font.HELVETICA, 7, Font.BOLD);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static String numero(BigDecimal n) {
        var s = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        s.setGroupingSeparator('.'); s.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00;(#,##0.00)", s).format(n);
    }
    private static PdfPCell celda(String texto, boolean bold, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, bold ? NEGRITA : NORMAL));
        c.setHorizontalAlignment(alineacion); c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(4); c.setBorder(Rectangle.BOTTOM); c.setBorderColor(new Color(216,224,233));
        return c;
    }
    private static void renglon(PdfPTable t, String codigo, String nombre, BigDecimal[] valores, boolean bold) {
        t.addCell(celda(codigo,bold,Element.ALIGN_LEFT));
        t.addCell(celda(nombre,bold,Element.ALIGN_LEFT));
        for (var v : valores) t.addCell(celda(numero(v),bold,Element.ALIGN_RIGHT));
    }
    public byte[] generar(MayorBalanceService.Resultado r, String formato) {
        boolean oficial = "OFICIAL".equalsIgnoreCase(formato);
        if (!oficial && !"INTERNO".equalsIgnoreCase(formato))
            throw new IllegalArgumentException("Formato PDF inválido: INTERNO u OFICIAL.");
        try (var out = new ByteArrayOutputStream()) {
            Document d = new Document(PageSize.LETTER.rotate(), 32, 32, oficial ? 98 : 40, 38);
            PdfWriter.getInstance(d, out); d.open();
            if (!oficial) {
                Paragraph empresa = new Paragraph("COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE",
                    new Font(Font.HELVETICA, 11, Font.BOLD));
                empresa.setAlignment(Element.ALIGN_CENTER); d.add(empresa);
            }
            Paragraph titulo = new Paragraph("LIBRO MAYOR Y BALANCE" + (r.acumuladoAnual() ? " - ANUAL" : " - MENSUAL"),
                new Font(Font.HELVETICA, 10, Font.BOLD));
            titulo.setAlignment(Element.ALIGN_CENTER); titulo.setSpacingAfter(7); d.add(titulo);
            d.add(new Paragraph("Agencia: " + r.codigoAgencia() + " - " + r.nombreAgencia() +
                "     Período: " + r.desde().format(FECHA) + " al " + r.hasta().format(FECHA) +
                "     Nivel contable: 3", NORMAL));
            d.add(Chunk.NEWLINE);
            PdfPTable t = new PdfPTable(new float[]{1.0f,3.5f,1.1f,1.1f,1.1f,1.1f,1.1f,1.1f});
            t.setWidthPercentage(100);
            PdfPCell codigo = celda("Código",true,Element.ALIGN_LEFT); codigo.setRowspan(2); t.addCell(codigo);
            PdfPCell cuenta = celda("Cuenta",true,Element.ALIGN_LEFT); cuenta.setRowspan(2); t.addCell(cuenta);
            for (String h : new String[]{"SALDO ANTERIOR","MOVIMIENTO DEL PERÍODO","NUEVO SALDO"}) {
                PdfPCell grupo = celda(h,true,Element.ALIGN_CENTER); grupo.setColspan(2); t.addCell(grupo);
            }
            for (int i=0; i<3; i++) { t.addCell(celda("Débito",true,Element.ALIGN_RIGHT)); t.addCell(celda("Crédito",true,Element.ALIGN_RIGHT)); }
            t.setHeaderRows(2);
            for (var x : r.filas()) renglon(t,x.codigo(),x.nombre(),new BigDecimal[]{x.debitoAnterior(),x.creditoAnterior(),
                x.debitoMovimiento(),x.creditoMovimiento(),x.debitoSaldo(),x.creditoSaldo()},false);
            var x = r.totales();
            renglon(t,"","TOTAL",new BigDecimal[]{x.debitoAnterior(),x.creditoAnterior(),x.debitoMovimiento(),
                x.creditoMovimiento(),x.debitoSaldo(),x.creditoSaldo()},true);
            d.add(t); d.close(); return out.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("No se pudo generar el PDF de Mayor y Balance.",e); }
    }
}
