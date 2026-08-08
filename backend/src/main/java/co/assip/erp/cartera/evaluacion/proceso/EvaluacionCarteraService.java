package co.assip.erp.cartera.evaluacion.proceso;

import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCarteraDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.assip.erp.cartera.evaluacion.proceso.motor.EvaluacionMotorService;
import co.assip.erp.cartera.evaluacion.proceso.motor.dto.EvaluacionCreditoResultadoDTO;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluacionCarteraService {

    private final EvaluacionCarteraRepository repository;

    private final UsuarioSesionService usuarioSesionService;

    private final EvaluacionMotorService evaluacionMotorService;

    // =========================================================
    // LISTAR
    // =========================================================

    @Transactional(readOnly = true)
    public List<EvaluacionCarteraDTO> listar() {
        return repository.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public EvaluacionCarteraDTO buscarPorId(
            Integer idEvaluacionCartera
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        return repository.buscarPorId(
                        idEvaluacionCartera
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe la evaluación de cartera solicitada."
                        )
                );
    }

    // =========================================================
    // BUSCAR POR FECHA DE CORTE
    // =========================================================

    @Transactional(readOnly = true)
    public EvaluacionCarteraDTO buscarPorFechaCorte(
            LocalDate fechaCorte
    ) {

        validarFechaCorte(
                fechaCorte
        );

        return repository.buscarPorFechaCorte(
                        fechaCorte
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe una evaluación de cartera "
                                        + "para la fecha de corte seleccionada."
                        )
                );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public EvaluacionCarteraDTO crear(
            EvaluacionCarteraDTO dto
    ) {

        normalizar(dto);

        validarCreacion(
                dto
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer idEvaluacionCartera =
                repository.crear(
                        dto,
                        idUsuario
                );

        if (idEvaluacionCartera == null) {
            throw new IllegalArgumentException(
                    "No fue posible crear la evaluación de cartera."
            );
        }

        /*
         * La evaluación se crea con totales en cero.
         *
         * Los totales se actualizarán cuando se ejecute
         * el motor completo y se almacenen los resultados.
         *
         * La prueba temporal del criterio 102 no modifica
         * la cabecera ni las tablas de resultados.
         */

        return buscarPorId(
                idEvaluacionCartera
        );
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public EvaluacionCarteraDTO actualizar(
            Integer idEvaluacionCartera,
            EvaluacionCarteraDTO dto
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        EvaluacionCarteraDTO actual =
                buscarPorId(
                        idEvaluacionCartera
                );

        validarEnProceso(
                actual
        );

        normalizar(dto);

        validarDatosEditables(
                dto
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int filas =
                repository.actualizar(
                        idEvaluacionCartera,
                        dto,
                        idUsuario
                );

        if (filas == 0) {
            throw new IllegalArgumentException(
                    "No fue posible actualizar la evaluación de cartera."
            );
        }

        return buscarPorId(
                idEvaluacionCartera
        );
    }

    // =========================================================
    // EJECUTAR EVALUACIÓN COMPLETA
    // =========================================================

    public List<EvaluacionCreditoResultadoDTO> ejecutarEvaluacion(
            Integer idEvaluacionCartera
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        EvaluacionCarteraDTO evaluacion =
                buscarPorId(
                        idEvaluacionCartera
                );

        validarEnProceso(
                evaluacion
        );

        return evaluacionMotorService.ejecutar(
                idEvaluacionCartera
        );
    }

    // =========================================================
    // MARCAR DEFINITIVA
    // =========================================================

    public EvaluacionCarteraDTO marcarDefinitiva(
            Integer idEvaluacionCartera
    ) {

        validarIdEvaluacion(
                idEvaluacionCartera
        );

        EvaluacionCarteraDTO actual =
                buscarPorId(
                        idEvaluacionCartera
                );

        validarEnProceso(
                actual
        );

        if (
                actual.getCantidadCreditos() == null
                        || actual.getCantidadCreditos() <= 0
        ) {
            throw new IllegalArgumentException(
                    "No se puede marcar como definitiva una evaluación "
                            + "sin créditos procesados."
            );
        }

        int cantidadResultados =
                sumaResultados(
                        actual
                );

        if (
                cantidadResultados
                        != actual.getCantidadCreditos()
        ) {
            throw new IllegalArgumentException(
                    "No se puede marcar como definitiva la evaluación. "
                            + "La cantidad de resultados R, H y M no coincide "
                            + "con la cantidad de créditos evaluados."
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int filas =
                repository.marcarDefinitiva(
                        idEvaluacionCartera,
                        idUsuario
                );

        if (filas == 0) {
            throw new IllegalArgumentException(
                    "No fue posible marcar la evaluación como definitiva."
            );
        }

        return buscarPorId(
                idEvaluacionCartera
        );
    }


    // =========================================================
    // NORMALIZACIÓN
    // =========================================================

    private void normalizar(
            EvaluacionCarteraDTO dto
    ) {

        if (dto == null) {
            return;
        }

        dto.setVersionMetodologia(
                limpiarMayuscula(
                        dto.getVersionMetodologia()
                )
        );

        dto.setNumeroActaRiesgos(
                limpiarMayuscula(
                        dto.getNumeroActaRiesgos()
                )
        );

        dto.setNumeroActaConsejo(
                limpiarMayuscula(
                        dto.getNumeroActaConsejo()
                )
        );

        dto.setObservaciones(
                limpiar(
                        dto.getObservaciones()
                )
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarCreacion(
            EvaluacionCarteraDTO dto
    ) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Los datos de la evaluación son obligatorios."
            );
        }

        validarFechaCorte(
                dto.getFechaCorte()
        );

        validarDatosEditables(
                dto
        );

        if (
                !repository.existenCierresPorFecha(
                        dto.getFechaCorte()
                )
        ) {
            throw new IllegalArgumentException(
                    "No existen cierres de cartera para la fecha "
                            + dto.getFechaCorte()
                            + "."
            );
        }

        if (
                !repository.existenCreditosPorFecha(
                        dto.getFechaCorte()
                )
        ) {
            throw new IllegalArgumentException(
                    "Los cierres de cartera de la fecha "
                            + dto.getFechaCorte()
                            + " no contienen créditos."
            );
        }

        if (
                repository.existeEvaluacionPorFecha(
                        dto.getFechaCorte()
                )
        ) {
            throw new IllegalArgumentException(
                    "Ya existe una evaluación de cartera para la fecha "
                            + dto.getFechaCorte()
                            + "."
            );
        }
    }

    private void validarDatosEditables(
            EvaluacionCarteraDTO dto
    ) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Los datos de la evaluación son obligatorios."
            );
        }

        if (
                esVacio(
                        dto.getVersionMetodologia()
                )
        ) {
            throw new IllegalArgumentException(
                    "La versión de la metodología es obligatoria."
            );
        }

        if (
                dto.getVersionMetodologia().length()
                        > 20
        ) {
            throw new IllegalArgumentException(
                    "La versión de la metodología no puede "
                            + "superar 20 caracteres."
            );
        }

        if (
                dto.getNumeroActaRiesgos() != null
                        && dto.getNumeroActaRiesgos().length()
                        > 30
        ) {
            throw new IllegalArgumentException(
                    "El número de acta de riesgos no puede "
                            + "superar 30 caracteres."
            );
        }

        if (
                dto.getNumeroActaConsejo() != null
                        && dto.getNumeroActaConsejo().length()
                        > 30
        ) {
            throw new IllegalArgumentException(
                    "El número de acta del Consejo no puede "
                            + "superar 30 caracteres."
            );
        }

        if (
                dto.getObservaciones() != null
                        && dto.getObservaciones().length()
                        > 1000
        ) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar 1000 caracteres."
            );
        }

        validarActa(
                dto.getFechaComiteRiesgos(),
                dto.getNumeroActaRiesgos(),
                "Comité de Riesgos"
        );

        validarActa(
                dto.getFechaConsejo(),
                dto.getNumeroActaConsejo(),
                "Consejo de Administración"
        );
    }

    private void validarActa(
            LocalDate fecha,
            String numeroActa,
            String nombreProceso
    ) {

        boolean tieneFecha =
                fecha != null;

        boolean tieneActa =
                !esVacio(
                        numeroActa
                );

        if (tieneFecha != tieneActa) {
            throw new IllegalArgumentException(
                    "La fecha y el número de acta de "
                            + nombreProceso
                            + " deben registrarse conjuntamente."
            );
        }
    }

    private void validarEnProceso(
            EvaluacionCarteraDTO evaluacion
    ) {

        if (evaluacion == null) {
            throw new IllegalArgumentException(
                    "La evaluación de cartera es obligatoria."
            );
        }

        if (
                !"P".equals(
                        evaluacion.getEstado()
                )
        ) {
            throw new IllegalArgumentException(
                    "La evaluación es definitiva y no admite modificaciones."
            );
        }
    }

    private void validarIdEvaluacion(
            Integer idEvaluacionCartera
    ) {

        if (
                idEvaluacionCartera == null
                        || idEvaluacionCartera <= 0
        ) {
            throw new IllegalArgumentException(
                    "El identificador de la evaluación no es válido."
            );
        }
    }

    private void validarFechaCorte(
            LocalDate fechaCorte
    ) {

        if (fechaCorte == null) {
            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }

        LocalDate ultimoDiaMes =
                fechaCorte.withDayOfMonth(
                        fechaCorte.lengthOfMonth()
                );

        if (
                !fechaCorte.equals(
                        ultimoDiaMes
                )
        ) {
            throw new IllegalArgumentException(
                    "La fecha de corte debe corresponder "
                            + "al último día calendario del mes."
            );
        }
    }

    private int sumaResultados(
            EvaluacionCarteraDTO evaluacion
    ) {

        int recalificados =
                evaluacion.getCantidadRecalificados() == null
                        ? 0
                        : evaluacion.getCantidadRecalificados();

        int habilitados =
                evaluacion.getCantidadHabilitados() == null
                        ? 0
                        : evaluacion.getCantidadHabilitados();

        int mantenidos =
                evaluacion.getCantidadMantenidos() == null
                        ? 0
                        : evaluacion.getCantidadMantenidos();

        return recalificados
                + habilitados
                + mantenidos;
    }

    private boolean esVacio(
            String valor
    ) {
        return valor == null
                || valor.isBlank();
    }

    private String limpiar(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String resultado =
                valor.trim();

        return resultado.isEmpty()
                ? null
                : resultado;
    }

    private String limpiarMayuscula(
            String valor
    ) {

        String resultado =
                limpiar(
                        valor
                );

        return resultado == null
                ? null
                : resultado.toUpperCase();
    }
}