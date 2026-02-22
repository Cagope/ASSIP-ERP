package co.assip.erp.nomina.novedades_nomina.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadNominaFormDTO {

    private Integer idPeriodo;
    private Integer idEmpleado;
    private Integer idContrato;

    private String codigoConcepto;

    private LocalDate fechaInicial;
    private LocalDate fechaFinal;

    private BigDecimal cantidad;
    private BigDecimal valor;

    private String observacion;

    private Integer fkAgencia;
}
