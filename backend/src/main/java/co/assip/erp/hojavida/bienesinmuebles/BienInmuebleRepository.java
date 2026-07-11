package co.assip.erp.hojavida.bienesinmuebles;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class BienInmuebleRepository {

    private final JdbcTemplate jdbc;

    public BienInmuebleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<BienInmueble> mapper = (rs, rowNum) -> {
        BienInmueble b = new BienInmueble();

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

        b.setIdBienInmueble(rs.getLong("id_bien_inmueble"));
        b.setIdTipoInmueble(rs.getLong("id_tipo_inmueble"));
        b.setCodigoTipoInmueble(rs.getString("codigo_tipo_inmueble"));
        b.setNombreTipoInmueble(rs.getString("nombre_tipo_inmueble"));

        b.setNumeroMatriculaInmobiliaria(rs.getString("numero_matricula_inmobiliaria"));
        b.setCedulaCatastral(rs.getString("cedula_catastral"));

        b.setIdPais(rs.getLong("id_pais"));
        b.setNombrePais(rs.getString("nombre_pais"));
        b.setIdDepartamento(rs.getLong("id_departamento"));
        b.setNombreDepartamento(rs.getString("nombre_departamento"));
        b.setIdCiudad(rs.getLong("id_ciudad"));
        b.setNombreCiudad(rs.getString("nombre_ciudad"));

        b.setDireccion(rs.getString("direccion"));
        b.setBarrioVereda(rs.getString("barrio_vereda"));
        b.setAreaTerreno(rs.getBigDecimal("area_terreno"));
        b.setAreaConstruida(rs.getBigDecimal("area_construida"));

        b.setNumeroEscritura(rs.getString("numero_escritura"));
        if (rs.getDate("fecha_escritura") != null) {
            b.setFechaEscritura(rs.getDate("fecha_escritura").toLocalDate());
        }

        b.setNotaria(rs.getString("notaria"));

        b.setIdPaisNotaria(getNullableLong(rs, "id_pais_notaria"));
        b.setNombrePaisNotaria(rs.getString("nombre_pais_notaria"));
        b.setIdDepartamentoNotaria(getNullableLong(rs, "id_departamento_notaria"));
        b.setNombreDepartamentoNotaria(rs.getString("nombre_departamento_notaria"));
        b.setIdCiudadNotaria(getNullableLong(rs, "id_ciudad_notaria"));
        b.setNombreCiudadNotaria(rs.getString("nombre_ciudad_notaria"));

        b.setIdTipoGravamen(rs.getLong("id_tipo_gravamen"));
        b.setNombreGravamen(rs.getString("nombre_gravamen"));
        b.setObservaciones(rs.getString("observaciones"));

        b.setTipoDocumento(rs.getString("tipo_documento"));
        b.setNombreTipoDocumento(rs.getString("nombre_tipo_documento"));
        b.setDocumento(rs.getString("documento"));
        b.setNombreCompleto(rs.getString("nombre_completo"));

        b.setIdBienInmuebleAvaluo(getNullableLong(rs, "id_bien_inmueble_avaluo"));
        if (rs.getDate("fecha_avaluo") != null) {
            b.setFechaAvaluo(rs.getDate("fecha_avaluo").toLocalDate());
        }
        b.setValorAvaluoComercial(rs.getBigDecimal("valor_avaluo_comercial"));
        b.setValorAvaluoCatastral(rs.getBigDecimal("valor_avaluo_catastral"));
        b.setEntidadAvaluadora(rs.getString("entidad_avaluadora"));
        b.setNumeroInforme(rs.getString("numero_informe"));
        b.setObservacionesAvaluo(rs.getString("observaciones_avaluo"));

        b.setIdBienInmuebleSeguro(getNullableLong(rs, "id_bien_inmueble_seguro"));
        b.setAseguradora(rs.getString("aseguradora"));
        b.setNumeroPoliza(rs.getString("numero_poliza"));
        b.setValorAsegurado(rs.getBigDecimal("valor_asegurado"));
        if (rs.getDate("fecha_inicio_seguro") != null) {
            b.setFechaInicioSeguro(rs.getDate("fecha_inicio_seguro").toLocalDate());
        }
        if (rs.getDate("fecha_vencimiento_seguro") != null) {
            b.setFechaVencimientoSeguro(rs.getDate("fecha_vencimiento_seguro").toLocalDate());
        }
        b.setEstadoSeguro(rs.getString("estado_seguro"));
        b.setNombreEstadoSeguro(rs.getString("nombre_estado_seguro"));
        b.setObservacionesSeguro(rs.getString("observaciones_seguro"));

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

    public List<BienInmueble> listarPorPersona(Long idDatosPersonal) {
        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_inmuebles_total
                WHERE id_datos_personal = ?
                ORDER BY fecha_creacion DESC, id_bien DESC
                """;

        return jdbc.query(sql, mapper, idDatosPersonal);
    }

    public Optional<BienInmueble> buscarPorIdBien(Long idBien) {
        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_inmuebles_total
                WHERE id_bien = ?
                LIMIT 1
                """;

        List<BienInmueble> lista = jdbc.query(sql, mapper, idBien);
        return lista.stream().findFirst();
    }

    public Long insertarBien(BienInmueble dto, Integer idUsuario) {
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

    public void actualizarBien(Long idBien, BienInmueble dto, Integer idUsuario) {
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

    public Long insertarBienPersona(BienInmueble dto, Long idBien, Integer idUsuario) {
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

    public void actualizarBienPersona(Long idBienPersona, BienInmueble dto, Integer idUsuario) {
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

    public Long insertarInmueble(BienInmueble dto, Long idBien, Integer idUsuario) {
        String sql = """
                INSERT INTO hoja_vida.bienes_inmuebles (
                    id_bien,
                    id_tipo_inmueble,
                    numero_matricula_inmobiliaria,
                    cedula_catastral,
                    id_pais,
                    id_departamento,
                    id_ciudad,
                    direccion,
                    barrio_vereda,
                    area_terreno,
                    area_construida,
                    numero_escritura,
                    fecha_escritura,
                    notaria,
                    id_pais_notaria,
                    id_departamento_notaria,
                    id_ciudad_notaria,
                    id_tipo_gravamen,
                    observaciones,
                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP)
                RETURNING id_bien_inmueble
                """;

        return jdbc.queryForObject(sql, Long.class,
                idBien,
                dto.getIdTipoInmueble(),
                dto.getNumeroMatriculaInmobiliaria(),
                dto.getCedulaCatastral(),
                dto.getIdPais(),
                dto.getIdDepartamento(),
                dto.getIdCiudad(),
                dto.getDireccion(),
                dto.getBarrioVereda(),
                dto.getAreaTerreno(),
                dto.getAreaConstruida(),
                dto.getNumeroEscritura(),
                dto.getFechaEscritura() == null ? null : Date.valueOf(dto.getFechaEscritura()),
                dto.getNotaria(),
                dto.getIdPaisNotaria(),
                dto.getIdDepartamentoNotaria(),
                dto.getIdCiudadNotaria(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarInmueble(Long idBienInmueble, BienInmueble dto, Integer idUsuario) {
        String sql = """
                UPDATE hoja_vida.bienes_inmuebles
                SET id_tipo_inmueble = ?,
                    numero_matricula_inmobiliaria = ?,
                    cedula_catastral = ?,
                    id_pais = ?,
                    id_departamento = ?,
                    id_ciudad = ?,
                    direccion = ?,
                    barrio_vereda = ?,
                    area_terreno = ?,
                    area_construida = ?,
                    numero_escritura = ?,
                    fecha_escritura = ?,
                    notaria = ?,
                    id_pais_notaria = ?,
                    id_departamento_notaria = ?,
                    id_ciudad_notaria = ?,
                    id_tipo_gravamen = ?,
                    observaciones = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_bien_inmueble = ?
                """;

        jdbc.update(sql,
                dto.getIdTipoInmueble(),
                dto.getNumeroMatriculaInmobiliaria(),
                dto.getCedulaCatastral(),
                dto.getIdPais(),
                dto.getIdDepartamento(),
                dto.getIdCiudad(),
                dto.getDireccion(),
                dto.getBarrioVereda(),
                dto.getAreaTerreno(),
                dto.getAreaConstruida(),
                dto.getNumeroEscritura(),
                dto.getFechaEscritura() == null ? null : Date.valueOf(dto.getFechaEscritura()),
                dto.getNotaria(),
                dto.getIdPaisNotaria(),
                dto.getIdDepartamentoNotaria(),
                dto.getIdCiudadNotaria(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idBienInmueble
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
        jdbc.update("DELETE FROM hoja_vida.bienes_inmuebles WHERE id_bien = ?", idBien);
        jdbc.update("DELETE FROM hoja_vida.bienes_personas WHERE id_bien = ?", idBien);
        jdbc.update("DELETE FROM hoja_vida.bienes WHERE id_bien = ?", idBien);
    }

    private static Long getNullableLong(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}