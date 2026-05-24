package co.assip.erp.cdat.cdats;

import co.assip.erp.cdat.cdats.dto.CdatFormDTO;
import co.assip.erp.cdat.cdats.dto.CdatListDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import co.assip.erp.cdat.cdats.dto.CdatAsociadoValidacionDTO;
import co.assip.erp.cdat.cdats.dto.CdatBeneficiarioDTO;
import co.assip.erp.cdat.cdats.dto.CuentaInfoDTO;
import co.assip.erp.cdat.cdats.dto.TerceroInfoDTO;

@Repository
@RequiredArgsConstructor
public class CdatRepository {

    private final JdbcTemplate jdbc;

    public Integer obtenerSiguienteConsecutivo(Integer idAgencia, Integer idUsuario) {
        String sql = """
        UPDATE general.parametros
        SET valor_parametro = COALESCE(valor_parametro, 0) + 1,
            fk_seguridad_edicion = ?,
            fecha_edicion = CURRENT_TIMESTAMP
        WHERE id_agencia = ?
          AND codigo_parametro = 500
        RETURNING valor_parametro::int
    """;

        Integer consecutivo = jdbc.queryForObject(
                sql,
                Integer.class,
                idUsuario,
                idAgencia
        );

        if (consecutivo == null) {
            throw new RuntimeException("No existe el parámetro 500 - CONSECUTIVO CDAT para la agencia.");
        }

        return consecutivo;
    }

    public boolean existeCodigo(Integer idAgencia, String codigoCdat, Long idCuentaCdat) {
        String sql = """
            SELECT COUNT(*)
            FROM cdat.cuentas_cdats
            WHERE id_agencia = ?
              AND codigo_cdat = ?
              AND (? IS NULL OR id_cuenta_cdat <> ?)
        """;

        Integer count = jdbc.queryForObject(
                sql,
                Integer.class,
                idAgencia,
                codigoCdat,
                idCuentaCdat,
                idCuentaCdat
        );

        return count != null && count > 0;
    }

