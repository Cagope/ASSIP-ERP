
package co.assip.erp.contabilidad.auxiliares_contables;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de controles documentales de auxiliares contables.
 *
 * Conserva los métodos existentes y permite registrar
 * controles tipificados para comprobantes manuales.
 */
@Repository
public class ControlesAuxiliaresRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ControlesAuxiliaresRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    /**
     * Registra un documento soporte mediante el contrato
     * existente, conservando la compatibilidad con otros módulos.
     */
    public void insertarDocumentoSoporte(
            Integer idAuxiliarContable,
            String numeroDocumento,
            Integer idTiposControl,
            Integer idUsuario
    ) {

        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException(
                    "El número de documento soporte es obligatorio."
            );
        }

        if (numeroDocumento.length() > 20) {
            throw new IllegalArgumentException(
                    "El documento soporte supera 20 caracteres."
            );
        }

        String sql = """
                INSERT INTO contabilidad.controles_auxiliares
                (
                    id_tipos_control,
                    id_auxiliar_contable,
                    numero_documento,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :idTiposControl,
                    :idAuxiliarContable,
                    :numeroDocumento,
                    :usuario,
                    :usuario
                )
                """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idTiposControl", idTiposControl)
                        .addValue("idAuxiliarContable", idAuxiliarContable)
                        .addValue("numeroDocumento", numeroDocumento)
                        .addValue("usuario", idUsuario)
        );
    }

    /**
     * Mantiene la firma utilizada por los módulos existentes.
     * Registra el documento sin especificar tipo de control.
     */
    public void insertarDocumentoSoporte(
            Integer idAuxiliarContable,
            String numeroDocumento,
            Integer idUsuario
    ) {
        insertarDocumentoSoporte(
                idAuxiliarContable,
                numeroDocumento,
                null,
                idUsuario
        );
    }

    /**
     * Registra el control documental de un comprobante manual.
     *
     * Consulta la parametrización del tipo de control.
     * Exige documento únicamente cuando pide_documento=true.
     * Valida los datos antes de insertar el registro.
     */
    public void insertarControlComprobanteManual(
            Integer idAuxiliarContable,
            Integer idTiposControl,
            String numeroDocumento,
            Integer idUsuario
    ) {

        if (idAuxiliarContable == null
                || idAuxiliarContable <= 0) {
            throw new IllegalArgumentException(
                    "El auxiliar contable es obligatorio."
            );
        }

        if (idTiposControl == null || idTiposControl <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de control válido."
            );
        }

        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException(
                    "El usuario responsable es obligatorio."
            );
        }

        String sqlTipo = """
                SELECT pide_documento
                FROM contabilidad.tipos_control_ent_sal
                WHERE id_tipos_control = :idTiposControl
                """;

        Boolean pideDocumento;

        try {
            pideDocumento = jdbc.queryForObject(
                    sqlTipo,
                    new MapSqlParameterSource()
                            .addValue("idTiposControl", idTiposControl),
                    Boolean.class
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException(
                    "El tipo de control seleccionado no existe.",
                    ex
            );
        }

        if (pideDocumento == null) {
            throw new IllegalStateException(
                    "El tipo de control no tiene definida "
                            + "la obligatoriedad del documento."
            );
        }

        String documento = numeroDocumento == null
                ? null
                : numeroDocumento.trim();

        if (documento != null && documento.isBlank()) {
            documento = null;
        }

        if (Boolean.TRUE.equals(pideDocumento)
                && documento == null) {
            throw new IllegalArgumentException(
                    "El tipo de control seleccionado "
                            + "requiere documento soporte."
            );
        }

        if (documento != null && documento.length() > 20) {
            throw new IllegalArgumentException(
                    "El documento soporte supera 20 caracteres."
            );
        }

        String sqlInsertar = """
                INSERT INTO contabilidad.controles_auxiliares
                (
                    id_tipos_control,
                    id_auxiliar_contable,
                    numero_documento,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :idTiposControl,
                    :idAuxiliarContable,
                    :numeroDocumento,
                    :usuario,
                    :usuario
                )
                """;

        jdbc.update(
                sqlInsertar,
                new MapSqlParameterSource()
                        .addValue("idTiposControl", idTiposControl)
                        .addValue("idAuxiliarContable", idAuxiliarContable)
                        .addValue("numeroDocumento", documento)
                        .addValue("usuario", idUsuario)
        );
    }
}
