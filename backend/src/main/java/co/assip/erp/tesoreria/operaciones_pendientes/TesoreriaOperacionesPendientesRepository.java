
package co.assip.erp.tesoreria.operaciones_pendientes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class TesoreriaOperacionesPendientesRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Publica una operación financiera pendiente.
     *
     * Si ya existe una operación con la misma referencia de origen,
     * comprueba que sus datos sean idénticos antes de devolver su ID.
     *
     * No modifica operaciones existentes.
     */
    public Long publicar(
            Integer idAgencia,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            Integer idDatosPersonal,
            String naturaleza,
            LocalDate fechaDocumento,
            String concepto,
            BigDecimal valorOriginal,
            Integer idUsuario
    ) {
        return publicar(idAgencia, moduloOrigen, procesoOrigen, tablaOrigen,
                idOrigen, idDatosPersonal, naturaleza, fechaDocumento,
                concepto, valorOriginal, idUsuario, null, null);
    }

    /**
     * Publica una obligación con su comprobante y cuenta CxP históricos.
     * Valida agencia y crédito original, y no sobrescribe publicaciones previas.
     */
    public Long publicar(
            Integer idAgencia,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            Integer idDatosPersonal,
            String naturaleza,
            LocalDate fechaDocumento,
            String concepto,
            BigDecimal valorOriginal,
            Integer idUsuario,
            Integer idOrigenComprobante,
            Integer idCatalogoCuentaCxp
    ) {
        return publicar(idAgencia, moduloOrigen, procesoOrigen, tablaOrigen,
                idOrigen, idDatosPersonal, naturaleza, fechaDocumento,
                concepto, valorOriginal, idUsuario, idOrigenComprobante,
                idCatalogoCuentaCxp, null);
    }

    /**
     * Publica una obligación con su configuración financiera identificada.
     * Conserva las validaciones contables y la idempotencia del origen.
     */
    public Long publicar(
            Integer idAgencia,
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen,
            Integer idDatosPersonal,
            String naturaleza,
            LocalDate fechaDocumento,
            String concepto,
            BigDecimal valorOriginal,
            Integer idUsuario,
            Integer idOrigenComprobante,
            Integer idCatalogoCuentaCxp,
            Long idTipoOperacion
    ) {
        if (idTipoOperacion != null && idTipoOperacion <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de configuración financiera no es válido.");
        }
        if ((idOrigenComprobante == null) != (idCatalogoCuentaCxp == null)) {
            throw new IllegalArgumentException(
                    "El comprobante original y la cuenta CxP deben informarse juntos.");
        }
        if (idOrigenComprobante != null) {
            validarRespaldoContable(idAgencia, idOrigenComprobante, idCatalogoCuentaCxp);
        }

        String sql = """
                INSERT INTO tesoreria.operaciones_pendientes (
                    id_agencia,
                    modulo_origen,
                    proceso_origen,
                    tabla_origen,
                    id_origen,
                    id_datos_personal,
                    naturaleza,
                    fecha_documento,
                    concepto,
                    valor_original,
                    fk_seguridad_creacion,
                    id_origen_comprobante,
                    id_catalogo_cuenta_cxp,
                    id_tipo_operacion
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (
                    modulo_origen,
                    proceso_origen,
                    tabla_origen,
                    id_origen
                )
                DO NOTHING
                RETURNING id_operacion_pendiente
                """;

        List<Long> insertados = jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        rs.getLong("id_operacion_pendiente"),
                idAgencia,
                moduloOrigen,
                procesoOrigen,
                tablaOrigen,
                idOrigen,
                idDatosPersonal,
                naturaleza,
                Date.valueOf(fechaDocumento),
                concepto,
                valorOriginal,
                idUsuario,
                idOrigenComprobante,
                idCatalogoCuentaCxp,
                idTipoOperacion
        );

        if (!insertados.isEmpty()) {
            return insertados.get(0);
        }

        Map<String, Object> existente = buscarPorOrigen(
                moduloOrigen,
                procesoOrigen,
                tablaOrigen,
                idOrigen
        );

        if (existente == null) {
            throw new IllegalStateException(
                    "No fue posible recuperar la operación financiera existente."
            );
        }

        validarCoincidencia(
                existente,
                idAgencia,
                idDatosPersonal,
                naturaleza,
                fechaDocumento,
                concepto,
                valorOriginal,
                idOrigenComprobante,
                idCatalogoCuentaCxp,
                idTipoOperacion
        );

        return ((Number) existente.get(
                "id_operacion_pendiente"
        )).longValue();
    }


    /**
     * Publicación estricta: un origen de reversión solamente puede publicarse una vez.
     * ON CONFLICT evita duplicaciones concurrentes sin abortar la transacción.
     */
    public Long publicarEstricto(
            Integer idAgencia, String moduloOrigen, String procesoOrigen,
            String tablaOrigen, Long idOrigen, Integer idDatosPersonal,
            String naturaleza, LocalDate fechaDocumento, String concepto,
            BigDecimal valorOriginal, Integer idUsuario, Long idTipoOperacion
    ) {
        String sql = """
                INSERT INTO tesoreria.operaciones_pendientes (
                    id_agencia, modulo_origen, proceso_origen, tabla_origen,
                    id_origen, id_datos_personal, naturaleza, fecha_documento,
                    concepto, valor_original, fk_seguridad_creacion, id_tipo_operacion
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (modulo_origen, proceso_origen, tabla_origen, id_origen)
                DO NOTHING
                RETURNING id_operacion_pendiente
                """;
        List<Long> insertados = jdbcTemplate.queryForList(
                sql, Long.class, idAgencia, moduloOrigen, procesoOrigen,
                tablaOrigen, idOrigen, idDatosPersonal, naturaleza,
                Date.valueOf(fechaDocumento), concepto, valorOriginal,
                idUsuario, idTipoOperacion
        );
        if (insertados.size() != 1) {
            throw new IllegalStateException(
                    "Ya existe una operación financiera para este origen de reversión.");
        }
        return insertados.get(0);
    }

    /**
     * Busca la operación completa por su referencia única.
     */
    public Map<String, Object> buscarPorOrigen(
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen
    ) {

        String sql = """
                SELECT
                    id_operacion_pendiente,
                    id_agencia,
                    id_datos_personal,
                    naturaleza,
                    fecha_documento,
                    concepto,
                    valor_original,
                    valor_ejecutado,
                    estado,
                    id_origen_comprobante,
                    id_catalogo_cuenta_cxp,
                    id_tipo_operacion
                FROM tesoreria.operaciones_pendientes
                WHERE modulo_origen = ?
                  AND proceso_origen = ?
                  AND tabla_origen = ?
                  AND id_origen = ?
                """;

        List<Map<String, Object>> resultados =
                jdbcTemplate.queryForList(
                        sql,
                        moduloOrigen,
                        procesoOrigen,
                        tablaOrigen,
                        idOrigen
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }

    /**
     * Obtiene el identificador por referencia de origen.
     */
    public Long buscarIdPorOrigen(
            String moduloOrigen,
            String procesoOrigen,
            String tablaOrigen,
            Long idOrigen
    ) {

        Map<String, Object> operacion = buscarPorOrigen(
                moduloOrigen,
                procesoOrigen,
                tablaOrigen,
                idOrigen
        );

        if (operacion == null) {
            return null;
        }

        return ((Number) operacion.get(
                "id_operacion_pendiente"
        )).longValue();
    }

    /**
     * Valida que una publicación repetida corresponda
     * exactamente a la misma obligación.
     */
    private void validarCoincidencia(
            Map<String, Object> existente,
            Integer idAgencia,
            Integer idDatosPersonal,
            String naturaleza,
            LocalDate fechaDocumento,
            String concepto,
            BigDecimal valorOriginal,
            Integer idOrigenComprobante,
            Integer idCatalogoCuentaCxp,
            Long idTipoOperacion
    ) {

        Number agenciaExistente =
                (Number) existente.get("id_agencia");

        Number terceroExistente =
                (Number) existente.get("id_datos_personal");

        BigDecimal valorExistente =
                (BigDecimal) existente.get("valor_original");

        Object fechaExistente =
                existente.get("fecha_documento");

        LocalDate fechaRegistrada =
                fechaExistente instanceof LocalDate fecha
                        ? fecha
                        : ((Date) fechaExistente).toLocalDate();

        boolean coincide =
                agenciaExistente != null
                        && agenciaExistente.intValue() == idAgencia
                        && Objects.equals(
                        terceroExistente == null
                                ? null
                                : terceroExistente.intValue(),
                        idDatosPersonal
                )
                        && Objects.equals(
                        existente.get("naturaleza"),
                        naturaleza
                )
                        && Objects.equals(
                        fechaRegistrada,
                        fechaDocumento
                )
                        && Objects.equals(
                        existente.get("concepto"),
                        concepto
                )
                        && valorExistente != null
                        && valorExistente.compareTo(valorOriginal) == 0
                        && (idOrigenComprobante == null || Objects.equals(
                        numeroEntero(existente.get("id_origen_comprobante")), idOrigenComprobante))
                        && (idCatalogoCuentaCxp == null || Objects.equals(
                        numeroEntero(existente.get("id_catalogo_cuenta_cxp")), idCatalogoCuentaCxp))
                        && (idTipoOperacion == null
                        || numeroLargo(existente.get("id_tipo_operacion")) == null
                        || Objects.equals(numeroLargo(existente.get("id_tipo_operacion")), idTipoOperacion));

        if (!coincide) {
            throw new IllegalStateException(
                    "La operación financiera ya existe, "
                            + "pero sus datos no coinciden con "
                            + "la publicación original."
            );
        }
    }

    /** Convierte una referencia JDBC opcional a Integer para validar idempotencia. */
    private Integer numeroEntero(Object valor) {
        return valor == null ? null : ((Number) valor).intValue();
    }

    /** Convierte una referencia BIGINT opcional para comprobar la configuración publicada. */
    private Long numeroLargo(Object valor) {
        return valor == null ? null : ((Number) valor).longValue();
    }

    /**
     * Comprueba que la cuenta sea de la agencia y esté acreditada en el
     * comprobante histórico indicado; no consulta parámetros actuales.
     */
    private void validarRespaldoContable(Integer idAgencia,
                                         Integer idOrigenComprobante,
                                         Integer idCatalogoCuentaCxp) {
        Integer cantidad = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM contabilidad.origen_comprobantes oc
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ?
                 AND cc.id_agencia = oc.id_agencia
                JOIN contabilidad.auxiliares_contables ac
                  ON ac.id_agencia = oc.id_agencia
                 AND ac.tipo_comprobante = oc.tipo_comprobante
                 AND ac.numero_comprobante = oc.numero_comprobante
                 AND ac.id_catalogo_cuenta = cc.id_catalogo_cuenta
                WHERE oc.id_origen_comprobante = ?
                  AND oc.id_agencia = ?
                  AND ac.valor_credito > 0
                """, Integer.class, idCatalogoCuentaCxp,
                idOrigenComprobante, idAgencia);
        if (cantidad == null || cantidad == 0) {
            throw new IllegalStateException(
                    "La cuenta por pagar no está acreditada en el comprobante original de la agencia.");
        }
    }

    /**
     * Lista las operaciones pendientes y parcialmente
     * ejecutadas de una agencia.
     */
    public List<Map<String, Object>> listarDisponibles(
            Integer idAgencia
    ) {

        String sql = """
                SELECT
                    op.id_operacion_pendiente,
                    op.id_agencia,
                    op.modulo_origen,
                    op.proceso_origen,
                    op.tabla_origen,
                    op.id_origen,
                    op.id_datos_personal,
                    NULLIF(TRIM(CONCAT_WS(' ', dp.nombres, dp.primer_apellido, dp.segundo_apellido)), '') AS nombre_tercero,
                    op.naturaleza,
                    op.fecha_documento,
                    op.concepto,
                    op.valor_original,
                    op.valor_ejecutado,
                    (
                        op.valor_original
                        - op.valor_ejecutado
                    ) AS saldo_pendiente,
                    op.estado,
                    op.fecha_creacion
                FROM tesoreria.operaciones_pendientes op
                LEFT JOIN hoja_vida.datos_personales dp
                    ON dp.id_datos_personal = op.id_datos_personal
                WHERE op.id_agencia = ?
                  AND op.estado IN (
                      'PENDIENTE',
                      'PARCIAL'
                  )
                ORDER BY
                    op.fecha_creacion,
                    op.id_operacion_pendiente
                """;

        return jdbcTemplate.queryForList(
                sql,
                idAgencia
        );
    }


    /**
     * Consulta las cuentas bancarias habilitadas para
     * realizar pagos desde una agencia.
     *
     * La autorización corresponde a la relación
     * cuentas_bancarias_agencias y no a la agencia
     * titular de la cuenta bancaria.
     *
     * No consulta saldos ni ejecuta pagos.
     */
    /** Lista las cuentas bancarias operables de la agencia para recibir recaudos. */
    public List<Map<String, Object>> listarCuentasParaRecaudo(Integer idAgencia) {
        return jdbcTemplate.queryForList("""
                SELECT cb.id_cuenta_bancaria, cba.id_agencia, b.nombre_banco,
                       cb.numero_cuenta, cb.nombre_cuenta, cba.id_cuenta_contable,
                       cc.codigo_cuenta, cc.nombre_cuenta AS nombre_cuenta_contable
                FROM tesoreria.cuentas_bancarias_agencias cba
                JOIN tesoreria.cuentas_bancarias cb ON cb.id_cuenta_bancaria = cba.id_cuenta_bancaria
                JOIN tesoreria.bancos b ON b.codigo_banco = cb.codigo_banco
                JOIN contabilidad.catalogo_cuentas cc ON cc.id_catalogo_cuenta = cba.id_cuenta_contable
                WHERE cba.id_agencia = ? AND cba.activo = TRUE AND cb.activo = TRUE
                  AND cb.estado_cuenta = 'ACTIVA' AND b.activo = TRUE
                  AND cc.id_agencia = cba.id_agencia AND cc.cuenta_operable = TRUE
                ORDER BY b.nombre_banco, cb.numero_cuenta
                """, idAgencia);
    }

    public List<Map<String, Object>> listarCuentasParaPago(
            Integer idAgencia
    ) {

        String sql = """
                SELECT
                    cb.id_cuenta_bancaria,
                    cba.id_agencia,
                    b.nombre_banco,
                    cb.numero_cuenta,
                    cb.nombre_cuenta,
                    cba.id_cuenta_contable,
                    cc.codigo_cuenta,
                    cc.nombre_cuenta AS nombre_cuenta_contable
                FROM tesoreria.cuentas_bancarias_agencias cba
                INNER JOIN tesoreria.cuentas_bancarias cb
                    ON cb.id_cuenta_bancaria =
                       cba.id_cuenta_bancaria
                INNER JOIN tesoreria.bancos b
                    ON b.codigo_banco = cb.codigo_banco
                INNER JOIN contabilidad.catalogo_cuentas cc
                    ON cc.id_catalogo_cuenta =
                       cba.id_cuenta_contable
                WHERE cba.id_agencia = ?
                  AND cba.activo = TRUE
                  AND cba.permite_pagar = TRUE
                  AND cb.activo = TRUE
                  AND cb.estado_cuenta = 'ACTIVA'
                  AND b.activo = TRUE
                  AND cc.id_agencia = cba.id_agencia
                  AND cc.cuenta_operable = TRUE
                ORDER BY
                    b.nombre_banco,
                    cb.numero_cuenta
                """;

        return jdbcTemplate.queryForList(
                sql,
                idAgencia
        );
    }


    /**
     * Obtiene la cuenta contable de la obligación
     * generada por la cancelación de un CDAT.
     *
     * Debe coincidir con la cuenta acreditada
     * durante la contabilización de la cancelación.
     */
    public Integer obtenerCuentaPorPagarCdat(
            Integer idAgencia
    ) {

        String sql = """
                SELECT
                    COALESCE(
                        id_catalogo_cuenta_debito,
                        id_catalogo_cuenta_credito
                    ) AS id_catalogo_cuenta
                FROM cdat.conceptos_contables_cdats
                WHERE codigo_proceso = 'CUENTA_POR_PAGAR_CDAT'
                  AND activo = TRUE
                  AND (
                      id_agencia = ?
                      OR id_agencia IS NULL
                  )
                  AND plazo_desde_meses IS NULL
                  AND plazo_hasta_meses IS NULL
                ORDER BY
                    CASE
                        WHEN id_agencia = ? THEN 0
                        ELSE 1
                    END
                LIMIT 1
                """;

        List<Integer> cuentas = jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        (Integer) rs.getObject(
                                "id_catalogo_cuenta"
                        ),
                idAgencia,
                idAgencia
        );

        if (cuentas.isEmpty() || cuentas.get(0) == null) {
            throw new IllegalStateException(
                    "No existe una cuenta por pagar CDAT "
                            + "configurada para la agencia "
                            + idAgencia
            );
        }

        return cuentas.get(0);
    }

}
