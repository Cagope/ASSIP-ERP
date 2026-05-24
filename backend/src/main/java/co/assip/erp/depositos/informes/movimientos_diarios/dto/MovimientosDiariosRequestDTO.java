package co.assip.erp.depositos.informes.movimientos_diarios.dto;

import lombok.Data;

@Data
public class MovimientosDiariosRequestDTO {

    private String fechaInicial;
    private String fechaFinal;

    private Integer idAgencia;

    /**
     * 0 = todas las formas.
     * Ejemplo: "01", "02", "07"
     */
    private String codigoForma;

    /**
     * 0 = todos los tipos de movimiento.
     * Ejemplo: "001", "002"
     */
    private String codigoMovimiento;

}