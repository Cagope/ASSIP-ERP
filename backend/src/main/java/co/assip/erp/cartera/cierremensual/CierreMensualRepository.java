package co.assip.erp.cartera.cierremensual;

import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CierreMensualRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR CIERRES
    // =========================================================

    public List<CierreMensualDTO> listar() {

        String sql = """
                SELECT
                    c.id_cierre_cartera,
                    c.fecha_corte,
                    c.estado_cierre,

                    c.saldo_cartera_maestro,
                    c.saldo_cartera_contable,
                    c.diferencia_cuadre,

                    c.cantidad_creditos,

                    c.fecha_inicio,
                    c.fecha_cuadre,
                    c.fecha_fotografia,
                    c.fecha_finalizacion,

                    c.observaciones,

                    c.fk_seguridad_creacion,
                    c.fecha_creacion,
                    c.fk_seguridad_edicion,
                    c.fecha_edicion

                FROM cartera.cierres_cartera c

                ORDER BY
                    c.fecha_corte DESC,
                    c.id_cierre_cartera DESC
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                this::mapear
        );
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<CierreMensualDTO> buscarPorId(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT
                    c.id_cierre_cartera,
                    c.fecha_corte,
                    c.estado_cierre,

                    c.saldo_cartera_maestro,
                    c.saldo_cartera_contable,
                    c.diferencia_cuadre,

                    c.cantidad_creditos,

                    c.fecha_inicio,
                    c.fecha_cuadre,
                    c.fecha_fotografia,
                    c.fecha_finalizacion,

                    c.observaciones,

                    c.fk_seguridad_creacion,
                    c.fecha_creacion,
                    c.fk_seguridad_edicion,
                    c.fecha_edicion

                FROM cartera.cierres_cartera c

                WHERE c.id_cierre_cartera =
                      :idCierreCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        List<CierreMensualDTO> lista =
                jdbc.query(
                        sql,
                        parametros,
                        this::mapear
                );

        return lista.stream().findFirst();
    }

    // =========================================================
    // BUSCAR CIERRE GENERAL POR FECHA
    // =========================================================

    public Optional<CierreMensualDTO> buscarPorFecha(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT
                    c.id_cierre_cartera,
                    c.fecha_corte,
                    c.estado_cierre,

                    c.saldo_cartera_maestro,
                    c.saldo_cartera_contable,
                    c.diferencia_cuadre,

                    c.cantidad_creditos,

                    c.fecha_inicio,
                    c.fecha_cuadre,
                    c.fecha_fotografia,
                    c.fecha_finalizacion,

                    c.observaciones,

                    c.fk_seguridad_creacion,
                    c.fecha_creacion,
                    c.fk_seguridad_edicion,
                    c.fecha_edicion

                FROM cartera.cierres_cartera c

                WHERE c.fecha_corte =
                      :fechaCorte

                LIMIT 1
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        List<CierreMensualDTO> lista =
                jdbc.query(
                        sql,
                        parametros,
                        this::mapear
                );

        return lista.stream().findFirst();
    }

    // =========================================================
    // CREAR CABECERA DEL CIERRE GENERAL
    // =========================================================

    public Integer crear(
            LocalDate fechaCorte,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.cierres_cartera
                (
                    fecha_corte,
                    estado_cierre,

                    saldo_cartera_maestro,
                    saldo_cartera_contable,

                    cantidad_creditos,

                    fecha_inicio,

                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :fechaCorte,
                    'P',

                    0,
                    0,

                    0,

                    CURRENT_TIMESTAMP,

                    :idUsuario,
                    :idUsuario
                )

                RETURNING id_cierre_cartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.queryForObject(
                sql,
                parametros,
                Integer.class
        );
    }

    // =========================================================
    // ACTUALIZAR RESULTADO DE LA FOTO
    // =========================================================

    public int actualizarFoto(
            Integer idCierreCartera,
            Integer cantidadCreditos,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.cierres_cartera

                   SET cantidad_creditos =
                           :cantidadCreditos,

                       fecha_fotografia =
                           CURRENT_TIMESTAMP,

                       fk_seguridad_edicion =
                           :idUsuario,

                       fecha_edicion =
                           CURRENT_TIMESTAMP

                 WHERE id_cierre_cartera =
                       :idCierreCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "cantidadCreditos",
                                cantidadCreditos
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // ACTUALIZAR CUADRE GENERAL
    // =========================================================

    public int actualizarCuadre(
            Integer idCierreCartera,
            java.math.BigDecimal saldoCarteraMaestro,
            java.math.BigDecimal saldoCarteraContable,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.cierres_cartera

                   SET saldo_cartera_maestro =
                           :saldoCarteraMaestro,

                       saldo_cartera_contable =
                           :saldoCarteraContable,

                       fecha_cuadre =
                           CURRENT_TIMESTAMP,

                       fk_seguridad_edicion =
                           :idUsuario,

                       fecha_edicion =
                           CURRENT_TIMESTAMP

                 WHERE id_cierre_cartera =
                       :idCierreCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "saldoCarteraMaestro",
                                saldoCarteraMaestro
                        )
                        .addValue(
                                "saldoCarteraContable",
                                saldoCarteraContable
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // MARCAR CIERRE COMO FINALIZADO
    // =========================================================

    public int finalizar(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.cierres_cartera

                   SET estado_cierre = 'C',

                       fecha_finalizacion =
                           CURRENT_TIMESTAMP,

                       fk_seguridad_edicion =
                           :idUsuario,

                       fecha_edicion =
                           CURRENT_TIMESTAMP

                 WHERE id_cierre_cartera =
                       :idCierreCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // MAPPER
    // =========================================================

    private CierreMensualDTO mapear(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        CierreMensualDTO dto =
                new CierreMensualDTO();

        dto.setIdCierreCartera(
                rs.getInt(
                        "id_cierre_cartera"
                )
        );

        dto.setFechaCorte(
                rs.getObject(
                        "fecha_corte",
                        LocalDate.class
                )
        );

        dto.setEstadoCierre(
                rs.getString(
                        "estado_cierre"
                )
        );

        dto.setSaldoCarteraMaestro(
                rs.getBigDecimal(
                        "saldo_cartera_maestro"
                )
        );

        dto.setSaldoCarteraContable(
                rs.getBigDecimal(
                        "saldo_cartera_contable"
                )
        );

        dto.setDiferenciaCuadre(
                rs.getBigDecimal(
                        "diferencia_cuadre"
                )
        );

        dto.setCantidadCreditos(
                rs.getInt(
                        "cantidad_creditos"
                )
        );

        if (rs.getTimestamp("fecha_inicio") != null) {
            dto.setFechaInicio(
                    rs.getTimestamp(
                            "fecha_inicio"
                    ).toLocalDateTime()
            );
        }

        if (rs.getTimestamp("fecha_cuadre") != null) {
            dto.setFechaCuadre(
                    rs.getTimestamp(
                            "fecha_cuadre"
                    ).toLocalDateTime()
            );
        }

        if (rs.getTimestamp("fecha_fotografia") != null) {
            dto.setFechaFotografia(
                    rs.getTimestamp(
                            "fecha_fotografia"
                    ).toLocalDateTime()
            );
        }

        if (rs.getTimestamp("fecha_finalizacion") != null) {
            dto.setFechaFinal(
                    rs.getTimestamp(
                            "fecha_finalizacion"
                    ).toLocalDateTime()
            );
        }

        dto.setObservaciones(
                rs.getString(
                        "observaciones"
                )
        );

        dto.setFkSeguridadCreacion(
                rs.getInt(
                        "fk_seguridad_creacion"
                )
        );

        if (rs.getTimestamp("fecha_creacion") != null) {
            dto.setFechaCreacion(
                    rs.getTimestamp(
                            "fecha_creacion"
                    ).toLocalDateTime()
            );
        }

        dto.setFkSeguridadEdicion(
                rs.getInt(
                        "fk_seguridad_edicion"
                )
        );

        if (rs.getTimestamp("fecha_edicion") != null) {
            dto.setFechaEdicion(
                    rs.getTimestamp(
                            "fecha_edicion"
                    ).toLocalDateTime()
            );
        }

        return dto;
    }

    // =========================================================
// ACTUALIZAR SALDO MAESTRO DE CARTERA
// =========================================================

    public int actualizarSaldoCarteraMaestro(
            Integer idCierreCartera,
            java.math.BigDecimal saldoCarteraMaestro,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.cierres_cartera

               SET saldo_cartera_maestro =
                       :saldoCarteraMaestro,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

             WHERE id_cierre_cartera =
                   :idCierreCartera
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "saldoCarteraMaestro",
                                saldoCarteraMaestro
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }
}