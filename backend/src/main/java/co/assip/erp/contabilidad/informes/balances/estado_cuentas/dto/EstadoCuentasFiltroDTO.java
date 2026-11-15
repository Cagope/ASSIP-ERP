package co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto;

import java.time.LocalDate;

/** Parámetros del informe al corte; el prefijo vacío incluye todas las cuentas. */
public record EstadoCuentasFiltroDTO(
        Integer idAgencia,
        LocalDate fechaCorte,
        String codigoCuentaPrincipal,
        Integer nivel,
        boolean incluirFirmas
) {}
