package co.assip.erp.contabilidad.plan_cuentas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanCuentaRepository extends JpaRepository<PlanCuenta, Integer> {

    // ==========================================================
    // LISTAR POR AGENCIA (catálogo completo)
    // ==========================================================
    List<PlanCuenta> findByIdAgenciaOrderByCodigoCuentaAsc(Integer idAgencia);

    // ==========================================================
    // BUSCAR EXACTA POR AGENCIA + CÓDIGO (validaciones jerarquía)
    // ==========================================================
    Optional<PlanCuenta> findByIdAgenciaAndCodigoCuenta(Integer idAgencia, String codigoCuenta);

    // ==========================================================
    // HIJAS POR PREFIJO (para validar jerarquía / eliminar)
    // ==========================================================
    @Query("""
           SELECT c FROM PlanCuenta c
           WHERE c.idAgencia = :idAgencia
             AND c.codigoCuenta LIKE CONCAT(:codigo, '%')
             AND c.codigoCuenta <> :codigo
           ORDER BY c.codigoCuenta
           """)
    List<PlanCuenta> buscarHijas(Integer idAgencia, String codigo);

    // ==========================================================
    // AUTOCOMPLETE (por agencia) — desde 2 caracteres (eso se valida en front/service)
    // • Busca por prefijo de código: "17" -> "17%"
    // • Busca por nombre: "iva" -> "%iva%"
    // ==========================================================
    @Query("""
       SELECT c
       FROM PlanCuenta c
       WHERE c.idAgencia = :idAgencia
         AND c.operable = true
         AND (
              LOWER(c.codigoCuenta) LIKE LOWER(CONCAT(:texto, '%'))
           OR LOWER(c.nombre)       LIKE LOWER(CONCAT('%', :texto, '%'))
         )
       ORDER BY c.codigoCuenta
       """)
    List<PlanCuenta> buscarPorAgenciaYTexto(Integer idAgencia, String texto);

    // ==========================================================
    // VALIDAR SI LA CUENTA EXIGE CONTROL DE DOCUMENTO
    // ==========================================================
    @Query("""
       SELECT COALESCE(p.controlEntradaSalida, false)
       FROM PlanCuenta p
       WHERE p.id = :idCatalogoCuenta
       """)
    Boolean requiereControlEntradaSalida(Integer idCatalogoCuenta);

}
