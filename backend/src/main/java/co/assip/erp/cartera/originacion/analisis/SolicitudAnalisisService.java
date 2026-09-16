package co.assip.erp.cartera.originacion.analisis;

import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisComponenteDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisDeudorDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisPersistenciaDTO;
import co.assip.erp.cartera.originacion.analisis.dto.SolicitudAnalisisResultadoDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SolicitudAnalisisService {

    private final SolicitudAnalisisRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public SolicitudAnalisisService(
            SolicitudAnalisisRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }


    // =========================================================
    // RESULTADO ACTUAL DE LA SOLICITUD
    // =========================================================

    @Transactional(readOnly = true)
    public Optional<SolicitudAnalisisResultadoDTO> obtenerResultadoSolicitud(
            Integer idSolicitudCredito
    ) {

        validarAccesoSolicitud(
                idSolicitudCredito
        );

        return repository.obtenerResultadoSolicitud(
                idSolicitudCredito
        );
    }


    // =========================================================
    // RESULTADOS POR DEUDOR
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudAnalisisDeudorDTO> listarDeudores(
            Integer idSolicitudCredito
    ) {

        validarAccesoSolicitud(
                idSolicitudCredito
        );

        return repository.listarDeudores(
                idSolicitudCredito
        );
    }


    // =========================================================
    // COMPONENTES
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudAnalisisComponenteDTO> listarComponentes(
            Integer idSolicitudCredito
    ) {

        validarAccesoSolicitud(
                idSolicitudCredito
        );

        return repository.listarComponentes(
                idSolicitudCredito
        );
    }


    // =========================================================
    // GUARDAR ANÁLISIS ACTUAL
    // =========================================================

    public SolicitudAnalisisPersistenciaDTO persistirAnalisis(
            Integer idSolicitudCredito
    ) {

        /*
         * PostgreSQL, mediante las vistas de análisis, determina:
         *
         * - modelo aplicable;
         * - componentes;
         * - indicadores;
         * - reglas;
         * - puntajes;
         * - capacidad de pago;
         * - perfil de riesgo;
         * - recomendación;
         * - viabilidad.
         *
         * Java controla:
         *
         * - existencia y acceso a la solicitud;
         * - usuario autenticado;
         * - transacción;
         * - validaciones de consistencia;
         * - persistencia del resultado actual.
         *
         * No se generan ejecuciones históricas.
         * Cada deudor conserva un único registro de análisis.
         */
        validarAccesoSolicitud(
                idSolicitudCredito
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return repository.persistirAnalisis(
                idSolicitudCredito,
                idUsuario
        );
    }


    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAccesoSolicitud(
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
    }


    // =========================================================
    // VALIDACIÓN
    // =========================================================

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
}