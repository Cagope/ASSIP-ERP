
package co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta del balance detallado mensual individual o consolidado.
 * Conserva valores numéricos para consulta, PDF y Excel.
 * Incluye indicadores y resumen financiero del período.
 * Los permisos y cierres se validan antes de construir esta respuesta.
 */
public record BalanceDetalladoMensualRespuestaDTO(
        Integer idAgencia,
        String codigoAgencia,
        String nombreAgencia,
        Integer ano,
        Integer mes,
        Integer nivel,
        boolean soloBalance,
        boolean compararAnoAnterior,
        Integer anoComparativo,
        Integer mesComparativo,
        boolean incluirFirmas,
        List<BalanceDetalladoMensualFilaDTO> filas,
        TotalesDTO totales,
        FirmasDTO firmas,
        IndicadoresFinancierosDTO indicadoresFinancieros,
        ResumenFinancieroDTO resumenFinanciero
) {
    /**
     * Totales del balance; no sustituyen las comprobaciones
     * de cuadre contable.
     */
    public record TotalesDTO(
            BigDecimal saldoAnterior,
            BigDecimal valorDebitoMes,
            BigDecimal valorCreditoMes,
            BigDecimal saldoFinal,
            BigDecimal saldoMismoMesAnoAnterior,
            BigDecimal variacionValor,
            BigDecimal variacionPorcentaje
    ) {
    }

    /**
     * Indicadores por clases contables.
     * ValorMes representa la variación neta del período.
     * ValorAcumulado representa el saldo al cierre solicitado.
     */
    public record IndicadoresFinancierosDTO(
            IndicadorDTO activo,
            IndicadorDTO pasivo,
            IndicadorDTO patrimonio,
            IndicadorDTO ingresos,
            IndicadorDTO costosGastos,
            IndicadorDTO gananciasPerdidas
    ) {
    }

    /**
     * Valores del mes y acumulados de un indicador.
     */
    public record IndicadorDTO(
            BigDecimal valorMes,
            BigDecimal valorAcumulado
    ) {
    }

    /**
     * Resumen de movimientos contables.
     * Los importes se obtienen sin duplicar cuentas agrupadoras.
     */
    public record ResumenFinancieroDTO(
            MovimientoResumenDTO acumuladoAnterior,
            MovimientoResumenDTO movimientosMes,
            MovimientoResumenDTO acumuladoFinal,
            BigDecimal diferenciaMes,
            BigDecimal diferenciaAcumulada
    ) {
    }

    /**
     * Valores débito y crédito de una etapa del resumen.
     */
    public record MovimientoResumenDTO(
            BigDecimal debitos,
            BigDecimal creditos
    ) {
    }

    /**
     * Firmantes configurados para la agencia;
     * se omiten cuando no se solicitan.
     */
    public record FirmasDTO(
            FirmanteDTO firmante1,
            FirmanteDTO firmante2,
            FirmanteDTO firmante3
    ) {
    }

    /**
     * Cargo, nombre y tarjeta profesional de cada firmante.
     */
    public record FirmanteDTO(
            String titulo,
            String nombre,
            String tarjetaProfesional
    ) {
    }

    /**
     * Constructor compatible con la respuesta anterior.
     * Permite mantener funcionando las consultas existentes
     * mientras se integra el nuevo resumen financiero.
     */
    public BalanceDetalladoMensualRespuestaDTO(
            Integer idAgencia,
            String codigoAgencia,
            String nombreAgencia,
            Integer ano,
            Integer mes,
            Integer nivel,
            boolean soloBalance,
            boolean compararAnoAnterior,
            Integer anoComparativo,
            Integer mesComparativo,
            boolean incluirFirmas,
            List<BalanceDetalladoMensualFilaDTO> filas,
            TotalesDTO totales,
            FirmasDTO firmas
    ) {
        this(
                idAgencia,
                codigoAgencia,
                nombreAgencia,
                ano,
                mes,
                nivel,
                soloBalance,
                compararAnoAnterior,
                anoComparativo,
                mesComparativo,
                incluirFirmas,
                filas,
                totales,
                firmas,
                null,
                null
        );
    }
}
