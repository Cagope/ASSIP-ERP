package co.assip.erp.depositos.informes.cumpleanios_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CumpleaniosAsociadosItemDTO {

    private Integer idAgencia;
    private String nombreAgencia;

    private String documento;
    private String nombreCompleto;

    private LocalDate fechaNacimiento;

    private Integer diaCumpleanios;

    private Integer edad;

    private String ciudad;

    private String celular;

    private String correo;

    private BigDecimal saldoAportes;

}