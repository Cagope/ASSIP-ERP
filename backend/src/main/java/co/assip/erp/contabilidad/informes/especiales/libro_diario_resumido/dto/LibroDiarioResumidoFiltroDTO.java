package co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto;

import java.time.LocalDate;

/**
 * Filtros para consultar el libro diario resumido.
 *
 * La agencia se selecciona desde una lista de agencias autorizadas
 * y su acceso se valida en el backend.
 *
 * Modalidades:
 * CUENTA: agrupación diaria por cuenta contable.
 * TIPO_COMPROBANTE: agrupación diaria por tipo de comprobante.
 */
public record LibroDiarioResumidoFiltroDTO(
        Integer idAgencia,
        LocalDate fechaInicial,
        LocalDate fechaFinal,
        String modalidad
) {
}