package co.assip.erp.cartera.evaluacion.criterios;

import co.assip.erp.cartera.evaluacion.criterios.dto.EvaluacionCriterioDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluacionCriterioService {

    private final EvaluacionCriterioRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // LISTAR
    // =========================================================

    @Transactional(readOnly = true)
    public List<EvaluacionCriterioDTO> listar() {
        return repository.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public EvaluacionCriterioDTO buscarPorId(
            Integer idEvaluacionCriterio
    ) {
        validarId(idEvaluacionCriterio);

        return repository.buscarPorId(idEvaluacionCriterio)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe el criterio de evaluación solicitado."
                        )
                );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public EvaluacionCriterioDTO crear(
            EvaluacionCriterioDTO dto
    ) {
        normalizar(dto);
        validar(dto, null);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer idEvaluacionCriterio =
                repository.crear(dto, idUsuario);

        return buscarPorId(idEvaluacionCriterio);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public EvaluacionCriterioDTO actualizar(
            Integer idEvaluacionCriterio,
            EvaluacionCriterioDTO dto
    ) {
        validarId(idEvaluacionCriterio);
        buscarPorId(idEvaluacionCriterio);

        normalizar(dto);
        validar(dto, idEvaluacionCriterio);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int filas =
                repository.actualizar(
                        idEvaluacionCriterio,
                        dto,
                        idUsuario
                );

        if (filas == 0) {
            throw new IllegalArgumentException(
                    "No fue posible actualizar el criterio de evaluación."
            );
        }

        return buscarPorId(idEvaluacionCriterio);
    }

    // =========================================================
    // CAMBIAR ESTADO
    // =========================================================

    public EvaluacionCriterioDTO cambiarActivo(
            Integer idEvaluacionCriterio,
            boolean activo
    ) {
        validarId(idEvaluacionCriterio);
        buscarPorId(idEvaluacionCriterio);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int filas =
                repository.cambiarActivo(
                        idEvaluacionCriterio,
                        activo,
                        idUsuario
                );

        if (filas == 0) {
            throw new IllegalArgumentException(
                    "No fue posible cambiar el estado del criterio."
            );
        }

        return buscarPorId(idEvaluacionCriterio);
    }

    // =========================================================
    // NORMALIZACIÓN
    // =========================================================

    private void normalizar(
            EvaluacionCriterioDTO dto
    ) {
        if (dto == null) {
            return;
        }

        dto.setCodigoProceso(
                limpiarMayuscula(dto.getCodigoProceso())
        );

        dto.setCodigoCriterio(
                limpiarMayuscula(dto.getCodigoCriterio())
        );

        dto.setNombreCriterio(
                limpiarMayuscula(dto.getNombreCriterio())
        );

        dto.setDescripcionCriterio(
                limpiar(dto.getDescripcionCriterio())
        );

        dto.setNivelAplicacion(
                limpiarMayuscula(dto.getNivelAplicacion())
        );

        dto.setTipoComparacion(
                limpiarMayuscula(dto.getTipoComparacion())
        );

        if (dto.getAplica() == null) {
            dto.setAplica(true);
        }

        if (dto.getActivo() == null) {
            dto.setActivo(true);
        }

        if (dto.getPuntajeMaximo() == null) {
            dto.setPuntajeMaximo(BigDecimal.ZERO);
        }
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validar(
            EvaluacionCriterioDTO dto,
            Integer excluirId
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Los datos del criterio son obligatorios."
            );
        }

        if (esVacio(dto.getCodigoProceso())) {
            throw new IllegalArgumentException(
                    "El código del proceso es obligatorio."
            );
        }

        if (dto.getCodigoProceso().length() > 2) {
            throw new IllegalArgumentException(
                    "El código del proceso no puede superar 2 caracteres."
            );
        }

        if (esVacio(dto.getCodigoCriterio())) {
            throw new IllegalArgumentException(
                    "El código del criterio es obligatorio."
            );
        }

        if (dto.getCodigoCriterio().length() > 3) {
            throw new IllegalArgumentException(
                    "El código del criterio no puede superar 3 caracteres."
            );
        }

        if (esVacio(dto.getNombreCriterio())) {
            throw new IllegalArgumentException(
                    "El nombre del criterio es obligatorio."
            );
        }

        if (dto.getNombreCriterio().length() > 150) {
            throw new IllegalArgumentException(
                    "El nombre del criterio no puede superar 150 caracteres."
            );
        }

        if (
                dto.getDescripcionCriterio() != null
                        && dto.getDescripcionCriterio().length() > 500
        ) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar 500 caracteres."
            );
        }

        if (
                !"A".equals(dto.getNivelAplicacion())
                        && !"C".equals(dto.getNivelAplicacion())
        ) {
            throw new IllegalArgumentException(
                    "El nivel de aplicación debe ser A o C."
            );
        }

        if (
                !"R".equals(dto.getTipoComparacion())
                        && !"C".equals(dto.getTipoComparacion())
        ) {
            throw new IllegalArgumentException(
                    "El tipo de comparación debe ser R o C."
            );
        }

        if (
                dto.getOrdenEvaluacion() == null
                        || dto.getOrdenEvaluacion() <= 0
        ) {
            throw new IllegalArgumentException(
                    "El orden de evaluación debe ser mayor que cero."
            );
        }

        if (
                dto.getPuntajeMaximo() == null
                        || dto.getPuntajeMaximo().compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "El puntaje máximo no puede ser negativo."
            );
        }

        if (
                repository.existePorCodigo(
                        dto.getCodigoProceso(),
                        dto.getCodigoCriterio(),
                        excluirId
                )
        ) {
            throw new IllegalArgumentException(
                    "Ya existe un criterio con el mismo proceso y código."
            );
        }
    }

    private void validarId(
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

        String resultado = valor.trim();

        return resultado.isEmpty()
                ? null
                : resultado;
    }

    private String limpiarMayuscula(
            String valor
    ) {
        String resultado = limpiar(valor);

        return resultado == null
                ? null
                : resultado.toUpperCase();
    }
}