package co.assip.erp.cartera.originacion.aprobacion;

import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionActuacionDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionBandejaDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionDecisionDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionFotosDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionDecisionRequestDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class SolicitudAprobacionService {

    private final SolicitudAprobacionRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final ObjectMapper objectMapper;

    public SolicitudAprobacionService(
            SolicitudAprobacionRepository repository,
            UsuarioSesionService usuarioSesionService,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
        this.objectMapper = objectMapper;
    }

    // =========================================================
    // BANDEJA
    // =========================================================

    public List<SolicitudAprobacionBandejaDTO> listarBandeja() {

        Integer idUsuario = usuarioSesionService.idUsuario();

        return repository.listarBandeja(idUsuario);
    }


    // =========================================================
    // CATÁLOGO DE DECISIONES
    // =========================================================

    public List<SolicitudAprobacionDecisionDTO> listarDecisiones() {

        return repository.listarDecisiones();
    }


    // =========================================================
    // HISTORIAL
    // =========================================================

    // =========================================================
    // HISTORIAL
    // =========================================================

    public List<SolicitudAprobacionActuacionDTO> listarActuaciones(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        validarAccesoConsulta(idSolicitudCredito);

        return repository.listarActuaciones(
                idSolicitudCredito
        );
    }

    // =========================================================
    // FOTOGRAFÍAS DE LA SOLICITUD
    // =========================================================

    public SolicitudAprobacionFotosDTO obtenerFotos(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        validarAccesoConsulta(
                idSolicitudCredito
        );
        SolicitudAprobacionFotosDTO dto =
                new SolicitudAprobacionFotosDTO();

        dto.setFotoSolicitud(
                convertirJson(
                        repository.obtenerFotoSolicitud(
                                idSolicitudCredito
                        )
                )
        );

        dto.setFotoDeudores(
                convertirJson(
                        repository.obtenerFotoDeudores(
                                idSolicitudCredito
                        )
                )
        );

        dto.setFotoFinanciero(
                convertirJson(
                        repository.obtenerFotoFinanciero(
                                idSolicitudCredito
                        )
                )
        );

        dto.setFotoBienes(
                convertirJson(
                        repository.obtenerFotoBienes(
                                idSolicitudCredito
                        )
                )
        );

        dto.setFotoCentralRiesgo(
                convertirJson(
                        repository.obtenerFotoCentralRiesgo(
                                idSolicitudCredito
                        )
                )
        );

        dto.setFotoAnalisis(
                convertirJson(
                        repository.obtenerFotoAnalisis(
                                idSolicitudCredito
                        )
                )
        );

        return dto;
    }

    // =========================================================
    // FOTOGRAFÍAS HISTÓRICAS DE UNA ACTUACIÓN
    // =========================================================

    public SolicitudAprobacionFotosDTO obtenerFotosActuacion(
            Integer idSolicitudCredito,
            Integer idSolicitudAprobacion
    ) {

        validarIdSolicitud(idSolicitudCredito);

        if (idSolicitudAprobacion == null
                || idSolicitudAprobacion <= 0) {

            throw new IllegalArgumentException(
                    "El id de la actuación de aprobación es obligatorio."
            );
        }

        validarAccesoConsulta(idSolicitudCredito);

        String[] fotos = repository.obtenerFotosActuacion(
                        idSolicitudAprobacion,
                        idSolicitudCredito
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "La actuación de aprobación no existe "
                                        + "o no pertenece a la solicitud indicada."
                        )
                );

        SolicitudAprobacionFotosDTO dto =
                new SolicitudAprobacionFotosDTO();

        dto.setFotoSolicitud(
                convertirJson(fotos[0])
        );

        dto.setFotoDeudores(
                convertirJson(fotos[1])
        );

        dto.setFotoFinanciero(
                convertirJson(fotos[2])
        );

        dto.setFotoBienes(
                convertirJson(fotos[3])
        );

        dto.setFotoCentralRiesgo(
                convertirJson(fotos[4])
        );

        dto.setFotoAnalisis(
                convertirJson(fotos[5])
        );

        return dto;
    }

    private JsonNode convertirJson(
            String json
    ) {

        try {
            return objectMapper.readTree(json);

        } catch (JsonProcessingException e) {

            throw new IllegalStateException(
                    "No fue posible interpretar la fotografía JSON.",
                    e
            );
        }
    }

    // =========================================================
    // REGISTRAR DECISIÓN
    // =========================================================

    @Transactional
    public Integer registrarDecision(
            Integer idSolicitudCredito,
            SolicitudAprobacionDecisionRequestDTO request
    ) {

        validarIdSolicitud(idSolicitudCredito);

        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de la decisión son obligatorios."
            );
        }

        if (request.getIdAprobacionDecision() == null
                || request.getIdAprobacionDecision() <= 0) {

            throw new IllegalArgumentException(
                    "La decisión de aprobación es obligatoria."
            );
        }

        String concepto = request.getConcepto() == null
                ? null
                : request.getConcepto().trim();

        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException(
                    "El concepto de aprobación es obligatorio."
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // -----------------------------------------------------
        // 1. Bloqueo pesimista de la solicitud
        // -----------------------------------------------------

        if (!repository.bloquearSolicitud(
                idSolicitudCredito
        )) {
            throw new IllegalStateException(
                    "La solicitud no existe o no se encuentra activa."
            );
        }

        // -----------------------------------------------------
        // 2. Revalidar autorización después del bloqueo
        // -----------------------------------------------------

        if (!repository.usuarioPuedeActuar(
                idSolicitudCredito,
                idUsuario
        )) {
            throw new IllegalStateException(
                    "El usuario autenticado no está autorizado "
                            + "para registrar una decisión sobre "
                            + "esta solicitud."
            );
        }

        // -----------------------------------------------------
        // 3. Obtener ente actual
        // -----------------------------------------------------

        Integer idEnteActual =
                repository
                        .obtenerEnteActual(
                                idSolicitudCredito
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "La solicitud no tiene un ente "
                                                + "pendiente de actuación."
                                )
                        );

        // -----------------------------------------------------
        // 4. Validar decisión
        // -----------------------------------------------------

        String codigoDecision = repository
                .buscarDecision(
                        request.getIdAprobacionDecision()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "La decisión seleccionada no existe "
                                        + "o no se encuentra activa."
                        )
                )
                .getCodigoDecision();

        if (!"APROBADA".equals(codigoDecision)
                && !"NO_VIABLE".equals(codigoDecision)
                && !"SOLICITA_AJUSTES".equals(codigoDecision)) {

            throw new IllegalStateException(
                    "Código de decisión no reconocido: "
                            + codigoDecision
            );
        }

        // -----------------------------------------------------
        // 5. Comité y Consejo requieren acta
        // Gerencia = 1
        // Comité   = 2
        // Consejo  = 3
        // -----------------------------------------------------

        if (idEnteActual == 2 || idEnteActual == 3) {

            String numeroActa =
                    request.getNumeroActa() == null
                            ? null
                            : request.getNumeroActa().trim();

            if (numeroActa == null
                    || numeroActa.isBlank()) {

                throw new IllegalArgumentException(
                        "El número de acta es obligatorio "
                                + "para Comité o Consejo."
                );
            }

            if (request.getFechaActa() == null) {
                throw new IllegalArgumentException(
                        "La fecha del acta es obligatoria "
                                + "para Comité o Consejo."
                );
            }
        }

        // -----------------------------------------------------
        // 6. Fotografías evaluadas
        // -----------------------------------------------------

        String fotoSolicitud =
                repository.obtenerFotoSolicitud(
                        idSolicitudCredito
                );

        String fotoDeudores =
                repository.obtenerFotoDeudores(
                        idSolicitudCredito
                );

        String fotoFinanciero =
                repository.obtenerFotoFinanciero(
                        idSolicitudCredito
                );

        String fotoBienes =
                repository.obtenerFotoBienes(
                        idSolicitudCredito
                );

        String fotoCentralRiesgo =
                repository.obtenerFotoCentralRiesgo(
                        idSolicitudCredito
                );

        String fotoAnalisis =
                repository.obtenerFotoAnalisis(
                        idSolicitudCredito
                );

        // -----------------------------------------------------
        // 7. Determinar ente final
        // -----------------------------------------------------

        Integer idEnteFinal =
                repository.obtenerEnteFinal(
                        idSolicitudCredito
                );

        if (idEnteActual != 1
                && !idEnteActual.equals(idEnteFinal)) {

            throw new IllegalStateException(
                    "El ente actual no corresponde al ente "
                            + "aprobador final de la solicitud."
            );
        }

        // -----------------------------------------------------
        // 8. Registrar actuación + evidencia
        // -----------------------------------------------------

        Integer idActuacion =
                repository.registrarActuacion(
                        idSolicitudCredito,
                        idEnteActual,
                        request.getIdAprobacionDecision(),
                        normalizarTexto(request.getNumeroActa()),
                        request.getFechaActa(),
                        concepto,
                        idUsuario,
                        fotoSolicitud,
                        fotoDeudores,
                        fotoFinanciero,
                        fotoBienes,
                        fotoCentralRiesgo,
                        fotoAnalisis
                );

        // -----------------------------------------------------
        // 9. La actuación queda registrada.
        // La vista determina si continúa otro ente o si
        // corresponde gestionar el concepto al asesor.
        // -----------------------------------------------------

        return idActuacion;

    }

    // =========================================================
