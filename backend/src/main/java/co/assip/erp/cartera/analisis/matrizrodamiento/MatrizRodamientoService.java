package co.assip.erp.cartera.analisis.matrizrodamiento;

import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoCeldaDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoCorteDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoDetalleDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoFilaDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MatrizRodamientoService {

    // =========================================================
    // CONSTANTES
    // =========================================================

    private static final List<String> CATEGORIAS =
            List.of(
                    "A",
                    "B",
                    "C",
                    "D",
                    "E"
            );

    private static final BigDecimal CIEN =
            new BigDecimal(
                    "100"
            );


    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final MatrizRodamientoRepository repository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MatrizRodamientoService(
            MatrizRodamientoRepository repository
    ) {

        this.repository = repository;
    }


    // =========================================================
    // CORTES
    // =========================================================

    public List<MatrizRodamientoCorteDTO>
    listarCortesDisponibles() {

        return repository
                .listarCortesDisponibles();
    }


    // =========================================================
    // CALCULAR
    // =========================================================

    public MatrizRodamientoDTO calcular(
            String tipoPartida,
            LocalDate fechaPartida,
            LocalDate fechaComparacion
    ) {

        String tipo =
                normalizarTipoPartida(
                        tipoPartida
                );

        validarFechas(
                tipo,
                fechaPartida,
                fechaComparacion
        );

        validarCortes(
                tipo,
                fechaPartida,
                fechaComparacion
        );

        int poblacionPartida =
                repository.contarPoblacionPartida(
                        tipo,
                        fechaPartida
                );

        /*
         * IMPORTANTE:
         *
         * PostgreSQL ya devuelve solamente
         * las 25 celdas agregadas.
         *
         * No cargamos los créditos individuales
         * para construir la matriz.
         */
        List<MatrizRodamientoCeldaDTO> celdas =
                repository.listarCeldasAgregadas(
                        tipo,
                        fechaPartida,
                        fechaComparacion
                );

        return construirResultado(
                tipo,
                fechaPartida,
                fechaComparacion,
                poblacionPartida,
                celdas
        );
    }


    // =========================================================
    // DETALLE CELDA
    // =========================================================

    public List<MatrizRodamientoDetalleDTO>
    listarDetalleCelda(
            String tipoPartida,
            LocalDate fechaPartida,
            LocalDate fechaComparacion,
            String categoriaAnterior,
            String categoriaPartida
    ) {

        String tipo =
                normalizarTipoPartida(
                        tipoPartida
                );

        validarFechas(
                tipo,
                fechaPartida,
                fechaComparacion
        );

        validarCortes(
                tipo,
                fechaPartida,
                fechaComparacion
        );

        String anterior =
                normalizarCategoria(
                        categoriaAnterior
                );

        String partida =
                normalizarCategoria(
                        categoriaPartida
                );

        return repository.listarDetalleCelda(
                tipo,
                fechaPartida,
                fechaComparacion,
                anterior,
                partida
        );
    }


    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private MatrizRodamientoDTO construirResultado(
            String tipoPartida,
            LocalDate fechaPartida,
            LocalDate fechaComparacion,
            int poblacionPartida,
            List<MatrizRodamientoCeldaDTO> celdas
    ) {

        MatrizRodamientoDTO resultado =
                new MatrizRodamientoDTO();

        resultado.setTipoPartida(
                tipoPartida
        );

        resultado.setFechaPartida(
                "ACTUAL".equals(tipoPartida)
                        ? LocalDate.now()
                        : fechaPartida
        );

        resultado.setFechaComparacion(
                fechaComparacion
        );

        resultado.setCantidadPoblacionPartida(
                poblacionPartida
        );

        // =====================================================
        // INDEXAR LAS 25 CELDAS
        // =====================================================

        Map<String, MatrizRodamientoCeldaDTO> mapa =
                new LinkedHashMap<>();

        for (
                MatrizRodamientoCeldaDTO celda
                : celdas
        ) {

            normalizarCelda(
                    celda
            );

            mapa.put(
                    llave(
                            celda.getCategoriaAnterior(),
                            celda.getCategoriaPartida()
                    ),
                    celda
            );
        }

        // =====================================================
        // GARANTIZAR LAS 25 CELDAS
        // =====================================================

        for (String anterior : CATEGORIAS) {

            for (String partida : CATEGORIAS) {

                String llave =
                        llave(
                                anterior,
                                partida
                        );

                if (!mapa.containsKey(llave)) {

                    MatrizRodamientoCeldaDTO celda =
                            nuevaCeldaCero(
                                    anterior,
                                    partida
                            );

                    mapa.put(
                            llave,
                            celda
                    );
                }
            }
        }

        // =====================================================
        // TOTALES GENERALES
        // =====================================================

        int totalCantidad = 0;

        BigDecimal totalValor =
                BigDecimal.ZERO;

        for (
                MatrizRodamientoCeldaDTO celda
                : mapa.values()
        ) {

            totalCantidad +=
                    valorEntero(
                            celda.getCantidad()
                    );

            totalValor =
                    totalValor.add(
                            valorDecimal(
                                    celda.getValor()
                            )
                    );
        }

        resultado.setCantidadCreditosMatriz(
                totalCantidad
        );

        resultado.setTotalCantidad(
                totalCantidad
        );

        resultado.setTotalValor(
                totalValor
        );

        // =====================================================
        // FILAS
        // =====================================================

        List<MatrizRodamientoFilaDTO> filas =
                new ArrayList<>();

        for (String anterior : CATEGORIAS) {

            MatrizRodamientoFilaDTO fila =
                    construirFila(
                            anterior,
                            mapa
                    );

            filas.add(
                    fila
            );
        }

        resultado.setFilas(
                filas
        );

        // =====================================================
        // TOTALES DE COLUMNAS
        // =====================================================

        Map<String, Integer>
                totalesCantidadColumna =
                new LinkedHashMap<>();

        Map<String, BigDecimal>
                totalesValorColumna =
                new LinkedHashMap<>();

        for (String partida : CATEGORIAS) {

            int cantidadColumna = 0;

            BigDecimal valorColumna =
                    BigDecimal.ZERO;

            for (String anterior : CATEGORIAS) {

                MatrizRodamientoCeldaDTO celda =
                        mapa.get(
                                llave(
                                        anterior,
                                        partida
                                )
                        );

                cantidadColumna +=
                        valorEntero(
                                celda.getCantidad()
                        );

                valorColumna =
                        valorColumna.add(
                                valorDecimal(
                                        celda.getValor()
                                )
                        );
            }

            totalesCantidadColumna.put(
                    partida,
                    cantidadColumna
            );

            totalesValorColumna.put(
                    partida,
                    valorColumna
            );
        }

        resultado.setTotalesCantidadPorCategoriaPartida(
                totalesCantidadColumna
        );

        resultado.setTotalesValorPorCategoriaPartida(
                totalesValorColumna
        );

        return resultado;
    }


    // =========================================================
    // CONSTRUIR FILA
    // =========================================================

    private MatrizRodamientoFilaDTO construirFila(
            String categoriaAnterior,
            Map<String, MatrizRodamientoCeldaDTO> mapa
    ) {

        MatrizRodamientoFilaDTO fila =
                new MatrizRodamientoFilaDTO();

        fila.setCategoriaAnterior(
                categoriaAnterior
        );

        List<MatrizRodamientoCeldaDTO> celdasFila =
                new ArrayList<>();

        int totalCantidad = 0;

        BigDecimal totalValor =
                BigDecimal.ZERO;

        // =====================================================
        // TOTAL DE FILA
        // =====================================================

        for (String partida : CATEGORIAS) {

            MatrizRodamientoCeldaDTO celda =
                    mapa.get(
                            llave(
                                    categoriaAnterior,
                                    partida
                            )
                    );

            totalCantidad +=
                    valorEntero(
                            celda.getCantidad()
                    );

            totalValor =
                    totalValor.add(
                            valorDecimal(
                                    celda.getValor()
                            )
                    );
        }

        fila.setTotalCantidad(
                totalCantidad
        );

        fila.setTotalValor(
                totalValor
        );

        // =====================================================
        // MEJORA / PERMANENCIA / DETERIORO
        // =====================================================

        int posicionAnterior =
                posicionCategoria(
                        categoriaAnterior
                );

        int cantidadMejora = 0;
        int cantidadPermanencia = 0;
        int cantidadDeterioro = 0;

        BigDecimal valorMejora =
                BigDecimal.ZERO;

        BigDecimal valorPermanencia =
                BigDecimal.ZERO;

        BigDecimal valorDeterioro =
                BigDecimal.ZERO;

        // =====================================================
        // CELDAS
        // =====================================================

        for (String partida : CATEGORIAS) {

            MatrizRodamientoCeldaDTO celda =
                    mapa.get(
                            llave(
                                    categoriaAnterior,
                                    partida
                            )
                    );

            int cantidad =
                    valorEntero(
                            celda.getCantidad()
                    );

            BigDecimal valor =
                    valorDecimal(
                            celda.getValor()
                    );

            celda.setCantidad(
                    cantidad
            );

            celda.setValor(
                    valor
            );

            celda.setPorcentajeCantidad(
                    porcentaje(
                            BigDecimal.valueOf(
                                    cantidad
                            ),
                            BigDecimal.valueOf(
                                    totalCantidad
                            )
                    )
            );

            celda.setPorcentajeValor(
                    porcentaje(
                            valor,
                            totalValor
                    )
            );

            celdasFila.add(
                    celda
            );

            int posicionPartida =
                    posicionCategoria(
                            partida
                    );

            if (posicionPartida < posicionAnterior) {

                cantidadMejora +=
                        cantidad;

                valorMejora =
                        valorMejora.add(
                                valor
                        );

            } else if (posicionPartida == posicionAnterior) {

                cantidadPermanencia +=
                        cantidad;

                valorPermanencia =
                        valorPermanencia.add(
                                valor
                        );

            } else {

                cantidadDeterioro +=
                        cantidad;

                valorDeterioro =
                        valorDeterioro.add(
                                valor
                        );
            }
        }

        fila.setCeldas(
                celdasFila
        );

        // =====================================================
        // PROBABILIDADES CANTIDAD
        // =====================================================

        fila.setProbabilidadMejoraCantidad(
                porcentaje(
                        BigDecimal.valueOf(
                                cantidadMejora
                        ),
                        BigDecimal.valueOf(
                                totalCantidad
                        )
                )
        );

        fila.setProbabilidadPermanenciaCantidad(
                porcentaje(
                        BigDecimal.valueOf(
                                cantidadPermanencia
                        ),
                        BigDecimal.valueOf(
                                totalCantidad
                        )
                )
        );

        fila.setProbabilidadDeterioroCantidad(
                porcentaje(
                        BigDecimal.valueOf(
                                cantidadDeterioro
                        ),
                        BigDecimal.valueOf(
                                totalCantidad
                        )
                )
        );

        // =====================================================
        // PROBABILIDADES VALOR
        // =====================================================

        fila.setProbabilidadMejoraValor(
                porcentaje(
                        valorMejora,
                        totalValor
                )
        );

        fila.setProbabilidadPermanenciaValor(
                porcentaje(
                        valorPermanencia,
                        totalValor
                )
        );

        fila.setProbabilidadDeterioroValor(
                porcentaje(
                        valorDeterioro,
                        totalValor
                )
        );

        return fila;
    }


    // =========================================================
    // NORMALIZAR CELDA
    // =========================================================

    private void normalizarCelda(
            MatrizRodamientoCeldaDTO celda
    ) {

        if (celda.getCantidad() == null) {

            celda.setCantidad(
                    0
            );
        }

        if (celda.getValor() == null) {

            celda.setValor(
                    BigDecimal.ZERO
            );
        }

        if (celda.getPorcentajeCantidad() == null) {

            celda.setPorcentajeCantidad(
                    BigDecimal.ZERO
            );
        }

        if (celda.getPorcentajeValor() == null) {

            celda.setPorcentajeValor(
                    BigDecimal.ZERO
            );
        }
    }


    // =========================================================
    // NUEVA CELDA CERO
    // =========================================================

    private MatrizRodamientoCeldaDTO nuevaCeldaCero(
            String anterior,
            String partida
    ) {

        MatrizRodamientoCeldaDTO celda =
                new MatrizRodamientoCeldaDTO();

        celda.setCategoriaAnterior(
                anterior
        );

        celda.setCategoriaPartida(
                partida
        );

        celda.setCantidad(
                0
        );

        celda.setValor(
                BigDecimal.ZERO
        );

        celda.setPorcentajeCantidad(
                BigDecimal.ZERO
        );

        celda.setPorcentajeValor(
                BigDecimal.ZERO
        );

        return celda;
    }


    // =========================================================
    // PORCENTAJE
    // =========================================================

    private BigDecimal porcentaje(
            BigDecimal valor,
            BigDecimal total
    ) {

        BigDecimal valorSeguro =
                valorDecimal(
                        valor
                );

        BigDecimal totalSeguro =
                valorDecimal(
                        total
                );

        if (
                totalSeguro.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {

            return BigDecimal.ZERO
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return valorSeguro
                .multiply(
                        CIEN
                )
                .divide(
                        totalSeguro,
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // =========================================================
    // ENTERO SEGURO
    // =========================================================

    private int valorEntero(
            Integer valor
    ) {

        return valor != null
                ? valor
                : 0;
    }


    // =========================================================
    // DECIMAL SEGURO
    // =========================================================

    private BigDecimal valorDecimal(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }


    // =========================================================
    // POSICIÓN CATEGORÍA
    // =========================================================

    private int posicionCategoria(
            String categoria
    ) {

        return CATEGORIAS.indexOf(
                categoria
        );
    }


    // =========================================================
    // LLAVE
    // =========================================================

    private String llave(
            String anterior,
            String partida
    ) {

        return anterior
                + "->"
                + partida;
    }


    // =========================================================
    // VALIDAR CORTES
    // =========================================================

    private void validarCortes(
            String tipoPartida,
            LocalDate fechaPartida,
            LocalDate fechaComparacion
    ) {

        if (
                !repository.existeCorte(
                        fechaComparacion
                )
        ) {

            throw new IllegalArgumentException(
                    "No existe cierre para la fecha de comparación "
                            + fechaComparacion
                            + "."
            );
        }

        if (
                "CORTE".equals(tipoPartida)
                        &&
                        !repository.existeCorte(
                                fechaPartida
                        )
        ) {

            throw new IllegalArgumentException(
                    "No existe cierre para la fecha de partida "
                            + fechaPartida
                            + "."
            );
        }
    }


    // =========================================================
    // NORMALIZAR TIPO
    // =========================================================

    private String normalizarTipoPartida(
            String tipoPartida
    ) {

        if (
                tipoPartida == null
                        ||
                        tipoPartida.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "tipoPartida es obligatorio."
            );
        }

        String tipo =
                tipoPartida
                        .trim()
                        .toUpperCase();

        if (
                !"ACTUAL".equals(tipo)
                        &&
                        !"CORTE".equals(tipo)
        ) {

            throw new IllegalArgumentException(
                    "tipoPartida debe ser ACTUAL o CORTE."
            );
        }

        return tipo;
    }


    // =========================================================
    // NORMALIZAR CATEGORÍA
    // =========================================================

    private String normalizarCategoria(
            String categoria
    ) {

        if (
                categoria == null
                        ||
                        categoria.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "La categoría es obligatoria."
            );
        }

        String valor =
                categoria
                        .trim()
                        .toUpperCase();

        if (!CATEGORIAS.contains(valor)) {

            throw new IllegalArgumentException(
                    "La categoría debe ser A, B, C, D o E."
            );
        }

        return valor;
    }


    // =========================================================
    // VALIDAR FECHAS
    // =========================================================

    private void validarFechas(
            String tipoPartida,
            LocalDate fechaPartida,
            LocalDate fechaComparacion
    ) {

        if (fechaComparacion == null) {

            throw new IllegalArgumentException(
                    "fechaComparacion es obligatoria."
            );
        }

        if ("CORTE".equals(tipoPartida)) {

            if (fechaPartida == null) {

                throw new IllegalArgumentException(
                        "fechaPartida es obligatoria cuando tipoPartida es CORTE."
                );
            }

            if (
                    !fechaPartida.isAfter(
                            fechaComparacion
                    )
            ) {

                throw new IllegalArgumentException(
                        "fechaPartida debe ser posterior a fechaComparacion."
                );
            }
        }
    }
}