
package co.assip.erp.contabilidad.periodos;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a los períodos contables independientes por agencia.
 * Proporciona consultas y bloqueos para controlar la contabilización
 * y los cierres mensuales.
 */
@Repository
@RequiredArgsConstructor
public class PeriodosContablesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Consulta el estado de un período sin bloquearlo.
     * Se utiliza para consultas informativas, no para autorizar registros.
     */
    public Optional<String> consultarEstado(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        String sql = """
            SELECT estado
              FROM contabilidad.periodos_contables
             WHERE id_agencia = :idAgencia
               AND ano = :ano
               AND mes = :mes
            """;

        List<String> estados = jdbc.queryForList(
                sql,
                parametrosPeriodo(idAgencia, ano, mes),
                String.class
        );

        return estados.stream().findFirst();
    }

    /**
     * Bloquea el período durante la transacción activa.
     * Impide que el cierre y la contabilización modifiquen
     * simultáneamente el mismo período.
     */
    public Optional<String> consultarEstadoConBloqueo(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        String sql = """
            SELECT estado
              FROM contabilidad.periodos_contables
             WHERE id_agencia = :idAgencia
               AND ano = :ano
               AND mes = :mes
             FOR UPDATE
            """;

        List<String> estados = jdbc.queryForList(
                sql,
                parametrosPeriodo(idAgencia, ano, mes),
                String.class
        );

        return estados.stream().findFirst();
    }

    /**
     * Cambia el estado de un período previamente bloqueado.
     * Solo permite la transición desde el estado esperado.
     */
    public boolean cambiarEstado(
            Long idPeriodoContable,
            String estadoAnterior,
            String estadoNuevo,
            Integer idUsuario
    ) {
        String sql = """
            UPDATE contabilidad.periodos_contables
               SET estado = :estadoNuevo,
                   fecha_modificacion = CURRENT_TIMESTAMP,
                   fk_seguridad_modificacion = :idUsuario
             WHERE id_periodo_contable = :idPeriodo
               AND estado = :estadoAnterior
            """;

        int actualizados = jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodo", idPeriodoContable)
                        .addValue("estadoAnterior", estadoAnterior)
                        .addValue("estadoNuevo", estadoNuevo)
                        .addValue("idUsuario", idUsuario)
        );

        return actualizados == 1;
    }

    /**
     * Obtiene el identificador y estado de un período
     * aplicando bloqueo de escritura durante la transacción.
     */
    public Optional<PeriodoBloqueado> bloquearPeriodo(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        String sql = """
            SELECT id_periodo_contable, estado
              FROM contabilidad.periodos_contables
             WHERE id_agencia = :idAgencia
               AND ano = :ano
               AND mes = :mes
             FOR UPDATE
            """;

        List<PeriodoBloqueado> periodos = jdbc.query(
                sql,
                parametrosPeriodo(idAgencia, ano, mes),
                (rs, rowNum) -> new PeriodoBloqueado(
                        rs.getLong("id_periodo_contable"),
                        rs.getString("estado")
                )
        );

        return periodos.stream().findFirst();
    }

    /**
     * Registra una apertura, cierre o reapertura.
     * Debe ejecutarse en la misma transacción del cambio de estado.
     */
    public void registrarHistorial(
            Long idPeriodoContable,
            String estadoAnterior,
            String estadoNuevo,
            String accion,
            String justificacion,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO contabilidad.periodos_contables_historial (
                id_periodo_contable,
                estado_anterior,
                estado_nuevo,
                accion,
                justificacion,
                fk_seguridad_usuario
            ) VALUES (
                :idPeriodo,
                :estadoAnterior,
                :estadoNuevo,
                :accion,
                :justificacion,
                :idUsuario
            )
            """;

        int insertados = jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodo", idPeriodoContable)
                        .addValue("estadoAnterior", estadoAnterior)
                        .addValue("estadoNuevo", estadoNuevo)
                        .addValue("accion", accion)
                        .addValue("justificacion", justificacion)
                        .addValue("idUsuario", idUsuario)
        );

        if (insertados != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar el historial del período contable."
            );
        }
    }

    /**
     * Crea un período abierto para una agencia.
     * La restricción única de PostgreSQL impide duplicados.
     */
    public Long crearPeriodo(
            Integer idAgencia,
            int ano,
            int mes,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO contabilidad.periodos_contables (
                id_agencia,
                ano,
                mes,
                estado,
                fk_seguridad_creacion
            ) VALUES (
                :idAgencia,
                :ano,
                :mes,
                'ABIERTO',
                :idUsuario
            )
            RETURNING id_periodo_contable
            """;

        Long idPeriodo = jdbc.queryForObject(
                sql,
                parametrosPeriodo(idAgencia, ano, mes)
                        .addValue("idUsuario", idUsuario),
                Long.class
        );

        if (idPeriodo == null) {
            throw new IllegalStateException(
                    "No se pudo crear el período contable."
            );
        }

        return idPeriodo;
    }

    /**
     * Construye los parámetros comunes de agencia y período.
     */
    private MapSqlParameterSource parametrosPeriodo(
            Integer idAgencia,
            int ano,
            int mes
    ) {
        return new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("ano", ano)
                .addValue("mes", mes);
    }

    /**
     * Representa el período identificado y bloqueado
     * para ejecutar operaciones transaccionales.
     */
    public record PeriodoBloqueado(
            Long idPeriodoContable,
            String estado
    ) {
    }
}
