package co.assip.erp.depositos.informes.inconsistencias;

import co.assip.erp.depositos.informes.inconsistencias.dto.InconsistenciaSaldoDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InconsistenciasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public InconsistenciasRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<InconsistenciaSaldoDTO> consultar(
            String agencia,
            String fechaCorte
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY
                    id_datos_personal
            ),

            mov AS (
                SELECT
                    e.id_cuenta_ahorro,
                    SUM(COALESCE(e.valor_debito, 0)) AS total_debitos,
                    SUM(COALESCE(e.valor_credito, 0)) AS total_creditos
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.fecha_movimiento <= CAST(:fecha AS DATE)
                GROUP BY e.id_cuenta_ahorro
            )

            SELECT
                c.codigo_cuenta,

                LPAD(a.codigo_agencia::text, 2, '0') AS codigo_agencia,
                a.nombre_agencia,

                LPAD(f.codigo_forma::text, 2, '0') AS codigo_forma,

                hv.documento,

                CASE
                    WHEN hv.tipo_persona = '2'
                        THEN hv.nombres
                    ELSE TRIM(
                        CONCAT(
                            COALESCE(hv.primer_apellido, ''),
                            ' ',
                            COALESCE(hv.segundo_apellido, ''),
                            ' ',
                            COALESCE(hv.nombres, '')
                        )
                    )
                END AS nombre_completo,

                COALESCE(c.saldo_actual_cuenta, 0) AS saldo_tabla,

                (
                    COALESCE(m.total_creditos, 0)
                    -
                    COALESCE(m.total_debitos, 0)
                ) AS saldo_mov,

                (
                    COALESCE(c.saldo_actual_cuenta, 0)
                    -
                    (
                        COALESCE(m.total_creditos, 0)
                        -
                        COALESCE(m.total_debitos, 0)
                    )
                ) AS diferencia

            FROM depositos.cuentas_ahorro c

            JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            LEFT JOIN hv_unica hv
                ON hv.id_datos_personal = c.id_datos_personal

            LEFT JOIN mov m
                ON m.id_cuenta_ahorro = c.id_cuenta_ahorro

            WHERE
                (:ag = '0' OR c.id_agencia = CAST(:ag AS INTEGER))

                AND COALESCE(c.saldo_actual_cuenta, 0) <>
                    (
                        COALESCE(m.total_creditos, 0)
                        -
                        COALESCE(m.total_debitos, 0)
                    )

            ORDER BY
                c.id_agencia,
                c.codigo_cuenta
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("ag", agencia == null || agencia.isBlank() ? "0" : agencia);
        params.put("fecha", fechaCorte);

        return jdbc.query(sql, params, (rs, i) -> {
            InconsistenciaSaldoDTO dto = new InconsistenciaSaldoDTO();

            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setCodigoAgencia(rs.getString("codigo_agencia"));
            dto.setNombreAgencia(rs.getString("nombre_agencia"));
            dto.setCodigoForma(rs.getString("codigo_forma"));

            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre_completo"));

            BigDecimal saldoTabla = rs.getBigDecimal("saldo_tabla");
            BigDecimal saldoMov = rs.getBigDecimal("saldo_mov");

            if (saldoTabla == null) {
                saldoTabla = BigDecimal.ZERO;
            }

            if (saldoMov == null) {
                saldoMov = BigDecimal.ZERO;
            }

            dto.setSaldoTabla(saldoTabla);
            dto.setSaldoMov(saldoMov);
            dto.setDiferencia(saldoTabla.subtract(saldoMov));

            dto.setNegativo(saldoMov.compareTo(BigDecimal.ZERO) < 0);

            return dto;
        });
    }
}