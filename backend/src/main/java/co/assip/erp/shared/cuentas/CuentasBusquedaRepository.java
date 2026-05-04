package co.assip.erp.shared.cuentas;

import co.assip.erp.shared.cuentas.dto.CuentaBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CuentasBusquedaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // 1) AUTOCOMPLETE
    // =========================================================
    public List<CuentaBusquedaDTO> buscar(
            String texto,
            Integer idAgencia,
            boolean esCentral
    ) {

        String sql = """
            SELECT
                c.id_catalogo_cuenta   AS id_cuenta,
                a.id_agencia           AS id_agencia,
                a.nombre_agencia       AS nombre_agencia,
                c.codigo_cuenta        AS codigo_cuenta,
                c.nombre_cuenta        AS nombre_cuenta
            FROM contabilidad.catalogo_cuentas c
            JOIN general.datos_agencias a
              ON a.id_agencia = c.id_agencia
            WHERE
                c.cuenta_operable = true
            AND (
                -- 🔎 Código: solo desde el inicio
                c.codigo_cuenta ILIKE :textoCodigo
                -- 🔎 Nombre: cualquier parte
                OR c.nombre_cuenta ILIKE :textoNombre
            )
            AND (
                :esCentral = true
                OR c.id_agencia = :idAgencia
            )
            ORDER BY c.codigo_cuenta
            LIMIT 20
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("textoCodigo", texto + "%")
                .addValue("textoNombre", "%" + texto + "%")
                .addValue("idAgencia", idAgencia)
                .addValue("esCentral", esCentral);

        return jdbc.query(sql, params, (rs, rowNum) ->
                new CuentaBusquedaDTO(
                        rs.getInt("id_cuenta"),
                        rs.getInt("id_agencia"),
                        rs.getString("nombre_agencia"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta")
                )
        );
    }

    // =========================================================
    // 2) OBTENER CUENTA POR ID (PARA EDICIÓN / CARGA FORM)
    // =========================================================
    public CuentaBusquedaDTO obtenerPorId(
            Long idCuenta,
            Integer idAgencia,
            boolean esCentral
    ) {

        String sql = """
            SELECT
                c.id_catalogo_cuenta   AS id_cuenta,
                a.id_agencia           AS id_agencia,
                a.nombre_agencia       AS nombre_agencia,
                c.codigo_cuenta        AS codigo_cuenta,
                c.nombre_cuenta        AS nombre_cuenta
            FROM contabilidad.catalogo_cuentas c
            JOIN general.datos_agencias a
              ON a.id_agencia = c.id_agencia
            WHERE
                c.id_catalogo_cuenta = :idCuenta
            AND (
                :esCentral = true
                OR c.id_agencia = :idAgencia
            )
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCuenta", idCuenta)
                .addValue("idAgencia", idAgencia)
                .addValue("esCentral", esCentral);

        return jdbc.queryForObject(sql, params, (rs, rowNum) ->
                new CuentaBusquedaDTO(
                        rs.getInt("id_cuenta"),
                        rs.getInt("id_agencia"),
                        rs.getString("nombre_agencia"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta")
                )
        );
    }

    public List<CuentaBusquedaDTO> buscarBancos(
            String texto,
            Integer idAgencia,
            boolean esCentral
    ) {

        String sql = """
        SELECT
            c.id_catalogo_cuenta   AS id_cuenta,
            a.id_agencia           AS id_agencia,
            a.nombre_agencia       AS nombre_agencia,
            c.codigo_cuenta        AS codigo_cuenta,
            c.nombre_cuenta        AS nombre_cuenta
        FROM contabilidad.catalogo_cuentas c
        JOIN general.datos_agencias a
          ON a.id_agencia = c.id_agencia
        WHERE
            c.cuenta_operable = true
        AND c.codigo_cuenta LIKE '1110%'
        AND (
            c.codigo_cuenta ILIKE :textoCodigo
            OR c.nombre_cuenta ILIKE :textoNombre
        )
        AND (
            :esCentral = true
            OR c.id_agencia = :idAgencia
        )
        ORDER BY c.codigo_cuenta
        LIMIT 20
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("textoCodigo", texto + "%")
                .addValue("textoNombre", "%" + texto + "%")
                .addValue("idAgencia", idAgencia)
                .addValue("esCentral", esCentral);

        return jdbc.query(sql, params, (rs, rowNum) ->
                new CuentaBusquedaDTO(
                        rs.getInt("id_cuenta"),
                        rs.getInt("id_agencia"),
                        rs.getString("nombre_agencia"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta")
                )
        );
    }
    public List<CuentaBusquedaDTO> buscarTrasladosAgencias(
            String texto,
            Integer idAgencia,
            boolean esCentral
    ) {

        String sql = """
        SELECT
            c.id_catalogo_cuenta   AS id_cuenta,
            a.id_agencia           AS id_agencia,
            a.nombre_agencia       AS nombre_agencia,
            c.codigo_cuenta        AS codigo_cuenta,
            c.nombre_cuenta        AS nombre_cuenta
        FROM contabilidad.catalogo_cuentas c
        JOIN general.datos_agencias a
          ON a.id_agencia = c.id_agencia
        WHERE
            c.cuenta_operable = true
        AND c.codigo_cuenta LIKE '2705%'
        AND (
            c.codigo_cuenta ILIKE :textoCodigo
            OR c.nombre_cuenta ILIKE :textoNombre
        )
        AND (
            :esCentral = true
            OR c.id_agencia = :idAgencia
        )
        ORDER BY c.codigo_cuenta
        LIMIT 20
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("textoCodigo", texto + "%")
                .addValue("textoNombre", "%" + texto + "%")
                .addValue("idAgencia", idAgencia)
                .addValue("esCentral", esCentral);

        return jdbc.query(sql, params, (rs, rowNum) ->
                new CuentaBusquedaDTO(
                        rs.getInt("id_cuenta"),
                        rs.getInt("id_agencia"),
                        rs.getString("nombre_agencia"),
                        rs.getString("codigo_cuenta"),
                        rs.getString("nombre_cuenta")
                )
        );
    }
}
