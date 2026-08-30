package co.assip.erp.cartera.analisis.cosechas.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CosechaResumenDTO {

    // =========================================================
    // PERÍODO ANALIZADO
    // =========================================================

    private LocalDate cosechaDesde;

    private LocalDate cosechaHasta;

    private LocalDate hastaCorte;


    // =========================================================
    // FILTROS
    // =========================================================

    private Integer idAgencia;

    private Integer idLineaCredito;


    // =========================================================
    // RESUMEN GENERAL
    // =========================================================

    private Integer cantidadCosechas;

    private Integer cantidadCreditosOriginados;

    private BigDecimal valorInicialTotal;

    private BigDecimal valorTotalDesembolsado;

    private Integer maxMob;


    // =========================================================
    // MATRIZ
    // =========================================================

    private List<CosechaFilaDTO> filas =
            new ArrayList<>();
}