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
import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoListViewDTO;


@Repository
@RequiredArgsConstructor
public class EmpleadoContratoRepository {

    private final NamedParameterJdbcTemplate jdbc;

// ============================================================
// LISTAR (vista decodificada)
// ============================================================
public List<EmpleadoContratoListViewDTO> listar() {

    String sql = """
        SELECT
          c.id_contrato                 AS idContrato,
          c.id_empleado                 AS idEmpleado,

          -- ✅ EMPLEADO (para Documento / Nombre)
          e.id_datos_personal           AS idDatosPersonal,
          dp.documento                  AS documentoEmpleado,
          (dp.primer_apellido || ' ' ||
           COALESCE(dp.segundo_apellido || ' ', '') ||
           dp.nombres)                  AS nombreEmpleado,

          c.fecha_inicio                AS fechaInicio,
          c.fecha_fin                   AS fechaFin,

          c.id_tipo_contrato            AS idTipoContrato,
          tc.nombre                     AS tipoContratoNombre,

          c.periodo_pago                AS periodoPago,

          c.id_seccion                  AS idSeccion,
          s.nombre_seccion              AS nombreSeccion,

          c.id_cargo                    AS idCargo,
          cg.nombre_cargo               AS nombreCargo,

          c.salario_base                AS salarioBase,
          c.salario_integral            AS salarioIntegral,

          c.id_eps                      AS idEps,
          eps.nombre_eps                AS nombreEps,

          c.id_afp                      AS idAfp,
          afp.nombre_afp                AS nombreAfp,

          c.id_cesantias                AS idCesantias,
          ces.nombre_cesantias          AS nombreCesantias,

          c.id_arl                      AS idArl,
          arl.nombre_arl                AS nombreArl,

          c.id_caja_compensacion        AS idCajaCompensacion,
          caja.nombre_caja              AS nombreCajaCompensacion,

          c.id_cuenta_ahorro_nomina     AS idCuentaAhorroNomina,

          -- ✅ CUENTA NÓMINA + FORMA (vista existente)
          dep.codigo_cuenta             AS cuentaNominaDisplay,
          dep.id_forma_ahorro           AS idFormaAhorroNomina,
          dep.codigo_forma              AS codigoFormaAhorroNomina,
          dep.nombre_forma_ahorro       AS nombreFormaAhorroNomina,

          c.fecha_envio_nota_renovacion AS fechaEnvioNotaRenovacion,
          c.clase_riesgo_arl            AS claseRiesgoArl,
          c.porcentaje_arl              AS porcentajeArl,

          c.activo                      AS activo

        FROM nomina.empleado_contratos c

        JOIN nomina.empleados e
          ON e.id_empleado = c.id_empleado

        JOIN hoja_vida.datos_personales dp
          ON dp.id_datos_personal = e.id_datos_personal

        LEFT JOIN nomina.secciones_nomina s
          ON s.id_seccion = c.id_seccion

        LEFT JOIN nomina.cargos cg
          ON cg.id_cargo = c.id_cargo

        LEFT JOIN nomina.tipos_contrato tc
          ON tc.id_tipo_contrato = c.id_tipo_contrato

        LEFT JOIN nomina.entidades_eps eps
          ON eps.id_eps = c.id_eps

        LEFT JOIN nomina.entidades_afp afp
          ON afp.id_afp = c.id_afp

        LEFT JOIN nomina.entidades_cesantias ces
          ON ces.id_cesantias = c.id_cesantias

        LEFT JOIN nomina.entidades_arl arl
          ON arl.id_arl = c.id_arl

        LEFT JOIN nomina.entidades_caja_compensacion caja
          ON caja.id_caja = c.id_caja_compensacion

        LEFT JOIN depositos.vw_depositos_cuentas_ahorro_detalle dep
          ON dep.id_cuenta_ahorro = c.id_cuenta_ahorro_nomina

        ORDER BY c.id_contrato DESC
    """;

    return jdbc.query(sql, (rs, rowNum) -> {

        Object idDpObj = rs.getObject("idDatosPersonal");
        Integer idDatosPersonal = (idDpObj == null) ? null : ((Number) idDpObj).intValue();

        return EmpleadoContratoListViewDTO.builder()
                .idContrato(rs.getInt("idContrato"))
                .idEmpleado(rs.getInt("idEmpleado"))

                // ✅ EMPLEADO
                .idDatosPersonal(idDatosPersonal)
                .documentoEmpleado(rs.getString("documentoEmpleado"))
                .nombreEmpleado(rs.getString("nombreEmpleado"))

                .fechaInicio(rs.getDate("fechaInicio").toLocalDate())
                .fechaFin(rs.getDate("fechaFin") != null ? rs.getDate("fechaFin").toLocalDate() : null)

                .idTipoContrato(rs.getObject("idTipoContrato", Integer.class))
                .tipoContratoNombre(rs.getString("tipoContratoNombre"))

                .periodoPago(rs.getString("periodoPago"))

                .idSeccion(rs.getObject("idSeccion", Integer.class))
                .nombreSeccion(rs.getString("nombreSeccion"))

                .idCargo(rs.getObject("idCargo", Integer.class))
                .nombreCargo(rs.getString("nombreCargo"))

                .salarioBase(rs.getBigDecimal("salarioBase"))
                .salarioIntegral(rs.getBoolean("salarioIntegral"))

                .idEps(rs.getObject("idEps", Integer.class))
                .nombreEps(rs.getString("nombreEps"))

                .idAfp(rs.getObject("idAfp", Integer.class))
                .nombreAfp(rs.getString("nombreAfp"))

                .idCesantias(rs.getObject("idCesantias", Integer.class))
                .nombreCesantias(rs.getString("nombreCesantias"))

                .idArl(rs.getObject("idArl", Integer.class))
                .nombreArl(rs.getString("nombreArl"))

                .idCajaCompensacion(rs.getObject("idCajaCompensacion", Integer.class))
                .nombreCajaCompensacion(rs.getString("nombreCajaCompensacion"))

                .idCuentaAhorroNomina(rs.getObject("idCuentaAhorroNomina", Long.class))
                .cuentaNominaDisplay(rs.getString("cuentaNominaDisplay"))
                .idFormaAhorroNomina(rs.getObject("idFormaAhorroNomina", Integer.class))
                .nombreFormaAhorroNomina(rs.getString("nombreFormaAhorroNomina"))

                .fechaEnvioNotaRenovacion(
                        rs.getDate("fechaEnvioNotaRenovacion") != null
                                ? rs.getDate("fechaEnvioNotaRenovacion").toLocalDate()
                                : null
                )

                .claseRiesgoArl(
                        rs.getObject("claseRiesgoArl") == null
                                ? null
                                : ((Number) rs.getObject("claseRiesgoArl")).shortValue()
                )
                .porcentajeArl(rs.getBigDecimal("porcentajeArl"))
                .activo(rs.getBoolean("activo"))
                .build();
        });
    }

