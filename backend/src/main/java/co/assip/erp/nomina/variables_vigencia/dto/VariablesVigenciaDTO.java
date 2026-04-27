package co.assip.erp.nomina.variables_vigencia.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariablesVigenciaDTO {

    private Integer idVariable;

    private LocalDate fechaInicial;
    private LocalDate fechaFinal;

    private BigDecimal smmlv;
    private BigDecimal auxTransporte;

    private BigDecimal porcSaludEmpleado;
    private BigDecimal porcSaludEmpleador;

    private BigDecimal porcPensionEmpleado;
    private BigDecimal porcPensionEmpleador;

    private BigDecimal porcCajaCompensacion;
    private BigDecimal porcSena;
    private BigDecimal porcIcbf;

    private BigDecimal porProvisionPrima;
    private BigDecimal porProvisionPrimaSemestral;
    private BigDecimal porProvisionVacaciones;
    private BigDecimal porProvisionCesantias;
    private BigDecimal porProvisionInteresCesantias;

    private BigDecimal topeIbcMinSmmlv;
    private BigDecimal topeIbcMaxSmmlv;

    private BigDecimal horasMes;
    private BigDecimal diasMes;

    private Boolean exoneradoSalud;
    private Boolean exoneradoParafiscales;

    private Boolean aplicaCajaCompensacion;
    private Boolean aplicaSena;
    private Boolean aplicaIcbf;

    private Boolean activo;
}