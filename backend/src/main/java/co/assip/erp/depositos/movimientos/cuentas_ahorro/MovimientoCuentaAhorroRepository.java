package co.assip.erp.depositos.movimientos.cuentasahorro;

import co.assip.erp.depositos.movimientos.cuentasahorro.dto.CuentaMovimientoDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.TipoMovimientoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.MovimientoCuentaRequestDTO;

@Repository
@RequiredArgsConstructor
public class MovimientoCuentaAhorroRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<CuentaMovimientoDTO> buscarCuentas(
            Integer idAgencia,
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido
    ) {
        String sql = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,
                ca.id_forma_ahorro,
                fa.codigo_forma,
                fa.nombre_forma,
                ca.codigo_cuenta,
                ca.id_datos_personal,
                hv.documento,
                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_asociado,
                ca.saldo_actual_cuenta,
                ca.estado_cuenta_cuenta,
                ea.descripcion_estado_ahorro,
                COALESCE(ea.operativo, false) AS estado_operativo,
                ca.cuenta_activa,
                ca.gmf_cuenta_cuenta,
                ca.fecha_apertura_cuenta,

                ds.tipo_documento_soporte,
                ds.numero_inicial,
                ds.numero_final,

                pca.documento_poder,
                pca.nombre_poder,
                pca.telefono_poder,
                pca.celular_poder,

                COALESCE(cj.cuenta_conjunta, false) AS cuenta_conjunta_real,
                cj.conjuntos,

                COALESCE(canje.valor_en_canje, 0) AS valor_en_canje,

                CASE
                    WHEN COALESCE(ea.operativo, false) = false THEN
                        'Cuenta no operativa: ' || COALESCE(ea.descripcion_estado_ahorro, ca.estado_cuenta_cuenta)
                    WHEN COALESCE(ca.saldo_actual_cuenta, 0) <= 0 THEN
                        'Cuenta sin saldo disponible'
                    ELSE
                        'Cuenta disponible para transaccionar'
                END AS mensaje_operativo

            FROM depositos.cuentas_ahorro ca

            INNER JOIN depositos.formas_ahorro fa
                    ON fa.id_forma_ahorro = ca.id_forma_ahorro

            INNER JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                    ON hv.id_datos_personal = ca.id_datos_personal

            LEFT JOIN general.datos_agencias ag
                   ON ag.id_agencia = ca.id_agencia

            LEFT JOIN depositos.estados_ahorros ea
                   ON TRIM(ea.codigo_estado_ahorro) = TRIM(ca.estado_cuenta_cuenta)

            LEFT JOIN LATERAL (
                SELECT
                    ds.tipo_documento_soporte,
                    ds.numero_inicial,
                    ds.numero_final
                FROM depositos.documentos_soporte ds
                WHERE ds.id_cuenta_ahorro = ca.id_cuenta_ahorro
                  AND TRIM(COALESCE(ds.estado_documento, '')) = 'A'
                ORDER BY ds.fecha_entrega DESC,
                         ds.id_documento_soporte DESC
                LIMIT 1
            ) ds ON true

            LEFT JOIN LATERAL (
                SELECT
                    pca.documento_poder,
                    pca.nombre_poder,
                    pca.telefono_poder,
                    pca.celular_poder
                FROM depositos.poderes_cuentas_ahorro pca
                WHERE pca.id_cuenta_ahorro = ca.id_cuenta_ahorro
                ORDER BY pca.id_poder DESC
                LIMIT 1
            ) pca ON true

            LEFT JOIN LATERAL (
                SELECT
                    true AS cuenta_conjunta,
                    STRING_AGG(
                        TRIM(hvc.documento) || ' - ' ||
                        CASE
                            WHEN hvc.tipo_persona = '2' THEN COALESCE(hvc.nombres, '')
                            ELSE TRIM(
                                COALESCE(hvc.primer_apellido, '') || ' ' ||
                                COALESCE(hvc.segundo_apellido, '') || ' ' ||
                                COALESCE(hvc.nombres, '')
                            )
                        END,
                        ' / '
                        ORDER BY hvc.documento
                    ) AS conjuntos
                FROM depositos.cuentas_ahorro_conjuntas cjc
                INNER JOIN (
                    SELECT DISTINCT ON (id_datos_personal)
                        *
                    FROM reporting.vw_hoja_vida_general_total_reciente
                    ORDER BY id_datos_personal
                ) hvc
                        ON hvc.id_datos_personal = cjc.id_datos_personal
                WHERE cjc.id_cuenta_ahorro = ca.id_cuenta_ahorro
            ) cj ON true

            LEFT JOIN LATERAL (
                SELECT
                    COALESCE(SUM(
                        COALESCE(cc.valor_canje, 0) - COALESCE(cc.valor_liberado, 0)
                    ), 0) AS valor_en_canje
                FROM depositos.canjes_cuentas_ahorros cc
                WHERE cc.id_cuenta_ahorro = ca.id_cuenta_ahorro
                  AND TRIM(COALESCE(cc.estado_canje, '')) = 'A'
            ) canje ON true

            WHERE ca.id_agencia = :idAgencia
              AND (
                    CAST(:documento AS text) IS NULL
                    OR hv.documento ILIKE '%' || CAST(:documento AS text) || '%'
                  )
              AND (
                    CAST(:nombres AS text) IS NULL
                    OR hv.nombres ILIKE '%' || CAST(:nombres AS text) || '%'
                  )
              AND (
                    CAST(:primerApellido AS text) IS NULL
                    OR hv.primer_apellido ILIKE '%' || CAST(:primerApellido AS text) || '%'
                  )
              AND (
                    CAST(:segundoApellido AS text) IS NULL
                    OR hv.segundo_apellido ILIKE '%' || CAST(:segundoApellido AS text) || '%'
                  )

            ORDER BY
                CASE WHEN COALESCE(ea.operativo, false) = true THEN 0 ELSE 1 END,
                hv.primer_apellido,
                hv.segundo_apellido,
                hv.nombres,
                fa.codigo_forma,
                ca.codigo_cuenta
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("documento", blankToNull(documento))
                .addValue("nombres", blankToNull(nombres))
                .addValue("primerApellido", blankToNull(primerApellido))
                .addValue("segundoApellido", blankToNull(segundoApellido));

        return jdbc.query(sql, params, (rs, rowNum) -> mapCuenta(rs));
    }

    public CuentaMovimientoDTO obtenerCuenta(Integer idCuentaAhorro) {
        String sql = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,
                ca.id_forma_ahorro,
                fa.codigo_forma,
                fa.nombre_forma,
                ca.codigo_cuenta,
                ca.id_datos_personal,
                hv.documento,
                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_asociado,
                ca.saldo_actual_cuenta,
                ca.estado_cuenta_cuenta,
                ea.descripcion_estado_ahorro,
                COALESCE(ea.operativo, false) AS estado_operativo,
                ca.cuenta_activa,
                ca.gmf_cuenta_cuenta,
                ca.fecha_apertura_cuenta,

                ds.tipo_documento_soporte,
                ds.numero_inicial,
                ds.numero_final,

                pca.documento_poder,
                pca.nombre_poder,
                pca.telefono_poder,
                pca.celular_poder,

                COALESCE(cj.cuenta_conjunta, false) AS cuenta_conjunta_real,
                cj.conjuntos,

                COALESCE(canje.valor_en_canje, 0) AS valor_en_canje,

                CASE
                    WHEN COALESCE(ea.operativo, false) = false THEN
                        'Cuenta no operativa: ' || COALESCE(ea.descripcion_estado_ahorro, ca.estado_cuenta_cuenta)
                    WHEN COALESCE(ca.saldo_actual_cuenta, 0) <= 0 THEN
                        'Cuenta sin saldo disponible'
                    ELSE
                        'Cuenta disponible para transaccionar'
                END AS mensaje_operativo

            FROM depositos.cuentas_ahorro ca

            INNER JOIN depositos.formas_ahorro fa
                    ON fa.id_forma_ahorro = ca.id_forma_ahorro

            INNER JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    *
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                    ON hv.id_datos_personal = ca.id_datos_personal

            LEFT JOIN general.datos_agencias ag
                   ON ag.id_agencia = ca.id_agencia

            LEFT JOIN depositos.estados_ahorros ea
                   ON TRIM(ea.codigo_estado_ahorro) = TRIM(ca.estado_cuenta_cuenta)

            LEFT JOIN LATERAL (
                SELECT
                    ds.tipo_documento_soporte,
                    ds.numero_inicial,
                    ds.numero_final
                FROM depositos.documentos_soporte ds
                WHERE ds.id_cuenta_ahorro = ca.id_cuenta_ahorro
                  AND TRIM(COALESCE(ds.estado_documento, '')) = 'A'
                ORDER BY ds.fecha_entrega DESC,
                         ds.id_documento_soporte DESC
                LIMIT 1
            ) ds ON true

            LEFT JOIN LATERAL (
                SELECT
                    pca.documento_poder,
                    pca.nombre_poder,
                    pca.telefono_poder,
                    pca.celular_poder
                FROM depositos.poderes_cuentas_ahorro pca
                WHERE pca.id_cuenta_ahorro = ca.id_cuenta_ahorro
                ORDER BY pca.id_poder DESC
                LIMIT 1
            ) pca ON true

            LEFT JOIN LATERAL (
                SELECT
                    true AS cuenta_conjunta,
                    STRING_AGG(
                        TRIM(hvc.documento) || ' - ' ||
                        CASE
                            WHEN hvc.tipo_persona = '2' THEN COALESCE(hvc.nombres, '')
                            ELSE TRIM(
                                COALESCE(hvc.primer_apellido, '') || ' ' ||
                                COALESCE(hvc.segundo_apellido, '') || ' ' ||
                                COALESCE(hvc.nombres, '')
                            )
                        END,
                        ' / '
                        ORDER BY hvc.documento
                    ) AS conjuntos
                FROM depositos.cuentas_ahorro_conjuntas cjc
                INNER JOIN (
                    SELECT DISTINCT ON (id_datos_personal)
                        *
                    FROM reporting.vw_hoja_vida_general_total_reciente
                    ORDER BY id_datos_personal
                ) hvc
                        ON hvc.id_datos_personal = cjc.id_datos_personal
                WHERE cjc.id_cuenta_ahorro = ca.id_cuenta_ahorro
            ) cj ON true

            LEFT JOIN LATERAL (
                SELECT
                    COALESCE(SUM(
                        COALESCE(cc.valor_canje, 0) - COALESCE(cc.valor_liberado, 0)
                    ), 0) AS valor_en_canje
                FROM depositos.canjes_cuentas_ahorros cc
                WHERE cc.id_cuenta_ahorro = ca.id_cuenta_ahorro
                  AND TRIM(COALESCE(cc.estado_canje, '')) = 'A'
            ) canje ON true

            WHERE ca.id_cuenta_ahorro = :idCuentaAhorro
        """;

        List<CuentaMovimientoDTO> lista = jdbc.query(
                sql,
                new MapSqlParameterSource("idCuentaAhorro", idCuentaAhorro),
                (rs, rowNum) -> mapCuenta(rs)
        );

        return lista.isEmpty() ? null : lista.get(0);
    }

    public List<TipoMovimientoDTO> listarTiposMovimiento() {
        String sql = """
            SELECT
                codigo_movimiento,
                descripcion,
                accion_movimiento,
                contabilizacion_diaria,
                genera_gmf,
                permite_inclusion_manual
            FROM depositos.tipo_movimiento
            WHERE COALESCE(permite_inclusion_manual, false) = true
            ORDER BY codigo_movimiento
        """;

        return jdbc.query(sql, (rs, rowNum) -> mapTipoMovimiento(rs));
    }

    public TipoMovimientoDTO obtenerTipoMovimiento(String codigoMovimiento) {
        String sql = """
            SELECT
                codigo_movimiento,
                descripcion,
                accion_movimiento,
                contabilizacion_diaria,
                genera_gmf,
                permite_inclusion_manual
            FROM depositos.tipo_movimiento
            WHERE TRIM(codigo_movimiento) = TRIM(:codigoMovimiento)
              AND COALESCE(permite_inclusion_manual, false) = true
        """;

        List<TipoMovimientoDTO> lista = jdbc.query(
                sql,
                new MapSqlParameterSource("codigoMovimiento", codigoMovimiento),
                (rs, rowNum) -> mapTipoMovimiento(rs)
        );

        return lista.isEmpty() ? null : lista.get(0);
    }

    private CuentaMovimientoDTO mapCuenta(java.sql.ResultSet rs) throws java.sql.SQLException {
        return CuentaMovimientoDTO.builder()
                .idCuentaAhorro(rs.getInt("id_cuenta_ahorro"))
                .idAgencia(rs.getInt("id_agencia"))
                .codigoAgencia(rs.getString("codigo_agencia"))
                .nombreAgencia(rs.getString("nombre_agencia"))
                .idFormaAhorro(rs.getInt("id_forma_ahorro"))
                .codigoForma(rs.getString("codigo_forma"))
                .nombreForma(rs.getString("nombre_forma"))
                .codigoCuenta(rs.getString("codigo_cuenta"))
                .idDatosPersonal(rs.getInt("id_datos_personal"))
                .documento(rs.getString("documento"))
                .nombreAsociado(rs.getString("nombre_asociado"))
                .saldoActualCuenta(nvl(rs.getBigDecimal("saldo_actual_cuenta")))
                .estadoCuenta(rs.getString("estado_cuenta_cuenta"))
                .descripcionEstadoCuenta(rs.getString("descripcion_estado_ahorro"))
                .estadoOperativo(rs.getBoolean("estado_operativo"))
                .cuentaActiva(rs.getString("cuenta_activa"))
                .gmfCuenta(rs.getString("gmf_cuenta_cuenta"))
                .tipoDocumentoSoporte(rs.getString("tipo_documento_soporte"))
                .numeroInicialLibreta(rs.getString("numero_inicial"))
                .numeroFinalLibreta(rs.getString("numero_final"))
                .documentoPoder(rs.getString("documento_poder"))
                .nombrePoder(rs.getString("nombre_poder"))
                .telefonoPoder(rs.getString("telefono_poder"))
                .celularPoder(rs.getString("celular_poder"))
                .cuentaConjuntaReal(rs.getBoolean("cuenta_conjunta_real"))
                .conjuntos(rs.getString("conjuntos"))
                .valorEnCanje(nvl(rs.getBigDecimal("valor_en_canje")))
                .mensajeOperativo(rs.getString("mensaje_operativo"))
                .fechaAperturaCuenta(rs.getDate("fecha_apertura_cuenta") == null
                        ? null
                        : rs.getDate("fecha_apertura_cuenta").toLocalDate())
                .build();
    }

    private TipoMovimientoDTO mapTipoMovimiento(java.sql.ResultSet rs) throws java.sql.SQLException {
        return TipoMovimientoDTO.builder()
                .codigoMovimiento(rs.getString("codigo_movimiento"))
                .descripcion(rs.getString("descripcion"))
                .accionMovimiento(rs.getString("accion_movimiento"))
                .contabilizacionDiaria(rs.getBoolean("contabilizacion_diaria"))
                .generaGmf(rs.getBoolean("genera_gmf"))
                .permiteInclusionManual(rs.getBoolean("permite_inclusion_manual"))
                .build();
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public boolean existeMovimientoIgual(
            MovimientoCuentaRequestDTO request,
            BigDecimal valorDebito,
            BigDecimal valorCredito
    ) {
        String sql = """
        SELECT COUNT(1)
        FROM depositos.extractos_cuentas_ahorros
        WHERE id_cuenta_ahorro = :idCuentaAhorro
          AND fecha_movimiento = :fechaMovimiento
          AND TRIM(tipo_movimiento) = TRIM(:tipoMovimiento)
          AND TRIM(tipo_comprobante) = TRIM(:tipoComprobante)
          AND TRIM(numero_comprobante) = TRIM(:numeroComprobante)
          AND COALESCE(valor_debito, 0) = COALESCE(:valorDebito, 0)
          AND COALESCE(valor_credito, 0) = COALESCE(:valorCredito, 0)
    """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCuentaAhorro", request.getIdCuentaAhorro())
                        .addValue("fechaMovimiento", request.getFechaMovimiento())
                        .addValue("tipoMovimiento", request.getTipoMovimiento())
                        .addValue("tipoComprobante", request.getTipoComprobante())
                        .addValue("numeroComprobante", request.getNumeroComprobante())
                        .addValue("valorDebito", valorDebito)
                        .addValue("valorCredito", valorCredito),
                Integer.class
        );

        return count != null && count > 0;
    }

}