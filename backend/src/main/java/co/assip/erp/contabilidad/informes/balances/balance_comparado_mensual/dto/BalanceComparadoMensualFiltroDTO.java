package co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto;

/**
 * Parámetros del Balance Comparado Mensual entre dos períodos independientes.
 * La agencia 1 representa el consolidado institucional (código 00).
 * Los permisos, el alcance de agencias y la validez de ambos períodos
 * se verifican en el servicio del backend.
 */
public record BalanceComparadoMensualFiltroDTO(
        Integer idAgencia,
        Integer anoPeriodo1,
        Integer mesPeriodo1,
        Integer anoPeriodo2,
        Integer mesPeriodo2,
        Integer nivel,
        boolean soloBalance,
        boolean incluirFirmas
) {
}
