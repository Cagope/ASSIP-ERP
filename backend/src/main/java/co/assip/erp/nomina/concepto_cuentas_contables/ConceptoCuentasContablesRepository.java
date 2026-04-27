package co.assip.erp.nomina.concepto_cuentas_contables;

import co.assip.erp.nomina.concepto_cuentas_contables.dto.ConceptoCuentaContableFormDTO;
import co.assip.erp.nomina.concepto_cuentas_contables.dto.ConceptoCuentaContableListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConceptoCuentasContablesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // LISTAR
    // ============================================================
    public List<ConceptoCuentaContableListDTO> listar() {

        String sql = """
        SELECT
          c.id_mapeo           AS idMapeo,
          c.codigo_concepto    AS codigoConcepto,
          c.id_agencia         AS idAgencia,
          a.nombre             AS nombreAgencia,

          c.id_cuenta_debito   AS idCuentaDebito,
          cd.codigo_cuenta || ' - ' || cd.nombre_cuenta AS cuentaDebito,

          c.id_cuenta_credito  AS idCuentaCredito,
          cc.codigo_cuenta || ' - ' || cc.nombre_cuenta AS cuentaCredito,

          c.activo             AS activo
        FROM nomina.concepto_cuentas_contables c

        INNER JOIN shared.vw_general_agencias a
          ON a.id = c.id_agencia

        LEFT JOIN contabilidad.catalogo_cuentas cd
          ON cd.id_catalogo_cuenta = c.id_cuenta_debito
         AND cd.id_agencia = c.id_agencia

        LEFT JOIN contabilidad.catalogo_cuentas cc
          ON cc.id_catalogo_cuenta = c.id_cuenta_credito
         AND cc.id_agencia = c.id_agencia

        ORDER BY a.nombre, c.codigo_concepto
        """;

        return jdbc.query(sql, new MapSqlParameterSource(), (rs, rowNum) ->
                ConceptoCuentaContableListDTO.builder()
                        .idMapeo(rs.getInt("idMapeo"))
                        .codigoConcepto(rs.getString("codigoConcepto"))
                        .idAgencia((Integer) rs.getObject("idAgencia"))
                        .nombreAgencia(rs.getString("nombreAgencia"))
                        .idCuentaDebito((Integer) rs.getObject("idCuentaDebito"))
                        .cuentaDebito(rs.getString("cuentaDebito"))
                        .idCuentaCredito((Integer) rs.getObject("idCuentaCredito"))
                        .cuentaCredito(rs.getString("cuentaCredito"))
                        .activo(rs.getBoolean("activo"))
                        .build()
        );
    }

    // ============================================================
    // OBTENER
    // ============================================================
    public Optional<ConceptoCuentaContableFormDTO> obtener(Integer idMapeo) {

        String sql = """
        SELECT
          c.id_mapeo            AS idMapeo,
          c.codigo_concepto     AS codigoConcepto,
          c.id_agencia          AS idAgencia,

          c.id_cuenta_debito    AS idCuentaDebito,
          cd.codigo_cuenta || ' - ' || cd.nombre_cuenta AS cuentaDebito,

          c.id_cuenta_credito   AS idCuentaCredito,
          cc.codigo_cuenta || ' - ' || cc.nombre_cuenta AS cuentaCredito,

          c.activo              AS activo
        FROM nomina.concepto_cuentas_contables c

        LEFT JOIN contabilidad.catalogo_cuentas cd
          ON cd.id_catalogo_cuenta = c.id_cuenta_debito
         AND cd.id_agencia = c.id_agencia

        LEFT JOIN contabilidad.catalogo_cuentas cc
          ON cc.id_catalogo_cuenta = c.id_cuenta_credito
         AND cc.id_agencia = c.id_agencia

        WHERE c.id_mapeo = :idMapeo
        """;

        var params = new MapSqlParameterSource()
                .addValue("idMapeo", idMapeo);

        List<ConceptoCuentaContableFormDTO> rows = jdbc.query(sql, params,
                (rs, rowNum) -> ConceptoCuentaContableFormDTO.builder()
                        .idMapeo(rs.getInt("idMapeo"))
                        .codigoConcepto(rs.getString("codigoConcepto"))
                        .idAgencia((Integer) rs.getObject("idAgencia"))
                        .idCuentaDebito((Integer) rs.getObject("idCuentaDebito"))
                        .cuentaDebito(rs.getString("cuentaDebito"))
                        .idCuentaCredito((Integer) rs.getObject("idCuentaCredito"))
                        .cuentaCredito(rs.getString("cuentaCredito"))
                        .activo(rs.getBoolean("activo"))
                        .build()
        );

        return rows.stream().findFirst();
    }

    // ============================================================
    // CREAR
    // ============================================================
    public void crear(ConceptoCuentaContableFormDTO dto, Integer idUsuario) {

        if (existeActivo(dto.getCodigoConcepto(), dto.getIdAgencia())) {
            throw new RuntimeException("Ya existe una configuración activa para este concepto y agencia.");
        }

        String sql = """
            INSERT INTO nomina.concepto_cuentas_contables (
              codigo_concepto,
              id_agencia,
              id_cuenta_debito,
              id_cuenta_credito,
              activo,
              fk_seguridad_creacion,
              fk_seguridad_edicion
            )
            VALUES (
              :codigoConcepto,
              :idAgencia,
              :idCuentaDebito,
              :idCuentaCredito,
              :activo,
              :usr,
              :usr
            )
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigoConcepto", dto.getCodigoConcepto())
                .addValue("idAgencia", dto.getIdAgencia())
                .addValue("idCuentaDebito", dto.getIdCuentaDebito())
                .addValue("idCuentaCredito", dto.getIdCuentaCredito())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idMapeo, ConceptoCuentaContableFormDTO dto, Integer idUsuario) {

        String sql = """
            UPDATE nomina.concepto_cuentas_contables
            SET
              id_cuenta_debito  = :idCuentaDebito,
              id_cuenta_credito = :idCuentaCredito,
              activo            = :activo,
              fk_seguridad_edicion = :usr,
              fecha_edicion     = CURRENT_TIMESTAMP
            WHERE id_mapeo = :idMapeo
        """;

        var params = new MapSqlParameterSource()
                .addValue("idMapeo", idMapeo)
                .addValue("idCuentaDebito", dto.getIdCuentaDebito())
                .addValue("idCuentaCredito", dto.getIdCuentaCredito())
                .addValue("activo", dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // ============================================================
    // ELIMINAR (SOFT)
    // ============================================================
    public void eliminar(Integer idMapeo, Integer idUsuario) {

        String sql = """
            UPDATE nomina.concepto_cuentas_contables
            SET
              activo = FALSE,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_mapeo = :idMapeo
        """;

        var params = new MapSqlParameterSource()
                .addValue("idMapeo", idMapeo)
                .addValue("usr", idUsuario != null ? idUsuario : 1);

        jdbc.update(sql, params);
    }

    // ============================================================
    // VALIDAR DUPLICADO
    // ============================================================
    public boolean existeActivo(String codigoConcepto, Integer idAgencia) {

        String sql = """
            SELECT COUNT(1)
            FROM nomina.concepto_cuentas_contables
            WHERE codigo_concepto = :codigoConcepto
              AND id_agencia = :idAgencia
              AND activo = TRUE
        """;

        var params = new MapSqlParameterSource()
                .addValue("codigoConcepto", codigoConcepto)
                .addValue("idAgencia", idAgencia);

        Integer count = jdbc.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}