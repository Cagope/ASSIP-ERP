package co.assip.erp.cartera.analisis.vectorcomportamiento.corte;

import co.assip.erp.cartera.analisis.vectorcomportamiento.corte.dto.VectorComportamientoCorteDetalleDTO;
import co.assip.erp.cartera.analisis.vectorcomportamiento.corte.dto.VectorComportamientoCorteResumenDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * Servicio del proceso de análisis
 * Vector de Comportamiento por Corte.
 *
 * Permite reconstruir el Vector de Comportamiento
 * tomando una fecha histórica de cierre
 * como fecha de referencia actual.
 *
 * Reglas fundamentales:
 *
 * 1. La población se determina exclusivamente
 *    con los créditos cuyo saldo en la fecha
 *    de corte seleccionada es mayor que cero.
 *
 * 2. No se utiliza el saldo actual del maestro
 *    de cartera para determinar la población.
 *
 * 3. La fecha de corte seleccionada constituye
 *    la posición 1 del Vector y se trata
 *    conceptualmente como el ACTUAL.
 *
 * 4. Después de la posición de referencia
 *    se consultan máximo 12 cierres anteriores.
 *
 * 5. El Vector puede contener:
 *
 *      posición 1:
 *          corte seleccionado.
 *
 *      posiciones 2..13:
 *          máximo 12 cierres anteriores,
 *          ordenados del más reciente
 *          al más antiguo.
 *
 * 6. Toda la información financiera,
 *    de comportamiento y riesgo proviene
 *    exclusivamente de los cierres históricos.
 *
 * Este servicio es exclusivamente de lectura.
 */
@Service
@Transactional(readOnly = true)
public class VectorComportamientoCorteService {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final VectorComportamientoCorteRepository repository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public VectorComportamientoCorteService(
            VectorComportamientoCorteRepository repository
    ) {

        this.repository =
                repository;
    }


    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    /**
     * Consulta las fechas históricas disponibles
     * para construir el Vector de Comportamiento.
     *
     * Solo devuelve fechas que contienen por lo menos
     * un crédito con saldo mayor que cero.
     */
    public List<LocalDate>
    listarFechasCorteDisponibles() {

        return repository
                .listarFechasCorteDisponibles();
    }


    // =========================================================
    // CARTERA DEL CORTE - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * para todos los créditos pertenecientes
     * a la población del corte seleccionado.
     *
     * La fecha seleccionada actúa como referencia actual.
     *
     * Los indicadores históricos consideran
     * máximo los 12 cierres anteriores.
     *
     * Devuelve una fila por crédito.
     */
    public List<VectorComportamientoCorteResumenDTO>
    listarResumenPorCorte(
            LocalDate fechaCorte
    ) {

        validarFechaCorte(
                fechaCorte
        );

        return repository
                .listarResumenPorCorte(
                        fechaCorte
                );
    }


    // =========================================================
    // CARTERA DEL CORTE - DETALLE
    // =========================================================

    /**
     * Consulta el Vector detallado de los créditos
     * pertenecientes a la población del corte seleccionado.
     *
     * Estructura por crédito:
     *
     * posición 1:
     *     corte seleccionado tratado como ACTUAL.
     *
     * posiciones 2..13:
     *     máximo 12 cierres anteriores,
     *     ordenados del más reciente
     *     al más antiguo.
     *
     * Devuelve una fila por crédito y posición.
     */
    public List<VectorComportamientoCorteDetalleDTO>
    listarDetallePorCorte(
            LocalDate fechaCorte
    ) {

        validarFechaCorte(
                fechaCorte
        );

        return repository
                .listarDetallePorCorte(
                        fechaCorte
                );
    }


