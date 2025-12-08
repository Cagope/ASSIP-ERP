package co.assip.erp.depositos.habilidad_asociado;

import co.assip.erp.depositos.habilidad_asociado.dto.HabilidadAsociadoEntradaDTO;
import co.assip.erp.depositos.habilidad_asociado.dto.HabilidadAsociadoItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HabilidadAsociadoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // 0️⃣ OBTENER PARÁMETRO 103 POR AGENCIA (edad menor)
    // ============================================================
    private int obtenerEdadMenor(Integer agenciaId) {

        // Si agenciaId es 0 o null → usar 1 (agencia matriz)
        int ag = (agenciaId == null || agenciaId == 0) ? 1 : agenciaId;

        String sql = """
        SELECT valor_parametro::int
        FROM general.parametros
        WHERE id_agencia = :agenciaId
          AND codigo_parametro = '103'
        LIMIT 1
    """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("agenciaId", ag),
                Integer.class
        );
    }


    // ============================================================
    // 1️⃣ CONSULTA — EVALUAR HABILIDAD / INHABILIDAD
    // ============================================================
    public List<HabilidadAsociadoItemDTO> evaluar(HabilidadAsociadoEntradaDTO dto) {

        // 🔥 Obtener parámetro 103 según agencia
        int edadMenores = obtenerEdadMenor(dto.getAgenciaId());

        String sql = """
        WITH config AS (
            SELECT
                :edadMenores::int        AS edad_menores,
                :valorMenores::numeric   AS valor_menores,
                :valorMayores::numeric   AS valor_mayores,
                :valorJuridicas::numeric AS valor_juridicas
        ),

        cuentas AS (
              SELECT
                  c.id_cuenta_ahorro,
                  c.codigo_cuenta,
                  c.id_datos_personal,
                  c.id_agencia,
                  c.estado_cuenta_cuenta,
                  c.fecha_apertura_cuenta
              FROM depositos.cuentas_ahorro c
              JOIN depositos.formas_ahorro f
                  ON f.id_forma_ahorro = c.id_forma_ahorro
              WHERE f.codigo_forma = '01'
                AND c.estado_cuenta_cuenta NOT IN ('C','T')
                AND (:agenciaId = 0 OR c.id_agencia = :agenciaId)
          ),

        saldo_actual AS (
            SELECT
                e.id_cuenta_ahorro,
                SUM(e.valor_debito - e.valor_credito) AS saldo_hoy
            FROM depositos.extractos_cuentas_ahorros e
            GROUP BY e.id_cuenta_ahorro
        ),

        cuentas_filtradas AS (
            SELECT c.*, s.saldo_hoy
            FROM cuentas c
            JOIN saldo_actual s ON s.id_cuenta_ahorro = c.id_cuenta_ahorro
            WHERE s.saldo_hoy > 0
        ),

        movs AS (
            SELECT
                e.id_cuenta_ahorro,
                SUM(e.valor_debito - e.valor_credito) AS total_aportes
            FROM depositos.extractos_cuentas_ahorros e
            WHERE e.fecha_movimiento BETWEEN :fechaInicio AND :fechaFin
              AND e.tipo_movimiento NOT IN ('005','006')
            GROUP BY e.id_cuenta_ahorro
        ),

        personas AS (
            SELECT
                hv.id_datos_personal,
                hv.documento,
                hv.nombre_completo_apellidos AS nombre,
                hv.nombre_zona,
                hv.nombre_sub_zona,
                hv.tipo_persona,
                DATE_PART('year', AGE(:fechaFin, hv.fecha_nacimiento))::int AS edad
            FROM reporting.vw_hoja_vida_general_total_extendida hv
        ),

        base AS (
            SELECT
                c.id_cuenta_ahorro,
                c.codigo_cuenta,
                p.documento,
                p.nombre,
                p.tipo_persona,
                p.edad,
                p.nombre_zona,
                p.nombre_sub_zona,
                c.estado_cuenta_cuenta,
                c.saldo_hoy,
                COALESCE(m.total_aportes, 0) AS total_aportes
            FROM cuentas_filtradas c
            JOIN personas p ON p.id_datos_personal = c.id_datos_personal
            LEFT JOIN movs m ON m.id_cuenta_ahorro = c.id_cuenta_ahorro
        ),

        evaluado AS (
            SELECT
                *,
                CASE
                    WHEN estado_cuenta_cuenta = 'F' THEN
                        'INHÁBIL (Fallecido)'

                    WHEN tipo_persona = '1' THEN
                        CASE
                            WHEN edad <= (SELECT edad_menores FROM config)
                                 AND total_aportes < (SELECT valor_menores FROM config)
                                THEN 'INHÁBIL (Menor — aportes insuficientes)'

                            WHEN edad > (SELECT edad_menores FROM config)
                                 AND total_aportes < (SELECT valor_mayores FROM config)
                                THEN 'INHÁBIL (Mayor — aportes insuficientes)'

                            ELSE 'HÁBIL'
                        END

                    WHEN tipo_persona = '2' THEN
                        CASE
                            WHEN total_aportes < (SELECT valor_juridicas FROM config)
                                THEN 'INHÁBIL (Jurídica — aportes insuficientes)'
                            ELSE 'HÁBIL'
                        END

                    ELSE
                        'HÁBIL'
                END AS resultado
            FROM base
        )

        SELECT
            id_cuenta_ahorro,
            codigo_cuenta,
            nombre_zona AS zona,
            nombre_sub_zona AS subzona,
            documento,
            nombre,
            tipo_persona,
            edad,
            estado_cuenta_cuenta AS estado,
            saldo_hoy,
            total_aportes,
            resultado
        FROM evaluado
        ORDER BY zona, subzona, nombre;
        """;

        var params = new MapSqlParameterSource()
                .addValue("agenciaId", dto.getAgenciaId())
                .addValue("fechaInicio", dto.getFechaInicio())
                .addValue("fechaFin", dto.getFechaFin())
                .addValue("edadMenores", edadMenores)
                .addValue("valorMenores", dto.getValorMenores())
                .addValue("valorMayores", dto.getValorMayores())
                .addValue("valorJuridicas", dto.getValorJuridicas());

        return jdbc.query(sql, params, (rs, rowNum) -> {
            var item = new HabilidadAsociadoItemDTO();
            item.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            item.setCodigoCuenta(rs.getString("codigo_cuenta"));
            item.setZona(rs.getString("zona"));
            item.setSubzona(rs.getString("subzona"));
            item.setDocumento(rs.getString("documento"));
            item.setNombre(rs.getString("nombre"));
            item.setTipoPersona(rs.getString("tipo_persona"));
            item.setEdad(rs.getInt("edad"));
            item.setEstado(rs.getString("estado"));
            item.setSaldoHoy(rs.getBigDecimal("saldo_hoy"));
            item.setTotalAportes(rs.getBigDecimal("total_aportes"));
            item.setResultado(rs.getString("resultado"));
            return item;
        });
    }

    // ============================================================
    // 2️⃣ UPDATE — CAMBIAR ESTADO A / I
    // ============================================================
    public void actualizarEstado(Integer idCuentaAhorro, String nuevoEstado, Integer usuarioId) {

        String sql = """
            UPDATE depositos.cuentas_ahorro
            SET estado_cuenta_cuenta = :estado,
                fecha_actualizacion = NOW(),
                usuario_actualizacion = :usuarioId
            WHERE id_cuenta_ahorro = :idCuenta
        """;

        var params = new MapSqlParameterSource()
                .addValue("estado", nuevoEstado)
                .addValue("idCuenta", idCuentaAhorro)
                .addValue("usuarioId", usuarioId);

        jdbc.update(sql, params);
    }
}
