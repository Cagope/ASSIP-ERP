package co.assip.erp.depositos.informes.intereses_retencion.dto;

import lombok.Data;

@Data
public class InteresesRetencionRequestDTO {

    private String fechaInicial;
    private String fechaFinal;

    private Integer idAgencia;

    /**
     * "0" = todas
     */
    private String codigoForma;

    /**
     * Opcional
     */
    private String codigoCuenta;

}