// GESTIÓN DEL ASESOR SOBRE EL CONCEPTO VIGENTE
// =========================================================

    @Transactional
    public void gestionarConcepto(
            Integer idSolicitudCredito,
            String accion
    ) {

        validarIdSolicitud(idSolicitudCredito);

        if (accion == null || accion.isBlank()) {
            throw new IllegalArgumentException(
                    "La acción del asesor es obligatoria."
            );
        }

        String accionNormalizada = accion.trim().toUpperCase(
                java.util.Locale.ROOT
        );

        if (!"RETOMAR".equals(accionNormalizada)
                && !"FORMALIZAR".equals(accionNormalizada)
                && !"CERRAR".equals(accionNormalizada)) {

            throw new IllegalArgumentException(
                    "Acción del asesor no reconocida: "
                            + accionNormalizada
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

// =========================================================
// BLOQUEAR SOLICITUD
// =========================================================

        if (!repository.bloquearSolicitud(idSolicitudCredito)) {
            throw new IllegalStateException(
                    "La solicitud no existe o no se encuentra activa."
            );
        }

        // =========================================================
        // VALIDAR ASESOR RESPONSABLE DESPUÉS DEL BLOQUEO
        // =========================================================

        if (!repository.usuarioEsAsesor(
                idSolicitudCredito,
                idUsuario
        )) {

            throw new IllegalStateException(
                    "El usuario autenticado no es el asesor "
                            + "responsable de esta solicitud."
            );
        }
        String conceptoVigente = repository
                .obtenerConceptoVigente(idSolicitudCredito)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "La solicitud no tiene un concepto vigente "
                                        + "pendiente de gestión del asesor."
                        )
                );

        int actualizados;

        switch (accionNormalizada) {

            case "RETOMAR" -> {

                actualizados = repository.devolverADocumentacion(
                        idSolicitudCredito,
                        idUsuario
                );
            }

            case "FORMALIZAR" -> {

                if (!"APROBADA".equals(conceptoVigente)) {
                    throw new IllegalStateException(
                            "Solo puede formalizarse una solicitud "
                                    + "con concepto APROBADA."
                    );
                }

                actualizados = repository.enviarAFormalizacion(
                        idSolicitudCredito,
                        idUsuario
                );
            }

            case "CERRAR" -> {

                if (!"NO_VIABLE".equals(conceptoVigente)) {
                    throw new IllegalStateException(
                            "Solo puede cerrarse por no viabilidad "
                                    + "una solicitud con concepto NO_VIABLE."
                    );
                }

                actualizados = repository.finalizarNoViable(
                        idSolicitudCredito,
                        idUsuario
                );
            }

            default -> throw new IllegalStateException(
                    "Acción del asesor no reconocida."
            );
        }

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible gestionar el concepto. "
                            + "Verifique el estado actual de la solicitud."
            );
        }
    }


    // =========================================================
    // SOPORTE
    // =========================================================

    private String normalizarTexto(
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


    // =========================================================
    // VALIDAR ACCESO A SOLICITUD
    // =========================================================

    public void validarAccesoSolicitud(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        if (!repository.usuarioPuedeActuar(
                idSolicitudCredito,
                idUsuario
        )) {
            throw new IllegalStateException(
                    "El usuario autenticado no está autorizado "
                            + "para gestionar esta solicitud "
                            + "en la etapa actual de aprobación."
            );
        }
    }

    // =========================================================
    // VALIDAR ACCESO DE CONSULTA
    // HISTORIAL Y FOTOGRAFÍAS
    // =========================================================

    private void validarAccesoConsulta(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        if (!repository.usuarioPuedeConsultar(
                idSolicitudCredito,
                idUsuario
        )) {

            throw new IllegalStateException(
                    "El usuario autenticado no está autorizado "
                            + "para consultar el historial o las "
                            + "fotografías de esta solicitud."
            );
        }
    }


    // =========================================================
    // OBTENER ENTE ACTUAL
    // =========================================================

    public Integer obtenerEnteActual(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        validarAccesoSolicitud(
                idSolicitudCredito
        );

        return repository
                .obtenerEnteActual(
                        idSolicitudCredito
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "La solicitud no tiene un ente "
                                        + "pendiente de actuación."
                        )
                );
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarIdSolicitud(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null
                || idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "El id de la solicitud es obligatorio."
            );
        }
    }
}