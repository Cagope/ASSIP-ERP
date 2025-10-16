package co.assip.erp.general.repository;

import co.assip.erp.general.domain.Parametro;
import co.assip.erp.general.domain.DatosAgencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametroRepository extends JpaRepository<Parametro, Integer> {

    // Búsqueda global
    Page<Parametro> findByNombreParametroContainingIgnoreCase(String q, Pageable pageable);
    Page<Parametro> findByCodigoParametro(Integer codigoParametro, Pageable pageable);

    // Búsqueda por agencia
    Page<Parametro> findByAgencia(DatosAgencia agencia, Pageable pageable);
    Page<Parametro> findByAgenciaAndNombreParametroContainingIgnoreCase(DatosAgencia agencia, String q, Pageable pageable);
    Page<Parametro> findByAgenciaAndCodigoParametro(DatosAgencia agencia, Integer codigoParametro, Pageable pageable);

    // Unicidad (id_agencia, codigo_parametro)
    boolean existsByAgenciaAndCodigoParametro(DatosAgencia agencia, Integer codigoParametro);
    boolean existsByAgenciaAndCodigoParametroAndIdParametroNot(DatosAgencia agencia, Integer codigoParametro, Integer idParametro);
}
