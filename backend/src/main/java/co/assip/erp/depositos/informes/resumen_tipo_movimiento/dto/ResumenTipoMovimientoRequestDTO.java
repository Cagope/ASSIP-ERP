package co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto;

import lombok.Data;

@Data
public class ResumenTipoMovimientoRequestDTO {

    private String fechaInicial;
    private String fechaFinal;

    private Integer idAgencia;

    /**
     * "0" = todas las formas.
     */
    private String codigoForma;

    /**
     * "0" = todos los tipos.
     * Se usa para el detalle del segundo nivel.
     */
    private String codigoMovimiento;

}