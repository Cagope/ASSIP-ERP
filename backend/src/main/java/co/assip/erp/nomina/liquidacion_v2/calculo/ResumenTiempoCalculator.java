package co.assip.erp.nomina.liquidacion_v2.calculo;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.ResumenTiempoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ResumenTiempoCalculator {

    private static final Integer ID_TIPO_CONTRATO_APRENDIZ_SENA = 3;

    private final NamedParameterJdbcTemplate jdbc;

    public ResumenTiempoDTO calcular(
            Integer idPeriodo,
            EmpleadoContratoDTO contrato
    ) {

        if (idPeriodo == null) {
            throw new IllegalArgumentException("El idPeriodo es obligatorio");
        }

        if (contrato == null || contrato.getIdContrato() == null) {
            throw new IllegalArgumentException("El contrato es obligatorio");
        }

        Integer diasPeriodo = obtenerDiasPeriodo(idPeriodo, contrato);

        Map<String, BigDecimal> mapa = obtenerNovedadesTiempo(
                idPeriodo,
                contrato.getIdContrato()
        );

        BigDecimal diasVacaciones =
                mapa.getOrDefault("VACACIONES", BigDecimal.ZERO);

        BigDecimal diasIncapacidad =
                mapa.getOrDefault("INCAPACIDAD", BigDecimal.ZERO);

        BigDecimal diasPermisoRemunerado =
                mapa.getOrDefault("PERMISO_REMUNERADO", BigDecimal.ZERO);

        BigDecimal diasPermisoNoRemunerado =
                mapa.getOrDefault("PERMISO_NO_REMUNERADO", BigDecimal.ZERO);

        BigDecimal diasLaborados = BigDecimal.valueOf(diasPeriodo)
                .subtract(diasVacaciones)
                .subtract(diasIncapacidad)
                .subtract(diasPermisoRemunerado)
                .subtract(diasPermisoNoRemunerado);

        if (diasLaborados.compareTo(BigDecimal.ZERO) < 0) {
            diasLaborados = BigDecimal.ZERO;
        }

        return ResumenTiempoDTO.builder()
                .diasPeriodo(diasPeriodo)
                .diasVacaciones(diasVacaciones)
                .diasIncapacidad(diasIncapacidad)
                .diasPermisoRemunerado(diasPermisoRemunerado)
                .diasPermisoNoRemunerado(diasPermisoNoRemunerado)
                .diasLaborados(diasLaborados)
                .build();
    }

    private Integer obtenerDiasPeriodo(
            Integer idPeriodo,
            EmpleadoContratoDTO contrato
    ) {

        if (contrato == null) {
            return 30;
        }

        PeriodoInfo periodo = obtenerPeriodoInfo(idPeriodo);
        if (periodo == null) {
            return 30;
        }

        String tipoPeriodo = periodo.tipoPeriodo() != null
                ? periodo.tipoPeriodo().trim().toUpperCase()
                : "";

        Integer numeroPeriodo = periodo.numeroPeriodo();

        // =====================================================
        // REGLA ESPECIAL: APRENDIZ SENA
        // - Q1: no liquida
        // - Q2: liquida el mes completo
        // =====================================================
        if (ID_TIPO_CONTRATO_APRENDIZ_SENA.equals(contrato.getIdTipoContrato())) {

            if ("QUINCENAL".equals(tipoPeriodo)) {
                if (numeroPeriodo != null && numeroPeriodo == 1) {
                    return 0;
                }
                if (numeroPeriodo != null && numeroPeriodo == 2) {
                    return 30;
                }
            }

            // Si por alguna razón el período no viene quincenal,
            // mantenemos pago mensual estándar.
            return 30;
        }

        // =====================================================
        // REGLA NORMAL
        // =====================================================
        return switch (tipoPeriodo) {
            case "QUINCENAL" -> 15;
            case "MENSUAL" -> 30;
            default -> 30;
        };
    }

    private PeriodoInfo obtenerPeriodoInfo(Integer idPeriodo) {

        String sql = """
            SELECT
              p.tipo_periodo,
              p.numero_periodo
            FROM nomina.periodos_nomina p
            WHERE p.id_periodo = :idPeriodo
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("idPeriodo", idPeriodo),
                rs -> {
                    if (!rs.next()) return null;

                    return new PeriodoInfo(
                            rs.getString("tipo_periodo"),
                            (Integer) rs.getObject("numero_periodo")
                    );
                }
        );
    }

    private Map<String, BigDecimal> obtenerNovedadesTiempo(
            Integer idPeriodo,
            Integer idContrato
    ) {

        String sql = """
            SELECT
              r.clase_novedad,
              COALESCE(SUM(n.cantidad), 0) AS dias
            FROM nomina.novedades_nomina n
            JOIN nomina.reglas_novedades_tiempo r
              ON r.codigo_concepto = n.codigo_concepto
            WHERE n.id_periodo = :idPeriodo
              AND n.id_contrato = :idContrato
              AND n.estado = 'ABIERTO'
              AND r.activo = TRUE
            GROUP BY r.clase_novedad
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodo)
                .addValue("idContrato", idContrato);

        Map<String, BigDecimal> map = new HashMap<>();

        jdbc.query(sql, params, rs -> {
            String clase = rs.getString("clase_novedad");
            BigDecimal dias = rs.getBigDecimal("dias");

            map.put(
                    clase,
                    dias != null ? dias : BigDecimal.ZERO
            );
        });

        return map;
    }

    private record PeriodoInfo(
            String tipoPeriodo,
            Integer numeroPeriodo
    ) {}
}