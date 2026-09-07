package co.assip.erp.depositos.analisis.concentracion;

import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosAgenciaDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosAsociadoDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosFormaDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosRequestDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosResponseDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosResumenDTO;
import co.assip.erp.depositos.analisis.concentracion.dto.ConcentracionDepositosTopDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcentracionDepositosService {

    private static final BigDecimal CIEN =
            new BigDecimal("100");

    private static final int ESCALA_PORCENTAJE = 4;

    private final ConcentracionDepositosRepository repository;


    public ConcentracionDepositosResponseDTO consultar(
            ConcentracionDepositosRequestDTO request
    ) {

        LocalDate fechaCorte =
                validarRequest(request);

        normalizarRequest(request);


        /*
         * =====================================================
         * CONSULTAS BASE
         * =====================================================
         */

        ConcentracionDepositosRepository.ResumenBase resumenBase =
                repository.obtenerResumen(request);

        List<ConcentracionDepositosAgenciaDTO> agencias =
                repository.listarAgencias(request);

        List<ConcentracionDepositosFormaDTO> formas =
                repository.listarFormas(request);

        List<ConcentracionDepositosAsociadoDTO> asociados =
                repository.listarAsociados(request);

        BigDecimal mayorSaldoAsociado =
                repository.obtenerMayorSaldoAsociado(request);


        boolean todasLasFormas =
                request.getIdFormaAhorro() == null
                        || request.getIdFormaAhorro() == 0;


        /*
         * =====================================================
         * CLASIFICACIÓN DEL SALDO
         * =====================================================
         *
         * Cuando se consultan todas las formas:
         * - la población principal contiene captaciones;
         * - aportes sociales se consultan aparte.
         *
         * Cuando se consulta una forma:
         * - determinamos por tipo_captacion_forma si corresponde
         *   a aportes sociales o a una captación ordinaria.
         */

        BigDecimal saldoCaptaciones;
        BigDecimal saldoAportes;
        BigDecimal saldoTotal;


        if (todasLasFormas) {

            ConcentracionDepositosRepository.SaldoAportesBase aportes =
                    repository.obtenerAportes(request);

            saldoCaptaciones =
                    nvl(
                            resumenBase.saldo()
                    );

            saldoAportes =
                    nvl(
                            aportes.saldo()
                    );

            saldoTotal =
                    saldoCaptaciones.add(
                            saldoAportes
                    );

        } else {

            boolean formaEsAporte =
                    !formas.isEmpty()
                            && "1".equals(
                            trim(
                                    formas
                                            .get(0)
                                            .getTipoCaptacionForma()
                            )
                    );


            if (formaEsAporte) {

                saldoCaptaciones =
                        BigDecimal.ZERO;

                saldoAportes =
                        nvl(
                                resumenBase.saldo()
                        );

            } else {

                saldoCaptaciones =
                        nvl(
                                resumenBase.saldo()
                        );

                saldoAportes =
                        BigDecimal.ZERO;
            }


            saldoTotal =
                    nvl(
                            resumenBase.saldo()
                    );
        }


        /*
         * =====================================================
         * PROMEDIOS
         * =====================================================
         *
         * Los promedios corresponden siempre a la población
         * efectivamente seleccionada para el análisis.
         */

        BigDecimal saldoPromedioCuenta =
                nvl(
                        resumenBase.saldoPromedioCuenta()
                );


        BigDecimal saldoPromedioAsociado =
                promedio(
                        nvl(
                                resumenBase.saldo()
                        ),
                        resumenBase.cantidadAsociados()
                );


        /*
         * =====================================================
         * RESUMEN
         * =====================================================
         */

        ConcentracionDepositosResumenDTO resumen =
                ConcentracionDepositosResumenDTO
                        .builder()

                        .fechaCorte(
                                fechaCorte
                        )

                        .cantidadCuentas(
                                nvl(
                                        resumenBase.cantidadCuentas()
                                )
                        )

                        .cantidadAsociados(
                                nvl(
                                        resumenBase.cantidadAsociados()
                                )
                        )

                        .saldoCaptaciones(
                                saldoCaptaciones
                        )

                        .saldoAportes(
                                saldoAportes
                        )

                        .saldoTotal(
                                saldoTotal
                        )

                        .saldoPromedioPorCuenta(
                                saldoPromedioCuenta
                        )

                        .saldoPromedioPorAsociado(
                                saldoPromedioAsociado
                        )

                        .mayorSaldoAsociado(
                                nvl(
                                        mayorSaldoAsociado
                                )
                        )

                        .build();


        /*
         * =====================================================
         * CONCENTRACIÓN TOP
         * =====================================================
         */

        List<ConcentracionDepositosTopDTO> concentracion =
                construirConcentracion(
                        asociados,
                        nvl(
                                resumenBase.saldo()
                        )
                );


        /*
         * =====================================================
         * RESPUESTA
         * =====================================================
         */

        return ConcentracionDepositosResponseDTO
                .builder()

                .resumen(
                        resumen
                )

                .agencias(
                        agencias
                )

                .formas(
                        formas
                )

                .concentracion(
                        concentracion
                )

                .asociados(
                        asociados
                )

                .build();
    }


    /*
     * =========================================================
     * TOP 10 / TOP 20 / TOP 50 / RESTO
     * =========================================================
     *
     * Los Top son acumulativos:
     *
     * TOP_10 = posiciones 1 a 10
     * TOP_20 = posiciones 1 a 20
     * TOP_50 = posiciones 1 a 50
     * RESTO  = posiciones 51 en adelante
     */

    private List<ConcentracionDepositosTopDTO> construirConcentracion(
            List<ConcentracionDepositosAsociadoDTO> asociados,
            BigDecimal saldoTotal
    ) {

        List<ConcentracionDepositosTopDTO> resultado =
                new ArrayList<>();


        resultado.add(
                construirTop(
                        "TOP_10",
                        asociados,
                        10,
                        saldoTotal
                )
        );


        resultado.add(
                construirTop(
                        "TOP_20",
                        asociados,
                        20,
                        saldoTotal
                )
        );


        resultado.add(
                construirTop(
                        "TOP_50",
                        asociados,
                        50,
                        saldoTotal
                )
        );


        resultado.add(
                construirResto(
                        asociados,
                        50,
                        saldoTotal
                )
        );


        return resultado;
    }


    private ConcentracionDepositosTopDTO construirTop(
            String grupo,
            List<ConcentracionDepositosAsociadoDTO> asociados,
            int limite,
            BigDecimal saldoTotal
    ) {

        int cantidad =
                Math.min(
                        limite,
                        asociados.size()
                );


        BigDecimal saldo =
                asociados
                        .stream()

                        .limit(
                                limite
                        )

                        .map(
                                ConcentracionDepositosAsociadoDTO::getSaldo
                        )

                        .map(
                                this::nvl
                        )

                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        return ConcentracionDepositosTopDTO
                .builder()

                .grupo(
                        grupo
                )

                .cantidadAsociados(
                        cantidad
                )

                .saldo(
                        saldo
                )

                .porcentajeParticipacion(
                        porcentaje(
                                saldo,
                                saldoTotal
                        )
                )

                .build();
    }


    private ConcentracionDepositosTopDTO construirResto(
            List<ConcentracionDepositosAsociadoDTO> asociados,
            int desde,
            BigDecimal saldoTotal
    ) {

        int cantidad =
                Math.max(
                        asociados.size() - desde,
                        0
                );


        BigDecimal saldo =
                asociados
                        .stream()

                        .skip(
                                desde
                        )

                        .map(
                                ConcentracionDepositosAsociadoDTO::getSaldo
                        )

                        .map(
                                this::nvl
                        )

                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        return ConcentracionDepositosTopDTO
                .builder()

                .grupo(
                        "RESTO"
                )

                .cantidadAsociados(
                        cantidad
                )

                .saldo(
                        saldo
                )

                .porcentajeParticipacion(
                        porcentaje(
                                saldo,
                                saldoTotal
                        )
                )

                .build();
    }


    /*
     * =========================================================
     * VALIDACIONES
     * =========================================================
     */

    private LocalDate validarRequest(
            ConcentracionDepositosRequestDTO request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Debe informar los parámetros del análisis."
            );
        }


        if (
                request.getFechaCorte() == null
                        || request.getFechaCorte().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Debe informar la fecha de corte."
            );
        }


        LocalDate fechaCorte;


        try {

            fechaCorte =
                    LocalDate.parse(
                            request
                                    .getFechaCorte()
                                    .trim()
                    );

        } catch (DateTimeParseException ex) {

            throw new IllegalArgumentException(
                    "La fecha de corte debe tener formato yyyy-MM-dd."
            );
        }


        if (
                request.getIdAgencia() != null
                        && request.getIdAgencia() < 0
        ) {

            throw new IllegalArgumentException(
                    "La agencia no es válida."
            );
        }


        if (
                request.getIdFormaAhorro() != null
                        && request.getIdFormaAhorro() < 0
        ) {

            throw new IllegalArgumentException(
                    "La forma de ahorro no es válida."
            );
        }


        return fechaCorte;
    }


    private void normalizarRequest(
            ConcentracionDepositosRequestDTO request
    ) {

        if (request.getIdAgencia() == null) {

            request.setIdAgencia(
                    0
            );
        }


        if (request.getIdFormaAhorro() == null) {

            request.setIdFormaAhorro(
                    0
            );
        }


        request.setFechaCorte(
                request
                        .getFechaCorte()
                        .trim()
        );
    }


    /*
     * =========================================================
     * UTILIDADES
     * =========================================================
     */

    private BigDecimal promedio(
            BigDecimal saldo,
            Integer cantidad
    ) {

        if (
                cantidad == null
                        || cantidad <= 0
        ) {

            return BigDecimal.ZERO;
        }


        return saldo.divide(
                BigDecimal.valueOf(
                        cantidad
                ),
                2,
                RoundingMode.HALF_UP
        );
    }


    private BigDecimal porcentaje(
            BigDecimal valor,
            BigDecimal total
    ) {

        if (
                total == null
                        || total.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {

            return BigDecimal.ZERO;
        }


        return nvl(
                valor
        )
                .multiply(
                        CIEN
                )
                .divide(
                        total,
                        ESCALA_PORCENTAJE,
                        RoundingMode.HALF_UP
                );
    }


    private BigDecimal nvl(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }


    private Integer nvl(
            Integer valor
    ) {

        return valor == null
                ? 0
                : valor;
    }


    private String trim(
            String valor
    ) {

        return valor == null
                ? ""
                : valor.trim();
    }
}