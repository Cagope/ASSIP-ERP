package co.assip.erp.depositos.informes.cuentasnr;

import co.assip.erp.depositos.informes.cuentasnr.dto.CuentasNRRequestDTO;
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

    private Map<String, Object> buildParams(CuentasNRRequestDTO r) {
        Map<String, Object> params = new HashMap<>();

        params.put("fi", r.getFechaInicial());
        params.put("ff", r.getFechaFinal());

        params.put(
                "idAgencia",
                r.getIdAgencia() == null ? 0 : r.getIdAgencia()
        );

        params.put("forma", normalizarForma(r.getForma()));

        return params;
    }

    private String normalizarForma(String forma) {
        if (forma == null || forma.isBlank()) {
            return "0";
        }

        return forma.trim();
    }

    public List<Map<String, Object>> cuentasNuevas(CuentasNRRequestDTO r) {

        String sql = """
        WITH cuentas_filtradas AS (
            SELECT
                c.id_cuenta_ahorro,
                c.codigo_cuenta,
                c.id_agencia,
                c.id_forma_ahorro,
                c.id_datos_personal,
                c.fecha_apertura_cuenta
            FROM depositos.cuentas_ahorro c
            JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro
            WHERE c.fecha_apertura_cuenta BETWEEN :fi AND :ff
              AND (:idAgencia = 0 OR c.id_agencia = :idAgencia)
              AND (
                    :forma = '0'
                 OR LPAD(f.codigo_forma, 2, '0') = LPAD(:forma, 2, '0')
              )
        ),
        mov AS (
            SELECT
                e.id_cuenta_ahorro,
                SUM(COALESCE(e.valor_debito, 0))  AS total_debitos,
                SUM(COALESCE(e.valor_credito, 0)) AS total_creditos
            FROM depositos.extractos_cuentas_ahorros e
            JOIN cuentas_filtradas cf
                ON cf.id_cuenta_ahorro = e.id_cuenta_ahorro
            WHERE e.fecha_movimiento <= cf.fecha_apertura_cuenta
            GROUP BY e.id_cuenta_ahorro
        ),
        hv_unica AS (
            SELECT *
            FROM (
                SELECT
                    h.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY h.id_datos_personal
                        ORDER BY h.id_datos_personal
                    ) AS rn
                FROM reporting.vw_hoja_vida_general_total_reciente h
            ) x
            WHERE x.rn = 1
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

            COALESCE(m.total_creditos, 0) - COALESCE(m.total_debitos, 0) AS saldo_inicial

        FROM cuentas_filtradas cf
        JOIN general.datos_agencias a
            ON a.id_agencia = cf.id_agencia
        JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = cf.id_forma_ahorro
        LEFT JOIN mov m
            ON m.id_cuenta_ahorro = cf.id_cuenta_ahorro
        LEFT JOIN hv_unica hv
            ON hv.id_datos_personal = cf.id_datos_personal

        ORDER BY cf.fecha_apertura_cuenta ASC;
        """;

        return jdbc.queryForList(sql, buildParams(r));
    }

    public List<Map<String, Object>> cuentasRetiradas(CuentasNRRequestDTO r) {

        String sql = """
        WITH cuentas_filtradas AS (
            SELECT
                c.id_cuenta_ahorro,
                c.codigo_cuenta,
                c.id_agencia,
                c.id_forma_ahorro,
                c.id_datos_personal
            FROM depositos.cuentas_ahorro c
            JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro
            WHERE (:idAgencia = 0 OR c.id_agencia = :idAgencia)
              AND (
                    :forma = '0'
                 OR LPAD(f.codigo_forma, 2, '0') = LPAD(:forma, 2, '0')
              )
        ),
        mov AS (
            SELECT
                e.id_cuenta_ahorro,
                MAX(e.fecha_movimiento) AS fecha_ultimo_mov,
                SUM(COALESCE(e.valor_debito, 0))  AS total_debitos,
                SUM(COALESCE(e.valor_credito, 0)) AS total_creditos
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
                (total_creditos - total_debitos) AS saldo_final
            FROM mov
            WHERE (total_creditos - total_debitos) = 0
        ),
        hv_unica AS (
            SELECT *
            FROM (
                SELECT
                    h.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY h.id_datos_personal
                        ORDER BY h.id_datos_personal
                    ) AS rn
                FROM reporting.vw_hoja_vida_general_total_reciente h
            ) x
            WHERE x.rn = 1
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
            u.saldo_final AS ultimo_saldo

        FROM ult_valid u
        JOIN cuentas_filtradas cf
            ON cf.id_cuenta_ahorro = u.id_cuenta_ahorro
        JOIN general.datos_agencias a
            ON a.id_agencia = cf.id_agencia
        JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = cf.id_forma_ahorro
        LEFT JOIN hv_unica hv
            ON hv.id_datos_personal = cf.id_datos_personal

        WHERE u.fecha_ultimo_mov BETWEEN :fi AND :ff

        ORDER BY u.fecha_ultimo_mov ASC;
        """;

        return jdbc.queryForList(sql, buildParams(r));
    }
}