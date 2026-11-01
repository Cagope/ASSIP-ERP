package co.assip.erp.cartera.informes.reversiones.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Fila de auditoría de una reversión, sin duplicar el extracto contrario. */
public record InformeReversionDTO(
        Long idPago,
        Integer idAgenciaCredito,
        String codigoAgencia,
        String nombreAgencia,
        Integer idAgenciaReceptora,
        String codigoAgenciaReceptora,
        String nombreAgenciaReceptora,
        String codigoLineaCredito,
        String nombreLineaCredito,
        String pagare,
        String documento,
        String asociado,
        LocalDate fechaPagoOriginal,
        LocalDate fechaReversion,
        OffsetDateTime fechaRegistroReversion,
        String motivo,
        String usuario,
        String funcionario,
        String comprobanteOriginal,
        String comprobanteReversion,
        BigDecimal capital,
        BigDecimal interesCausado,
        BigDecimal interesIngreso,
        BigDecimal interesAnticipado,
        BigDecimal mora,
        BigDecimal seguros,
        BigDecimal aportes,
        BigDecimal papeleria,
        BigDecimal fondoGarantia,
        BigDecimal otros,
        BigDecimal valorReversado
) {}
