package co.assip.erp.depositos.informes.saldos.repository;

import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteItemDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SaldosCorteRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SaldosCorteRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SaldosCorteItemDTO> consultar(String fechaCorte, String agencia) {

        String sql = """
        WITH mov AS (
            SELECT 
                e.id_cuenta_ahorro,
                SUM(e.valor_debito) AS total_debitos,
                SUM(e.valor_credito) AS total_creditos
            FROM depositos.extractos_cuentas_ahorros e
            WHERE e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
            GROUP BY e.id_cuenta_ahorro
        ),
        base AS (
            SELECT
                c.codigo_cuenta,
                hv.documento,
                concat_ws(' ', hv.primer_apellido, hv.segundo_apellido, hv.nombres) AS nombre_completo,
                
                LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
                a.nombre_agencia AS agencia,

                LPAD(f.codigo_forma, 2, '0') AS codigo_forma,
                f.nombre_forma AS forma,

                hv.nombre_zona AS zona,
                hv.nombre_sub_zona AS sub_zona,

                c.estado_cuenta_cuenta AS estado_cuenta_codigo,
                ea.descripcion_estado_ahorro AS estado_cuenta_nombre,

                hv.telefono,
                hv.celular_uno AS celular,
                hv.correo_personal AS correo,

                pe.recibe_llamadas,
                pe.recibe_msm,
                pe.recibe_emails,
                pe.recibe_cartas,
                pe.recibe_redes_sociales,

                c.fecha_apertura_cuenta AS fecha_apertura,

                COALESCE(m.total_debitos,0) - COALESCE(m.total_creditos,0) AS saldo_corte
            FROM depositos.cuentas_ahorro c
            JOIN general.datos_agencias a ON a.id_agencia = c.id_agencia
            LEFT JOIN depositos.formas_ahorro f ON f.id_forma_ahorro = c.id_forma_ahorro
            LEFT JOIN depositos.estados_ahorros ea ON ea.codigo_estado_ahorro = c.estado_cuenta_cuenta
            LEFT JOIN reporting.vw_hoja_vida_general_total_reciente hv ON hv.id_datos_personal = c.id_datos_personal
            LEFT JOIN hoja_vida.permisos_especiales pe ON pe.id_datos_personal = c.id_datos_personal
            LEFT JOIN mov m ON m.id_cuenta_ahorro = c.id_cuenta_ahorro
            WHERE (:agencia = '0' OR c.id_agencia = CAST(:agencia AS INTEGER))
        )
        SELECT *
        FROM base
        WHERE saldo_corte <> 0
        ORDER BY codigo_agencia, codigo_forma, codigo_cuenta
        """;

        return jdbc.query(sql, Map.of(
                "fechaCorte", fechaCorte,
                "agencia", agencia
        ), (rs, i) -> {
            SaldosCorteItemDTO dto = new SaldosCorteItemDTO();

            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre_completo"));

            dto.setCodigoAgencia(rs.getString("codigo_agencia"));
            dto.setAgencia(rs.getString("agencia"));
            dto.setCodigoForma(rs.getString("codigo_forma"));
            dto.setForma(rs.getString("forma"));

            dto.setZona(rs.getString("zona"));
            dto.setSubZona(rs.getString("sub_zona"));

            dto.setEstadoCuentaCodigo(rs.getString("estado_cuenta_codigo"));
            dto.setEstadoCuentaNombre(rs.getString("estado_cuenta_nombre"));

            dto.setTelefono(rs.getString("telefono"));
            dto.setCelular(rs.getString("celular"));
            dto.setCorreo(rs.getString("correo"));

            dto.setRecibeLlamadas(rs.getBoolean("recibe_llamadas"));
            dto.setRecibeMsm(rs.getBoolean("recibe_msm"));
            dto.setRecibeEmails(rs.getBoolean("recibe_emails"));
            dto.setRecibeCartas(rs.getBoolean("recibe_cartas"));
            dto.setRecibeRedesSociales(rs.getBoolean("recibe_redes_sociales"));

            dto.setFechaApertura(rs.getString("fecha_apertura"));
            dto.setSaldoCorte(rs.getDouble("saldo_corte"));

            return dto;
        });
    }


    // ============================================================
    // 🆕 RESUMEN POR AGENCIA Y FORMA  — AHORA INCLUYE CODIGOS
    // ============================================================
    public List<Map<String, Object>> resumenPorAgencia(String fechaCorte) {

        String sql = """
        WITH mov AS (
            SELECT 
                e.id_cuenta_ahorro,
                SUM(e.valor_debito) AS total_debitos,
                SUM(e.valor_credito) AS total_creditos
            FROM depositos.extractos_cuentas_ahorros e
            WHERE e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
            GROUP BY e.id_cuenta_ahorro
        ),
        base AS (
            SELECT
                LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
                a.nombre_agencia,

                LPAD(f.codigo_forma, 2, '0') AS codigo_forma,
                f.nombre_forma,

                COALESCE(m.total_debitos,0) - COALESCE(m.total_creditos,0) AS saldo_corte
            FROM depositos.cuentas_ahorro c
            JOIN general.datos_agencias a ON a.id_agencia = c.id_agencia
            JOIN depositos.formas_ahorro f ON f.id_forma_ahorro = c.id_forma_ahorro
            LEFT JOIN mov m ON m.id_cuenta_ahorro = c.id_cuenta_ahorro
        )
        SELECT
            codigo_agencia,
            nombre_agencia,
            codigo_forma,
            nombre_forma,
            SUM(saldo_corte) AS saldo,
            COUNT(*)        AS cantidad
        FROM base
        WHERE saldo_corte <> 0
        GROUP BY codigo_agencia, nombre_agencia, codigo_forma, nombre_forma
        ORDER BY codigo_agencia, codigo_forma
        """;

        return jdbc.query(sql,
                Map.of("fechaCorte", fechaCorte),
                (rs, i) -> Map.of(
                        "codigoAgencia", rs.getString("codigo_agencia"),
                        "nombreAgencia", rs.getString("nombre_agencia"),
                        "codigoForma", rs.getString("codigo_forma"),
                        "nombreForma", rs.getString("nombre_forma"),
                        "saldo", rs.getDouble("saldo"),
                        "cantidad", rs.getInt("cantidad")
                )
        );
    }

}
