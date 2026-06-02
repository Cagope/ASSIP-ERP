package co.assip.erp.cajas.provisiones.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CajaProvisionActivaDTO {

    private Long idCaja;
    private String codigoCaja;
    private String descripcionCaja;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Long idProvision;
    private LocalDate fechaContable;

    private String estado;
}