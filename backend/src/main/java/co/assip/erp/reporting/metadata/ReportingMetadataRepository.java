package co.assip.erp.reporting.metadata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 🧭 ReportingMetadataRepository
 * --------------------------------------------------------------
 * Permite listar las vistas disponibles del esquema `reporting`.
 * Usa consultas directas a `pg_catalog.pg_views` para detectar
 * las vistas creadas dinámicamente.
 */
@Repository
public interface ReportingMetadataRepository extends JpaRepository<ReportingMetadata, String> {

    /**
     * 🔹 Lista todas las vistas registradas dentro del esquema reporting.
     * Retorna solo nombre y definición básica.
     */
    @Query(value = """
        SELECT viewname AS view_name,
               obj_description(('reporting.' || viewname)::regclass, 'pg_class') AS description
        FROM pg_catalog.pg_views
        WHERE schemaname = 'reporting'
        ORDER BY viewname
        """, nativeQuery = true)
    List<Object[]> findAllViews();

    /**
     * 🔹 Recupera toda la metadata almacenada en la tabla reporting_metadata (si existe).
     */
    @Query(value = "SELECT * FROM reporting.reporting_metadata ORDER BY view_name", nativeQuery = true)
    List<ReportingMetadata> findAllMetadata();
}
