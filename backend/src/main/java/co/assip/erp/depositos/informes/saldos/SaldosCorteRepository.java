package co.assip.erp.depositos.informes.saldos;

import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteItemDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SaldosCorteRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SaldosCorteRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public List<SaldosCorteItemDTO> consultar(
            String fechaCorte,
            String agencia
    ) {

        String sql = """
            WITH mov AS (
                SELECT
                    e.id_cuenta_ahorro,
                    COALESCE(SUM(e.valor_debito), 0) AS total_debitos,
                    COALESCE(SUM(e.valor_credito), 0) AS total_creditos,
                    MAX(e.fecha_movimiento) AS fecha_ultimo_movimiento
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
                GROUP BY
                    e.id_cuenta_ahorro
            ),

            hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY
                    id_datos_personal
            ),

            pe_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM hoja_vida.permisos_especiales
                ORDER BY
                    id_datos_personal
            ),

            base AS (
                SELECT
                    TRIM(c.codigo_cuenta) AS codigo_cuenta,

                    hv.documento,

                    concat_ws(
                        ' ',
                        hv.primer_apellido,
                        hv.segundo_apellido,
                        hv.nombres
                    ) AS nombre_completo,

                    LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
                    a.nombre_agencia AS agencia,

                    LPAD(f.codigo_forma, 2, '0') AS codigo_forma,
                    f.nombre_forma AS forma,

                    hv.direccion_residencia AS direccion,
                    hv.departamento_residencia AS departamento,
                    hv.ciudad_residencia AS ciudad,

                    hv.nombre_zona AS zona,
                    hv.nombre_sub_zona AS sub_zona,

                    c.estado_cuenta_cuenta AS estado_cuenta_codigo,
                    ea.descripcion_estado_ahorro AS estado_cuenta_nombre,

                    hv.telefono,
                    hv.celular_uno AS celular,
                    hv.celular_dos AS celular_dos,
                    hv.correo_personal AS correo,

                    COALESCE(pe.recibe_llamadas, false) AS recibe_llamadas,
                    COALESCE(pe.recibe_msm, false) AS recibe_msm,
                    COALESCE(pe.recibe_emails, false) AS recibe_emails,
                    COALESCE(pe.recibe_cartas, false) AS recibe_cartas,
                    COALESCE(pe.recibe_redes_sociales, false) AS recibe_redes_sociales,

                    c.fecha_apertura_cuenta AS fecha_apertura,
                    m.fecha_ultimo_movimiento,

                    COALESCE(m.total_debitos, 0) AS total_debitos,
                    COALESCE(m.total_creditos, 0) AS total_creditos,

                    COALESCE(m.total_creditos, 0)
                    - COALESCE(m.total_debitos, 0) AS saldo_corte

                FROM depositos.cuentas_ahorro c

                INNER JOIN general.datos_agencias a
                    ON a.id_agencia = c.id_agencia

                LEFT JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.id_forma_ahorro

                LEFT JOIN depositos.estados_ahorros ea
                    ON ea.codigo_estado_ahorro = c.estado_cuenta_cuenta

                LEFT JOIN hv_unica hv
                    ON hv.id_datos_personal = c.id_datos_personal

                LEFT JOIN pe_unica pe
                    ON pe.id_datos_personal = c.id_datos_personal

                LEFT JOIN mov m
                    ON m.id_cuenta_ahorro = c.id_cuenta_ahorro

                WHERE (:agencia = '0' OR c.id_agencia = CAST(:agencia AS INTEGER))
            )

            SELECT *
            FROM base
            WHERE saldo_corte <> 0
            ORDER BY
                codigo_agencia,
                codigo_forma,
                codigo_cuenta
            """;

        return jdbc.query(
                sql,
                Map.of(
                        "fechaCorte", fechaCorte,
                        "agencia", agencia
                ),
                (rs, i) -> {

                    SaldosCorteItemDTO dto =
                            new SaldosCorteItemDTO();

                    dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
                    dto.setDocumento(rs.getString("documento"));
                    dto.setNombreCompleto(rs.getString("nombre_completo"));

                    dto.setCodigoAgencia(rs.getString("codigo_agencia"));
                    dto.setAgencia(rs.getString("agencia"));

                    dto.setCodigoForma(rs.getString("codigo_forma"));
                    dto.setForma(rs.getString("forma"));

                    dto.setDireccion(rs.getString("direccion"));
                    dto.setDepartamento(rs.getString("departamento"));
                    dto.setCiudad(rs.getString("ciudad"));

                    dto.setZona(rs.getString("zona"));
                    dto.setSubZona(rs.getString("sub_zona"));

                    dto.setEstadoCuentaCodigo(rs.getString("estado_cuenta_codigo"));
                    dto.setEstadoCuentaNombre(rs.getString("estado_cuenta_nombre"));

                    dto.setTelefono(rs.getString("telefono"));
                    dto.setCelular(rs.getString("celular"));
                    dto.setCelularDos(rs.getString("celular_dos"));
                    dto.setCorreo(rs.getString("correo"));

                    dto.setRecibeLlamadas(rs.getBoolean("recibe_llamadas"));
                    dto.setRecibeMsm(rs.getBoolean("recibe_msm"));
                    dto.setRecibeEmails(rs.getBoolean("recibe_emails"));
                    dto.setRecibeCartas(rs.getBoolean("recibe_cartas"));
                    dto.setRecibeRedesSociales(rs.getBoolean("recibe_redes_sociales"));

                    dto.setFechaApertura(rs.getString("fecha_apertura"));
                    dto.setFechaUltimoMovimiento(rs.getString("fecha_ultimo_movimiento"));

                    dto.setTotalDebitos(rs.getDouble("total_debitos"));
                    dto.setTotalCreditos(rs.getDouble("total_creditos"));
                    dto.setSaldoCorte(rs.getDouble("saldo_corte"));

                    return dto;
                }
        );
    }

    public List<Map<String, Object>> resumenPorAgencia(
            String fechaCorte,
            String agencia
    ) {

        String sql = """
            WITH mov AS (
                SELECT
                    e.id_cuenta_ahorro,
                    COALESCE(SUM(e.valor_debito), 0) AS total_debitos,
                    COALESCE(SUM(e.valor_credito), 0) AS total_creditos
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
                GROUP BY
                    e.id_cuenta_ahorro
            ),

            base AS (
                SELECT
                    c.id_agencia,

                    LPAD(a.codigo_agencia, 2, '0') AS codigo_agencia,
                    a.nombre_agencia,

                    LPAD(f.codigo_forma, 2, '0') AS codigo_forma,
                    f.nombre_forma,

                    COALESCE(m.total_debitos, 0) AS total_debitos,
                    COALESCE(m.total_creditos, 0) AS total_creditos,

                    COALESCE(m.total_creditos, 0)
                    - COALESCE(m.total_debitos, 0) AS saldo_corte

                FROM depositos.cuentas_ahorro c

                INNER JOIN general.datos_agencias a
                    ON a.id_agencia = c.id_agencia

                INNER JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.id_forma_ahorro

                LEFT JOIN mov m
                    ON m.id_cuenta_ahorro = c.id_cuenta_ahorro

                WHERE (:agencia = '0' OR c.id_agencia = CAST(:agencia AS INTEGER))
            )

            SELECT
                id_agencia,
                codigo_agencia,
                nombre_agencia,
                codigo_forma,
                nombre_forma,

                COUNT(*) AS cantidad,

                COALESCE(SUM(total_debitos), 0) AS total_debitos,
                COALESCE(SUM(total_creditos), 0) AS total_creditos,
                COALESCE(SUM(saldo_corte), 0) AS saldo,

                COALESCE(AVG(saldo_corte), 0) AS saldo_promedio,
                COALESCE(MIN(saldo_corte), 0) AS saldo_minimo,
                COALESCE(MAX(saldo_corte), 0) AS saldo_maximo

            FROM base
            WHERE saldo_corte <> 0

            GROUP BY
                id_agencia,
                codigo_agencia,
                nombre_agencia,
                codigo_forma,
                nombre_forma

            ORDER BY
                codigo_agencia,
                codigo_forma
            """;

        return jdbc.query(
                sql,
                Map.of(
                        "fechaCorte", fechaCorte,
                        "agencia", agencia
                ),
                (rs, i) -> {
                    Map<String, Object> item =
                            new HashMap<>();

                    item.put("idAgencia", rs.getInt("id_agencia"));
                    item.put("codigoAgencia", rs.getString("codigo_agencia"));
                    item.put("nombreAgencia", rs.getString("nombre_agencia"));
                    item.put("codigoForma", rs.getString("codigo_forma"));
                    item.put("nombreForma", rs.getString("nombre_forma"));
                    item.put("cantidad", rs.getInt("cantidad"));
                    item.put("totalDebitos", rs.getDouble("total_debitos"));
                    item.put("totalCreditos", rs.getDouble("total_creditos"));
                    item.put("saldo", rs.getDouble("saldo"));
                    item.put("saldoPromedio", rs.getDouble("saldo_promedio"));
                    item.put("saldoMinimo", rs.getDouble("saldo_minimo"));
                    item.put("saldoMaximo", rs.getDouble("saldo_maximo"));

                    return item;
                }
        );
    }

}