package co.assip.erp.hojavida.bienesvehiculos;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BienVehiculoRepository {

    private final JdbcTemplate jdbc;

    public BienVehiculoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<BienVehiculo> mapper = (rs, rowNum) -> {
        BienVehiculo b = new BienVehiculo();

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

        b.setIdBienVehiculo(rs.getLong("id_bien_vehiculo"));
        b.setIdTipoVehiculo(rs.getLong("id_tipo_vehiculo"));
        b.setNombreTipoVehiculo(rs.getString("nombre_tipo_vehiculo"));

        b.setPlaca(rs.getString("placa"));
        b.setMarca(rs.getString("marca"));
        b.setLinea(rs.getString("linea"));

        int modelo = rs.getInt("modelo");
        b.setModelo(rs.wasNull() ? null : modelo);

        b.setColor(rs.getString("color"));
        b.setNumeroMotor(rs.getString("numero_motor"));
        b.setNumeroChasis(rs.getString("numero_chasis"));
        b.setNumeroSerie(rs.getString("numero_serie"));

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

    // ============================================================
    // CONSULTAS
    // ============================================================

    public List<BienVehiculo> listarPorPersona(Long idDatosPersonal) {
        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_vehiculos_total
                WHERE id_datos_personal = ?
                ORDER BY fecha_creacion DESC, id_bien DESC
                """;

        return jdbc.query(sql, mapper, idDatosPersonal);
    }

    public Optional<BienVehiculo> buscarPorIdBien(Long idBien) {
        String sql = """
                SELECT *
                FROM reporting.vw_hoja_vida_bienes_vehiculos_total
                WHERE id_bien = ?
                LIMIT 1
                """;

        List<BienVehiculo> lista = jdbc.query(sql, mapper, idBien);
        return lista.stream().findFirst();
    }

    // ============================================================
    // BIEN GENERAL
    // ============================================================

    public Long insertarBien(BienVehiculo dto, Integer idUsuario) {
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

    public void actualizarBien(Long idBien, BienVehiculo dto, Integer idUsuario) {
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

    // ============================================================
    // RELACIÓN BIEN - PERSONA
    // ============================================================

    public Long insertarBienPersona(BienVehiculo dto, Long idBien, Integer idUsuario) {
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

    public void actualizarBienPersona(Long idBienPersona, BienVehiculo dto, Integer idUsuario) {
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

    // ============================================================
    // DETALLE VEHÍCULO
    // ============================================================

    public Long insertarVehiculo(BienVehiculo dto, Long idBien, Integer idUsuario) {
        String sql = """
                INSERT INTO hoja_vida.bienes_vehiculos (
                    id_bien,
                    id_tipo_vehiculo,
                    placa,
                    marca,
                    linea,
                    modelo,
                    color,
                    numero_motor,
                    numero_chasis,
                    numero_serie,
                    id_tipo_gravamen,
                    observaciones,
                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP)
                RETURNING id_bien_vehiculo
                """;

        return jdbc.queryForObject(sql, Long.class,
                idBien,
                dto.getIdTipoVehiculo(),
                dto.getPlaca(),
                dto.getMarca(),
                dto.getLinea(),
                dto.getModelo(),
                dto.getColor(),
                dto.getNumeroMotor(),
                dto.getNumeroChasis(),
                dto.getNumeroSerie(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idUsuario
        );
    }

    public void actualizarVehiculo(Long idBienVehiculo, BienVehiculo dto, Integer idUsuario) {
        String sql = """
                UPDATE hoja_vida.bienes_vehiculos
                SET id_tipo_vehiculo = ?,
                    placa = ?,
                    marca = ?,
                    linea = ?,
                    modelo = ?,
                    color = ?,
                    numero_motor = ?,
                    numero_chasis = ?,
                    numero_serie = ?,
                    id_tipo_gravamen = ?,
                    observaciones = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_bien_vehiculo = ?
                """;

        jdbc.update(sql,
                dto.getIdTipoVehiculo(),
                dto.getPlaca(),
                dto.getMarca(),
                dto.getLinea(),
                dto.getModelo(),
                dto.getColor(),
                dto.getNumeroMotor(),
                dto.getNumeroChasis(),
                dto.getNumeroSerie(),
                dto.getIdTipoGravamen(),
                dto.getObservaciones(),
                idUsuario,
                idBienVehiculo
        );
    }

    // ============================================================
    // EXISTENCIA / ELIMINACIÓN
    // ============================================================

    public boolean existeBien(Long idBien) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hoja_vida.bienes WHERE id_bien = ?",
                Integer.class,
                idBien
        );

        return total != null && total > 0;
    }

    public void eliminarPorBien(Long idBien) {
        jdbc.update("DELETE FROM hoja_vida.bienes_vehiculos WHERE id_bien = ?", idBien);
        jdbc.update("DELETE FROM hoja_vida.bienes_personas WHERE id_bien = ?", idBien);
        jdbc.update("DELETE FROM hoja_vida.bienes WHERE id_bien = ?", idBien);
    }
}