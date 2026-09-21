package co.assip.erp.cartera.originacion.impresion;

import co.assip.erp.cartera.originacion.aprobacion.SolicitudAprobacionService;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionActuacionDTO;
import co.assip.erp.cartera.originacion.aprobacion.dto.SolicitudAprobacionFotosDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OriginacionImpresionService {

    private final SolicitudAprobacionService aprobacionService;

    public OriginacionImpresionService(
            SolicitudAprobacionService aprobacionService
    ) {
        this.aprobacionService = aprobacionService;
    }

    // =========================================================
    // EXPEDIENTE DE SOLICITUD
    // =========================================================

    public ExpedienteImpresion obtenerExpediente(
            Integer idSolicitudCredito
    ) {

        validarSolicitud(idSolicitudCredito);

        SolicitudAprobacionFotosDTO fotos =
                aprobacionService.obtenerFotos(
                        idSolicitudCredito
                );

        List<SolicitudAprobacionActuacionDTO> actuaciones =
                aprobacionService.listarActuaciones(
                        idSolicitudCredito
                );

        return new ExpedienteImpresion(
                idSolicitudCredito,
                null,
                fotos,
                actuaciones
        );
    }

    // =========================================================
    // EXPEDIENTE DE UNA ACTUACIÓN HISTÓRICA
    // =========================================================

    public ExpedienteImpresion obtenerExpedienteActuacion(
            Integer idSolicitudCredito,
            Integer idSolicitudAprobacion
    ) {

        validarSolicitud(idSolicitudCredito);

        if (idSolicitudAprobacion == null
                || idSolicitudAprobacion <= 0) {

            throw new IllegalArgumentException(
                    "La actuación de aprobación es obligatoria."
            );
        }

        SolicitudAprobacionFotosDTO fotos =
                aprobacionService.obtenerFotosActuacion(
                        idSolicitudCredito,
                        idSolicitudAprobacion
                );

        List<SolicitudAprobacionActuacionDTO> actuaciones =
                aprobacionService.listarActuaciones(
                        idSolicitudCredito
                );

        return new ExpedienteImpresion(
                idSolicitudCredito,
                idSolicitudAprobacion,
                fotos,
                actuaciones
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null
                || idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "La solicitud de crédito es obligatoria."
            );
        }
    }

    // =========================================================
    // DATOS PARA GENERACIÓN DEL PDF
    // =========================================================

    public record ExpedienteImpresion(

            Integer idSolicitudCredito,

            Integer idSolicitudAprobacion,

            SolicitudAprobacionFotosDTO fotos,

            List<SolicitudAprobacionActuacionDTO> actuaciones

    ) {
    }
}