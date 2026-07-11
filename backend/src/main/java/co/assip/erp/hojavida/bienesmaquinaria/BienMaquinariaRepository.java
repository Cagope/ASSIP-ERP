package co.assip.erp.hojavida.bienesmaquinaria;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BienMaquinariaRepository {

    private final JdbcTemplate jdbc;

    public BienMaquinariaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<BienMaquinaria> mapper = (rs, rowNum) -> {
        BienMaquinaria b = new BienMaquinaria();

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

        b.setIdBienMaquinaria(rs.getLong("id_bien_maquinaria"));
        b.setIdTipoMaquinaria(rs.getLong("id_tipo_maquinaria"));
        b.setNombreTipoMaquinaria(rs.getString("nombre_tipo_maquinaria"));

        b.setMarca(rs.getString("marca"));
        b.setModelo(rs.getString("modelo"));
        b.setSerial(rs.getString("serial"));
        b.setReferencia(rs.getString("referencia"));
        b.setDescripcionTecnica(rs.getString("descripcion_tecnica"));
        b.setUbicacion(rs.getString("ubicacion"));
        b.setEstadoOperativo(rs.getString("estado_operativo"));

        if (rs.getDate("fecha_adquisicion") != null) {
            b.setFechaAdquisicion(rs.getDate("fecha_adquisicion").toLocalDate());
        }

        b.setIdTipoGravamen(rs.getLong("id_tipo_gravamen"));
        b.setNombreGravamen(rs.getString("nombre_gravamen"));

        b.setObservaciones(rs.getString("observaciones"));

        b.setTipoDocumento(rs.getString("tipo_documento"));
        b.setNombreTipoDocumento(rs.getString("nombre_tipo_documento"));
        b.setDocumento(rs.getString("documento"));
        b.setNombreCompleto(rs.getString("nombre_completo"));

        b.setValorNetoBien(rs.getBigDecimal("valor_neto_bien"));
        b.setValorPropiedadAsociado(rs.getBigDecimal("valor_propiedad_asociado"));
        b.setValorGravamenAsociado(rs.getBigDecimal("valor_gravamen_asociado"));
        b.setValorNetoAsociado(rs.getBigDecimal("valor_neto_asociado"));

        b.setFkSeguridadCreacion(rs.getInt("fk_seguridad_creacion"));

        if (rs.getTimestamp("fecha_creacion") != null) {
            b.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        }

        int fkEdicion = rs.getInt("fk_seguridad_edicion");
        b.setFkSeguridadEdicion(rs.wasNull() ? null : fkEdicion);

        if (rs.getTimestamp("fecha_edicion") != null) {
            b.setFechaEdicion(rs.getTimestamp("fecha_edicion").toLocalDateTime());
        }

        return b;
    };

    public List<BienMaquinaria> listarPorPersona(Long idDatosPersonal) {
        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_maquinaria_total
                WHERE id_datos_personal = ?
                ORDER BY fecha_creacion DESC, id_bien DESC
                """;

        return jdbc.query(sql, mapper, idDatosPersonal);
    }

    public Optional<BienMaquinaria> buscarPorIdBien(Long idBien) {
        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_maquinaria_total
                WHERE id_bien = ?
                LIMIT 1
                """;

        List<BienMaquinaria> lista = jdbc.query(sql, mapper, idBien);
        return lista.stream().findFirst();
    }

    public Long insertarBien(BienMaquinaria dto, Integer idUsuario) {
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
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP)
                RETURNING id_bien
                """;

        return jdbc.queryForObject(sql, Long.class,
                dto.getIdTipoBien(),
                dto.getDescripcionGeneral(),
                dto.getValorComercial(),
                dto.getValorGravamen(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarBien(Long idBien, BienMaquinaria dto, Integer idUsuario) {
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

        jdbc.update(sql,
                dto.getIdTipoBien(),
                dto.getDescripcionGeneral(),
                dto.getValorComercial(),
                dto.getValorGravamen(),
                idUsuario,
                idBien
        );
    }

    public Long insertarBienPersona(BienMaquinaria dto, Long idBien, Integer idUsuario) {
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
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP)
                RETURNING id_bien_persona
                """;

        return jdbc.queryForObject(sql, Long.class,
                idBien,
                dto.getIdDatosPersonal(),
                dto.getPorcentajePropiedad(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarBienPersona(Long idBienPersona, BienMaquinaria dto, Integer idUsuario) {
        String sql = """
                UPDATE hoja_vida.bienes_personas
                SET id_datos_personal = ?,
                    porcentaje_propiedad = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_bien_persona = ?
                """;

        jdbc.update(sql,
                dto.getIdDatosPersonal(),
                dto.getPorcentajePropiedad(),
                idUsuario,
                idBienPersona
        );
    }

    public Long insertarMaquinaria(BienMaquinaria dto, Long idBien, Integer idUsuario) {
        String sql = """
                INSERT INTO hoja_vida.bienes_maquinaria (
                    id_bien,
                    id_tipo_maquinaria,
                    marca,
                    modelo,
                    serial,
                    referencia,
                    descripcion_tecnica,
                    ubicacion,
                    estado_operativo,
                    fecha_adquisicion,
                    id_tipo_gravamen,
                    observaciones,
                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP)
                RETURNING id_bien_maquinaria
                """;

        return jdbc.queryForObject(sql, Long.class,
                idBien,
                dto.getIdTipoMaquinaria(),
                dto.getMarca(),
                dto.getModelo(),
                dto.getSerial(),
                dto.getReferencia(),
                dto.getDescripcionTecnica(),
                dto.getUbicacion(),
                dto.getEstadoOperativo(),
                dto.getFechaAdquisicion(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarMaquinaria(Long idBienMaquinaria, BienMaquinaria dto, Integer idUsuario) {
        String sql = """
                UPDATE hoja_vida.bienes_maquinaria
                SET id_tipo_maquinaria = ?,
                    marca = ?,
                    modelo = ?,
                    serial = ?,
                    referencia = ?,
                    descripcion_tecnica = ?,
                    ubicacion = ?,
                    estado_operativo = ?,
                    fecha_adquisicion = ?,
                    id_tipo_gravamen = ?,
                    observaciones = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_bien_maquinaria = ?
                """;

        jdbc.update(sql,
                dto.getIdTipoMaquinaria(),
                dto.getMarca(),
                dto.getModelo(),
                dto.getSerial(),
                dto.getReferencia(),
                dto.getDescripcionTecnica(),
                dto.getUbicacion(),
                dto.getEstadoOperativo(),
                dto.getFechaAdquisicion(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idBienMaquinaria
        );
    }

    public boolean existeBien(Long idBien) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hoja_vida.bienes WHERE id_bien = ?",
                Integer.class,
                idBien
        );

        return total != null && total > 0;
    }

    public void eliminarPorBien(Long idBien) {
        jdbc.update("DELETE FROM hoja_vida.bienes_maquinaria WHERE id_bien = ?", idBien);
        jdbc.update("DELETE FROM hoja_vida.bienes_personas WHERE id_bien = ?", idBien);
        jdbc.update("DELETE FROM hoja_vida.bienes WHERE id_bien = ?", idBien);
    }
}