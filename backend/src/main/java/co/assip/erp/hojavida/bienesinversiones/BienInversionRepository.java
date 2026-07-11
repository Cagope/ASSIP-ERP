package co.assip.erp.hojavida.bienesinversiones;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BienInversionRepository {

    private final JdbcTemplate jdbc;

    public BienInversionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<BienInversion> mapper = (rs, rowNum) -> {
        BienInversion b = new BienInversion();

        b.setIdBienPersona(rs.getLong("id_bien_persona"));
        b.setIdDatosPersonal(rs.getLong("id_datos_personal"));
        b.setPorcentajePropiedad(rs.getBigDecimal("porcentaje_propiedad"));

        b.setIdBien(rs.getLong("id_bien"));
        b.setIdTipoBien(rs.getLong("id_tipo_bien"));
        b.setCodigoTipoBien(rs.getString("codigo_tipo_bien"));
        b.setNombreTipoBien(rs.getString("nombre_tipo_bien"));
        b.setDescripcionGeneral(rs.getString("descripcion_general"));
        b.setValorComercial(rs.getBigDecimal("valor_comercial"));
        b.setValorGravamen(rs.getBigDecimal("valor_gravamen"));

        b.setIdBienInversion(rs.getLong("id_bien_inversion"));
        b.setIdTipoInversion(rs.getLong("id_tipo_inversion"));
        b.setNombreTipoInversion(rs.getString("nombre_tipo_inversion"));

        b.setEntidad(rs.getString("entidad"));
        b.setNumeroTitulo(rs.getString("numero_titulo"));

        if (rs.getDate("fecha_inversion") != null) {
            b.setFechaInversion(
                    rs.getDate("fecha_inversion").toLocalDate()
            );
        }

        if (rs.getDate("fecha_vencimiento") != null) {
            b.setFechaVencimiento(
                    rs.getDate("fecha_vencimiento").toLocalDate()
            );
        }

        b.setValorNominal(rs.getBigDecimal("valor_nominal"));
        b.setValorActual(rs.getBigDecimal("valor_actual"));
        b.setTasaRendimiento(rs.getBigDecimal("tasa_rendimiento"));

        b.setIdTipoGravamen(rs.getLong("id_tipo_gravamen"));
        b.setNombreGravamen(rs.getString("nombre_gravamen"));

        b.setObservaciones(rs.getString("observaciones"));

        b.setTipoDocumento(rs.getString("tipo_documento"));
        b.setNombreTipoDocumento(rs.getString("nombre_tipo_documento"));
        b.setDocumento(rs.getString("documento"));
        b.setNombreCompleto(rs.getString("nombre_completo"));

        b.setValorNetoBien(rs.getBigDecimal("valor_neto_bien"));
        b.setValorPropiedadAsociado(
                rs.getBigDecimal("valor_propiedad_asociado")
        );
        b.setValorGravamenAsociado(
                rs.getBigDecimal("valor_gravamen_asociado")
        );
        b.setValorNetoAsociado(
                rs.getBigDecimal("valor_neto_asociado")
        );

        b.setFkSeguridadCreacion(
                rs.getInt("fk_seguridad_creacion")
        );

        if (rs.getTimestamp("fecha_creacion") != null) {
            b.setFechaCreacion(
                    rs.getTimestamp("fecha_creacion")
                            .toLocalDateTime()
            );
        }

        int fkEdicion =
                rs.getInt("fk_seguridad_edicion");

        b.setFkSeguridadEdicion(
                rs.wasNull()
                        ? null
                        : fkEdicion
        );

        if (rs.getTimestamp("fecha_edicion") != null) {
            b.setFechaEdicion(
                    rs.getTimestamp("fecha_edicion")
                            .toLocalDateTime()
            );
        }

        return b;
    };

    // ============================================================
    // CONSULTAS
    // ============================================================

    public List<BienInversion> listarPorPersona(
            Long idDatosPersonal
    ) {
        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_inversiones_total
                WHERE id_datos_personal = ?
                ORDER BY fecha_creacion DESC, id_bien DESC
                """;

        return jdbc.query(
                sql,
                mapper,
                idDatosPersonal
        );
    }

    public Optional<BienInversion> buscarPorIdBien(
            Long idBien
    ) {
        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_inversiones_total
                WHERE id_bien = ?
                LIMIT 1
                """;

        List<BienInversion> lista =
                jdbc.query(
                        sql,
                        mapper,
                        idBien
                );

        return lista.stream().findFirst();
    }

    // ============================================================
    // BIEN GENERAL
    // ============================================================

    public Long insertarBien(
            BienInversion dto,
            Integer idUsuario
    ) {
        String sql = """
                INSERT INTO hoja_vida.bienes (
                    id_tipo_bien,
                    descripcion_general,
                    valor_comercial,
                    valor_gravamen,
                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP
                )
                RETURNING id_bien
                """;

        return jdbc.queryForObject(
                sql,
                Long.class,
                dto.getIdTipoBien(),
                dto.getDescripcionGeneral(),
                dto.getValorComercial(),
                dto.getValorGravamen(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarBien(
            Long idBien,
            BienInversion dto,
            Integer idUsuario
    ) {
        String sql = """
                UPDATE hoja_vida.bienes
                SET id_tipo_bien = ?,
                    descripcion_general = ?,
                    valor_comercial = ?,
                    valor_gravamen = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_bien = ?
                """;

        jdbc.update(
                sql,
                dto.getIdTipoBien(),
                dto.getDescripcionGeneral(),
                dto.getValorComercial(),
                dto.getValorGravamen(),
                idUsuario,
                idBien
        );
    }

    // ============================================================
    // RELACIÓN BIEN - PERSONA
    // ============================================================

    public Long insertarBienPersona(
            BienInversion dto,
            Long idBien,
            Integer idUsuario
    ) {
        String sql = """
                INSERT INTO hoja_vida.bienes_personas (
                    id_bien,
                    id_datos_personal,
                    porcentaje_propiedad,
                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES (
                    ?, ?, ?, ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP
                )
                RETURNING id_bien_persona
                """;

        return jdbc.queryForObject(
                sql,
                Long.class,
                idBien,
                dto.getIdDatosPersonal(),
                dto.getPorcentajePropiedad(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarBienPersona(
            Long idBienPersona,
            BienInversion dto,
            Integer idUsuario
    ) {
        String sql = """
                UPDATE hoja_vida.bienes_personas
                SET id_datos_personal = ?,
                    porcentaje_propiedad = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_bien_persona = ?
                """;

        jdbc.update(
                sql,
                dto.getIdDatosPersonal(),
                dto.getPorcentajePropiedad(),
                idUsuario,
                idBienPersona
        );
    }

    // ============================================================
    // DETALLE INVERSIÓN
    // ============================================================

    public Long insertarInversion(
            BienInversion dto,
            Long idBien,
            Integer idUsuario
    ) {
        String sql = """
                INSERT INTO hoja_vida.bienes_inversiones (
                    id_bien,
                    id_tipo_inversion,
                    entidad,
                    numero_titulo,
                    fecha_inversion,
                    fecha_vencimiento,
                    valor_nominal,
                    valor_actual,
                    tasa_rendimiento,
                    id_tipo_gravamen,
                    observaciones,
                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP
                )
                RETURNING id_bien_inversion
                """;

        return jdbc.queryForObject(
                sql,
                Long.class,
                idBien,
                dto.getIdTipoInversion(),
                dto.getEntidad(),
                dto.getNumeroTitulo(),
                dto.getFechaInversion(),
                dto.getFechaVencimiento(),
                dto.getValorNominal(),
                dto.getValorActual(),
                dto.getTasaRendimiento(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarInversion(
            Long idBienInversion,
            BienInversion dto,
            Integer idUsuario
    ) {
        String sql = """
                UPDATE hoja_vida.bienes_inversiones
                SET id_tipo_inversion = ?,
                    entidad = ?,
                    numero_titulo = ?,
                    fecha_inversion = ?,
                    fecha_vencimiento = ?,
                    valor_nominal = ?,
                    valor_actual = ?,
                    tasa_rendimiento = ?,
                    id_tipo_gravamen = ?,
                    observaciones = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_bien_inversion = ?
                """;

        jdbc.update(
                sql,
                dto.getIdTipoInversion(),
                dto.getEntidad(),
                dto.getNumeroTitulo(),
                dto.getFechaInversion(),
                dto.getFechaVencimiento(),
                dto.getValorNominal(),
                dto.getValorActual(),
                dto.getTasaRendimiento(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idBienInversion
        );
    }

    // ============================================================
    // EXISTENCIA / ELIMINACIÓN
    // ============================================================

    public boolean existeBien(Long idBien) {
        Integer total = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM hoja_vida.bienes
                WHERE id_bien = ?
                """,
                Integer.class,
                idBien
        );

        return total != null && total > 0;
    }

    public void eliminarPorBien(Long idBien) {
        jdbc.update(
                """
                DELETE FROM hoja_vida.bienes_inversiones
                WHERE id_bien = ?
                """,
                idBien
        );

        jdbc.update(
                """
                DELETE FROM hoja_vida.bienes_personas
                WHERE id_bien = ?
                """,
                idBien
        );

        jdbc.update(
                """
                DELETE FROM hoja_vida.bienes
                WHERE id_bien = ?
                """,
                idBien
        );
    }
}