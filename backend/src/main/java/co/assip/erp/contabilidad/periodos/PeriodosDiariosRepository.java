package co.assip.erp.contabilidad.periodos;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acceso al control diario de contabilización por agencia y fecha.
 * Las operaciones de bloqueo y cambio de estado requieren una transacción activa.
 */
@Repository
@RequiredArgsConstructor
public class PeriodosDiariosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /** Consulta el estado de una fecha sin bloquearla; uso exclusivamente informativo. */
    public Optional<String> consultarEstado(Integer idAgencia, LocalDate fecha) {
        String sql = """
            SELECT estado
              FROM contabilidad.periodos_diarios
             WHERE id_agencia = :idAgencia
               AND fecha = :fecha
            """;
        List<String> estados = jdbc.queryForList(sql, parametrosFecha(idAgencia, fecha), String.class);
        return estados.stream().findFirst();
    }

    /**
     * Crea el control de una fecha en estado abierto si todavía no existe.
     * No modifica un día previamente cerrado ni reemplaza su usuario creador.
     */
    public boolean crearAbiertoSiNoExiste(Integer idAgencia, LocalDate fecha, Integer idUsuario) {
        String sql = """
            INSERT INTO contabilidad.periodos_diarios (
                id_agencia, fecha, estado, fk_seguridad_creacion
            ) VALUES (
                :idAgencia, :fecha, 'A', :idUsuario
            )
            ON CONFLICT (id_agencia, fecha) DO NOTHING
            """;
        int insertados = jdbc.update(sql, parametrosFecha(idAgencia, fecha)
                .addValue("idUsuario", idUsuario));
        return insertados == 1;
    }

    /**
     * Bloquea el registro diario hasta finalizar la transacción.
     * El llamador debe bloquear primero el período mensual correspondiente.
     */
    public Optional<PeriodoDiarioBloqueado> bloquearDia(Integer idAgencia, LocalDate fecha) {
        String sql = """
            SELECT id_periodo_diario, estado
              FROM contabilidad.periodos_diarios
             WHERE id_agencia = :idAgencia
               AND fecha = :fecha
             FOR UPDATE
            """;
        List<PeriodoDiarioBloqueado> periodos = jdbc.query(
                sql,
                parametrosFecha(idAgencia, fecha),
                (rs, rowNum) -> new PeriodoDiarioBloqueado(
                        rs.getLong("id_periodo_diario"),
                        rs.getString("estado").trim()
                )
        );
        return periodos.stream().findFirst();
    }

    /**
     * Actualiza el estado únicamente si coincide con el estado esperado.
     * Registra el usuario y la fecha de modificación para trazabilidad.
     */
    public boolean cambiarEstado(Long idPeriodoDiario, String estadoAnterior,
                                 String estadoNuevo, Integer idUsuario) {
        String sql = """
            UPDATE contabilidad.periodos_diarios
               SET estado = :estadoNuevo,
                   fecha_modificacion = CURRENT_TIMESTAMP,
                   fk_seguridad_modificacion = :idUsuario
             WHERE id_periodo_diario = :idPeriodoDiario
               AND estado = :estadoAnterior
            """;
        int actualizados = jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idPeriodoDiario", idPeriodoDiario)
                .addValue("estadoAnterior", estadoAnterior)
                .addValue("estadoNuevo", estadoNuevo)
                .addValue("idUsuario", idUsuario));
        return actualizados == 1;
    }

    /**
     * Identifica únicamente los días con movimientos contables activos
     * cuyo control diario no está cerrado. Los días sin actividad no bloquean.
     */
    public List<DiaPendienteCierre> consultarDiasPendientesCierre(
            Integer idAgencia, int ano, int mes) {

        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate finExclusivo = inicio.plusMonths(1);

        String sql = """
            WITH dias_con_movimientos AS (
                SELECT DISTINCT a.fecha_auxiliar::date AS fecha
                  FROM contabilidad.auxiliares_contables a
                 WHERE a.id_agencia = :idAgencia
                   AND a.fecha_auxiliar >= :inicio
                   AND a.fecha_auxiliar < :finExclusivo
                   AND a.estado_movimiento IN ('A', 'C')
            )
            SELECT d.fecha,
                   COALESCE(pd.estado, 'SIN_REGISTRO') AS estado
              FROM dias_con_movimientos d
              LEFT JOIN contabilidad.periodos_diarios pd
                ON pd.id_agencia = :idAgencia
               AND pd.fecha = d.fecha
             WHERE pd.id_periodo_diario IS NULL
                OR pd.estado <> 'C'
             ORDER BY d.fecha
            """;

        return jdbc.query(sql, new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("inicio", inicio)
                        .addValue("finExclusivo", finExclusivo),
                (rs, rowNum) -> new DiaPendienteCierre(
                        rs.getDate("fecha").toLocalDate(),
                        rs.getString("estado").trim()
                ));
    }

    /** Cuenta exclusivamente los días con cierre diario real en el período. */
    public int contarDiasCerrados(Integer idAgencia, int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        String sql = """
            SELECT COUNT(*)
            FROM contabilidad.periodos_diarios
            WHERE id_agencia = :idAgencia
              AND fecha >= :inicio
              AND fecha < :finExclusivo
              AND estado = 'C'
            """;
        Integer cantidad = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("inicio", inicio)
                .addValue("finExclusivo", inicio.plusMonths(1)), Integer.class);
        return cantidad == null ? 0 : cantidad;
    }

    /** Fecha y estado pendientes del cierre mensual. */
    public record DiaPendienteCierre(LocalDate fecha, String estado) {
    }

    /** Construye los parámetros compartidos por las consultas de agencia y fecha. */
    private MapSqlParameterSource parametrosFecha(Integer idAgencia, LocalDate fecha) {
        return new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("fecha", fecha);
    }

    /** Identifica un día bloqueado y su estado actual dentro de la transacción. */
    public record PeriodoDiarioBloqueado(Long idPeriodoDiario, String estado) {
    }
}
