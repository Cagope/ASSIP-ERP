package co.assip.erp.cartera.analisis.vectorcomportamiento.actual;

import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoDetalleDTO;
import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoResumenDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * Servicio del proceso de análisis
 * Vector de Comportamiento Actual.
 *
 * Permite consultar el comportamiento de:
 *
 * - Toda la cartera actualmente con saldo.
 * - Todos los créditos actualmente con saldo de un asociado.
 * - Un crédito específico actualmente con saldo.
 *
 * La población está compuesta exclusivamente por créditos
 * cuyo saldo actual en cartera.carteras_creditos
 * es mayor que cero.
 *
 * Para cada crédito el Vector de Comportamiento Actual contiene:
 *
 * - Una posición ACTUAL.
 * - Hasta 12 cierres históricos mensuales.
 *
 * Por lo tanto, cada crédito puede tener como máximo
 * 13 posiciones dentro del vector.
 *
 * Este servicio es exclusivamente de lectura.
 */
@Service
@Transactional(readOnly = true)
public class VectorComportamientoActualService {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final VectorComportamientoActualRepository repository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public VectorComportamientoActualService(
            VectorComportamientoActualRepository repository
    ) {

        this.repository =
                repository;
    }


    // =========================================================
    // CARTERA ACTUAL - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * para toda la cartera actualmente con saldo.
     *
     * Devuelve una fila por crédito.
     *
     * Los indicadores históricos del resumen se calculan
     * únicamente sobre un máximo de 12 cierres históricos.
     */
    public List<VectorComportamientoResumenDTO>
    listarResumenCarteraActiva() {

        return repository
                .listarResumenCarteraActiva();
    }


    // =========================================================
    // CARTERA ACTUAL - DETALLE
    // =========================================================

    /**
     * Consulta el Vector de Comportamiento detallado
     * de todos los créditos actualmente con saldo.
     *
     * Cada crédito contiene:
     *
     * - Posición 1: ACTUAL.
     * - Posiciones 2 a 13: hasta 12 cierres históricos,
     *   ordenados desde el más reciente hacia el más antiguo.
     *
     * Devuelve una fila por crédito y posición del vector.
     */
    public List<VectorComportamientoDetalleDTO>
    listarDetalleCarteraActiva() {

        return repository
                .listarDetalleCarteraActiva();
    }


    // =========================================================
    // ASOCIADO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen de todos los créditos
     * actualmente con saldo de un asociado.
     *
     * Devuelve una fila por crédito.
     */
    public List<VectorComportamientoResumenDTO>
    listarResumenPorPersona(
            Integer idDatosPersonal
    ) {

        validarIdDatosPersonal(
                idDatosPersonal
        );

        return repository
                .listarResumenPorPersona(
                        idDatosPersonal
                );
    }


    // =========================================================
    // ASOCIADO - DETALLE
    // =========================================================

    /**
     * Consulta el Vector de Comportamiento detallado
     * de los créditos actualmente con saldo
     * de un asociado.
     *
     * Cada crédito contiene:
     *
     * - ACTUAL en posición 1.
     * - Hasta 12 cierres históricos en posiciones 2 a 13.
     */
    public List<VectorComportamientoDetalleDTO>
    listarDetallePorPersona(
            Integer idDatosPersonal
    ) {

        validarIdDatosPersonal(
                idDatosPersonal
        );

        return repository
                .listarDetallePorPersona(
                        idDatosPersonal
                );
    }


    // =========================================================
    // CRÉDITO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * para un crédito específico.
     *
     * Si el crédito no pertenece actualmente
     * a la población del Vector, devuelve Optional.empty().
     */
    public Optional<VectorComportamientoResumenDTO>
    buscarResumenPorCredito(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        List<VectorComportamientoResumenDTO> resultados =
                repository
                        .listarResumenPorCredito(
                                idCarteraCredito
                        );

        if (resultados.isEmpty()) {

            return Optional.empty();
        }

        return Optional.of(
                resultados.get(0)
        );
    }


    // =========================================================
    // CRÉDITO - DETALLE
    // =========================================================

    /**
     * Consulta el Vector de Comportamiento detallado
     * de un crédito actualmente con saldo.
     *
     * El resultado contiene:
     *
     * - Posición 1: estado ACTUAL.
     * - Posiciones 2 a 13: hasta 12 cierres históricos.
     *
     * Los cierres históricos se ordenan desde
     * el más reciente hacia el más antiguo.
     */
    public List<VectorComportamientoDetalleDTO>
    listarDetallePorCredito(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return repository
                .listarDetallePorCredito(
                        idCarteraCredito
                );
    }


    // =========================================================
    // EXISTENCIA EN VECTOR ACTUAL
    // =========================================================

    /**
     * Verifica si un crédito pertenece actualmente
     * a la población del Vector de Comportamiento.
     *
     * La población está compuesta únicamente por créditos
     * cuyo saldo actual en el maestro es mayor que cero.
     */
    public boolean existeCreditoEnVector(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return repository
                .existeCreditoEnVector(
                        idCarteraCredito
                );
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarIdDatosPersonal(
            Integer idDatosPersonal
    ) {

        if (
                idDatosPersonal == null
                        ||
                        idDatosPersonal <= 0
        ) {

            throw new IllegalArgumentException(
                    "El idDatosPersonal es obligatorio y debe ser mayor que cero."
            );
        }
    }


    private void validarIdCarteraCredito(
            Integer idCarteraCredito
    ) {

        if (
                idCarteraCredito == null
                        ||
                        idCarteraCredito <= 0
        ) {

            throw new IllegalArgumentException(
                    "El idCarteraCredito es obligatorio y debe ser mayor que cero."
            );
        }
    }
}