package co.assip.erp.depositos.informes.asociados_sin_movimientos;

import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosItemDTO;
import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AsociadosSinMovimientosRepository {

    private final JdbcTemplate jdbc;

    public List<AsociadosSinMovimientosItemDTO> consultar(
            AsociadosSinMovimientosRequestDTO request
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY
                    id_datos_personal
            ),

            ultimos AS (
                SELECT
                    e.id_cuenta_ahorro,
                    MAX(e.fecha_movimiento) AS fecha_ultimo_movimiento
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.fecha_movimiento <= ?::date
                GROUP BY
                    e.id_cuenta_ahorro
            )

            SELECT
                c.id_cuenta_ahorro,
                c.id_datos_personal,

                TRIM(c.codigo_cuenta) AS codigo_cuenta,

                hv.documento,

                CASE
                    WHEN hv.tipo_persona = '2'
                        THEN hv.nombres
                    ELSE TRIM(CONCAT(
                        COALESCE(hv.primer_apellido, ''),
                        ' ',
                        COALESCE(hv.segundo_apellido, ''),
                        ' ',
                        COALESCE(hv.nombres, '')
                    ))
                END AS nombre_completo,

                f.codigo_forma,
                f.nombre_forma,

                a.codigo_agencia,
                a.sigla_agencia AS nombre_agencia,

                c.fecha_apertura_cuenta,

                u.fecha_ultimo_movimiento,

                (
                    ?::date
                    -
                    COALESCE(
                        u.fecha_ultimo_movimiento,
                        c.fecha_apertura_cuenta
                    )
                )::int AS dias_sin_movimiento,

                COALESCE(c.saldo_actual_cuenta, 0) AS saldo_actual,

                c.estado_cuenta_cuenta AS estado_cuenta

            FROM depositos.cuentas_ahorro c

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            INNER JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            LEFT JOIN hv_unica hv
                ON hv.id_datos_personal = c.id_datos_personal

            LEFT JOIN ultimos u
                ON u.id_cuenta_ahorro = c.id_cuenta_ahorro

            WHERE 1 = 1

              AND (
                    ? = 0
                    OR c.id_agencia = ?
                  )

              AND (
                    ? = '0'
                    OR f.codigo_forma = ?
                  )

              AND (
                    ? = 0
                    OR (
                        ? = 1
                        AND COALESCE(c.saldo_actual_cuenta, 0) > 0
                    )
                    OR (
                        ? = 2
                        AND COALESCE(c.saldo_actual_cuenta, 0) = 0
                    )
                  )

              AND COALESCE(c.saldo_actual_cuenta, 0) >= ?

              AND (
                    ?::date
                    -
                    COALESCE(
                        u.fecha_ultimo_movimiento,
                        c.fecha_apertura_cuenta
                    )
                  )::int >= ?

            ORDER BY
                dias_sin_movimiento DESC,
                a.codigo_agencia,
                f.codigo_forma,
                nombre_completo,
                c.codigo_cuenta
            """;

        Integer idAgencia =
                request.getIdAgencia() == null
                        ? 0
                        : request.getIdAgencia();

        String codigoForma =
                request.getCodigoForma() == null
                        || request.getCodigoForma().isBlank()
                        ? "0"
                        : request.getCodigoForma();

        Integer diasMinimos =
                request.getDiasMinimos() == null
                        ? 90
                        : request.getDiasMinimos();

        Integer tipoSaldo =
                request.getTipoSaldo() == null
                        ? 0
                        : request.getTipoSaldo();

        BigDecimal saldoMinimo =
                request.getSaldoMinimo() == null
                        ? BigDecimal.ZERO
                        : request.getSaldoMinimo();

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        AsociadosSinMovimientosItemDTO.builder()
                                .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                                .idDatosPersonal(rs.getInt("id_datos_personal"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .codigoAgencia(rs.getString("codigo_agencia"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .fechaAperturaCuenta(rs.getObject("fecha_apertura_cuenta", java.time.LocalDate.class))
                                .fechaUltimoMovimiento(rs.getObject("fecha_ultimo_movimiento", java.time.LocalDate.class))
                                .diasSinMovimiento(rs.getInt("dias_sin_movimiento"))
                                .saldoActual(rs.getBigDecimal("saldo_actual"))
                                .estadoCuenta(rs.getString("estado_cuenta"))
                                .build(),

                request.getFechaCorte(),
                request.getFechaCorte(),

                idAgencia,
                idAgencia,

                codigoForma,
                codigoForma,

                tipoSaldo,
                tipoSaldo,
                tipoSaldo,

                saldoMinimo,

                request.getFechaCorte(),
                diasMinimos
        );
    }

}