package co.assip.erp.contabilidad.comprobantes.copiar.dto;

import java.time.LocalDate;

/** Identifica el origen y los datos generales del nuevo comprobante. */
public record ComprobanteCopiarRequestDTO(
        Integer idAgenciaOrigen,
        String tipoComprobanteOrigen,
        String numeroComprobanteOrigen,
        Integer idAgenciaDestino,
        String tipoComprobanteDestino,
        LocalDate fechaContableDestino,
        String conceptoComprobanteDestino
) {}
