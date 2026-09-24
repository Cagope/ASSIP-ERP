package co.assip.erp.cartera.catalogos;

import co.assip.erp.cartera.catalogos.dto.CarteraCatalogoDTO;
import co.assip.erp.cartera.catalogos.dto.FondoGarantiaCatalogoDTO;
import co.assip.erp.cartera.catalogos.dto.LineaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.ClasificacionCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.GarantiaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.DestinoEconomicoDTO;
import co.assip.erp.cartera.catalogos.dto.SubgarantiaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.FormaPagoDTO;
import co.assip.erp.cartera.catalogos.dto.ModalidadInteresDTO;
import co.assip.erp.cartera.catalogos.dto.TipoCuotaDTO;
import co.assip.erp.cartera.catalogos.dto.CentralRiesgoDTO;

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
// LÍNEAS DE CRÉDITO - CATÁLOGO COMPARTIDO DETALLADO
// =========================================================

    public List<LineaCreditoDTO> listarLineasCreditoDetalle(
            boolean soloActivas
    ) {

        String sql = """
        SELECT
            id_linea_credito                  AS "idLineaCredito",
            codigo_linea_credito              AS "codigoLineaCredito",
            nombre_linea_credito              AS "nombreLineaCredito",
            es_utilizacion_cupo_tarjeta       AS "esUtilizacionCupoTarjeta",
            permite_creditos_simultaneos      AS "permiteCreditosSimultaneos",
            activo
        FROM cartera.lineas_creditos
        WHERE (:soloActivas = FALSE OR activo = TRUE)
        ORDER BY
            codigo_linea_credito,
            nombre_linea_credito
        """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(LineaCreditoDTO.class)
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
    // CLASIFICACIONES DE CRÉDITO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<ClasificacionCreditoDTO> listarClasificacionesCreditoDetalle(
            boolean soloActivas
    ) {

        String sql = """
                SELECT
                    codigo_clasificacion_credito
                        AS "codigoClasificacionCredito",

                    descripcion_clasificacion_credito
                        AS "descripcionClasificacionCredito",

                    activo
                FROM cartera.clasificaciones_creditos
                WHERE (:soloActivas = FALSE OR activo = TRUE)
                ORDER BY
                    codigo_clasificacion_credito,
                    descripcion_clasificacion_credito
                """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(ClasificacionCreditoDTO.class)
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
    // GARANTÍAS DE CRÉDITO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<GarantiaCreditoDTO> listarGarantiasCreditoDetalle(
            boolean soloActivas
    ) {

        String sql = """
                SELECT
                    codigo_garantia_credito
                        AS "codigoGarantiaCredito",

                    descripcion_garantia_credito
                        AS "descripcionGarantiaCredito",

                    tipo_garantia
                        AS "tipoGarantia",

                    activo
                FROM cartera.garantias_creditos
                WHERE (:soloActivas = FALSE OR activo = TRUE)
                ORDER BY
                    descripcion_garantia_credito,
                    codigo_garantia_credito
                """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(GarantiaCreditoDTO.class)
                .list();
    }

    // =========================================================
    // DESTINOS ECONÓMICOS - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<DestinoEconomicoDTO> listarDestinosEconomicosDetalle(
            boolean soloActivas
    ) {

        String sql = """
                SELECT
                    codigo_destino_economico
                        AS "codigoDestinoEconomico",

                    descripcion_destino_economico
                        AS "descripcionDestinoEconomico",

                    activo
                FROM cartera.destinos_economicos
                WHERE (:soloActivas = FALSE OR activo = TRUE)
                ORDER BY
                    descripcion_destino_economico,
                    codigo_destino_economico
                """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(DestinoEconomicoDTO.class)
                .list();
    }

    // =========================================================
    // SUBGARANTÍAS DE CRÉDITO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<SubgarantiaCreditoDTO> listarSubgarantiasCreditoDetalle(
            boolean soloActivas
    ) {

        String sql = """
                SELECT
                    codigo_subgarantia
                        AS "codigoSubgarantia",

                    descripcion_subgarantia
                        AS "descripcionSubgarantia",

                    activo
                FROM cartera.subgarantias_creditos
                WHERE (:soloActivas = FALSE OR activo = TRUE)
                ORDER BY
                    descripcion_subgarantia,
                    codigo_subgarantia
                """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(SubgarantiaCreditoDTO.class)
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

    // =========================================================
    // FORMAS DE PAGO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<FormaPagoDTO> listarFormasPagoDetalle(
            boolean soloActivas
    ) {

        String sql = """
                SELECT
                    codigo_forma_pago
                        AS "codigoFormaPago",

                    descripcion_forma_pago
                        AS "descripcionFormaPago",

                    activo
                FROM cartera.formas_pago
                WHERE (:soloActivas = FALSE OR activo = TRUE)
                ORDER BY
                    descripcion_forma_pago,
                    codigo_forma_pago
                """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(FormaPagoDTO.class)
                .list();
    }

    // =========================================================
    // MODALIDADES DE INTERÉS - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<ModalidadInteresDTO> listarModalidadesInteresesDetalle(
            boolean soloActivas
    ) {

        String sql = """
                SELECT
                    periodo_codigo
                        AS "periodoCodigo",

                    tipo_modalidad
                        AS "tipoModalidad",

                    descripcion_modalidad_interes
                        AS "descripcionModalidadInteres",

                    periodo_meses
                        AS "periodoMeses",

                    activo
                FROM cartera.modalidades_intereses
                WHERE (:soloActivas = FALSE OR activo = TRUE)
                ORDER BY
                    periodo_meses,
                    periodo_codigo,
                    tipo_modalidad
                """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(ModalidadInteresDTO.class)
                .list();
    }

    // =========================================================
    // TIPOS DE CUOTA - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<TipoCuotaDTO> listarTiposCuotasDetalle(
            boolean soloActivas
    ) {

        String sql = """
                SELECT
                    codigo_tipo_cuota
                        AS "codigoTipoCuota",

                    descripcion_tipo_cuota
                        AS "descripcionTipoCuota",

                    activo
                FROM cartera.tipos_cuotas
                WHERE (:soloActivas = FALSE OR activo = TRUE)
                ORDER BY
                    descripcion_tipo_cuota,
                    codigo_tipo_cuota
                """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(TipoCuotaDTO.class)
                .list();
    }

    // =========================================================
    // CENTRALES DE RIESGO - CATÁLOGO COMPARTIDO DETALLADO
    // =========================================================

    public List<CentralRiesgoDTO> listarCentralesRiesgoDetalle(
            boolean soloActivas
    ) {

        String sql = """
                SELECT
                    id_central_riesgo
                        AS "idCentralRiesgo",

                    codigo_central
                        AS "codigoCentral",

                    documento_central
                        AS "documentoCentral",

                    nombre_central
                        AS "nombreCentral",

                    descripcion,

                    activo
                FROM cartera.centrales_riesgo
                WHERE (:soloActivas = FALSE OR activo = TRUE)
                ORDER BY
                    nombre_central,
                    codigo_central
                """;

        return jdbc
                .sql(sql)
                .param("soloActivas", soloActivas)
                .query(CentralRiesgoDTO.class)
                .list();
    }


    // =========================================================
    // FONDOS DE GARANTÍAS
    // =========================================================

    public List<FondoGarantiaCatalogoDTO> listarFondosGarantias() {

        String sql = """
            SELECT
                id_fondo_garantia    AS "idFondoGarantia",
                codigo_fondo         AS "codigoFondo",
                nombre_fondo         AS "nombreFondo",
                porcentaje_fondo     AS "porcentajeFondo",
                activo
            FROM cartera.fondos_garantias
            WHERE activo = true
            ORDER BY
                codigo_fondo,
                nombre_fondo
            """;

        return jdbc
                .sql(sql)
                .query(FondoGarantiaCatalogoDTO.class)
                .list();
    }

}