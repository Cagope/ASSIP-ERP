package co.assip.erp.nomina.empleado_contratos.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoContratoDTO {

    private Integer idContrato;

    private Integer idEmpleado;
    private Integer idSeccion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Integer idTipoContrato;
    private Integer idCargo;

    private BigDecimal salarioBase;
    private Boolean salarioIntegral;

    private String periodoPago;

    private Integer idEps;
    private Integer idAfp;
    private Integer idCesantias;
    private Integer idArl;
    private Integer idCajaCompensacion;

    private String cuentaNominaDisplay;
    private Long idCuentaAhorroNomina;

    private LocalDate fechaEnvioNotaRenovacion;

    private Short claseRiesgoArl;
    private BigDecimal porcentajeArl;

    private Boolean activo;
}
