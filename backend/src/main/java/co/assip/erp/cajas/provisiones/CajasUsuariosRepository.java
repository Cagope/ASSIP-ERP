package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CajasUsuariosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // CAJA
    // =========================================================

    /**
     * Obtiene una caja activa.
     *
     * Se utiliza antes de realizar la vinculación para conocer
     * la agencia real de la caja y validar posteriormente que
     * el usuario tenga acceso a dicha agencia.
     */
    public Optional<CajaDisponibleDTO> obtenerCajaActiva(Long idCaja) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion,
                c.permite_efectivo,
                c.permite_cheques,
                c.permite_tarjetas,
                c.permite_transferencias
            FROM cajas.cajas c
            WHERE c.id_caja = :idCaja
              AND c.activa = TRUE
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCaja", idCaja);

        List<CajaDisponibleDTO> lista = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> mapCajaDisponible(rs)
        );

        return lista.stream().findFirst();
    }

    // =========================================================
    // CAJAS DISPONIBLES
    // =========================================================

    /**
     * Lista las cajas activas de una agencia que todavía no
     * han tenido vinculación durante la jornada actual.
     *
     * La fecha se obtiene directamente del servidor PostgreSQL.
     */
    public List<CajaDisponibleDTO> listarDisponibles(Integer idAgencia) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion,
                c.permite_efectivo,
                c.permite_cheques,
                c.permite_tarjetas,
                c.permite_transferencias
            FROM cajas.cajas c
            WHERE c.id_agencia = :idAgencia
              AND c.activa = TRUE
              AND NOT EXISTS (
                    SELECT 1
                    FROM cajas.cajas_usuarios cu
                    WHERE cu.id_caja = c.id_caja
                      AND cu.fecha_jornada = CURRENT_DATE
              )
            ORDER BY
                c.codigo_caja,
                c.id_caja
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia);

        return jdbc.query(
                sql,
                params,
                (rs, rowNum) -> mapCajaDisponible(rs)
        );
    }

    // =========================================================
    // VINCULACIÓN ACTIVA DEL USUARIO
    // =========================================================

    /**
     * Obtiene la caja actualmente vinculada al usuario
     * durante la jornada del servidor.
     */
    public Optional<CajaUsuarioDTO> obtenerActivaPorUsuario(Integer idUsuario) {

        String sql = """
        SELECT
            cu.id_caja_usuario,
            cu.id_caja,
            cu.id_usuario,
            cu.fecha_jornada,
            cu.estado,
            cu.fecha_vinculacion,
            cu.fecha_finalizacion,
            cu.motivo_finalizacion,

            c.id_agencia,
            c.codigo_caja,
            c.descripcion AS descripcion_caja,

            u.username,
            u.nombre_completo

        FROM cajas.cajas_usuarios cu

        INNER JOIN cajas.cajas c
            ON c.id_caja = cu.id_caja

        INNER JOIN seguridad.usuarios u
            ON u.id_usuario = cu.id_usuario

        WHERE cu.id_usuario = :idUsuario
          AND cu.fecha_jornada = CURRENT_DATE
          AND cu.estado = 'ACTIVA'
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idUsuario", idUsuario);

        List<CajaUsuarioDTO> lista = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> mapCajaUsuario(rs)
        );

        return lista.stream().findFirst();
    }

    // =========================================================
    // VINCULACIÓN DE UNA CAJA
    // =========================================================

    /**
     * Obtiene la vinculación activa de una caja durante
     * la jornada actual.
     */
    public Optional<CajaUsuarioDTO> obtenerActivaPorCaja(Long idCaja) {

        String sql = """
        SELECT
            cu.id_caja_usuario,
            cu.id_caja,
            cu.id_usuario,
            cu.fecha_jornada,
            cu.estado,
            cu.fecha_vinculacion,
            cu.fecha_finalizacion,
            cu.motivo_finalizacion,

            c.id_agencia,
            c.codigo_caja,
            c.descripcion AS descripcion_caja,

            u.username,
            u.nombre_completo

        FROM cajas.cajas_usuarios cu

        INNER JOIN cajas.cajas c
            ON c.id_caja = cu.id_caja

        INNER JOIN seguridad.usuarios u
            ON u.id_usuario = cu.id_usuario

        WHERE cu.id_caja = :idCaja
          AND cu.fecha_jornada = CURRENT_DATE
          AND cu.estado = 'ACTIVA'
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCaja", idCaja);

        List<CajaUsuarioDTO> lista = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> mapCajaUsuario(rs)
        );

        return lista.stream().findFirst();
    }

    // =========================================================
    // EXISTENCIA DE JORNADA
    // =========================================================

    /**
     * Indica si el usuario ya tuvo una vinculación durante
     * la jornada actual, incluso si ya fue finalizada.
     *
     * Esto evita que el usuario abra una segunda caja el mismo día.
     */
    public boolean existeJornadaUsuario(Integer idUsuario) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cajas.cajas_usuarios
                WHERE id_usuario = :idUsuario
                  AND fecha_jornada = CURRENT_DATE
            )
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idUsuario", idUsuario);

        Boolean existe = jdbc.queryForObject(
                sql,
                params,
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    /**
     * Indica si la caja ya tuvo una vinculación durante
     * la jornada actual, incluso si fue finalizada.
     *
     * Una caja cerrada definitivamente no puede iniciar
     * otra jornada durante el mismo día.
     */
    public boolean existeJornadaCaja(Long idCaja) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cajas.cajas_usuarios
                WHERE id_caja = :idCaja
                  AND fecha_jornada = CURRENT_DATE
            )
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCaja", idCaja);

        Boolean existe = jdbc.queryForObject(
                sql,
                params,
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    // =========================================================
    // CREAR VINCULACIÓN
    // =========================================================

    /**
     * Vincula el usuario autenticado con una caja.
     *
     * La fecha de jornada, estado y fechas se generan
     * directamente en PostgreSQL.
     */
    public Long crear(Long idCaja, Integer idUsuario) {

        String sql = """
            INSERT INTO cajas.cajas_usuarios
            (
                id_caja,
                id_usuario,
                fk_seguridad_creacion
            )
            VALUES
            (
                :idCaja,
                :idUsuario,
                :idUsuario
            )
            RETURNING id_caja_usuario
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCaja", idCaja)
                .addValue("idUsuario", idUsuario);

        return jdbc.queryForObject(
                sql,
                params,
                Long.class
        );
    }

    // =========================================================
    // OBTENER POR ID
    // =========================================================

    public Optional<CajaUsuarioDTO> obtenerPorId(Long idCajaUsuario) {

        String sql = """
        SELECT
            cu.id_caja_usuario,
            cu.id_caja,
            cu.id_usuario,
            cu.fecha_jornada,
            cu.estado,
            cu.fecha_vinculacion,
            cu.fecha_finalizacion,
            cu.motivo_finalizacion,

            c.id_agencia,
            c.codigo_caja,
            c.descripcion AS descripcion_caja,

            u.username,
            u.nombre_completo

        FROM cajas.cajas_usuarios cu

        INNER JOIN cajas.cajas c
            ON c.id_caja = cu.id_caja

        INNER JOIN seguridad.usuarios u
            ON u.id_usuario = cu.id_usuario

        WHERE cu.id_caja_usuario = :idCajaUsuario
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCajaUsuario", idCajaUsuario);

        List<CajaUsuarioDTO> lista = jdbc.query(
                sql,
                params,
                (rs, rowNum) -> mapCajaUsuario(rs)
        );

        return lista.stream().findFirst();
    }

    // =========================================================
    // MOVIMIENTOS DE LA CAJA EN LA JORNADA
    // =========================================================

    /**
     * Indica si la caja tiene movimientos registrados durante
     * la jornada actual.
     *
     * Se utiliza para impedir el cambio de caja cuando el usuario
     * ya inició operación.
     */
    public boolean tieneMovimientosHoy(Long idCaja) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cajas.movimientos_caja
                WHERE id_caja = :idCaja
                  AND fecha_contable = CURRENT_DATE
            )
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCaja", idCaja);

        Boolean existe = jdbc.queryForObject(
                sql,
                params,
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    // =========================================================
    // CAMBIAR CAJA
    // =========================================================

    /**
     * Corrige la caja de una vinculación activa.
     *
     * No crea una nueva jornada. Conserva el usuario,
     * fecha de jornada, estado y fecha de vinculación.
     */
    public int cambiarCaja(
            Long idCajaUsuario,
            Long idCajaNueva,
            Integer idUsuarioEdicion
    ) {

        String sql = """
            UPDATE cajas.cajas_usuarios
            SET
                id_caja = :idCajaNueva,
                fk_seguridad_edicion = :idUsuarioEdicion,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_caja_usuario = :idCajaUsuario
              AND estado = 'ACTIVA'
              AND fecha_jornada = CURRENT_DATE
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCajaUsuario", idCajaUsuario)
                .addValue("idCajaNueva", idCajaNueva)
                .addValue("idUsuarioEdicion", idUsuarioEdicion);

        return jdbc.update(sql, params);
    }

    // =========================================================
    // FINALIZAR VINCULACIÓN
    // =========================================================

    /**
     * Finaliza la vinculación.
     *
     * Este método quedará disponible para ser invocado
     * posteriormente desde el cierre definitivo de caja.
     */
    public int finalizar(
            Long idCajaUsuario,
            Integer idUsuarioEdicion,
            String motivoFinalizacion
    ) {

        String sql = """
            UPDATE cajas.cajas_usuarios
            SET
                estado = 'FINALIZADA',
                fecha_finalizacion = CURRENT_TIMESTAMP,
                motivo_finalizacion = :motivoFinalizacion,
                fk_seguridad_edicion = :idUsuarioEdicion,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_caja_usuario = :idCajaUsuario
              AND estado = 'ACTIVA'
              AND fecha_jornada = CURRENT_DATE
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCajaUsuario", idCajaUsuario)
                .addValue("idUsuarioEdicion", idUsuarioEdicion)
                .addValue("motivoFinalizacion", motivoFinalizacion);

        return jdbc.update(sql, params);
    }

    // =========================================================
    // MAPPERS
    // =========================================================

    private CajaDisponibleDTO mapCajaDisponible(java.sql.ResultSet rs)
            throws java.sql.SQLException {

        return CajaDisponibleDTO.builder()
                .idCaja(rs.getLong("id_caja"))
                .idAgencia(rs.getInt("id_agencia"))
                .codigoCaja(rs.getString("codigo_caja"))
                .descripcionCaja(rs.getString("descripcion"))
                .permiteEfectivo(rs.getBoolean("permite_efectivo"))
                .permiteCheques(rs.getBoolean("permite_cheques"))
                .permiteTarjetas(rs.getBoolean("permite_tarjetas"))
                .permiteTransferencias(rs.getBoolean("permite_transferencias"))
                .build();
    }

    private CajaUsuarioDTO mapCajaUsuario(java.sql.ResultSet rs)
            throws java.sql.SQLException {

        CajaUsuarioDTO dto = new CajaUsuarioDTO();

        dto.setIdCajaUsuario(rs.getLong("id_caja_usuario"));
        dto.setFechaJornada(
                rs.getObject("fecha_jornada", java.time.LocalDate.class)
        );
        dto.setEstado(rs.getString("estado"));
        dto.setFechaVinculacion(
                rs.getObject(
                        "fecha_vinculacion",
                        java.time.LocalDateTime.class
                )
        );
        dto.setFechaFinalizacion(
                rs.getObject(
                        "fecha_finalizacion",
                        java.time.LocalDateTime.class
                )
        );
        dto.setMotivoFinalizacion(
                rs.getString("motivo_finalizacion")
        );

        dto.setIdCaja(rs.getLong("id_caja"));
        dto.setIdAgencia(rs.getInt("id_agencia"));
        dto.setCodigoCaja(rs.getString("codigo_caja"));
        dto.setDescripcionCaja(rs.getString("descripcion_caja"));

        dto.setIdUsuario(rs.getInt("id_usuario"));
        dto.setUsername(rs.getString("username"));
        dto.setNombreCompleto(rs.getString("nombre_completo"));

        return dto;
    }
}