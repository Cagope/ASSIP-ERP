package co.assip.erp.cajas.convenios_recaudo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioRecaudoDTO {

    private Long idConvenio;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private String codigoConvenio;
    private String nombreConvenio;

    private Long idCuentaAhorro;
    private String codigoCuenta;
    private String codigoForma;
    private String nombreForma;

    private Long idDatosPersonal;
    private String documento;
    private String nombreTitular;

    private BigDecimal saldoActual;

    private String estado;
}