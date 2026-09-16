package co.assip.erp.cartera.originacion.centralriesgo;

import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoDTO;
import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoDetalleDTO;
import co.assip.erp.cartera.originacion.centralriesgo.dto.SolicitudCentralRiesgoGuardarRequestDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class SolicitudCentralRiesgoService {

    // =========================================================
    // ESTADOS TÉCNICOS DE LA CONSULTA
    // =========================================================

    private static final String CON_HISTORIAL =
            "CON_HISTORIAL";

    private static final String SIN_HISTORIAL =
            "SIN_HISTORIAL";


    // =========================================================
    // CALIFICACIONES CUALITATIVAS DEL MODELO
    // =========================================================

    private static final String CUALITATIVA_OK =
            "CON HISTORIAL - OK";

    private static final String CUALITATIVA_PERMANENCIAS =
            "CON PERMANENCIAS";

    private static final String CUALITATIVA_REPORTES =
            "CON REPORTES";

    private static final String CUALITATIVA_SIN_HISTORIAL =
            "SIN HISTORIAL";

    private static final Set<String>
            CALIFICACIONES_CUALITATIVAS_CON_HISTORIAL =
            Set.of(
                    CUALITATIVA_OK,
                    CUALITATIVA_PERMANENCIAS,
                    CUALITATIVA_REPORTES
            );


    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final SolicitudCentralRiesgoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public SolicitudCentralRiesgoService(
            SolicitudCentralRiesgoRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }


    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudCentralRiesgoDTO> listarPorSolicitud(
            Integer idSolicitudCredito
    ) {

        validarId(
                idSolicitudCredito,
                "solicitud"
        );

        Integer idAgencia =
                repository.obtenerAgenciaSolicitud(
                                idSolicitudCredito
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe la solicitud de crédito con id "
                                                + idSolicitudCredito
                                                + "."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        return repository.listarPorSolicitud(
                idSolicitudCredito
        );
    }


    // =========================================================
    // LISTAR POR DEUDOR
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudCentralRiesgoDTO> listarPorDeudor(
            Integer idSolicitudDeudor
    ) {

        validarId(
                idSolicitudDeudor,
                "deudor"
        );

        Integer idAgencia =
                repository.obtenerAgenciaDeudor(
                                idSolicitudDeudor
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe el deudor de la solicitud con id "
                                                + idSolicitudDeudor
                                                + "."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        return repository.listarPorDeudor(
                idSolicitudDeudor
        );
    }


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public Optional<SolicitudCentralRiesgoDetalleDTO> buscarPorId(
            Integer idSolicitudDeudorCentral
    ) {

        validarId(
                idSolicitudDeudorCentral,
                "registro de central de riesgo"
        );

        Optional<SolicitudCentralRiesgoDetalleDTO> resultado =
                repository.buscarPorId(
                        idSolicitudDeudorCentral
                );

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        Integer idAgencia =
                repository.obtenerAgenciaDeudor(
                                resultado.get()
                                        .getIdSolicitudDeudor()
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "No fue posible identificar la agencia de la solicitud."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        return resultado;
    }


    // =========================================================
    // GUARDAR / ACTUALIZAR
    // =========================================================

    public SolicitudCentralRiesgoDetalleDTO guardar(
            SolicitudCentralRiesgoGuardarRequestDTO request
    ) {

        validarRequest(
                request
        );

        /*
         * Normaliza y garantiza coherencia entre:
         *
         * - estado técnico de consulta;
         * - puntaje de la central;
         * - clasificación cualitativa.
         */
        normalizarResultadoCentral(
                request
        );

        /*
         * Bloquea la solicitud durante el registro o actualización
         * de la información de central de riesgo.
         *
         * También evita modificaciones cuando la solicitud ya
         * tiene un resultado final.
         */
        Integer idAgencia =
                repository.bloquearSolicitudEditable(
                                request.getIdSolicitudDeudor()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "El deudor no existe, la solicitud está inactiva o ya tiene un resultado final."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        /*
         * La central seleccionada debe existir y encontrarse activa.
         */
        if (!repository.existeCentralActiva(
                request.getIdCentralRiesgo()
        )) {

            throw new IllegalArgumentException(
                    "La central de riesgo seleccionada no existe o está inactiva."
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        /*
         * Regla funcional:
         *
         * Cada deudor de la solicitud trabaja con una sola
         * central de riesgo.
         *
         * El Repository realiza UPSERT por id_solicitud_deudor:
         *
         * - si no existe información, crea el registro;
         * - si ya existe, actualiza la misma fila;
         * - la central seleccionada puede cambiar;
         * - se conserva la auditoría de creación;
         * - se actualiza la auditoría de edición.
         */
        Integer idSolicitudDeudorCentral =
                repository.guardar(
                        request,
                        idUsuario
                );

        return repository.buscarPorId(
                        idSolicitudDeudorCentral
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "La información de la central de riesgo fue guardada pero no pudo ser consultada posteriormente."
                        )
                );
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarRequest(
            SolicitudCentralRiesgoGuardarRequestDTO request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La información de la central de riesgo es obligatoria."
            );
        }

        validarId(
                request.getIdSolicitudDeudor(),
                "deudor"
        );

        validarId(
                request.getIdCentralRiesgo(),
                "central de riesgo"
        );

        if (request.getFechaConsulta() == null) {

            throw new IllegalArgumentException(
                    "La fecha de consulta de la central de riesgo es obligatoria."
            );
        }

        if (request.getFechaConsulta().isAfter(
                LocalDate.now()
        )) {

            throw new IllegalArgumentException(
                    "La fecha de consulta de la central de riesgo no puede ser futura."
            );
        }


        // =====================================================
        // OBLIGACIONES
        // =====================================================

        validarNoNegativo(
                request.getValorInicialObligaciones(),
                "valor inicial de obligaciones"
        );

        validarNoNegativo(
                request.getSaldoActualObligaciones(),
                "saldo actual de obligaciones"
        );

        validarNoNegativo(
                request.getValorCuotasMensuales(),
                "valor de cuotas mensuales"
        );


        // =====================================================
        // RESULTADO CENTRAL
        // =====================================================

        validarNoNegativo(
                request.getPuntajeCentral(),
                "puntaje de la central"
        );

        validarResultadoCentral(
                request
        );


        // =====================================================
        // CALIFICACIONES
        // =====================================================

        validarCantidad(
                request.getCantidadCalificacionA(),
                "cantidad calificación A"
        );

        validarCantidad(
                request.getCantidadCalificacionB(),
                "cantidad calificación B"
        );

        validarCantidad(
                request.getCantidadCalificacionC(),
                "cantidad calificación C"
        );

        validarCantidad(
                request.getCantidadCalificacionD(),
                "cantidad calificación D"
        );

        validarCantidad(
                request.getCantidadCalificacionE(),
                "cantidad calificación E"
        );

        validarCantidad(
                request.getCantidadCalificacionK(),
                "cantidad calificación K"
        );


        // =====================================================
        // NOVEDADES
        // =====================================================

        validarCantidad(
                request.getCantidadReestructuraciones(),
                "cantidad de reestructuraciones"
        );

        validarCantidad(
                request.getCantidadRefinanciaciones(),
                "cantidad de refinanciaciones"
        );

        validarCantidad(
                request.getCantidadCuentasEmbargadas(),
                "cantidad de cuentas embargadas"
        );
    }


    // =========================================================
    // VALIDAR RESULTADO CENTRAL
    // =========================================================

    private void validarResultadoCentral(
            SolicitudCentralRiesgoGuardarRequestDTO request
    ) {

        String estado =
                normalizarCodigo(
                        request.getCalificacionCentral()
                );

        if (estado == null) {

            throw new IllegalArgumentException(
                    "Seleccione el estado de la consulta de la central de riesgo."
            );
        }

        if (!CON_HISTORIAL.equals(estado)
                && !SIN_HISTORIAL.equals(estado)) {

            throw new IllegalArgumentException(
                    "El estado de la consulta de la central de riesgo no es válido."
            );
        }


        // -----------------------------------------------------
        // CON HISTORIAL
        // -----------------------------------------------------

        if (CON_HISTORIAL.equals(estado)) {

            if (request.getPuntajeCentral() == null) {

                throw new IllegalArgumentException(
                        "El puntaje de la central es obligatorio cuando la persona tiene historial."
                );
            }

            String calificacionCualitativa =
                    normalizarTextoMayuscula(
                            request.getCalificacionCualitativa()
                    );

            if (calificacionCualitativa == null) {

                throw new IllegalArgumentException(
                        "Seleccione la calificación cualitativa de la central de riesgo."
                );
            }

            if (!CALIFICACIONES_CUALITATIVAS_CON_HISTORIAL.contains(
                    calificacionCualitativa
            )) {

                throw new IllegalArgumentException(
                        "La calificación cualitativa no es válida para una consulta con historial."
                );
            }

            return;
        }


        // -----------------------------------------------------
        // SIN HISTORIAL
        // -----------------------------------------------------

        /*
         * SIN_HISTORIAL no utiliza puntaje numérico.
         *
         * La clasificación cualitativa correspondiente será
         * establecida automáticamente como SIN HISTORIAL.
         */
    }


    // =========================================================
    // NORMALIZAR RESULTADO CENTRAL
    // =========================================================

    private void normalizarResultadoCentral(
            SolicitudCentralRiesgoGuardarRequestDTO request
    ) {

        String estado =
                normalizarCodigo(
                        request.getCalificacionCentral()
                );

        request.setCalificacionCentral(
                estado
        );

        if (SIN_HISTORIAL.equals(estado)) {

            /*
             * Regla del modelo:
             *
             * SIN HISTORIAL:
             *
             * - no tiene puntaje numérico;
             * - cualitativamente corresponde a SIN HISTORIAL.
             */
            request.setPuntajeCentral(
                    null
            );

            request.setCalificacionCualitativa(
                    CUALITATIVA_SIN_HISTORIAL
            );

            return;
        }

        request.setCalificacionCualitativa(
                normalizarTextoMayuscula(
                        request.getCalificacionCualitativa()
                )
        );
    }


    // =========================================================
    // SOPORTE
    // =========================================================

    private void validarId(
            Integer valor,
            String campo
    ) {

        if (valor == null
                || valor <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de "
                            + campo
                            + " no es válido."
            );
        }
    }


    private void validarNoNegativo(
            BigDecimal valor,
            String campo
    ) {

        if (valor != null
                && valor.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El campo "
                            + campo
                            + " no puede ser negativo."
            );
        }
    }


    private void validarCantidad(
            Integer valor,
            String campo
    ) {

        if (valor != null
                && valor < 0) {

            throw new IllegalArgumentException(
                    "El campo "
                            + campo
                            + " no puede ser negativo."
            );
        }
    }


    private String normalizarCodigo(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String texto =
                valor.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return texto.isEmpty()
                ? null
                : texto;
    }


    private String normalizarTextoMayuscula(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String texto =
                valor.trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        )
                        .toUpperCase(
                                Locale.ROOT
                        );

        return texto.isEmpty()
                ? null
                : texto;
    }
}