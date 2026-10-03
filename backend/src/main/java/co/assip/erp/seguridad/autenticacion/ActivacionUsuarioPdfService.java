
package co.assip.erp.seguridad.autenticacion;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera el PDF cifrado de activación de usuarios.
 *
 * El código temporal se incorpora exclusivamente
 * al documento protegido.
 *
 * No guarda archivos, códigos ni contraseñas.
 */
@Service
public class ActivacionUsuarioPdfService {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static final Color PRIMARIO =
            new Color(31, 78, 121);

    private static final Color SECUNDARIO =
            new Color(71, 85, 105);

    private static final Color BORDE =
            new Color(203, 213, 225);

    private static final Font EMPRESA =
            new Font(
                    Font.HELVETICA,
                    14,
                    Font.BOLD,
                    PRIMARIO
            );

    private static final Font TITULO =
            new Font(
                    Font.HELVETICA,
                    13,
                    Font.BOLD,
                    PRIMARIO
            );

    private static final Font SUBTITULO =
            new Font(
                    Font.HELVETICA,
                    10,
                    Font.BOLD,
                    PRIMARIO
            );

    private static final Font TEXTO =
            new Font(
                    Font.HELVETICA,
                    9,
                    Font.NORMAL,
                    Color.BLACK
            );

    private static final Font ETIQUETA =
            new Font(
                    Font.HELVETICA,
                    9,
                    Font.BOLD,
                    SECUNDARIO
            );

    private static final Font NOTA =
            new Font(
                    Font.HELVETICA,
                    8,
                    Font.NORMAL,
                    SECUNDARIO
            );

    private static final Font CODIGO =
            new Font(
                    Font.COURIER,
                    22,
                    Font.BOLD,
                    PRIMARIO
            );

    /**
     * Información institucional necesaria
     * para el encabezado del documento.
     */
    public record DatosEmpresa(
            String nombre,
            String sigla,
            String nit,
            String direccion
    ) {
    }

    /**
     * Información administrativa del usuario.
     *
     * No contiene contraseñas ni códigos.
     */
    public record DatosUsuario(
            String username,
            String nombreCompleto,
            String correo,
            List<String> agencias,
            List<String> roles,
            LocalDateTime inicioVigencia,
            LocalDateTime finVigencia
    ) {
    }

    /**
     * Genera un PDF cifrado en memoria.
     *
     * La contraseña de apertura corresponde
     * al documento de identidad del titular.
     *
     * @param empresa datos institucionales
     * @param usuario datos del usuario
     * @param codigoActivacion código de seis dígitos
     * @param documentoIdentidad clave de apertura
     * @param fechaExpiracion vencimiento del código
     * @return PDF cifrado
     */
    public byte[] generar(
            DatosEmpresa empresa,
            DatosUsuario usuario,
            String codigoActivacion,
            String documentoIdentidad,
            LocalDateTime fechaExpiracion
    ) {

        validar(
                empresa,
                usuario,
                codigoActivacion,
                documentoIdentidad,
                fechaExpiracion
        );

        try (ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {

            Document documento =
                    new Document(
                            PageSize.LETTER,
                            35,
                            35,
                            30,
                            30
                    );

            PdfWriter writer =
                    PdfWriter.getInstance(
                            documento,
                            salida
                    );

            byte[] claveUsuario =
                    documentoIdentidad
                            .trim()
                            .getBytes(StandardCharsets.UTF_8);

            byte[] clavePropietario =
                    new byte[32];

            RANDOM.nextBytes(clavePropietario);

            /*
             * El PDF requiere contraseña
             * para abrirse.
             *
             * No se trata de restricciones
             * superficiales de impresión.
             */
            writer.setEncryption(
                    claveUsuario,
                    clavePropietario,
                    0,
                    PdfWriter.ENCRYPTION_AES_128
            );

            documento.open();

            agregarEncabezado(
                    documento,
                    empresa
            );

            agregarTitulo(documento);

            agregarDatosUsuario(
                    documento,
                    usuario
            );

            agregarCodigo(
                    documento,
                    codigoActivacion,
                    fechaExpiracion
            );

            agregarInstrucciones(documento);

            agregarAdvertencia(documento);

            agregarPie(documento);

            documento.close();

            return salida.toByteArray();

        } catch (Exception error) {

            throw new IllegalStateException(
                    "No fue posible generar el PDF "
                            + "de activación del usuario.",
                    error
            );
        }
    }

    /**
     * Valida la información antes de crear
     * el documento cifrado.
     */
    private void validar(
            DatosEmpresa empresa,
            DatosUsuario usuario,
            String codigo,
            String documento,
            LocalDateTime expiracion
    ) {

        if (empresa == null) {
            throw new IllegalArgumentException(
                    "Los datos institucionales son obligatorios."
            );
        }

        if (usuario == null
                || vacio(usuario.username())) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        if (codigo == null
                || !codigo.matches("\\d{6}")) {

            throw new IllegalArgumentException(
                    "El código debe contener seis dígitos."
            );
        }

        if (vacio(documento)) {
            throw new IllegalArgumentException(
                    "El documento de identidad es obligatorio."
            );
        }

        if (expiracion == null
                || !expiracion.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "El código de activación está vencido."
            );
        }
    }

