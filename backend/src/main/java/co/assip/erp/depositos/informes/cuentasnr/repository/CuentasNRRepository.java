package co.assip.erp.depositos.informes.cuentasnr.repository;

import co.assip.erp.depositos.informes.cuentasnr.dto.CuentasNRRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CuentasNRRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CuentasNRRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ==========================================================
    // 🧩 Decodificador seguro (sin NULL)
    // ==========================================================
    private Map<String, Object> buildFormaParams(String forma) {
        Map<String, Object> params = new HashMap<>();

        if (forma == null || forma.isBlank()) {
            params.put("formaId", -99999);
            params.put("formaCod", "##INVALID##");
            return params;
        }

        if (forma.matches("\\d+")) {
            params.put("formaId", Integer.parseInt(forma));
            params.put("formaCod", "##INVALID##");
        } else {
            params.put("formaId", -99999);
            params.put("formaCod", forma);
        }

        return params;
    }

    // ==========================================================
    // 🔵 1. CUENTAS NUEVAS (corregida totalmente)
    // ==========================================================
    public List<Map<String, Object>> cuentasNuevas(CuentasNRRequest r) {

        String sql = """
            WITH cuentas_filtradas AS (
                SELECT 
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.codigo_agencia,
                    c.codigo_forma,
                    c.id_datos_personal,
                    c.fecha_apertura_cuenta
                FROM depositos.cuentas_ahorro c
                JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.codigo_forma
                WHERE c.fecha_apertura_cuenta BETWEEN :fi AND :ff
                  AND (
                        c.codigo_forma = :formaId
                     OR LPAD(f.codigo_forma, 2, '0') = :formaCod
                  )
            ),
            mov AS (
                SELECT 
                    e.id_cuenta_ahorro,
                    SUM(e.valor_debito)  AS total_debitos,
                    SUM(e.valor_credito) AS total_credits
                FROM depositos.extractos_cuentas_ahorros e
                JOIN cuentas_filtradas cf
                    ON cf.id_cuenta_ahorro = e.id_cuenta_ahorro
                WHERE e.fecha_movimiento <= cf.fecha_apertura_cuenta
                GROUP BY e.id_cuenta_ahorro
            )
            SELECT 
                cf.codigo_cuenta,
                hv.documento,
                concat_ws(' ', hv.primer_apellido, hv.segundo_apellido, hv.nombres) AS nombre_completo,

                LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
                a.nombre_agencia AS agencia,

                LPAD(f.codigo_forma, 2, '0') AS codigo_forma,
                f.nombre_forma AS forma,

                cf.fecha_apertura_cuenta AS fecha_apertura,

                COALESCE(m.total_debitos,0) - COALESCE(m.total_credits,0) AS saldo_inicial

            FROM cuentas_filtradas cf
            JOIN general.datos_agencias a 
                ON a.id_agencia = cf.codigo_agencia
            JOIN depositos.formas_ahorro f 
                ON f.id_forma_ahorro = cf.codigo_forma
            LEFT JOIN mov m 
                ON m.id_cuenta_ahorro = cf.id_cuenta_ahorro
            LEFT JOIN reporting.vw_hoja_vida_general_total_reciente hv 
                ON hv.id_datos_personal = cf.id_datos_personal

            ORDER BY cf.fecha_apertura_cuenta ASC;
            """;

        Map<String, Object> params = buildFormaParams(r.getForma());
        params.put("fi", r.getFechaInicial());
        params.put("ff", r.getFechaFinal());

        return jdbc.queryForList(sql, params);
    }

    // ==========================================================
    // 🔴 2. CUENTAS RETIRADAS (ya estaba correcta)
    // ==========================================================
    public List<Map<String, Object>> cuentasRetiradas(CuentasNRRequest r) {

        String sql = """
            WITH cuentas_filtradas AS (
                SELECT 
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.codigo_agencia,
                    c.codigo_forma,
                    c.id_datos_personal
                FROM depositos.cuentas_ahorro c
                JOIN depositos.formas_ahorro f 
                    ON f.id_forma_ahorro = c.codigo_forma
                WHERE 
                        c.codigo_forma = :formaId
                     OR LPAD(f.codigo_forma, 2, '0') = :formaCod
            ),
            mov AS (
                SELECT 
                    e.id_cuenta_ahorro,
                    MAX(e.fecha_movimiento) AS fecha_ultimo_mov,
                    SUM(e.valor_debito)    AS total_debitos,
                    SUM(e.valor_credito)   AS total_creditos
                FROM depositos.extractos_cuentas_ahorros e
                JOIN cuentas_filtradas cf 
                    ON cf.id_cuenta_ahorro = e.id_cuenta_ahorro
                GROUP BY e.id_cuenta_ahorro
            ),
            ult_valid AS (
                SELECT 
                    id_cuenta_ahorro,
                    fecha_ultimo_mov,
                    total_debitos,
                    total_creditos,
                    (total_debitos - total_creditos) AS saldo_final
                FROM mov
                WHERE (total_debitos - total_creditos) = 0
            )
            SELECT 
                cf.codigo_cuenta,
                hv.documento,
                concat_ws(' ', hv.primer_apellido, hv.segundo_apellido, hv.nombres) AS nombre_completo,

                LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
                a.nombre_agencia AS agencia,

                LPAD(f.codigo_forma, 2, '0') AS codigo_forma,
                f.nombre_forma AS forma,

                u.fecha_ultimo_mov AS fecha_retiro,
                u.saldo_final      AS ultimo_saldo

            FROM ult_valid u
            JOIN cuentas_filtradas cf 
                ON cf.id_cuenta_ahorro = u.id_cuenta_ahorro
            JOIN general.datos_agencias a 
                ON a.id_agencia = cf.codigo_agencia
            JOIN depositos.formas_ahorro f 
                ON f.id_forma_ahorro = cf.codigo_forma
            LEFT JOIN reporting.vw_hoja_vida_general_total_reciente hv 
                ON hv.id_datos_personal = cf.id_datos_personal

            WHERE u.fecha_ultimo_mov BETWEEN :fi AND :ff

            ORDER BY u.fecha_ultimo_mov ASC;
            """;

        Map<String, Object> params = buildFormaParams(r.getForma());
        params.put("fi", r.getFechaInicial());
        params.put("ff", r.getFechaFinal());

        return jdbc.queryForList(sql, params);
    }
}
