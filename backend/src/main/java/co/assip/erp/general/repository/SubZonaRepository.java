package co.assip.erp.general.repository;

import co.assip.erp.general.domain.SubZona;
import co.assip.erp.general.domain.Zona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubZonaRepository extends JpaRepository<SubZona, Integer> {

    // =======================
    // MÉTODOS EXISTENTES (Entidad)
    // =======================
    Page<SubZona> findByNombreSubZonaContainingIgnoreCase(String q, Pageable pageable);
    Page<SubZona> findByCodigoSubZonaContainingIgnoreCase(String q, Pageable pageable);

    Page<SubZona> findByZonaAndNombreSubZonaContainingIgnoreCase(Zona zona, String q, Pageable pageable);
    Page<SubZona> findByZonaAndCodigoSubZonaContainingIgnoreCase(Zona zona, String q, Pageable pageable);
    Page<SubZona> findByZona(Zona zona, Pageable pageable);

    boolean existsByZonaAndCodigoSubZona(Zona zona, String codigoSubZona);
    boolean existsByZonaAndCodigoSubZonaAndIdSubZonaNot(Zona zona, String codigoSubZona, Integer idSubZona);

    // =======================
    // NUEVO: PROYECCIÓN PARA LISTADO (incluye nombreZona)
    // =======================
    interface SubZonaListView {
        Integer getIdSubZona();
        Integer getIdZona();
        String  getCodigoSubZona();
        String  getNombreSubZona();
        String  getComentarioSubZona();
        String  getNombreZona();
    }

    /** Listado completo con nombre de zona (sin lookups en el front) */
    @Query(value = """
        SELECT 
          sz.id_sub_zona         AS idSubZona,
          sz.id_zona             AS idZona,
          sz.codigo_sub_zona     AS codigoSubZona,
          sz.nombre_sub_zona     AS nombreSubZona,
          sz.comentario_sub_zona AS comentarioSubZona,
          z.nombre_zona          AS nombreZona
        FROM general.sub_zonas sz
        JOIN general.zonas z ON z.id_zona = sz.id_zona
        ORDER BY z.nombre_zona, sz.codigo_sub_zona
        """, nativeQuery = true)
    List<SubZonaListView> findAllWithZona();

    /** Listado filtrado por zona con nombre de zona */
    @Query(value = """
        SELECT 
          sz.id_sub_zona         AS idSubZona,
          sz.id_zona             AS idZona,
          sz.codigo_sub_zona     AS codigoSubZona,
          sz.nombre_sub_zona     AS nombreSubZona,
          sz.comentario_sub_zona AS comentarioSubZona,
          z.nombre_zona          AS nombreZona
        FROM general.sub_zonas sz
        JOIN general.zonas z ON z.id_zona = sz.id_zona
        WHERE sz.id_zona = :idZona
        ORDER BY sz.codigo_sub_zona
        """, nativeQuery = true)
    List<SubZonaListView> findByIdZonaWithZona(@Param("idZona") Integer idZona);
}
