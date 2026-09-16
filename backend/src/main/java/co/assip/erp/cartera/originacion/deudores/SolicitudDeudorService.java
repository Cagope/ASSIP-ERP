package co.assip.erp.cartera.originacion.deudores;

import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorAgregarRequestDTO;
import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorDTO;
import co.assip.erp.cartera.originacion.deudores.dto.SolicitudDeudorDetalleDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SolicitudDeudorService {

    private static final String TIPO_CODEUDOR = "CODEUDOR";

    private final SolicitudDeudorRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public SolicitudDeudorService(
            SolicitudDeudorRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService =
                usuarioSesionService;
    }


    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudDeudorDTO> listarPorSolicitud(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitudCredito(
                idSolicitudCredito
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
    // BUSCAR POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public Optional<SolicitudDeudorDetalleDTO> buscarPorId(
            Integer idSolicitudDeudor
    ) {

        validarIdSolicitudDeudor(
                idSolicitudDeudor
        );

        Optional<SolicitudDeudorDetalleDTO> deudor =
                repository.buscarPorId(
                        idSolicitudDeudor
                );

        if (deudor.isEmpty()) {
            return Optional.empty();
        }

        Integer idAgencia =
                repository.obtenerAgenciaSolicitud(
                                deudor.get()
                                        .getIdSolicitudCredito()
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "La solicitud asociada al deudor no existe o no está activa."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        return deudor;
    }


    // =========================================================
    // AGREGAR CODEUDOR
    // =========================================================

    public SolicitudDeudorDetalleDTO agregarCodeudor(
            SolicitudDeudorAgregarRequestDTO request
    ) {

        validarAgregar(
                request
        );

        /*
         * Bloqueamos la solicitud durante la operación.
         *
         * Esto evita asignar el mismo orden a dos codeudores
         * concurrentemente y además impide modificar solicitudes
         * que ya tengan un resultado final.
         */
        Integer idAgencia =
                repository.bloquearSolicitudEditable(
                                request.getIdSolicitudCredito()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "La solicitud no existe, está inactiva o ya tiene un resultado final."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        if (!repository.existePersona(
                request.getIdDatosPersonal()
        )) {

            throw new IllegalArgumentException(
                    "La persona seleccionada no existe."
            );
        }

        Optional<Integer> deudorActivo =
                repository.buscarActivoPorPersona(
                        request.getIdSolicitudCredito(),
                        request.getIdDatosPersonal()
                );

        if (deudorActivo.isPresent()) {

            throw new IllegalArgumentException(
                    "La persona ya se encuentra vinculada a la solicitud."
            );
        }

        Integer ordenDeudor =
                repository.obtenerSiguienteOrden(
                        request.getIdSolicitudCredito()
                );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        /*
         * Si la persona estuvo anteriormente vinculada y fue
         * retirada, reutilizamos el registro histórico en lugar
         * de crear un duplicado.
         *
         * Sus fotografías operativas se reinician.
         */
        Optional<Integer> deudorInactivo =
                repository.buscarInactivoPorPersona(
                        request.getIdSolicitudCredito(),
                        request.getIdDatosPersonal()
                );

        Integer idSolicitudDeudor;

        if (deudorInactivo.isPresent()) {

            idSolicitudDeudor =
                    deudorInactivo.get();

            repository.reactivarCodeudor(
                    idSolicitudDeudor,
                    ordenDeudor,
                    idUsuario
            );

        } else {

            idSolicitudDeudor =
                    repository.insertarCodeudor(
                            request.getIdSolicitudCredito(),
                            request.getIdDatosPersonal(),
                            ordenDeudor,
                            idUsuario
                    );
        }

        return repository.buscarPorId(
                        idSolicitudDeudor
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "El codeudor fue registrado pero no pudo ser consultado posteriormente."
                        )
                );
    }


    // =========================================================
    // RETIRAR CODEUDOR
    // =========================================================

    public void retirarCodeudor(
            Integer idSolicitudDeudor
    ) {

        validarIdSolicitudDeudor(
                idSolicitudDeudor
        );

        SolicitudDeudorDetalleDTO deudor =
                repository.buscarPorId(
                                idSolicitudDeudor
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe el deudor con id "
                                                + idSolicitudDeudor
                                                + "."
                                )
                        );

        /*
         * El deudor principal forma parte estructural de la
         * solicitud y no se elimina desde este endpoint.
         */
        if (!TIPO_CODEUDOR.equalsIgnoreCase(
                normalizar(
                        deudor.getTipoDeudor()
                )
        )) {

            throw new IllegalArgumentException(
                    "El deudor principal de la solicitud no puede ser retirado."
            );
        }

        Integer idAgencia =
                repository.bloquearSolicitudEditable(
                                deudor.getIdSolicitudCredito()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "La solicitud no existe, está inactiva o ya tiene un resultado final."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        /*
         * No borramos físicamente información histórica.
         *
         * Primero desactivamos las fotografías dependientes y
         * finalmente el codeudor.
         */
        repository.desactivarDependencias(
                idSolicitudDeudor,
                idUsuario
        );

        boolean retirado =
                repository.desactivarCodeudor(
                        idSolicitudDeudor,
                        idUsuario
                );

        if (!retirado) {

            throw new IllegalStateException(
                    "No fue posible retirar el codeudor."
            );
        }
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarAgregar(
            SolicitudDeudorAgregarRequestDTO request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La información del codeudor es obligatoria."
            );
        }

        validarIdSolicitudCredito(
                request.getIdSolicitudCredito()
        );

        if (request.getIdDatosPersonal() == null
                || request.getIdDatosPersonal() <= 0) {

            throw new IllegalArgumentException(
                    "La persona del codeudor no es válida."
            );
        }
    }

    private void validarIdSolicitudCredito(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null
                || idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la solicitud no es válido."
            );
        }
    }

    private void validarIdSolicitudDeudor(
            Integer idSolicitudDeudor
    ) {

        if (idSolicitudDeudor == null
                || idSolicitudDeudor <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del deudor no es válido."
            );
        }
    }

    private String normalizar(
            String valor
    ) {

        return valor == null
                ? ""
                : valor.trim();
    }
}