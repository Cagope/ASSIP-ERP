package co.assip.erp.depositos.interesdiario_sm.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 🎯 Entrada para el proceso de Interés Diario SM
 * ------------------------------------------------------------
 * - agenciaId: Agencia seleccionada (0 = todas)
 * - fechaProceso: Fecha a evaluar para hallar saldo mínimo
 * - fechaLiquidacion: Fecha del asiento contable
 * - formaId: Forma de ahorro seleccionada
 * - confirmado: Indica si el usuario ya aceptó continuar
 *               cuando han pasado más de 1 día desde la
 *               última liquidación.
 */
@Getter
@Setter
public class InteresDiarioSMEntradaDTO {

    private Integer agenciaId;
    private String fechaProceso;
    private String fechaLiquidacion;
    private Integer formaId;

    /**
     * 🟡 Campo agregado:
     * true  → usuario ya confirmó continuar
     * false → proceso debe advertir y detener
     */
    private Boolean confirmado = false;
}
