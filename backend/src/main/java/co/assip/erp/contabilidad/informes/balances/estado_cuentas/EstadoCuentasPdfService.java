package co.assip.erp.contabilidad.informes.balances.estado_cuentas;

import co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto.EstadoCuentasRespuestaDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Genera un informe PDF paginado, independiente de la pantalla Angular. */
@Service
public class EstadoCuentasPdfService {
    private static final Font TITULO = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 8);
    private static final Font NEGRITA = new Font(Font.HELVETICA, 8, Font.BOLD);

    public byte[] generar(EstadoCuentasRespuestaDTO informe) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4.rotate(), 30, 30, 50, 40);
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();
            Paragraph titulo = new Paragraph("ESTADO DE CUENTAS", TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            Paragraph encabezado = new Paragraph(informe.codigoAgencia() + " - "
                    + informe.nombreAgencia() + " | Corte: " + informe.fechaCorte()
                    + " | Desde: " + informe.fechaCorte().withDayOfMonth(1)
                    + " | Cuenta: " + (informe.codigoCuentaPrincipal().isBlank()
                    ? "Todas" : informe.codigoCuentaPrincipal()), NORMAL);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            encabezado.setSpacingAfter(12);
            documento.add(encabezado);
            PdfPTable tabla = new PdfPTable(new float[]{12, 35, 14, 13, 13, 13});
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);
            String[] columnas = {"Código", "Cuenta", "Saldo anterior",
                    "Débitos", "Créditos", "Saldo al corte"};
            for (String columna : columnas) celda(tabla, columna, true, false);
            for (var fila : informe.filas()) {
                celda(tabla, fila.codigoCuenta(), false, false);
                celda(tabla, "  ".repeat(Math.max(0, fila.nivelCuenta() - 1))
                        + fila.nombreCuenta(), !fila.cuentaOperable(), false);
                celda(tabla, dinero(fila.saldoAnterior()), false, true);
                celda(tabla, dinero(fila.valorDebitoMes()), false, true);
                celda(tabla, dinero(fila.valorCreditoMes()), false, true);
                celda(tabla, dinero(fila.saldoFinal()), false, true);
            }
            celda(tabla, "", true, false);
            celda(tabla, "TOTALES", true, false);
            celda(tabla, dinero(informe.totales().saldoAnterior()), true, true);
            celda(tabla, dinero(informe.totales().valorDebitoMes()), true, true);
            celda(tabla, dinero(informe.totales().valorCreditoMes()), true, true);
            celda(tabla, dinero(informe.totales().saldoFinal()), true, true);
            documento.add(tabla);
            if (informe.incluirFirmas() && informe.firmas() != null) {
                PdfPTable firmas = new PdfPTable(3);
                firmas.setWidthPercentage(100);
                firmas.setSpacingBefore(32);
                for (var firma : new EstadoCuentasRespuestaDTO.FirmanteDTO[]{
                        informe.firmas().firmante1(), informe.firmas().firmante2(),
                        informe.firmas().firmante3()}) {
                    PdfPCell c = new PdfPCell(new Phrase(firma == null ? ""
                            : "____________________________\n" + firma.nombre()
                            + "\n" + firma.titulo() + (firma.tarjetaProfesional() == null
                            ? "" : "\nT.P. " + firma.tarjetaProfesional()), NORMAL));
                    c.setBorder(Rectangle.NO_BORDER);
                    firmas.addCell(c);
                }
                documento.add(firmas);
            }
            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("No fue posible generar el PDF.", e);
        }
    }

    private void celda(PdfPTable tabla, String texto, boolean negrita, boolean numero) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto,
                negrita ? NEGRITA : NORMAL));
        c.setPadding(4);
        c.setHorizontalAlignment(numero ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        c.setBorderColor(new Color(220, 225, 230));
        tabla.addCell(c);
    }

    private String dinero(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00;(#,##0.00)", simbolos).format(valor);
    }

    private static final class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            com.lowagie.text.pdf.ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), NORMAL),
                    documento.right(), 20, 0);
        }
    }
}
