package co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resultado único para pantalla, PDF y Excel. */
public record EstadoCuentasRespuestaDTO(
        Integer idAgencia,
        String codigoAgencia,
        String nombreAgencia,
        LocalDate fechaCorte,
        String codigoCuentaPrincipal,
        Integer nivel,
        boolean incluirFirmas,
        List<EstadoCuentasFilaDTO> filas,
        TotalesDTO totales,
        FirmasDTO firmas
) {
    public record TotalesDTO(BigDecimal saldoAnterior, BigDecimal valorDebitoMes,
                             BigDecimal valorCreditoMes, BigDecimal saldoFinal) {}
    public record FirmasDTO(FirmanteDTO firmante1, FirmanteDTO firmante2,
                            FirmanteDTO firmante3) {}
    public record FirmanteDTO(String titulo, String nombre, String tarjetaProfesional) {}
}
