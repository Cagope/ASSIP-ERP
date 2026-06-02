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
public class ConvenioRecaudoCuentaDTO {

    private Long idCuentaAhorro;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Long idDatosPersonal;
    private String documento;
    private String nombreTitular;

    private String codigoCuenta;
    private String codigoForma;
    private String nombreForma;

    private BigDecimal saldoActual;

    private String estadoCuenta;
    private Boolean estadoOperativo;
    private String mensajeOperativo;
}