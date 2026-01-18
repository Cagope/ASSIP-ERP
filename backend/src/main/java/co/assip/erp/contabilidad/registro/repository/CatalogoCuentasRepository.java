package co.assip.erp.contabilidad.registro.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository JDBC para validar reglas del catálogo contable.
 * Aquí usamos: control_entrada_salida (si exige documento soporte).
 */
@Repository
public class CatalogoCuentasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CatalogoCuentasRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * ✅ Retorna true si la cuenta exige control_entrada_salida = true.
     */
    public boolean requiereControlEntradaSalida(Integer idCatalogoCuenta) {

        String sql = """
            SELECT COALESCE(control_entrada_salida, false)
            FROM contabilidad.catalogo_cuentas
            WHERE id_catalogo_cuenta = :idCatalogoCuenta
        """;

        Boolean requiere = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idCatalogoCuenta", idCatalogoCuenta),
                Boolean.class
        );

        return requiere != null && requiere;
    }
}
