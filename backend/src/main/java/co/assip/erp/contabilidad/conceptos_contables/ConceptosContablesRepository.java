
package co.assip.erp.contabilidad.conceptos_contables;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JDBC de cabeceras lógicas de comprobantes contables.
 * Separa la inserción estricta de la operación compatible con procesos existentes.
 */
@Repository
public class ConceptosContablesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ConceptosContablesRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Inserta o actualiza el concepto de un comprobante existente.
     * Conserva el comportamiento utilizado por los módulos actuales.
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
                fk_seguridad_edicion = EXCLUDED.fk_seguridad_edicion,
                fecha_edicion = CURRENT_TIMESTAMP
            RETURNING id_conceptos_contables
            """;

        Integer id = jdbc.queryForObject(
                sql,
                parametros(
                        idAgencia,
                        tipoComprobante,
                        numeroComprobante,
                        conceptoComprobante,
                        idUsuario
                ),
                Integer.class
        );

        if (id == null) {
            throw new IllegalStateException(
                    "No se pudo registrar el concepto contable."
            );
        }

        return id;
    }

    /**
     * Inserta exclusivamente una cabecera nueva.
     * Una identidad repetida genera un error de unicidad y no modifica datos.
     */
    public Integer insertarConceptoNuevo(
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
            RETURNING id_conceptos_contables
            """;

        Integer id = jdbc.queryForObject(
                sql,
                parametros(
                        idAgencia,
                        tipoComprobante,
                        numeroComprobante,
                        conceptoComprobante,
                        idUsuario
                ),
                Integer.class
        );

        if (id == null) {
            throw new IllegalStateException(
                    "No se pudo crear la cabecera del comprobante."
            );
        }

        return id;
    }

    /**
     * Construye los parámetros comunes de ambas inserciones.
     * Centraliza el mapeo sin modificar los valores recibidos.
     */
    private MapSqlParameterSource parametros(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String conceptoComprobante,
            Integer idUsuario
    ) {
        return new MapSqlParameterSource()
                .addValue("tipoComprobante", tipoComprobante)
                .addValue("numeroComprobante", numeroComprobante)
                .addValue("concepto", conceptoComprobante)
                .addValue("usuario", idUsuario)
                .addValue("idAgencia", idAgencia);
    }
}
