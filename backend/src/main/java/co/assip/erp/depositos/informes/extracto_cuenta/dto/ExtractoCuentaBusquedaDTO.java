package co.assip.erp.depositos.informes.extracto_cuenta.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExtractoCuentaBusquedaDTO {

    private Integer idCuentaAhorro;

    private String codigoCuenta;

    private String documento;

    private String nombreCompleto;

    private String codigoForma;
    private String nombreForma;

    private String nombreAgencia;

    private Double saldoActual;

}