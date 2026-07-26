package co.assip.erp.hojavida.bienesinmuebles;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class BienInmuebleRepository {

    private final JdbcTemplate jdbc;

    public BienInmuebleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // Mapeo de la vista:
    // reporting.vw_hoja_vida_bienes_inmuebles_total
    // =========================================================
    private final RowMapper<BienInmueble> mapper = (rs, rowNum) -> {

        BienInmueble b = new BienInmueble();

        // =====================================================
        // Relación persona ↔ bien
        // =====================================================
        b.setIdBienPersona(
                getNullableLong(rs, "id_bien_persona")
        );

        b.setIdDatosPersonal(
                getNullableLong(rs, "id_datos_personal")
        );

        b.setPorcentajePropiedad(
                rs.getBigDecimal("porcentaje_propiedad")
        );

        // =====================================================
        // Bien general: hoja_vida.bienes
        // =====================================================
        b.setIdBien(
                getNullableLong(rs, "id_bien")
        );

        b.setIdTipoBien(
                getNullableLong(rs, "id_tipo_bien")
        );

        b.setCodigoTipoBien(
                rs.getString("codigo_tipo_bien")
        );

        b.setNombreTipoBien(
                rs.getString("nombre_tipo_bien")
        );

        b.setDescripcionGeneral(
                rs.getString("descripcion_general")
        );

        b.setValorComercial(
                rs.getBigDecimal("valor_comercial")
        );

        b.setValorGravamen(
                rs.getBigDecimal("valor_gravamen")
        );

        b.setFechaAdquisicion(
                getNullableLocalDate(rs, "fecha_adquisicion")
        );

        b.setEstadoBien(
                rs.getString("estado_bien")
        );

        b.setNombreEstadoBien(
                rs.getString("nombre_estado_bien")
        );

        b.setFechaEstado(
                getNullableLocalDate(rs, "fecha_estado")
        );

        b.setObservacionesBien(
                rs.getString("observaciones_bien")
        );

        // =====================================================
        // Inmueble: hoja_vida.bienes_inmuebles
        // =====================================================
        b.setIdBienInmueble(
                getNullableLong(rs, "id_bien_inmueble")
        );

        b.setIdTipoInmueble(
                getNullableLong(rs, "id_tipo_inmueble")
        );

        b.setCodigoTipoInmueble(
                rs.getString("codigo_tipo_inmueble")
        );

        b.setNombreTipoInmueble(
                rs.getString("nombre_tipo_inmueble")
        );

        b.setIdTipoZonaInmueble(
                getNullableLong(rs, "id_tipo_zona_inmueble")
        );

        b.setCodigoTipoZonaInmueble(
                rs.getString("codigo_tipo_zona_inmueble")
        );

        b.setNombreTipoZonaInmueble(
                rs.getString("nombre_tipo_zona_inmueble")
        );

        b.setNumeroMatriculaInmobiliaria(
                rs.getString("numero_matricula_inmobiliaria")
        );

        b.setCedulaCatastral(
                rs.getString("cedula_catastral")
        );

        b.setIdPais(
                getNullableLong(rs, "id_pais")
        );

        b.setNombrePais(
                rs.getString("nombre_pais")
        );

        b.setIdDepartamento(
                getNullableLong(rs, "id_departamento")
        );

        b.setNombreDepartamento(
                rs.getString("nombre_departamento")
        );

        b.setIdCiudad(
                getNullableLong(rs, "id_ciudad")
        );

        b.setNombreCiudad(
                rs.getString("nombre_ciudad")
        );

        b.setDireccion(
                rs.getString("direccion")
        );

        b.setBarrioVereda(
                rs.getString("barrio_vereda")
        );

        b.setAreaTerreno(
                rs.getBigDecimal("area_terreno")
        );

        b.setAreaConstruida(
                rs.getBigDecimal("area_construida")
        );

        b.setNumeroEscritura(
                rs.getString("numero_escritura")
        );

        b.setFechaEscritura(
                getNullableLocalDate(rs, "fecha_escritura")
        );

        b.setNotaria(
                rs.getString("notaria")
        );

        b.setFechaRegistroEscritura(
                getNullableLocalDate(rs, "fecha_registro_escritura")
        );

        b.setOficinaRegistro(
                rs.getString("oficina_registro")
        );

        b.setIdPaisNotaria(
                getNullableLong(rs, "id_pais_notaria")
        );

        b.setNombrePaisNotaria(
                rs.getString("nombre_pais_notaria")
        );

        b.setIdDepartamentoNotaria(
                getNullableLong(rs, "id_departamento_notaria")
        );

        b.setNombreDepartamentoNotaria(
                rs.getString("nombre_departamento_notaria")
        );

        b.setIdCiudadNotaria(
                getNullableLong(rs, "id_ciudad_notaria")
        );

        b.setNombreCiudadNotaria(
                rs.getString("nombre_ciudad_notaria")
        );

        b.setIdTipoGravamen(
                getNullableLong(rs, "id_tipo_gravamen")
        );

        b.setNombreGravamen(
                rs.getString("nombre_gravamen")
        );

        /*
         * Observaciones específicas del inmueble.
         * No confundir con observaciones_bien.
         */
        b.setObservaciones(
                rs.getString("observaciones")
        );

        // =====================================================
        // Datos del asociado desde la vista
        // =====================================================
        b.setTipoDocumento(
                rs.getString("tipo_documento")
        );

        b.setNombreTipoDocumento(
                rs.getString("nombre_tipo_documento")
        );

        b.setDocumento(
                rs.getString("documento")
        );

        b.setNombreCompleto(
                rs.getString("nombre_completo")
        );

        // =====================================================
        // Último avalúo desde la vista
        // =====================================================
        b.setIdBienInmuebleAvaluo(
                getNullableLong(rs, "id_bien_inmueble_avaluo")
        );

        b.setFechaAvaluo(
                getNullableLocalDate(rs, "fecha_avaluo")
        );

        b.setValorAvaluoComercial(
                rs.getBigDecimal("valor_avaluo_comercial")
        );

        b.setValorAvaluoCatastral(
                rs.getBigDecimal("valor_avaluo_catastral")
        );

        b.setValorTerreno(
                rs.getBigDecimal("valor_terreno")
        );

        b.setValorConstruccion(
                rs.getBigDecimal("valor_construccion")
        );

        b.setValorCultivos(
                rs.getBigDecimal("valor_cultivos")
        );

        b.setValorOtros(
                rs.getBigDecimal("valor_otros")
        );

        b.setEntidadAvaluadora(
                rs.getString("entidad_avaluadora")
        );

        b.setNumeroInforme(
                rs.getString("numero_informe")
        );

        b.setObservacionesAvaluo(
                rs.getString("observaciones_avaluo")
        );

        b.setVigenciaAnios(
                getNullableInteger(rs, "vigencia_anios")
        );

        b.setFechaVencimientoAvaluo(
                getNullableLocalDate(rs, "fecha_vencimiento_avaluo")
        );

        b.setNombreEstadoAvaluo(
                rs.getString("nombre_estado_avaluo")
        );

        b.setValorComponentesAvaluo(
                rs.getBigDecimal("valor_componentes_avaluo")
        );

        b.setValorAvaluoPropiedadAsociado(
                rs.getBigDecimal("valor_avaluo_propiedad_asociado")
        );

        // =====================================================
        // Seguro vigente o más reciente desde la vista
        // =====================================================
        b.setIdBienInmuebleSeguro(
                getNullableLong(rs, "id_bien_inmueble_seguro")
        );

        b.setAseguradora(
                rs.getString("aseguradora")
        );

        b.setNumeroPoliza(
                rs.getString("numero_poliza")
        );

        b.setValorAsegurado(
                rs.getBigDecimal("valor_asegurado")
        );

        b.setFechaInicioSeguro(
                getNullableLocalDate(rs, "fecha_inicio_seguro")
        );

        b.setFechaVencimientoSeguro(
                getNullableLocalDate(rs, "fecha_vencimiento_seguro")
        );

        b.setEstadoSeguro(
                rs.getString("estado_seguro")
        );

        b.setNombreEstadoSeguro(
                rs.getString("nombre_estado_seguro")
        );

        b.setObservacionesSeguro(
                rs.getString("observaciones_seguro")
        );

        // =====================================================
        // Valores calculados desde la vista
        // =====================================================
        b.setValorNetoBien(
                rs.getBigDecimal("valor_neto_bien")
        );

        b.setValorPropiedadAsociado(
                rs.getBigDecimal("valor_propiedad_asociado")
        );

        b.setValorGravamenAsociado(
                rs.getBigDecimal("valor_gravamen_asociado")
        );

        b.setValorNetoAsociado(
                rs.getBigDecimal("valor_neto_asociado")
        );

        // =====================================================
        // Auditoría
        // =====================================================
        b.setFkSeguridadCreacion(
                getNullableInteger(rs, "fk_seguridad_creacion")
        );

        b.setFechaCreacion(
                rs.getTimestamp("fecha_creacion") == null
                        ? null
                        : rs.getTimestamp("fecha_creacion").toLocalDateTime()
        );

        b.setFkSeguridadEdicion(
                getNullableInteger(rs, "fk_seguridad_edicion")
        );

        b.setFechaEdicion(
                rs.getTimestamp("fecha_edicion") == null
                        ? null
                        : rs.getTimestamp("fecha_edicion").toLocalDateTime()
        );

        return b;
    };

    // =========================================================
    // Consultas
    // =========================================================

    public List<BienInmueble> listarPorPersona(Long idDatosPersonal) {

        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_inmuebles_total
                WHERE id_datos_personal = ?
                ORDER BY fecha_creacion DESC, id_bien DESC
                """;

        return jdbc.query(
                sql,
                mapper,
                idDatosPersonal
        );
    }

    public Optional<BienInmueble> buscarPorIdBien(Long idBien) {

        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_inmuebles_total
                WHERE id_bien = ?
                LIMIT 1
                """;

        List<BienInmueble> lista = jdbc.query(
                sql,
                mapper,
                idBien
        );

        return lista.stream().findFirst();
    }

    // =========================================================
    // Bien general: hoja_vida.bienes
    // =========================================================

    public Long insertarBien(
            BienInmueble dto,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO hoja_vida.bienes (
                    id_tipo_bien,
                    descripcion_general,
                    valor_comercial,
                    valor_gravamen,
                    fecha_adquisicion,
                    estado_bien,
                    fecha_estado,
                    observaciones,
                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES (
                    ?, ?, ?, ?,
                    ?, ?, ?, ?,
                    ?, CURRENT_TIMESTAMP,
                    ?, CURRENT_TIMESTAMP
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
                toSqlDate(dto.getFechaAdquisicion()),
                dto.getEstadoBien(),
                toSqlDate(dto.getFechaEstado()),
                dto.getObservacionesBien(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarBien(
            Long idBien,
            BienInmueble dto,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE hoja_vida.bienes
                SET id_tipo_bien = ?,
                    descripcion_general = ?,
                    valor_comercial = ?,
                    valor_gravamen = ?,
                    fecha_adquisicion = ?,
                    estado_bien = ?,
                    fecha_estado = ?,
                    observaciones = ?,
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
                toSqlDate(dto.getFechaAdquisicion()),
                dto.getEstadoBien(),
                toSqlDate(dto.getFechaEstado()),
                dto.getObservacionesBien(),
                idUsuario,
                idBien
        );
    }

    // =========================================================
    // Relación persona ↔ bien: hoja_vida.bienes_personas
    // =========================================================

    public Long insertarBienPersona(
            BienInmueble dto,
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
                    ?, ?, ?,
                    ?, CURRENT_TIMESTAMP,
                    ?, CURRENT_TIMESTAMP
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
            BienInmueble dto,
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

    // =========================================================
    // Inmueble: hoja_vida.bienes_inmuebles
    // =========================================================

    public Long insertarInmueble(
            BienInmueble dto,
            Long idBien,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO hoja_vida.bienes_inmuebles (
                    id_bien,
                    id_tipo_inmueble,
                    id_tipo_zona_inmueble,
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
                    fecha_registro_escritura,
                    oficina_registro,
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
                VALUES (
                    ?, ?, ?,
                    ?, ?,
                    ?, ?, ?,
                    ?, ?,
                    ?, ?,
                    ?, ?, ?,
                    ?, ?,
                    ?, ?, ?,
                    ?, ?,
                    ?, CURRENT_TIMESTAMP,
                    ?, CURRENT_TIMESTAMP
                )
                RETURNING id_bien_inmueble
                """;

        return jdbc.queryForObject(
                sql,
                Long.class,
                idBien,
                dto.getIdTipoInmueble(),
                dto.getIdTipoZonaInmueble(),
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
                toSqlDate(dto.getFechaEscritura()),
                dto.getNotaria(),
                toSqlDate(dto.getFechaRegistroEscritura()),
                dto.getOficinaRegistro(),
                dto.getIdPaisNotaria(),
                dto.getIdDepartamentoNotaria(),
                dto.getIdCiudadNotaria(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarInmueble(
            Long idBienInmueble,
            BienInmueble dto,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE hoja_vida.bienes_inmuebles
                SET id_tipo_inmueble = ?,
                    id_tipo_zona_inmueble = ?,
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
                    fecha_registro_escritura = ?,
                    oficina_registro = ?,
                    id_pais_notaria = ?,
                    id_departamento_notaria = ?,
                    id_ciudad_notaria = ?,
                    id_tipo_gravamen = ?,
                    observaciones = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_bien_inmueble = ?
                """;

        jdbc.update(
                sql,
                dto.getIdTipoInmueble(),
                dto.getIdTipoZonaInmueble(),
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
                toSqlDate(dto.getFechaEscritura()),
                dto.getNotaria(),
                toSqlDate(dto.getFechaRegistroEscritura()),
                dto.getOficinaRegistro(),
                dto.getIdPaisNotaria(),
                dto.getIdDepartamentoNotaria(),
                dto.getIdCiudadNotaria(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idBienInmueble
        );
    }

    // =========================================================
    // Validaciones y eliminación
    // =========================================================

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
                DELETE FROM hoja_vida.bienes_inmuebles
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

    // =========================================================
    // Utilidades privadas
    // =========================================================

    private static Long getNullableLong(
            ResultSet rs,
            String column
    ) throws SQLException {

        long value = rs.getLong(column);

        return rs.wasNull()
                ? null
                : value;
    }

    private static Integer getNullableInteger(
            ResultSet rs,
            String column
    ) throws SQLException {

        int value = rs.getInt(column);

        return rs.wasNull()
                ? null
                : value;
    }

    private static java.time.LocalDate getNullableLocalDate(
            ResultSet rs,
            String column
    ) throws SQLException {

        Date value = rs.getDate(column);

        return value == null
                ? null
                : value.toLocalDate();
    }

    private static Date toSqlDate(java.time.LocalDate value) {

        return value == null
                ? null
                : Date.valueOf(value);
    }
}