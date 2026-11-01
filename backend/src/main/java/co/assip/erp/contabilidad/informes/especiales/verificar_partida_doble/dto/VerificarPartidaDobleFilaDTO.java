package co.assip.erp.contabilidad.informes.especiales.verificar_partida_doble.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Resultado de la verificación de débitos y créditos de un comprobante. */
public record VerificarPartidaDobleFilaDTO(
        String tipoComprobante,
        String nombreTipoComprobante,
        String numeroComprobante,
        LocalDate fecha,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia,
        boolean descuadrado
) {}