    /**
     * Encabezado institucional basado en
     * el formato del sistema anterior.
     */
    private void agregarEncabezado(
            Document documento,
            DatosEmpresa empresa
    ) throws DocumentException {

        Paragraph nombre =
                new Paragraph(
                        texto(empresa.nombre()),
                        EMPRESA
                );

        nombre.setAlignment(Element.ALIGN_CENTER);
        nombre.setSpacingAfter(4);

        documento.add(nombre);

        Paragraph sigla =
                new Paragraph(
                        texto(empresa.sigla()),
                        SUBTITULO
                );

        sigla.setAlignment(Element.ALIGN_CENTER);

        documento.add(sigla);

        Paragraph nit =
                new Paragraph(
                        texto(empresa.nit()),
                        NOTA
                );

        nit.setAlignment(Element.ALIGN_CENTER);

        documento.add(nit);

        Paragraph direccion =
                new Paragraph(
                        texto(empresa.direccion()),
                        NOTA
                );

        direccion.setAlignment(Element.ALIGN_CENTER);
        direccion.setSpacingAfter(8);

        documento.add(direccion);

        PdfPTable linea =
                new PdfPTable(1);

        linea.setWidthPercentage(100);

        PdfPCell celda =
                new PdfPCell(
                        new Paragraph(" ")
                );

        celda.setBorder(PdfPCell.BOTTOM);
        celda.setBorderColor(PRIMARIO);
        celda.setBorderWidthBottom(1.5f);
        celda.setFixedHeight(4);

        linea.addCell(celda);

        documento.add(linea);
    }

    private void agregarTitulo(
            Document documento
    ) throws DocumentException {

        Paragraph titulo =
                new Paragraph(
                        "GESTIÓN DE CONTRASEÑA DE USUARIO",
                        TITULO
                );

        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(10);
        titulo.setSpacingAfter(3);

        documento.add(titulo);

        Paragraph descripcion =
                new Paragraph(
                        "Documento confidencial "
                                + "- Uso exclusivo del titular",
                        NOTA
                );

        descripcion.setAlignment(Element.ALIGN_CENTER);
        descripcion.setSpacingAfter(10);

        documento.add(descripcion);
    }

    /**
     * Presenta la información administrativa
     * del usuario sin incluir credenciales.
     */
    private void agregarDatosUsuario(
            Document documento,
            DatosUsuario usuario
    ) throws DocumentException {

        agregarSeccion(
                documento,
                "INFORMACIÓN DEL USUARIO"
        );

        PdfPTable tabla =
                new PdfPTable(
                        new float[]{2.0f, 5.0f}
                );

        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(8);

        agregarFila(
                tabla,
                "Usuario",
                usuario.username()
        );

        agregarFila(
                tabla,
                "Nombre completo",
                usuario.nombreCompleto()
        );

        agregarFila(
                tabla,
                "Correo electrónico",
                usuario.correo()
        );

        agregarFila(
                tabla,
                "Agencias",
                unir(usuario.agencias())
        );

        agregarFila(
                tabla,
                "Roles asignados",
                unir(usuario.roles())
        );

        agregarFila(
                tabla,
                "Inicio de vigencia",
                fecha(usuario.inicioVigencia())
        );

        agregarFila(
                tabla,
                "Fin de vigencia",
                fecha(usuario.finVigencia())
        );

        documento.add(tabla);
    }

    /**
     * Muestra el código temporal únicamente
     * dentro del documento cifrado.
     */
    private void agregarCodigo(
            Document documento,
            String codigo,
            LocalDateTime expiracion
    ) throws DocumentException {

        agregarSeccion(
                documento,
                "CÓDIGO PERSONAL DE SEGURIDAD"
        );

        PdfPTable tabla =
                new PdfPTable(1);

        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(4);
        tabla.setSpacingAfter(7);

        Paragraph valor =
                new Paragraph(
                        codigo,
                        CODIGO
                );

        valor.setAlignment(Element.ALIGN_CENTER);

        PdfPCell celda =
                new PdfPCell(valor);

        celda.setPadding(9);
        celda.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        celda.setBorderColor(BORDE);
        celda.setBackgroundColor(
                new Color(241, 245, 249)
        );

        tabla.addCell(celda);

        documento.add(tabla);

        Paragraph vencimiento =
                new Paragraph(
                        "Válido hasta: "
                                + expiracion.format(FECHA_HORA),
                        TEXTO
                );

        vencimiento.setAlignment(
                Element.ALIGN_CENTER
        );

        vencimiento.setSpacingAfter(8);

        documento.add(vencimiento);
    }

