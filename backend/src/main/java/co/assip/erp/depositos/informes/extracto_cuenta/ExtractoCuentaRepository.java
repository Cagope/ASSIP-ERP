package co.assip.erp.depositos.informes.extracto_cuenta;

import co.assip.erp.depositos.informes.extracto_cuenta.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExtractoCuentaRepository {

    private final JdbcTemplate jdbc;

    public List<ExtractoCuentaBusquedaDTO> buscarCuentas(
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
            c.id_cuenta_ahorro,
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
            a.nombre_agencia,

            COALESCE(c.saldo_actual_cuenta, 0) AS saldo_actual

        FROM depositos.cuentas_ahorro c

        INNER JOIN depositos.formas_ahorro f
            ON f.id_forma_ahorro = c.id_forma_ahorro

        INNER JOIN general.datos_agencias a
            ON a.id_agencia = c.id_agencia

        LEFT JOIN hv_unica hv
            ON hv.id_datos_personal = c.id_datos_personal

        WHERE 1 = 1
        """;

        StringBuilder where = new StringBuilder();

        if (documento != null && !documento.isBlank()) {
            where.append("""
            
            AND UPPER(TRIM(hv.documento))
                LIKE '%' || UPPER(TRIM(?)) || '%'
            """);
        }

        if (nombres != null && !nombres.isBlank()) {
            where.append("""
            
            AND UPPER(TRIM(hv.nombres))
                LIKE '%' || UPPER(TRIM(?)) || '%'
            """);
        }

        if (primerApellido != null && !primerApellido.isBlank()) {
            where.append("""
            
            AND UPPER(TRIM(hv.primer_apellido))
                LIKE '%' || UPPER(TRIM(?)) || '%'
            """);
        }

        if (segundoApellido != null && !segundoApellido.isBlank()) {
            where.append("""
            
            AND UPPER(TRIM(hv.segundo_apellido))
                LIKE '%' || UPPER(TRIM(?)) || '%'
            """);
        }

        if (codigoCuenta != null && !codigoCuenta.isBlank()) {
            where.append("""
            
            AND UPPER(TRIM(c.codigo_cuenta))
                LIKE '%' || UPPER(TRIM(?)) || '%'
            """);
        }

        sql += where + """
        
        ORDER BY
            nombre_completo,
            f.codigo_forma,
            codigo_cuenta
        
        LIMIT 100
        """;

        List<Object> params =
                new java.util.ArrayList<>();

        if (documento != null && !documento.isBlank()) {
            params.add(documento);
        }

        if (nombres != null && !nombres.isBlank()) {
            params.add(nombres);
        }

        if (primerApellido != null && !primerApellido.isBlank()) {
            params.add(primerApellido);
        }

        if (segundoApellido != null && !segundoApellido.isBlank()) {
            params.add(segundoApellido);
        }

        if (codigoCuenta != null && !codigoCuenta.isBlank()) {
            params.add(codigoCuenta);
        }

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        ExtractoCuentaBusquedaDTO.builder()
                                .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                                .codigoCuenta(rs.getString("codigo_cuenta"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .codigoForma(rs.getString("codigo_forma"))
                                .nombreForma(rs.getString("nombre_forma"))
                                .nombreAgencia(rs.getString("nombre_agencia"))
                                .saldoActual(rs.getDouble("saldo_actual"))
                                .build(),
                params.toArray()
        );
    }

    public ExtractoCuentaResumenDTO obtenerResumen(ExtractoCuentaRequestDTO request) {

        BigDecimal saldoInicial =
                obtenerSaldoInicial(
                        request.getIdCuentaAhorro(),
                        request.getFechaInicial()
                );

        String sql = """
            SELECT
                c.id_cuenta_ahorro,
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

                hv.direccion_residencia,
                hv.ciudad_residencia,
                hv.departamento_residencia,
                hv.telefono,
                hv.celular_uno,
                hv.correo_personal,

                f.codigo_forma,
                f.nombre_forma,
                a.nombre_agencia,

                COALESCE(SUM(e.valor_credito), 0) AS total_creditos,
                COALESCE(SUM(e.valor_debito), 0) AS total_debitos

            FROM depositos.cuentas_ahorro c
            INNER JOIN depositos.formas_ahorro f
                ON f.id_forma_ahorro = c.id_forma_ahorro
            INNER JOIN general.datos_agencias a
                ON a.id_agencia = c.id_agencia
            LEFT JOIN reporting.vw_hoja_vida_general_total_reciente hv
                ON hv.id_datos_personal = c.id_datos_personal
            LEFT JOIN depositos.extractos_cuentas_ahorros e
                ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                AND e.fecha_movimiento >= ?::date
                AND e.fecha_movimiento <= ?::date

            WHERE c.id_cuenta_ahorro = ?

            GROUP BY
                c.id_cuenta_ahorro,
                c.codigo_cuenta,
                hv.documento,
                hv.tipo_persona,
                hv.nombres,
                hv.primer_apellido,
                hv.segundo_apellido,
                hv.direccion_residencia,
                hv.ciudad_residencia,
                hv.departamento_residencia,
                hv.telefono,
                hv.celular_uno,
                hv.correo_personal,
                f.codigo_forma,
                f.nombre_forma,
                a.nombre_agencia
            """;

        return jdbc.queryForObject(
                sql,
                (rs, rowNum) -> {

                    BigDecimal totalCreditos = rs.getBigDecimal("total_creditos");
                    BigDecimal totalDebitos = rs.getBigDecimal("total_debitos");

                    return ExtractoCuentaResumenDTO.builder()
                            .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                            .codigoCuenta(rs.getString("codigo_cuenta"))
                            .documento(rs.getString("documento"))
                            .nombreCompleto(rs.getString("nombre_completo"))
                            .direccion(rs.getString("direccion_residencia"))
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
                request.getFechaFinal(),
                request.getIdCuentaAhorro()
        );
    }

    public List<ExtractoCuentaMovimientoDTO> obtenerMovimientos(
            ExtractoCuentaRequestDTO request,
            BigDecimal saldoInicial
    ) {

        String sql = """
        SELECT
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
                ? +
                SUM(
                    COALESCE(e.valor_credito, 0)
                    - COALESCE(e.valor_debito, 0)
                ) OVER (
                    ORDER BY
                        e.fecha_movimiento,
                        e.hora_movimiento,
                        e.id_extracto_cuenta_ahorro
                )
            ) AS saldo

        FROM depositos.extractos_cuentas_ahorros e

        LEFT JOIN depositos.tipo_movimiento tm
            ON tm.codigo_movimiento = e.tipo_movimiento

        WHERE e.id_cuenta_ahorro = ?
          AND e.fecha_movimiento >= ?::date
          AND e.fecha_movimiento <= ?::date

        ORDER BY
            e.fecha_movimiento,
            e.hora_movimiento,
            e.id_extracto_cuenta_ahorro
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        ExtractoCuentaMovimientoDTO.builder()
                                .fechaMovimiento(rs.getDate("fecha_movimiento").toLocalDate())
                                .horaMovimiento(rs.getTime("hora_movimiento").toLocalTime())
                                .tipoMovimiento(rs.getString("tipo_movimiento"))
                                .descripcionMovimiento(rs.getString("descripcion_movimiento"))
                                .tipoComprobante(rs.getString("tipo_comprobante"))
                                .numeroComprobante(rs.getString("numero_comprobante"))
                                .debito(rs.getBigDecimal("debito"))
                                .credito(rs.getBigDecimal("credito"))
                                .saldo(rs.getBigDecimal("saldo"))
                                .build(),
                saldoInicial,
                request.getIdCuentaAhorro(),
                request.getFechaInicial(),
                request.getFechaFinal()
        );
    }

    private BigDecimal obtenerSaldoInicial(
            Integer idCuenta,
            String fechaInicial
    ) {

        String sql = """
        SELECT
            COALESCE(c.saldo_inicial_cuenta, 0)
            +
            COALESCE((
                SELECT
                    SUM(COALESCE(e.valor_credito, 0))
                    -
                    SUM(COALESCE(e.valor_debito, 0))
                FROM depositos.extractos_cuentas_ahorros e
                WHERE e.id_cuenta_ahorro = c.id_cuenta_ahorro
                  AND e.fecha_movimiento < ?::date
            ), 0) AS saldo

        FROM depositos.cuentas_ahorro c
        WHERE c.id_cuenta_ahorro = ?
        """;

        return jdbc.queryForObject(
                sql,
                BigDecimal.class,
                fechaInicial,
                idCuenta
        );
    }

}