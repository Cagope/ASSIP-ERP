package co.assip.erp.cartera.consultacreditos.pdf;

import co.assip.erp.cartera.consultacreditos.ConsultaCreditosService;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoDetalleDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoExtractoDTO;
import co.assip.erp.general.empresas.EmpresaDTO;
import co.assip.erp.shared.config.EmpresaConfigService;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Genera el extracto de un crédito en formato PDF.
 *
 * Reutiliza los DTO y consultas existentes del proceso
 * Consulta de Créditos. No crea consultas SQL adicionales.
 */
@Service
public class ConsultaCreditoExtractoPdfService {

    // =========================================================
    // RECURSOS
    // =========================================================

    private static final String[] LOGO_PATHS = {
            "/static/logo/LOGO_EMPRESA.png",
            "/static/logo/logo-print.png",
            "/static/logo/logo_empresa.jpeg",
            "/static/logo/logo-web.png"
    };

    // =========================================================
    // FORMATOS
    // =========================================================

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================================
    // COLORES
    // =========================================================

    private static final Color COLOR_PRIMARIO =
            new Color(4, 120, 87);

    private static final Color COLOR_PRIMARIO_CLARO =
            new Color(236, 253, 245);

    private static final Color COLOR_ENCABEZADO_TABLA =
            new Color(241, 245, 249);

    private static final Color COLOR_BORDE =
            new Color(203, 213, 225);

    private static final Color COLOR_TEXTO_SECUNDARIO =
            new Color(71, 85, 105);

    private static final Color COLOR_ALERTA_CLARO =
            new Color(254, 242, 242);

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final ConsultaCreditosService consultaCreditosService;
    private final EmpresaConfigService empresaConfigService;

    public ConsultaCreditoExtractoPdfService(
            ConsultaCreditosService consultaCreditosService,
            EmpresaConfigService empresaConfigService
    ) {
        this.consultaCreditosService = consultaCreditosService;
        this.empresaConfigService = empresaConfigService;
    }

    // =========================================================
    // RESPUESTA HTTP
    // =========================================================

    public ResponseEntity<byte[]> generarPdfResponse(
            Integer idCarteraCredito
    ) {

        ConsultaCreditoDetalleDTO credito =
                consultaCreditosService.buscarPorId(
                        idCarteraCredito
                );

        byte[] pdf = generarPdf(credito);

        String pagare = normalizarNombreArchivo(
                credito.getPagareCartera()
        );

        if (pagare.isBlank()) {
            pagare = String.valueOf(idCarteraCredito);
        }

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition
                        .inline()
                        .filename(
                                "extracto_credito_"
                                        + pagare
                                        + ".pdf"
                        )
                        .build()
        );

