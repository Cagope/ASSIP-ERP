
package co.assip.erp.contabilidad.informes.pyg.individual;

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
import com.lowagie.text.pdf.ColumnText;
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
 * Genera el Estado de Excedentes o Pérdidas con formato individual.
 * Presenta un período, cuentas detalladas y firmas opcionales.
 */
@Service
public class PyGIndividualPdfService {
    private static final Locale COLOMBIA =
            Locale.forLanguageTag("es-CO");

    private static final Color AZUL =
            new Color(23, 59, 92);
    private static final Color FONDO =
            new Color(226, 237, 247);
    private static final Color FONDO_RESULTADO =
            new Color(211, 229, 243);
    private static final Color BORDE =
            new Color(211, 223, 234);
    private static final Color TEXTO =
            new Color(39, 49, 60);
    private static final Color ROJO =
            new Color(153, 27, 27);

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
     * Construye el PDF con los resultados previamente calculados.
     * No realiza consultas ni modifica movimientos contables.
     */
    public byte[] generar(
            PyGIndividualService.ResultadoPyG resultado,
            String codigoAgencia,
            String nombreAgencia,
            FirmasDTO firmas
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado del PyG individual es obligatorio."
            );
        }

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(
                    PageSize.A4, 42, 42, 55, 48
            );

            PdfWriter escritor = PdfWriter.getInstance(
                    documento, salida
            );
            escritor.setPageEvent(new PiePagina());
            documento.open();

            agregarEncabezado(
                    documento,
                    resultado,
                    codigoAgencia,
                    nombreAgencia
            );

            agregarDetalle(documento, resultado);

            if (firmas != null) {
                agregarFirmas(documento, firmas);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el PDF del PyG individual.",
                    e
            );
        }
    }

    /**
     * Identifica la entidad, agencia, período y nivel de detalle.
     * Mantiene la presentación institucional de los informes PyG.
     */
    private void agregarEncabezado(
            Document documento,
            PyGIndividualService.ResultadoPyG resultado,
            String codigoAgencia,
            String nombreAgencia
    ) throws DocumentException {
        Paragraph entidad = new Paragraph(
                "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE",
                TITULO
        );
        entidad.setAlignment(Element.ALIGN_CENTER);
        documento.add(entidad);

        Paragraph titulo = new Paragraph(
                "ESTADO DE EXCEDENTES O PERDIDAS - INDIVIDUAL",
                SUBTITULO
        );
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(7);
        documento.add(titulo);

        Paragraph agencia = new Paragraph(
                "Agencia: " + texto(codigoAgencia)
                        + " - " + texto(nombreAgencia),
                NORMAL
        );
        agencia.setAlignment(Element.ALIGN_CENTER);
        agencia.setSpacingBefore(8);
        documento.add(agencia);

        Paragraph periodo = new Paragraph(
                "Período: " + periodo(
                        resultado.ano(),
                        resultado.mes()
                ),
                NORMAL
        );
        periodo.setAlignment(Element.ALIGN_CENTER);
        periodo.setSpacingBefore(4);
        documento.add(periodo);

        Paragraph modalidad = new Paragraph(
                resultado.incluirDetalle()
                        ? "Presentación: Con detalle de subcuentas"
                        : "Presentación: Sin detalle de subcuentas",
                PEQUENA
        );
        modalidad.setAlignment(Element.ALIGN_CENTER);
        modalidad.setSpacingBefore(4);
        modalidad.setSpacingAfter(16);
        documento.add(modalidad);
    }

    /**
     * Construye el detalle contable con una sola columna monetaria.
     * Destaca agrupaciones, subtotales y resultado del ejercicio.
     */
    private void agregarDetalle(
            Document documento,
            PyGIndividualService.ResultadoPyG resultado
    ) throws DocumentException {
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{14, 62, 24});
        tabla.setHeaderRows(1);
        tabla.setSpacingAfter(15);

        agregarCabecera(tabla, "Código", Element.ALIGN_LEFT);
        agregarCabecera(tabla, "Concepto", Element.ALIGN_LEFT);
        agregarCabecera(
                tabla,
                periodo(resultado.ano(), resultado.mes()),
                Element.ALIGN_RIGHT
        );

        if (resultado.filas() != null) {
            for (PyGIndividualService.FilaPyG fila : resultado.filas()) {
                agregarFila(tabla, fila);
            }
        }

        documento.add(tabla);
    }

    /**
     * Presenta cada cuenta o total según su clasificación.
     * Indenta las subcuentas y conserva los importes con signo.
     */
    private void agregarFila(
            PdfPTable tabla,
            PyGIndividualService.FilaPyG fila
    ) {
        boolean grupo = "GRUPO".equals(fila.tipo());
        boolean total = "TOTAL".equals(fila.tipo());
        boolean resultadoFinal = "RESULTADO".equals(fila.tipo());
        boolean destacada = grupo || total || resultadoFinal;

        Font fuente = destacada ? NEGRITA : NORMAL;

        Color fondo = resultadoFinal
                ? FONDO_RESULTADO
                : total || grupo ? FONDO : Color.WHITE;

        if (resultadoFinal
                && fila.valor() != null
                && fila.valor().signum() < 0) {
            fuente = new Font(
                    Font.HELVETICA, 8, Font.BOLD, ROJO
            );
        }

        agregarCelda(
                tabla,
                texto(fila.codigo()),
                fuente,
                Element.ALIGN_LEFT,
                fondo,
                0
        );

        int sangria = "CUENTA".equals(fila.tipo())
                ? Math.max(0, fila.nivel() - 2) * 8
                : 0;

        agregarCelda(
                tabla,
                texto(fila.nombre()),
                fuente,
                Element.ALIGN_LEFT,
                fondo,
                sangria
        );

        agregarCelda(
                tabla,
                numero(fila.valor()),
                fuente,
                Element.ALIGN_RIGHT,
                fondo,
                0
        );
    }

    /**
     * Construye las cabeceras repetibles de la tabla.
     * Mantiene la alineación derecha de los valores.
     */
    private void agregarCabecera(
            PdfPTable tabla,
            String contenido,
            int alineacion
    ) {
        PdfPCell celda = new PdfPCell(
                new Phrase(texto(contenido), NEGRITA)
        );
        celda.setBackgroundColor(FONDO);
        celda.setBorderColor(BORDE);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(8);
        tabla.addCell(celda);
    }

    /**
     * Construye una celda con color, alineación y sangría.
     * Evita modificar los valores numéricos del informe.
     */
    private void agregarCelda(
            PdfPTable tabla,
            String contenido,
            Font fuente,
            int alineacion,
            Color fondo,
            int sangria
    ) {
        PdfPCell celda = new PdfPCell(
                new Phrase(texto(contenido), fuente)
        );
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(BORDE);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPaddingTop(6);
        celda.setPaddingBottom(6);
        celda.setPaddingLeft(7 + sangria);
        celda.setPaddingRight(7);
        tabla.addCell(celda);
    }

    /**
     * Incorpora únicamente los firmantes configurados.
     * Mantiene juntos nombre, cargo y tarjeta profesional.
     */
    private void agregarFirmas(
            Document documento,
            FirmasDTO firmas
    ) throws DocumentException {
        FirmanteDTO[] firmantes = {
                firmas.firmante1(),
                firmas.firmante2(),
                firmas.firmante3()
        };

        int cantidad = 0;

        for (FirmanteDTO firmante : firmantes) {
            if (tieneNombre(firmante)) {
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

        for (FirmanteDTO firmante : firmantes) {
            if (!tieneNombre(firmante)) {
                continue;
            }

            PdfPCell celda = new PdfPCell();
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(8);

            Paragraph linea = new Paragraph(
                    "________________________",
                    NORMAL
            );
            linea.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(linea);

            Paragraph nombre = new Paragraph(
                    firmante.nombre(),
                    NEGRITA
            );
            nombre.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(nombre);

            Paragraph cargo = new Paragraph(
                    texto(firmante.titulo()),
                    PEQUENA
            );
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

    /** Comprueba si el firmante tiene un nombre configurado. */
    private boolean tieneNombre(FirmanteDTO firmante) {
        return firmante != null
                && firmante.nombre() != null
                && !firmante.nombre().isBlank();
    }

    /**
     * Formatea los importes con separadores colombianos.
     * Los valores negativos se presentan entre paréntesis.
     */
    private String numero(BigDecimal valor) {
        DecimalFormatSymbols simbolos =
                DecimalFormatSymbols.getInstance(COLOMBIA);

        DecimalFormat formato = new DecimalFormat(
                "#,##0.00;(#,##0.00)",
                simbolos
        );

        return formato.format(
                valor == null ? BigDecimal.ZERO : valor
        );
    }

    /** Obtiene el nombre del mes y año del informe. */
    private String periodo(int ano, int mes) {
        return Month.of(mes)
                .getDisplayName(
                        TextStyle.FULL,
                        COLOMBIA
                )
                .toUpperCase(COLOMBIA)
                + " " + ano;
    }

    /** Normaliza textos opcionales. */
    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    /**
     * Incorpora fecha de generación y número de página.
     * Se ejecuta en todas las páginas del documento.
     */
    private static class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(
                PdfWriter escritor,
                Document documento
        ) {
            String fecha = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy HH:mm"
                    )
            );

            ColumnText.showTextAligned(
                    escritor.getDirectContent(),
                    Element.ALIGN_LEFT,
                    new Phrase(
                            "Generado: " + fecha,
                            PEQUENA
                    ),
                    documento.left(),
                    documento.bottom() - 20,
                    0
            );

            ColumnText.showTextAligned(
                    escritor.getDirectContent(),
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            "Página " + escritor.getPageNumber(),
                            PEQUENA
                    ),
                    documento.right(),
                    documento.bottom() - 20,
                    0
            );
        }
    }
}