    // ============================================================
    // LISTAR POR EMPLEADO
    // ============================================================
    public List<EmpleadoContratoListViewDTO> listarPorEmpleado(Integer idEmpleado) {

        String sql = """
        SELECT
          c.id_contrato                 AS idContrato,
          c.id_empleado                 AS idEmpleado,

          -- ✅ EMPLEADO
          e.id_datos_personal           AS idDatosPersonal,
          dp.documento                  AS documentoEmpleado,
          (dp.primer_apellido || ' ' ||
           COALESCE(dp.segundo_apellido || ' ', '') ||
           dp.nombres)                  AS nombreEmpleado,

          c.fecha_inicio                AS fechaInicio,
          c.fecha_fin                   AS fechaFin,

          c.id_tipo_contrato            AS idTipoContrato,
          tc.nombre                     AS tipoContratoNombre,

          c.periodo_pago                AS periodoPago,

          c.id_seccion                  AS idSeccion,
          s.nombre_seccion              AS nombreSeccion,

          c.id_cargo                    AS idCargo,
          cg.nombre_cargo               AS nombreCargo,

          c.salario_base                AS salarioBase,
          c.salario_integral            AS salarioIntegral,

          c.id_eps                      AS idEps,
          eps.nombre_eps                AS nombreEps,

          c.id_afp                      AS idAfp,
          afp.nombre_afp                AS nombreAfp,

          c.id_cesantias                AS idCesantias,
          ces.nombre_cesantias          AS nombreCesantias,

          c.id_arl                      AS idArl,
          arl.nombre_arl                AS nombreArl,

          c.id_caja_compensacion        AS idCajaCompensacion,
          caja.nombre_caja              AS nombreCajaCompensacion,

          c.id_cuenta_ahorro_nomina     AS idCuentaAhorroNomina,

          dep.codigo_cuenta             AS cuentaNominaDisplay,
          dep.id_forma_ahorro           AS idFormaAhorroNomina,
          dep.codigo_forma              AS codigoFormaAhorroNomina,
          dep.nombre_forma_ahorro       AS nombreFormaAhorroNomina,

          c.fecha_envio_nota_renovacion AS fechaEnvioNotaRenovacion,
          c.clase_riesgo_arl            AS claseRiesgoArl,
          c.porcentaje_arl              AS porcentajeArl,

          c.activo                      AS activo

        FROM nomina.empleado_contratos c

        JOIN nomina.empleados e
          ON e.id_empleado = c.id_empleado

        JOIN hoja_vida.datos_personales dp
          ON dp.id_datos_personal = e.id_datos_personal

        LEFT JOIN nomina.secciones_nomina s
          ON s.id_seccion = c.id_seccion

        LEFT JOIN nomina.cargos cg
          ON cg.id_cargo = c.id_cargo

        LEFT JOIN nomina.tipos_contrato tc
          ON tc.id_tipo_contrato = c.id_tipo_contrato

        LEFT JOIN nomina.entidades_eps eps
          ON eps.id_eps = c.id_eps

        LEFT JOIN nomina.entidades_afp afp
          ON afp.id_afp = c.id_afp

        LEFT JOIN nomina.entidades_cesantias ces
          ON ces.id_cesantias = c.id_cesantias

        LEFT JOIN nomina.entidades_arl arl
          ON arl.id_arl = c.id_arl

        LEFT JOIN nomina.entidades_caja_compensacion caja
          ON caja.id_caja = c.id_caja_compensacion

        LEFT JOIN depositos.vw_depositos_cuentas_ahorro_detalle dep
          ON dep.id_cuenta_ahorro = c.id_cuenta_ahorro_nomina

        WHERE c.id_empleado = :idEmpleado
            AND c.activo = TRUE
        ORDER BY c.fecha_inicio DESC
    """;

        var params = new MapSqlParameterSource().addValue("idEmpleado", idEmpleado);

        return jdbc.query(sql, params, (rs, rowNum) -> {

            Object idDpObj = rs.getObject("idDatosPersonal");
            Integer idDatosPersonal = (idDpObj == null) ? null : ((Number) idDpObj).intValue();

            return EmpleadoContratoListViewDTO.builder()
                    .idContrato(rs.getInt("idContrato"))
                    .idEmpleado(rs.getInt("idEmpleado"))

                    .idDatosPersonal(idDatosPersonal)
                    .documentoEmpleado(rs.getString("documentoEmpleado"))
                    .nombreEmpleado(rs.getString("nombreEmpleado"))

                    .fechaInicio(rs.getDate("fechaInicio").toLocalDate())
                    .fechaFin(rs.getDate("fechaFin") != null ? rs.getDate("fechaFin").toLocalDate() : null)

                    .idTipoContrato(rs.getObject("idTipoContrato", Integer.class))
                    .tipoContratoNombre(rs.getString("tipoContratoNombre"))

                    .periodoPago(rs.getString("periodoPago"))

                    .idSeccion(rs.getObject("idSeccion", Integer.class))
                    .nombreSeccion(rs.getString("nombreSeccion"))

                    .idCargo(rs.getObject("idCargo", Integer.class))
                    .nombreCargo(rs.getString("nombreCargo"))

                    .salarioBase(rs.getBigDecimal("salarioBase"))
                    .salarioIntegral(rs.getBoolean("salarioIntegral"))

                    .idEps(rs.getObject("idEps", Integer.class))
                    .nombreEps(rs.getString("nombreEps"))

                    .idAfp(rs.getObject("idAfp", Integer.class))
                    .nombreAfp(rs.getString("nombreAfp"))

                    .idCesantias(rs.getObject("idCesantias", Integer.class))
                    .nombreCesantias(rs.getString("nombreCesantias"))

                    .idArl(rs.getObject("idArl", Integer.class))
                    .nombreArl(rs.getString("nombreArl"))

                    .idCajaCompensacion(rs.getObject("idCajaCompensacion", Integer.class))
                    .nombreCajaCompensacion(rs.getString("nombreCajaCompensacion"))

                    .idCuentaAhorroNomina(rs.getObject("idCuentaAhorroNomina", Long.class))
                    .cuentaNominaDisplay(rs.getString("cuentaNominaDisplay"))
                    .idFormaAhorroNomina(rs.getObject("idFormaAhorroNomina", Integer.class))
                    .nombreFormaAhorroNomina(rs.getString("nombreFormaAhorroNomina"))

                    .fechaEnvioNotaRenovacion(
                            rs.getDate("fechaEnvioNotaRenovacion") != null
                                    ? rs.getDate("fechaEnvioNotaRenovacion").toLocalDate()
                                    : null
                    )

                    .claseRiesgoArl(
                            rs.getObject("claseRiesgoArl") == null
                                    ? null
                                    : ((Number) rs.getObject("claseRiesgoArl")).shortValue()
                    )
                    .porcentajeArl(rs.getBigDecimal("porcentajeArl"))
                    .activo(rs.getBoolean("activo"))
                    .build();
        });
    }

