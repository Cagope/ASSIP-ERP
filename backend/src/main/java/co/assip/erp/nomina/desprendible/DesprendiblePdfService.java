package co.assip.erp.nomina.desprendible;

import co.assip.erp.nomina.desprendible.dto.DesprendibleConceptoDTO;
import co.assip.erp.nomina.desprendible.dto.DesprendibleEmpleadoDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
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
import java.util.List;
import com.lowagie.text.pdf.draw.LineSeparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesprendiblePdfService {

    private final DesprendibleService service;
    private final DesprendibleRepository repository;
    private static final String OWNER_PASSWORD = "ASSIP-ERP";
    private static final String LOGO_PATH = "/static/logo/LOGO_EMPRESA.PNG";

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DFH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");

    // =====================================================
    // PUBLIC
    // =====================================================
    public ResponseEntity<byte[]> generarPdfResponse(DesprendibleEmpleadoDTO dto) {

        byte[] pdf = generarPdf(dto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("desprendible_" + dto.getIdContrato() + ".pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    public byte[] generarPdf(DesprendibleEmpleadoDTO dto) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.LETTER, 36, 36, 50, 50);

            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new FooterEvent());

            // 🔐 Protección PDF
            writer.setEncryption(
                    dto.getDocumento().getBytes(),
                    OWNER_PASSWORD.getBytes(),
                    PdfWriter.ALLOW_PRINTING,
                    PdfWriter.ENCRYPTION_AES_128
            );

            document.open();

            // =========================
            // FUENTES
            // =========================
            Font title = new Font(Font.HELVETICA, 13, Font.BOLD);
            Font subtitle = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font label = new Font(Font.HELVETICA, 9, Font.BOLD);
            Font value = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Font small = new Font(Font.HELVETICA, 8, Font.NORMAL);

            // =========================
            // CABECERA (LOGO + TITULO)
            // =========================
            PdfPTable header = new PdfPTable(new float[]{1.6f, 4.4f});
            header.setWidthPercentage(100);

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setPadding(5);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Image logo = loadLogo();
            if (logo != null) {
                logo.scaleToFit(120, 60);
                logoCell.addElement(logo);
            } else {
                logoCell.addElement(new Phrase("ASSIP\nSolidaria y Financiera", subtitle));
            }

            PdfPCell titulo = new PdfPCell(
                    new Phrase("DESPRENDIBLE DE NÓMINA", title)
            );
            titulo.setBorder(Rectangle.NO_BORDER);
            titulo.setHorizontalAlignment(Element.ALIGN_RIGHT);
            titulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titulo.setPadding(5);

            header.addCell(logoCell);
            header.addCell(titulo);

            document.add(header);
            document.add(new LineSeparator());

            // =========================
            // PERÍODO
            // =========================
            document.add(new Paragraph("Período de nómina", subtitle));

            PdfPTable periodo = new PdfPTable(4);
            periodo.setWidthPercentage(100);

            add(periodo, "Período:", dto.getPeriodoDescripcion(), label, value);
            add(periodo, "Desde:", dto.getFechaInicio().format(DF), label, value);
            add(periodo, "Hasta:", dto.getFechaFin().format(DF), label, value);
            add(periodo, "Contrato:", dto.getIdContrato(), label, value);

            document.add(periodo);
            document.add(Chunk.NEWLINE);

            // =========================
            // EMPLEADO
            // =========================
            document.add(new Paragraph("Datos del empleado", subtitle));

            PdfPTable emp = new PdfPTable(4);
            emp.setWidthPercentage(100);

            add(emp, "Empleado:", dto.getNombreCompleto(), label, value);
            add(emp, "Documento:", dto.getDocumento(), label, value);
            add(emp, "Cargo:", dto.getCargo(), label, value);
            add(emp, "Salario base:", money(dto.getSalarioBase()), label, value);

            document.add(emp);
            document.add(Chunk.NEWLINE);

            // =========================
            // DEVENGADOS
            // =========================
            document.add(new Paragraph("Devengados", subtitle));
            document.add(tablaConceptos(dto.getDevengados()));

            right(document,
                    "Total devengados: " + money(dto.getTotales().getTotalDevengados()),
                    label
            );

            document.add(Chunk.NEWLINE);

            // =========================
            // DEDUCCIONES
            // =========================
            document.add(new Paragraph("Deducciones", subtitle));
            document.add(tablaConceptos(dto.getDeducciones()));

            right(document,
                    "Total deducciones: " + money(dto.getTotales().getTotalDeducciones()),
                    label
            );

            document.add(Chunk.NEWLINE);

            // =========================
            // NETO
            // =========================
            PdfPTable neto = new PdfPTable(2);
            neto.setWidthPercentage(40);
            neto.setHorizontalAlignment(Element.ALIGN_RIGHT);

            add(neto, "NETO A PAGAR:", money(dto.getTotales().getNetoPagar()), subtitle, subtitle);
            document.add(neto);

            document.close();
            writer.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("Error generando PDF del desprendible", e);
        }
    }

    // =====================================================
    // FOOTER EVENT (Página X de Y)
    // =====================================================
    private static class FooterEvent extends PdfPageEventHelper {

        private final Font footerFont =
                new Font(Font.HELVETICA, 8, Font.NORMAL);

        private final String fechaGeneracion =
                LocalDateTime.now().format(DFH);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase(
                    "Página " + writer.getPageNumber() + " de ",
                    footerFont
            );

            float x = (document.right() + document.left()) / 2;
            float y = document.bottom() - 20;

            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_CENTER,
                    footer,
                    x,
                    y,
                    0
            );

            // Texto derecho
            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            "Desprendible de Nómina – ASSIP ERP | " + fechaGeneracion,
                            footerFont
                    ),
                    document.right(),
                    y,
                    0
            );
        }
    }

    // =====================================================
    // TABLA DE CONCEPTOS
    // =====================================================
    private PdfPTable tablaConceptos(List<DesprendibleConceptoDTO> items) {

        PdfPTable t = new PdfPTable(new float[]{1.2f, 3.5f, 1.1f, 1.4f});
        t.setWidthPercentage(100);

        Font th = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font td = new Font(Font.HELVETICA, 9, Font.NORMAL);

        header(t, "Código", th);
        header(t, "Concepto", th);
        header(t, "Cant.", th);
        header(t, "Valor", th);

        if (items == null || items.isEmpty()) {
            PdfPCell c = new PdfPCell(new Phrase("Sin registros", td));
            c.setColspan(4);
            c.setPadding(6);
            t.addCell(c);
            return t;
        }

        for (DesprendibleConceptoDTO x : items) {
            cell(t, x.getCodigoConcepto(), td, Element.ALIGN_LEFT);
            cell(t, x.getNombreConcepto(), td, Element.ALIGN_LEFT);
            cell(t, num(x.getCantidad()), td, Element.ALIGN_RIGHT);
            cell(t, money(x.getValor()), td, Element.ALIGN_RIGHT);
        }
        return t;
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private void add(PdfPTable t, String k, Object v, Font fk, Font fv) {
        PdfPCell c1 = new PdfPCell(new Phrase(k, fk));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(4);

        PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(v), fv));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(4);

        t.addCell(c1);
        t.addCell(c2);
    }

    private void header(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(5);
        t.addCell(c);
    }

    private void cell(PdfPTable t, String text, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(5);
        c.setHorizontalAlignment(align);
        t.addCell(c);
    }

    private void right(Document document, String text, Font f)
            throws DocumentException {

        Paragraph p = new Paragraph(text, f);
        p.setAlignment(Element.ALIGN_RIGHT);
        document.add(p);
    }

    private String money(BigDecimal v) {
        return v == null ? "0.00" : MONEY.format(v);
    }

    private String num(BigDecimal v) {
        return v == null ? "" : MONEY.format(v);
    }

    private Image loadLogo() {
        try (InputStream is = getClass().getResourceAsStream(LOGO_PATH)) {
            if (is == null) return null;
            return Image.getInstance(is.readAllBytes());
        } catch (Exception e) {
            return null;
        }
    }

    public ResponseEntity<byte[]> generarZipPeriodo(Integer idPeriodo) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // 1) Obtener empleados/contratos del período
            List<DesprendibleRepository.EmpleadoPeriodoRow> empleados =
                    repository.listarEmpleadosPeriodo(idPeriodo);

            if (empleados.isEmpty()) {
                throw new IllegalStateException(
                        "No hay empleados para generar desprendibles"
                );
            }

            // 2) Generar PDF por contrato y meter al ZIP
            for (var e : empleados) {

                DesprendibleEmpleadoDTO dto =
                        service.generar(idPeriodo, e.idContrato());

                byte[] pdfBytes = generarPdf(dto);

                String nombreArchivo =
                        e.documento() + "_" +
                                e.nombreCompleto()
                                        .replace(" ", "_")
                                        .toUpperCase() +
                                ".pdf";

                ZipEntry entry = new ZipEntry(nombreArchivo);
                zos.putNextEntry(entry);
                zos.write(pdfBytes);
                zos.closeEntry();
            }

            zos.finish();

            // 3) Headers ZIP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("desprendibles_periodo_" + idPeriodo + ".zip")
                            .build()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error generando ZIP de desprendibles", e
            );
        }
    }

}