    private void agregarInstrucciones(
            Document documento
    ) throws DocumentException {

        agregarSeccion(
                documento,
                "INSTRUCCIONES PARA ESTABLECER LA CONTRASEÑA"
        );

        agregarPaso(
                documento,
                "1. Ingrese a ASSIP ERP."
        );

        agregarPaso(
                documento,
                "2. Digite su nombre de usuario."
        );

        agregarPaso(
                documento,
                "3. Cuando el sistema lo solicite, ingrese el código contenido en este documento."
        );

        agregarPaso(
                documento,
                "4. Establezca y confirme "
                        + "su contraseña personal."
        );

        agregarPaso(
                documento,
                "5. Regrese al inicio de sesión "
                        + "e ingrese con su nueva contraseña."
        );
    }

    private void agregarAdvertencia(
            Document documento
    ) throws DocumentException {

        Paragraph advertencia =
                new Paragraph(
                        "Este documento contiene un código temporal y personal de seguridad.\n\n"
                                + "No comparta el código con terceros. "
                                + "La contraseña definitiva del ERP "
                                + "debe ser establecida exclusivamente "
                                + "por el propietario de la cuenta.",
                        NOTA
                );

        advertencia.setSpacingBefore(8);
        advertencia.setSpacingAfter(10);

        documento.add(advertencia);
    }

    /**
     * Pie institucional inspirado en
     * el formato anterior de VB.
     */
    private void agregarPie(
            Document documento
    ) throws DocumentException {

        PdfPTable tabla =
                new PdfPTable(1);

        tabla.setWidthPercentage(45);
        tabla.setHorizontalAlignment(
                Element.ALIGN_LEFT
        );

        PdfPCell celda =
                new PdfPCell(
                        new Paragraph(
                                "Departamento de Sistemas",
                                TEXTO
                        )
                );

        celda.setBorder(PdfPCell.TOP);
        celda.setBorderColor(PRIMARIO);
        celda.setPaddingTop(5);

        tabla.addCell(celda);

        documento.add(tabla);
    }

    private void agregarSeccion(
            Document documento,
            String titulo
    ) throws DocumentException {

        Paragraph parrafo =
                new Paragraph(
                        titulo,
                        SUBTITULO
                );

        parrafo.setSpacingBefore(5);
        parrafo.setSpacingAfter(5);

        documento.add(parrafo);
    }

    private void agregarPaso(
            Document documento,
            String contenido
    ) throws DocumentException {

        Paragraph paso =
                new Paragraph(
                        contenido,
                        TEXTO
                );

        paso.setSpacingAfter(2);

        documento.add(paso);
    }

    private void agregarFila(
            PdfPTable tabla,
            String etiqueta,
            String valor
    ) {

        PdfPCell izquierda =
                new PdfPCell(
                        new Paragraph(
                                etiqueta,
                                ETIQUETA
                        )
                );

        izquierda.setPadding(5);
        izquierda.setBorderColor(BORDE);
        izquierda.setBackgroundColor(
                new Color(248, 250, 252)
        );

        PdfPCell derecha =
                new PdfPCell(
                        new Paragraph(
                                texto(valor),
                                TEXTO
                        )
                );

        derecha.setPadding(5);
        derecha.setBorderColor(BORDE);

        tabla.addCell(izquierda);
        tabla.addCell(derecha);
    }

    private String unir(
            List<String> valores
    ) {

        if (valores == null || valores.isEmpty()) {
            return "No registrado";
        }

        return valores.stream()
                .filter(valor ->
                        valor != null
                                && !valor.isBlank()
                )
                .distinct()
                .reduce(
                        (a, b) -> a + ", " + b
                )
                .orElse("No registrado");
    }

    private String fecha(
            LocalDateTime valor
    ) {

        return valor == null
                ? "No definida"
                : valor.format(FECHA);
    }

    private String texto(
            String valor
    ) {

        return vacio(valor)
                ? "No registrado"
                : valor.trim();
    }

    private boolean vacio(
            String valor
    ) {

        return valor == null
                || valor.isBlank();
    }
}
