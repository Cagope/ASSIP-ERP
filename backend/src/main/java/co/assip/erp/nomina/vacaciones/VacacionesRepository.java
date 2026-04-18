package co.assip.erp.nomina.vacaciones;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class VacacionesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EmpleadoContratoDTO obtenerContrato(Integer idContrato) {

        String sql = """
            SELECT
                c.id_contrato,
                c.id_empleado,
                e.id_agencia,
                e.id_datos_personal,
                c.fecha_inicio,
                c.fecha_fin,
                c.salario_base,
                c.salario_integral,
                c.periodo_pago,
                c.id_eps,
                c.id_afp,
                c.activo,

                dp.documento,
                CASE
                    WHEN COALESCE(dp.tipo_persona, '1') = '2'
                        THEN COALESCE(dp.nombres, '')
                    ELSE TRIM(
                        COALESCE(dp.primer_apellido, '') || ' ' ||
                        COALESCE(dp.segundo_apellido, '') || ' ' ||
                        COALESCE(dp.nombres, '')
                    )
                END AS nombre_empleado
            FROM nomina.empleado_contratos c
            JOIN nomina.empleados e
              ON e.id_empleado = c.id_empleado
            JOIN hoja_vida.datos_personales dp
              ON dp.id_datos_personal = e.id_datos_personal
            WHERE c.id_contrato = :idContrato
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("idContrato", idContrato),
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }

                    return EmpleadoContratoDTO.builder()
                            .idContrato(rs.getInt("id_contrato"))
                            .idEmpleado(rs.getInt("id_empleado"))
                            .idDatosPersonal(rs.getLong("id_datos_personal"))
                            .fechaInicio(rs.getObject("fecha_inicio", LocalDate.class))
                            .fechaFin(rs.getObject("fecha_fin", LocalDate.class))
                            .salarioBase(rs.getBigDecimal("salario_base"))
                            .salarioIntegral((Boolean) rs.getObject("salario_integral"))
                            .periodoPago(rs.getString("periodo_pago"))
                            .idEps((Integer) rs.getObject("id_eps"))
                            .idAfp((Integer) rs.getObject("id_afp"))
                            .activo((Boolean) rs.getObject("activo"))
                            .documentoEmpleado(rs.getString("documento"))
                            .nombreEmpleado(rs.getString("nombre_empleado"))
                            .aplicaSalud(rs.getObject("id_eps") != null)
                            .aplicaPension(rs.getObject("id_afp") != null)
                            .build();
                }
        );
    }

    public Integer obtenerAgenciaPorContrato(Integer idContrato) {

        String sql = """
            SELECT e.id_agencia
            FROM nomina.empleado_contratos c
            JOIN nomina.empleados e
              ON e.id_empleado = c.id_empleado
            WHERE c.id_contrato = :idContrato
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idContrato", idContrato),
                Integer.class
        );
    }

    public BigDecimal obtenerSumaRecargosDominicales(
            Integer idContrato,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        String sql = """
            SELECT COALESCE(SUM(d.valor_total), 0)
            FROM nomina.liquidacion_detalle d
            JOIN nomina.liquidaciones l
              ON l.id_liquidacion = d.id_liquidacion
            WHERE l.id_contrato = :idContrato
              AND d.codigo_concepto = 'RECARGO_DOM'
              AND l.fecha_creacion::date BETWEEN :fechaInicio AND :fechaFin
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idContrato", idContrato)
                        .addValue("fechaInicio", fechaInicio)
                        .addValue("fechaFin", fechaFin),
                BigDecimal.class
        );
    }
}