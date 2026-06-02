package co.assip.erp.cajas.recaudos_convenios.dto;

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
public class RecaudoConvenioDTO {

    private Long idRecaudoConvenio;

    private Long idProvision;
    private Long idCaja;
    private Integer idAgencia;
    private LocalDate fechaRecaudo;

    private Long idConvenio;
    private String codigoConvenio;
    private String nombreConvenio;

    private Long idCuentaAhorro;
    private String codigoCuenta;

    private String documentoSoporte;
    private BigDecimal valorRecaudo;

    private String estadoRecaudo;
}