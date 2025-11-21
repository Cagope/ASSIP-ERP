package co.assip.erp.depositos.informes.cuentasnr.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CuentaRetiradaDTO {

    private String codigoCuenta;
    private String documento;
    private String nombreCompleto;

    private String agencia;
    private String forma;

    private String fechaRetiro;
    private Double ultimoSaldo;
}
