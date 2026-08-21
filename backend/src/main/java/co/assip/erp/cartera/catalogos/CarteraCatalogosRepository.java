package co.assip.erp.cartera.catalogos;

import co.assip.erp.cartera.catalogos.dto.CarteraCatalogoDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CarteraCatalogosRepository {

    private final JdbcClient jdbc;

    public CarteraCatalogosRepository(
            JdbcClient jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // LÍNEAS DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarLineasCredito() {

        String sql = """
            SELECT
                id_linea_credito::bigint        AS id,
                codigo_linea_credito            AS codigo,
                nombre_linea_credito            AS nombre,
                es_utilizacion_cupo_tarjeta     AS "esUtilizacionCupoTarjeta",
                activo
            FROM cartera.lineas_creditos
            ORDER BY
                codigo_linea_credito,
                nombre_linea_credito
            """;

        return jdbc
                .sql(sql)
                .query(CarteraCatalogoDTO.class)
                .list();
    }

    // =========================================================
    // EDADES DE RIESGO
    // =========================================================

    public List<CarteraCatalogoDTO> listarEdadesRiesgo() {

        String sql = """
                SELECT
                    NULL::bigint            AS id,
                    codigo_edad_riesgo      AS codigo,
                    descripcion_edad_riesgo AS nombre,
                    activo
                FROM cartera.edades_riesgo
                ORDER BY
                    orden_edad_riesgo,
                    codigo_edad_riesgo
                """;

        return jdbc
                .sql(sql)
                .query(CarteraCatalogoDTO.class)
                .list();
    }

    // =========================================================
    // CLASIFICACIONES DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarClasificacionesCredito() {

        String sql = """
                SELECT
                    NULL::bigint                       AS id,
                    codigo_clasificacion_credito       AS codigo,
                    descripcion_clasificacion_credito  AS nombre,
                    activo
                FROM cartera.clasificaciones_creditos
                ORDER BY
                    codigo_clasificacion_credito,
                    descripcion_clasificacion_credito
                """;

        return jdbc
                .sql(sql)
                .query(CarteraCatalogoDTO.class)
                .list();
    }

    // =========================================================
    // GARANTÍAS DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarGarantiasCredito() {

        String sql = """
                SELECT
                    NULL::bigint                    AS id,
                    codigo_garantia_credito         AS codigo,
                    descripcion_garantia_credito    AS nombre,
                    activo
                FROM cartera.garantias_creditos
                ORDER BY
                    codigo_garantia_credito,
                    descripcion_garantia_credito
                """;

        return jdbc
                .sql(sql)
                .query(CarteraCatalogoDTO.class)
                .list();
    }

    // =========================================================
    // ESTADOS DE CARTERA
    // =========================================================

    public List<CarteraCatalogoDTO> listarEstadosCartera() {

        String sql = """
                SELECT
                    NULL::bigint                 AS id,
                    codigo_estado_cartera        AS codigo,
                    descripcion_estado_cartera   AS nombre,
                    activo
                FROM cartera.estados_cartera
                ORDER BY
                    codigo_estado_cartera,
                    descripcion_estado_cartera
                """;

        return jdbc
                .sql(sql)
                .query(CarteraCatalogoDTO.class)
                .list();
    }

    // =========================================================
    // ESTADOS JURÍDICOS
    // =========================================================

    public List<CarteraCatalogoDTO> listarEstadosJuridicos() {

        String sql = """
                SELECT
                    NULL::bigint                  AS id,
                    codigo_estado_juridico        AS codigo,
                    descripcion_estado_juridico   AS nombre,
                    activo
                FROM cartera.estados_juridicos
                ORDER BY
                    codigo_estado_juridico,
                    descripcion_estado_juridico
                """;

        return jdbc
                .sql(sql)
                .query(CarteraCatalogoDTO.class)
                .list();
    }

    // =========================================================
    // FORMAS DE PAGO
    // =========================================================

    public List<CarteraCatalogoDTO> listarFormasPago() {

        String sql = """
                SELECT
                    NULL::bigint              AS id,
                    codigo_forma_pago         AS codigo,
                    descripcion_forma_pago    AS nombre,
                    activo
                FROM cartera.formas_pago
                ORDER BY
                    codigo_forma_pago,
                    descripcion_forma_pago
                """;

        return jdbc
                .sql(sql)
                .query(CarteraCatalogoDTO.class)
                .list();
    }

}