package co.assip.erp.cartera.originacion.solicitudes.list;

import co.assip.erp.cartera.originacion.solicitudes.list.dto.SolicitudListadoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SolicitudListadoService {

    private final SolicitudListadoRepository repository;


    public SolicitudListadoService(
            SolicitudListadoRepository repository
    ) {
        this.repository = repository;
    }


    // =========================================================
    // LISTAR SOLICITUDES
    // =========================================================

    public List<SolicitudListadoDTO> listar(
            Integer idAgencia,
            String numeroSolicitud,
            String documento,
            String nombreSolicitante,
            Integer idAsesor,
            Integer idSolicitudProceso,
            Integer idSolicitudResultado
    ) {

        return repository.listar(
                normalizarId(idAgencia),
                normalizarTexto(numeroSolicitud),
                normalizarTexto(documento),
                normalizarTexto(nombreSolicitante),
                normalizarId(idAsesor),
                normalizarId(idSolicitudProceso),
                normalizarId(idSolicitudResultado)
        );
    }


    // =========================================================
    // NORMALIZACIÓN
    // =========================================================

    private Integer normalizarId(Integer valor) {

        if (valor == null || valor <= 0) {
            return null;
        }

        return valor;
    }


    private String normalizarTexto(String valor) {

        if (valor == null) {
            return null;
        }

        String texto = valor.trim();

        return texto.isEmpty()
                ? null
                : texto;
    }
}