    // =========================================================
    // ASOCIADO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen de los créditos
     * de un asociado que pertenecían
     * a la población del corte seleccionado.
     *
     * Cada crédito se analiza tomando
     * el corte seleccionado como referencia actual
     * y máximo 12 cierres anteriores.
     */
    public List<VectorComportamientoCorteResumenDTO>
    listarResumenPorPersona(
            LocalDate fechaCorte,
            Integer idDatosPersonal
    ) {

        validarFechaCorte(
                fechaCorte
        );

        validarIdDatosPersonal(
                idDatosPersonal
        );

        return repository
                .listarResumenPorPersona(
                        fechaCorte,
                        idDatosPersonal
                );
    }


    // =========================================================
    // ASOCIADO - DETALLE
    // =========================================================

    /**
     * Consulta el Vector detallado
     * de los créditos de un asociado
     * pertenecientes a la población
     * del corte seleccionado.
     *
     * Por cada crédito devuelve:
     *
     * posición 1:
     *     corte seleccionado.
     *
     * posiciones 2..13:
     *     máximo 12 cierres anteriores.
     */
    public List<VectorComportamientoCorteDetalleDTO>
    listarDetallePorPersona(
            LocalDate fechaCorte,
            Integer idDatosPersonal
    ) {

        validarFechaCorte(
                fechaCorte
        );

        validarIdDatosPersonal(
                idDatosPersonal
        );

        return repository
                .listarDetallePorPersona(
                        fechaCorte,
                        idDatosPersonal
                );
    }


    // =========================================================
    // CRÉDITO - RESUMEN
    // =========================================================

    /**
     * Consulta el resumen del Vector de Comportamiento
     * para un crédito específico
     * en una fecha de corte.
     *
     * El crédito debe pertenecer a la población
     * de la fecha seleccionada.
     *
     * Si el crédito no tenía saldo mayor que cero
     * en dicho corte, devuelve Optional.empty().
     */
    public Optional<VectorComportamientoCorteResumenDTO>
    buscarResumenPorCredito(
            LocalDate fechaCorte,
            Integer idCarteraCredito
    ) {

        validarFechaCorte(
                fechaCorte
        );

        validarIdCarteraCredito(
                idCarteraCredito
        );

        List<VectorComportamientoCorteResumenDTO> resultados =
                repository
                        .listarResumenPorCredito(
                                fechaCorte,
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
     * Consulta el Vector detallado
     * de un crédito perteneciente
     * a la población del corte seleccionado.
     *
     * El resultado contiene:
     *
     * posición 1:
     *     corte seleccionado tratado como ACTUAL.
     *
     * posiciones 2..13:
     *     máximo 12 cierres anteriores,
     *     ordenados del más reciente
     *     al más antiguo.
     */
    public List<VectorComportamientoCorteDetalleDTO>
    listarDetallePorCredito(
            LocalDate fechaCorte,
            Integer idCarteraCredito
    ) {

        validarFechaCorte(
                fechaCorte
        );

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return repository
                .listarDetallePorCredito(
                        fechaCorte,
                        idCarteraCredito
                );
    }


    // =========================================================
    // EXISTENCIA DEL CRÉDITO EN EL CORTE
    // =========================================================

    /**
     * Verifica si un crédito pertenecía
     * a la población del Vector
     * en la fecha seleccionada.
     *
     * Condición poblacional:
     *
     * saldo_credito_fecha_corte > 0.
     */
    public boolean existeCreditoEnCorte(
            LocalDate fechaCorte,
            Integer idCarteraCredito
    ) {

        validarFechaCorte(
                fechaCorte
        );

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return repository
                .existeCreditoEnCorte(
                        fechaCorte,
                        idCarteraCredito
                );
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    /**
     * Valida que la fecha de corte haya sido informada.
     *
     * La existencia real de la fecha dentro
     * de los cierres disponibles se resuelve
     * mediante las consultas del Repository.
     */
    private void validarFechaCorte(
            LocalDate fechaCorte
    ) {

        if (fechaCorte == null) {

            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }
    }


    /**
     * Valida el identificador de la persona.
     */
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


    /**
     * Valida el identificador del crédito.
     */
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