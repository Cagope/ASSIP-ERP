package co.assip.erp.nomina.variables_vigencia;

import co.assip.erp.nomina.variables_vigencia.dto.VariablesVigenciaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VariablesVigenciaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    public List<VariablesVigenciaDTO> listar() {

        String sql = """
            SELECT
              v.id_variable                       AS idVariable,
              v.fecha_inicial                     AS fechaInicial,
              v.fecha_final                       AS fechaFinal,
              v.smmlv                             AS smmlv,
              v.aux_transporte                    AS auxTransporte,
              v.porc_salud_empleado               AS porcSaludEmpleado,
              v.porc_salud_empleador              AS porcSaludEmpleador,
              v.porc_pension_empleado             AS porcPensionEmpleado,
              v.porc_pension_empleador            AS porcPensionEmpleador,
              v.porc_caja_compensacion            AS porcCajaCompensacion,
              v.porc_sena                         AS porcSena,
              v.porc_icbf                         AS porcIcbf,
              v.por_provision_prima               AS porProvisionPrima,
              v.por_provision_vacaciones          AS porProvisionVacaciones,
              v.por_provision_cesantias           AS porProvisionCesantias,
              v.por_provision_interes_cesantias   AS porProvisionInteresCesantias,
              v.tope_ibc_min_smmlv                AS topeIbcMinSmmlv,
              v.tope_ibc_max_smmlv                AS topeIbcMaxSmmlv,
              v.exonerado_salud                   AS exoneradoSalud,
              v.exonerado_parafiscales            AS exoneradoParafiscales,
              v.activo                            AS activo
            FROM nomina.variables_vigencia v
            ORDER BY v.fecha_inicial DESC, v.id_variable DESC
        """;

        return jdbc.query(sql, (rs, rowNum) -> VariablesVigenciaDTO.builder()
                .idVariable(rs.getInt("idVariable"))
                .fechaInicial(rs.getObject("fechaInicial", java.time.LocalDate.class))
                .fechaFinal(rs.getObject("fechaFinal", java.time.LocalDate.class))
                .smmlv(rs.getBigDecimal("smmlv"))
                .auxTransporte(rs.getBigDecimal("auxTransporte"))
                .porcSaludEmpleado(rs.getBigDecimal("porcSaludEmpleado"))
                .porcSaludEmpleador(rs.getBigDecimal("porcSaludEmpleador"))
                .porcPensionEmpleado(rs.getBigDecimal("porcPensionEmpleado"))
                .porcPensionEmpleador(rs.getBigDecimal("porcPensionEmpleador"))
                .porcCajaCompensacion(rs.getBigDecimal("porcCajaCompensacion"))
                .porcSena(rs.getBigDecimal("porcSena"))
                .porcIcbf(rs.getBigDecimal("porcIcbf"))
                .porProvisionPrima(rs.getBigDecimal("porProvisionPrima"))
                .porProvisionVacaciones(rs.getBigDecimal("porProvisionVacaciones"))
                .porProvisionCesantias(rs.getBigDecimal("porProvisionCesantias"))
                .porProvisionInteresCesantias(rs.getBigDecimal("porProvisionInteresCesantias"))
                .topeIbcMinSmmlv(rs.getBigDecimal("topeIbcMinSmmlv"))
                .topeIbcMaxSmmlv(rs.getBigDecimal("topeIbcMaxSmmlv"))
                .exoneradoSalud((Boolean) rs.getObject("exoneradoSalud"))
                .exoneradoParafiscales((Boolean) rs.getObject("exoneradoParafiscales"))
                .activo((Boolean) rs.getObject("activo"))
                .build());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public Optional<VariablesVigenciaDTO> obtener(Integer id) {

        String sql = """
            SELECT
              v.id_variable                       AS idVariable,
              v.fecha_inicial                     AS fechaInicial,
              v.fecha_final                       AS fechaFinal,
              v.smmlv                             AS smmlv,
              v.aux_transporte                    AS auxTransporte,
              v.porc_salud_empleado               AS porcSaludEmpleado,
              v.porc_salud_empleador              AS porcSaludEmpleador,
              v.porc_pension_empleado             AS porcPensionEmpleado,
              v.porc_pension_empleador            AS porcPensionEmpleador,
              v.porc_caja_compensacion            AS porcCajaCompensacion,
              v.porc_sena                         AS porcSena,
              v.porc_icbf                         AS porcIcbf,
              v.por_provision_prima               AS porProvisionPrima,
              v.por_provision_vacaciones          AS porProvisionVacaciones,
              v.por_provision_cesantias           AS porProvisionCesantias,
              v.por_provision_interes_cesantias   AS porProvisionInteresCesantias,
              v.tope_ibc_min_smmlv                AS topeIbcMinSmmlv,
              v.tope_ibc_max_smmlv                AS topeIbcMaxSmmlv,
              v.exonerado_salud                   AS exoneradoSalud,
              v.exonerado_parafiscales            AS exoneradoParafiscales,
              v.activo                            AS activo
            FROM nomina.variables_vigencia v
            WHERE v.id_variable = :id
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", id);

        List<VariablesVigenciaDTO> rows = jdbc.query(sql, params, (rs, rowNum) ->
                VariablesVigenciaDTO.builder()
                        .idVariable(rs.getInt("idVariable"))
                        .fechaInicial(rs.getObject("fechaInicial", java.time.LocalDate.class))
                        .fechaFinal(rs.getObject("fechaFinal", java.time.LocalDate.class))
                        .smmlv(rs.getBigDecimal("smmlv"))
                        .auxTransporte(rs.getBigDecimal("auxTransporte"))
                        .porcSaludEmpleado(rs.getBigDecimal("porcSaludEmpleado"))
                        .porcSaludEmpleador(rs.getBigDecimal("porcSaludEmpleador"))
                        .porcPensionEmpleado(rs.getBigDecimal("porcPensionEmpleado"))
                        .porcPensionEmpleador(rs.getBigDecimal("porcPensionEmpleador"))
                        .porcCajaCompensacion(rs.getBigDecimal("porcCajaCompensacion"))
                        .porcSena(rs.getBigDecimal("porcSena"))
                        .porcIcbf(rs.getBigDecimal("porcIcbf"))
                        .porProvisionPrima(rs.getBigDecimal("porProvisionPrima"))
                        .porProvisionVacaciones(rs.getBigDecimal("porProvisionVacaciones"))
                        .porProvisionCesantias(rs.getBigDecimal("porProvisionCesantias"))
                        .porProvisionInteresCesantias(rs.getBigDecimal("porProvisionInteresCesantias"))
                        .topeIbcMinSmmlv(rs.getBigDecimal("topeIbcMinSmmlv"))
                        .topeIbcMaxSmmlv(rs.getBigDecimal("topeIbcMaxSmmlv"))
                        .exoneradoSalud((Boolean) rs.getObject("exoneradoSalud"))
                        .exoneradoParafiscales((Boolean) rs.getObject("exoneradoParafiscales"))
                        .activo((Boolean) rs.getObject("activo"))
                        .build()
        );

        return rows.stream().findFirst();
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(VariablesVigenciaDTO dto, Integer idUsuario) {

        String sql = """
            INSERT INTO nomina.variables_vigencia (
              fecha_inicial,
              fecha_final,
              smmlv,
              aux_transporte,
              porc_salud_empleado,
              porc_salud_empleador,
              porc_pension_empleado,
              porc_pension_empleador,
              porc_caja_compensacion,
              porc_sena,
              porc_icbf,
              por_provision_prima,
              por_provision_vacaciones,
              por_provision_cesantias,
              por_provision_interes_cesantias,
              tope_ibc_min_smmlv,
              tope_ibc_max_smmlv,
              exonerado_salud,
              exonerado_parafiscales,
              activo,
              fk_seguridad_creacion,
              fk_seguridad_edicion
            )
            VALUES (
              :fechaInicial,
              :fechaFinal,
              :smmlv,
              :auxTransporte,
              :porcSaludEmpleado,
              :porcSaludEmpleador,
              :porcPensionEmpleado,
              :porcPensionEmpleador,
              :porcCajaCompensacion,
              :porcSena,
              :porcIcbf,
              :porProvisionPrima,
              :porProvisionVacaciones,
              :porProvisionCesantias,
              :porProvisionInteresCesantias,
              :topeIbcMinSmmlv,
              :topeIbcMaxSmmlv,
              :exoneradoSalud,
              :exoneradoParafiscales,
              :activo,
              :usr,
              :usr
            )
            RETURNING id_variable
        """;

        var params = params(dto, idUsuario);

        return jdbc.queryForObject(sql, params, Integer.class);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer id, VariablesVigenciaDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.variables_vigencia
            SET
              fecha_inicial = :fechaInicial,
              fecha_final = :fechaFinal,
              smmlv = :smmlv,
              aux_transporte = :auxTransporte,
              porc_salud_empleado = :porcSaludEmpleado,
              porc_salud_empleador = :porcSaludEmpleador,
              porc_pension_empleado = :porcPensionEmpleado,
              porc_pension_empleador = :porcPensionEmpleador,
              porc_caja_compensacion = :porcCajaCompensacion,
              porc_sena = :porcSena,
              porc_icbf = :porcIcbf,
              por_provision_prima = :porProvisionPrima,
              por_provision_vacaciones = :porProvisionVacaciones,
              por_provision_cesantias = :porProvisionCesantias,
              por_provision_interes_cesantias = :porProvisionInteresCesantias,
              tope_ibc_min_smmlv = :topeIbcMinSmmlv,
              tope_ibc_max_smmlv = :topeIbcMaxSmmlv,
              exonerado_salud = :exoneradoSalud,
              exonerado_parafiscales = :exoneradoParafiscales,
              activo = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_variable = :id
        """;

        var params = params(dto, idUsuario).addValue("id", id);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer id) {

        String sql = """
            DELETE FROM nomina.variables_vigencia
            WHERE id_variable = :id
        """;

        var params = new MapSqlParameterSource().addValue("id", id);

        jdbc.update(sql, params);
    }

    private MapSqlParameterSource params(VariablesVigenciaDTO dto, Integer idUsuario) {
        return new MapSqlParameterSource()
                .addValue("fechaInicial", dto.getFechaInicial())
                .addValue("fechaFinal", dto.getFechaFinal())
                .addValue("smmlv", n(dto.getSmmlv()))
                .addValue("auxTransporte", n(dto.getAuxTransporte()))
                .addValue("porcSaludEmpleado", n(dto.getPorcSaludEmpleado()))
                .addValue("porcSaludEmpleador", n(dto.getPorcSaludEmpleador()))
                .addValue("porcPensionEmpleado", n(dto.getPorcPensionEmpleado()))
                .addValue("porcPensionEmpleador", n(dto.getPorcPensionEmpleador()))
                .addValue("porcCajaCompensacion", n(dto.getPorcCajaCompensacion()))
                .addValue("porcSena", n(dto.getPorcSena()))
                .addValue("porcIcbf", n(dto.getPorcIcbf()))
                .addValue("porProvisionPrima", n(dto.getPorProvisionPrima()))
                .addValue("porProvisionVacaciones", n(dto.getPorProvisionVacaciones()))
                .addValue("porProvisionCesantias", n(dto.getPorProvisionCesantias()))
                .addValue("porProvisionInteresCesantias", n(dto.getPorProvisionInteresCesantias()))
                .addValue("topeIbcMinSmmlv", n(dto.getTopeIbcMinSmmlv()))
                .addValue("topeIbcMaxSmmlv", n(dto.getTopeIbcMaxSmmlv()))
                .addValue("exoneradoSalud", dto.getExoneradoSalud() != null ? dto.getExoneradoSalud() : Boolean.FALSE)
                .addValue("exoneradoParafiscales", dto.getExoneradoParafiscales() != null ? dto.getExoneradoParafiscales() : Boolean.FALSE)
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario != null ? idUsuario : 1);
    }

    private BigDecimal n(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
