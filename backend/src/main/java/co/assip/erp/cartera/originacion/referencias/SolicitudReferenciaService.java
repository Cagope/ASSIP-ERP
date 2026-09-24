package co.assip.erp.cartera.referencias;

import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaListadoDTO;
import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaParticipanteDTO;
import co.assip.erp.cartera.referencias.dto.SolicitudReferenciaPersonalDTO;

import co.assip.erp.seguridad.service.UsuarioSesionService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class SolicitudReferenciaService {

    private final SolicitudReferenciaRepository repository;

    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SolicitudReferenciaService(
            SolicitudReferenciaRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // 1. LISTAR SOLICITUDES
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudReferenciaListadoDTO> listar(
            Integer idAgencia,
            String numeroSolicitud,
            String documento,
            String nombreSolicitante,
            Integer idAsesor,
            Integer idSolicitudProceso,
            String estadoReferencias
    ) {

        String estado = normalizarEstadoReferencias(
                estadoReferencias
        );

        return repository.listar(
                idAgencia,
                limpiar(numeroSolicitud),
                limpiar(documento),
                limpiar(nombreSolicitante),
                idAsesor,
                idSolicitudProceso,
                estado
        );
    }

    // =========================================================
    // 2. CONSULTAR SOLICITUD
    // =========================================================

    @Transactional(readOnly = true)
    public SolicitudReferenciaListadoDTO buscarPorSolicitud(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitud(idSolicitudCredito);

        SolicitudReferenciaListadoDTO solicitud =
                repository.buscarPorSolicitud(
                        idSolicitudCredito
                );

        if (solicitud == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La solicitud de crédito no existe "
                            + "o no se encuentra activa y en curso."
            );
        }

        return solicitud;
    }

    // =========================================================
    // 3. LISTAR PARTICIPANTES
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudReferenciaParticipanteDTO>
    listarParticipantes(
            Integer idSolicitudCredito
    ) {

        buscarPorSolicitud(idSolicitudCredito);

        return repository.listarPorSolicitud(
                idSolicitudCredito
        );
    }

    // =========================================================
    // 4. CONSULTAR PARTICIPANTE
    // =========================================================

    @Transactional(readOnly = true)
    public SolicitudReferenciaParticipanteDTO buscarParticipante(
            Integer idSolicitudCredito,
            Integer idSolicitudDeudor
    ) {

        buscarPorSolicitud(idSolicitudCredito);

        validarIdParticipante(idSolicitudDeudor);

        SolicitudReferenciaParticipanteDTO participante =
                repository.buscarPorParticipante(
                        idSolicitudCredito,
                        idSolicitudDeudor
                );

        if (participante == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El participante no existe "
                            + "o no pertenece a la solicitud."
            );
        }

        return participante;
    }

    // =========================================================
    // 5. LISTAR REFERENCIAS DE UN PARTICIPANTE
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudReferenciaPersonalDTO>
    listarReferenciasPorParticipante(
            Integer idSolicitudCredito,
            Integer idSolicitudDeudor
    ) {

        buscarParticipante(
                idSolicitudCredito,
                idSolicitudDeudor
        );

        return repository.listarReferenciasPorParticipante(
                idSolicitudCredito,
                idSolicitudDeudor
        );
    }

    // =========================================================
    // 6. LISTAR REFERENCIAS DE UNA SOLICITUD
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudReferenciaPersonalDTO>
    listarReferenciasPorSolicitud(
            Integer idSolicitudCredito
    ) {

        buscarPorSolicitud(idSolicitudCredito);

        return repository.listarReferenciasPorSolicitud(
                idSolicitudCredito
        );
    }

    // =========================================================
    // 7. CONSULTAR REFERENCIA PERSONAL
    // =========================================================

    @Transactional(readOnly = true)
    public SolicitudReferenciaPersonalDTO buscarReferenciaPorId(
            Long idSolicitudReferenciaPersonal
    ) {

        validarIdReferencia(
                idSolicitudReferenciaPersonal
        );

        SolicitudReferenciaPersonalDTO referencia =
                repository.buscarReferenciaPorId(
                        idSolicitudReferenciaPersonal
                );

        if (referencia == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "La referencia personal no existe "
                            + "o se encuentra inactiva."
            );
        }

        return referencia;
    }

    // =========================================================
    // 8. INICIAR GESTIÓN DE REFERENCIAS
    // =========================================================

    public Long iniciarGestion(
            Integer idSolicitudCredito
    ) {

        SolicitudReferenciaListadoDTO solicitud =
                buscarPorSolicitud(
                        idSolicitudCredito
                );

        validarProcesoNoCerrado(
                solicitud
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long idProceso =
                repository.crearORecuperarProceso(
                        idSolicitudCredito,
                        idUsuario
                );

        if (idProceso == null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No fue posible iniciar la gestión "
                            + "de referencias personales. "
                            + "Verifique el estado de la solicitud."
            );
        }

        return idProceso;
    }

    // =========================================================
    // 9. REGISTRAR REFERENCIA PERSONAL
    // =========================================================

    public SolicitudReferenciaPersonalDTO crearReferencia(
            SolicitudReferenciaPersonalDTO dto
    ) {

        if (dto == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe suministrar los datos "
                            + "de la referencia personal."
            );
        }

        validarIdSolicitud(
                dto.getIdSolicitudCredito()
        );

        validarIdParticipante(
                dto.getIdSolicitudDeudor()
        );

        validarDatosContacto(
                dto.getNombreCompleto(),
                dto.getTelefonoCelular(),
                dto.getTelefonoFijo()
        );

        // -----------------------------------------------------
        // VALIDAR PARTICIPANTE
        // -----------------------------------------------------

        buscarParticipante(
                dto.getIdSolicitudCredito(),
                dto.getIdSolicitudDeudor()
        );

        // -----------------------------------------------------
        // INICIAR O RECUPERAR PROCESO
        // -----------------------------------------------------

        Long idProceso =
                iniciarGestion(
                        dto.getIdSolicitudCredito()
                );

        // -----------------------------------------------------
        // PREPARAR DATOS
        // -----------------------------------------------------

        dto.setIdSolicitudReferenciaPersonal(null);

        dto.setIdSolicitudReferenciaProceso(
                idProceso
        );

        dto.setNombreCompleto(
                limpiar(dto.getNombreCompleto())
        );

        dto.setTelefonoCelular(
                limpiar(dto.getTelefonoCelular())
        );

        dto.setTelefonoFijo(
                limpiar(dto.getTelefonoFijo())
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // -----------------------------------------------------
        // PERSISTIR
        // -----------------------------------------------------

        Long idReferencia =
                repository.crearReferencia(
                        dto,
                        idUsuario
                );

        if (idReferencia == null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No fue posible registrar la referencia. "
                            + "Verifique que la solicitud "
                            + "y el proceso continúen abiertos."
            );
        }

        return buscarReferenciaPorId(
                idReferencia
        );
    }

    // =========================================================
    // 10. ACTUALIZAR DATOS DE CONTACTO
    // =========================================================

    public SolicitudReferenciaPersonalDTO actualizarReferencia(
            Long idSolicitudReferenciaPersonal,
            SolicitudReferenciaPersonalDTO dto
    ) {

        if (dto == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe suministrar los datos "
                            + "de la referencia personal."
            );
        }

        SolicitudReferenciaPersonalDTO actual =
                buscarReferenciaPorId(
                        idSolicitudReferenciaPersonal
                );

        validarProcesoEditable(
                actual.getIdSolicitudCredito()
        );

        validarDatosContacto(
                dto.getNombreCompleto(),
                dto.getTelefonoCelular(),
                dto.getTelefonoFijo()
        );

        // -----------------------------------------------------
        // NO PERMITIR CAMBIAR LA SOLICITUD O EL PARTICIPANTE
        // -----------------------------------------------------

        if (
                dto.getIdSolicitudCredito() != null
                        && !dto.getIdSolicitudCredito().equals(
                        actual.getIdSolicitudCredito()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede cambiar la solicitud "
                            + "de una referencia personal."
            );
        }

        if (
                dto.getIdSolicitudDeudor() != null
                        && !dto.getIdSolicitudDeudor().equals(
                        actual.getIdSolicitudDeudor()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede cambiar el participante "
                            + "de una referencia personal."
            );
        }

        // -----------------------------------------------------
        // PREPARAR ACTUALIZACIÓN
        // -----------------------------------------------------

        dto.setIdSolicitudReferenciaPersonal(
                idSolicitudReferenciaPersonal
        );

        dto.setNombreCompleto(
                limpiar(dto.getNombreCompleto())
        );

        dto.setTelefonoCelular(
                limpiar(dto.getTelefonoCelular())
        );

        dto.setTelefonoFijo(
                limpiar(dto.getTelefonoFijo())
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int actualizados =
                repository.actualizarReferencia(
                        dto,
                        idUsuario
                );

        validarActualizacion(
                actualizados,
                "No fue posible actualizar "
                        + "la referencia personal."
        );

        return buscarReferenciaPorId(
                idSolicitudReferenciaPersonal
        );
    }

    // =========================================================
    // 11. REGISTRAR ENTREVISTA
    // =========================================================

    public SolicitudReferenciaPersonalDTO registrarEntrevista(
            Long idSolicitudReferenciaPersonal,
            String medioEntrevista,
            Boolean contactoEstablecido,
            String conceptoReferencia
    ) {

        SolicitudReferenciaPersonalDTO referencia =
                buscarReferenciaPorId(
                        idSolicitudReferenciaPersonal
                );

        validarProcesoEditable(
                referencia.getIdSolicitudCredito()
        );

        // -----------------------------------------------------
        // VALIDAR MEDIO DE ENTREVISTA
        // -----------------------------------------------------

        String medio =
                normalizarMedioEntrevista(
                        medioEntrevista
                );

        // -----------------------------------------------------
        // VALIDAR RESULTADO DE CONTACTO
        // -----------------------------------------------------

        if (contactoEstablecido == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar si se estableció "
                            + "contacto con la referencia."
            );
        }

        // -----------------------------------------------------
        // VALIDAR CONCEPTO
        // -----------------------------------------------------

        String concepto =
                limpiar(conceptoReferencia);

        if (
                Boolean.TRUE.equals(contactoEstablecido)
                        && concepto == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe registrar el concepto "
                            + "de la referencia contactada."
            );
        }

        // -----------------------------------------------------
        // VALIDAR TELÉFONO SEGÚN MEDIO
        // -----------------------------------------------------

        if (
                (
                        "CELULAR".equals(medio)
                                || "WHATSAPP".equals(medio)
                )
                        && limpiar(
                        referencia.getTelefonoCelular()
                ) == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La referencia no tiene registrado "
                            + "un teléfono celular."
            );
        }

        if (
                "FIJO".equals(medio)
                        && limpiar(
                        referencia.getTelefonoFijo()
                ) == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La referencia no tiene registrado "
                            + "un teléfono fijo."
            );
        }

        // -----------------------------------------------------
        // REGISTRAR ENTREVISTA
        // -----------------------------------------------------

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int actualizados =
                repository.registrarEntrevista(
                        idSolicitudReferenciaPersonal,
                        medio,
                        contactoEstablecido,
                        concepto,
                        idUsuario
                );

        validarActualizacion(
                actualizados,
                "No fue posible registrar "
                        + "la entrevista."
        );

        return buscarReferenciaPorId(
                idSolicitudReferenciaPersonal
        );
    }

    // =========================================================
    // 12. DESACTIVAR REFERENCIA PERSONAL
    // =========================================================

    public void desactivarReferencia(
            Long idSolicitudReferenciaPersonal
    ) {

        SolicitudReferenciaPersonalDTO referencia =
                buscarReferenciaPorId(
                        idSolicitudReferenciaPersonal
                );

        validarProcesoEditable(
                referencia.getIdSolicitudCredito()
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int actualizados =
                repository.desactivarReferencia(
                        idSolicitudReferenciaPersonal,
                        idUsuario
                );

        validarActualizacion(
                actualizados,
                "No fue posible desactivar "
                        + "la referencia personal."
        );
    }

    // =========================================================
    // 13. CERRAR GESTIÓN DE REFERENCIAS
    // =========================================================

    public SolicitudReferenciaListadoDTO cerrarGestion(
            Integer idSolicitudCredito,
            String tipoCierre,
            String observacionCierre
    ) {

        SolicitudReferenciaListadoDTO solicitud =
                buscarPorSolicitud(
                        idSolicitudCredito
                );

        validarProcesoNoCerrado(
                solicitud
        );

        String tipo =
                normalizarTipoCierre(
                        tipoCierre
                );

        String observacion =
                limpiar(observacionCierre);

        // -----------------------------------------------------
        // CONSULTAR REFERENCIAS ACTIVAS
        // -----------------------------------------------------

        List<SolicitudReferenciaPersonalDTO> referencias =
                repository.listarReferenciasPorSolicitud(
                        idSolicitudCredito
                );

        boolean tieneReferencias =
                !referencias.isEmpty();

        // -----------------------------------------------------
        // CIERRE CON REFERENCIAS
        // -----------------------------------------------------

        if ("CON_REFERENCIAS".equals(tipo)) {

            if (!tieneReferencias) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No puede cerrar con referencias "
                                + "porque no existen referencias "
                                + "personales activas."
                );
            }
        }

        // -----------------------------------------------------
        // CIERRE SIN REFERENCIAS
        // -----------------------------------------------------

        if ("SIN_REFERENCIAS".equals(tipo)) {

            if (tieneReferencias) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La solicitud tiene referencias "
                                + "personales activas. "
                                + "No corresponde un cierre "
                                + "sin referencias."
                );
            }

            if (observacion == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Debe justificar el cierre "
                                + "sin referencias personales."
                );
            }
        }

        // -----------------------------------------------------
        // CREAR PROCESO SI TODAVÍA NO EXISTE
        // -----------------------------------------------------

        iniciarGestion(
                idSolicitudCredito
        );

        // -----------------------------------------------------
        // CERRAR
        // -----------------------------------------------------

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int actualizados =
                repository.cerrarProceso(
                        idSolicitudCredito,
                        tipo,
                        observacion,
                        idUsuario
                );

        validarActualizacion(
                actualizados,
                "No fue posible cerrar la gestión "
                        + "de referencias personales."
        );

        return buscarPorSolicitud(
                idSolicitudCredito
        );
    }

    // =========================================================
    // VALIDACIONES - PROCESO
    // =========================================================

    private SolicitudReferenciaListadoDTO validarProcesoEditable(
            Integer idSolicitudCredito
    ) {

        SolicitudReferenciaListadoDTO solicitud =
                buscarPorSolicitud(
                        idSolicitudCredito
                );

        validarProcesoNoCerrado(
                solicitud
        );

        if (
                solicitud.getIdSolicitudReferenciaProceso() == null
                        || !"EN_PROCESO".equals(
                        solicitud.getEstadoReferencias()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La gestión de referencias personales "
                            + "no se encuentra iniciada."
            );
        }

        return solicitud;
    }

    private void validarProcesoNoCerrado(
            SolicitudReferenciaListadoDTO solicitud
    ) {

        if (
                "CERRADO".equals(
                        solicitud.getEstadoReferencias()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La gestión de referencias personales "
                            + "ya se encuentra cerrada."
            );
        }
    }

    // =========================================================
    // VALIDACIONES - IDENTIFICADORES
    // =========================================================

    private void validarIdSolicitud(
            Integer idSolicitudCredito
    ) {

        if (
                idSolicitudCredito == null
                        || idSolicitudCredito <= 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar una solicitud "
                            + "de crédito válida."
            );
        }
    }

    private void validarIdParticipante(
            Integer idSolicitudDeudor
    ) {

        if (
                idSolicitudDeudor == null
                        || idSolicitudDeudor <= 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar un participante válido."
            );
        }
    }

    private void validarIdReferencia(
            Long idSolicitudReferenciaPersonal
    ) {

        if (
                idSolicitudReferenciaPersonal == null
                        || idSolicitudReferenciaPersonal <= 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar una referencia "
                            + "personal válida."
            );
        }
    }

    // =========================================================
    // VALIDACIONES - DATOS DE CONTACTO
    // =========================================================

    private void validarDatosContacto(
            String nombreCompleto,
            String telefonoCelular,
            String telefonoFijo
    ) {

        String nombre =
                limpiar(nombreCompleto);

        String celular =
                limpiar(telefonoCelular);

        String fijo =
                limpiar(telefonoFijo);

        if (nombre == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe registrar el nombre completo "
                            + "de la referencia personal."
            );
        }

        if (nombre.length() > 200) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de la referencia "
                            + "no puede superar 200 caracteres."
            );
        }

        if (
                celular == null
                        && fijo == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe registrar al menos "
                            + "un teléfono de contacto."
            );
        }

        if (
                celular != null
                        && celular.length() > 30
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El teléfono celular "
                            + "no puede superar 30 caracteres."
            );
        }

        if (
                fijo != null
                        && fijo.length() > 30
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El teléfono fijo "
                            + "no puede superar 30 caracteres."
            );
        }
    }

    // =========================================================
    // VALIDACIONES - ESTADO DE REFERENCIAS
    // =========================================================

    private String normalizarEstadoReferencias(
            String estadoReferencias
    ) {

        String estado =
                limpiar(estadoReferencias);

        if (estado == null) {
            return null;
        }

        estado =
                estado.toUpperCase(
                        Locale.ROOT
                );

        if (
                !"PENDIENTE".equals(estado)
                        && !"EN_PROCESO".equals(estado)
                        && !"CERRADO".equals(estado)
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado de referencias personales "
                            + "no válido."
            );
        }

        return estado;
    }

    // =========================================================
    // VALIDACIONES - MEDIO DE ENTREVISTA
    // =========================================================

    private String normalizarMedioEntrevista(
            String medioEntrevista
    ) {

        String medio =
                limpiar(medioEntrevista);

        if (medio == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar el medio "
                            + "de la entrevista."
            );
        }

        medio =
                medio.toUpperCase(
                        Locale.ROOT
                );

        if (
                !"CELULAR".equals(medio)
                        && !"FIJO".equals(medio)
                        && !"WHATSAPP".equals(medio)
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El medio de entrevista "
                            + "no es válido."
            );
        }

        return medio;
    }

    // =========================================================
    // VALIDACIONES - TIPO DE CIERRE
    // =========================================================

    private String normalizarTipoCierre(
            String tipoCierre
    ) {

        String tipo =
                limpiar(tipoCierre);

        if (tipo == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar el tipo "
                            + "de cierre."
            );
        }

        tipo =
                tipo.toUpperCase(
                        Locale.ROOT
                );

        if (
                !"CON_REFERENCIAS".equals(tipo)
                        && !"SIN_REFERENCIAS".equals(tipo)
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo de cierre "
                            + "no es válido."
            );
        }

        return tipo;
    }

    // =========================================================
    // VALIDAR RESULTADO DE ACTUALIZACIÓN
    // =========================================================

    private void validarActualizacion(
            int actualizados,
            String mensaje
    ) {

        if (actualizados != 1) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    mensaje
            );
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private String limpiar(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String limpio =
                valor.trim();

        return limpio.isEmpty()
                ? null
                : limpio;
    }
}