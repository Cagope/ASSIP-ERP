package co.assip.erp.contabilidad.informes.especiales.resumen_diario_detallado;

import co.assip.erp.contabilidad.informes.especiales.resumen_diario_detallado.dto.ResumenDiarioDetalladoFilaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Consulta los movimientos individuales del Resumen diario detallado.
 * Utiliza auxiliares contables activos y conserva la información de trazabilidad.
 */
@Repository
@RequiredArgsConstructor
public class ResumenDiarioDetalladoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Recupera todos los movimientos activos de una agencia y período.
     * Conserva una fila por auxiliar, sin agrupar ni modificar valores contables.
     */
    public List<ResumenDiarioDetalladoFilaDTO> consultar(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        String sql = """
                SELECT
                    ac.id_auxiliar_contable,
                    ac.fecha_auxiliar AS fecha,
                    TRIM(ac.tipo_comprobante) AS tipo_comprobante,
                    TRIM(ac.numero_comprobante) AS numero_comprobante,
                    cc.codigo_cuenta,
                    cc.nombre_cuenta,
                    dp.documento AS documento_tercero,
                    CASE
                        WHEN dp.tipo_persona = '2' THEN
                            NULLIF(TRIM(dp.nombres), '')
                        ELSE
                            NULLIF(
                                TRIM(
                                    CONCAT_WS(
                                        ' ',
                                        NULLIF(TRIM(dp.nombres), ''),
                                        NULLIF(TRIM(dp.primer_apellido), ''),
                                        NULLIF(TRIM(dp.segundo_apellido), '')
                                    )
                                ),
                                ''
                            )
                    END AS nombre_tercero,
                    ac.detalle_movimiento,
                    COALESCE(ac.valor_debito, 0) AS valor_debito,
                    COALESCE(ac.valor_credito, 0) AS valor_credito,
                    ac.origen_modulo,
                    ac.documento_origen,
                    ac.documento_soporte
                FROM contabilidad.auxiliares_contables ac
                INNER JOIN contabilidad.catalogo_cuentas cc
                    ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                   AND cc.id_agencia = ac.id_agencia
                LEFT JOIN hoja_vida.datos_personales dp
                    ON dp.id_datos_personal = ac.id_datos_personal
                WHERE ac.id_agencia = :idAgencia
                  AND ac.fecha_auxiliar BETWEEN :fechaInicial AND :fechaFinal
                  AND ac.estado_movimiento = 'A'
                ORDER BY
                    ac.fecha_auxiliar,
                    ac.id_auxiliar_contable
                """;

        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("fechaInicial", fechaInicial)
                .addValue("fechaFinal", fechaFinal);

        return jdbc.query(sql, parametros, (rs, rowNum) ->
                new ResumenDiarioDetalladoFilaDTO(
                        rs.getObject("id_auxiliar_contable", Integer.class),
                        rs.getObject("fecha", LocalDate.class),
                        rs.getString("tipo_comprobante"),
                        rs.getString("numero_comprobante"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta"),
                        rs.getString("documento_tercero"),
                        rs.getString("nombre_tercero"),
                        rs.getString("detalle_movimiento"),
                        rs.getBigDecimal("valor_debito"),
                        rs.getBigDecimal("valor_credito"),
                        rs.getString("origen_modulo"),
                        rs.getString("documento_origen"),
                        rs.getString("documento_soporte")
                )
        );
    }
}