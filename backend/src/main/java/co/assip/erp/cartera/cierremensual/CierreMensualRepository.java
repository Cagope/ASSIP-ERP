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
    // SELECT BASE
    // =========================================================

    private static final String SELECT_BASE = """
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
            c.estado_fotografia,
            c.fecha_fotografia_firme,

            c.estado_calculos,
            c.fecha_calculos_inicio,
            c.fecha_calculos_firme,

            c.estado_anexo1,
            c.fecha_anexo1_inicio,
            c.fecha_anexo1_firme,

            c.estado_anexo2,
            c.fecha_anexo2_inicio,
            c.fecha_anexo2_firme,

            c.fecha_finalizacion,

            c.observaciones,

            c.fk_seguridad_creacion,
            c.fecha_creacion,
            c.fk_seguridad_edicion,
            c.fecha_edicion

        FROM cartera.cierres_cartera c
        """;

    // =========================================================
    // LISTAR CIERRES
    // =========================================================

    public List<CierreMensualDTO> listar() {

        String sql = SELECT_BASE + """
            
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

        String sql = SELECT_BASE + """
            
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

        String sql = SELECT_BASE + """
            
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

                estado_fotografia,
                estado_calculos,
                estado_anexo1,
                estado_anexo2,

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

                'P',
                'P',
                'P',
                'P',

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
    // ACTUALIZAR RESULTADO DE LA FOTOGRAFÍA
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

                   estado_fotografia =
                       'E',

                   fecha_fotografia_firme =
                       NULL,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

             WHERE id_cierre_cartera =
                   :idCierreCartera

               AND estado_fotografia <>
                   'C'
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
    // CERRAR FOTOGRAFÍA EN FIRME
    //
    // estado_fotografia:
    // P = Pendiente
    // E = En proceso
    // C = Cerrada / En firme
    //
    // Solamente permite E -> C.
    // =========================================================

    public int finalizar(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.cierres_cartera

               SET estado_fotografia =
                       'C',

                   fecha_fotografia_firme =
                       CURRENT_TIMESTAMP,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

             WHERE id_cierre_cartera =
                   :idCierreCartera

               AND estado_fotografia =
                   'E'
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

    // =========================================================
    // INICIAR ETAPA DE CÁLCULOS
    //
    // REQUISITOS:
    // - fotografía cerrada en firme
    // - cálculos todavía no cerrados en firme
    //
    // RESULTADO:
    // estado_calculos = E
    //
    // fecha_calculos_inicio:
    // - se registra solamente la primera vez
    // - una reejecución conserva la fecha original
    // =========================================================

    public int iniciarCalculos(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        UPDATE cartera.cierres_cartera

           SET estado_calculos =
                   'E',

               fecha_calculos_inicio =
                   COALESCE(
                       fecha_calculos_inicio,
                       CURRENT_TIMESTAMP
                   ),

               fk_seguridad_edicion =
                   :idUsuario,

               fecha_edicion =
                   CURRENT_TIMESTAMP

         WHERE id_cierre_cartera =
               :idCierreCartera

           AND estado_fotografia =
               'C'

           AND estado_calculos IN ('P', 'E')
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
    // CERRAR ETAPA DE CÁLCULOS EN FIRME
    //
    // REQUISITOS:
    // - fotografía cerrada en firme
    // - cálculos en proceso
    //
    // RESULTADO:
    // estado_calculos = C
    // fecha_calculos_firme = CURRENT_TIMESTAMP
    //
    // Solamente permite E -> C.
    // =========================================================

    public int cerrarCalculos(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.cierres_cartera

               SET estado_calculos =
                       'C',

                   fecha_calculos_firme =
                       CURRENT_TIMESTAMP,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

             WHERE id_cierre_cartera =
                   :idCierreCartera

               AND estado_fotografia =
                   'C'

               AND estado_calculos =
                   'E'
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
    // INICIAR ETAPA ANEXO 1
    //
    // REQUISITOS:
    // - fotografía cerrada en firme
    // - cálculos cerrados en firme
    // - Anexo 1 todavía no cerrado en firme
    //
    // RESULTADO:
    // estado_anexo1 = E
    //
    // fecha_anexo1_inicio:
    // - se registra solamente la primera vez
    // - una reejecución conserva la fecha original
    // =========================================================

    public int iniciarAnexo1(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        UPDATE cartera.cierres_cartera

           SET estado_anexo1 =
               'E',

               fecha_anexo1_inicio =
                   COALESCE(
                       fecha_anexo1_inicio,
                       CURRENT_TIMESTAMP
                   ),

               fk_seguridad_edicion =
                   :idUsuario,

               fecha_edicion =
                   CURRENT_TIMESTAMP

         WHERE id_cierre_cartera =
               :idCierreCartera

           AND estado_fotografia =
               'C'

           AND estado_calculos =
               'C'

           AND estado_anexo1 IN ('P', 'E')
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
    // CERRAR ETAPA ANEXO 1 EN FIRME
    //
    // REQUISITOS:
    // - cálculos cerrados en firme
    // - Anexo 1 en proceso
    //
    // RESULTADO:
    // estado_anexo1 = C
    // fecha_anexo1_firme = CURRENT_TIMESTAMP
    //
    // Solamente permite E -> C.
    // =========================================================

    public int cerrarAnexo1(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        UPDATE cartera.cierres_cartera

           SET estado_anexo1 =
               'C',

               fecha_anexo1_firme =
                   CURRENT_TIMESTAMP,

               fk_seguridad_edicion =
                   :idUsuario,

               fecha_edicion =
                   CURRENT_TIMESTAMP

         WHERE id_cierre_cartera =
               :idCierreCartera

           AND estado_fotografia =
               'C'

           AND estado_calculos =
               'C'

           AND estado_anexo1 =
               'E'
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
                rs.getInt("id_cierre_cartera")
        );

        dto.setFechaCorte(
                rs.getObject(
                        "fecha_corte",
                        LocalDate.class
                )
        );

        dto.setEstadoCierre(
                rs.getString("estado_cierre")
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

        dto.setFechaInicio(
                obtenerFecha(
                        rs,
                        "fecha_inicio"
                )
        );

        dto.setFechaCuadre(
                obtenerFecha(
                        rs,
                        "fecha_cuadre"
                )
        );

        dto.setFechaFotografia(
                obtenerFecha(
                        rs,
                        "fecha_fotografia"
                )
        );

        dto.setEstadoFotografia(
                rs.getString(
                        "estado_fotografia"
                )
        );

        dto.setFechaFotografiaFirme(
                obtenerFecha(
                        rs,
                        "fecha_fotografia_firme"
                )
        );

        dto.setEstadoCalculos(
                rs.getString(
                        "estado_calculos"
                )
        );

        dto.setFechaCalculosInicio(
                obtenerFecha(
                        rs,
                        "fecha_calculos_inicio"
                )
        );

        dto.setFechaCalculosFirme(
                obtenerFecha(
                        rs,
                        "fecha_calculos_firme"
                )
        );

        dto.setEstadoAnexo1(
                rs.getString(
                        "estado_anexo1"
                )
        );

        dto.setFechaAnexo1Inicio(
                obtenerFecha(
                        rs,
                        "fecha_anexo1_inicio"
                )
        );

        dto.setFechaAnexo1Firme(
                obtenerFecha(
                        rs,
                        "fecha_anexo1_firme"
                )
        );

        dto.setEstadoAnexo2(
                rs.getString(
                        "estado_anexo2"
                )
        );

        dto.setFechaAnexo2Inicio(
                obtenerFecha(
                        rs,
                        "fecha_anexo2_inicio"
                )
        );

        dto.setFechaAnexo2Firme(
                obtenerFecha(
                        rs,
                        "fecha_anexo2_firme"
                )
        );

        dto.setFechaFinalizacion(
                obtenerFecha(
                        rs,
                        "fecha_finalizacion"
                )
        );

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

        dto.setFechaCreacion(
                obtenerFecha(
                        rs,
                        "fecha_creacion"
                )
        );

        dto.setFkSeguridadEdicion(
                rs.getInt(
                        "fk_seguridad_edicion"
                )
        );

        dto.setFechaEdicion(
                obtenerFecha(
                        rs,
                        "fecha_edicion"
                )
        );

        return dto;
    }

    // =========================================================
    // UTILIDAD FECHAS
    // =========================================================

    private java.time.LocalDateTime obtenerFecha(
            ResultSet rs,
            String columna
    ) throws SQLException {

        java.sql.Timestamp timestamp =
                rs.getTimestamp(columna);

        return timestamp != null
                ? timestamp.toLocalDateTime()
                : null;
    }
}