package co.assip.erp.nomina.vacaciones.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacacionPreviewDTO {

    private Integer idContrato;
    private Integer idEmpleado;
    private Integer idAgencia;

    private String documentoEmpleado;
    private String nombreEmpleado;

    private LocalDate fechaInicioContrato;
    private LocalDate fechaLiquidacion;

    private BigDecimal salarioBase;

    private Integer diasTrabajados;
    private Integer diasVacaciones;

    private LocalDate promedioDesde;
    private LocalDate promedioHasta;
    private BigDecimal promedioRecargosDominicales;

    private BigDecimal valorVacaciones;
    private BigDecimal valorPrimaVacaciones;

    private BigDecimal totalDevengado;
    private BigDecimal salud;
    private BigDecimal pension;
    private BigDecimal netoPagar;
}