package co.assip.erp.cajas.ventanilla;

import co.assip.erp.cajas.ventanilla.dto.CajaVentanillaEstadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CajasVentanillaRepository {

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // BLOQUEO DE PROVISIÓN
    // =========================================================

    /**
     * Bloquea la provisión antes de realizar un traslado
     * Caja ↔ Ventanilla.
     *
     * El bloqueo permanece hasta finalizar la transacción
     * del servicio.
     *
     * De esta forma dos solicitudes concurrentes no pueden
     * utilizar simultáneamente el mismo saldo físico.
     */
    public void bloquearProvision(
            Long idProvision
    ) {

        String sql = """
            SELECT
                id_provision
            FROM cajas.provisiones_diarias
            WHERE id_provision = :idProvision
              AND estado = 'ABIERTA'
            FOR UPDATE
            """;

        List<Long> resultado =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idProvision",
                                        idProvision
                                ),
                        (rs, rowNum) ->
                                rs.getLong(
                                        "id_provision"
                                )
                );

        if (resultado.isEmpty()) {
            throw new RuntimeException(
                    "La provisión no existe o la caja "
                            + "ya se encuentra cerrada."
            );
        }
    }


    // =========================================================
    // ESTADO ACTUAL DE CAJA / VENTANILLA
    // =========================================================

    /**
     * Calcula el estado físico actual del efectivo.
     *
     * Solo se consideran movimientos de caja en estado
     * REGISTRADO y medio de pago EFECTIVO.
     *
     * Los traslados Caja ↔ Ventanilla son internos y se
     * obtienen exclusivamente de movimientos_ventanilla.
     */
    public Optional<CajaVentanillaEstadoDTO> obtenerEstado(
            Long idProvision
    ) {

        String sql = """
            SELECT
                p.id_provision,
                p.fecha_contable,
                p.estado AS estado_provision,

                c.id_caja,
                c.id_agencia,
                c.codigo_caja,
                c.descripcion AS descripcion_caja,
                c.tipo_caja,

                COALESCE(
                    c.fondo_sugerido_ventanilla,
                    0
                ) AS fondo_sugerido_ventanilla,

                COALESCE(
                    c.limite_maximo_ventanilla,
                    0
                ) AS limite_maximo_ventanilla,

                COALESCE(
                    p.efectivo_inicio,
                    0
                ) AS efectivo_inicio,

                COALESCE(
                    mc.entradas_efectivo,
                    0
                ) AS entradas_operaciones_efectivo,

                COALESCE(
                    mc.salidas_efectivo,
                    0
                ) AS salidas_operaciones_efectivo,

                COALESCE(
                    mv.caja_a_ventanilla,
                    0
                ) AS caja_a_ventanilla,

                COALESCE(
                    mv.ventanilla_a_caja,
                    0
                ) AS ventanilla_a_caja

            FROM cajas.provisiones_diarias p

            INNER JOIN cajas.cajas c
                    ON c.id_caja = p.id_caja

            LEFT JOIN LATERAL (

                SELECT
                    COALESCE(
                        SUM(
                            CASE
                                WHEN m.estado = 'REGISTRADO'
                                 AND m.naturaleza = 'INGRESO'
                                 AND m.medio_pago = 'EFECTIVO'
                                THEN m.valor
                                ELSE 0
                            END
                        ),
                        0
                    ) AS entradas_efectivo,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN m.estado = 'REGISTRADO'
                                 AND m.naturaleza = 'EGRESO'
                                 AND m.medio_pago = 'EFECTIVO'
                                THEN m.valor
                                ELSE 0
                            END
                        ),
                        0
                    ) AS salidas_efectivo

                FROM cajas.movimientos_caja m

                WHERE m.id_provision =
                      p.id_provision

            ) mc
                ON TRUE

            LEFT JOIN LATERAL (

                SELECT
                    COALESCE(
                        SUM(v.valor_entrada),
                        0
                    ) AS caja_a_ventanilla,

                    COALESCE(
                        SUM(v.valor_salida),
                        0
                    ) AS ventanilla_a_caja

                FROM cajas.movimientos_ventanilla v

                WHERE v.id_provision =
                      p.id_provision

            ) mv
                ON TRUE

            WHERE p.id_provision = :idProvision
              AND c.activa = TRUE
            """;

        List<CajaVentanillaEstadoDTO> resultado =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idProvision",
                                        idProvision
                                ),
                        (rs, rowNum) -> {

                            BigDecimal efectivoInicio =
                                    nvl(
                                            rs.getBigDecimal(
                                                    "efectivo_inicio"
                                            )
                                    );

                            BigDecimal entradasOperaciones =
                                    nvl(
                                            rs.getBigDecimal(
                                                    "entradas_operaciones_efectivo"
                                            )
                                    );

                            BigDecimal salidasOperaciones =
                                    nvl(
                                            rs.getBigDecimal(
                                                    "salidas_operaciones_efectivo"
                                            )
                                    );

                            BigDecimal cajaAVentanilla =
                                    nvl(
                                            rs.getBigDecimal(
                                                    "caja_a_ventanilla"
                                            )
                                    );

                            BigDecimal ventanillaACaja =
                                    nvl(
                                            rs.getBigDecimal(
                                                    "ventanilla_a_caja"
                                            )
                                    );

                            /*
                             * Efectivo físico total de la provisión.
                             *
                             * Los movimientos internos de ventanilla
                             * no alteran este valor.
                             */
                            BigDecimal efectivoTotal =
                                    efectivoInicio
                                            .add(
                                                    entradasOperaciones
                                            )
                                            .subtract(
                                                    salidasOperaciones
                                            );

                            /*
                             * Mismo criterio utilizado actualmente
                             * por el cierre diario.
                             */
                            BigDecimal saldoVentanilla =
                                    cajaAVentanilla
                                            .subtract(
                                                    ventanillaACaja
                                            )
                                            .add(
                                                    entradasOperaciones
                                            )
                                            .subtract(
                                                    salidasOperaciones
                                            );

                            BigDecimal saldoCajaInterna =
                                    efectivoTotal.subtract(
                                            saldoVentanilla
                                    );

                            return CajaVentanillaEstadoDTO
                                    .builder()
                                    .idProvision(
                                            rs.getLong(
                                                    "id_provision"
                                            )
                                    )
                                    .fechaContable(
                                            rs.getObject(
                                                    "fecha_contable",
                                                    java.time.LocalDate.class
                                            )
                                    )
                                    .estadoProvision(
                                            rs.getString(
                                                    "estado_provision"
                                            )
                                    )
                                    .idCaja(
                                            rs.getLong(
                                                    "id_caja"
                                            )
                                    )
                                    .idAgencia(
                                            rs.getInt(
                                                    "id_agencia"
                                            )
                                    )
                                    .codigoCaja(
                                            rs.getString(
                                                    "codigo_caja"
                                            )
                                    )
                                    .descripcionCaja(
                                            rs.getString(
                                                    "descripcion_caja"
                                            )
                                    )
                                    .tipoCaja(
                                            rs.getString(
                                                    "tipo_caja"
                                            )
                                    )
                                    .fondoSugeridoVentanilla(
                                            nvl(
                                                    rs.getBigDecimal(
                                                            "fondo_sugerido_ventanilla"
                                                    )
                                            )
                                    )
                                    .limiteMaximoVentanilla(
                                            nvl(
                                                    rs.getBigDecimal(
                                                            "limite_maximo_ventanilla"
                                                    )
                                            )
                                    )
                                    .efectivoInicio(
                                            efectivoInicio
                                    )
                                    .entradasOperacionesEfectivo(
                                            entradasOperaciones
                                    )
                                    .salidasOperacionesEfectivo(
                                            salidasOperaciones
                                    )
                                    .efectivoTotal(
                                            efectivoTotal
                                    )
                                    .cajaAVentanilla(
                                            cajaAVentanilla
                                    )
                                    .ventanillaACaja(
                                            ventanillaACaja
                                    )
                                    .saldoVentanilla(
                                            saldoVentanilla
                                    )
                                    .saldoCajaInterna(
                                            saldoCajaInterna
                                    )
                                    .build();
                        }
                );

        return resultado
                .stream()
                .findFirst();
    }


    // =========================================================
    // CAJA → VENTANILLA
    // =========================================================

    /**
     * Registra únicamente el movimiento físico interno.
     *
     * NO genera:
     *
     * - movimientos_caja
     * - comprobante
     * - contabilización
     */
    public Long registrarCajaAVentanilla(
            Long idProvision,
            BigDecimal valor,
            String observacion,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cajas.movimientos_ventanilla (
                id_provision,
                valor_entrada,
                valor_salida,
                observacion,
                fk_seguridad_creacion
            )
            VALUES (
                :idProvision,
                :valor,
                0,
                :observacion,
                :idUsuario
            )
            RETURNING id_movimiento_ventanilla
            """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idProvision",
                                idProvision
                        )
                        .addValue(
                                "valor",
                                valor
                        )
                        .addValue(
                                "observacion",
                                observacion
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        ),
                Long.class
        );
    }


    // =========================================================
    // VENTANILLA → CAJA
    // =========================================================

    /**
     * Registra únicamente el movimiento físico interno.
     *
     * valor_salida representa efectivo que sale de
     * Ventanilla y regresa a la caja interna.
     */
    public Long registrarVentanillaACaja(
            Long idProvision,
            BigDecimal valor,
            String observacion,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cajas.movimientos_ventanilla (
                id_provision,
                valor_entrada,
                valor_salida,
                observacion,
                fk_seguridad_creacion
            )
            VALUES (
                :idProvision,
                0,
                :valor,
                :observacion,
                :idUsuario
            )
            RETURNING id_movimiento_ventanilla
            """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idProvision",
                                idProvision
                        )
                        .addValue(
                                "valor",
                                valor
                        )
                        .addValue(
                                "observacion",
                                observacion
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        ),
                Long.class
        );
    }


    // =========================================================
    // UTILIDADES
    // =========================================================

    private BigDecimal nvl(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }
}