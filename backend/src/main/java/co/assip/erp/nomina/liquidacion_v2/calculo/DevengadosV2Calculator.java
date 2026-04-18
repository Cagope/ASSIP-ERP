package co.assip.erp.nomina.liquidacion_v2.calculo;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.calculo.engine.BaseCalculo;
import co.assip.erp.nomina.liquidacion.calculo.engine.ContextoCalculo;
import co.assip.erp.nomina.liquidacion.calculo.engine.MotorCalculoConcepto;
import co.assip.erp.nomina.liquidacion.calculo.engine.TipoCalculo;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.ResumenTiempoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DevengadosV2Calculator {

    private final NamedParameterJdbcTemplate jdbc;
    private final ResumenTiempoCalculator resumenTiempoCalculator;

    public List<LiquidacionDetalleDTO> calcular(
            Integer idPeriodoNomina,
            EmpleadoContratoDTO contrato
    ) {

        List<LiquidacionDetalleDTO> detalles = new ArrayList<>();

        ResumenTiempoDTO tiempo = resumenTiempoCalculator.calcular(
                idPeriodoNomina,
                contrato
        );

        ContextoCalculo ctx = new ContextoCalculo();

        BigDecimal salarioBase = contrato.getSalarioBase() != null
                ? contrato.getSalarioBase()
                : BigDecimal.ZERO;

        ctx.put(BaseCalculo.SALARIO_BASE, salarioBase);

        Set<String> codigosManuales = obtenerCodigosDevengadoManual(
                idPeriodoNomina,
                contrato.getIdContrato()
        );

        // 1) BASICO por días laborados
        if (!codigosManuales.contains("BASICO")
                && tiempo.getDiasLaboradosSafe().compareTo(BigDecimal.ZERO) > 0) {

            MotorCalculoConcepto.ResultadoCalculo r =
                    MotorCalculoConcepto.calcular(
                            TipoCalculo.POR_DIAS,
                            BaseCalculo.SALARIO_BASE,
                            BigDecimal.ONE,
                            tiempo.getDiasLaboradosSafe(),
                            ctx.asMap()
                    );

            detalles.add(
                    LiquidacionDetalleDTO.devengadoAutomatico(
                            "BASICO",
                            r.cantidad(),
                            r.valorUnitario(),
                            r.valorTotal(),
                            r.baseCalculo()
                    )
            );
        }

        // 2) AUXILIO DE TRANSPORTE
        if (!codigosManuales.contains("AUX_TRANSP")
                && tiempo.getDiasLaboradosSafe().compareTo(BigDecimal.ZERO) > 0
                && !Boolean.FALSE.equals(contrato.getAplicaAuxTransporte())) {

            LiquidacionDetalleDTO auxTransporte =
                    calcularAuxilioTransporte(
                            contrato,
                            tiempo.getDiasLaboradosSafe()
                    );

            if (auxTransporte != null) {
                detalles.add(auxTransporte);
            }
        }

        // 3) VACACIONES
        // No se liquidan aquí.
        // Solo afectan los días laborados porque se pagan en un proceso aparte.

        // 4) INCAPACIDAD
        if (tiempo.getDiasIncapacidadSafe().compareTo(BigDecimal.ZERO) > 0) {
            detalles.add(
                    calcularPorDias(
                            "INCAPACIDAD",
                            tiempo.getDiasIncapacidadSafe(),
                            ctx
                    )
            );
        }

        // 5) PERMISO REMUNERADO
        if (tiempo.getDiasPermisoRemuneradoSafe().compareTo(BigDecimal.ZERO) > 0) {
            detalles.add(
                    calcularPorDias(
                            "PERMISO_REM",
                            tiempo.getDiasPermisoRemuneradoSafe(),
                            ctx
                    )
            );
        }

        // 6) NOVEDADES DEVENGADO ADICIONALES
        detalles.addAll(
                obtenerNovedadesDevengado(
                        idPeriodoNomina,
                        contrato.getIdContrato()
                )
        );

        return detalles;
    }

    private LiquidacionDetalleDTO calcularPorDias(
            String codigo,
            BigDecimal dias,
            ContextoCalculo ctx
    ) {

        MotorCalculoConcepto.ResultadoCalculo r =
                MotorCalculoConcepto.calcular(
                        TipoCalculo.POR_DIAS,
                        BaseCalculo.SALARIO_BASE,
                        BigDecimal.ONE,
                        dias,
                        ctx.asMap()
                );

        return LiquidacionDetalleDTO.devengadoAutomatico(
                codigo,
                r.cantidad(),
                r.valorUnitario(),
                r.valorTotal(),
                r.baseCalculo()
        );
    }

    private LiquidacionDetalleDTO calcularAuxilioTransporte(
            EmpleadoContratoDTO contrato,
            BigDecimal diasLaborados
    ) {

        if (contrato == null || contrato.getSalarioBase() == null) {
            return null;
        }

        VariableAuxilioTransporteDTO variable = obtenerAuxilioTransporteVigente();

        if (variable == null) {
            return null;
        }

        if (variable.smmlv() == null || variable.smmlv().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        if (variable.auxTransporte() == null || variable.auxTransporte().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        if (variable.diasMes() == null || variable.diasMes().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal tope = variable.smmlv().multiply(BigDecimal.valueOf(2));

        // Regla actual: solo si salario base < 2 SMMLV
        if (contrato.getSalarioBase().compareTo(tope) >= 0) {
            return null;
        }

        BigDecimal valorUnitario = variable.auxTransporte()
                .divide(variable.diasMes(), 8, RoundingMode.HALF_UP);

        BigDecimal valorTotal = valorUnitario.multiply(diasLaborados);

        return LiquidacionDetalleDTO.devengadoAutomatico(
                "AUX_TRANSP",
                diasLaborados,
                valorUnitario,
                valorTotal,
                variable.auxTransporte()
        );
    }

    private VariableAuxilioTransporteDTO obtenerAuxilioTransporteVigente() {

        String sql = """
            SELECT
              v.smmlv,
              v.aux_transporte,
              v.dias_mes
            FROM nomina.variables_vigencia v
            WHERE v.activo = TRUE
            ORDER BY v.fecha_inicial DESC
            LIMIT 1
        """;

        List<VariableAuxilioTransporteDTO> lista = jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (rs, rowNum) -> new VariableAuxilioTransporteDTO(
                        rs.getBigDecimal("smmlv"),
                        rs.getBigDecimal("aux_transporte"),
                        rs.getBigDecimal("dias_mes")
                )
        );

        return lista.isEmpty() ? null : lista.get(0);
    }

    private Set<String> obtenerCodigosDevengadoManual(
            Integer idPeriodoNomina,
            Integer idContrato
    ) {

        String sql = """
            SELECT DISTINCT n.codigo_concepto
            FROM nomina.novedades_nomina n
            JOIN nomina.conceptos_nomina c
              ON c.codigo_concepto = n.codigo_concepto
            WHERE n.id_periodo = :idPeriodo
              AND n.id_contrato = :idContrato
              AND n.estado = 'ABIERTO'
              AND c.tipo_concepto = 'DEVENGADO'
              AND n.codigo_concepto IN ('BASICO', 'AUX_TRANSP')
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodoNomina)
                .addValue("idContrato", idContrato);

        Set<String> codigos = new HashSet<>();

        jdbc.query(sql, params, (rs) -> {
            while (rs.next()) {
                codigos.add(rs.getString("codigo_concepto"));
            }
            return null;
        });

        return codigos;
    }

    private List<LiquidacionDetalleDTO> obtenerNovedadesDevengado(
            Integer idPeriodoNomina,
            Integer idContrato
    ) {

        String sql = """
            SELECT
              n.id_novedad,
              n.codigo_concepto,
              n.cantidad,
              n.valor
            FROM nomina.novedades_nomina n
            JOIN nomina.conceptos_nomina c
              ON c.codigo_concepto = n.codigo_concepto
            WHERE n.id_periodo = :idPeriodo
              AND n.id_contrato = :idContrato
              AND n.estado = 'ABIERTO'
              AND c.tipo_concepto = 'DEVENGADO'
              AND n.codigo_concepto NOT IN (
                  'VACACIONES',
                  'INCAPACIDAD',
                  'PERMISO_REM',
                  'PERMISO_NO_REM'
              )
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodoNomina)
                .addValue("idContrato", idContrato);

        List<LiquidacionDetalleDTO> list = new ArrayList<>();

        jdbc.query(sql, params, rs -> {

            BigDecimal cantidad = rs.getBigDecimal("cantidad");
            if (cantidad == null || cantidad.signum() <= 0) {
                cantidad = BigDecimal.ONE;
            }

            BigDecimal total = rs.getBigDecimal("valor");
            if (total == null) {
                total = BigDecimal.ZERO;
            }

            total = (total);

            BigDecimal valorUnitario =

                            total.divide(cantidad, 8, RoundingMode.HALF_UP
                    );

            list.add(
                    LiquidacionDetalleDTO.devengadoNovedad(
                            rs.getString("codigo_concepto"),
                            cantidad,
                            valorUnitario,
                            total,
                            total,
                            rs.getInt("id_novedad")
                    )
            );
        });

        return list;
    }

    private record VariableAuxilioTransporteDTO(
            BigDecimal smmlv,
            BigDecimal auxTransporte,
            BigDecimal diasMes
    ) {}

}