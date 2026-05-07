package co.assip.erp.contabilidad.consecutivos_comprobantes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConsecutivosComprobantesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ======================================================
    // OBTENER CSC ACTUAL
    // ======================================================

    public Integer obtenerCscActual(
            String tipoComprobante,
            Integer idAgencia
    ) {

        String sql = """
            SELECT COALESCE(csc_comprobante, 0)
            FROM contabilidad.tipos_comprobantes
            WHERE tipo_comprobante = :tipoComprobante
              AND id_agencia = :idAgencia
              AND comprobante_activo = true
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("tipoComprobante", tipoComprobante)
                        .addValue("idAgencia", idAgencia),
                Integer.class
        );
    }

    // ======================================================
    // OBTENER CSC CON LOCK
    // ======================================================

    public Integer obtenerCscActualConLock(
            String tipoComprobante,
            Integer idAgencia
    ) {

        String sql = """
            SELECT COALESCE(csc_comprobante, 0)
            FROM contabilidad.tipos_comprobantes
            WHERE tipo_comprobante = :tipoComprobante
              AND id_agencia = :idAgencia
              AND comprobante_activo = true
            FOR UPDATE
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("tipoComprobante", tipoComprobante)
                        .addValue("idAgencia", idAgencia),
                Integer.class
        );
    }

    // ======================================================
    // ACTUALIZAR CSC
    // ======================================================

    public void actualizarCsc(
            String tipoComprobante,
            Integer idAgencia,
            Integer nuevoCsc,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE contabilidad.tipos_comprobantes
               SET csc_comprobante = :nuevoCsc,
                   fk_seguridad_edicion = :idUsuario,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE tipo_comprobante = :tipoComprobante
               AND id_agencia = :idAgencia
        """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("nuevoCsc", nuevoCsc)
                        .addValue("idUsuario", idUsuario)
                        .addValue("tipoComprobante", tipoComprobante)
                        .addValue("idAgencia", idAgencia)
        );
    }
}