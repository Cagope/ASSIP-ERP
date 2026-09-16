package co.assip.erp.cartera.originacion.catalogos;

import co.assip.erp.cartera.originacion.catalogos.dto.CentralRiesgoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.ClasificacionCreditoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.DestinoEconomicoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.FormaPagoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.GarantiaCreditoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.LineaCreditoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.ModalidadInteresDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.SubgarantiaCreditoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.TipoCuotaDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OriginacionCatalogoRepository {

    // =========================================================
    // LÍNEAS DE CRÉDITO
    // =========================================================

    private static final String SQL_LINEAS_CREDITO = """
            SELECT
                id_linea_credito,
                codigo_linea_credito,
                nombre_linea_credito,
                es_utilizacion_cupo_tarjeta
            FROM cartera.lineas_creditos
            WHERE activo = true
            ORDER BY
                codigo_linea_credito,
                nombre_linea_credito
            """;


    // =========================================================
    // CLASIFICACIONES DE CRÉDITO
    // =========================================================

    private static final String SQL_CLASIFICACIONES_CREDITO = """
            SELECT
                codigo_clasificacion_credito,
                descripcion_clasificacion_credito
            FROM cartera.clasificaciones_creditos
            WHERE activo = true
            ORDER BY
                codigo_clasificacion_credito,
                descripcion_clasificacion_credito
            """;


    // =========================================================
    // DESTINOS ECONÓMICOS
    // =========================================================

    private static final String SQL_DESTINOS_ECONOMICOS = """
            SELECT
                codigo_destino_economico,
                descripcion_destino_economico
            FROM cartera.destinos_economicos
            WHERE activo = true
            ORDER BY
                descripcion_destino_economico,
                codigo_destino_economico
            """;


    // =========================================================
    // GARANTÍAS
    // =========================================================

    private static final String SQL_GARANTIAS_CREDITOS = """
            SELECT
                codigo_garantia_credito,
                descripcion_garantia_credito,
                tipo_garantia
            FROM cartera.garantias_creditos
            WHERE activo = true
            ORDER BY
                descripcion_garantia_credito,
                codigo_garantia_credito
            """;


    // =========================================================
    // SUBGARANTÍAS
    // =========================================================

    private static final String SQL_SUBGARANTIAS_CREDITOS = """
            SELECT
                codigo_subgarantia,
                descripcion_subgarantia
            FROM cartera.subgarantias_creditos
            WHERE activo = true
            ORDER BY
                descripcion_subgarantia,
                codigo_subgarantia
            """;


    // =========================================================
    // FORMAS DE PAGO
    // =========================================================

    private static final String SQL_FORMAS_PAGO = """
            SELECT
                codigo_forma_pago,
                descripcion_forma_pago
            FROM cartera.formas_pago
            WHERE activo = true
            ORDER BY
                descripcion_forma_pago,
                codigo_forma_pago
            """;


    // =========================================================
    // MODALIDADES DE INTERÉS
    // =========================================================

    private static final String SQL_MODALIDADES_INTERESES = """
            SELECT
                periodo_codigo,
                tipo_modalidad,
                descripcion_modalidad_interes,
                periodo_meses
            FROM cartera.modalidades_intereses
            WHERE activo = true
            ORDER BY
                periodo_meses,
                periodo_codigo,
                tipo_modalidad
            """;


    // =========================================================
    // TIPOS DE CUOTA
    // =========================================================

    private static final String SQL_TIPOS_CUOTAS = """
            SELECT
                codigo_tipo_cuota,
                descripcion_tipo_cuota
            FROM cartera.tipos_cuotas
            WHERE activo = true
            ORDER BY
                descripcion_tipo_cuota,
                codigo_tipo_cuota
            """;


    // =========================================================
    // CENTRALES DE RIESGO
    // =========================================================

    private static final String SQL_CENTRALES_RIESGO = """
            SELECT
                id_central_riesgo,
                codigo_central,
                documento_central,
                nombre_central,
                descripcion
            FROM cartera.centrales_riesgo
            WHERE activo = true
            ORDER BY
                nombre_central,
                codigo_central
            """;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<LineaCreditoDTO>
            LINEA_CREDITO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    LineaCreditoDTO.class
            );

    private static final BeanPropertyRowMapper<ClasificacionCreditoDTO>
            CLASIFICACION_CREDITO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    ClasificacionCreditoDTO.class
            );

    private static final BeanPropertyRowMapper<DestinoEconomicoDTO>
            DESTINO_ECONOMICO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    DestinoEconomicoDTO.class
            );

    private static final BeanPropertyRowMapper<GarantiaCreditoDTO>
            GARANTIA_CREDITO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    GarantiaCreditoDTO.class
            );

    private static final BeanPropertyRowMapper<SubgarantiaCreditoDTO>
            SUBGARANTIA_CREDITO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SubgarantiaCreditoDTO.class
            );

    private static final BeanPropertyRowMapper<FormaPagoDTO>
            FORMA_PAGO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    FormaPagoDTO.class
            );

    private static final BeanPropertyRowMapper<ModalidadInteresDTO>
            MODALIDAD_INTERES_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    ModalidadInteresDTO.class
            );

    private static final BeanPropertyRowMapper<TipoCuotaDTO>
            TIPO_CUOTA_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    TipoCuotaDTO.class
            );

    private static final BeanPropertyRowMapper<CentralRiesgoDTO>
            CENTRAL_RIESGO_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    CentralRiesgoDTO.class
            );


    // =========================================================
    // DEPENDENCIA
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    public OriginacionCatalogoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // LÍNEAS DE CRÉDITO
    // =========================================================

    public List<LineaCreditoDTO> listarLineasCredito() {

        return jdbc.query(
                SQL_LINEAS_CREDITO,
                EmptySqlParameterSource.INSTANCE,
                LINEA_CREDITO_MAPPER
        );
    }


    // =========================================================
    // CLASIFICACIONES DE CRÉDITO
    // =========================================================

    public List<ClasificacionCreditoDTO> listarClasificacionesCredito() {

        return jdbc.query(
                SQL_CLASIFICACIONES_CREDITO,
                EmptySqlParameterSource.INSTANCE,
                CLASIFICACION_CREDITO_MAPPER
        );
    }


    // =========================================================
    // DESTINOS ECONÓMICOS
    // =========================================================

    public List<DestinoEconomicoDTO> listarDestinosEconomicos() {

        return jdbc.query(
                SQL_DESTINOS_ECONOMICOS,
                EmptySqlParameterSource.INSTANCE,
                DESTINO_ECONOMICO_MAPPER
        );
    }


    // =========================================================
    // GARANTÍAS
    // =========================================================

    public List<GarantiaCreditoDTO> listarGarantiasCreditos() {

        return jdbc.query(
                SQL_GARANTIAS_CREDITOS,
                EmptySqlParameterSource.INSTANCE,
                GARANTIA_CREDITO_MAPPER
        );
    }


    // =========================================================
    // SUBGARANTÍAS
    // =========================================================

    public List<SubgarantiaCreditoDTO> listarSubgarantiasCreditos() {

        return jdbc.query(
                SQL_SUBGARANTIAS_CREDITOS,
                EmptySqlParameterSource.INSTANCE,
                SUBGARANTIA_CREDITO_MAPPER
        );
    }


    // =========================================================
    // FORMAS DE PAGO
    // =========================================================

    public List<FormaPagoDTO> listarFormasPago() {

        return jdbc.query(
                SQL_FORMAS_PAGO,
                EmptySqlParameterSource.INSTANCE,
                FORMA_PAGO_MAPPER
        );
    }


    // =========================================================
    // MODALIDADES DE INTERÉS
    // =========================================================

    public List<ModalidadInteresDTO> listarModalidadesIntereses() {

        return jdbc.query(
                SQL_MODALIDADES_INTERESES,
                EmptySqlParameterSource.INSTANCE,
                MODALIDAD_INTERES_MAPPER
        );
    }


    // =========================================================
    // TIPOS DE CUOTA
    // =========================================================

    public List<TipoCuotaDTO> listarTiposCuotas() {

        return jdbc.query(
                SQL_TIPOS_CUOTAS,
                EmptySqlParameterSource.INSTANCE,
                TIPO_CUOTA_MAPPER
        );
    }


    // =========================================================
    // CENTRALES DE RIESGO
    // =========================================================

    public List<CentralRiesgoDTO> listarCentralesRiesgo() {

        return jdbc.query(
                SQL_CENTRALES_RIESGO,
                EmptySqlParameterSource.INSTANCE,
                CENTRAL_RIESGO_MAPPER
        );
    }
}