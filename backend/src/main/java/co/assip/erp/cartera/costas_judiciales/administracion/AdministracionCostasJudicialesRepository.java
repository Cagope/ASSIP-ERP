
package co.assip.erp.cartera.costas_judiciales.administracion;

import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Persistencia financiera de cargos de costas judiciales por crédito. */
@Repository
@RequiredArgsConstructor
public class AdministracionCostasJudicialesRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final PagoCuotasMotorRepository cuentasCartera;

    /** Bloquea el crédito para serializar cargos concurrentes y devuelve su agencia y titular. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Credito bloquearCredito(Integer idCredito) {
        if (idCredito == null || idCredito <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un crédito válido.");
        }
        String sql = """
                SELECT id_cartera_credito, id_agencia, id_datos_personal,
                       codigo_estado_cartera
                FROM cartera.carteras_creditos
                WHERE id_cartera_credito = :credito
                FOR UPDATE
                """;
        return jdbc.query(sql, new MapSqlParameterSource("credito", idCredito),
                        (rs, i) -> new Credito(
                                rs.getInt("id_cartera_credito"),
                                rs.getInt("id_agencia"),
                                rs.getLong("id_datos_personal"),
                                rs.getString("codigo_estado_cartera")))
                .stream().findFirst().orElseThrow(() ->
                        new IllegalArgumentException("No existe el crédito seleccionado."));
    }

    /** Reutiliza la resolución contable de Cartera para determinar la cuenta de costas. */
    public PagoCuotasMotorRepository.CuentaCartera resolverCuentaCostas(Integer idCredito) {
        var datos = cuentasCartera.consultarDatosCredito(idCredito);
        return cuentasCartera.resolverCuenta(datos, "COSTAS_JUDICIALES", null);
    }

    /** Resuelve la contrapartida por agencia; la presencia de tercero define la modalidad. */
    public PagoCuotasMotorRepository.CuentaCartera resolverContrapartida(
            Integer idAgencia, Long idTercero) {
        String concepto = idTercero == null
                ? "COSTAS_JUDICIALES_AGENCIA"
                : "COSTAS_JUDICIALES_CXP";
        return cuentasCartera.resolverParametroContable(idAgencia, concepto);
    }

    /** Comprueba que el tercero seleccionado existe; no permite identificadores arbitrarios. */
    public boolean existeTercero(Long idTercero) {
        if (idTercero == null || idTercero <= 0) {
            return false;
        }
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM hoja_vida.datos_personales
                    WHERE id_datos_personal = :tercero
                )
                """;
        return Boolean.TRUE.equals(jdbc.queryForObject(sql,
                new MapSqlParameterSource("tercero", idTercero), Boolean.class));
    }

    /**
     * Obtiene el nombre de la cuenta y el documento del tercero.
     * Valida la agencia de la cuenta y no modifica registros financieros.
     */
    public DatosLinea consultarDatosLinea(
            Integer idCatalogoCuenta, Integer idAgencia, Long idTercero) {
        if (idCatalogoCuenta == null || idCatalogoCuenta <= 0
                || idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException("La cuenta contable o agencia no es válida.");
        }
        String sql = """
                SELECT cc.nombre_cuenta,
                       COALESCE(dp.documento, '') AS documento_tercero
                FROM contabilidad.catalogo_cuentas cc
                LEFT JOIN hoja_vida.datos_personales dp
                  ON dp.id_datos_personal = :tercero
                WHERE cc.id_catalogo_cuenta = :cuenta
                  AND cc.id_agencia = :agencia
                """;
        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("cuenta", idCatalogoCuenta)
                .addValue("agencia", idAgencia)
                .addValue("tercero", idTercero);
        return jdbc.query(sql, parametros,
                        (rs, i) -> new DatosLinea(
                                rs.getString("nombre_cuenta"),
                                rs.getString("documento_tercero")))
                .stream().findFirst().orElseThrow(() ->
                        new IllegalArgumentException(
                                "La cuenta contable no pertenece a la agencia del crédito."));
    }

    /** Busca la cabecera activa bajo bloqueo del crédito; detecta configuraciones ambiguas. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long obtenerCabeceraActiva(Integer idCredito) {
        String sql = """
                SELECT id_credito_costa_judicial
                FROM cartera.creditos_costas_judiciales
                WHERE id_cartera_credito = :credito AND activo = TRUE
                ORDER BY id_credito_costa_judicial
                FOR UPDATE
                """;
        List<Long> ids = jdbc.query(sql,
                new MapSqlParameterSource("credito", idCredito),
                (rs, i) -> rs.getLong(1));
        if (ids.size() > 1) {
            throw new IllegalStateException(
                    "El crédito tiene varias cabeceras activas de costas judiciales.");
        }
        return ids.isEmpty() ? null : ids.get(0);
    }


    /** Crea la cabecera activa utilizando la identidad automática de PostgreSQL. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long crearCabecera(Integer idCredito, Integer idUsuario) {
        validarUsuario(idUsuario);
        String sql = """
            INSERT INTO cartera.creditos_costas_judiciales (
                id_cartera_credito, activo,
                fk_seguridad_creacion, fk_seguridad_edicion
            ) VALUES (
                :credito, TRUE, :usuario, :usuario
            )
            RETURNING id_credito_costa_judicial
            """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("credito", idCredito)
                .addValue("usuario", idUsuario), Long.class);
    }



    /** Registra el cargo financiero después de confirmar el comprobante contable. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long registrarCargo(Long idCabecera, LocalDate fecha, BigDecimal valor,
                               String tipoComprobante, String numeroComprobante,
                               String observacion, Integer idUsuario) {
        validarUsuario(idUsuario);
        if (idCabecera == null || idCabecera <= 0 || fecha == null
                || valor == null || valor.signum() <= 0
                || tipoComprobante == null || tipoComprobante.isBlank()
                || numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "El cargo de costas judiciales está incompleto.");
        }
        String sql = """
            INSERT INTO cartera.creditos_costas_judiciales_detalle (
                id_credito_costa_judicial,
                fecha_movimiento, tipo_movimiento,
                valor_debito, valor_credito,
                tipo_comprobante, numero_comprobante,
                observacion_costa, estado,
                fk_seguridad_creacion, fk_seguridad_edicion
            ) VALUES (
                :cabecera, :fecha, 'D',
                :valor, 0,
                :tipo, :numero,
                :observacion, 'A',
                :usuario, :usuario
            )
            RETURNING id_credito_costa_judicial_detalle
            """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("cabecera", idCabecera)
                .addValue("fecha", fecha)
                .addValue("valor", valor)
                .addValue("tipo", tipoComprobante.trim())
                .addValue("numero", numeroComprobante.trim())
                .addValue("observacion", observacion)
                .addValue("usuario", idUsuario), Long.class);
    }

    /** Calcula el saldo con la misma regla usada por Pago de Cuotas. */
    public BigDecimal consultarSaldo(Integer idCredito) {
        String sql = """
                SELECT COALESCE(SUM(d.valor_debito - d.valor_credito), 0)
                FROM cartera.creditos_costas_judiciales c
                JOIN cartera.creditos_costas_judiciales_detalle d
                  ON d.id_credito_costa_judicial = c.id_credito_costa_judicial
                WHERE c.id_cartera_credito = :credito
                  AND c.activo = TRUE AND d.estado = 'A'
                """;
        return jdbc.queryForObject(sql,
                new MapSqlParameterSource("credito", idCredito), BigDecimal.class);
    }

    /** Valida el identificador del usuario de sesión para auditoría. */
    private void validarUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("No se identificó el usuario de sesión.");
        }
    }

    public record Credito(Integer idCredito, Integer idAgencia,
                          Long idTitular, String estado) { }

    public record DatosLinea(String nombreCuenta, String documentoTercero) { }
}
