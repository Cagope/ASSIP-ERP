package co.assip.erp.cartera.calculosprevios.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DetalleCalculosCierreDTO(

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

        String pagareCartera,

        Integer idDatosPersonal,
        String tipoDocumento,
        String documento,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String nombreCompleto,

        // =====================================================
        // LÍNEA Y CLASIFICACIÓN
        // =====================================================

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

        // =====================================================
        // CONDICIONES FINANCIERAS
        // =====================================================

        String tipoModalidadInteres,
        String descripcionModalidadInteres,
        String periodoCodigoInteres,
        Integer periodoMesesInteres,

        Integer amortizacionCapital,

        String codigoTipoCuota,
        String descripcionTipoCuota,

        Integer plazo,
        Integer mesesGraciaCapital,
        Integer mesesGraciaInteres,

        String codigoFormaPago,
        String descripcionFormaPago,

        BigDecimal tasaNominalAnual,
        BigDecimal tasaEfectivaAnual,

        // =====================================================
        // ORIGEN / APROBACIÓN
        // =====================================================

        Integer idEmpresaLibranza,
        String documentoEmpresaLibranza,

        Integer idEnteAprobacion,
        String nombreEnteAprobacion,

        String tipoComprobante,
        String numeroComprobante,

        // =====================================================
        // ESTADO
        // =====================================================

        String codigoEstadoCartera,
        String descripcionEstadoCartera,

        String codigoEstadoJuridico,
        String descripcionEstadoJuridico,
        LocalDate fechaEstadoJuridico,

        String codigoModificacionCredito,
        String descripcionModificacionCredito,
        Integer numeroNovaciones,

        // =====================================================
        // VALORES DEL CRÉDITO
        // =====================================================

        BigDecimal valorInicialCredito,
        BigDecimal valorDesembolsado,
        BigDecimal valorBaseCalculoCuota,
        BigDecimal valorPrimeraCuota,
        BigDecimal valorCuota,
        BigDecimal saldoFotografia,
        BigDecimal abonosPendientes,

        Integer alturaCuota,

        // =====================================================
        // FECHAS
        // =====================================================

        LocalDate fechaInclusionSistema,
        LocalDate fechaContable,
        LocalDate fechaDesembolso,
        LocalDate fechaPrimeraCuota,
        LocalDate fechaPrimeraCuotaCapital,
        LocalDate fechaPrimeraCuotaInteres,
        LocalDate fechaFinal,

        LocalDate ultimaFechaCapital,
        LocalDate ultimaFechaInteres,
        LocalDate ultimaFechaMora,
        LocalDate ultimaFechaSeguro,
        LocalDate ultimaFechaFondo,

        LocalDate proximaFechaCapital,
        LocalDate proximaFechaInteres,
        LocalDate proximaFechaSeguro,
        LocalDate proximaFechaFondo,

        LocalDate interesesPagadosHasta,
        LocalDate interesesMoraHasta,

        // =====================================================
        // EVALUACIÓN / RIESGO FOTOGRAFÍA
        // =====================================================

        Boolean creditoEvaluado,
        LocalDate fechaEvaluacion,

        String edadRiesgoFotografia,
        String edadRiesgoInicialFotografia,
        String edadMoraFotografia,
        String edadPeFotografia,
        String edadHomologacionFotografia,
        String edadContableFotografia,

        // =====================================================
        // REESTRUCTURACIÓN FOTOGRAFÍA
        // =====================================================

        Boolean creditoReestructuradoFotografia,
        LocalDate fechaReestructuracion,

        String edadReestructuracionInicialFotografia,
        String edadReestructuradoFotografia,

        // =====================================================
        // CÁLCULOS DEL CIERRE
        // =====================================================

        Boolean esUnaSolaCuota,
        Boolean esReestructurado,

        Integer diasMora,
        Integer diasDiferencia,

        String edadRiesgoInicialCalculada,
        String edadMoraCalculada,
        String edadRiesgoCalculada,

        String edadReestructuracionInicialCalculada,
        String edadReestructuradoCalculada,

        // =====================================================
        // APORTES
        // =====================================================

        Integer cantidadCreditosAsociado,
        BigDecimal saldoTotalCreditosAsociado,
        BigDecimal saldoAportesFechaCorte,
        BigDecimal porcentajeAportesCredito,
        BigDecimal valorAportesCredito,

        // =====================================================
        // GARANTÍAS
        // =====================================================

        Integer cantidadBienesGarantia,
        BigDecimal valorGarantiasTotal,
        BigDecimal porcentajeGarantiasCredito,
        BigDecimal valorGarantiasCredito,

        // =====================================================
        // OTROS VALORES
        // =====================================================

        BigDecimal saldoInteresesCausados,
        BigDecimal valorInteresesCausadosMes,

        BigDecimal saldoInteresesContingentes,
        BigDecimal valorInteresesContingentesMes,

        BigDecimal valorCostasJudiciales,

        BigDecimal saldoSeguros,
        BigDecimal valorSegurosMes,

        BigDecimal saldoAlivios,
        BigDecimal valorAliviosMes,

        BigDecimal valorFondosGarantias,
        BigDecimal valorOtrosConceptos,

        // =====================================================
        // RESULTADOS POSTERIORES
        // =====================================================

        String edadPeResultado,
        String edadHomologacionResultado,
        String edadContableResultado,

        BigDecimal vea,
        BigDecimal pi,
        BigDecimal pdi,
        BigDecimal perdidaEsperada,

        BigDecimal deterioroCapital,
        BigDecimal deterioroIntereses,
        BigDecimal deterioroOtros,

        String codigoMetodoCalculo,

        // =====================================================
        // CONTROL
        // =====================================================

        LocalDateTime fechaCalculo

) {
}