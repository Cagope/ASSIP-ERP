package co.assip.erp.cdat.cierre_mensual_cdat;

import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualItemDTO;
import co.assip.erp.cdat.cierre_mensual_cdat.dto.CdatCierreMensualPreviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CdatCierreMensualRepository {

    private final JdbcTemplate jdbc;

    public boolean existeCierre(Integer idAgencia, LocalDate fechaCorte) {
        String sql = """
            SELECT COUNT(*)
            FROM cdat.cierres_mensuales_cdats
            WHERE id_agencia = ?
              AND fecha_corte = ?
        """;

        Integer count = jdbc.queryForObject(
                sql,
                Integer.class,
                idAgencia,
                Date.valueOf(fechaCorte)
        );

        return count != null && count > 0;
    }

    public List<CdatCierreMensualItemDTO> obtenerCdatsActivos(
            Integer idAgencia,
            LocalDate fechaCorte
    ) {

        String sql = """
        SELECT
            c.id_cuenta_cdat,
            c.codigo_cdat,
            c.id_agencia,

            c.id_datos_personal,

            dp.documento,

            CASE
                WHEN dp.tipo_persona = '2'
                    THEN TRIM(COALESCE(dp.nombres, ''))
                ELSE TRIM(
                    COALESCE(dp.primer_apellido, '') || ' ' ||
                    COALESCE(dp.segundo_apellido, '') || ' ' ||
                    COALESCE(dp.nombres, '')
                )
            END AS nombre_completo,

            c.id_datos_personal_cotitular,
            
            dpc.documento AS documento_cotitular,
                
            CASE
                WHEN dpc.tipo_persona = '2'
                    THEN TRIM(COALESCE(dpc.nombres, ''))
                ELSE TRIM(
                    COALESCE(dpc.primer_apellido, '') || ' ' ||
                    COALESCE(dpc.segundo_apellido, '') || ' ' ||
                    COALESCE(dpc.nombres, '')
                )
            END AS nombre_cotitular,

            c.fecha_apertura_cdat,
            c.fecha_vencimiento_cdat,

            c.fecha_ultima_liquidacion,
            c.fecha_proxima_liquidacion,

            c.fecha_ultimo_traslado_interes,
            c.fecha_proximo_traslado_interes,

            c.plazo_meses,
            c.plazo_dias,

            c.valor_apertura_cdat,
            c.saldo_actual_cdat,

            c.tasa_nominal_anual,
            c.tasa_efectiva_anual,

            c.tasa_nominal_mensual,
            c.tasa_efectiva_mensual,

            c.retencion_fuente_cdat,

            c.amortizacion_deposito,
            c.modalidad_cdat,

            c.id_cuenta_aportes,
            cap.codigo_cuenta AS codigo_cuenta_aportes,

            c.id_cuenta_ahorro,
            ca.codigo_cuenta AS codigo_cuenta_ahorro,

            c.cuenta_conjunta,
            c.accion_conjunta,

            c.origen_cdat,
            c.id_cuenta_cdat_origen,

            c.estado_cdat

        FROM cdat.cuentas_cdats c

        LEFT JOIN hoja_vida.datos_personales dp
               ON dp.id_datos_personal = c.id_datos_personal
               
        LEFT JOIN hoja_vida.datos_personales dpc
               ON dpc.id_datos_personal = c.id_datos_personal_cotitular
        
        LEFT JOIN depositos.cuentas_ahorro ca
               ON ca.id_cuenta_ahorro = c.id_cuenta_ahorro
        LEFT JOIN depositos.cuentas_ahorro cap
               ON cap.id_cuenta_ahorro = c.id_cuenta_aportes

        WHERE c.id_agencia = ?
          AND c.estado_cdat = 'A'
          AND c.fecha_apertura_cdat <= ?

        ORDER BY c.codigo_cdat
    """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> CdatCierreMensualItemDTO.builder()

                        .idCuentaCdat(rs.getLong("id_cuenta_cdat"))
                        .codigoCdat(rs.getString("codigo_cdat"))

                        .idAgencia(rs.getInt("id_agencia"))

                        .idDatosPersonal((Integer) rs.getObject("id_datos_personal"))
                        .documento(rs.getString("documento"))
                        .nombreCompleto(rs.getString("nombre_completo"))

                        .idDatosPersonalCotitular(
                                (Integer) rs.getObject("id_datos_personal_cotitular")
                        )

                        .documentoCotitular(rs.getString("documento_cotitular"))
                        .nombreCotitular(rs.getString("nombre_cotitular"))

                        .fechaAperturaCdat(
                                toLocalDate(rs.getDate("fecha_apertura_cdat"))
                        )

                        .fechaVencimientoCdat(
                                toLocalDate(rs.getDate("fecha_vencimiento_cdat"))
                        )

                        .fechaUltimaLiquidacion(
                                toLocalDate(rs.getDate("fecha_ultima_liquidacion"))
                        )

                        .fechaProximaLiquidacion(
                                toLocalDate(rs.getDate("fecha_proxima_liquidacion"))
                        )

                        .fechaUltimoTrasladoInteres(
                                toLocalDate(rs.getDate("fecha_ultimo_traslado_interes"))
                        )

                        .fechaProximoTrasladoInteres(
                                toLocalDate(rs.getDate("fecha_proximo_traslado_interes"))
                        )

                        .plazoMeses((Integer) rs.getObject("plazo_meses"))
                        .plazoDias((Integer) rs.getObject("plazo_dias"))

                        .valorAperturaCdat(
                                rs.getBigDecimal("valor_apertura_cdat")
                        )

                        .saldoActualCdat(
                                rs.getBigDecimal("saldo_actual_cdat")
                        )

                        .tasaNominalAnual(
                                rs.getBigDecimal("tasa_nominal_anual")
                        )

                        .tasaEfectivaAnual(
                                rs.getBigDecimal("tasa_efectiva_anual")
                        )

                        .tasaNominalMensual(
                                rs.getBigDecimal("tasa_nominal_mensual")
                        )

                        .tasaEfectivaMensual(
                                rs.getBigDecimal("tasa_efectiva_mensual")
                        )

                        .retencionFuenteCdat(
                                rs.getBoolean("retencion_fuente_cdat")
                        )

                        .amortizacionDeposito(
                                rs.getString("amortizacion_deposito")
                        )

                        .modalidadCdat(
                                rs.getString("modalidad_cdat")
                        )

                        .idCuentaAportes(
                                (Integer) rs.getObject("id_cuenta_aportes")
                        )

                        .codigoCuentaAportes(
                                rs.getString("codigo_cuenta_aportes")
                        )

                        .idCuentaAhorro(
                                (Integer) rs.getObject("id_cuenta_ahorro")
                        )

                        .codigoCuentaAhorro(
                                rs.getString("codigo_cuenta_ahorro")
                        )

                        .cuentaConjunta(
                                rs.getString("cuenta_conjunta") != null
                                        && rs.getString("cuenta_conjunta").equalsIgnoreCase("S")
                        )

                        .accionConjunta(
                                rs.getString("accion_conjunta")
                        )

                        .origenCdat(
                                rs.getString("origen_cdat")
                        )

                        .idCuentaCdatOrigen(
                                (Long) rs.getObject("id_cuenta_cdat_origen")
                        )

                        .estadoCdat(
                                rs.getString("estado_cdat")
                        )

                        .build(),

                idAgencia,
                Date.valueOf(fechaCorte)
        );
    }

    public Long crearEncabezado(
            Integer idAgencia,
            LocalDate fechaCorte,
            Integer anio,
            Integer mes,
            Integer totalCdats,
            BigDecimal totalCapital,
            Integer usuarioCreacion
    ) {
        String sql = """
            INSERT INTO cdat.cierres_mensuales_cdats (
                id_agencia,
                fecha_corte,
                anio,
                mes,
                total_cdats,
                total_capital,
                estado,
                usuario_creacion
            )
            VALUES (?, ?, ?, ?, ?, ?, 'GENERADO', ?)
            RETURNING id_cierre_mensual_cdat
        """;

        return jdbc.queryForObject(
                sql,
                Long.class,
                idAgencia,
                Date.valueOf(fechaCorte),
                anio,
                mes,
                totalCdats,
                totalCapital,
                usuarioCreacion
        );
    }

    public void insertarDetalle(Long idCierre, CdatCierreMensualItemDTO item) {
        String sql = """
        INSERT INTO cdat.cierres_mensuales_cdats_detalle (
            id_cierre_mensual_cdat,
            id_cuenta_cdat,
            codigo_cdat,
            id_agencia,
            id_datos_personal,
            documento,
            nombre_completo,
            id_datos_personal_cotitular,
            documento_cotitular,
            nombre_cotitular,
            fecha_apertura_cdat,
            fecha_vencimiento_cdat,
            fecha_ultima_liquidacion,
            fecha_proxima_liquidacion,
            fecha_ultimo_traslado_interes,
            fecha_proximo_traslado_interes,
            plazo_meses,
            plazo_dias,
            valor_apertura_cdat,
            saldo_actual_cdat,
            tasa_nominal_anual,
            tasa_efectiva_anual,
            tasa_nominal_mensual,
            tasa_efectiva_mensual,
            retencion_fuente_cdat,
            amortizacion_deposito,
            modalidad_cdat,
            id_cuenta_aportes,
            codigo_cuenta_aportes,
            id_cuenta_ahorro,
            codigo_cuenta_ahorro,
            cuenta_conjunta,
            accion_conjunta,
            origen_cdat,
            id_cuenta_cdat_origen,
            estado_cdat
        )
        VALUES (
            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
        )
    """;

        jdbc.update(
                sql,
                idCierre,
                item.getIdCuentaCdat(),
                item.getCodigoCdat(),
                item.getIdAgencia(),
                item.getIdDatosPersonal(),
                item.getDocumento(),
                item.getNombreCompleto(),
                item.getIdDatosPersonalCotitular(),
                item.getDocumentoCotitular(),
                item.getNombreCotitular(),
                toSqlDate(item.getFechaAperturaCdat()),
                toSqlDate(item.getFechaVencimientoCdat()),
                toSqlDate(item.getFechaUltimaLiquidacion()),
                toSqlDate(item.getFechaProximaLiquidacion()),
                toSqlDate(item.getFechaUltimoTrasladoInteres()),
                toSqlDate(item.getFechaProximoTrasladoInteres()),
                item.getPlazoMeses(),
                item.getPlazoDias(),
                item.getValorAperturaCdat(),
                item.getSaldoActualCdat(),
                item.getTasaNominalAnual(),
                item.getTasaEfectivaAnual(),
                item.getTasaNominalMensual(),
                item.getTasaEfectivaMensual(),
                item.getRetencionFuenteCdat(),
                item.getAmortizacionDeposito(),
                item.getModalidadCdat(),
                item.getIdCuentaAportes(),
                item.getCodigoCuentaAportes(),
                item.getIdCuentaAhorro(),
                item.getCodigoCuentaAhorro(),
                item.getCuentaConjunta(),
                item.getAccionConjunta(),
                item.getOrigenCdat(),
                item.getIdCuentaCdatOrigen(),
                item.getEstadoCdat()
        );
    }

    public List<CdatCierreMensualPreviewDTO> listar() {
        String sql = """
            SELECT
                id_cierre_mensual_cdat,
                id_agencia,
                fecha_corte,
                anio,
                mes,
                total_cdats,
                total_capital,
                estado
            FROM cdat.cierres_mensuales_cdats
            ORDER BY fecha_corte DESC, id_agencia
        """;

        return jdbc.query(sql, (rs, rowNum) -> CdatCierreMensualPreviewDTO.builder()
                .idCierreMensualCdat(rs.getLong("id_cierre_mensual_cdat"))
                .idAgencia(rs.getInt("id_agencia"))
                .fechaCorte(toLocalDate(rs.getDate("fecha_corte")))
                .anio(rs.getInt("anio"))
                .mes(rs.getInt("mes"))
                .totalCdats(rs.getInt("total_cdats"))
                .totalCapital(rs.getBigDecimal("total_capital"))
                .estado(rs.getString("estado"))
                .existeCierre(true)
                .items(List.of())
                .build());
    }

    public Optional<CdatCierreMensualPreviewDTO> obtenerPorId(Long idCierre) {
        String sql = """
            SELECT
                id_cierre_mensual_cdat,
                id_agencia,
                fecha_corte,
                anio,
                mes,
                total_cdats,
                total_capital,
                estado
            FROM cdat.cierres_mensuales_cdats
            WHERE id_cierre_mensual_cdat = ?
        """;

        List<CdatCierreMensualPreviewDTO> lista = jdbc.query(
                sql,
                (rs, rowNum) -> CdatCierreMensualPreviewDTO.builder()
                        .idCierreMensualCdat(rs.getLong("id_cierre_mensual_cdat"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .fechaCorte(toLocalDate(rs.getDate("fecha_corte")))
                        .anio(rs.getInt("anio"))
                        .mes(rs.getInt("mes"))
                        .totalCdats(rs.getInt("total_cdats"))
                        .totalCapital(rs.getBigDecimal("total_capital"))
                        .estado(rs.getString("estado"))
                        .existeCierre(true)
                        .items(obtenerDetalle(idCierre))
                        .build(),
                idCierre
        );

        return lista.stream().findFirst();
    }

    private List<CdatCierreMensualItemDTO> obtenerDetalle(Long idCierre) {
        String sql = """
        SELECT
            id_cuenta_cdat,
            codigo_cdat,
            id_agencia,

            id_datos_personal,
            documento,
            nombre_completo,

            id_datos_personal_cotitular,

            fecha_apertura_cdat,
            fecha_vencimiento_cdat,

            fecha_ultima_liquidacion,
            fecha_proxima_liquidacion,

            fecha_ultimo_traslado_interes,
            fecha_proximo_traslado_interes,

            plazo_meses,
            plazo_dias,

            valor_apertura_cdat,
            saldo_actual_cdat,

            tasa_nominal_anual,
            tasa_efectiva_anual,

            tasa_nominal_mensual,
            tasa_efectiva_mensual,

            retencion_fuente_cdat,

            amortizacion_deposito,
            modalidad_cdat,

            id_cuenta_aportes,

            id_cuenta_ahorro,
            codigo_cuenta_ahorro,

            cuenta_conjunta,
            accion_conjunta,

            origen_cdat,
            id_cuenta_cdat_origen,

            estado_cdat

        FROM cdat.cierres_mensuales_cdats_detalle
        WHERE id_cierre_mensual_cdat = ?
        ORDER BY codigo_cdat
    """;

        return jdbc.query(sql, (rs, rowNum) -> CdatCierreMensualItemDTO.builder()
                .idCuentaCdat(rs.getLong("id_cuenta_cdat"))
                .codigoCdat(rs.getString("codigo_cdat"))
                .idAgencia(rs.getInt("id_agencia"))

                .idDatosPersonal((Integer) rs.getObject("id_datos_personal"))
                .documento(rs.getString("documento"))
                .nombreCompleto(rs.getString("nombre_completo"))

                .idDatosPersonalCotitular((Integer) rs.getObject("id_datos_personal_cotitular"))

                .fechaAperturaCdat(toLocalDate(rs.getDate("fecha_apertura_cdat")))
                .fechaVencimientoCdat(toLocalDate(rs.getDate("fecha_vencimiento_cdat")))

                .fechaUltimaLiquidacion(toLocalDate(rs.getDate("fecha_ultima_liquidacion")))
                .fechaProximaLiquidacion(toLocalDate(rs.getDate("fecha_proxima_liquidacion")))

                .fechaUltimoTrasladoInteres(toLocalDate(rs.getDate("fecha_ultimo_traslado_interes")))
                .fechaProximoTrasladoInteres(toLocalDate(rs.getDate("fecha_proximo_traslado_interes")))

                .plazoMeses((Integer) rs.getObject("plazo_meses"))
                .plazoDias((Integer) rs.getObject("plazo_dias"))

                .valorAperturaCdat(rs.getBigDecimal("valor_apertura_cdat"))
                .saldoActualCdat(rs.getBigDecimal("saldo_actual_cdat"))

                .tasaNominalAnual(rs.getBigDecimal("tasa_nominal_anual"))
                .tasaEfectivaAnual(rs.getBigDecimal("tasa_efectiva_anual"))

                .tasaNominalMensual(rs.getBigDecimal("tasa_nominal_mensual"))
                .tasaEfectivaMensual(rs.getBigDecimal("tasa_efectiva_mensual"))

                .retencionFuenteCdat(rs.getBoolean("retencion_fuente_cdat"))

                .amortizacionDeposito(rs.getString("amortizacion_deposito"))
                .modalidadCdat(rs.getString("modalidad_cdat"))

                .idCuentaAportes((Integer) rs.getObject("id_cuenta_aportes"))

                .idCuentaAhorro((Integer) rs.getObject("id_cuenta_ahorro"))
                .codigoCuentaAhorro(rs.getString("codigo_cuenta_ahorro"))

                .cuentaConjunta(rs.getBoolean("cuenta_conjunta"))
                .accionConjunta(rs.getString("accion_conjunta"))

                .origenCdat(rs.getString("origen_cdat"))
                .idCuentaCdatOrigen((Long) rs.getObject("id_cuenta_cdat_origen"))

                .estadoCdat(rs.getString("estado_cdat"))
                .build(), idCierre);
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private Date toSqlDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    public boolean tieneCausacionMensual(Long idCierre) {

        String sql = """
        SELECT COUNT(*)
        FROM cdat.causaciones_mensuales_intereses
        WHERE id_cierre_mensual_cdat = ?
    """;

        Integer count = jdbc.queryForObject(
                sql,
                Integer.class,
                idCierre
        );

        return count != null && count > 0;
    }

    public void eliminar(Long idCierre) {

        String sql = """
        DELETE
        FROM cdat.cierres_mensuales_cdats
        WHERE id_cierre_mensual_cdat = ?
    """;

        jdbc.update(sql, idCierre);
    }

}