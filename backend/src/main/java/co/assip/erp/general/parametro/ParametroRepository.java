package co.assip.erp.general.parametro;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 🗃️ Repositorio — Parametros
 * ------------------------------------------------------------
 * Acceso a datos para la tabla general.parametros
 */
public interface ParametroRepository extends JpaRepository<Parametro, Integer> {

    /** 🔍 Buscar un parámetro por agencia y código */
    Optional<Parametro> findByIdAgenciaAndCodigoParametro(Integer idAgencia, Integer codigoParametro);
}