        headers.setCacheControl(
                "no-cache, no-store, must-revalidate"
        );

        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }

    // =========================================================
    // GENERACIÓN
    // =========================================================

    public byte[] generarPdf(
            Integer idCarteraCredito
    ) {

        ConsultaCreditoDetalleDTO credito =
                consultaCreditosService.buscarPorId(
                        idCarteraCredito
                );

        return generarPdf(credito);
    }

    private byte[] generarPdf(
            ConsultaCreditoDetalleDTO credito
    ) {

        List<ConsultaCreditoExtractoDTO> movimientos =
                consultaCreditosService.listarExtracto(
                        credito.getIdCarteraCredito()
                );

        EmpresaDTO empresa =
                empresaConfigService.getEmpresa();

        try (
                ByteArrayOutputStream salida =
                        new ByteArrayOutputStream()
        ) {

            Document documento = new Document(
                    PageSize.A4.rotate(),
                    24,
                    24,
                    34,
                    42
            );

            PdfWriter writer = PdfWriter.getInstance(
                    documento,
                    salida
            );

            writer.setPageEvent(
                    new FooterEvent(
                            credito.getPagareCartera()
                    )
            );

            documento.open();

            Fuentes fuentes = crearFuentes();

            agregarEncabezado(
                    documento,
                    empresa,
                    credito,
                    fuentes
            );

            agregarDatosCredito(
                    documento,
                    credito,
                    fuentes
            );

            agregarResumenFinanciero(
                    documento,
                    credito,
                    movimientos,
                    fuentes
            );

            agregarTablaMovimientos(
                    documento,
                    movimientos,
                    fuentes
            );

            agregarNotaFinal(
                    documento,
                    movimientos,
                    fuentes
            );

            documento.close();

            return salida.toByteArray();

        } catch (Exception error) {

            throw new IllegalStateException(
                    "No fue posible generar el PDF del extracto del crédito "
                            + texto(credito.getPagareCartera())
                            + ".",
                    error
            );
        }
    }

    // =========================================================
    // ENCABEZADO
    // =========================================================

    private void agregarEncabezado(
            Document documento,
            EmpresaDTO empresa,
            ConsultaCreditoDetalleDTO credito,
            Fuentes fuentes
    ) throws DocumentException {

        PdfPTable encabezado = new PdfPTable(
                new float[]{1.45f, 6.55f}
        );

        encabezado.setWidthPercentage(100);

        PdfPCell celdaLogo = new PdfPCell();
        celdaLogo.setBorder(Rectangle.NO_BORDER);
        celdaLogo.setPadding(3);
        celdaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Image logo = cargarLogo();

        if (logo != null) {
            logo.scaleToFit(110, 58);
            logo.setAlignment(Element.ALIGN_CENTER);
            celdaLogo.addElement(logo);
        } else {
            celdaLogo.addElement(
                    new Phrase(
                            "ASSIP ERP",
                            fuentes.tituloEmpresa
                    )
            );
        }

        PdfPCell celdaEmpresa = new PdfPCell();
        celdaEmpresa.setBorder(Rectangle.NO_BORDER);
        celdaEmpresa.setPadding(3);
        celdaEmpresa.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph razonSocial = new Paragraph(
                empresa != null
                        ? texto(empresa.getRazonSocial())
                        : "",
                fuentes.tituloEmpresa
        );
        razonSocial.setAlignment(Element.ALIGN_CENTER);

        Paragraph nit = new Paragraph(
                construirNit(empresa),
                fuentes.texto
        );
        nit.setAlignment(Element.ALIGN_CENTER);

        Paragraph titulo = new Paragraph(
                "EXTRACTO DE CRÉDITO",
                fuentes.tituloInforme
        );
        titulo.setAlignment(Element.ALIGN_CENTER);

        Paragraph agencia = new Paragraph(
                "Agencia: "
                        + texto(credito.getNombreAgencia()),
                fuentes.texto
        );
        agencia.setAlignment(Element.ALIGN_CENTER);

        celdaEmpresa.addElement(razonSocial);
        celdaEmpresa.addElement(nit);
        celdaEmpresa.addElement(titulo);
        celdaEmpresa.addElement(agencia);

        encabezado.addCell(celdaLogo);
        encabezado.addCell(celdaEmpresa);

        documento.add(encabezado);
        documento.add(new LineSeparator());
        documento.add(Chunk.NEWLINE);
    }

    // =========================================================
    // DATOS DEL CRÉDITO
    // =========================================================

    private void agregarDatosCredito(
            Document documento,
            ConsultaCreditoDetalleDTO credito,
            Fuentes fuentes
    ) throws DocumentException {

        documento.add(
                crearTituloSeccion(
                        "Datos del crédito",
                        fuentes
                )
        );

        PdfPTable tabla = new PdfPTable(8);
        tabla.setWidthPercentage(100);

        agregarDato(
                tabla,
                "Pagaré:",
                credito.getPagareCartera(),
                fuentes
        );

        agregarDato(
                tabla,
                "ID asociado:",
                credito.getIdDatosPersonal(),
                fuentes
        );

        agregarDato(
                tabla,
                "Línea:",
                codigoDescripcion(
                        credito.getCodigoLineaCredito(),
                        credito.getNombreLineaCredito()
                ),
                fuentes
        );

        agregarDato(
                tabla,
                "Estado:",
                codigoDescripcion(
                        credito.getCodigoEstadoCartera(),
                        credito.getDescripcionEstadoCartera()
                ),
                fuentes
        );

        agregarDato(
                tabla,
                "Desembolso:",
                fecha(credito.getFechaDesembolso()),
                fuentes
        );

        agregarDato(
                tabla,
                "Vencimiento:",
                fecha(credito.getFechaFinal()),
                fuentes
        );

        agregarDato(
                tabla,
                "Próximo capital:",
                fecha(credito.getProximaFechaCapital()),
                fuentes
        );

        agregarDato(
                tabla,
                "Edad de riesgo:",
                codigoDescripcion(
                        credito.getEdadDeRiesgo(),
                        credito.getDescripcionEdadDeRiesgo()
                ),
                fuentes
        );

        agregarDato(
                tabla,
                "Edad de mora:",
                descripcionEdadMora(credito),
                fuentes
        );

        agregarDato(
                tabla,
                "Garantía:",
                codigoDescripcion(
                        credito.getCodigoGarantiaCredito(),
                        credito.getDescripcionGarantiaCredito()
                ),
                fuentes
        );

        agregarDato(
                tabla,
                "Tipo de cuota:",
                codigoDescripcion(
                        credito.getCodigoTipoCuota(),
                        credito.getDescripcionTipoCuota()
                ),
                fuentes
        );

        agregarDato(
                tabla,
                "Forma de pago:",
                codigoDescripcion(
                        credito.getCodigoFormaPago(),
                        credito.getDescripcionFormaPago()
                ),
                fuentes
        );

        documento.add(tabla);
        documento.add(Chunk.NEWLINE);
    }

    // =========================================================
    // RESUMEN FINANCIERO
    // =========================================================

    private void agregarResumenFinanciero(
            Document documento,
            ConsultaCreditoDetalleDTO credito,
            List<ConsultaCreditoExtractoDTO> movimientos,
            Fuentes fuentes
    ) throws DocumentException {

        documento.add(
                crearTituloSeccion(
                        "Resumen financiero",
                        fuentes
                )
        );

        BigDecimal totalCapital = sumar(
                movimientos,
                ConceptoMovimiento.CAPITAL
        );

        BigDecimal totalIntereses = sumar(
                movimientos,
                ConceptoMovimiento.INTERESES
        );

        BigDecimal totalMora = sumar(
                movimientos,
                ConceptoMovimiento.MORA
        );

        BigDecimal totalSeguro = sumar(
                movimientos,
                ConceptoMovimiento.SEGURO
        );

        BigDecimal totalFondo = sumar(
                movimientos,
                ConceptoMovimiento.FONDO
        );

        BigDecimal totalOtros = sumar(
                movimientos,
                ConceptoMovimiento.OTROS
        );

        BigDecimal totalAplicado = sumar(
                movimientos,
                ConceptoMovimiento.TOTAL
        );

        PdfPTable tabla = new PdfPTable(8);
        tabla.setWidthPercentage(100);

        agregarResumen(
                tabla,
                "Valor desembolsado",
                dinero(credito.getValorDesembolsado()),
                false,
                fuentes
        );

        agregarResumen(
                tabla,
                "Saldo actual",
                dinero(credito.getSaldoActual()),
                true,
                fuentes
        );

        agregarResumen(
                tabla,
                "Capital aplicado",
                dinero(totalCapital),
                false,
                fuentes
        );

        agregarResumen(
                tabla,
                "Intereses",
                dinero(totalIntereses),
                false,
                fuentes
        );

        agregarResumen(
                tabla,
                "Mora",
                dinero(totalMora),
                totalMora.compareTo(BigDecimal.ZERO) > 0,
                fuentes
        );

        agregarResumen(
                tabla,
                "Seguro",
                dinero(totalSeguro),
                false,
                fuentes
        );

        agregarResumen(
                tabla,
                "Fondo y otros",
                dinero(totalFondo.add(totalOtros)),
                false,
                fuentes
        );

        agregarResumen(
                tabla,
                "Total aplicado",
                dinero(totalAplicado),
                true,
                fuentes
        );

        documento.add(tabla);
        documento.add(Chunk.NEWLINE);
    }

    // =========================================================
    // MOVIMIENTOS
    // =========================================================

    private void agregarTablaMovimientos(
            Document documento,
            List<ConsultaCreditoExtractoDTO> movimientos,
            Fuentes fuentes
    ) throws DocumentException {

        documento.add(
                crearTituloSeccion(
                        "Detalle de movimientos",
                        fuentes
                )
        );

        PdfPTable tabla = new PdfPTable(
                new float[]{
                        0.82f,
                        1.15f,
                        1.10f,
                        1.10f,
                        0.95f,
                        0.95f,
                        0.95f,
                        0.95f,
                        1.15f,
                        1.15f,
                        0.70f
                }
        );

        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);

        agregarEncabezadoTabla(tabla, "Fecha", fuentes);
        agregarEncabezadoTabla(tabla, "Comprobante", fuentes);
        agregarEncabezadoTabla(tabla, "Capital", fuentes);
        agregarEncabezadoTabla(tabla, "Intereses", fuentes);
        agregarEncabezadoTabla(tabla, "Mora", fuentes);
        agregarEncabezadoTabla(tabla, "Seguro", fuentes);
        agregarEncabezadoTabla(tabla, "Fondo", fuentes);
        agregarEncabezadoTabla(tabla, "Otros", fuentes);
        agregarEncabezadoTabla(tabla, "Total", fuentes);
        agregarEncabezadoTabla(tabla, "Medio de pago", fuentes);
        agregarEncabezadoTabla(tabla, "Días mora", fuentes);

        if (movimientos == null || movimientos.isEmpty()) {

            PdfPCell sinMovimientos = new PdfPCell(
                    new Phrase(
                            "El crédito no tiene movimientos registrados.",
                            fuentes.tabla
                    )
            );

            sinMovimientos.setColspan(11);
            sinMovimientos.setPadding(8);
            sinMovimientos.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );
            sinMovimientos.setBorderColor(COLOR_BORDE);

            tabla.addCell(sinMovimientos);
            documento.add(tabla);
            return;
        }

        for (ConsultaCreditoExtractoDTO movimiento : movimientos) {

            boolean tieneMora =
                    decimal(movimiento.getValorInteresMora())
                            .compareTo(BigDecimal.ZERO) > 0
                            || entero(movimiento.getDiasMora()) > 0;

            Color fondo = tieneMora
                    ? COLOR_ALERTA_CLARO
                    : Color.WHITE;

            agregarCeldaTabla(
                    tabla,
                    fecha(movimiento.getFechaPago()),
                    Element.ALIGN_CENTER,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    comprobante(movimiento),
                    Element.ALIGN_LEFT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    dinero(movimiento.getValorCapital()),
                    Element.ALIGN_RIGHT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    dinero(movimiento.getTotalInteresesRegistrados()),
                    Element.ALIGN_RIGHT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    dinero(movimiento.getValorInteresMora()),
                    Element.ALIGN_RIGHT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    dinero(movimiento.getValorSeguro()),
                    Element.ALIGN_RIGHT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    dinero(movimiento.getValorFondoGarantia()),
                    Element.ALIGN_RIGHT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    dinero(movimiento.getTotalOtrosConceptos()),
                    Element.ALIGN_RIGHT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    dinero(movimiento.getTotalComponentesRegistrados()),
                    Element.ALIGN_RIGHT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    texto(movimiento.getMedioPagoPrincipal()),
                    Element.ALIGN_LEFT,
                    fondo,
                    fuentes
            );

            agregarCeldaTabla(
                    tabla,
                    String.valueOf(
                            entero(movimiento.getDiasMora())
                    ),
                    Element.ALIGN_CENTER,
                    fondo,
                    fuentes
            );
        }

        documento.add(tabla);
    }

    // =========================================================
    // NOTA FINAL
    // =========================================================

    private void agregarNotaFinal(
            Document documento,
            List<ConsultaCreditoExtractoDTO> movimientos,
            Fuentes fuentes
    ) throws DocumentException {

        documento.add(Chunk.NEWLINE);

        String textoNota =
                movimientos == null || movimientos.isEmpty()
                        ? "Nota: a la fecha de generación no existen movimientos registrados en el extracto del crédito."
                        : "Nota: los valores corresponden a los movimientos registrados en el sistema a la fecha de generación.";

        Paragraph nota = new Paragraph(
                textoNota,
                fuentes.nota
        );

        nota.setAlignment(Element.ALIGN_LEFT);
        documento.add(nota);
    }

    // =========================================================
    // COMPONENTES VISUALES
    // =========================================================

    private Paragraph crearTituloSeccion(
            String texto,
            Fuentes fuentes
    ) {

        Paragraph titulo = new Paragraph(
                texto,
                fuentes.subtitulo
        );

        titulo.setSpacingAfter(5);
        return titulo;
    }

    private void agregarDato(
            PdfPTable tabla,
            String etiqueta,
            Object contenido,
            Fuentes fuentes
    ) {

        PdfPCell celdaEtiqueta = new PdfPCell(
                new Phrase(
                        etiqueta,
                        fuentes.etiqueta
                )
        );

        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        celdaEtiqueta.setPadding(3);
        celdaEtiqueta.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        PdfPCell celdaValor = new PdfPCell(
                new Phrase(
                        texto(contenido),
                        fuentes.texto
                )
        );

        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setPadding(3);
        celdaValor.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }

    private void agregarResumen(
            PdfPTable tabla,
            String etiqueta,
            String contenido,
            boolean destacado,
            Fuentes fuentes
    ) {

        PdfPCell celda = new PdfPCell();

        celda.setPadding(5);
        celda.setBorderColor(
                destacado
                        ? COLOR_PRIMARIO
                        : COLOR_BORDE
        );
        celda.setBackgroundColor(
                destacado
                        ? COLOR_PRIMARIO_CLARO
                        : Color.WHITE
        );

        Paragraph textoEtiqueta = new Paragraph(
                etiqueta,
                fuentes.etiquetaResumen
        );

        Paragraph textoValor = new Paragraph(
                contenido,
                destacado
                        ? fuentes.valorDestacado
                        : fuentes.valorResumen
        );

        textoValor.setSpacingBefore(2);

        celda.addElement(textoEtiqueta);
        celda.addElement(textoValor);
        tabla.addCell(celda);
    }

    private void agregarEncabezadoTabla(
            PdfPTable tabla,
            String texto,
            Fuentes fuentes
    ) {

        PdfPCell celda = new PdfPCell(
                new Phrase(
                        texto,
                        fuentes.tablaEncabezado
                )
        );

        celda.setPadding(4);
        celda.setBackgroundColor(
                COLOR_ENCABEZADO_TABLA
        );
        celda.setBorderColor(COLOR_BORDE);
        celda.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );
        celda.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        tabla.addCell(celda);
    }

    private void agregarCeldaTabla(
            PdfPTable tabla,
            String contenido,
            int alineacion,
            Color fondo,
            Fuentes fuentes
    ) {

        PdfPCell celda = new PdfPCell(
                new Phrase(
                        texto(contenido),
                        fuentes.tabla
                )
        );

        celda.setPadding(3.5f);
        celda.setBorderColor(COLOR_BORDE);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );
        celda.setBackgroundColor(fondo);

        tabla.addCell(celda);
    }

    // =========================================================
    // TOTALES
    // =========================================================

    private BigDecimal sumar(
            List<ConsultaCreditoExtractoDTO> movimientos,
            ConceptoMovimiento concepto
    ) {

        if (movimientos == null || movimientos.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ConsultaCreditoExtractoDTO movimiento : movimientos) {

            BigDecimal importe = switch (concepto) {
                case CAPITAL -> movimiento.getValorCapital();
                case INTERESES -> movimiento.getTotalInteresesRegistrados();
                case MORA -> movimiento.getValorInteresMora();
                case SEGURO -> movimiento.getValorSeguro();
                case FONDO -> movimiento.getValorFondoGarantia();
                case OTROS -> movimiento.getTotalOtrosConceptos();
                case TOTAL -> movimiento.getTotalComponentesRegistrados();
            };

            total = total.add(decimal(importe));
        }

        return total;
    }

    // =========================================================
    // FORMATOS
    // =========================================================

    private String construirNit(
            EmpresaDTO empresa
    ) {

        if (empresa == null) {
            return "";
        }

        String documento = texto(
                empresa.getDocumentoEmpresa()
        ).trim();

        String digito = texto(
                empresa.getDigitoVerificacion()
        ).trim();

        if (documento.isBlank()) {
            return "";
        }

        return digito.isBlank()
                ? "NIT: " + documento
                : "NIT: "
                + documento
                + " - "
                + digito;
    }

    private String fecha(
            LocalDate valor
    ) {

        return valor == null
                ? ""
                : valor.format(FORMATO_FECHA);
    }

    private String dinero(
            BigDecimal valor
    ) {

        DecimalFormatSymbols simbolos =
                new DecimalFormatSymbols(
                        new Locale("es", "CO")
                );

        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        DecimalFormat formato = new DecimalFormat(
                "$ #,##0",
                simbolos
        );

        formato.setRoundingMode(
                RoundingMode.HALF_UP
        );

        return formato.format(decimal(valor));
    }

    private String comprobante(
            ConsultaCreditoExtractoDTO movimiento
    ) {

        String completo = texto(
                movimiento.getComprobanteCompleto()
        ).trim();

        if (!completo.isBlank()) {
            return completo;
        }

        String tipo = texto(
                movimiento.getTipoComprobante()
        ).trim();

        String numero = texto(
                movimiento.getNumeroComprobante()
        ).trim();

        return (tipo + " " + numero).trim();
    }

    private String codigoDescripcion(
            String codigo,
            String descripcion
    ) {

        String codigoNormalizado = texto(codigo).trim();
        String descripcionNormalizada = texto(descripcion).trim();

        if (codigoNormalizado.isBlank()) {
            return descripcionNormalizada;
        }

        if (
                descripcionNormalizada.isBlank()
                        || descripcionNormalizada.equalsIgnoreCase(
                        codigoNormalizado
                )
        ) {
            return codigoNormalizado;
        }

        return codigoNormalizado
                + " - "
                + descripcionNormalizada;
    }

    private String descripcionEdadMora(
            ConsultaCreditoDetalleDTO credito
    ) {

        if (
                decimal(credito.getSaldoActual())
                        .compareTo(BigDecimal.ZERO) <= 0
        ) {
            return "No aplica";
        }

        int diasMora =
                credito.getDiasParaProximoCapital() != null
                        && credito.getDiasParaProximoCapital() < 0
                        ? Math.abs(
                        credito.getDiasParaProximoCapital()
                )
                        : 0;

        String clasificacion = codigoDescripcion(
                credito.getEdadDeMora(),
                credito.getDescripcionEdadDeMora()
        );

        if (diasMora <= 0) {
            return clasificacion.isBlank()
                    ? "Al día"
                    : clasificacion + " - Al día";
        }

        return clasificacion.isBlank()
                ? diasMora + " días"
                : clasificacion
                + " - "
                + diasMora
                + " días";
    }

    private String normalizarNombreArchivo(
            String valor
    ) {

        return texto(valor)
                .trim()
                .replaceAll(
                        "[^a-zA-Z0-9_-]",
                        "_"
                );
    }

    private String texto(
            Object valor
    ) {

        return valor == null
                ? ""
                : String.valueOf(valor);
    }

    private BigDecimal decimal(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private int entero(
            Integer valor
    ) {

        return valor == null
                ? 0
                : valor;
    }

    // =========================================================
    // LOGO
    // =========================================================

    private Image cargarLogo() {

        for (String ruta : LOGO_PATHS) {

            try (
                    InputStream entrada =
                            getClass().getResourceAsStream(ruta)
            ) {

                if (entrada == null) {
                    continue;
                }

                return Image.getInstance(
                        entrada.readAllBytes()
                );

            } catch (Exception ignored) {
                // Se intenta la siguiente ruta disponible.
            }
        }

        return null;
    }

    // =========================================================
    // FUENTES
    // =========================================================

    private Fuentes crearFuentes() {

        return new Fuentes(
                new Font(
                        Font.HELVETICA,
                        13,
                        Font.BOLD,
                        COLOR_PRIMARIO
                ),
                new Font(
                        Font.HELVETICA,
                        11,
                        Font.BOLD,
                        Color.BLACK
                ),
                new Font(
                        Font.HELVETICA,
                        9,
                        Font.BOLD,
                        COLOR_PRIMARIO
                ),
                new Font(
                        Font.HELVETICA,
                        7.5f,
                        Font.BOLD,
                        COLOR_TEXTO_SECUNDARIO
                ),
                new Font(
                        Font.HELVETICA,
                        7.5f,
                        Font.NORMAL,
                        Color.BLACK
                ),
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.BOLD,
                        COLOR_TEXTO_SECUNDARIO
                ),
                new Font(
                        Font.HELVETICA,
                        8,
                        Font.BOLD,
                        Color.BLACK
                ),
                new Font(
                        Font.HELVETICA,
                        8,
                        Font.BOLD,
                        COLOR_PRIMARIO
                ),
                new Font(
                        Font.HELVETICA,
                        6.5f,
                        Font.BOLD,
                        Color.BLACK
                ),
                new Font(
                        Font.HELVETICA,
                        6.3f,
                        Font.NORMAL,
                        Color.BLACK
                ),
                new Font(
                        Font.HELVETICA,
                        7,
                        Font.ITALIC,
                        COLOR_TEXTO_SECUNDARIO
                )
        );
    }

    private record Fuentes(
            Font tituloEmpresa,
            Font tituloInforme,
            Font subtitulo,
            Font etiqueta,
            Font texto,
            Font etiquetaResumen,
            Font valorResumen,
            Font valorDestacado,
            Font tablaEncabezado,
            Font tabla,
            Font nota
    ) {
    }

    private enum ConceptoMovimiento {
        CAPITAL,
        INTERESES,
        MORA,
        SEGURO,
        FONDO,
        OTROS,
        TOTAL
    }

    // =========================================================
    // PIE DE PÁGINA
    // =========================================================

    private static class FooterEvent
            extends PdfPageEventHelper {

        private final Font fuentePie = new Font(
                Font.HELVETICA,
                7,
                Font.NORMAL,
                COLOR_TEXTO_SECUNDARIO
        );

        private final String fechaGeneracion =
                LocalDateTime.now().format(
                        FORMATO_FECHA_HORA
                );

        private final String pagare;

        private FooterEvent(
                String pagare
        ) {
            this.pagare = pagare == null
                    ? ""
                    : pagare;
        }

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document documento
        ) {

            float posicionY =
                    documento.bottom() - 18;

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_LEFT,
                    new Phrase(
                            "Pagaré: " + pagare,
                            fuentePie
                    ),
                    documento.left(),
                    posicionY,
                    0
            );

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    new Phrase(
                            "Página "
                                    + writer.getPageNumber(),
                            fuentePie
                    ),
                    (
                            documento.right()
                                    + documento.left()
                    ) / 2,
                    posicionY,
                    0
            );

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            "Generado por ASSIP ERP | "
                                    + fechaGeneracion,
                            fuentePie
                    ),
                    documento.right(),
                    posicionY,
                    0
            );
        }
    }
}
