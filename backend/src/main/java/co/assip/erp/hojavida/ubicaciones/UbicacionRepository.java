package co.assip.erp.hojavida.ubicaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Integer> {

    /**
     * 🔹 Retorna todas las ubicaciones ordenadas por fecha de edición descendente.
     */
    List<Ubicacion> findAllByOrderByFechaEdicionDesc();

    /**
     * 🔹 Busca todas las ubicaciones asociadas a un ID de persona.
     * @param idDatosPersonal identificador de la persona.
     */
    List<Ubicacion> findByIdDatosPersonal(Integer idDatosPersonal);

    /**
     * 🔹 Busca todas las ubicaciones de una zona específica.
     * @param idZona identificador de la zona.
     */
    List<Ubicacion> findByIdZona(Integer idZona);

    /**
     * 🔹 Busca todas las ubicaciones de una subzona específica.
     * @param idSubZona identificador de la subzona.
     */
    List<Ubicacion> findByIdSubZona(Integer idSubZona);

    /**
     * 🔹 Busca todas las ubicaciones filtrando por zona y subzona (útil para reportes zonales).
     * @param idZona identificador de la zona.
     * @param idSubZona identificador de la subzona.
     */
    List<Ubicacion> findByIdZonaAndIdSubZona(Integer idZona, Integer idSubZona);
}
