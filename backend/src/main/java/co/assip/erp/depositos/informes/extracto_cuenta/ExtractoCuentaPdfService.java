package co.assip.erp.depositos.informes.extracto_cuenta;

import co.assip.erp.depositos.informes.extracto_cuenta.dto.ExtractoCuentaMovimientoDTO;
import co.assip.erp.depositos.informes.extracto_cuenta.dto.ExtractoCuentaResponseDTO;
import co.assip.erp.depositos.informes.extracto_cuenta.dto.ExtractoCuentaResumenDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ExtractoCuentaPdfService {

    private static final String OWNER_PASSWORD = "ASSIP-ERP";
    private static final String LOGO_PATH = "/static/logo/LOGO_EMPRESA.PNG";

    private static final DecimalFormat MONEY =
            new DecimalFormat("#,##0.00");

    private static final DateTimeFormatter DFH =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ResponseEntity<byte[]> generarPdfResponse(
            ExtractoCuentaResponseDTO dto
    ) {

        byte[] pdf =
                generarPdf(dto);

        String codigoCuenta =
                dto.getResumen().getCodigoCuenta() == null
                        ? "cuenta"
                        : dto.getResumen().getCodigoCuenta().trim();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("extracto_cuenta_" + codigoCuenta + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    public byte[] generarPdf(
            ExtractoCuentaResponseDTO dto
    ) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            ExtractoCuentaResumenDTO r =
                    dto.getResumen();

            Document document =
                    new Document(PageSize.LETTER, 28, 28, 38, 42);

            PdfWriter writer =
                    PdfWriter.getInstance(document, baos);

            writer.setPageEvent(new FooterEvent());

            String clave =
                    r.getDocumento() == null || r.getDocumento().isBlank()
                            ? "0000"
                            : r.getDocumento().trim();

            writer.setEncryption(
                    clave.getBytes(),
                    OWNER_PASSWORD.getBytes(),
                    PdfWriter.ALLOW_PRINTING,
                    PdfWriter.ENCRYPTION_AES_128
            );

            document.open();

            Font title =
                    new Font(Font.HELVETICA, 13, Font.BOLD);

            Font subtitle =
                    new Font(Font.HELVETICA, 10, Font.BOLD);

            Font label =
                    new Font(Font.HELVETICA, 8, Font.BOLD);

            Font value =
                    new Font(Font.HELVETICA, 8, Font.NORMAL);

            Font table =
                    new Font(Font.HELVETICA, 7, Font.NORMAL);

            Font tableBold =
                    new Font(Font.HELVETICA, 7, Font.BOLD);

            agregarEncabezado(
                    document,
                    r,
                    title,
                    subtitle,
                    value
            );

            document.add(new Paragraph("Datos personales", subtitle));

            PdfPTable datosPersona =
                    new PdfPTable(6);

            datosPersona.setWidthPercentage(100);

            add(datosPersona, "Señor(a):", r.getNombreCompleto(), label, value);
            add(datosPersona, "Documento:", r.getDocumento(), label, value);
            add(datosPersona, "Dirección:", r.getDireccion(), label, value);

            document.add(datosPersona);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Datos de la cuenta", subtitle));

            PdfPTable datosCuenta =
                    new PdfPTable(8);

            datosCuenta.setWidthPercentage(100);

            add(datosCuenta, "Cuenta:", r.getCodigoCuenta(), label, value);
            add(datosCuenta, "Tipo depósito:", r.getCodigoForma() + " - " + r.getNombreForma(), label, value);
            add(datosCuenta, "Agencia:", r.getNombreAgencia(), label, value);
            add(datosCuenta, "Extracto:", "Generado", label, value);

            document.add(datosCuenta);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Valores resumen", subtitle));

            PdfPTable resumen =
                    new PdfPTable(8);

            resumen.setWidthPercentage(100);

            add(resumen, "Saldo inicial:", money(r.getSaldoInicial()), label, value);
            add(resumen, "Total créditos:", money(r.getTotalCreditos()), label, value);
            add(resumen, "Total débitos:", money(r.getTotalDebitos()), label, value);
            add(resumen, "Nuevo saldo:", money(r.getSaldoFinal()), label, value);

            document.add(resumen);
            document.add(Chunk.NEWLINE);

            document.add(tablaMovimientos(dto, table, tableBold));

            document.close();
            writer.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error generando PDF del extracto de cuenta.",
                    e
            );
        }
    }

    private void agregarEncabezado(
            Document document,
            ExtractoCuentaResumenDTO r,
            Font title,
            Font subtitle,
            Font value
    ) throws DocumentException {

        PdfPTable header =
                new PdfPTable(new float[]{1.4f, 5.6f});

        header.setWidthPercentage(100);

        PdfPCell logoCell =
                new PdfPCell();

        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(4);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Image logo =
                loadLogo();

        if (logo != null) {
            logo.scaleToFit(110, 70);
            logoCell.addElement(logo);
        }

        PdfPCell empresa =
                new PdfPCell();

        empresa.setBorder(Rectangle.NO_BORDER);
        empresa.setPadding(4);
        empresa.setVerticalAlignment(Element.ALIGN_MIDDLE);
        empresa.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph p1 =
                new Paragraph(nvl(r.getRazonSocial()), title);

        p1.setAlignment(Element.ALIGN_CENTER);

        Paragraph p2 =
                new Paragraph(nvl(r.getNombreAgencia()), subtitle);

        p2.setAlignment(Element.ALIGN_CENTER);

        Paragraph p3 =
                new Paragraph(
                        "NIT: " + nvl(r.getDocumentoEmpresa()) +
                                (r.getDigitoVerificacion() == null || r.getDigitoVerificacion().isBlank()
                                        ? ""
                                        : " - " + r.getDigitoVerificacion()),
                        value
                );

        p3.setAlignment(Element.ALIGN_CENTER);

        Paragraph p4 =
                new Paragraph("EXTRACTO DE MOVIMIENTOS", subtitle);

        p4.setAlignment(Element.ALIGN_CENTER);

        empresa.addElement(p1);
        empresa.addElement(p2);
        empresa.addElement(p3);
        empresa.addElement(p4);

        header.addCell(logoCell);
        header.addCell(empresa);

        document.add(header);
        document.add(new LineSeparator());
        document.add(Chunk.NEWLINE);
    }

    private PdfPTable tablaMovimientos(
            ExtractoCuentaResponseDTO dto,
            Font td,
            Font th
    ) {

        PdfPTable t =
                new PdfPTable(new float[]{
                        1.3f,
                        0.8f,
                        2.8f,
                        0.8f,
                        1.1f,
                        1.3f,
                        1.3f,
                        1.3f
                });

        t.setWidthPercentage(100);

        header(t, "Fecha", th);
        header(t, "Tipo", th);
        header(t, "Concepto", th);
        header(t, "T.C.", th);
        header(t, "Núm. Comp.", th);
        header(t, "Débito", th);
        header(t, "Crédito", th);
        header(t, "Saldo", th);

        if (dto.getMovimientos() == null || dto.getMovimientos().isEmpty()) {

            PdfPCell c =
                    new PdfPCell(new Phrase("Sin movimientos", td));

            c.setColspan(8);
            c.setPadding(5);

            t.addCell(c);

            return t;
        }

        for (ExtractoCuentaMovimientoDTO m : dto.getMovimientos()) {

            cell(t, nvl(m.getFechaMovimiento()), td, Element.ALIGN_LEFT);
            cell(t, nvl(m.getTipoMovimiento()), td, Element.ALIGN_LEFT);
            cell(t, nvl(m.getDescripcionMovimiento()), td, Element.ALIGN_LEFT);
            cell(t, nvl(m.getTipoComprobante()), td, Element.ALIGN_LEFT);
            cell(t, nvl(m.getNumeroComprobante()), td, Element.ALIGN_LEFT);
            cell(t, money(m.getDebito()), td, Element.ALIGN_RIGHT);
            cell(t, money(m.getCredito()), td, Element.ALIGN_RIGHT);
            cell(t, money(m.getSaldo()), td, Element.ALIGN_RIGHT);
        }

        return t;
    }

    private void add(
            PdfPTable t,
            String k,
            Object v,
            Font fk,
            Font fv
    ) {

        PdfPCell c1 =
                new PdfPCell(new Phrase(k, fk));

        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(4);

        PdfPCell c2 =
                new PdfPCell(new Phrase(nvl(v), fv));

        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(4);

        t.addCell(c1);
        t.addCell(c2);
    }

    private void header(
            PdfPTable t,
            String text,
            Font f
    ) {

        PdfPCell c =
                new PdfPCell(new Phrase(text, f));

        c.setPadding(4);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);

        t.addCell(c);
    }

    private void cell(
            PdfPTable t,
            String text,
            Font f,
            int align
    ) {

        PdfPCell c =
                new PdfPCell(new Phrase(nvl(text), f));

        c.setPadding(4);
        c.setHorizontalAlignment(align);

        t.addCell(c);
    }

    private String money(
            BigDecimal v
    ) {
        return v == null
                ? "0.00"
                : MONEY.format(v);
    }

    private String nvl(
            Object v
    ) {
        return v == null
                ? ""
                : String.valueOf(v);
    }

    private Image loadLogo() {

        try (InputStream is = getClass().getResourceAsStream(LOGO_PATH)) {

            if (is == null) {
                return null;
            }

            return Image.getInstance(
                    is.readAllBytes()
            );

        } catch (Exception e) {
            return null;
        }
    }

    private static class FooterEvent extends PdfPageEventHelper {

        private final Font footerFont =
                new Font(Font.HELVETICA, 8, Font.NORMAL);

        private final String fechaGeneracion =
                LocalDateTime.now().format(DFH);

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document document
        ) {

            float y =
                    document.bottom() - 18;

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    new Phrase(
                            "Página " + writer.getPageNumber(),
                            footerFont
                    ),
                    (document.right() + document.left()) / 2,
                    y,
                    0
            );

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            "Extracto de cuenta – ASSIP ERP | " + fechaGeneracion,
                            footerFont
                    ),
                    document.right(),
                    y,
                    0
            );
        }
    }
}