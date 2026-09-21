package co.assip.erp.cartera.originacion.impresion;

import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionActuacionDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionFotosDTO;
import co.assip.erp.general.empresas.EmpresaDTO;
import co.assip.erp.shared.config.EmpresaConfigService;
import com.fasterxml.jackson.databind.JsonNode;
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
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.ColumnText;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generador documental de originación. Usa las fotografías suministradas por
 * OriginacionImpresionService; no consulta tablas ni recalcula indicadores.
 *
 * Las autorizaciones legales y los datos actuales de Hoja de Vida se incorporarán
 * mediante sus fuentes verificadas, sin inventar declaraciones ni campos.
 */
@Service
public class OriginacionExpedientePdfService {

    private static final Color VERDE = new Color(4, 120, 87);
    private static final Color VERDE_SUAVE = new Color(236, 253, 245);
    private static final Color GRIS = new Color(71, 85, 105);
    private static final Color BORDE = new Color(203, 213, 225);
    private static final Font TITULO = new Font(Font.HELVETICA, 14, Font.BOLD, VERDE);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 10, Font.BOLD, VERDE);
    private static final Font ETIQUETA = new Font(Font.HELVETICA, 7.4f, Font.BOLD, GRIS);
    private static final Font VALOR = new Font(Font.HELVETICA, 8.2f, Font.NORMAL, Color.BLACK);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 7, Font.NORMAL, GRIS);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] LOGOS = {
            "/static/logo/LOGO_EMPRESA.png",
            "/static/logo/logo-print.png",
            "/static/logo/logo_empresa.jpeg",
            "/static/logo/logo-web.png"
    };

    private final OriginacionImpresionService impresionService;
    private final EmpresaConfigService empresaConfigService;

    public OriginacionExpedientePdfService(
            OriginacionImpresionService impresionService,
            EmpresaConfigService empresaConfigService
    ) {
        this.impresionService = impresionService;
        this.empresaConfigService = empresaConfigService;
    }

    public ResponseEntity<byte[]> generarPdfResponse(Integer idSolicitudCredito) {
        return responder(idSolicitudCredito, null);
    }

    public ResponseEntity<byte[]> generarPdfActuacionResponse(
            Integer idSolicitudCredito, Integer idSolicitudAprobacion
    ) {
        return responder(idSolicitudCredito, idSolicitudAprobacion);
    }

    public byte[] generarPdf(Integer idSolicitudCredito) {
        return generarPdf(impresionService.obtenerExpediente(idSolicitudCredito));
    }

    public byte[] generarPdfActuacion(Integer idSolicitudCredito, Integer idSolicitudAprobacion) {
        return generarPdf(impresionService.obtenerExpedienteActuacion(
                idSolicitudCredito, idSolicitudAprobacion));
    }

    private ResponseEntity<byte[]> responder(Integer idSolicitudCredito, Integer idSolicitudAprobacion) {
        byte[] contenido = idSolicitudAprobacion == null
                ? generarPdf(idSolicitudCredito)
                : generarPdfActuacion(idSolicitudCredito, idSolicitudAprobacion);
        String nombre = "expediente_solicitud_" + idSolicitudCredito
                + (idSolicitudAprobacion == null ? "" : "_actuacion_" + idSolicitudAprobacion)
                + ".pdf";
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setContentType(MediaType.APPLICATION_PDF);
        cabeceras.setContentDisposition(ContentDisposition.inline().filename(nombre).build());
        cabeceras.setCacheControl("no-cache, no-store, must-revalidate");
        cabeceras.setPragma("no-cache");
        cabeceras.setExpires(0);
        return ResponseEntity.ok().headers(cabeceras).body(contenido);
    }

    public byte[] generarPdf(OriginacionImpresionService.ExpedienteImpresion expediente) {
        if (expediente == null || expediente.fotos() == null) {
            throw new IllegalArgumentException("No existe información para generar el expediente.");
        }
        SolicitudAprobacionFotosDTO fotos = expediente.fotos();
        if (vacio(fotos.getFotoSolicitud())) {
            throw new IllegalStateException("La solicitud no tiene fotografía para impresión.");
        }
        EmpresaDTO empresa = empresaConfigService.getEmpresa();
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.LETTER, 37, 37, 47, 48);
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();
            encabezado(documento, empresa, expediente);
            seccionSolicitud(documento, fotos.getFotoSolicitud());
            seccionJson(documento, "2. SOLICITANTE Y CODEUDORES", fotos.getFotoDeudores(), true);
            seccionJson(documento, "3. INFORMACIÓN FINANCIERA", fotos.getFotoFinanciero(), false);
            seccionJson(documento, "4. BIENES Y GARANTÍAS", fotos.getFotoBienes(), false);
            seccionJson(documento, "5. CENTRALES DE RIESGO", fotos.getFotoCentralRiesgo(), false);
            seccionJson(documento, "6. ANÁLISIS DE OTORGAMIENTO", fotos.getFotoAnalisis(), false);
            actuaciones(documento, expediente.actuaciones(), expediente.idSolicitudAprobacion());
            firmas(documento, fotos.getFotoDeudores());
            documento.close();
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el expediente PDF.", e);
        }
    }

    private void encabezado(Document doc, EmpresaDTO empresa,
                            OriginacionImpresionService.ExpedienteImpresion expediente)
            throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{1.1f, 5.9f});
        tabla.setWidthPercentage(100);
        PdfPCell logo = new PdfPCell();
        logo.setBorder(Rectangle.NO_BORDER);
        logo.setPaddingBottom(8);
        Image imagen = logo();
        if (imagen != null) {
            imagen.scaleToFit(72, 57);
            logo.addElement(imagen);
        }
        tabla.addCell(logo);
        PdfPCell datos = new PdfPCell();
        datos.setBorder(Rectangle.NO_BORDER);
        datos.setVerticalAlignment(Element.ALIGN_MIDDLE);
        String razon = empresa == null ? "COOPVALLE" : texto(empresa.getRazonSocial());
        if (razon.equals("No registrado")) razon = "COOPVALLE";
        datos.addElement(new Paragraph(razon, SUBTITULO));
        if (empresa != null) {
            datos.addElement(new Paragraph("NIT: " + texto(empresa.getDocumentoEmpresa()), PEQUENA));
        }
        datos.addElement(new Paragraph("EXPEDIENTE DE SOLICITUD Y OTORGAMIENTO DE CRÉDITO", TITULO));
        datos.addElement(new Paragraph("Solicitud N.º " + expediente.idSolicitudCredito()
                + (expediente.idSolicitudAprobacion() == null ? " · Información de originación"
                : " · Actuación N.º " + expediente.idSolicitudAprobacion()), PEQUENA));
        tabla.addCell(datos);
        doc.add(tabla);
        doc.add(new Paragraph(" "));
    }

    private void seccionSolicitud(Document doc, JsonNode solicitud) throws DocumentException {
        titulo(doc, "1. DATOS Y CONDICIONES DE LA SOLICITUD");
        camposSeleccionados(doc, solicitud, new String[][]{
                {"numero_solicitud", "Número de solicitud"},
                {"fecha_inicio_solicitud", "Fecha de solicitud"},
                {"nombre_linea_credito", "Línea de crédito"},
                {"nombre_clasificacion_credito", "Clasificación"},
                {"nombre_destino_economico", "Destino económico"},
                {"nombre_resultado", "Estado del trámite"},
                {"valor_solicitado", "Valor solicitado"},
                {"plazo_solicitado", "Plazo solicitado (meses)"},
                {"valor_cuota_proyectada", "Cuota proyectada"},
                {"tasa_colocacion_aplicada", "Tasa de colocación aplicada"},
                {"tasa_efectiva_anual", "Tasa efectiva anual"},
                {"nombre_modalidad_interes", "Modalidad de interés"},
                {"nombre_tipo_cuota", "Tipo de cuota"},
                {"nombre_forma_pago", "Forma de pago"},
                {"periodo_meses", "Periodicidad (meses)"},
                {"meses_gracia_capital", "Gracia capital (meses)"},
                {"meses_gracia_interes", "Gracia intereses (meses)"},
                {"nombre_garantia_credito", "Garantía"},
                {"nombre_subgarantia", "Subgarantía"},
                {"nombre_fondo_garantia", "Fondo de garantías"},
                {"valor_fondo_garantia", "Valor del fondo"},
                {"valor_aportes_inicio", "Aportes al inicio"},
                {"valor_aportes_requerido", "Aportes requeridos"},
                {"cupo_maximo_por_aportes", "Cupo por aportes"},
                {"cumple_aportes_inicio", "Validación inicial de aportes"},
                {"cumple_aportes_validacion", "Validación posterior de aportes"},
                {"observacion_asesor", "Observaciones del asesor"}
        });
        // No se descartan campos nuevos del JSON: se imprimen a continuación.
        List<String> usados = new ArrayList<>();
        for (String[] campo : CAMPOS_SOLICITUD) usados.add(campo[0]);
        // El método anterior ya imprimió los campos conocidos; los adicionales
        // quedan disponibles en la sección complementaria sin repetir valores.
        if (solicitud != null && solicitud.isObject()) {
            PdfPTable extras = tabla();
            Iterator<Map.Entry<String, JsonNode>> it =
                    solicitud.properties().iterator();
            int n = 0;
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                if (usados.contains(e.getKey()) || vacio(e.getValue())) continue;
                if (n++ == 0) titulo(doc, "Datos adicionales de la solicitud");
                if (e.getValue().isContainerNode()) {
                    if (extras.size() > 0) { cerrarTabla(doc, extras); extras = tabla(); }
                    nodo(doc, etiqueta(e.getKey()), e.getValue(), 0);
                } else {
                    par(extras, etiqueta(e.getKey()), mostrar(e.getValue(), e.getKey()));
                }
            }
            cerrarTabla(doc, extras);
        }
    }

    private static final String[][] CAMPOS_SOLICITUD = {
            {"numero_solicitud"}, {"fecha_inicio_solicitud"}, {"nombre_linea_credito"},
            {"nombre_clasificacion_credito"}, {"nombre_destino_economico"}, {"nombre_resultado"},
            {"valor_solicitado"}, {"plazo_solicitado"}, {"valor_cuota_proyectada"},
            {"tasa_colocacion_aplicada"}, {"tasa_efectiva_anual"}, {"nombre_modalidad_interes"},
            {"nombre_tipo_cuota"}, {"nombre_forma_pago"}, {"periodo_meses"},
            {"meses_gracia_capital"}, {"meses_gracia_interes"}, {"nombre_garantia_credito"},
            {"nombre_subgarantia"}, {"nombre_fondo_garantia"}, {"valor_fondo_garantia"},
            {"valor_aportes_inicio"}, {"valor_aportes_requerido"},
            {"cupo_maximo_por_aportes"}, {"cumple_aportes_inicio"},
            {"cumple_aportes_validacion"}, {"observacion_asesor"}
    };

    private void camposSeleccionados(Document doc, JsonNode raiz, String[][] campos)
            throws DocumentException {
        PdfPTable tabla = tabla();
        for (String[] campo : campos) {
            JsonNode valor = raiz == null ? null : raiz.get(campo[0]);
            if (vacio(valor)) continue;
            par(tabla, campo[1], mostrar(valor, campo[0]));
        }
        cerrarTabla(doc, tabla);
    }

    private void seccionJson(Document doc, String titulo, JsonNode raiz, boolean nuevaPagina)
            throws DocumentException {
        if (vacio(raiz)) return;
        if (nuevaPagina) doc.newPage();
        titulo(doc, titulo);
        nodo(doc, "", raiz, 0);
    }

    private void nodo(Document doc, String nombre, JsonNode valor, int nivel)
            throws DocumentException {
        if (vacio(valor) || nivel > 12) return;
        if (valor.isArray()) {
            if (!nombre.isBlank()) subtitulo(doc, nombre);
            int i = 0;
            for (JsonNode item : valor) {
                if (vacio(item)) continue;
                if (item.isContainerNode()) {
                    subtitulo(doc, "Registro " + (++i));
                    nodo(doc, "", item, nivel + 1);
                } else {
                    doc.add(new Paragraph((++i) + ". " + mostrar(item, ""), VALOR));
                }
            }
            return;
        }
        if (valor.isObject()) {
            if (!nombre.isBlank()) subtitulo(doc, nombre);
            PdfPTable tabla = tabla();
            Iterator<Map.Entry<String, JsonNode>> campos =
                    valor.properties().iterator();
            while (campos.hasNext()) {
                Map.Entry<String, JsonNode> campo = campos.next();
                if (vacio(campo.getValue())) continue;
                if (campo.getValue().isContainerNode()) {
                    cerrarTabla(doc, tabla);
                    tabla = tabla();
                    nodo(doc, etiqueta(campo.getKey()), campo.getValue(), nivel + 1);
                } else {
                    par(tabla, etiqueta(campo.getKey()), mostrar(campo.getValue(), campo.getKey()));
                }
            }
            cerrarTabla(doc, tabla);
            return;
        }
        doc.add(new Paragraph(nombre + ": " + mostrar(valor, ""), VALOR));
    }

    private void actuaciones(Document doc, List<SolicitudAprobacionActuacionDTO> lista,
                             Integer idActuacion) throws DocumentException {
        if (lista == null || lista.isEmpty()) return;
        doc.newPage();
        titulo(doc, "7. HISTORIAL DE DECISIONES DE APROBACIÓN");
        for (SolicitudAprobacionActuacionDTO a : lista) {
            if (idActuacion != null && !idActuacion.equals(a.getIdSolicitudAprobacion())) continue;
            subtitulo(doc, texto(a.getNombreEnteAprobacion()) + " · " + texto(a.getNombreDecision()));
            PdfPTable tabla = tabla();
            par(tabla, "Fecha de decisión", mostrarFechaHora(a.getFechaDecision()));
            par(tabla, "Acta", texto(a.getNumeroActa()));
            par(tabla, "Fecha de acta", mostrarFecha(a.getFechaActa()));
            par(tabla, "Responsable", texto(a.getNombreUsuarioDecision()));
            par(tabla, "Concepto", texto(a.getConcepto()));
            cerrarTabla(doc, tabla);
        }
    }

    private void firmas(Document doc, JsonNode deudores) throws DocumentException {
        doc.newPage();
        titulo(doc, "8. FIRMAS Y HUELLAS");
        doc.add(new Paragraph(
                "Espacios para firma y huella de las personas vinculadas a la solicitud. "
                        + "Las declaraciones y autorizaciones legales se incorporarán desde el texto "
                        + "institucional aprobado; este espacio no las sustituye.", PEQUENA));
        doc.add(new Paragraph(" "));
        if (deudores != null && deudores.isArray()) {
            int i = 0;
            for (JsonNode persona : deudores) {
                if (!persona.isObject()) continue;
                String nombre = textoNodo(persona, "nombre_completo");
                String rol = textoNodo(persona, "tipo_deudor");
                String documento = textoNodo(persona, "documento");
                bloqueFirma(doc, rol + " · " + nombre, documento);
                i++;
            }
            if (i == 0) bloqueFirma(doc, "Solicitante", "");
        } else {
            bloqueFirma(doc, "Solicitante", "");
        }
    }

    private void bloqueFirma(Document doc, String persona, String identificacion)
            throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{4, 1.3f});
        t.setWidthPercentage(100);
        t.setSpacingBefore(13);
        t.setKeepTogether(true);
        PdfPCell firma = new PdfPCell();
        firma.setBorder(Rectangle.BOX);
        firma.setBorderColor(BORDE);
        firma.setFixedHeight(91);
        firma.setVerticalAlignment(Element.ALIGN_BOTTOM);
        firma.setPadding(7);
        firma.addElement(new Paragraph("_____________________________________", VALOR));
        firma.addElement(new Paragraph("Firma: " + persona, PEQUENA));
        firma.addElement(new Paragraph("Documento: " + identificacion, PEQUENA));
        t.addCell(firma);
        PdfPCell huella = new PdfPCell(new Phrase("Huella", PEQUENA));
        huella.setBorder(Rectangle.BOX);
        huella.setBorderColor(BORDE);
        huella.setHorizontalAlignment(Element.ALIGN_CENTER);
        huella.setVerticalAlignment(Element.ALIGN_BOTTOM);
        huella.setPaddingBottom(5);
        t.addCell(huella);
        doc.add(t);
    }

    private static PdfPTable tabla() {
        PdfPTable t = new PdfPTable(new float[]{1, 1});
        t.setWidthPercentage(100);
        t.setSpacingAfter(7);
        t.setSplitLate(false);
        return t;
    }

    private static void par(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.BOX);
        celda.setBorderColor(BORDE);
        celda.setPadding(5);
        celda.addElement(new Paragraph(etiqueta, ETIQUETA));
        celda.addElement(new Paragraph(valor, VALOR));
        tabla.addCell(celda);
    }

    private static void cerrarTabla(Document doc, PdfPTable tabla) throws DocumentException {
        if (tabla.size() == 0) return;
        if (tabla.size() % 2 != 0) {
            PdfPCell vacia = new PdfPCell(new Phrase(""));
            vacia.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(vacia);
        }
        doc.add(tabla);
    }

    private static void titulo(Document doc, String texto) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(10);
        t.setSpacingAfter(6);
        PdfPCell c = new PdfPCell(new Phrase(texto, SUBTITULO));
        c.setBorder(Rectangle.NO_BORDER);
        c.setBackgroundColor(VERDE_SUAVE);
        c.setPadding(7);
        t.addCell(c);
        doc.add(t);
    }

    private static void subtitulo(
            Document doc,
            String texto
    ) throws DocumentException {

        Paragraph p = new Paragraph(
                texto,
                SUBTITULO
        );

        p.setSpacingBefore(7);
        p.setSpacingAfter(4);

        doc.add(p);
    }

    private static boolean vacio(JsonNode n) {
        return n == null || n.isNull() || n.isMissingNode()
                || (n.isTextual() && n.asText().isBlank())
                || (n.isArray() && n.isEmpty())
                || (n.isObject() && n.isEmpty());
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? "No registrado" : s.trim();
    }

    private static String textoNodo(JsonNode n, String campo) {
        return n != null && n.hasNonNull(campo) ? texto(n.get(campo).asText()) : "No registrado";
    }

    private static String etiqueta(String clave) {
        if (clave == null || clave.isBlank()) return "Dato";
        String s = clave.replace('_', ' ').trim();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String mostrar(JsonNode n, String clave) {
        if (vacio(n)) return "No registrado";
        if (n.isBoolean()) return n.asBoolean() ? "Sí" : "No";
        if (n.isNumber()) {
            String k = clave.toLowerCase(Locale.ROOT);
            if (k.contains("tasa") || k.contains("porcentaje") || k.startsWith("porce")) {
                return formatoNumero(n.decimalValue(), 4) + " %";
            }
            if (k.startsWith("valor_") || k.startsWith("saldo_") || k.startsWith("total_")
                    || k.contains("ingreso") || k.contains("egreso") || k.contains("patrimonio")
                    || k.contains("activo") || k.contains("pasivo") || k.contains("cuota")) {
                return "$ " + formatoNumero(n.decimalValue(), 2);
            }
            return n.asText();
        }
        String s = n.asText();
        if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try { return LocalDate.parse(s).format(FECHA); }
            catch (Exception ignored) { return s; }
        }
        return s.replace('_', ' ');
    }

    private static String formatoNumero(BigDecimal numero, int decimales) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("es", "CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat f = new DecimalFormat(decimales == 4 ? "#,##0.####" : "#,##0.##", simbolos);
        return f.format(numero);
    }

    private static String mostrarFecha(LocalDate fecha) {
        return fecha == null ? "No registrada" : fecha.format(FECHA);
    }

    private static String mostrarFechaHora(LocalDateTime fecha) {
        return fecha == null ? "No registrada" : fecha.format(FECHA_HORA);
    }

    private static Image logo() {
        for (String ruta : LOGOS) {
            try (InputStream recurso = OriginacionExpedientePdfService.class.getResourceAsStream(ruta)) {
                if (recurso == null) continue;
                return Image.getInstance(recurso.readAllBytes());
            } catch (Exception ignored) {
                // Probar la siguiente ubicación institucional existente.
            }
        }
        return null;
    }

    private static final class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                    new Phrase("COOPVALLE · Expediente de originación", PEQUENA),
                    document.left(), document.bottom() - 19, 0);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), PEQUENA),
                    document.right(), document.bottom() - 19, 0);
        }
    }
}
