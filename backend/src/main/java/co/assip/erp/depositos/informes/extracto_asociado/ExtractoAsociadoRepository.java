package co.assip.erp.depositos.informes.extracto_asociado;

import co.assip.erp.depositos.informes.extracto_asociado.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExtractoAsociadoRepository {

    private final JdbcTemplate jdbc;

    public List<ExtractoAsociadoBusquedaDTO> buscarAsociados(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String codigoCuenta
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY
                    id_datos_personal
            )

            SELECT
                hv.id_datos_personal,
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

                hv.direccion_residencia AS direccion,

                COUNT(DISTINCT c.id_cuenta_ahorro) AS total_cuentas,

                COALESCE(SUM(c.saldo_actual_cuenta), 0) AS saldo_total

            FROM hv_unica hv

            INNER JOIN depositos.cuentas_ahorro c
                ON c.id_datos_personal = hv.id_datos_personal

            WHERE 1 = 1
            """;

        StringBuilder where =
                new StringBuilder();

        List<Object> params =
                new ArrayList<>();

        if (documento != null && !documento.isBlank()) {
            where.append("""
                
                AND UPPER(TRIM(hv.documento))
                    LIKE '%' || UPPER(TRIM(?)) || '%'
                """);
            params.add(documento);
        }

        if (nombres != null && !nombres.isBlank()) {
            where.append("""
                
                AND UPPER(TRIM(hv.nombres))
                    LIKE '%' || UPPER(TRIM(?)) || '%'
                """);
            params.add(nombres);
        }

        if (primerApellido != null && !primerApellido.isBlank()) {
            where.append("""
                
                AND UPPER(TRIM(hv.primer_apellido))
                    LIKE '%' || UPPER(TRIM(?)) || '%'
                """);
            params.add(primerApellido);
        }

        if (segundoApellido != null && !segundoApellido.isBlank()) {
            where.append("""
                
                AND UPPER(TRIM(hv.segundo_apellido))
                    LIKE '%' || UPPER(TRIM(?)) || '%'
                """);
            params.add(segundoApellido);
        }

        if (codigoCuenta != null && !codigoCuenta.isBlank()) {
            where.append("""
                
                AND UPPER(TRIM(c.codigo_cuenta))
                    LIKE '%' || UPPER(TRIM(?)) || '%'
                """);
            params.add(codigoCuenta);
        }

        sql += where + """
            
            GROUP BY
                hv.id_datos_personal,
                hv.documento,
                hv.tipo_persona,
                hv.nombres,
                hv.primer_apellido,
                hv.segundo_apellido,
                hv.direccion_residencia

            ORDER BY
                nombre_completo

            LIMIT 100
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        ExtractoAsociadoBusquedaDTO.builder()
                                .idDatosPersonal(rs.getInt("id_datos_personal"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .direccion(rs.getString("direccion"))
                                .totalCuentas(rs.getInt("total_cuentas"))
                                .saldoTotal(rs.getBigDecimal("saldo_total"))
                                .build(),
                params.toArray()
        );
    }

    public ExtractoAsociadoResumenDTO obtenerResumen(
            ExtractoAsociadoRequestDTO request
    ) {

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY
                    id_datos_personal
            ),

            cuentas AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.id_datos_personal,
                    c.id_forma_ahorro,

                    COALESCE((
                        SELECT
                            SUM(COALESCE(ei.valor_credito, 0))
                            -
                            SUM(COALESCE(ei.valor_debito, 0))
                        FROM depositos.extractos_cuentas_ahorros ei
                        WHERE ei.id_cuenta_ahorro = c.id_cuenta_ahorro
                          AND ei.fecha_movimiento < ?::date
                    ), 0) AS saldo_inicial,

                    COALESCE((
                        SELECT SUM(COALESCE(er.valor_credito, 0))
                        FROM depositos.extractos_cuentas_ahorros er
                        WHERE er.id_cuenta_ahorro = c.id_cuenta_ahorro
                          AND er.fecha_movimiento >= ?::date
                          AND er.fecha_movimiento <= ?::date
                    ), 0) AS total_creditos,

                    COALESCE((
                        SELECT SUM(COALESCE(er.valor_debito, 0))
                        FROM depositos.extractos_cuentas_ahorros er
                        WHERE er.id_cuenta_ahorro = c.id_cuenta_ahorro
                          AND er.fecha_movimiento >= ?::date
                          AND er.fecha_movimiento <= ?::date
                    ), 0) AS total_debitos

                FROM depositos.cuentas_ahorro c

                INNER JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.id_forma_ahorro

                WHERE c.id_datos_personal = ?

                  AND (
                        ? = '0'
                        OR f.codigo_forma = ?
                      )
            )

            SELECT
                hv.id_datos_personal,
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

                hv.direccion_residencia AS direccion,

                COUNT(c.id_cuenta_ahorro) AS total_cuentas,

                COALESCE(SUM(c.saldo_inicial), 0) AS saldo_inicial,
                COALESCE(SUM(c.total_creditos), 0) AS total_creditos,
                COALESCE(SUM(c.total_debitos), 0) AS total_debitos,

                COALESCE(SUM(
                    c.saldo_inicial
                    + c.total_creditos
                    - c.total_debitos
                ), 0) AS saldo_final

            FROM hv_unica hv

            LEFT JOIN cuentas c
                ON c.id_datos_personal = hv.id_datos_personal

            WHERE hv.id_datos_personal = ?

            GROUP BY
                hv.id_datos_personal,
                hv.documento,
                hv.tipo_persona,
                hv.nombres,
                hv.primer_apellido,
                hv.segundo_apellido,
                hv.direccion_residencia
            """;

        String codigoForma =
                normalizarCodigoForma(
                        request.getCodigoForma()
                );

        return jdbc.queryForObject(
                sql,
                (rs, rowNum) ->
                        ExtractoAsociadoResumenDTO.builder()
                                .idDatosPersonal(rs.getInt("id_datos_personal"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .direccion(rs.getString("direccion"))
                                .totalCuentas(rs.getInt("total_cuentas"))
                                .saldoInicial(rs.getBigDecimal("saldo_inicial"))
                                .totalCreditos(rs.getBigDecimal("total_creditos"))
                                .totalDebitos(rs.getBigDecimal("total_debitos"))
                                .saldoFinal(rs.getBigDecimal("saldo_final"))
                                .build(),

                request.getFechaInicial(),

                request.getFechaInicial(),
                request.getFechaFinal(),

                request.getFechaInicial(),
                request.getFechaFinal(),

                request.getIdDatosPersonal(),

                codigoForma,
                codigoForma,

                request.getIdDatosPersonal()
        );
    }

    public List<ExtractoAsociadoCuentaDTO> obtenerCuentas(
            ExtractoAsociadoRequestDTO request
    ) {

        String sql = """
            SELECT
                c.id_cuenta_ahorro,
                TRIM(c.codigo_cuenta) AS codigo_cuenta,

                f.codigo_forma,
                f.nombre_forma,

                a.sigla_agencia AS nombre_agencia,

                COALESCE((
                    SELECT
                        SUM(COALESCE(ei.valor_credito, 0))
                        -
                        SUM(COALESCE(ei.valor_debito, 0))
                    FROM depositos.extractos_cuentas_ahorros ei
                    WHERE ei.id_cuenta_ahorro = c.id_cuenta_ahorro
                      AND ei.fecha_movimiento < ?::date
                ), 0) AS saldo_inicial,

                COALESCE((
                    SELECT SUM(COALESCE(er.valor_credito, 0))
                    FROM depositos.extractos_cuentas_ahorros er
                    WHERE er.id_cuenta_ahorro = c.id_cuenta_ahorro
                      AND er.fecha_movimiento >= ?::date
                      AND er.fecha_movimiento <= ?::date
                ), 0) AS total_creditos,

                COALESCE((
                    SELECT SUM(COALESCE(er.valor_debito, 0))
                    FROM depositos.extractos_cuentas_ahorros er
                    WHERE er.id_cuenta_ahorro = c.id_cuenta_ahorro
                      AND er.fecha_movimiento >= ?::date
                      AND er.fecha_movimiento <= ?::date
                ), 0) AS total_debitos

            FROM depositos.cuentas_ahorro c

            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro

            INNER JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia

            WHERE c.id_datos_personal = ?

              AND (
                    ? = '0'
                    OR f.codigo_forma = ?
                  )

            ORDER BY
                f.codigo_forma,
                c.codigo_cuenta
            """;

        String codigoForma =
                normalizarCodigoForma(
                        request.getCodigoForma()
                );

        return jdbc.query(
                sql,
                (rs, rowNum) -> {

                    BigDecimal saldoInicial =
                            rs.getBigDecimal("saldo_inicial");

                    BigDecimal totalCreditos =
                            rs.getBigDecimal("total_creditos");

                    BigDecimal totalDebitos =
                            rs.getBigDecimal("total_debitos");

                    return ExtractoAsociadoCuentaDTO.builder()
                            .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                            .codigoCuenta(rs.getString("codigo_cuenta"))
                            .codigoForma(rs.getString("codigo_forma"))
                            .nombreForma(rs.getString("nombre_forma"))
                            .nombreAgencia(rs.getString("nombre_agencia"))
                            .saldoInicial(saldoInicial)
                            .totalCreditos(totalCreditos)
                            .totalDebitos(totalDebitos)
                            .saldoFinal(
                                    saldoInicial
                                            .add(totalCreditos)
                                            .subtract(totalDebitos)
                            )
                            .build();
                },

                request.getFechaInicial(),

                request.getFechaInicial(),
                request.getFechaFinal(),

                request.getFechaInicial(),
                request.getFechaFinal(),

                request.getIdDatosPersonal(),

                codigoForma,
                codigoForma
        );
    }

    public List<ExtractoAsociadoMovimientoDTO> obtenerMovimientos(
            ExtractoAsociadoRequestDTO request
    ) {

        String sql = """
            WITH cuentas AS (
                SELECT
                    c.id_cuenta_ahorro,
                    TRIM(c.codigo_cuenta) AS codigo_cuenta,

                    f.codigo_forma,
                    f.nombre_forma,

                    a.sigla_agencia AS nombre_agencia,

                    COALESCE((
                        SELECT
                            SUM(COALESCE(ei.valor_credito, 0))
                            -
                            SUM(COALESCE(ei.valor_debito, 0))
                        FROM depositos.extractos_cuentas_ahorros ei
                        WHERE ei.id_cuenta_ahorro = c.id_cuenta_ahorro
                          AND ei.fecha_movimiento < ?::date
                    ), 0) AS saldo_inicial

                FROM depositos.cuentas_ahorro c

                INNER JOIN depositos.formas_ahorro f
                    ON f.id_forma_ahorro = c.id_forma_ahorro

                INNER JOIN general.datos_agencias a
                    ON a.id_agencia = c.id_agencia

                WHERE c.id_datos_personal = ?

                  AND (
                        ? = '0'
                        OR f.codigo_forma = ?
                      )
            )

            SELECT
                ct.id_cuenta_ahorro,
                ct.codigo_cuenta,
                ct.codigo_forma,
                ct.nombre_forma,
                ct.nombre_agencia,

                e.fecha_movimiento,
                e.hora_movimiento,

                e.tipo_movimiento,
                tm.descripcion AS descripcion_movimiento,

                TRIM(COALESCE(e.tipo_comprobante, '')) AS tipo_comprobante,

                LPAD(
                    TRIM(COALESCE(e.numero_comprobante, '')),
                    8,
                    '0'
                ) AS numero_comprobante,

                COALESCE(e.valor_debito, 0) AS debito,
                COALESCE(e.valor_credito, 0) AS credito,

                (
                    ct.saldo_inicial
                    +
                    SUM(
                        COALESCE(e.valor_credito, 0)
                        -
                        COALESCE(e.valor_debito, 0)
                    ) OVER (
                        PARTITION BY ct.id_cuenta_ahorro
                        ORDER BY
                            e.fecha_movimiento,
                            e.hora_movimiento,
                            e.id_extracto_cuenta_ahorro
                    )
                ) AS saldo_cuenta

            FROM cuentas ct

            INNER JOIN depositos.extractos_cuentas_ahorros e
                ON e.id_cuenta_ahorro = ct.id_cuenta_ahorro
                AND e.fecha_movimiento >= ?::date
                AND e.fecha_movimiento <= ?::date

            LEFT JOIN depositos.tipo_movimiento tm
                ON tm.codigo_movimiento = e.tipo_movimiento

            ORDER BY
                e.fecha_movimiento,
                e.hora_movimiento,
                ct.codigo_forma,
                ct.codigo_cuenta,
                e.id_extracto_cuenta_ahorro
            """;

        String codigoForma =
                normalizarCodigoForma(
                        request.getCodigoForma()
                );

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        ExtractoAsociadoMovimientoDTO.builder()
                                .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .fechaMovimiento(rs.getObject("fecha_movimiento", java.time.LocalDate.class))
                                .horaMovimiento(rs.getObject("hora_movimiento", java.time.LocalTime.class))
                                .tipoMovimiento(rs.getString("tipo_movimiento"))
                                .descripcionMovimiento(rs.getString("descripcion_movimiento"))
                                .tipoComprobante(rs.getString("tipo_comprobante"))
                                .numeroComprobante(rs.getString("numero_comprobante"))
                                .debito(rs.getBigDecimal("debito"))
                                .credito(rs.getBigDecimal("credito"))
                                .saldoCuenta(rs.getBigDecimal("saldo_cuenta"))
                                .build(),

                request.getFechaInicial(),

                request.getIdDatosPersonal(),

                codigoForma,
                codigoForma,

                request.getFechaInicial(),
                request.getFechaFinal()
        );
    }

    private String normalizarCodigoForma(
            String codigoForma
    ) {

        if (codigoForma == null || codigoForma.isBlank()) {
            return "0";
        }

        return codigoForma;
    }

}