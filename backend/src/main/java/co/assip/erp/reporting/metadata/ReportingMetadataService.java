package co.assip.erp.reporting.metadata;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * ⚙️ ReportingMetadataService
 * ==============================================================
 * Servicio encargado de administrar y exponer la lista de vistas
 * disponibles dentro del esquema `reporting`.
 *
 * Este servicio consulta directamente a pg_catalog.pg_views
 * para no depender de tablas auxiliares.
 * ==============================================================
 */
@Service
@Transactional(readOnly = true)
public class ReportingMetadataService {

    private final ReportingMetadataRepository repository;

    public ReportingMetadataService(ReportingMetadataRepository repository) {
        this.repository = repository;
    }

    /**
     * 🔹 Obtiene todas las vistas del esquema reporting (nombre + comentario).
     */
    public List<ReportingMetadata> listarVistas() {
        List<Object[]> filas = repository.findAllViews();
        List<ReportingMetadata> vistas = new ArrayList<>();

        for (Object[] fila : filas) {
            ReportingMetadata meta = new ReportingMetadata();
            meta.setViewName((String) fila[0]);
            meta.setDescription(fila[1] != null ? fila[1].toString() : "(sin descripción)");
            vistas.add(meta);
        }

        return vistas;
    }

    /**
     * 🔹 Devuelve la metadata persistida (si se está guardando en tabla local).
     */
    public List<ReportingMetadata> listarMetadata() {
        return repository.findAllMetadata();
    }
}
