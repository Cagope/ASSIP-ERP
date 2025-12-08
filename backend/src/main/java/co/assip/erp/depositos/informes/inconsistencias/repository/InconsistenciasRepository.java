package co.assip.erp.depositos.informes.inconsistencias.repository;

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

    public List<InconsistenciaSaldoDTO> consultar(String agencia, String fechaCorte) {

        String sql = """
            WITH mov AS (
                SELECT 
                    e.id_cuenta_ahorro,
                    SUM(e.valor_debito) AS total_debitos,
                    SUM(e.valor_credito) AS total_credits
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.fecha_movimiento <= CAST(:fecha AS DATE)
                GROUP BY e.id_cuenta_ahorro
            )
            SELECT
                c.codigo_cuenta,
                LPAD(c.codigo_agencia::text, 2, '0') AS codigo_agencia,
                a.nombre_agencia,

                LPAD(f.codigo_forma, 2, '0') AS codigo_forma,

                hv.documento,
                CONCAT_WS(' ', hv.primer_apellido, hv.segundo_apellido, hv.nombres) AS nombre_completo,

                c.saldo_actual_cuenta AS saldo_tabla,
                (COALESCE(m.total_debitos,0) - COALESCE(m.total_credits,0)) AS saldo_mov,

                (c.saldo_actual_cuenta - (COALESCE(m.total_debitos,0) - COALESCE(m.total_credits,0))) AS diferencia

            FROM depositos.cuentas_ahorro c
            JOIN general.datos_agencias a 
                ON a.id_agencia = c.id_agencia
            JOIN depositos.formas_ahorro f 
                ON f.id_forma_ahorro = c.id_forma_ahorro
            LEFT JOIN reporting.vw_hoja_vida_general_total_reciente hv 
                ON hv.id_datos_personal = c.id_datos_personal
            LEFT JOIN mov m 
                ON m.id_cuenta_ahorro = c.id_cuenta_ahorro
            WHERE
                (:ag = '0' OR c.id_agencia = CAST(:ag AS INTEGER))
                AND c.saldo_actual_cuenta <> (COALESCE(m.total_debitos,0) - COALESCE(m.total_credits,0))
            ORDER BY c.id_agencia, c.codigo_cuenta
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("ag", agencia);
        params.put("fecha", fechaCorte);

        return jdbc.query(sql, params, (rs, i) -> {
            InconsistenciaSaldoDTO dto = new InconsistenciaSaldoDTO();

            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setCodigoAgencia(rs.getString("codigo_agencia"));
            dto.setNombreAgencia(rs.getString("nombre_agencia"));
            dto.setCodigoForma(rs.getString("codigo_forma"));

            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre_completo"));

            // ============================
            //  BIGDECIMAL — FINANZAS OK
            // ============================
            BigDecimal saldoTabla = rs.getBigDecimal("saldo_tabla");
            BigDecimal saldoMov = rs.getBigDecimal("saldo_mov");

            if (saldoTabla == null) saldoTabla = BigDecimal.ZERO;
            if (saldoMov == null) saldoMov = BigDecimal.ZERO;

            dto.setSaldoTabla(saldoTabla);
            dto.setSaldoMov(saldoMov);
            dto.setDiferencia(saldoTabla.subtract(saldoMov));

            dto.setNegativo(saldoMov.compareTo(BigDecimal.ZERO) < 0);

            return dto;
        });
    }
}
