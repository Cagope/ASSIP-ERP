package co.assip.erp.contabilidad.auxiliares_contables;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository JDBC para insertar en contabilidad.controles_auxiliares
 *
 * ✅ Regla:
 * Solo se inserta si catalogo_cuentas.control_entrada_salida = true.
 *
 * Tabla real:
 * - id_controles_auxiliares (PK)
 * - id_tipos_control (nullable)
 * - id_auxiliar_contable (nullable)
 * - numero_documento (varchar 20)
 * - auditoría
 */
@Repository
public class ControlesAuxiliaresRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ControlesAuxiliaresRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * ✅ Inserta el documento soporte asociado al auxiliar contable.
     *
     * @param idAuxiliarContable id del movimiento auxiliar recién creado
     * @param numeroDocumento   soporte (cheque / transferencia / ref) máx 20
     * @param idTiposControl    opcional (si manejan catálogo de tipos control)
     * @param idUsuario         usuario autenticado
     */
    public void insertarDocumentoSoporte(
            Integer idAuxiliarContable,
            String numeroDocumento,
            Integer idTiposControl,
            Integer idUsuario
    ) {

        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException("El número de documento soporte es obligatorio.");
        }

        // PostgreSQL: numero_documento es varchar(20)
        if (numeroDocumento.length() > 20) {
            throw new IllegalArgumentException("El número de documento soporte no puede superar 20 caracteres.");
        }

        String sql = """
            INSERT INTO contabilidad.controles_auxiliares (
                id_tipos_control,
                id_auxiliar_contable,
                numero_documento,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            ) VALUES (
                :idTiposControl,
                :idAuxiliarContable,
                :numeroDocumento,
                :usuario,
                :usuario
            )
        """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idTiposControl", idTiposControl) // puede ir null
                .addValue("idAuxiliarContable", idAuxiliarContable)
                .addValue("numeroDocumento", numeroDocumento)
                .addValue("usuario", idUsuario)
        );
    }

    /**
     * ✅ Versión simple cuando NO manejas id_tipos_control
     */
    public void insertarDocumentoSoporte(
            Integer idAuxiliarContable,
            String numeroDocumento,
            Integer idUsuario
    ) {
        insertarDocumentoSoporte(idAuxiliarContable, numeroDocumento, null, idUsuario);
    }
}
