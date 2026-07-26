package co.assip.erp.gerencia.expedienteasociado.repository;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteAfiliacionDTO;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ExpedienteAfiliacionRepository {

    private static final String PARAM_ID_DATOS_PERSONAL =
            "idDatosPersonal";

    private final NamedParameterJdbcTemplate jdbc;

    private static final BeanPropertyRowMapper<ExpedienteAfiliacionDTO>
            AFILIACION_MAPPER =
            crearMapper(ExpedienteAfiliacionDTO.class);

    public ExpedienteAfiliacionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // SQL: afiliación integral
    // =========================================================

    /**
     * Construye la información de afiliación tomando como registro
     * principal la cuenta de Aportes Sociales.
     *
     * Reglas:
     *
     * 1. La cuenta de aportes se identifica mediante:
     *      codigo_tipo_captacion = '1'
     *
     * 2. Cuando exista más de una cuenta de aportes se selecciona:
     *      - primero la operativamente activa;
     *      - después la de apertura más reciente;
     *      - finalmente la de mayor identificador.
     *
     * 3. El estado, la fecha de afiliación y la agencia se toman
     *    de la cuenta de aportes seleccionada.
     *
     * 4. Si la persona existe, pero no tiene cuenta de aportes,
     *    se devuelve igualmente el DTO con la información básica
     *    de Hoja de Vida y los datos de aportes en null o cero.
     */
    private static final String SQL_AFILIACION = """
            WITH persona AS (
                SELECT
                    h.id_datos_personal,
                    h.tipo_documento,
                    h.nombre_tipo_documento,
                    h.documento,
                    h.nombres,
                    h.primer_apellido,
                    h.segundo_apellido,

                    trim(
                        concat_ws(
                            ' ',
                            h.nombres,
                            h.primer_apellido,
                            h.segundo_apellido
                        )
                    ) AS nombre_completo,

                    h.fecha_actualizacion,
                    h.fecha_creacion_datos,
                    h.fecha_edicion_datos

                FROM reporting.vw_hoja_vida_general_total h

                WHERE h.id_datos_personal = :idDatosPersonal

                LIMIT 1
            ),

            ultimo_movimiento AS (
                SELECT
                    e.id_cuenta_ahorro,
                    max(e.fecha_movimiento)::date
                        AS fecha_ultimo_movimiento

                FROM depositos.extractos_cuentas_ahorros e

                GROUP BY
                    e.id_cuenta_ahorro
            ),

            cuenta_aportes AS (
                SELECT
                    a.id_cuenta_ahorro,
                    a.id_datos_personal,

                    trim(a.codigo_cuenta)
                        AS codigo_cuenta,

                    a.codigo_tipo_captacion,
                    a.nombre_tipo_captacion,

                    a.codigo_estado_cuenta,
                    a.nombre_estado_cuenta,

                    COALESCE(
                        a.estado_operativo,
                        false
                    ) AS estado_operativo,

                    a.fecha_apertura_cuenta,

                    COALESCE(
                        a.cuota_mensual_cuenta,
                        0
                    )::numeric AS cuota_mensual_cuenta,

                    COALESCE(
                        a.saldo_actual_cuenta,
                        0
                    )::numeric AS saldo_actual_cuenta,

                    a.id_agencia,
                    a.codigo_agencia,
                    a.nombre_agencia,

                    um.fecha_ultimo_movimiento

                FROM reporting.vw_depositos_cuentas_ahorro_integral a

                LEFT JOIN ultimo_movimiento um
                       ON um.id_cuenta_ahorro = a.id_cuenta_ahorro

                WHERE a.id_datos_personal = :idDatosPersonal
                  AND trim(a.codigo_tipo_captacion) = '1'

                ORDER BY
                    COALESCE(a.estado_operativo, false) DESC,
                    a.fecha_apertura_cuenta DESC NULLS LAST,
                    a.id_cuenta_ahorro DESC

                LIMIT 1
            )

            SELECT
                -- =================================================
                -- Identificación
                -- =================================================

                p.id_datos_personal,
                p.tipo_documento,
                p.nombre_tipo_documento,
                p.documento,
                p.nombres,
                p.primer_apellido,
                p.segundo_apellido,
                p.nombre_completo,

                -- =================================================
                -- Identificación de la afiliación
                -- =================================================

                ca.id_cuenta_ahorro::bigint
                    AS id_afiliacion,

                ca.codigo_cuenta
                    AS codigo_asociado,

                ca.codigo_cuenta
                    AS numero_afiliacion,

                ca.fecha_apertura_cuenta
                    AS fecha_afiliacion,

                ca.fecha_apertura_cuenta
                    AS fecha_ingreso,

                ca.fecha_apertura_cuenta
                    AS fecha_antiguedad,

                -- =================================================
                -- Estado de la afiliación
                -- =================================================

                ca.codigo_estado_cuenta
                    AS codigo_estado_asociado,

                ca.nombre_estado_cuenta
                    AS nombre_estado_asociado,

                ca.codigo_estado_cuenta
                    AS codigo_estado_afiliacion,

                ca.nombre_estado_cuenta
                    AS nombre_estado_afiliacion,

                COALESCE(
                    ca.estado_operativo,
                    false
                ) AS afiliacion_activa,

                -- =================================================
                -- Agencia de afiliación
                -- =================================================

                ca.id_agencia
                    AS id_agencia_afiliacion,

                ca.codigo_agencia
                    AS codigo_agencia_afiliacion,

                ca.nombre_agencia
                    AS nombre_agencia_afiliacion,

                -- =================================================
                -- Agencia actual
                -- =================================================

                ca.id_agencia
                    AS id_agencia_actual,

                ca.codigo_agencia
                    AS codigo_agencia_actual,

                ca.nombre_agencia
                    AS nombre_agencia_actual,

                -- =================================================
                -- Cuenta de aportes
                -- =================================================

                ca.id_cuenta_ahorro::bigint
                    AS id_cuenta_aportes,

                ca.codigo_cuenta
                    AS codigo_cuenta_aportes,

                ca.codigo_tipo_captacion
                    AS codigo_forma_aportes,

                ca.nombre_tipo_captacion
                    AS nombre_forma_aportes,

                ca.fecha_apertura_cuenta
                    AS fecha_apertura_aportes,

                COALESCE(
                    ca.cuota_mensual_cuenta,
                    0
                )::numeric AS cuota_aportes,

                COALESCE(
                    ca.saldo_actual_cuenta,
                    0
                )::numeric AS saldo_aportes,

                COALESCE(
                    ca.saldo_actual_cuenta,
                    0
                )::numeric AS saldo_disponible_aportes,

                ca.codigo_estado_cuenta
                    AS codigo_estado_cuenta_aportes,

                ca.nombre_estado_cuenta
                    AS nombre_estado_cuenta_aportes,

                COALESCE(
                    ca.estado_operativo,
                    false
                ) AS cuenta_aportes_activa,

                -- =================================================
                -- Antigüedad
                -- =================================================

                CASE
                    WHEN ca.fecha_apertura_cuenta IS NULL
                        THEN 0

                    ELSE (
                        current_date
                        - ca.fecha_apertura_cuenta
                    )::integer
                END AS antiguedad_dias,

                CASE
                    WHEN ca.fecha_apertura_cuenta IS NULL
                        THEN 0

                    ELSE (
                        extract(
                            year FROM age(
                                current_date,
                                ca.fecha_apertura_cuenta
                            )
                        ) * 12
                        +
                        extract(
                            month FROM age(
                                current_date,
                                ca.fecha_apertura_cuenta
                            )
                        )
                    )::integer
                END AS antiguedad_meses,

                CASE
                    WHEN ca.fecha_apertura_cuenta IS NULL
                        THEN 0

                    ELSE extract(
                        year FROM age(
                            current_date,
                            ca.fecha_apertura_cuenta
                        )
                    )::integer
                END AS antiguedad_anios,

                ca.fecha_ultimo_movimiento
                    AS fecha_ultimo_aporte,

                ca.fecha_ultimo_movimiento
                    AS fecha_ultimo_movimiento_aportes,

                -- =================================================
                -- Retiro
                -- =================================================

                (
                    ca.id_cuenta_ahorro IS NOT NULL
                    AND COALESCE(ca.estado_operativo, false) = false
                ) AS retirado,

                -- =================================================
                -- Actualización de información
                -- =================================================

                p.fecha_actualizacion
                    AS fecha_ultima_actualizacion,

                CASE
                    WHEN p.fecha_actualizacion IS NULL
                        THEN 0

                    ELSE (
                        current_date
                        - p.fecha_actualizacion
                    )::integer
                END AS dias_sin_actualizar,

                CASE
                    WHEN p.fecha_actualizacion IS NULL
                        THEN false

                    WHEN (
                        current_date
                        - p.fecha_actualizacion
                    ) <= 365
                        THEN true

                    ELSE false
                END AS informacion_actualizada,

                -- =================================================
                -- Auditoría disponible
                -- =================================================

                p.fecha_creacion_datos::date
                    AS fecha_creacion,

                p.fecha_edicion_datos::date
                    AS fecha_edicion

            FROM persona p

            LEFT JOIN cuenta_aportes ca
                   ON ca.id_datos_personal = p.id_datos_personal
            """;

    // =========================================================
    // Consulta pública
    // =========================================================

    public Optional<ExpedienteAfiliacionDTO> obtenerAfiliacion(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        try {
            ExpedienteAfiliacionDTO afiliacion =
                    jdbc.queryForObject(
                            SQL_AFILIACION,
                            parametros(idDatosPersonal),
                            AFILIACION_MAPPER
                    );

            if (afiliacion == null) {
                return Optional.empty();
            }

            completarValoresCalculados(afiliacion);

            return Optional.of(afiliacion);

        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    // =========================================================
    // Complementos controlados
    // =========================================================

    /**
     * Completa únicamente valores derivados que no requieren
     * reglas institucionales externas.
     *
     * No se calculan aquí:
     *
     * - asociado hábil;
     * - aporte mínimo;
     * - reciprocidad;
     * - derechos políticos;
     * - documentación completa.
     *
     * Esos valores deberán calcularse cuando sus reglas y fuentes
     * institucionales estén formalmente definidas.
     */
    private void completarValoresCalculados(
            ExpedienteAfiliacionDTO afiliacion
    ) {
        if (afiliacion.getSaldoAportes() == null) {
            afiliacion.setSaldoAportes(
                    java.math.BigDecimal.ZERO
            );
        }

        if (afiliacion.getCuotaAportes() == null) {
            afiliacion.setCuotaAportes(
                    java.math.BigDecimal.ZERO
            );
        }

        if (afiliacion.getValorReciprocidad() == null) {
            afiliacion.setValorReciprocidad(
                    java.math.BigDecimal.ZERO
            );
        }

        afiliacion.calcularSaldoDisponibleAportes();
    }

    // =========================================================
    // Parámetros
    // =========================================================

    private MapSqlParameterSource parametros(
            Long idDatosPersonal
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        PARAM_ID_DATOS_PERSONAL,
                        idDatosPersonal
                );
    }

    // =========================================================
    // RowMapper
    // =========================================================

    private static <T> BeanPropertyRowMapper<T> crearMapper(
            Class<T> tipo
    ) {
        BeanPropertyRowMapper<T> mapper =
                BeanPropertyRowMapper.newInstance(tipo);

        mapper.setCheckFullyPopulated(false);
        mapper.setPrimitivesDefaultedForNullValue(true);

        return mapper;
    }

    // =========================================================
    // Validaciones
    // =========================================================

    private void validarIdDatosPersonal(
            Long idDatosPersonal
    ) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException(
                    "El idDatosPersonal debe ser mayor que cero."
            );
        }
    }
}