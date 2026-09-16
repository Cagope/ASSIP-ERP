package co.assip.erp.cartera.originacion.bienes;

import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienCreditoRespaldadoDTO;
import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienDTO;
import co.assip.erp.cartera.originacion.bienes.dto.SolicitudBienSeleccionRequestDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SolicitudBienService {

    private final SolicitudBienRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public SolicitudBienService(
            SolicitudBienRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }


    // =========================================================
    // LISTAR BIENES DE LA SOLICITUD
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudBienDTO> listarPorSolicitud(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitudCredito(
                idSolicitudCredito
        );

        return repository.listarPorSolicitud(
                idSolicitudCredito
        );
    }


    // =========================================================
    // LISTAR BIENES DE UN DEUDOR
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudBienDTO> listarPorDeudor(
            Integer idSolicitudDeudor
    ) {

        validarIdSolicitudDeudor(
                idSolicitudDeudor
        );

        return repository.listarPorDeudor(
                idSolicitudDeudor
        );
    }


    // =========================================================
    // CONSULTAR CRÉDITOS RESPALDADOS POR UN BIEN
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudBienCreditoRespaldadoDTO>
    listarCreditosRespaldados(
            Long idBien
    ) {

        validarIdBien(
                idBien
        );

        return repository.listarCreditosRespaldados(
                idBien
        );
    }


    // =========================================================
    // SELECCIONAR / RETIRAR BIEN
    // =========================================================

    public SolicitudBienDTO seleccionar(
            SolicitudBienSeleccionRequestDTO request
    ) {

        validarRequest(
                request
        );

        Integer idSolicitudDeudor =
                request.getIdSolicitudDeudor();

        Long idBienPersona =
                request.getIdBienPersona();

        boolean seleccionado =
                Boolean.TRUE.equals(
                        request.getSeleccionado()
                );

        Integer idUsuario =
                usuarioSesionService.idUsuario();


        // -----------------------------------------------------
        // Bloquear y validar solicitud editable
        // -----------------------------------------------------

        Integer idSolicitudCredito =
                repository
                        .bloquearSolicitudEditable(
                                idSolicitudDeudor
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "La solicitud no existe, el deudor no está activo o la solicitud ya no permite modificaciones."
                                        )
                        );


        // -----------------------------------------------------
        // Validar que el bien realmente pertenece al deudor
        // -----------------------------------------------------

        if (!repository.existeBienDeudor(
                idSolicitudDeudor,
                idBienPersona
        )) {

            throw new IllegalArgumentException(
                    "El bien seleccionado no pertenece al deudor o no se encuentra activo."
            );
        }


        // -----------------------------------------------------
        // Seleccionar
        // -----------------------------------------------------

        if (seleccionado) {

            repository.guardarFotografia(
                    idSolicitudDeudor,
                    idBienPersona,
                    normalizarObservacion(
                            request.getObservacion()
                    ),
                    idUsuario
            );

        } else {

            repository.desactivarFotografia(
                    idSolicitudDeudor,
                    idBienPersona,
                    idUsuario
            );
        }


        // -----------------------------------------------------
        // Actualizar gestión de la solicitud
        // -----------------------------------------------------

        repository.actualizarUltimaGestion(
                idSolicitudCredito,
                idUsuario
        );


        // -----------------------------------------------------
        // Retornar estado actual
        // -----------------------------------------------------

        return repository
                .buscarBienActual(
                        idSolicitudDeudor,
                        idBienPersona
                )
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "No fue posible consultar el bien después de actualizar su selección."
                                )
                );
    }


    // =========================================================
    // VALIDAR PASO BIENES
    // =========================================================
    //
    // Regla:
    //
    // GARANTÍA REAL:
    // debe existir por lo menos un bien seleccionado entre
    // cualquiera de los deudores activos de la solicitud.
    //
    // GARANTÍA PERSONAL:
    // no se exige un bien como garantía real.
    //
    // La clasificación R/P se toma exclusivamente del catálogo
    // de garantías del crédito.
    //
    // =========================================================

    @Transactional(readOnly = true)
    public void validarParaContinuar(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitudCredito(
                idSolicitudCredito
        );

        String tipoGarantia =
                repository
                        .buscarTipoGarantiaSolicitud(
                                idSolicitudCredito
                        )
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "La solicitud no tiene una garantía válida seleccionada."
                                        )
                        );


        // -----------------------------------------------------
        // Garantía personal
        // -----------------------------------------------------

        if ("P".equals(tipoGarantia)) {
            return;
        }


        // -----------------------------------------------------
        // Garantía real
        // -----------------------------------------------------

        if ("R".equals(tipoGarantia)) {

            int cantidadSeleccionados =
                    repository.contarSeleccionados(
                            idSolicitudCredito
                    );

            if (cantidadSeleccionados <= 0) {

                throw new IllegalStateException(
                        "La garantía seleccionada para el crédito es REAL. Debe seleccionar al menos un bien de cualquiera de los deudores de la solicitud antes de continuar."
                );
            }

            return;
        }


        // -----------------------------------------------------
        // Tipo no reconocido
        // -----------------------------------------------------

        throw new IllegalStateException(
                "El tipo de garantía de la solicitud no es válido."
        );
    }


    // =========================================================
    // CONSULTAR SI PUEDE CONTINUAR
    // =========================================================

    @Transactional(readOnly = true)
    public boolean puedeContinuar(
            Integer idSolicitudCredito
    ) {

        validarParaContinuar(
                idSolicitudCredito
        );

        return true;
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarRequest(
            SolicitudBienSeleccionRequestDTO request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La información de selección del bien es obligatoria."
            );
        }

        validarIdSolicitudDeudor(
                request.getIdSolicitudDeudor()
        );

        validarIdBienPersona(
                request.getIdBienPersona()
        );

        if (request.getSeleccionado() == null) {

            throw new IllegalArgumentException(
                    "Debe indicar si el bien participa o no en la solicitud."
            );
        }
    }


    private void validarIdSolicitudCredito(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null
                || idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la solicitud de crédito es obligatorio."
            );
        }
    }


    private void validarIdSolicitudDeudor(
            Integer idSolicitudDeudor
    ) {

        if (idSolicitudDeudor == null
                || idSolicitudDeudor <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del deudor de la solicitud es obligatorio."
            );
        }
    }


    private void validarIdBienPersona(
            Long idBienPersona
    ) {

        if (idBienPersona == null
                || idBienPersona <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del bien del deudor es obligatorio."
            );
        }
    }


    private void validarIdBien(
            Long idBien
    ) {

        if (idBien == null
                || idBien <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del bien es obligatorio."
            );
        }
    }


    // =========================================================
    // NORMALIZACIÓN
    // =========================================================

    private String normalizarObservacion(
            String observacion
    ) {

        if (observacion == null) {
            return null;
        }

        String valor =
                observacion.trim();

        if (valor.isEmpty()) {
            return null;
        }

        if (valor.length() > 500) {

            throw new IllegalArgumentException(
                    "La observación del bien no puede superar los 500 caracteres."
            );
        }

        return valor;
    }
}