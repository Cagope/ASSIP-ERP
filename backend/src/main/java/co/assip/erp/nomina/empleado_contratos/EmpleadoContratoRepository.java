package co.assip.erp.nomina.empleado_contratos;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmpleadoContratoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    public List<EmpleadoContratoDTO> listar() {

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
              c.cuenta_nomina_display        AS cuentaNominaDisplay,
              c.id_cuenta_ahorro_nomina      AS idCuentaAhorroNomina,
              c.fecha_envio_nota_renovacion  AS fechaEnvioNotaRenovacion,
              c.clase_riesgo_arl             AS claseRiesgoArl,
              c.porcentaje_arl               AS porcentajeArl,
              c.activo                       AS activo
            FROM nomina.empleado_contratos c
            ORDER BY c.id_contrato DESC
        """;

        return jdbc.query(sql, (rs, rowNum) -> EmpleadoContratoDTO.builder()
                .idContrato(rs.getInt("idContrato"))
                .idEmpleado((Integer) rs.getObject("idEmpleado"))
                .idSeccion((Integer) rs.getObject("idSeccion"))
                .fechaInicio(rs.getDate("fechaInicio").toLocalDate())
                .fechaFin(rs.getDate("fechaFin") != null ? rs.getDate("fechaFin").toLocalDate() : null)
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
                .cuentaNominaDisplay(rs.getString("cuentaNominaDisplay"))
                .idCuentaAhorroNomina((Long) rs.getObject("idCuentaAhorroNomina"))
                .fechaEnvioNotaRenovacion(rs.getDate("fechaEnvioNotaRenovacion") != null
                        ? rs.getDate("fechaEnvioNotaRenovacion").toLocalDate()
                        : null)
                .claseRiesgoArl((Short) rs.getObject("claseRiesgoArl"))
                .porcentajeArl(rs.getBigDecimal("porcentajeArl"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public Optional<EmpleadoContratoDTO> obtener(Integer idContrato) {

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
              c.cuenta_nomina_display        AS cuentaNominaDisplay,
              c.id_cuenta_ahorro_nomina      AS idCuentaAhorroNomina,
              c.fecha_envio_nota_renovacion  AS fechaEnvioNotaRenovacion,
              c.clase_riesgo_arl             AS claseRiesgoArl,
              c.porcentaje_arl               AS porcentajeArl,
              c.activo                       AS activo
            FROM nomina.empleado_contratos c
            WHERE c.id_contrato = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idContrato);

        List<EmpleadoContratoDTO> rows = jdbc.query(sql, params, (rs, rowNum) -> EmpleadoContratoDTO.builder()
                .idContrato(rs.getInt("idContrato"))
                .idEmpleado((Integer) rs.getObject("idEmpleado"))
                .idSeccion((Integer) rs.getObject("idSeccion"))
                .fechaInicio(rs.getDate("fechaInicio").toLocalDate())
                .fechaFin(rs.getDate("fechaFin") != null ? rs.getDate("fechaFin").toLocalDate() : null)
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
                .cuentaNominaDisplay(rs.getString("cuentaNominaDisplay"))
                .idCuentaAhorroNomina((Long) rs.getObject("idCuentaAhorroNomina"))
                .fechaEnvioNotaRenovacion(rs.getDate("fechaEnvioNotaRenovacion") != null
                        ? rs.getDate("fechaEnvioNotaRenovacion").toLocalDate()
                        : null)
                .claseRiesgoArl((Short) rs.getObject("claseRiesgoArl"))
                .porcentajeArl(rs.getBigDecimal("porcentajeArl"))
                .activo((Boolean) rs.getObject("activo"))
                .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(EmpleadoContratoDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.empleado_contratos (
              id_empleado,
              id_seccion,
              fecha_inicio,
              fecha_fin,
              id_tipo_contrato,
              id_cargo,
              salario_base,
              salario_integral,
              periodo_pago,
              id_eps,
              id_afp,
              id_cesantias,
              id_arl,
              id_caja_compensacion,
              cuenta_nomina_display,
              id_cuenta_ahorro_nomina,
              fecha_envio_nota_renovacion,
              clase_riesgo_arl,
              porcentaje_arl,
              activo,
              fk_seguridad_creacion,
              fk_seguridad_edicion
            )
            VALUES (
              :idEmpleado,
              :idSeccion,
              :fechaInicio,
              :fechaFin,
              :idTipoContrato,
              :idCargo,
              :salarioBase,
              :salarioIntegral,
              :periodoPago,
              :idEps,
              :idAfp,
              :idCesantias,
              :idArl,
              :idCaja,
              :cuentaNominaDisplay,
              :idCuentaAhorroNomina,
              :fechaEnvioNotaRenovacion,
              :claseRiesgoArl,
              :porcentajeArl,
              :activo,
              :usr,
              :usr
            )
            RETURNING id_contrato
        """;

        var params = new MapSqlParameterSource()
                .addValue("idEmpleado", dto.getIdEmpleado())
                .addValue("idSeccion", dto.getIdSeccion())
                .addValue("fechaInicio", Date.valueOf(dto.getFechaInicio()))
                .addValue("fechaFin", dto.getFechaFin() != null ? Date.valueOf(dto.getFechaFin()) : null)
                .addValue("idTipoContrato", dto.getIdTipoContrato())
                .addValue("idCargo", dto.getIdCargo())
                .addValue("salarioBase", dto.getSalarioBase() != null ? dto.getSalarioBase() : BigDecimal.ZERO)
                .addValue("salarioIntegral", dto.getSalarioIntegral() != null ? dto.getSalarioIntegral() : Boolean.FALSE)
                .addValue("periodoPago", dto.getPeriodoPago() != null ? dto.getPeriodoPago() : "MENSUAL")
                .addValue("idEps", dto.getIdEps())
                .addValue("idAfp", dto.getIdAfp())
                .addValue("idCesantias", dto.getIdCesantias())
                .addValue("idArl", dto.getIdArl())
                .addValue("idCaja", dto.getIdCajaCompensacion())
                .addValue("cuentaNominaDisplay", dto.getCuentaNominaDisplay())
                .addValue("idCuentaAhorroNomina", dto.getIdCuentaAhorroNomina())
                .addValue("fechaEnvioNotaRenovacion", dto.getFechaEnvioNotaRenovacion() != null
                        ? Date.valueOf(dto.getFechaEnvioNotaRenovacion())
                        : null)
                .addValue("claseRiesgoArl", dto.getClaseRiesgoArl() != null ? dto.getClaseRiesgoArl() : (short) 1)
                .addValue("porcentajeArl", dto.getPorcentajeArl() != null ? dto.getPorcentajeArl() : BigDecimal.ZERO)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idContrato, EmpleadoContratoDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.empleado_contratos
            SET
              id_empleado = :idEmpleado,
              id_seccion = :idSeccion,
              fecha_inicio = :fechaInicio,
              fecha_fin = :fechaFin,
              id_tipo_contrato = :idTipoContrato,
              id_cargo = :idCargo,
              salario_base = :salarioBase,
              salario_integral = :salarioIntegral,
              periodo_pago = :periodoPago,
              id_eps = :idEps,
              id_afp = :idAfp,
              id_cesantias = :idCesantias,
              id_arl = :idArl,
              id_caja_compensacion = :idCaja,
              cuenta_nomina_display = :cuentaNominaDisplay,
              id_cuenta_ahorro_nomina = :idCuentaAhorroNomina,
              fecha_envio_nota_renovacion = :fechaEnvioNotaRenovacion,
              clase_riesgo_arl = :claseRiesgoArl,
              porcentaje_arl = :porcentajeArl,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_contrato = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idContrato)
                .addValue("idEmpleado", dto.getIdEmpleado())
                .addValue("idSeccion", dto.getIdSeccion())
                .addValue("fechaInicio", Date.valueOf(dto.getFechaInicio()))
                .addValue("fechaFin", dto.getFechaFin() != null ? Date.valueOf(dto.getFechaFin()) : null)
                .addValue("idTipoContrato", dto.getIdTipoContrato())
                .addValue("idCargo", dto.getIdCargo())
                .addValue("salarioBase", dto.getSalarioBase() != null ? dto.getSalarioBase() : BigDecimal.ZERO)
                .addValue("salarioIntegral", dto.getSalarioIntegral() != null ? dto.getSalarioIntegral() : Boolean.FALSE)
                .addValue("periodoPago", dto.getPeriodoPago() != null ? dto.getPeriodoPago() : "MENSUAL")
                .addValue("idEps", dto.getIdEps())
                .addValue("idAfp", dto.getIdAfp())
                .addValue("idCesantias", dto.getIdCesantias())
                .addValue("idArl", dto.getIdArl())
                .addValue("idCaja", dto.getIdCajaCompensacion())
                .addValue("cuentaNominaDisplay", dto.getCuentaNominaDisplay())
                .addValue("idCuentaAhorroNomina", dto.getIdCuentaAhorroNomina())
                .addValue("fechaEnvioNotaRenovacion", dto.getFechaEnvioNotaRenovacion() != null
                        ? Date.valueOf(dto.getFechaEnvioNotaRenovacion())
                        : null)
                .addValue("claseRiesgoArl", dto.getClaseRiesgoArl() != null ? dto.getClaseRiesgoArl() : (short) 1)
                .addValue("porcentajeArl", dto.getPorcentajeArl() != null ? dto.getPorcentajeArl() : BigDecimal.ZERO)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer idContrato) {

        String sql = """
            DELETE FROM nomina.empleado_contratos
            WHERE id_contrato = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", idContrato);

        jdbc.update(sql, params);
    }
}
