package co.assip.erp.contabilidad.registro.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository JDBC para insertar en contabilidad.conceptos_contables
 * (cabecera lógica del comprobante).
 */
@Repository
public class ConceptosContablesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ConceptosContablesRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * ✅ Inserta el concepto contable del comprobante (1 por comprobante).
     * Si ya existe (misma agencia + tipo + número), retorna el id existente.
     */
    public Integer insertarConcepto(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String conceptoComprobante,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO contabilidad.conceptos_contables (
                tipo_comprobante,
                numero_comprobante,
                concepto_comprobante,
                fk_seguridad_creacion,
                fk_seguridad_edicion,
                id_agencia
            ) VALUES (
                :tipoComprobante,
                :numeroComprobante,
                :concepto,
                :usuario,
                :usuario,
                :idAgencia
            )
            ON CONFLICT (id_agencia, tipo_comprobante, numero_comprobante)
            DO UPDATE SET
                concepto_comprobante = EXCLUDED.concepto_comprobante,
                fk_seguridad_edicion = EXCLUDED.fk_seguridad_edicion
            RETURNING id_conceptos_contables
        """;

        Integer id = jdbc.queryForObject(sql,
                new MapSqlParameterSource()
                        .addValue("tipoComprobante", tipoComprobante)
                        .addValue("numeroComprobante", numeroComprobante)
                        .addValue("concepto", conceptoComprobante)
                        .addValue("usuario", idUsuario)
                        .addValue("idAgencia", idAgencia),
                Integer.class
        );

        if (id == null) {
            throw new IllegalStateException("No se pudo insertar/actualizar el concepto contable.");
        }

        return id;
    }
}
