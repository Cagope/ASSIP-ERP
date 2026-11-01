package co.assip.erp.contabilidad.informes.especiales.verificar_partida_doble.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Comprobantes verificados y resumen del período solicitado. */
public record VerificarPartidaDobleResponseDTO(
        Integer idAgencia,
        LocalDate fechaInicial,
        LocalDate fechaFinal,
        List<VerificarPartidaDobleFilaDTO> filas,
        int cantidadComprobantes,
        int cantidadCuadrados,
        int cantidadDescuadrados,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia
) {}
