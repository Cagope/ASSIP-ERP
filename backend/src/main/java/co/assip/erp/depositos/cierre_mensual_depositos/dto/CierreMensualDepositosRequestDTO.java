package co.assip.erp.depositos.cierre_mensual_depositos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CierreMensualDepositosRequestDTO {

    // =========================================================
    // FECHA DE CIERRE
    //
    // El cierre mensual de depósitos es centralizado.
    //
    // - una sola fotografía por fecha
    // - incluye todas las agencias
    // - debe corresponder al último día del mes
    // =========================================================

    private LocalDate fechaCierre;
}