package co.assip.erp.cartera.cierremensual.anexo2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrabajoAnexo2DTO(

        Integer idCierreCartera,
        LocalDate fechaCorte,

        Integer idCarteraCredito,
        Integer idCierreCarteraCredito,
        Integer idDatosPersonal,

        Integer idAgencia,

        Integer idLineaCredito,
        String codigoLineaCredito,
        String nombreLineaCredito,

        String pagareCartera,
        String documento,
        String nombreCompleto,

        Integer idModeloPe,
        String nombreModeloPe,

        String codigoClasificacionCredito,
        String codigoFormaPago,
        Integer idEmpresaLibranza,
        String tipoPersona,
        String codigoGarantiaCredito,

        LocalDate fechaDesembolso,

        Integer diasMoraActual,
        String edadMoraEntradaPe,
        String edadRiesgoEntradaPe,

        BigDecimal saldoActual,
        BigDecimal saldoInteresesCausados,
        BigDecimal saldoAportesFechaCorte,
        BigDecimal valorAportesCredito,
        BigDecimal valorCostasJudiciales,
        BigDecimal valorOtrosConceptos,

        Integer moraMax3m,
        Integer moraMax12m,
        Integer moraMax24m,
        Integer moraMax36m,
        Integer cantidadMora3160_3m,

        Integer ea,
        BigDecimal eaContenido,

        Integer fe,
        String feContenido,

        Integer valcuota,
        BigDecimal valcuotaContenido,

        Integer fondplazo,
        Integer fondplazoContenido,

        Integer mora1230,
        Integer mora1230Contenido,

        Integer mora1260,
        Integer mora1260Contenido,

        Integer sinmora,
        Integer sinmoraContenido,

        Integer mora2430n,
        Integer mora2430nContenido,

        Integer mora315,
        Integer mora315Contenido,

        Integer mortrim,
        Integer mortrimContenido,

        Integer mora3660,
        Integer mora3660MoraMax36m,
        Integer mora3660MoraMax24m,

        BigDecimal betaIntercepto,
        BigDecimal betaEa,
        BigDecimal betaFe,
        BigDecimal betaValcuota,
        BigDecimal betaFondplazo,
        BigDecimal betaMora1230,
        BigDecimal betaMora1260,
        BigDecimal betaSinmora,
        BigDecimal betaMora2430n,
        BigDecimal betaMora315,
        BigDecimal betaMortrim,
        BigDecimal betaMora3660,

        BigDecimal aporteZIntercepto,
        BigDecimal aporteZEa,
        BigDecimal aporteZFe,
        BigDecimal aporteZValcuota,
        BigDecimal aporteZFondplazo,
        BigDecimal aporteZMora1230,
        BigDecimal aporteZMora1260,
        BigDecimal aporteZSinmora,
        BigDecimal aporteZMora2430n,
        BigDecimal aporteZMora315,
        BigDecimal aporteZMortrim,
        BigDecimal aporteZMora3660,

        BigDecimal z,
        BigDecimal puntaje,
        String calificacionModelo,

        Integer diasDefaultModelo,
        Integer defaultPe,
        String calificacionPe,
        String edadDeterioro,
        Integer tipoEntidadPe,
        String calificacionBasePi,
        BigDecimal pi,

        BigDecimal baseVeaCapital,
        BigDecimal baseVeaIntereses,
        BigDecimal baseVeaCostasJudiciales,
        BigDecimal baseVeaOtros,
        BigDecimal baseVeaAportes,
        BigDecimal baseVeaAhorroPermanente,

        BigDecimal veaBruto,
        BigDecimal veaDeducciones,
        BigDecimal vea,

        String codigoGarantiaPdi,
        String nombreGarantiaPdi,

        BigDecimal valorGarantia,
        BigDecimal porcentajeGarantiaReconocido,
        BigDecimal valorGarantiaReconocido,

        Integer diasMoraPdi,
        Integer tramoPdi,
        Integer diasDesdePdi,
        Integer diasHastaPdi,
        BigDecimal pdi,

        BigDecimal perdidaEsperada,
        BigDecimal porcentajePerdida,

        BigDecimal baseDeterioroCapital,
        BigDecimal baseDeterioroIntereses,
        BigDecimal baseDeterioroOtros,
        BigDecimal baseDeterioroTotal,

        BigDecimal porcentajeDeterioroCapital,
        BigDecimal porcentajeDeterioroIntereses,
        BigDecimal porcentajeDeterioroOtros,

        BigDecimal valorPerdidaCapital,
        BigDecimal valorPerdidaIntereses,
        BigDecimal valorPerdidaOtros,

        BigDecimal deterioroCapitalPe,
        BigDecimal deterioroInteresesPe,
        BigDecimal deterioroOtrosPe,
        BigDecimal deterioroTotalPe,

        Integer diasMoraHomologacion,
        String edadHomologadaIndividual,
        String edadContablePe

) {
}