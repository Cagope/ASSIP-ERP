package co.assip.erp.nomina.vacaciones;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.vacaciones.dto.VacacionPreviewDTO;
import co.assip.erp.shared.math.MathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class VacacionesCalculator {

    private static final int DIAS_BASE_EMPRESA = 19;
    private static final BigDecimal PORCENTAJE_SALUD = new BigDecimal("0.04");
    private static final BigDecimal PORCENTAJE_PENSION = new BigDecimal("0.04");

    private final VacacionesRepository repo;

    public VacacionPreviewDTO calcular(
            EmpleadoContratoDTO contrato,
            LocalDate fechaLiquidacion
    ) {

        if (contrato == null || contrato.getIdContrato() == null) {
            throw new IllegalArgumentException("El contrato es obligatorio");
        }

        if (fechaLiquidacion == null) {
            throw new IllegalArgumentException("La fecha de liquidación es obligatoria");
        }

        if (contrato.getFechaInicio() == null) {
            throw new IllegalStateException("El contrato no tiene fecha de inicio");
        }

        if (contrato.getSalarioBase() == null) {
            throw new IllegalStateException("El contrato no tiene salario base");
        }

        int diasTrabajados = calcularDias360(
                contrato.getFechaInicio(),
                fechaLiquidacion
        );

        if (diasTrabajados < 0) {
            diasTrabajados = 0;
        }

        int diasVacaciones = calcularDiasVacaciones(diasTrabajados);

        LocalDate promedioDesde = LocalDate.of(
                fechaLiquidacion.getYear() - 1,
                1,
                1
        );

        LocalDate promedioHasta = LocalDate.of(
                fechaLiquidacion.getYear() - 1,
                12,
                31
        );

        BigDecimal sumaRecargos = repo.obtenerSumaRecargosDominicales(
                contrato.getIdContrato(),
                promedioDesde,
                promedioHasta
        );

        if (sumaRecargos == null) {
            sumaRecargos = BigDecimal.ZERO;
        }

        BigDecimal promedioRecargos = sumaRecargos
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        BigDecimal salarioBase = contrato.getSalarioBase();

        BigDecimal valorVacaciones = salarioBase
                .divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP)
                .add(promedioRecargos);

        BigDecimal valorPrimaVacaciones = salarioBase
                .divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(diasVacaciones));

        BigDecimal totalDevengado = valorVacaciones.add(valorPrimaVacaciones);

        BigDecimal salud = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(contrato.getAplicaSalud())) {
            salud = totalDevengado.multiply(PORCENTAJE_SALUD);
        }

        BigDecimal pension = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(contrato.getAplicaPension())) {
            pension = totalDevengado.multiply(PORCENTAJE_PENSION);
        }

        valorVacaciones = MathUtils.pesos(valorVacaciones);
        valorPrimaVacaciones = MathUtils.pesos(valorPrimaVacaciones);
        totalDevengado = MathUtils.pesos(totalDevengado);
        salud = MathUtils.pesos(salud);
        pension = MathUtils.pesos(pension);

        BigDecimal netoPagar = totalDevengado
                .subtract(salud)
                .subtract(pension);

        netoPagar = MathUtils.pesos(netoPagar);

        return VacacionPreviewDTO.builder()
                .idContrato(contrato.getIdContrato())
                .idEmpleado(contrato.getIdEmpleado())
                .idAgencia(repo.obtenerAgenciaPorContrato(contrato.getIdContrato()))
                .documentoEmpleado(contrato.getDocumentoEmpleado())
                .nombreEmpleado(contrato.getNombreEmpleado())
                .fechaInicioContrato(contrato.getFechaInicio())
                .fechaLiquidacion(fechaLiquidacion)
                .salarioBase(salarioBase)
                .diasTrabajados(diasTrabajados)
                .diasVacaciones(diasVacaciones)
                .promedioDesde(promedioDesde)
                .promedioHasta(promedioHasta)
                .promedioRecargosDominicales(promedioRecargos)
                .valorVacaciones(valorVacaciones)
                .valorPrimaVacaciones(valorPrimaVacaciones)
                .totalDevengado(totalDevengado)
                .salud(salud)
                .pension(pension)
                .netoPagar(netoPagar)
                .build();
    }

    private int calcularDiasVacaciones(int diasTrabajados) {
        if (diasTrabajados >= 360) {
            return DIAS_BASE_EMPRESA;
        }

        return (diasTrabajados * DIAS_BASE_EMPRESA) / 360;
    }

    private int calcularDias360(LocalDate desde, LocalDate hasta) {

        int d1 = Math.min(desde.getDayOfMonth(), 30);
        int d2 = Math.min(hasta.getDayOfMonth(), 30);

        int years = hasta.getYear() - desde.getYear();
        int months = hasta.getMonthValue() - desde.getMonthValue();
        int days = d2 - d1;

        return years * 360 + months * 30 + days;
    }
}