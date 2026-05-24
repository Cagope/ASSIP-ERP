package co.assip.erp.depositos.informes.antiguedad_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class AntiguedadAsociadosItemDTO {

    private Integer idAgencia;
    private String nombreAgencia;

    private String codigoCuenta;
    private String documento;
    private String nombreCompleto;

    private LocalDate fechaVinculacion;
    private Integer aniosAsociado;
    private Integer edad;

    private BigDecimal saldoAportes;

    private String ciudad;
    private String departamento;
    private String celular;
    private String correo;

    private String rangoAntiguedad;

}