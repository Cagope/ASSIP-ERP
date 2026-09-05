package co.assip.erp.cartera.cierremensual.anexo2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetalleAnexo2DTO(

        // =====================================================
        // CIERRE
        // =====================================================

        Integer idCierreCartera,
        LocalDate fechaCorte,

        // =====================================================
        // IDENTIFICACIÓN DEL CRÉDITO
        // =====================================================

        Integer idCarteraCredito,
        Integer idCierreCarteraCredito,
        Integer idCierreCarteraResultado,
        Integer idAgencia,

        Integer idDatosPersonal,

        String pagareCartera,

        String tipoDocumento,
        String documento,

        String nombres,
        String primerApellido,
        String segundoApellido,
        String nombreCompleto,

        // =====================================================
        // MODELO PE
        //
        // 1 = Consumo con Libranza
        // 2 = Consumo sin Libranza
        // 3 = Comercial Persona Natural
        // =====================================================

        Integer idModeloPe,
        String codigoModeloPe,
        String nombreModeloPe,

        // =====================================================
        // LÍNEA Y CLASIFICACIÓN
        // =====================================================

        Integer idLineaCredito,
        String codigoLineaCredito,
        String nombreLineaCredito,

        String codigoClasificacionCredito,
        String descripcionClasificacionCredito,

        String tipoPersona,

        // =====================================================
        // GARANTÍA
        // =====================================================

        String codigoGarantiaCredito,
        String descripcionGarantiaCredito,
        String tipoGarantia,

        BigDecimal valorGarantiasCredito,
        BigDecimal porcentajeGarantiasCredito,

        // =====================================================
        // FECHAS DEL CRÉDITO
        // =====================================================

        LocalDate fechaDesembolso,
        LocalDate fechaVencimiento,

        // =====================================================
        // SALDOS
        // =====================================================

        BigDecimal saldoCapital,
        BigDecimal saldoIntereses,
        BigDecimal saldoOtrosConceptos,

        BigDecimal saldoAportes,
        BigDecimal valorAportesAplicados,

        // =====================================================
        // EXPOSICIÓN
        // =====================================================

        BigDecimal vea,

        // =====================================================
        // PÉRDIDA ESPERADA
        // =====================================================

        BigDecimal pi,
        BigDecimal pdi,

        BigDecimal perdidaEsperada,
        BigDecimal porcentajePerdidaEsperada,

        // =====================================================
        // DETERIOROS PE
        // =====================================================

        BigDecimal deterioroCapital,
        BigDecimal deterioroIntereses,
        BigDecimal deterioroOtros,

        // =====================================================
        // MORA Y EDADES
        // =====================================================

        Integer diasMora,

        String edadRiesgoInicial,
        String edadDeMora,
        String edadDeRiesgo,

        String edadPe,
        String edadHomologada,
        String edadContable,

        // =====================================================
        // CONDICIONES DEL CRÉDITO
        // =====================================================

        String codigoFormaPago,
        String codigoEstadoJuridico,
        Boolean esLibranza,
        Boolean esReestructurado,

        // =====================================================
        // CONTROL
        // =====================================================

        String codigoMetodoCalculo

) {
}