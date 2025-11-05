package co.assip.erp.reporting.api;

import co.assip.erp.reporting.impl.ReportesRepositoryImpl;
import co.assip.erp.reporting.metadata.ReportingMetadataService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

/**
 * 📊 ReportingController — Módulo Reporting ASSIP-ERP
 * ==============================================================
 * Endpoint REST principal del módulo de reportes.
 *
 * 🔹 Endpoints:
 *   GET  /api/v1/reporting/metadata  → Listar vistas disponibles
 *   POST /api/v1/reporting/query     → Ejecutar consulta dinámica segura
 *
 * Autor: Carlos González Pérez
 * Empresa: ERP ASSIP SOLIDARIA Y FINANCIERA
 * Fecha: 2025-11-03
 * ==============================================================
 */
@RestController
@RequestMapping(value = "/reporting", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportingController {

    private final ReportesRepositoryImpl repository;
    private final ReportingMetadataService metadataService;

    @Autowired
    public ReportingController(ReportesRepositoryImpl repository,
                               ReportingMetadataService metadataService) {
        this.repository = repository;
        this.metadataService = metadataService;
    }

    /**
     * 🔹 Ejecutar una consulta dinámica segura sobre una vista del esquema reporting.
     */
    @PostMapping("/query")
    public ReportResult ejecutarConsulta(@RequestBody ReportQueryRequest request) {
        return repository.ejecutarConsultaSegura(request);
    }

    /**
     * 🔹 Listar metadatos y vistas disponibles en el esquema reporting.
     */
    @GetMapping("/metadata")
    public List<Map<String, Object>> listarMetadata() {
        return repository.listarMetadata();
    }

    /**
     * 🔹 Listar las vistas detectadas con sus descripciones (usando pg_catalog).
     */
    @GetMapping("/vistas")
    public Object listarVistas() {
        return metadataService.listarVistas();
    }
}
