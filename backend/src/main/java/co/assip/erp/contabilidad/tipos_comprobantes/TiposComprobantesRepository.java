package co.assip.erp.contabilidad.tipos_comprobantes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TiposComprobantesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<TipoComprobante> rowMapper = (rs, rowNum) -> {
        TipoComprobante t = new TipoComprobante();
        t.setTipoComprobante(rs.getString("tipo_comprobante"));
        t.setIdAgencia(rs.getInt("id_agencia"));
        t.setNombreTipoComprobante(rs.getString("nombre_tipo_comprobante"));
        t.setCscComprobante(rs.getInt("csc_comprobante"));
        t.setComprobanteActivo(rs.getBoolean("comprobante_activo"));
        t.setFkSeguridadCreacion(rs.getInt("fk_seguridad_creacion"));
        t.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        t.setFkSeguridadEdicion(rs.getInt("fk_seguridad_edicion"));
        t.setFechaEdicion(rs.getTimestamp("fecha_edicion").toLocalDateTime());
        return t;
    };

    // ============================
    // LISTAR (SOLUCIÓN DEFINITIVA)
    // ============================

    public List<TipoComprobante> listar(Integer idAgencia, Boolean soloActivos) {

        String sql = """
            SELECT *
            FROM contabilidad.tipos_comprobantes
            WHERE (:agencia IS NULL OR id_agencia = :agencia)
              AND (:activo IS NULL OR comprobante_activo = :activo)
            ORDER BY tipo_comprobante
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("agencia", idAgencia, Types.INTEGER)
                .addValue("activo", soloActivos, Types.BOOLEAN);

        return jdbc.query(sql, params, rowMapper);
    }

    // ============================
    // INSERT
    // ============================

    public void insertar(TipoComprobante t) {

        String sql = """
            INSERT INTO contabilidad.tipos_comprobantes (
                tipo_comprobante,
                id_agencia,
                nombre_tipo_comprobante,
                csc_comprobante,
                comprobante_activo,
                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            ) VALUES (
                :tipo,
                :agencia,
                :nombre,
                :csc,
                :activo,
                :usuario,
                now(),
                :usuario,
                now()
            )
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tipo", t.getTipoComprobante())
                .addValue("agencia", t.getIdAgencia(), Types.INTEGER)
                .addValue("nombre", t.getNombreTipoComprobante())
                .addValue("csc", t.getCscComprobante(), Types.INTEGER)
                .addValue("activo", t.getComprobanteActivo(), Types.BOOLEAN)
                .addValue("usuario", t.getFkSeguridadCreacion(), Types.INTEGER);

        jdbc.update(sql, params);
    }

    // ============================
    // UPDATE
    // ============================

    public void actualizar(TipoComprobante t) {

        String sql = """
            UPDATE contabilidad.tipos_comprobantes
            SET nombre_tipo_comprobante = :nombre,
                csc_comprobante = :csc,
                comprobante_activo = :activo,
                fk_seguridad_edicion = :usuario,
                fecha_edicion = now()
            WHERE tipo_comprobante = :tipo
              AND id_agencia = :agencia
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tipo", t.getTipoComprobante())
                .addValue("agencia", t.getIdAgencia(), Types.INTEGER)
                .addValue("nombre", t.getNombreTipoComprobante())
                .addValue("csc", t.getCscComprobante(), Types.INTEGER)
                .addValue("activo", t.getComprobanteActivo(), Types.BOOLEAN)
                .addValue("usuario", t.getFkSeguridadEdicion(), Types.INTEGER);

        jdbc.update(sql, params);
    }

    // ============================
// OBTENER POR PK (EDITAR)
// ============================

    public TipoComprobante obtener(String tipoComprobante, Integer idAgencia) {

        String sql = """
        SELECT *
        FROM contabilidad.tipos_comprobantes
        WHERE tipo_comprobante = :tipo
          AND id_agencia = :agencia
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tipo", tipoComprobante)
                .addValue("agencia", idAgencia, Types.INTEGER);

        return jdbc.queryForObject(sql, params, rowMapper);
    }

}
