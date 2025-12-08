package co.assip.erp.depositos.apertura_cuentas.dto;

import lombok.Data;

@Data
public class AperturaCuentaItemDTO {

    private Integer idFormaAhorro;
    private String codigoForma;
    private String nombreForma;

    // Indicadores de negocio
    private boolean obligatoria;
    private boolean permiteApoderado;
    private boolean permiteGmf;
    private boolean permiteRetencion;

    private String observacion; // texto explicativo opcional

    /** ✔ Consecutivo sugerido (consecutivo_forma + 1) */
    private Integer consecutivo;

    /** ✔ NUEVO — requerido por frontend para filtrar por agencia */
    private Integer idAgencia;
}
