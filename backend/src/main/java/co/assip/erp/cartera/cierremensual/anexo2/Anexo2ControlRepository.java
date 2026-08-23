package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo2ControlRepository {

    private final JdbcTemplate jdbcTemplate;

    // Control del proceso PE y estado de ejecución.

    public boolean existeCierre(Integer idCierreCartera) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.cierres_cartera
                WHERE id_cierre_cartera = ?
            )
            """;

        Boolean existe = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                idCierreCartera
        );

        return Boolean.TRUE.equals(existe);
    }


    public Integer iniciarProceso(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cartera.pe_procesos
            (
                id_cierre_cartera,
                estado,
                fecha_inicio,
                fecha_fin,
                fk_seguridad_creacion
            )
            VALUES
            (
                ?,
                'PROCESANDO',
                CURRENT_TIMESTAMP,
                NULL,
                ?
            )
            ON CONFLICT (id_cierre_cartera)
            DO UPDATE SET
                estado = 'PROCESANDO',
                fecha_inicio = CURRENT_TIMESTAMP,
                fecha_fin = NULL,
                fk_seguridad_creacion = EXCLUDED.fk_seguridad_creacion
            RETURNING id_pe_proceso
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idCierreCartera,
                idUsuario
        );
    }


    public void finalizarProceso(
            Integer idPeProceso
    ) {

        jdbcTemplate.update(
                """
                UPDATE cartera.pe_procesos
                SET
                    estado = 'FINALIZADO',
                    fecha_fin = CURRENT_TIMESTAMP
                WHERE id_pe_proceso = ?
                """,
                idPeProceso
        );
    }


    public void marcarProcesoError(
            Integer idPeProceso
    ) {

        jdbcTemplate.update(
                """
                UPDATE cartera.pe_procesos
                SET
                    estado = 'ERROR',
                    fecha_fin = CURRENT_TIMESTAMP
                WHERE id_pe_proceso = ?
                """,
                idPeProceso
        );
    }


}
