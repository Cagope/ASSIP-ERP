package co.assip.erp.contabilidad.informes.balances.balances_vigencias.dto;

import java.math.BigDecimal;
import java.util.List;

/** Resultado único para pantalla, PDF y Excel. */
public record BalancesVigenciasRespuestaDTO(
        Integer idAgencia,
        String codigoAgencia,
        String nombreAgencia,
        Integer ano,
        Integer nivel,
        boolean incluirFirmas,
        List<BalancesVigenciasFilaDTO> filas,
        TotalesDTO totales,
        FirmasDTO firmas
) {
    public record TotalesDTO(BigDecimal saldoAnterior, BigDecimal valorDebitoMes,
                             BigDecimal valorCreditoMes, BigDecimal saldoFinal) {}
    public record FirmasDTO(FirmanteDTO firmante1, FirmanteDTO firmante2,
                            FirmanteDTO firmante3) {}
    public record FirmanteDTO(String titulo, String nombre, String tarjetaProfesional) {}
}
