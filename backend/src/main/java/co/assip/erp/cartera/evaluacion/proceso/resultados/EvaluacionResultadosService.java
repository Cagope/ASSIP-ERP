package co.assip.erp.cartera.evaluacion.proceso.resultados;

import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoCreditoDTO;
import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoDetalleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoHojaVidaDTO;
import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoMorosidadDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluacionResultadosService {

    private final EvaluacionResultadosRepository repository;


    // =========================================================
    // LISTAR RESULTADOS DE UNA EVALUACIÓN
    // =========================================================

    public List<EvaluacionResultadoCreditoDTO> listarResultados(
            Integer idEvaluacionCartera
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        return repository.listarResultados(
                idEvaluacionCartera
        );
    }


    // =========================================================
    // BUSCAR RESULTADO CONSOLIDADO
    //
    // Se utiliza principalmente para:
    // - consultar un crédito evaluado;
    // - construir la impresión individual.
    // =========================================================

    public EvaluacionResultadoCreditoDTO buscarResultadoPorId(
            Integer idEvaluacionCartera,
            Integer idEvaluacionCarteraCredito
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        validarIdEvaluacionCredito(
                idEvaluacionCarteraCredito
        );

        EvaluacionResultadoCreditoDTO resultado =
                repository.buscarResultadoPorId(
                        idEvaluacionCarteraCredito
                );

        if (resultado == null) {
            throw new IllegalArgumentException(
                    "No existe el resultado de evaluación solicitado."
            );
        }

        validarPertenenciaEvaluacion(
                idEvaluacionCartera,
                resultado
        );

        return resultado;
    }


    // =========================================================
    // LISTAR DETALLE DE CRITERIOS
    //
    // Devuelve los criterios aplicados al crédito evaluado
    // ordenados según la configuración de evaluación.
    // =========================================================

    public List<EvaluacionResultadoDetalleDTO> listarDetalle(
            Integer idEvaluacionCartera,
            Integer idEvaluacionCarteraCredito
    ) {

        /*
         * Primero se valida el resultado consolidado.
         *
         * Además de comprobar que existe, esta llamada garantiza
         * que el crédito solicitado pertenece realmente a la
         * evaluación indicada en la URL.
         */
        buscarResultadoPorId(
                idEvaluacionCartera,
                idEvaluacionCarteraCredito
        );

        return repository.listarDetalle(
                idEvaluacionCarteraCredito
        );
    }


    // =========================================================
    // LISTAR DETALLE MASIVO POR ACCIÓN
    //
    // Utilizado para impresión masiva de formatos:
    //
    // R = Reclasificar
    // H = Habilitar
    // M = Mantener
    //
    // Devuelve en una sola consulta todos los criterios
    // correspondientes a los créditos de la acción indicada.
    // =========================================================

    public List<EvaluacionResultadoDetalleDTO> listarDetallePorAccion(
            Integer idEvaluacionCartera,
            String accionEvaluacion
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        String accion =
                validarAccionEvaluacion(
                        accionEvaluacion
                );

        return repository.listarDetallePorAccion(
                idEvaluacionCartera,
                accion
        );
    }

    // =========================================================
    // LISTAR FOTOGRAFÍA DE HOJA DE VIDA
    //
    // Devuelve una fila por asociado incluido en la evaluación.
    //
    // La información corresponde a la fotografía definitiva
    // de Hoja de Vida utilizada para la misma fecha de corte.
    // =========================================================

    public List<EvaluacionResultadoHojaVidaDTO> listarFotoHojaVida(
            Integer idEvaluacionCartera
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        return repository.listarFotoHojaVida(
                idEvaluacionCartera
        );
    }

    // =========================================================
    // LISTAR INSUMO DE MOROSIDAD DEL EXTRACTO
    //
    // Devuelve los comprobantes consolidados utilizados como
    // insumo para el criterio 401 - Servicio de la deuda.
    //
    // La consulta conserva la misma ventana temporal y la misma
    // lógica de consolidación utilizada por el motor.
    // =========================================================

    public List<EvaluacionResultadoMorosidadDTO> listarMorosidadExtracto(
            Integer idEvaluacionCartera
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        return repository.listarMorosidadExtracto(
                idEvaluacionCartera
        );
    }


    // =========================================================
    // VALIDAR IDENTIFICADOR DE EVALUACIÓN
    // =========================================================

    private void validarIdEvaluacion(
            Integer idEvaluacionCartera
    ) {

        if (
                idEvaluacionCartera == null
                        || idEvaluacionCartera <= 0
        ) {
            throw new IllegalArgumentException(
                    "El identificador de la evaluación de cartera "
                            + "es obligatorio."
            );
        }
    }


    // =========================================================
    // VALIDAR IDENTIFICADOR DEL CRÉDITO EVALUADO
    // =========================================================

    private void validarIdEvaluacionCredito(
            Integer idEvaluacionCarteraCredito
    ) {

        if (
                idEvaluacionCarteraCredito == null
                        || idEvaluacionCarteraCredito <= 0
        ) {
            throw new IllegalArgumentException(
                    "El identificador del crédito evaluado "
                            + "es obligatorio."
            );
        }
    }


    // =========================================================
    // VALIDAR ACCIÓN DE EVALUACIÓN
    //
    // Únicamente se permiten:
    // R = Reclasificar
    // H = Habilitar
    // M = Mantener
    // =========================================================

    private String validarAccionEvaluacion(
            String accionEvaluacion
    ) {

        if (
                accionEvaluacion == null
                        || accionEvaluacion.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La acción de evaluación es obligatoria."
            );
        }


        String accion =
                accionEvaluacion
                        .trim()
                        .toUpperCase();


        if (
                !"R".equals(accion)
                        && !"H".equals(accion)
                        && !"M".equals(accion)
        ) {
            throw new IllegalArgumentException(
                    "La acción de evaluación debe ser "
                            + "R, H o M."
            );
        }


        return accion;
    }


    // =========================================================
    // VALIDAR PERTENENCIA DEL RESULTADO A LA EVALUACIÓN
    // =========================================================

    private void validarPertenenciaEvaluacion(
            Integer idEvaluacionCartera,
            EvaluacionResultadoCreditoDTO resultado
    ) {

        if (
                resultado.getIdEvaluacionCartera() == null
                        || !resultado
                        .getIdEvaluacionCartera()
                        .equals(
                                idEvaluacionCartera
                        )
        ) {
            throw new IllegalArgumentException(
                    "El crédito evaluado solicitado no pertenece "
                            + "a la evaluación de cartera indicada."
            );
        }
    }
}