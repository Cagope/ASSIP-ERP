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

    public ReportResult ejecutarConsulta(ReportQueryRequest request) {
        return repository.ejecutarConsultaSegura(request);
    }

    public List<Map<String, Object>> listarMetadata() {
        return repository.listarMetadata();
    }

    public Object listarVistas() {
        return metadataService.listarVistas();
    }
}
