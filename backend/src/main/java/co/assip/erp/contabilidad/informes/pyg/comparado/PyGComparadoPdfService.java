
package co.assip.erp.contabilidad.informes.pyg.comparado;

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
 * Genera el Estado de Excedentes o Pérdidas con formato comparado.
 * Presenta ambos períodos, variaciones, subtotales y firmas opcionales.
 */
@Service
public class PyGComparadoPdfService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final Color AZUL = new Color(23, 59, 92);
    private static final Color FONDO = new Color(226, 237, 247);
    private static final Color FONDO_RESULTADO = new Color(211, 229, 243);
    private static final Color BORDE = new Color(211, 223, 234);
    private static final Color TEXTO = new Color(39, 49, 60);

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
     * Construye el documento a partir de los resultados contables calculados.
     * No consulta ni modifica información financiera.
     */
    public byte[] generar(
            PyGComparadoService.ResultadoPyG resultado,
            String codigoAgencia,
            String nombreAgencia,
            FirmasDTO firmas
    ) {
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado del PyG comparado es obligatorio."
            );
        }

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(
                    PageSize.A4.rotate(), 38, 38, 62, 48
            );

            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            escritor.setPageEvent(new PiePagina());
            documento.open();

            agregarEncabezado(
                    documento, resultado, codigoAgencia, nombreAgencia
            );
            agregarDetalle(documento, resultado);

            if (firmas != null) {
                agregarFirmas(documento, firmas);
            }

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException(
                    "No fue posible generar el PDF del PyG comparado.", e
            );
        }
    }

    /** Identifica la entidad, agencia y períodos objeto de comparación. */
    private void agregarEncabezado(
            Document documento,
            PyGComparadoService.ResultadoPyG resultado,
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
                "ESTADO DE EXCEDENTES O PERDIDAS - COMPARADO",
                SUBTITULO
        );
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(6);
        documento.add(titulo);

        Paragraph agencia = new Paragraph(
                "Agencia: " + texto(codigoAgencia)
                        + " - " + texto(nombreAgencia),
                NORMAL
        );
        agencia.setAlignment(Element.ALIGN_CENTER);
        agencia.setSpacingBefore(7);
        documento.add(agencia);

        Paragraph periodos = new Paragraph(
                "Período 1: "
                        + periodo(resultado.anoPeriodo1(), resultado.mesPeriodo1())
                        + "     |     Período 2: "
                        + periodo(resultado.anoPeriodo2(), resultado.mesPeriodo2()),
                NORMAL
        );
        periodos.setAlignment(Element.ALIGN_CENTER);
        periodos.setSpacingBefore(4);
        periodos.setSpacingAfter(16);
        documento.add(periodos);
    }

    /**
     * Presenta los renglones históricos y las cuatro columnas financieras.
     * Los resultados calculados reciben un tratamiento visual diferenciado.
     */
    private void agregarDetalle(
            Document documento,
            PyGComparadoService.ResultadoPyG resultado
    ) throws DocumentException {
        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{8, 36, 16, 16, 16, 8});
        tabla.setHeaderRows(1);
        tabla.setSpacingAfter(15);

        cabecera(tabla, "Código", Element.ALIGN_LEFT);
        cabecera(tabla, "Concepto", Element.ALIGN_LEFT);
        cabecera(
                tabla,
                periodo(resultado.anoPeriodo1(), resultado.mesPeriodo1()),
                Element.ALIGN_RIGHT
        );
        cabecera(
                tabla,
                periodo(resultado.anoPeriodo2(), resultado.mesPeriodo2()),
                Element.ALIGN_RIGHT
        );
        cabecera(tabla, "Variación", Element.ALIGN_RIGHT);
        cabecera(tabla, "Variación %", Element.ALIGN_RIGHT);

        if (resultado.filas() != null) {
            for (PyGComparadoService.FilaPyG fila : resultado.filas()) {
                boolean total = "TOTAL".equals(fila.tipo());
                boolean resultadoFinal = "RESULTADO".equals(fila.tipo());
                boolean destacada = total || resultadoFinal;

                Font fuente = destacada ? NEGRITA : NORMAL;
                Color fondo = resultadoFinal
                        ? FONDO_RESULTADO
                        : total ? FONDO : Color.WHITE;

                celda(
                        tabla, fila.codigo(), fuente,
                        Element.ALIGN_LEFT, fondo
                );
                celda(
                        tabla, fila.nombre(), fuente,
                        Element.ALIGN_LEFT, fondo
                );
                celda(
                        tabla, numero(fila.periodo1()), fuente,
                        Element.ALIGN_RIGHT, fondo
                );
                celda(
                        tabla, numero(fila.periodo2()), fuente,
                        Element.ALIGN_RIGHT, fondo
                );
                celda(
                        tabla, numero(fila.variacion()), fuente,
                        Element.ALIGN_RIGHT, fondo
                );
                celda(
                        tabla, porcentaje(fila.porcentaje()), fuente,
                        Element.ALIGN_RIGHT, fondo
                );
            }
        }

        documento.add(tabla);
    }

    /** Agrega únicamente los firmantes institucionales configurados. */
    private void agregarFirmas(
            Document documento,
            FirmasDTO firmas
    ) throws DocumentException {
        FirmanteDTO[] disponibles = {
                firmas.firmante1(),
                firmas.firmante2(),
                firmas.firmante3()
        };

        int cantidad = 0;
        for (FirmanteDTO firmante : disponibles) {
            if (firmante != null
                    && firmante.nombre() != null
                    && !firmante.nombre().isBlank()) {
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

        for (FirmanteDTO firmante : disponibles) {
            if (firmante == null
                    || firmante.nombre() == null
                    || firmante.nombre().isBlank()) {
                continue;
            }

            PdfPCell celda = new PdfPCell();
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(8);

            Paragraph linea = new Paragraph(
                    "____________________________", NORMAL
            );
            linea.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(linea);

            Paragraph nombre = new Paragraph(
                    firmante.nombre(), NEGRITA
            );
            nombre.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(nombre);

            Paragraph cargo = new Paragraph(
                    texto(firmante.titulo()), PEQUENA
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

    /** Construye una cabecera repetible cuando el informe ocupa varias páginas. */
    private void cabecera(
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
        celda.setPadding(7);
        tabla.addCell(celda);
    }

    /** Construye las celdas de detalle con formato institucional uniforme. */
    private void celda(
            PdfPTable tabla,
            String contenido,
            Font fuente,
            int alineacion,
            Color fondo
    ) {
        PdfPCell celda = new PdfPCell(
                new Phrase(texto(contenido), fuente)
        );
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(BORDE);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(6);
        tabla.addCell(celda);
    }

    /** Formatea importes monetarios con separadores colombianos. */
    private String numero(BigDecimal valor) {
        DecimalFormatSymbols simbolos =
                DecimalFormatSymbols.getInstance(COLOMBIA);
        DecimalFormat formato = new DecimalFormat(
                "#,##0.00;(#,##0.00)", simbolos
        );
        return formato.format(
                valor == null ? BigDecimal.ZERO : valor
        );
    }

    /** Formatea el porcentaje; evita representar como cero un valor indefinido. */
    private String porcentaje(BigDecimal valor) {
        if (valor == null) {
            return "N/D";
        }

        DecimalFormatSymbols simbolos =
                DecimalFormatSymbols.getInstance(COLOMBIA);
        DecimalFormat formato = new DecimalFormat(
                "#,##0.00;(#,##0.00)", simbolos
        );
        return formato.format(valor) + " %";
    }

    /** Identifica el mes y año de cada columna comparativa. */
    private String periodo(int ano, int mes) {
        return Month.of(mes)
                .getDisplayName(TextStyle.FULL, COLOMBIA)
                .toUpperCase(COLOMBIA)
                + " " + ano;
    }

    /** Normaliza textos opcionales para la presentación. */
    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    /** Agrega fecha de generación y paginación en todas las hojas. */
    private static class PiePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            String fecha = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );

            ColumnText.showTextAligned(
                    escritor.getDirectContent(),
                    Element.ALIGN_LEFT,
                    new Phrase("Generado: " + fecha, PEQUENA),
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
