package co.assip.erp.cartera.cierremensual.anexo1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetalleAnexo1DTO(

        // =====================================================
        // CIERRE
        // =====================================================

        Integer idCierreCartera,
        LocalDate fechaCorte,

        // =====================================================
        // IDENTIFICACIÓN
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
        // LÍNEA
        // =====================================================

        Integer idLineaCredito,
        String codigoLineaCredito,
        String nombreLineaCredito,

        // =====================================================
        // CLASIFICACIÓN DEL CRÉDITO
        // =====================================================

        String codigoClasificacionCredito,
        String descripcionClasificacionCredito,

        // =====================================================
        // GARANTÍA DEL CRÉDITO
        // =====================================================

        String codigoGarantiaCredito,
        String descripcionGarantiaCredito,
        String tipoGarantia,

        // =====================================================
        // TIPO DE PERSONA
        //
        // Se obtiene de la fotografía de hoja de vida
        // correspondiente a la fecha de corte.
        // =====================================================

        String tipoPersona,

        // =====================================================
        // MORA Y EDADES
        // =====================================================

        Integer diasMora,

        String edadRiesgoInicial,
        String edadDeMora,
        String edadDeRiesgo,
        String edadContable,

        // =====================================================
        // SALDOS DEL CRÉDITO
        // =====================================================

        BigDecimal saldoCapital,

        BigDecimal tasaNominalAnual,

        BigDecimal saldoInteresesCausados,
        BigDecimal saldoInteresesContingentes,

        // =====================================================
        // APORTES
        // =====================================================

        BigDecimal saldoAportesFechaCorte,
        BigDecimal porcentajeAportesCredito,
        BigDecimal valorAportesCredito,

        // =====================================================
        // GARANTÍAS PRORRATEADAS
        // =====================================================

        Integer cantidadBienesGarantia,

        BigDecimal valorGarantiasTotal,
        BigDecimal porcentajeGarantiasCredito,
        BigDecimal valorGarantiasCredito,

        // =====================================================
        // GARANTÍA RECONOCIDA PARA ANEXO 1
        //
        // porcentajeAplicacionGarantia:
        // porcentaje reconocido según:
        //
        // - tipo de garantía
        // - días de mora
        // - vigencia
        //
        // valorGarantiaReconocida:
        //
        // valorGarantiasCredito
        // * porcentajeAplicacionGarantia
        // / 100
        // =====================================================

        BigDecimal porcentajeAplicacionGarantia,
        BigDecimal valorGarantiaReconocida,

        // =====================================================
        // DETERIORO DE CAPITAL - ANEXO 1
        //
        // porcentajeDeterioroCapital:
        // porcentaje normal o especial finalmente aplicado.
        //
        // baseDeterioroCapital:
        //
        // saldoCapital
        // - valorAportesCredito
        // - valorGarantiaReconocida
        //
        // con piso en cero.
        //
        // deterioroCapital:
        //
        // baseDeterioroCapital
        // * porcentajeDeterioroCapital
        // / 100
        // =====================================================

        BigDecimal porcentajeDeterioroCapital,
        BigDecimal baseDeterioroCapital,
        BigDecimal deterioroCapital,

        // =====================================================
        // DETERIORO DE INTERESES - ANEXO 1
        //
        // porcentajeDeterioroIntereses:
        // porcentaje normal o especial finalmente aplicado.
        //
        // deterioroIntereses:
        //
        // saldoInteresesCausados
        // * porcentajeDeterioroIntereses
        // / 100
        //
        // La garantía no disminuye la base de intereses.
        // =====================================================

        BigDecimal porcentajeDeterioroIntereses,
        BigDecimal deterioroIntereses,

        // =====================================================
        // CONTROL
        // =====================================================

        String codigoMetodoCalculo

) {
}