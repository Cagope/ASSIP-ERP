package co.assip.erp.reporting.api;

import co.assip.erp.reporting.impl.ReportesRepositoryImpl;
import co.assip.erp.reporting.metadata.ReportingMetadataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/**
 * ⚙️ ReportingService
 * ==============================================================
 * Servicio intermedio entre los controladores y la capa JDBC.
 * - No contiene lógica SQL directa.
 * - Delegación total a la implementación dinámica de Reporting.
 * ==============================================================
 */
@Service
@Transactional
public class ReportingService {

    private final ReportesRepositoryImpl repository;
    private final ReportingMetadataService metadataService;

    public ReportingService(ReportesRepositoryImpl repository,
                            ReportingMetadataService metadataService) {
        this.repository = repository;
        this.metadataService = metadataService;
    }

    /**
     * Ejecuta una consulta dinámica basada en el esquema, vista,
     * columnas, filtros y joins opcionales definidos en el request.
     */
    public ReportResult ejecutarConsulta(ReportQueryRequest request) {
        // 🔹 Ahora el repositorio sabrá interpretar los nuevos campos:
        // columns[], joins[], groupBy[], filters{}, aggregations{}
        return repository.ejecutarConsultaSegura(request);
    }

    /**
     * Retorna metadatos registrados de las vistas disponibles
     * en el módulo reporting.
     */
    public List<Map<String, Object>> listarMetadata() {
        return repository.listarMetadata();
    }

    /**
     * Lista las vistas registradas (vista, esquema, descripción, etc.)
     * a través del servicio de metadatos centralizado.
     */
    public Object listarVistas() {
        return metadataService.listarVistas();
    }
}
