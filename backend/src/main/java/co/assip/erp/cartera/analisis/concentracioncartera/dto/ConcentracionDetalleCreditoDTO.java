package co.assip.erp.cartera.analisis.concentracioncartera.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConcentracionDetalleCreditoDTO(
        Long idCarteraCredito,
        Long idDatosPersonal,
        String documento,
        String nombreCompleto,
        String pagareCartera,

        Integer idAgencia,
        String codigoAgencia,
        String nombreAgencia,

        Integer idLineaCredito,
        String codigoLineaCredito,
        String nombreLineaCredito,

        String codigoClasificacionCredito,
        String descripcionClasificacionCredito,

        String codigoGarantiaCredito,
        String descripcionGarantiaCredito,
        String tipoGarantia,
        String codigoSubgarantia,
        String descripcionSubgarantia,

        String codigoDestinoEconomico,
        String descripcionDestinoEconomico,

        LocalDate fechaDesembolso,
        BigDecimal saldo,
        BigDecimal porcentajeCartera,

        String edadContable,
        Integer diasMora,

        BigDecimal deterioroCapital,
        BigDecimal deterioroIntereses,
        BigDecimal deterioroOtros,
        BigDecimal deterioroTotal,
        BigDecimal perdidaEsperada,

        Integer cantidadBienesGarantia,
        BigDecimal valorGarantiasTotal,
        BigDecimal porcentajeGarantiasCredito,
        BigDecimal valorGarantiasCredito,
        BigDecimal valorFondosGarantias
) {
}
