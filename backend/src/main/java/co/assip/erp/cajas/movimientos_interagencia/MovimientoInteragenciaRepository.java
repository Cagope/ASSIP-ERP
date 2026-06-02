package co.assip.erp.cajas.movimientos_interagencia;

import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaCuentaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovimientoInteragenciaRepository {

    private final JdbcTemplate jdbcTemplate;

    public Long obtenerProvisionAbierta(
            Long idCaja,
            LocalDate fechaContable
    ) {
        String sql = """
            SELECT id_provision
            FROM cajas.provisiones_diarias
            WHERE id_caja = ?
              AND fecha_contable = ?
              AND estado = 'ABIERTA'
            LIMIT 1
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_provision"),
                idCaja,
                fechaContable
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public Long obtenerDepartamentoDepositos() {
        String sql = """
            SELECT id_departamento_operativo
            FROM cajas.departamentos_operativos
            WHERE codigo_departamento = '00'
              AND estado = 'A'
            LIMIT 1
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_departamento_operativo")
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public Long obtenerOperacionCaja(
            String codigoOperacion
    ) {
        String sql = """
            SELECT id_operacion_caja
            FROM cajas.operaciones_caja
            WHERE codigo_operacion = ?
              AND estado = 'A'
            LIMIT 1
        """;

        List<Long> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_operacion_caja"),
                codigoOperacion
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public String obtenerNaturalezaOperacion(
            String codigoOperacion
    ) {
        String sql = """
            SELECT naturaleza
            FROM cajas.operaciones_caja
            WHERE codigo_operacion = ?
              AND estado = 'A'
            LIMIT 1
        """;

        List<String> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("naturaleza"),
                codigoOperacion
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public List<MovimientoInteragenciaCuentaDTO> buscarCuentas(
            Integer idAgenciaCaja,
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido
    ) {

        String sql = baseCuentaSql() + """
            WHERE ca.id_agencia <> ?
              AND (CAST(? AS text) IS NULL OR hv.documento ILIKE '%' || CAST(? AS text) || '%')
              AND (CAST(? AS text) IS NULL OR hv.nombres ILIKE '%' || CAST(? AS text) || '%')
              AND (CAST(? AS text) IS NULL OR hv.primer_apellido ILIKE '%' || CAST(? AS text) || '%')
              AND (CAST(? AS text) IS NULL OR hv.segundo_apellido ILIKE '%' || CAST(? AS text) || '%')
            ORDER BY
                ag.codigo_agencia,
                hv.primer_apellido,
                hv.segundo_apellido,
                hv.nombres,
                fa.codigo_forma,
                ca.codigo_cuenta
            LIMIT 50
        """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapCuenta(rs),
                idAgenciaCaja,
                clean(documento), clean(documento),
                clean(nombres), clean(nombres),
                clean(primerApellido), clean(primerApellido),
                clean(segundoApellido), clean(segundoApellido)
        );
    }

    public MovimientoInteragenciaCuentaDTO obtenerCuenta(
            Long idCuentaAhorro
    ) {

        String sql = baseCuentaSql() + """
            WHERE ca.id_cuenta_ahorro = ?
        """;

        List<MovimientoInteragenciaCuentaDTO> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapCuenta(rs),
                idCuentaAhorro
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    private String baseCuentaSql() {
        return """
            SELECT
                ca.id_cuenta_ahorro,
                ca.id_agencia,
                ag.codigo_agencia,
                ag.nombre_agencia,

                ca.codigo_cuenta,
                ca.id_datos_personal,

                hv.tipo_documento AS tipo_documento,
                hv.documento AS documento,

                CASE
                    WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                    ELSE TRIM(
                        COALESCE(hv.primer_apellido, '') || ' ' ||
                        COALESCE(hv.segundo_apellido, '') || ' ' ||
                        COALESCE(hv.nombres, '')
                    )
                END AS nombre_asociado,

                ca.id_forma_ahorro,
                fa.codigo_forma,
                fa.nombre_forma,

                COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_actual,
                COALESCE(canje.valor_en_canje, 0) AS valor_canje,

                ca.estado_cuenta_cuenta AS estado_cuenta,

                ea.descripcion_estado_ahorro AS descripcion_estado_cuenta,

                ca.gmf_cuenta_cuenta AS gmf_cuenta,

                ca.cuenta_conjunta AS cuenta_conjunta_real,

                cj.conjuntos,

                pca.documento_poder,
                pca.nombre_poder,
                pca.telefono_poder,
                pca.celular_poder,

                ca.fecha_apertura_cuenta,

                COALESCE(ea.operativo, false) AS estado_operativo,

                CASE
                    WHEN COALESCE(ea.operativo, false) = false THEN
                        'Cuenta no operativa: ' ||
                        COALESCE(
                            ea.descripcion_estado_ahorro,
                            ca.estado_cuenta_cuenta
                        )
                    ELSE
                        'Cuenta disponible para transaccionar'
                END AS mensaje_operativo,

                hv.firma_uno,
                hv.firma_dos,

                ds.tipo_documento_soporte,
                ds.numero_inicial AS numero_inicial_libreta,
                ds.numero_final AS numero_final_libreta

            FROM depositos.cuentas_ahorro ca

            LEFT JOIN general.datos_agencias ag
                ON ag.id_agencia = ca.id_agencia

            LEFT JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro

            LEFT JOIN depositos.estados_ahorros ea
                ON TRIM(ea.codigo_estado_ahorro)
                 = TRIM(ca.estado_cuenta_cuenta)

            LEFT JOIN (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    tipo_documento,
                    documento,
                    tipo_persona,
                    nombres,
                    primer_apellido,
                    segundo_apellido,
                    firma_uno,
                    firma_dos
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ) hv
                ON hv.id_datos_personal = ca.id_datos_personal

            LEFT JOIN LATERAL (
                SELECT
                    STRING_AGG(
                        TRIM(hvc.documento) || ' - ' ||
                        CASE
                            WHEN hvc.tipo_persona = '2'
                                THEN COALESCE(hvc.nombres, '')
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
                        id_datos_personal,
                        documento,
                        tipo_persona,
                        nombres,
                        primer_apellido,
                        segundo_apellido
                    FROM reporting.vw_hoja_vida_general_total_reciente
                    ORDER BY id_datos_personal
                ) hvc
                    ON hvc.id_datos_personal = cjc.id_datos_personal
                WHERE cjc.id_cuenta_ahorro = ca.id_cuenta_ahorro
            ) cj ON true

            LEFT JOIN LATERAL (
                SELECT
                    COALESCE(SUM(
                        COALESCE(cc.valor_canje, 0)
                        - COALESCE(cc.valor_liberado, 0)
                    ), 0) AS valor_en_canje
                FROM depositos.canjes_cuentas_ahorros cc
                WHERE cc.id_cuenta_ahorro = ca.id_cuenta_ahorro
                  AND TRIM(COALESCE(cc.estado_canje, '')) = 'A'
            ) canje ON true

            LEFT JOIN LATERAL (
                SELECT
                    ds.tipo_documento_soporte,
                    ds.numero_inicial,
                    ds.numero_final
                FROM depositos.documentos_soporte ds
                WHERE ds.id_cuenta_ahorro = ca.id_cuenta_ahorro
                  AND ds.estado_documento = 'A'
                ORDER BY ds.id_documento_soporte DESC
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
        """;
    }

    public Long insertarMovimientoCaja(
            Long idProvision,
            Long idCaja,
            Long idDepartamento,
            Long idOperacion,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            String tipoDocumento,
            String numeroDocumento,
            String codigoReferencia,
            String naturaleza,
            String medioPago,
            BigDecimal valor,
            String concepto,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO cajas.movimientos_caja (
                id_provision,
                id_caja,
                fecha_contable,
                id_departamento_operativo,
                id_operacion_caja,
                modulo_origen,
                proceso_origen,
                tabla_origen,
                id_origen,
                tipo_documento,
                numero_documento,
                codigo_referencia,
                naturaleza,
                medio_pago,
                valor,
                concepto,
                estado,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            SELECT
                ?,
                ?,
                p.fecha_contable,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                'REGISTRADO',
                ?,
                ?
            FROM cajas.provisiones_diarias p
            WHERE p.id_provision = ?
            RETURNING id_movimiento_caja
        """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                idProvision,
                idCaja,
                idDepartamento,
                idOperacion,
                moduloOrigen,
                procesoOrigen,
                tablaOrigen,
                idOrigen,
                tipoDocumento,
                numeroDocumento,
                codigoReferencia,
                naturaleza,
                medioPago,
                valor,
                concepto,
                idUsuario,
                idUsuario,
                idProvision
        );
    }

    public void insertarChequeRecibido(
            Long idMovimientoCaja,
            Long idCaja,
            String codigoBanco,
            String numeroCheque,
            BigDecimal valorCheque,
            String numeroDocumento,
            String codigoReferencia,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            String observacion,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cajas.cheques_recibidos (
                id_movimiento_caja,
                id_caja,
                fecha_recibido,
                codigo_banco,
                numero_cheque,
                valor_cheque,
                estado_cheque,
                fecha_estado,
                numero_documento,
                codigo_referencia,
                modulo_origen,
                proceso_origen,
                tabla_origen,
                id_origen,
                observacion,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                ?,
                ?,
                CURRENT_DATE,
                ?,
                ?,
                ?,
                'RECIBIDO',
                CURRENT_DATE,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
        """;

        jdbcTemplate.update(
                sql,
                idMovimientoCaja,
                idCaja,
                codigoBanco,
                numeroCheque,
                valorCheque,
                numeroDocumento,
                codigoReferencia,
                moduloOrigen,
                procesoOrigen,
                tablaOrigen,
                idOrigen,
                observacion,
                idUsuario,
                idUsuario
        );
    }

    public void insertarCanjeCuentaAhorro(
            Long idCuentaAhorro,
            LocalDate fechaIngreso,
            BigDecimal valorCanje,
            String numeroCheque,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO depositos.canjes_cuentas_ahorros (
                id_cuenta_ahorro,
                fecha_ingreso,
                valor_canje,
                numero_cheque,
                estado_canje,
                valor_liberado,
                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            )
            VALUES (
                ?,
                ?,
                ?,
                ?,
                'A',
                0,
                ?,
                CURRENT_TIMESTAMP,
                ?,
                CURRENT_TIMESTAMP
            )
        """;

        jdbcTemplate.update(
                sql,
                idCuentaAhorro,
                fechaIngreso,
                valorCanje,
                numeroCheque,
                idUsuario,
                idUsuario
        );
    }

    public boolean validarDocumentoSoporteCuenta(
            Long idCuentaAhorro,
            String numeroComprobante
    ) {
        String sql = """
            SELECT COUNT(1)
            FROM depositos.documentos_soporte
            WHERE id_cuenta_ahorro = ?
              AND estado_documento = 'A'
              AND CAST(? AS BIGINT)
                  BETWEEN CAST(numero_inicial AS BIGINT)
                      AND CAST(numero_final AS BIGINT)
        """;

        Integer cantidad = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idCuentaAhorro,
                numeroComprobante
        );

        return cantidad != null && cantidad > 0;
    }

    public boolean existeDocumentoSoporteUsado(
            Long idCuentaAhorro,
            String tipoComprobante,
            String numeroComprobante
    ) {
        String sql = """
            SELECT COUNT(1)
            FROM depositos.extractos_cuentas_ahorros
            WHERE id_cuenta_ahorro = ?
              AND TRIM(tipo_comprobante) = TRIM(?)
              AND TRIM(numero_comprobante) = TRIM(?)
        """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idCuentaAhorro,
                tipoComprobante,
                numeroComprobante
        );

        return count != null && count > 0;
    }

    private MovimientoInteragenciaCuentaDTO mapCuenta(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        BigDecimal saldoActual =
                nvl(rs.getBigDecimal("saldo_actual"));

        BigDecimal valorCanje =
                nvl(rs.getBigDecimal("valor_canje"));

        return MovimientoInteragenciaCuentaDTO.builder()
                .idCuentaAhorro(rs.getLong("id_cuenta_ahorro"))
                .idAgenciaCuenta(rs.getInt("id_agencia"))
                .codigoAgenciaCuenta(rs.getString("codigo_agencia"))
                .nombreAgenciaCuenta(rs.getString("nombre_agencia"))
                .codigoCuenta(rs.getString("codigo_cuenta"))
                .idDatosPersonal(rs.getLong("id_datos_personal"))
                .tipoDocumento(rs.getString("tipo_documento"))
                .documento(rs.getString("documento"))
                .nombreAsociado(rs.getString("nombre_asociado"))
                .idFormaAhorro(rs.getInt("id_forma_ahorro"))
                .codigoForma(rs.getString("codigo_forma"))
                .nombreForma(rs.getString("nombre_forma"))
                .saldoActual(saldoActual)
                .valorCanje(valorCanje)
                .saldoDisponible(saldoActual.subtract(valorCanje))
                .estadoCuenta(rs.getString("estado_cuenta"))
                .estadoOperativo(rs.getBoolean("estado_operativo"))
                .mensajeOperativo(rs.getString("mensaje_operativo"))
                .build();
    }

    private String clean(
            String value
    ) {
        return value == null || value.trim().isEmpty()
                ? null
                : value.trim();
    }

    private BigDecimal nvl(
            BigDecimal value
    ) {
        return value == null ? BigDecimal.ZERO : value;
    }
}