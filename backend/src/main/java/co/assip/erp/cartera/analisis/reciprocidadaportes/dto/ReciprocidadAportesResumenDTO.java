package co.assip.erp.cartera.analisis.reciprocidadaportes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReciprocidadAportesResumenDTO(
        Integer idCierreCartera,
        LocalDate fechaCorte,
        Long cantidadPersonas,
        BigDecimal cantidadCreditos,
        BigDecimal saldoCartera,
        BigDecimal saldoAportes,
        BigDecimal porcentajeReciprocidadGlobal,
        BigDecimal apalancamientoGlobal,
        BigDecimal exposicionNetaAportes,
        BigDecimal excedenteAportes,
        Long personasSinAportes,
        Long personasAportesMenoresCartera,
        Long personasAportesCubrenCartera,
        Long personasReciprocidadMenor5,
        Long personasReciprocidad5_10,
        Long personasReciprocidad10_20,
        Long personasReciprocidad20_50,
        Long personasReciprocidad50_100,
        Long personasReciprocidad100Mas
) {}
