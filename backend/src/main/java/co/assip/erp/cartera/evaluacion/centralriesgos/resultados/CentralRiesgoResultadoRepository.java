package co.assip.erp.cartera.evaluacion.centralriesgos.resultados;

import co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto.CentralRiesgoResultadoDatoDTO;
import co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto.CentralRiesgoResultadoImportacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CentralRiesgoResultadoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // SQL BASE
    // =========================================================

    private static final String SELECT_ARCHIVO_BASE = """
            SELECT
                a.id_central_archivo
                    AS idCentralArchivo,

                a.id_central_riesgo
                    AS idCentralRiesgo,

                c.codigo_central
                    AS codigoCentral,

                c.nombre_central
                    AS nombreCentral,

                a.fecha_corte
                    AS fechaCorte,

                a.nombre_archivo
                    AS nombreArchivo,

                a.tamano_archivo
                    AS tamanoArchivo,

                a.extension_archivo
                    AS extensionArchivo,

                a.codificacion_archivo
                    AS codificacionArchivo,

                a.separador_archivo
                    AS separadorArchivo,

                a.fecha_importacion
                    AS fechaImportacion,

                a.cantidad_registros_leidos
                    AS cantidadRegistrosLeidos,

                a.cantidad_registros_importados
                    AS cantidadRegistrosImportados,

                a.cantidad_registros_rechazados
                    AS cantidadRegistrosRechazados,

                a.observaciones,

                a.fk_seguridad_creacion
                    AS fkSeguridadCreacion,

                a.fecha_creacion
                    AS fechaCreacion,

                a.fk_seguridad_edicion
                    AS fkSeguridadEdicion,

                a.fecha_edicion
                    AS fechaEdicion

            FROM cartera.evaluaciones_cartera_central_archivos a

            INNER JOIN cartera.centrales_riesgo c
                ON c.id_central_riesgo =
                   a.id_central_riesgo
            """;

    // =========================================================
    // VALIDAR CENTRAL
    // =========================================================

    public boolean existeCentralActiva(
            Integer idCentralRiesgo
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1
                    FROM cartera.centrales_riesgo
                    WHERE id_central_riesgo =
                          :idCentralRiesgo
                      AND activo = TRUE
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCentralRiesgo",
                                idCentralRiesgo
                        );

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Boolean.class
                )
        );
    }

    // =========================================================
    // BUSCAR ARCHIVO POR FECHA Y CENTRAL
    // =========================================================

    public Optional<Integer> buscarIdArchivo(
            Integer idCentralRiesgo,
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT id_central_archivo
                FROM cartera.evaluaciones_cartera_central_archivos
                WHERE id_central_riesgo = :idCentralRiesgo
                  AND fecha_corte = :fechaCorte
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCentralRiesgo",
                                idCentralRiesgo
                        )
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        List<Integer> resultado =
                jdbc.query(
                        sql,
                        parametros,
                        (rs, rowNum) ->
                                rs.getInt(
                                        "id_central_archivo"
                                )
                );

        return resultado.stream().findFirst();
    }

    // =========================================================
    // ELIMINAR CARGA ANTERIOR
    // Los datos se eliminan por ON DELETE CASCADE
    // =========================================================

    public int eliminarArchivo(
            Integer idCentralArchivo
    ) {

        String sql = """
                DELETE FROM cartera.evaluaciones_cartera_central_archivos
                WHERE id_central_archivo =
                      :idCentralArchivo
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCentralArchivo",
                                idCentralArchivo
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CREAR CABECERA
    // =========================================================

    public Integer crearArchivo(
            Integer idCentralRiesgo,
            LocalDate fechaCorte,
            String nombreArchivo,
            long tamanoArchivo,
            int cantidadRegistrosLeidos,
            int cantidadRegistrosImportados,
            int cantidadRegistrosRechazados,
            String observaciones,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.evaluaciones_cartera_central_archivos
                (
                    id_central_riesgo,
                    fecha_corte,
                    nombre_archivo,
                    tamano_archivo,
                    extension_archivo,
                    codificacion_archivo,
                    separador_archivo,
                    fecha_importacion,
                    cantidad_registros_leidos,
                    cantidad_registros_importados,
                    cantidad_registros_rechazados,
                    observaciones,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :idCentralRiesgo,
                    :fechaCorte,
                    :nombreArchivo,
                    :tamanoArchivo,
                    'CSV',
                    'UTF-8',
                    ';',
                    CURRENT_TIMESTAMP,
                    :cantidadRegistrosLeidos,
                    :cantidadRegistrosImportados,
                    :cantidadRegistrosRechazados,
                    :observaciones,
                    :idUsuario,
                    :idUsuario
                )
                RETURNING id_central_archivo
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCentralRiesgo",
                                idCentralRiesgo
                        )
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        )
                        .addValue(
                                "nombreArchivo",
                                nombreArchivo
                        )
                        .addValue(
                                "tamanoArchivo",
                                tamanoArchivo
                        )
                        .addValue(
                                "cantidadRegistrosLeidos",
                                cantidadRegistrosLeidos
                        )
                        .addValue(
                                "cantidadRegistrosImportados",
                                cantidadRegistrosImportados
                        )
                        .addValue(
                                "cantidadRegistrosRechazados",
                                cantidadRegistrosRechazados
                        )
                        .addValue(
                                "observaciones",
                                observaciones
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
    // INSERTAR DATOS
    // =========================================================

    public void insertarDatos(
            Integer idCentralArchivo,
            List<CentralRiesgoResultadoDatoDTO> registros,
            Integer idUsuario
    ) {

        if (registros == null || registros.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO cartera.evaluaciones_cartera_central_datos
                (
                    id_central_archivo,
                    numero_fila,
                    documento,
                    clasificacion_cartera,
                    calificacion_guia_final,
                    alertas_totales,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :idCentralArchivo,
                    :numeroFila,
                    :documento,
                    :clasificacionCartera,
                    :calificacionGuiaFinal,
                    :alertasTotales,
                    :idUsuario,
                    :idUsuario
                )
                """;

        MapSqlParameterSource[] lote =
                registros.stream()
                        .map(
                                registro ->
                                        new MapSqlParameterSource()
                                                .addValue(
                                                        "idCentralArchivo",
                                                        idCentralArchivo
                                                )
                                                .addValue(
                                                        "numeroFila",
                                                        registro.getNumeroFila()
                                                )
                                                .addValue(
                                                        "documento",
                                                        registro.getDocumento()
                                                )
                                                .addValue(
                                                        "clasificacionCartera",
                                                        registro.getClasificacionCartera()
                                                )
                                                .addValue(
                                                        "calificacionGuiaFinal",
                                                        registro.getCalificacionGuiaFinal()
                                                )
                                                .addValue(
                                                        "alertasTotales",
                                                        registro.getAlertasTotales()
                                                )
                                                .addValue(
                                                        "idUsuario",
                                                        idUsuario
                                                )
                        )
                        .toArray(
                                MapSqlParameterSource[]::new
                        );

        jdbc.batchUpdate(
                sql,
                lote
        );
    }

    // =========================================================
    // BUSCAR CABECERA POR ID
    // =========================================================

    public Optional<CentralRiesgoResultadoImportacionDTO> buscarArchivoPorId(
            Integer idCentralArchivo
    ) {

        String sql = SELECT_ARCHIVO_BASE + """
                
                WHERE a.id_central_archivo =
                      :idCentralArchivo
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCentralArchivo",
                                idCentralArchivo
                        );

        List<CentralRiesgoResultadoImportacionDTO> resultado =
                jdbc.query(
                        sql,
                        parametros,
                        BeanPropertyRowMapper.newInstance(
                                CentralRiesgoResultadoImportacionDTO.class
                        )
                );

        return resultado.stream().findFirst();
    }

    // =========================================================
    // LISTAR DATOS DEL ARCHIVO
    // =========================================================

    public List<CentralRiesgoResultadoDatoDTO> listarDatos(
            Integer idCentralArchivo
    ) {

        String sql = """
                SELECT
                    id_central_dato
                        AS idCentralDato,

                    id_central_archivo
                        AS idCentralArchivo,

                    numero_fila
                        AS numeroFila,

                    documento,
                    
                    clasificacion_cartera
                        AS clasificacionCartera,

                    calificacion_guia_final
                        AS calificacionGuiaFinal,

                    alertas_totales
                        AS alertasTotales,

                    fk_seguridad_creacion
                        AS fkSeguridadCreacion,

                    fecha_creacion
                        AS fechaCreacion,

                    fk_seguridad_edicion
                        AS fkSeguridadEdicion,

                    fecha_edicion
                        AS fechaEdicion

                FROM cartera.evaluaciones_cartera_central_datos

                WHERE id_central_archivo =
                      :idCentralArchivo

                ORDER BY
                    numero_fila
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCentralArchivo",
                                idCentralArchivo
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        CentralRiesgoResultadoDatoDTO.class
                )
        );
    }
}