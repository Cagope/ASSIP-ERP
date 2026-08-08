package co.assip.erp.cartera.evaluacion.reglas;

import co.assip.erp.cartera.evaluacion.criterios.EvaluacionCriterioRepository;
import co.assip.erp.cartera.evaluacion.criterios.dto.EvaluacionCriterioDTO;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluacionCriterioReglaService {

    private final EvaluacionCriterioReglaRepository repository;

    private final EvaluacionCriterioRepository criterioRepository;

    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // LISTAR POR CRITERIO
    // =========================================================

    @Transactional(readOnly = true)
    public List<EvaluacionCriterioReglaDTO> listarPorCriterio(
            Integer idEvaluacionCriterio
    ) {

        validarIdCriterio(idEvaluacionCriterio);

        validarExistenciaCriterio(
                idEvaluacionCriterio
        );

        return repository.listarPorCriterio(
                idEvaluacionCriterio
        );
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public EvaluacionCriterioReglaDTO buscarPorId(
            Integer idEvaluacionCriterioRegla
    ) {

        validarIdRegla(
                idEvaluacionCriterioRegla
        );

        return repository.buscarPorId(
                        idEvaluacionCriterioRegla
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe la regla de evaluación solicitada."
                        )
                );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public EvaluacionCriterioReglaDTO crear(
            EvaluacionCriterioReglaDTO dto
    ) {

        normalizar(dto);

        validar(
                dto,
                null
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer idEvaluacionCriterioRegla =
                repository.crear(
                        dto,
                        idUsuario
                );

        return buscarPorId(
                idEvaluacionCriterioRegla
        );
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public EvaluacionCriterioReglaDTO actualizar(
            Integer idEvaluacionCriterioRegla,
            EvaluacionCriterioReglaDTO dto
    ) {

        validarIdRegla(
                idEvaluacionCriterioRegla
        );

        buscarPorId(
                idEvaluacionCriterioRegla
        );

        normalizar(dto);

        validar(
                dto,
                idEvaluacionCriterioRegla
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int filas =
                repository.actualizar(
                        idEvaluacionCriterioRegla,
                        dto,
                        idUsuario
                );

        if (filas == 0) {
            throw new IllegalArgumentException(
                    "No fue posible actualizar la regla de evaluación."
            );
        }

        return buscarPorId(
                idEvaluacionCriterioRegla
        );
    }

    // =========================================================
    // CAMBIAR ESTADO
    // =========================================================

    public EvaluacionCriterioReglaDTO cambiarActivo(
            Integer idEvaluacionCriterioRegla,
            boolean activo
    ) {

        validarIdRegla(
                idEvaluacionCriterioRegla
        );

        buscarPorId(
                idEvaluacionCriterioRegla
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int filas =
                repository.cambiarActivo(
                        idEvaluacionCriterioRegla,
                        activo,
                        idUsuario
                );

        if (filas == 0) {
            throw new IllegalArgumentException(
                    "No fue posible cambiar el estado de la regla."
            );
        }

        return buscarPorId(
                idEvaluacionCriterioRegla
        );
    }

    // =========================================================
    // NORMALIZACIÓN
    // =========================================================

    private void normalizar(
            EvaluacionCriterioReglaDTO dto
    ) {

        if (dto == null) {
            return;
        }

        dto.setCodigoRegla(
                limpiarMayuscula(
                        dto.getCodigoRegla()
                )
        );

        dto.setNombreRegla(
                limpiarMayuscula(
                        dto.getNombreRegla()
                )
        );

        dto.setTipoRegla(
                limpiarMayuscula(
                        dto.getTipoRegla()
                )
        );

        dto.setObservaciones(
                limpiar(
                        dto.getObservaciones()
                )
        );

        if (dto.getPuntaje() == null) {
            dto.setPuntaje(
                    BigDecimal.ZERO
            );
        }

        if (dto.getAplica() == null) {
            dto.setAplica(true);
        }

        if (dto.getActivo() == null) {
            dto.setActivo(true);
        }

        if ("C".equals(dto.getTipoRegla())) {
            dto.setValorDesde(null);
            dto.setValorHasta(null);
        }

        if ("R".equals(dto.getTipoRegla())) {
            dto.setValorComparacion(null);
        }
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validar(
            EvaluacionCriterioReglaDTO dto,
            Integer excluirId
    ) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Los datos de la regla son obligatorios."
            );
        }

        validarIdCriterio(
                dto.getIdEvaluacionCriterio()
        );

        EvaluacionCriterioDTO criterio =
                validarExistenciaCriterio(
                        dto.getIdEvaluacionCriterio()
                );

        if (esVacio(dto.getCodigoRegla())) {
            throw new IllegalArgumentException(
                    "El código de la regla es obligatorio."
            );
        }

        if (dto.getCodigoRegla().length() > 3) {
            throw new IllegalArgumentException(
                    "El código de la regla no puede superar 3 caracteres."
            );
        }

        if (esVacio(dto.getNombreRegla())) {
            throw new IllegalArgumentException(
                    "El nombre de la regla es obligatorio."
            );
        }

        if (dto.getNombreRegla().length() > 200) {
            throw new IllegalArgumentException(
                    "El nombre de la regla no puede superar 200 caracteres."
            );
        }

        if (
                !"C".equals(dto.getTipoRegla())
                        && !"R".equals(dto.getTipoRegla())
        ) {
            throw new IllegalArgumentException(
                    "El tipo de regla debe ser C o R."
            );
        }

        validarTipoPersona(
                criterio,
                dto.getIdTipoPersona()
        );

        validarConfiguracionRegla(dto);

        if (
                dto.getPuntaje() == null
                        || dto.getPuntaje().compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "El puntaje no puede ser negativo."
            );
        }

        if (
                dto.getOrden() == null
                        || dto.getOrden() <= 0
        ) {
            throw new IllegalArgumentException(
                    "El orden debe ser mayor que cero."
            );
        }

        if (
                dto.getObservaciones() != null
                        && dto.getObservaciones().length() > 500
        ) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar 500 caracteres."
            );
        }

        if (
                repository.existePorCodigo(
                        dto.getIdEvaluacionCriterio(),
                        dto.getCodigoRegla(),
                        dto.getIdTipoPersona(),
                        excluirId
                )
        ) {
            throw new IllegalArgumentException(
                    "Ya existe una regla con el mismo código para el criterio y tipo de persona."
            );
        }
    }

    private void validarConfiguracionRegla(
            EvaluacionCriterioReglaDTO dto
    ) {

        // =========================================================
        // REGLA POR COMPARACIÓN EXACTA
        // =========================================================

        if ("C".equals(dto.getTipoRegla())) {

            if (dto.getValorComparacion() == null) {
                throw new IllegalArgumentException(
                        "La regla de comparación requiere un valor de comparación."
                );
            }

            return;
        }

        // =========================================================
        // REGLA POR RANGO
        //
        // Debe existir al menos uno de los dos límites.
        // Se permiten rangos abiertos:
        //
        // NULL - 30
        // 361  - NULL
        // =========================================================

        if (
                dto.getValorDesde() == null
                        &&
                        dto.getValorHasta() == null
        ) {
            throw new IllegalArgumentException(
                    "La regla por rango requiere al menos un límite."
            );
        }

        // =========================================================
        // Si existen ambos límites, validar coherencia.
        // =========================================================

        if (
                dto.getValorDesde() != null
                        &&
                        dto.getValorHasta() != null
                        &&
                        dto.getValorHasta().compareTo(
                                dto.getValorDesde()
                        ) < 0
        ) {
            throw new IllegalArgumentException(
                    "El valor hasta no puede ser menor que el valor desde."
            );
        }
    }

    private void validarTipoPersona(
            EvaluacionCriterioDTO criterio,
            Short idTipoPersona
    ) {

        boolean esEdadCronologica =
                "201".equals(
                        criterio.getCodigoCriterio()
                );

        if (esEdadCronologica) {

            if (
                    idTipoPersona == null
                            || (
                            idTipoPersona != 1
                                    && idTipoPersona != 2
                    )
            ) {
                throw new IllegalArgumentException(
                        "La edad cronológica requiere tipo de persona 1 o 2."
                );
            }

            return;
        }

        if (idTipoPersona != null) {
            throw new IllegalArgumentException(
                    "El tipo de persona solo aplica al criterio de edad cronológica."
            );
        }
    }

    private EvaluacionCriterioDTO validarExistenciaCriterio(
            Integer idEvaluacionCriterio
    ) {

        return criterioRepository.buscarPorId(
                        idEvaluacionCriterio
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe el criterio de evaluación seleccionado."
                        )
                );
    }

    private void validarIdCriterio(
            Integer idEvaluacionCriterio
    ) {

        if (
                idEvaluacionCriterio == null
                        || idEvaluacionCriterio <= 0
        ) {
            throw new IllegalArgumentException(
                    "El identificador del criterio no es válido."
            );
        }
    }

    private void validarIdRegla(
            Integer idEvaluacionCriterioRegla
    ) {

        if (
                idEvaluacionCriterioRegla == null
                        || idEvaluacionCriterioRegla <= 0
        ) {
            throw new IllegalArgumentException(
                    "El identificador de la regla no es válido."
            );
        }
    }

    private boolean esVacio(
            String valor
    ) {
        return valor == null || valor.isBlank();
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
                limpiar(valor);

        return resultado == null
                ? null
                : resultado.toUpperCase();
    }
}