package co.assip.erp.cartera.analisis.rollforward.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RollForwardLineaDTO(
        LocalDate fechaCorteAnterior, LocalDate fechaCorte,
        Long idLineaCredito, String codigoLineaCredito, String nombreLineaCredito,
        Integer creditosIniciales, Integer creditosFinales,
        Integer creditosNuevos, Integer creditosReingresados,
        Integer creditosReduccion, Integer creditosSinVariacion, Integer creditosAumento,
        Integer creditosPrepago, Integer creditosCancelacionNormal,
        Integer creditosCancelacionPostVencimiento, Integer creditosAusenciaTemporal,
        Integer creditosOtrasSalidas,
        BigDecimal saldoInicial, BigDecimal nuevos, BigDecimal reingresos,
        BigDecimal aumentosSaldo, BigDecimal reduccionesSaldo,
        BigDecimal prepagos, BigDecimal cancelacionesNormales,
        BigDecimal cancelacionesPostVencimiento, BigDecimal ausenciasTemporales,
        BigDecimal otrasSalidas, BigDecimal saldoFinal,
        BigDecimal variacionNeta, BigDecimal variacionPorcentaje,
        BigDecimal saldoFinalCalculado, BigDecimal diferenciaControl,
        Integer creditosFinalesCalculados, Integer diferenciaControlCreditos
) {
}
