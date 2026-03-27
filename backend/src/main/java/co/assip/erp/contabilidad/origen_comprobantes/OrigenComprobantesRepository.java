package co.assip.erp.contabilidad.origen_comprobantes;

import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository JDBC para insertar en contabilidad.origen_comprobantes
 * (trazabilidad del origen del comprobante).
 */
@Repository
public class OrigenComprobantesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OrigenComprobantesRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * ✅ Inserta el origen del comprobante.
     * Si ya existe (misma agencia + tipo + número), actualiza datos de origen.
     *
     * Requiere UNIQUE (id_agencia, tipo_comprobante, numero_comprobante)
     */
    public void insertarOrigen(
            OrigenComprobanteDTO dto,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO contabilidad.origen_comprobantes (
                id_agencia,
                tipo_comprobante,
                numero_comprobante,
                origen_tipo,
                modulo_origen,
                proceso_origen,
                tabla_origen,
                id_origen,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            ) VALUES (
                :idAgencia,
                :tipoComprobante,
                :numeroComprobante,
                :origenTipo,
                :moduloOrigen,
                :procesoOrigen,
                :tablaOrigen,
                :idOrigen,
                :usuario,
                :usuario
            )
            ON CONFLICT (id_agencia, tipo_comprobante, numero_comprobante)
            DO UPDATE SET
                origen_tipo = EXCLUDED.origen_tipo,
                modulo_origen = EXCLUDED.modulo_origen,
                proceso_origen = EXCLUDED.proceso_origen,
                tabla_origen = EXCLUDED.tabla_origen,
                id_origen = EXCLUDED.id_origen,
                fk_seguridad_edicion = EXCLUDED.fk_seguridad_edicion
            """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idAgencia", dto.getIdAgencia())
                .addValue("tipoComprobante", dto.getTipoComprobante())
                .addValue("numeroComprobante", dto.getNumeroComprobante())
                .addValue("origenTipo", dto.getOrigenTipo())
                .addValue("moduloOrigen", dto.getModuloOrigen())
                .addValue("procesoOrigen", dto.getProcesoOrigen())
                .addValue("tablaOrigen", dto.getTablaOrigen())
                .addValue("idOrigen", dto.getIdOrigen())
                .addValue("usuario", idUsuario)
        );
    }

    // =========================================================
    // VALIDAR SI UN PROCESO YA GENERÓ COMPROBANTE
    // (IDEMPOTENCIA ERP)
    // =========================================================
    public boolean existeProcesoOrigen(
            String modulo,
            String proceso,
            String tabla,
            Long idOrigen
    ) {

        String sql = """
        SELECT COUNT(1)
        FROM contabilidad.origen_comprobantes
        WHERE modulo_origen = :modulo
          AND proceso_origen = :proceso
          AND tabla_origen = :tabla
          AND id_origen = :idOrigen
        """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("modulo", modulo)
                        .addValue("proceso", proceso)
                        .addValue("tabla", tabla)
                        .addValue("idOrigen", idOrigen),
                Integer.class
        );

        return count != null && count > 0;
    }
}