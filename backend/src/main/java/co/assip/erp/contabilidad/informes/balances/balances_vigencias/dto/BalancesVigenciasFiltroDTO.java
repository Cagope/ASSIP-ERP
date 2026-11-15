package co.assip.erp.contabilidad.informes.balances.balances_vigencias.dto;


/** Parámetros del informe al corte; incluye todas las cuentas del catálogo. */
public record BalancesVigenciasFiltroDTO(
        Integer idAgencia,
        Integer ano,
        Integer nivel,
        boolean incluirFirmas
) {}
