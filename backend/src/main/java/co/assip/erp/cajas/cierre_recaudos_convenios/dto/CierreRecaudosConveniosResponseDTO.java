package co.assip.erp.cajas.cierre_recaudos_convenios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CierreRecaudosConveniosResponseDTO {

    private Long idProvision;
    private Long idCaja;
    private Integer idAgencia;
    private LocalDate fechaContable;

    private Long idConvenio;
    private String codigoConvenio;
    private String nombreConvenio;

    private Long idCuentaAhorro;
    private String codigoCuenta;

    private String documentoSoporte;

    private Integer cantidadRecaudos;
    private BigDecimal valorTotal;

    private Long idMovimientoCaja;

    private String mensaje;
}