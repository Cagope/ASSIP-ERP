package co.assip.erp.nomina.liquidacion_v2;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.dto.TotalesLiquidacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LiquidacionV2Repository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // 🔎 VALIDAR EXISTENCIA (IDEMPOTENCIA)
    // =========================================================
    public boolean existeLiquidacion(Integer idPeriodoNomina, Integer idContrato) {

        String sql = """
            SELECT COUNT(1)
            FROM nomina.liquidaciones
            WHERE id_periodo_nomina = :idPeriodo
              AND id_contrato = :idContrato
              AND estado <> 'ANULADA'
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodoNomina)
                .addValue("idContrato", idContrato);

        Integer count = jdbc.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    // =========================================================
    // 🧾 INSERTAR CABECERA
    // =========================================================
    public Integer insertarCabecera(
            Integer idPeriodoNomina,
            EmpleadoContratoDTO contrato,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO nomina.liquidaciones (
                id_periodo_nomina,
                id_contrato,
                id_empleado,
                fk_agencia,
                salario_base,
                dias_laborados,
                ibc,
                total_devengados,
                total_deducciones,
                total_provisiones,
                neto_pagar,
                estado,
                fk_seguridad_creacion,
                fecha_creacion
            ) VALUES (
                :idPeriodo,
                :idContrato,
                :idEmpleado,
                (SELECT p.id_agencia FROM nomina.periodos_nomina p WHERE p.id_periodo = :idPeriodo),
                :salarioBase,
                0,
                0,
                0,
                0,
                0,
                0,
                'ABIERTO',
                :idUsuario,
                NOW()
            )
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodoNomina)
                .addValue("idContrato", contrato.getIdContrato())
                .addValue("idEmpleado", contrato.getIdEmpleado())
                .addValue("salarioBase", contrato.getSalarioBase() != null
                        ? contrato.getSalarioBase()
                        : BigDecimal.ZERO)
                .addValue("idUsuario", idUsuario);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"id_liquidacion"});

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("No se pudo generar id_liquidacion");
        }

        return id.intValue();
    }

    // =========================================================
    // 🧮 ACTUALIZAR TOTALES
    // =========================================================
    public void actualizarTotales(
            Integer idLiquidacion,
            BigDecimal ibc,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE nomina.liquidaciones l
            SET
                ibc = :ibc,
                total_devengados = COALESCE((
                    SELECT SUM(d.valor_total)
                    FROM nomina.liquidacion_detalle d
                    WHERE d.id_liquidacion = l.id_liquidacion
                      AND d.tipo = 'DEVENGADO'
                ), 0),
                total_deducciones = COALESCE((
                    SELECT SUM(d.valor_total)
                    FROM nomina.liquidacion_detalle d
                    WHERE d.id_liquidacion = l.id_liquidacion
                      AND d.tipo = 'DEDUCCION'
                ), 0),
                total_provisiones = COALESCE((
                    SELECT SUM(d.valor_total)
                    FROM nomina.liquidacion_detalle d
                    WHERE d.id_liquidacion = l.id_liquidacion
                      AND d.tipo = 'PROVISION'
                ), 0),
                neto_pagar = COALESCE((
                    SELECT
                        SUM(CASE WHEN d.tipo = 'DEVENGADO' THEN d.valor_total ELSE 0 END)
                      - SUM(CASE WHEN d.tipo = 'DEDUCCION' THEN d.valor_total ELSE 0 END)
                    FROM nomina.liquidacion_detalle d
                    WHERE d.id_liquidacion = l.id_liquidacion
                ), 0),
                estado = 'CERRADO',
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = NOW()
            WHERE l.id_liquidacion = :idLiquidacion
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idLiquidacion", idLiquidacion)
                .addValue("ibc", ibc != null ? ibc : BigDecimal.ZERO)
                .addValue("idUsuario", idUsuario);

        jdbc.update(sql, params);
    }

    // =========================================================
    // 📊 OBTENER TOTALES
    // =========================================================
    public TotalesLiquidacionDTO obtenerTotales(Integer idLiquidacion) {

        String sql = """
            SELECT
                total_devengados,
                total_deducciones,
                total_provisiones,
                neto_pagar
            FROM nomina.liquidaciones
            WHERE id_liquidacion = :idLiquidacion
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idLiquidacion", idLiquidacion);

        TotalesLiquidacionDTO dto = jdbc.queryForObject(sql, params, (rs, rowNum) -> {
            TotalesLiquidacionDTO x = new TotalesLiquidacionDTO();
            x.setTotalDevengados(rs.getBigDecimal("total_devengados"));
            x.setTotalDeducciones(rs.getBigDecimal("total_deducciones"));
            x.setTotalProvisiones(rs.getBigDecimal("total_provisiones"));
            x.setNetoPagar(rs.getBigDecimal("neto_pagar"));
            return x;
        });

        if (dto == null) {
            throw new IllegalStateException(
                    "No existe liquidación con id_liquidacion=" + idLiquidacion
            );
        }

        return dto;
    }

    // =========================================================
    // 📌 CONTRATOS A LIQUIDAR EN EL PERÍODO
    // =========================================================
    public List<EmpleadoContratoDTO> obtenerContratosParaLiquidacion(
            Integer idPeriodo,
            Integer idAgencia
    ) {

        String sql = """
            SELECT
              c.id_contrato                  AS idContrato,
              c.id_empleado                  AS idEmpleado,
              c.id_seccion                   AS idSeccion,
              c.fecha_inicio                 AS fechaInicio,
              c.fecha_fin                    AS fechaFin,
              c.id_tipo_contrato             AS idTipoContrato,
              c.id_cargo                     AS idCargo,
              c.salario_base                 AS salarioBase,
              c.salario_integral             AS salarioIntegral,
              c.periodo_pago                 AS periodoPago,
              c.id_eps                       AS idEps,
              c.id_afp                       AS idAfp,
              c.id_cesantias                 AS idCesantias,
              c.id_arl                       AS idArl,
              c.id_caja_compensacion         AS idCajaCompensacion,
              c.id_cuenta_ahorro_nomina      AS idCuentaAhorroNomina,
              c.fecha_envio_nota_renovacion  AS fechaEnvioNotaRenovacion,
              c.clase_riesgo_arl             AS claseRiesgoArl,
              c.porcentaje_arl               AS porcentajeArl,
              c.activo                       AS activo,
              tc.aplica_salud                AS aplicaSalud,
              tc.aplica_pension              AS aplicaPension,
              tc.aplica_arl                  AS aplicaArl,
              tc.aplica_caja_compensacion    AS aplicaCajaCompensacion,
              tc.aplica_cesantias            AS aplicaCesantias,
              tc.aplica_prima                AS aplicaPrima,
              tc.aplica_vacaciones           AS aplicaVacaciones,
              tc.aplica_parafiscales         AS aplicaParafiscales,
              tc.aplica_aux_transporte       AS aplicaAuxTransporte,
              pr.documento                   AS documentoEmpleado,
              TRIM(BOTH FROM CONCAT_WS(' ', pr.primer_apellido, pr.segundo_apellido, pr.nombres)) AS nombreEmpleado
            FROM nomina.empleado_contratos c
            LEFT JOIN nomina.tipos_contrato tc
              ON tc.id_tipo_contrato = c.id_tipo_contrato
            JOIN nomina.periodos_nomina p
              ON c.fecha_inicio <= p.fecha_fin
             AND (c.fecha_fin IS NULL OR c.fecha_fin >= p.fecha_inicio)
            LEFT JOIN nomina.empleados e
              ON e.id_empleado = c.id_empleado
             AND e.activo = TRUE
            LEFT JOIN shared.vw_personas_resumen pr
              ON pr.id_datos_personal = e.id_datos_personal
             AND pr.activo = TRUE
            WHERE p.id_periodo = :idPeriodo
              AND e.id_agencia = :idAgencia
              AND c.activo = TRUE
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodo", idPeriodo)
                        .addValue("idAgencia", idAgencia),
                (rs, rowNum) -> {

                    Date fi = rs.getDate("fechaInicio");
                    Date ff = rs.getDate("fechaFin");
                    Date fe = rs.getDate("fechaEnvioNotaRenovacion");

                    return EmpleadoContratoDTO.builder()
                            .idContrato(rs.getInt("idContrato"))
                            .idEmpleado(rs.getInt("idEmpleado"))
                            .idSeccion((Integer) rs.getObject("idSeccion"))
                            .fechaInicio(fi != null ? fi.toLocalDate() : null)
                            .fechaFin(ff != null ? ff.toLocalDate() : null)
                            .idTipoContrato((Integer) rs.getObject("idTipoContrato"))
                            .idCargo((Integer) rs.getObject("idCargo"))
                            .salarioBase(rs.getBigDecimal("salarioBase"))
                            .salarioIntegral((Boolean) rs.getObject("salarioIntegral"))
                            .periodoPago(rs.getString("periodoPago"))
                            .idEps((Integer) rs.getObject("idEps"))
                            .idAfp((Integer) rs.getObject("idAfp"))
                            .idCesantias((Integer) rs.getObject("idCesantias"))
                            .idArl((Integer) rs.getObject("idArl"))
                            .idCajaCompensacion((Integer) rs.getObject("idCajaCompensacion"))
                            .idCuentaAhorroNomina((Long) rs.getObject("idCuentaAhorroNomina"))
                            .fechaEnvioNotaRenovacion(fe != null ? fe.toLocalDate() : null)
                            .claseRiesgoArl(
                                    rs.getObject("claseRiesgoArl") != null
                                            ? rs.getShort("claseRiesgoArl")
                                            : null
                            )
                            .porcentajeArl(rs.getBigDecimal("porcentajeArl"))
                            .activo(rs.getBoolean("activo"))
                            .aplicaSalud((Boolean) rs.getObject("aplicaSalud"))
                            .aplicaPension((Boolean) rs.getObject("aplicaPension"))
                            .aplicaArl((Boolean) rs.getObject("aplicaArl"))
                            .aplicaCajaCompensacion((Boolean) rs.getObject("aplicaCajaCompensacion"))
                            .aplicaCesantias((Boolean) rs.getObject("aplicaCesantias"))
                            .aplicaPrima((Boolean) rs.getObject("aplicaPrima"))
                            .aplicaVacaciones((Boolean) rs.getObject("aplicaVacaciones"))
                            .aplicaParafiscales((Boolean) rs.getObject("aplicaParafiscales"))
                            .aplicaAuxTransporte((Boolean) rs.getObject("aplicaAuxTransporte"))
                            .documentoEmpleado(rs.getString("documentoEmpleado"))
                            .nombreEmpleado(rs.getString("nombreEmpleado"))
                            .build();
                }
        );
    }

    // =========================================================
    // 📆 PERÍODO OPERATIVO (primer ABIERTO por agencia)
    // =========================================================
    public Integer obtenerPeriodoOperativo(Integer idAgencia) {

        String sql = """
            SELECT p.id_periodo
            FROM nomina.periodos_nomina p
            WHERE UPPER(TRIM(p.estado)) = 'ABIERTO'
              AND p.id_agencia = :idAgencia
            ORDER BY p.fecha_inicio ASC, p.id_periodo ASC
            LIMIT 1
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("idAgencia", idAgencia),
                rs -> rs.next() ? rs.getInt("id_periodo") : null
        );
    }

    // =========================================================
    // 🔎 OBTENER AGENCIA DEL PERÍODO
    // =========================================================
    public Integer obtenerAgenciaDelPeriodo(Integer idPeriodo) {

        String sql = """
            SELECT id_agencia
            FROM nomina.periodos_nomina
            WHERE id_periodo = :idPeriodo
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idPeriodo", idPeriodo),
                Integer.class
        );
    }

    // =========================================================
    // 🧹 ELIMINAR LIQUIDACIÓN POR PERÍODO (detalle + cabecera)
    // =========================================================
    public int eliminarPorPeriodo(Integer idPeriodoNomina) {

        if (idPeriodoNomina == null) {
            throw new IllegalArgumentException("El id del período es obligatorio");
        }

        String sqlDetalle = """
            DELETE FROM nomina.liquidacion_detalle d
            USING nomina.liquidaciones l
            WHERE d.id_liquidacion = l.id_liquidacion
              AND l.id_periodo_nomina = :idPeriodo
        """;

        jdbc.update(sqlDetalle, new MapSqlParameterSource("idPeriodo", idPeriodoNomina));

        String sqlCab = """
            DELETE FROM nomina.liquidaciones
            WHERE id_periodo_nomina = :idPeriodo
        """;

        return jdbc.update(sqlCab, new MapSqlParameterSource("idPeriodo", idPeriodoNomina));
    }

    // =========================================================
    // 🗓️ ACTUALIZAR DÍAS LABORADOS
    // =========================================================
    public void actualizarDiasLaborados(
            Integer idLiquidacion,
            BigDecimal diasLaborados,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE nomina.liquidaciones
            SET dias_laborados = :dias,
                fk_seguridad_edicion = :usuario,
                fecha_edicion = NOW()
            WHERE id_liquidacion = :id
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", idLiquidacion)
                .addValue("dias", diasLaborados)
                .addValue("usuario", idUsuario);

        jdbc.update(sql, params);
    }
}