
package co.assip.erp.contabilidad.comprobantes.crear;

import co.assip.erp.contabilidad.comprobantes.crear.dto.ComprobanteCrearRequestDTO;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador para la creación de comprobantes contables manuales.
 *
 * Expone el registro de comprobantes y la consulta
 * del catálogo de tipos de control documental.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/comprobantes")
public class ComprobantesCrearController {

    private final ComprobantesCrearService service;

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Consulta los tipos de control de entrada y salida
     * configurados en Contabilidad.
     *
     * La obligatoriedad del documento se determina
     * mediante la parametrización de PostgreSQL.
     */
    @GetMapping("/tipos-control")
    public List<TipoControlResponse> listarTiposControl() {

        String sql = """
            SELECT
                id_tipos_control,
                descripcion,
                COALESCE(pide_documento, FALSE) AS pide_documento
            FROM contabilidad.tipos_control_ent_sal
            ORDER BY id_tipos_control
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> new TipoControlResponse(
                        rs.getInt("id_tipos_control"),
                        rs.getString("descripcion"),
                        rs.getBoolean("pide_documento")
                )
        );
    }

    /**
     * Crea un comprobante contable manual.
     *
     * La validación de permisos, agencia, período y movimientos
     * se realiza en el servicio.
     *
     * @return comprobante creado con su consecutivo definitivo.
     */
    @PostMapping
    public ResponseEntity<ComprobanteCreadoResponse> crear(
            @RequestBody ComprobanteCrearRequestDTO solicitud,
            HttpServletRequest request
    ) {

        String numeroComprobante = service.crear(
                solicitud,
                request
        );

        ComprobanteCreadoResponse respuesta =
                new ComprobanteCreadoResponse(
                        solicitud.idAgencia(),
                        solicitud.tipoComprobante().trim().toUpperCase(),
                        numeroComprobante,
                        "Comprobante creado correctamente."
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    /**
     * Datos públicos de un tipo de control documental.
     */
    public record TipoControlResponse(
            Integer idTiposControl,
            String nombreTipoControl,
            Boolean pideDocumento
    ) {
    }

    /**
     * Respuesta de la creación de un comprobante contable.
     */
    public record ComprobanteCreadoResponse(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String mensaje
    ) {
    }
}
