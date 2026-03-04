package co.assip.erp.nomina.desprendible;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DesprendibleRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR EMPLEADOS DEL PERÍODO (FRONT)
    // =========================================================
    public List<EmpleadoPeriodoRow> listarEmpleadosPeriodo(Integer idPeriodo) {

        String sql = """
            SELECT DISTINCT
                e.id_empleado,
                c.id_contrato,
                pr.documento,
                TRIM(
                  BOTH FROM CONCAT_WS(
                    ' ',
                    pr.primer_apellido,
                    pr.segundo_apellido,
                    pr.nombres
                  )
                ) AS nombre_completo,
                ca.nombre_cargo
            FROM nomina.liquidaciones l
            JOIN nomina.empleado_contratos c
              ON c.id_contrato = l.id_contrato
            LEFT JOIN nomina.empleados e
              ON e.id_empleado = c.id_empleado
             AND e.activo = TRUE
            LEFT JOIN shared.vw_personas_resumen pr
              ON pr.id_datos_personal = e.id_datos_personal
             AND pr.activo = TRUE
            LEFT JOIN nomina.cargos ca
              ON ca.id_cargo = c.id_cargo
            WHERE l.id_periodo_nomina = :idPeriodo
              AND l.estado = 'CERRADO'
            ORDER BY nombre_completo
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("idPeriodo", idPeriodo),
                (rs, rn) -> new EmpleadoPeriodoRow(
                        rs.getInt("id_empleado"),
                        rs.getInt("id_contrato"),
                        rs.getString("documento"),
                        rs.getString("nombre_completo"),
                        rs.getString("nombre_cargo")
                )
        );
    }

    // =========================================================
    // DETALLE: NOVEDADES CERRADOS
    // =========================================================
    public List<DesprendibleRow> obtenerConceptosCerrados(
            Integer idPeriodo,
            Integer idContrato
    ) {

        String sql = """
            SELECT
                n.codigo_concepto,
                c.nombre_concepto,
                c.tipo_concepto,
                n.cantidad,
                n.valor
            FROM nomina.novedades_nomina n
            JOIN nomina.conceptos_nomina c
              ON c.codigo_concepto = n.codigo_concepto
            WHERE n.id_periodo = :idPeriodo
              AND n.id_contrato = :idContrato
              AND n.estado = 'CERRADO'
            ORDER BY n.codigo_concepto
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodo", idPeriodo)
                        .addValue("idContrato", idContrato),
                (rs, rn) -> new DesprendibleRow(
                        rs.getString("codigo_concepto"),
                        rs.getString("nombre_concepto"),
                        rs.getString("tipo_concepto"),
                        rs.getBigDecimal("cantidad"),
                        rs.getBigDecimal("valor")
                )
        );
    }

    // =========================================================
    // ENCABEZADO PERÍODO
    // =========================================================
    public PeriodoRow obtenerPeriodo(Integer idPeriodo) {

        String sql = """
            SELECT
                id_periodo,
                fecha_inicio,
                fecha_fin,
                descripcion
            FROM nomina.periodos_nomina
            WHERE id_periodo = :idPeriodo
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idPeriodo", idPeriodo),
                (rs, rn) -> new PeriodoRow(
                        rs.getInt("id_periodo"),
                        rs.getDate("fecha_inicio").toLocalDate(),
                        rs.getDate("fecha_fin").toLocalDate(),
                        rs.getString("descripcion")
                )
        );
    }

    // =========================================================
    // ENCABEZADO EMPLEADO / CONTRATO
    // =========================================================
    public EmpleadoContratoRow obtenerEmpleadoContrato(Integer idContrato) {

        String sql = """
            SELECT
                c.id_contrato,
                c.id_empleado,
                c.salario_base,
                ca.nombre_cargo,
                pr.documento,
                TRIM(
                  BOTH FROM CONCAT_WS(
                    ' ',
                    pr.primer_apellido,
                    pr.segundo_apellido,
                    pr.nombres
                  )
                ) AS nombre_completo
            FROM nomina.empleado_contratos c
            LEFT JOIN nomina.empleados e
              ON e.id_empleado = c.id_empleado
            LEFT JOIN shared.vw_personas_resumen pr
              ON pr.id_datos_personal = e.id_datos_personal
             AND pr.activo = TRUE
            LEFT JOIN nomina.cargos ca
              ON ca.id_cargo = c.id_cargo
            WHERE c.id_contrato = :idContrato
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idContrato", idContrato),
                (rs, rn) -> new EmpleadoContratoRow(
                        rs.getInt("id_empleado"),
                        rs.getString("documento"),
                        rs.getString("nombre_completo"),
                        rs.getInt("id_contrato"),
                        rs.getBigDecimal("salario_base"),
                        rs.getString("nombre_cargo")
                )
        );
    }

    // =========================================================
    // RECORDS
    // =========================================================
    public record EmpleadoPeriodoRow(
            Integer idEmpleado,
            Integer idContrato,
            String documento,
            String nombreCompleto,
            String cargo
    ) {}

    public record DesprendibleRow(
            String codigoConcepto,
            String nombreConcepto,
            String tipoConcepto,
            BigDecimal cantidad,
            BigDecimal valor
    ) {}

    public record PeriodoRow(
            Integer idPeriodo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String descripcion
    ) {}

    public record EmpleadoContratoRow(
            Integer idEmpleado,
            String documento,
            String nombreCompleto,
            Integer idContrato,
            BigDecimal salarioBase,
            String cargo
    ) {}
}