    public Long crear(CdatSaveDTO dto,
                      LocalDate fechaVencimiento,
                      LocalDate fechaProximaLiquidacion,
                      LocalDate fechaProximoTraslado,
                      Integer idUsuario) {

        String sql = """
            INSERT INTO cdat.cuentas_cdats (
                id_agencia,
                id_producto_cdat,
                codigo_cdat,
                id_datos_personal,
                fecha_apertura_cdat,
                fecha_vencimiento_cdat,
                plazo_meses,
                plazo_dias,
                valor_apertura_cdat,
                saldo_actual_cdat,
                tasa_nominal_anual,
                tasa_efectiva_anual,
                tasa_nominal_mensual,
                tasa_efectiva_mensual,
                estado_cdat,
                fecha_estado_cdat,
                retencion_fuente_cdat,
                amortizacion_deposito,
                modalidad_cdat,
                fecha_ultima_liquidacion,
                fecha_proxima_liquidacion,
                fecha_ultimo_traslado_interes,
                fecha_proximo_traslado_interes,
                id_cuenta_aportes,
                id_cuenta_ahorro,
                cuenta_conjunta,
                accion_conjunta,
                observacion,
                fk_seguridad_creacion,
                id_datos_personal_cotitular,
                origen_cdat,
                id_cuenta_cdat_origen
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'A', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"id_cuenta_cdat"}
            );

            ps.setObject(1, dto.getIdAgencia());
            ps.setObject(2, dto.getIdProductoCdat());
            ps.setString(3, dto.getCodigoCdat());
            ps.setObject(4, dto.getIdDatosPersonal());
            ps.setObject(5, dto.getFechaAperturaCdat());
            ps.setObject(6, fechaVencimiento);
            ps.setObject(7, dto.getPlazoMeses());
            ps.setObject(8, dto.getPlazoDias());
            ps.setBigDecimal(9, dto.getValorAperturaCdat());
            ps.setBigDecimal(10, dto.getValorAperturaCdat());
            ps.setBigDecimal(11, nvl(dto.getTasaNominalAnual()));
            ps.setBigDecimal(12, nvl(dto.getTasaEfectivaAnual()));
            ps.setBigDecimal(13, nvl(dto.getTasaNominalMensual()));
            ps.setBigDecimal(14, nvl(dto.getTasaEfectivaMensual()));
            ps.setObject(15, dto.getFechaAperturaCdat());
            ps.setObject(16, Boolean.TRUE.equals(dto.getRetencionFuenteCdat()));
            ps.setString(17, dto.getAmortizacionDeposito());
            ps.setString(18, dto.getModalidadCdat());
            ps.setObject(19, dto.getFechaAperturaCdat());
            ps.setObject(20, fechaProximaLiquidacion);
            ps.setObject(21, null);
            ps.setObject(22, fechaProximoTraslado);
            ps.setObject(23, dto.getIdCuentaAportes());
            ps.setObject(24, dto.getIdCuentaAhorro());
            ps.setString(25, dto.getCuentaConjunta());
            ps.setString(26, dto.getAccionConjunta());
            ps.setString(27, dto.getObservacion());
            ps.setObject(28, idUsuario);
            ps.setObject(29, dto.getIdDatosPersonalCotitular());
            ps.setString(30, dto.getOrigenCdat());
            ps.setObject(31, dto.getIdCuentaCdatOrigen());

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new RuntimeException("No se pudo obtener el ID del CDAT creado.");
        }

        return key.longValue();
    }


    public List<CdatListDTO> listar() {
        String sql = """
        SELECT
            c.id_cuenta_cdat,
            c.codigo_cdat,
            c.id_agencia,
            c.id_datos_personal,
            v.nombre_completo,
            c.fecha_apertura_cdat,
            c.fecha_vencimiento_cdat,
            c.plazo_meses,
            c.valor_apertura_cdat,
            c.saldo_actual_cdat,
            c.tasa_efectiva_anual,
            c.estado_cdat,
            c.origen_cdat
        FROM cdat.cuentas_cdats c
        LEFT JOIN LATERAL (
            SELECT
                CASE
                    WHEN v.tipo_persona = '2' THEN COALESCE(v.nombres, '')
                    ELSE TRIM(CONCAT_WS(' ',
                        v.primer_apellido,
                        v.segundo_apellido,
                        v.nombres
                    ))
                END AS nombre_completo
            FROM reporting.vw_hoja_vida_general_total_extendida v
            WHERE v.id_datos_personal = c.id_datos_personal
            LIMIT 1
        ) v ON true
        ORDER BY c.id_cuenta_cdat DESC
        LIMIT 50
    """;

        return jdbc.query(sql, (rs, rowNum) -> CdatListDTO.builder()
                .idCuentaCdat(rs.getLong("id_cuenta_cdat"))
                .codigoCdat(rs.getString("codigo_cdat"))
                .idAgencia(rs.getInt("id_agencia"))
                .idDatosPersonal(rs.getInt("id_datos_personal"))
                .nombreCompleto(rs.getString("nombre_completo"))
                .fechaAperturaCdat(rs.getObject("fecha_apertura_cdat", LocalDate.class))
                .fechaVencimientoCdat(rs.getObject("fecha_vencimiento_cdat", LocalDate.class))
                .plazoMeses(rs.getInt("plazo_meses"))
                .valorAperturaCdat(rs.getBigDecimal("valor_apertura_cdat"))
                .saldoActualCdat(rs.getBigDecimal("saldo_actual_cdat"))
                .tasaEfectivaAnual(rs.getBigDecimal("tasa_efectiva_anual"))
                .estadoCdat(rs.getString("estado_cdat"))
                .origenCdat(rs.getString("origen_cdat"))
                .build());
    }

    public Optional<CdatFormDTO> obtenerPorId(Long idCuentaCdat) {
        String sql = """
            SELECT *
            FROM cdat.cuentas_cdats
            WHERE id_cuenta_cdat = ?
        """;

        List<CdatFormDTO> lista = jdbc.query(sql, (rs, rowNum) -> CdatFormDTO.builder()
                .idCuentaCdat(rs.getLong("id_cuenta_cdat"))
                .idAgencia(rs.getInt("id_agencia"))
                .idProductoCdat((Integer) rs.getObject("id_producto_cdat"))
                .codigoCdat(rs.getString("codigo_cdat"))
                .idDatosPersonal(rs.getInt("id_datos_personal"))
                .idDatosPersonalCotitular((Integer) rs.getObject("id_datos_personal_cotitular"))
                .fechaAperturaCdat(rs.getObject("fecha_apertura_cdat", LocalDate.class))
                .fechaVencimientoCdat(rs.getObject("fecha_vencimiento_cdat", LocalDate.class))
                .plazoMeses(rs.getInt("plazo_meses"))
                .plazoDias((Integer) rs.getObject("plazo_dias"))
                .valorAperturaCdat(rs.getBigDecimal("valor_apertura_cdat"))
                .saldoActualCdat(rs.getBigDecimal("saldo_actual_cdat"))
                .tasaNominalAnual(rs.getBigDecimal("tasa_nominal_anual"))
                .tasaEfectivaAnual(rs.getBigDecimal("tasa_efectiva_anual"))
                .tasaNominalMensual(rs.getBigDecimal("tasa_nominal_mensual"))
                .tasaEfectivaMensual(rs.getBigDecimal("tasa_efectiva_mensual"))
                .retencionFuenteCdat((Boolean) rs.getObject("retencion_fuente_cdat"))
                .amortizacionDeposito(rs.getString("amortizacion_deposito"))
                .modalidadCdat(rs.getString("modalidad_cdat"))
                .fechaUltimaLiquidacion(rs.getObject("fecha_ultima_liquidacion", LocalDate.class))
                .fechaProximaLiquidacion(rs.getObject("fecha_proxima_liquidacion", LocalDate.class))
                .fechaUltimoTrasladoInteres(rs.getObject("fecha_ultimo_traslado_interes", LocalDate.class))
                .fechaProximoTrasladoInteres(rs.getObject("fecha_proximo_traslado_interes", LocalDate.class))
                .idCuentaAportes((Integer) rs.getObject("id_cuenta_aportes"))
                .idCuentaAhorro((Integer) rs.getObject("id_cuenta_ahorro"))
                .cuentaConjunta(rs.getString("cuenta_conjunta"))
                .accionConjunta(rs.getString("accion_conjunta"))
                .origenCdat(rs.getString("origen_cdat"))
                .idCuentaCdatOrigen((Long) rs.getObject("id_cuenta_cdat_origen"))
                .estadoCdat(rs.getString("estado_cdat"))
                .observacion(rs.getString("observacion"))
                .build(), idCuentaCdat);

        return lista.stream().findFirst();
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public Optional<CdatAsociadoValidacionDTO> validarAsociado(String documento) {

        String sql = """
        SELECT
            v.id_datos_personal,
            v.documento,
            CASE
                WHEN v.tipo_persona = '2' THEN COALESCE(v.nombres, '')
                ELSE TRIM(CONCAT_WS(' ',
                    v.primer_apellido,
                    v.segundo_apellido,
                    v.nombres
                ))
            END AS nombre_completo,

            EXISTS (
                SELECT 1
                FROM depositos.cuentas_ahorro ca
                JOIN depositos.formas_ahorro fa
                    ON fa.id_forma_ahorro = ca.id_forma_ahorro
                WHERE ca.id_datos_personal = v.id_datos_personal
                  AND fa.codigo_forma = '01'
                  AND ca.estado_cuenta_cuenta = 'A'
            ) AS tiene_aportes_activos,

            true AS datos_actualizados

        FROM reporting.vw_hoja_vida_general_total_extendida v
        WHERE v.documento = ?
        LIMIT 1
    """;

        List<CdatAsociadoValidacionDTO> lista = jdbc.query(sql, (rs, rowNum) -> {

            Boolean tieneAportes = (Boolean) rs.getObject("tiene_aportes_activos");
            Boolean datosActualizados = (Boolean) rs.getObject("datos_actualizados");

            String mensajeError = null;

            if (!Boolean.TRUE.equals(tieneAportes)) {
                mensajeError = "El asociado no tiene aportes activos.";
            }

            if (!Boolean.TRUE.equals(datosActualizados)) {
                mensajeError = "El asociado no tiene los datos actualizados.";
            }

            return CdatAsociadoValidacionDTO.builder()
                    .idDatosPersonal(rs.getInt("id_datos_personal"))
                    .documento(rs.getString("documento"))
                    .nombreCompleto(rs.getString("nombre_completo"))
                    .tieneAportesActivos(tieneAportes)
                    .datosActualizados(datosActualizados)
                    .mensajeError(mensajeError)
                    .build();

        }, documento);

        return lista.stream().findFirst();
    }

    public Integer obtenerProximoConsecutivo(Integer idAgencia) {
        String sql = """
        SELECT COALESCE(valor_parametro, 0)::int + 1
        FROM general.parametros
        WHERE id_agencia = ?
          AND codigo_parametro = 500
    """;

        Integer consecutivo = jdbc.queryForObject(sql, Integer.class, idAgencia);

        if (consecutivo == null) {
            throw new RuntimeException("No existe el parámetro 500 - CONSECUTIVO CDAT para la agencia.");
        }

        return consecutivo;
    }

    public Optional<CdatAsociadoValidacionDTO> buscarCotitular(String documento) {

        String sql = """
        SELECT
            v.id_datos_personal,
            TRIM(v.documento) AS documento,
            CASE
                WHEN v.tipo_persona = '2' THEN COALESCE(v.nombres, '')
                ELSE TRIM(CONCAT_WS(' ',
                    v.primer_apellido,
                    v.segundo_apellido,
                    v.nombres
                ))
            END AS nombre_completo
        FROM reporting.vw_hoja_vida_general_total_extendida v
        WHERE TRIM(v.documento) = TRIM(CAST(? AS TEXT))
        LIMIT 1
    """;

        List<CdatAsociadoValidacionDTO> lista = jdbc.query(sql, (rs, rowNum) ->
                        CdatAsociadoValidacionDTO.builder()
                                .idDatosPersonal(rs.getInt("id_datos_personal"))
                                .documento(rs.getString("documento"))
                                .nombreCompleto(rs.getString("nombre_completo"))
                                .build()
                , documento);

        return lista.stream().findFirst();
    }

    public void guardarBeneficiarios(
            Long idCuentaCdat,
            List<CdatBeneficiarioDTO> beneficiarios,
            Integer idUsuario
    ) {
        if (beneficiarios == null || beneficiarios.isEmpty()) {
            return;
        }

        String sql = """
        INSERT INTO cdat.beneficiarios_cdats (
            id_cuenta_cdat,
            documento,
            nombre,
            telefono,
            tipo_parentesco,
            estado,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        )
        VALUES (?, ?, ?, ?, ?, 'A', ?, ?)
    """;

        for (CdatBeneficiarioDTO b : beneficiarios) {
            jdbc.update(
                    sql,
                    idCuentaCdat,
                    b.getDocumento(),
                    b.getNombre(),
                    b.getTelefono(),
                    b.getTipoParentesco(),
                    idUsuario,
                    idUsuario
            );
        }
    }


    public Integer obtenerMesesAmortizacion(String codigoAmortizacion) {
        String sql = """
        SELECT meses
        FROM cdat.amortizaciones_cdats
        WHERE codigo_amortizacion = ?
    """;

        return jdbc.queryForObject(sql, Integer.class, codigoAmortizacion);
    }

    public Integer obtenerCuentaContableCaja(Long idCaja) {

        String sql = """
        SELECT id_catalogo_cuenta_caja
        FROM cajas.cajas
        WHERE id_caja = ?
        ORDER BY id_caja
        LIMIT 1
    """;

        return jdbc.queryForObject(sql, Integer.class, idCaja);
    }

    public Integer obtenerCuentaContableFormaAhorroPorCuenta(Integer idCuentaAhorro) {

        String sql = """
        SELECT f.cuenta_forma_corto
        FROM depositos.cuentas_ahorro c
        JOIN depositos.formas_ahorro f
          ON f.id_forma_ahorro = c.id_forma_ahorro
        WHERE c.id_cuenta_ahorro = ?
        ORDER BY c.id_cuenta_ahorro
        LIMIT 1
    """;

        return jdbc.queryForObject(sql, Integer.class, idCuentaAhorro);
    }

    public Integer obtenerCuentaCapitalCdat(
            Integer idAgencia,
            Integer plazoMeses,
            String codigoProceso
    ) {

        String sql = """
        SELECT id_catalogo_cuenta_credito
        FROM cdat.conceptos_contables_cdats
        WHERE codigo_proceso = ?
          AND activo = TRUE
          AND (id_agencia IS NULL OR id_agencia = ?)
          AND ? BETWEEN plazo_desde_meses AND plazo_hasta_meses
          AND id_catalogo_cuenta_credito IS NOT NULL
        ORDER BY id_agencia DESC
        LIMIT 1
    """;

        return jdbc.queryForObject(
                sql,
                Integer.class,
                codigoProceso,
                idAgencia,
                plazoMeses
        );
    }

    public CuentaInfoDTO obtenerCuentaInfo(Integer idCuenta) {

        String sql = """
        SELECT codigo_cuenta, nombre_cuenta
        FROM contabilidad.catalogo_cuentas
        WHERE id_catalogo_cuenta = ?
        LIMIT 1
    """;

        return jdbc.queryForObject(sql, (rs, rowNum) -> {
            CuentaInfoDTO dto = new CuentaInfoDTO();
            dto.setCodigo(rs.getString("codigo_cuenta"));
            dto.setNombre(rs.getString("nombre_cuenta"));
            return dto;
        }, idCuenta);
    }

    public TerceroInfoDTO obtenerTerceroInfo(Integer idDatosPersonal) {

        String sql = """
        SELECT
            documento,
            CASE
                WHEN tipo_persona = '2' THEN nombres
                ELSE TRIM(
                    COALESCE(primer_apellido, '') || ' ' ||
                    COALESCE(segundo_apellido, '') || ' ' ||
                    COALESCE(nombres, '')
                )
            END AS nombre
        FROM reporting.vw_hoja_vida_general_total_extendida
        WHERE id_datos_personal = ?
        LIMIT 1
    """;

        return jdbc.queryForObject(sql, (rs, rowNum) -> {
            TerceroInfoDTO dto = new TerceroInfoDTO();
            dto.setDocumento(rs.getString("documento"));
            dto.setNombre(rs.getString("nombre"));
            return dto;
        }, idDatosPersonal);
    }

}