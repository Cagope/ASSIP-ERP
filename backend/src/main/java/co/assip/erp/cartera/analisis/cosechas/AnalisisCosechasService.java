package co.assip.erp.cartera.analisis.cosechas;

import co.assip.erp.cartera.analisis.cosechas.dto.CosechaCatalogoDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaCeldaDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaCorteDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaDetalleDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaFilaDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaResumenDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalisisCosechasService {

    private static final List<String> INDICADORES_VALIDOS =
            List.of(
                    "ORIGINADOS",
                    "PRESENTES",
                    "SIN_PRESENCIA",
                    "CON_SALDO",
                    "SALDO",
                    "SALDO_REMANENTE",
                    "MORA_30",
                    "MORA_60",
                    "MORA_90",
                    "MORA_180"
            );


    private final AnalisisCosechasRepository repository;


    public AnalisisCosechasService(
            AnalisisCosechasRepository repository
    ) {
        this.repository = repository;
    }


    // =========================================================
    // CATÁLOGOS
    // =========================================================

    public List<CosechaCorteDTO> listarCortes() {

        return repository.listarCortes();
    }


    public List<CosechaCatalogoDTO> listarAgencias() {

        return repository.listarAgencias();
    }


    public List<CosechaCatalogoDTO> listarLineas() {

        return repository.listarLineas();
    }


    // =========================================================
    // ANALIZAR
    // =========================================================

    public CosechaResumenDTO analizar(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            LocalDate hastaCorte,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        LocalDate desde =
                normalizarMes(
                        cosechaDesde
                );

        LocalDate hasta =
                normalizarMes(
                        cosechaHasta
                );


        if (desde.isAfter(hasta)) {

            throw new IllegalArgumentException(
                    "La cosecha inicial no puede ser posterior a la cosecha final."
            );
        }


        LocalDate ultimoCorteDisponible =
                repository.obtenerUltimoCorte();


        if (ultimoCorteDisponible == null) {

            throw new IllegalStateException(
                    "No existen cierres mensuales de cartera disponibles."
            );
        }


        LocalDate corteFinal =
                hastaCorte != null
                        ? hastaCorte
                        : ultimoCorteDisponible;


        if (corteFinal.isAfter(
                ultimoCorteDisponible
        )) {

            throw new IllegalArgumentException(
                    "El corte seleccionado es posterior al último cierre disponible: "
                            + ultimoCorteDisponible
                            + "."
            );
        }


        if (
                !repository.existeCorte(
                        corteFinal
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha "
                            + corteFinal
                            + " no corresponde a un cierre mensual disponible."
            );
        }


        if (
                hasta.isAfter(
                        YearMonth.from(
                                corteFinal
                        ).atDay(1)
                )
        ) {

            throw new IllegalArgumentException(
                    "La cosecha final no puede ser posterior al mes del corte final."
            );
        }


        List<CosechaCeldaDTO> celdas =
                repository.analizar(
                        desde,
                        hasta,
                        corteFinal,
                        idAgencia,
                        idLineaCredito
                );


        return construirResumen(
                desde,
                hasta,
                corteFinal,
                idAgencia,
                idLineaCredito,
                celdas
        );
    }


    // =========================================================
    // DETALLE
    // =========================================================

    public List<CosechaDetalleDTO> listarDetalle(
            LocalDate cosecha,
            LocalDate fechaCorte,
            String indicador,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        if (cosecha == null) {

            throw new IllegalArgumentException(
                    "La cosecha es obligatoria."
            );
        }


        if (fechaCorte == null) {

            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }


        LocalDate mesCosecha =
                normalizarMes(
                        cosecha
                );


        if (
                fechaCorte.isBefore(
                        YearMonth.from(
                                mesCosecha
                        ).atEndOfMonth()
                )
        ) {

            throw new IllegalArgumentException(
                    "El corte no puede ser anterior al mes de la cosecha."
            );
        }


        if (
                !repository.existeCorte(
                        fechaCorte
                )
        ) {

            throw new IllegalArgumentException(
                    "La fecha "
                            + fechaCorte
                            + " no corresponde a un cierre mensual disponible."
            );
        }


        String indicadorNormalizado =
                indicador == null
                        ? ""
                        : indicador
                        .trim()
                        .toUpperCase();


        if (
                !INDICADORES_VALIDOS.contains(
                        indicadorNormalizado
                )
        ) {

            throw new IllegalArgumentException(
                    "Indicador no válido. Valores permitidos: "
                            +
                            String.join(
                                    ", ",
                                    INDICADORES_VALIDOS
                            )
            );
        }


        return repository.listarDetalle(
                mesCosecha,
                fechaCorte,
                indicadorNormalizado,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // CONSTRUIR RESUMEN
    // =========================================================

    private CosechaResumenDTO construirResumen(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            LocalDate hastaCorte,
            Integer idAgencia,
            Integer idLineaCredito,
            List<CosechaCeldaDTO> celdas
    ) {

        CosechaResumenDTO resumen =
                new CosechaResumenDTO();

        resumen.setCosechaDesde(
                cosechaDesde
        );

        resumen.setCosechaHasta(
                cosechaHasta
        );

        resumen.setHastaCorte(
                hastaCorte
        );

        resumen.setIdAgencia(
                idAgencia
        );

        resumen.setIdLineaCredito(
                idLineaCredito
        );


        Map<LocalDate, CosechaFilaDTO> mapa =
                new LinkedHashMap<>();

        int maxMob = 0;


        for (CosechaCeldaDTO celda : celdas) {

            normalizarCelda(
                    celda
            );


            CosechaFilaDTO fila =
                    mapa.computeIfAbsent(
                            celda.getCosecha(),
                            key -> {

                                CosechaFilaDTO nueva =
                                        new CosechaFilaDTO();

                                nueva.setCosecha(
                                        key
                                );

                                nueva.setCantidadOriginada(
                                        celda.getCantidadOriginada()
                                );

                                nueva.setValorInicialOriginal(
                                        celda.getValorInicialOriginal()
                                );

                                nueva.setValorDesembolsadoOriginal(
                                        celda.getValorDesembolsadoOriginal()
                                );

                                nueva.setMaxMob(
                                        0
                                );

                                nueva.setCeldas(
                                        new ArrayList<>()
                                );

                                return nueva;
                            }
                    );


            fila.getCeldas().add(
                    celda
            );


            int mob =
                    numero(
                            celda.getMob()
                    );


            if (
                    mob >
                            numero(
                                    fila.getMaxMob()
                            )
            ) {

                fila.setMaxMob(
                        mob
                );
            }


            if (mob > maxMob) {

                maxMob =
                        mob;
            }
        }


        List<CosechaFilaDTO> filas =
                new ArrayList<>(
                        mapa.values()
                );


        filas.sort(
                Comparator.comparing(
                        CosechaFilaDTO::getCosecha
                )
        );


        for (CosechaFilaDTO fila : filas) {

            fila.getCeldas().sort(
                    Comparator.comparing(
                            CosechaCeldaDTO::getMob
                    )
            );
        }


        int cantidadCreditosOriginados =
                filas.stream()
                        .mapToInt(
                                fila ->
                                        numero(
                                                fila.getCantidadOriginada()
                                        )
                        )
                        .sum();


        BigDecimal valorInicialTotal =
                filas.stream()
                        .map(
                                fila ->
                                        decimal(
                                                fila.getValorInicialOriginal()
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal valorTotalDesembolsado =
                filas.stream()
                        .map(
                                fila ->
                                        decimal(
                                                fila.getValorDesembolsadoOriginal()
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        resumen.setCantidadCosechas(
                filas.size()
        );

        resumen.setCantidadCreditosOriginados(
                cantidadCreditosOriginados
        );

        resumen.setValorInicialTotal(
                valorInicialTotal
        );

        resumen.setValorTotalDesembolsado(
                valorTotalDesembolsado
        );

        resumen.setMaxMob(
                maxMob
        );

        resumen.setFilas(
                filas
        );


        return resumen;
    }


    // =========================================================
    // NORMALIZAR CELDA
    // =========================================================

    private void normalizarCelda(
            CosechaCeldaDTO dto
    ) {

        dto.setCantidadOriginada(
                numero(dto.getCantidadOriginada())
        );

        dto.setValorInicialOriginal(
                decimal(dto.getValorInicialOriginal())
        );

        dto.setValorDesembolsadoOriginal(
                decimal(dto.getValorDesembolsadoOriginal())
        );


        dto.setCantidadPresentesCorte(
                numero(dto.getCantidadPresentesCorte())
        );

        dto.setCantidadSinPresenciaCorte(
                numero(dto.getCantidadSinPresenciaCorte())
        );

        dto.setPorcentajePresentesCorte(
                decimal(dto.getPorcentajePresentesCorte())
        );

        dto.setPorcentajeSinPresenciaCorte(
                decimal(dto.getPorcentajeSinPresenciaCorte())
        );


        dto.setCantidadConSaldo(
                numero(dto.getCantidadConSaldo())
        );

        dto.setSaldoCapital(
                decimal(dto.getSaldoCapital())
        );

        dto.setPorcentajeSaldoRemanente(
                decimal(dto.getPorcentajeSaldoRemanente())
        );


        dto.setCantidadMora30(
                numero(dto.getCantidadMora30())
        );

        dto.setSaldoMora30(
                decimal(dto.getSaldoMora30())
        );

        dto.setPorcentajeCantidadMora30(
                decimal(dto.getPorcentajeCantidadMora30())
        );

        dto.setPorcentajeSaldoMora30SobreSaldo(
                decimal(dto.getPorcentajeSaldoMora30SobreSaldo())
        );

        dto.setPorcentajeSaldoMora30SobreOriginacion(
                decimal(dto.getPorcentajeSaldoMora30SobreOriginacion())
        );


        dto.setCantidadMora60(
                numero(dto.getCantidadMora60())
        );

        dto.setSaldoMora60(
                decimal(dto.getSaldoMora60())
        );

        dto.setPorcentajeCantidadMora60(
                decimal(dto.getPorcentajeCantidadMora60())
        );

        dto.setPorcentajeSaldoMora60SobreSaldo(
                decimal(dto.getPorcentajeSaldoMora60SobreSaldo())
        );

        dto.setPorcentajeSaldoMora60SobreOriginacion(
                decimal(dto.getPorcentajeSaldoMora60SobreOriginacion())
        );


        dto.setCantidadMora90(
                numero(dto.getCantidadMora90())
        );

        dto.setSaldoMora90(
                decimal(dto.getSaldoMora90())
        );

        dto.setPorcentajeCantidadMora90(
                decimal(dto.getPorcentajeCantidadMora90())
        );

        dto.setPorcentajeSaldoMora90SobreSaldo(
                decimal(dto.getPorcentajeSaldoMora90SobreSaldo())
        );

        dto.setPorcentajeSaldoMora90SobreOriginacion(
                decimal(dto.getPorcentajeSaldoMora90SobreOriginacion())
        );


        dto.setCantidadMora180(
                numero(dto.getCantidadMora180())
        );

        dto.setSaldoMora180(
                decimal(dto.getSaldoMora180())
        );

        dto.setPorcentajeCantidadMora180(
                decimal(dto.getPorcentajeCantidadMora180())
        );

        dto.setPorcentajeSaldoMora180SobreSaldo(
                decimal(dto.getPorcentajeSaldoMora180SobreSaldo())
        );

        dto.setPorcentajeSaldoMora180SobreOriginacion(
                decimal(dto.getPorcentajeSaldoMora180SobreOriginacion())
        );
    }


    // =========================================================
    // AUXILIARES
    // =========================================================

    private LocalDate normalizarMes(
            LocalDate fecha
    ) {

        if (fecha == null) {

            throw new IllegalArgumentException(
                    "La fecha de cosecha es obligatoria."
            );
        }


        return YearMonth.from(
                fecha
        ).atDay(1);
    }


    private int numero(
            Integer valor
    ) {

        return valor != null
                ? valor
                : 0;
    }


    private BigDecimal decimal(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }
}