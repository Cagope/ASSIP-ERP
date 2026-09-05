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
        String codigoAgencia,
        String nombreAgencia,

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

        // =====================================================
        // CAUSACIÓN DE INTERESES
        // =====================================================

        BigDecimal valorInteresesCausadosMes,
        BigDecimal saldoInteresesCausados,

        BigDecimal valorInteresesContingentesMes,
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
        // GARANTÍAS CALCULADAS PARA ANEXO 1
        // =====================================================

        BigDecimal porcentajeAplicacionGarantia,
        BigDecimal valorGarantiaReconocida,

        // =====================================================
        // DETERIORO DE CAPITAL - ANEXO 1
        // =====================================================

        BigDecimal porcentajeDeterioroCapital,
        BigDecimal baseDeterioroCapital,
        BigDecimal deterioroCapital,

        // =====================================================
        // DETERIORO DE INTERESES - ANEXO 1
        // =====================================================

        BigDecimal porcentajeDeterioroIntereses,
        BigDecimal deterioroIntereses,

        // =====================================================
        // CONTROL
        // =====================================================

        String codigoMetodoCalculo

) {
}