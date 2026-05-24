package co.assip.erp.depositos.informes.asociados_sin_movimientos.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AsociadosSinMovimientosRequestDTO {

    private String fechaCorte;

    private Integer idAgencia;

    /**
     * "0" = todas
     */
    private String codigoForma;

    /**
     * Días mínimos sin movimiento.
     * Ejemplo: 30, 60, 90.
     */
    private Integer diasMinimos;

    /**
     * 0 = todos
     * 1 = con saldo
     * 2 = sin saldo
     */
    private Integer tipoSaldo;

    private BigDecimal saldoMinimo;

}