    // ============================================================
    // OBTENER
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
              c.id_cuenta_ahorro_nomina      AS idCuentaAhorroNomina,
              c.fecha_envio_nota_renovacion  AS fechaEnvioNotaRenovacion,
              c.clase_riesgo_arl             AS claseRiesgoArl,
              c.porcentaje_arl               AS porcentajeArl,
              c.activo                       AS activo,
              
              tc.aplica_salud              AS aplicaSalud,
              tc.aplica_pension            AS aplicaPension,
              tc.aplica_arl                AS aplicaArl,
              tc.aplica_caja_compensacion  AS aplicaCajaCompensacion,
              tc.aplica_cesantias          AS aplicaCesantias,
              tc.aplica_prima              AS aplicaPrima,
              tc.aplica_vacaciones         AS aplicaVacaciones,
              tc.aplica_parafiscales       AS aplicaParafiscales
              
            FROM nomina.empleado_contratos c
            LEFT JOIN nomina.tipos_contrato tc
              ON tc.id_tipo_contrato = c.id_tipo_contrato
            WHERE c.id_contrato = :id
        """;

        var params = new MapSqlParameterSource().addValue("id", idContrato);

        List<EmpleadoContratoDTO> rows = jdbc.query(sql, params, (rs, rowNum) ->
                EmpleadoContratoDTO.builder()
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
                        .idCuentaAhorroNomina((Long) rs.getObject("idCuentaAhorroNomina"))
                        .fechaEnvioNotaRenovacion(
                                rs.getDate("fechaEnvioNotaRenovacion") != null
                                        ? rs.getDate("fechaEnvioNotaRenovacion").toLocalDate()
                                        : null
                        )
                        .claseRiesgoArl(
                                rs.getObject("claseRiesgoArl") == null
                                        ? null
                                        : (short) rs.getInt("claseRiesgoArl")
                        )
                        .porcentajeArl(rs.getBigDecimal("porcentajeArl"))
                        .activo((Boolean) rs.getObject("activo"))

                        .aplicaSalud((Boolean) rs.getObject("aplicaSalud"))
                        .aplicaPension((Boolean) rs.getObject("aplicaPension"))
                        .aplicaArl((Boolean) rs.getObject("aplicaArl"))
                        .aplicaCajaCompensacion((Boolean) rs.getObject("aplicaCajaCompensacion"))
                        .aplicaCesantias((Boolean) rs.getObject("aplicaCesantias"))
                        .aplicaPrima((Boolean) rs.getObject("aplicaPrima"))
                        .aplicaVacaciones((Boolean) rs.getObject("aplicaVacaciones"))
                        .aplicaParafiscales((Boolean) rs.getObject("aplicaParafiscales"))

                        .build());

        return rows.stream().findFirst();
    }

    // ============================================================
    // CREAR
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
                .addValue("idCuentaAhorroNomina", dto.getIdCuentaAhorroNomina())
                .addValue("fechaEnvioNotaRenovacion",
                        dto.getFechaEnvioNotaRenovacion() != null
                                ? Date.valueOf(dto.getFechaEnvioNotaRenovacion())
                                : null)
                .addValue("claseRiesgoArl", dto.getClaseRiesgoArl() != null ? dto.getClaseRiesgoArl() : (short) 1)
                .addValue("porcentajeArl", dto.getPorcentajeArl() != null ? dto.getPorcentajeArl() : BigDecimal.ZERO)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ACTUALIZAR
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
                .addValue("idCuentaAhorroNomina", dto.getIdCuentaAhorroNomina())
                .addValue("fechaEnvioNotaRenovacion",
                        dto.getFechaEnvioNotaRenovacion() != null
                                ? Date.valueOf(dto.getFechaEnvioNotaRenovacion())
                                : null)
                .addValue("claseRiesgoArl", dto.getClaseRiesgoArl() != null ? dto.getClaseRiesgoArl() : (short) 1)
                .addValue("porcentajeArl", dto.getPorcentajeArl() != null ? dto.getPorcentajeArl() : BigDecimal.ZERO)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    public void eliminar(Integer idContrato) {

        String sql = """
            DELETE FROM nomina.empleado_contratos
            WHERE id_contrato = :id
        """;

        jdbc.update(sql, new MapSqlParameterSource().addValue("id", idContrato));
    }
}
