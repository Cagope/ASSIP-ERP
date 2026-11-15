package co.assip.erp.contabilidad.informes.balances.balance_comprobacion.dto;

/**
 * Parámetros de consulta del balance de comprobación.
 * La agencia 1 representa el consolidado institucional (código 00).
 * Los permisos y el alcance de agencias se validan en el backend.
 */
public record BalanceComprobacionFiltroDTO(
        Integer idAgencia,
        Integer ano,
        Integer mes,
        Integer nivel,
        boolean soloBalance,
        boolean compararAnoAnterior,
        boolean incluirFirmas
) {
}