
package co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado del Balance Comparado Mensual para dos períodos independientes.
 * Los saldos y variaciones son numéricos y se comparten entre pantalla, PDF y Excel.
 */
public record BalanceComparadoMensualRespuestaDTO(
        Integer idAgencia,
        String codigoAgencia,
        String nombreAgencia,
        Integer anoPeriodo1,
        Integer mesPeriodo1,
        Integer anoPeriodo2,
        Integer mesPeriodo2,
        Integer nivel,
        boolean soloBalance,
        boolean incluirFirmas,
        List<BalanceComparadoMensualFilaDTO> filas,
        TotalesDTO totales,
        FirmasDTO firmas
) {
    /** Totales de saldos de ambos períodos y variaciones del informe. */
    public record TotalesDTO(
            BigDecimal saldoPeriodo1,
            BigDecimal saldoPeriodo2,
            BigDecimal variacionValor,
            BigDecimal variacionPorcentaje
    ) {
    }

    /** Firmantes configurados para la agencia, cuando se solicitan. */
    public record FirmasDTO(
            FirmanteDTO firmante1,
            FirmanteDTO firmante2,
            FirmanteDTO firmante3
    ) {
    }

    /** Identificación profesional del firmante. */
    public record FirmanteDTO(
            String titulo,
            String nombre,
            String tarjetaProfesional
    ) {
    }
}
