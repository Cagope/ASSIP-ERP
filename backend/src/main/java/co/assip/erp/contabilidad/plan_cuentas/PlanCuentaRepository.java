package co.assip.erp.contabilidad.plan_cuentas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanCuentaRepository extends JpaRepository<PlanCuenta, Integer> {

    List<PlanCuenta> findByIdAgenciaOrderByCodigoCuentaAsc(Integer idAgencia);

    Optional<PlanCuenta> findByIdAgenciaAndCodigoCuenta(Integer idAgencia, String codigoCuenta);

    @Query("""
           SELECT c FROM PlanCuenta c
           WHERE c.idAgencia = :idAgencia
             AND c.codigoCuenta LIKE CONCAT(:codigo, '%')
             AND c.codigoCuenta <> :codigo
           ORDER BY c.codigoCuenta
           """)
    List<PlanCuenta> buscarHijas(Integer idAgencia, String codigo);
}
