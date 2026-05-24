package co.assip.erp.cdat.causacion_mensual_cdat;

import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualItemDTO;
import co.assip.erp.cdat.causacion_mensual_cdat.dto.CdatCausacionMensualPreviewDTO;
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
public class CdatCausacionMensualRepository {

    private final JdbcTemplate jdbc;

    public Optional<Long> buscarCierre(Integer idAgencia, LocalDate fechaCorte) {
        String sql = """
            SELECT id_cierre_mensual_cdat
            FROM cdat.cierres_mensuales_cdats
            WHERE id_agencia = ?
              AND fecha_corte = ?
        """;

        List<Long> lista = jdbc.query(
                sql,
                (rs, rowNum) -> rs.getLong("id_cierre_mensual_cdat"),
                idAgencia,
                Date.valueOf(fechaCorte)
        );

        return lista.stream().findFirst();
    }

    public boolean existeCausacion(Integer idAgencia, LocalDate fechaCorte) {
        String sql = """
            SELECT COUNT(*)
            FROM cdat.causaciones_mensuales_intereses
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

    public List<CdatCausacionMensualItemDTO> obtenerDetalleCierre(Long idCierre) {
        String sql = """
            SELECT
                id_cierre_mensual_cdat_detalle,
                id_cuenta_cdat,
                codigo_cdat,

                id_datos_personal,
                documento,
                nombre_completo,

                id_datos_personal_cotitular,
                documento_cotitular,
                nombre_cotitular,

                fecha_apertura_cdat,
                fecha_vencimiento_cdat,
                fecha_ultima_liquidacion,

                saldo_actual_cdat,
                tasa_nominal_anual,

                retencion_fuente_cdat,

                modalidad_cdat,
                amortizacion_deposito,

                id_cuenta_aportes,
                codigo_cuenta_aportes,

                id_cuenta_ahorro,
                codigo_cuenta_ahorro

            FROM cdat.cierres_mensuales_cdats_detalle
            WHERE id_cierre_mensual_cdat = ?
              AND estado_cdat = 'A'
            ORDER BY codigo_cdat
        """;

        return jdbc.query(sql, (rs, rowNum) -> CdatCausacionMensualItemDTO.builder()
                .idCierreMensualCdatDetalle(rs.getLong("id_cierre_mensual_cdat_detalle"))
                .idCuentaCdat(rs.getLong("id_cuenta_cdat"))
                .codigoCdat(rs.getString("codigo_cdat"))

                .idDatosPersonal((Integer) rs.getObject("id_datos_personal"))
                .documento(rs.getString("documento"))
                .nombreCompleto(rs.getString("nombre_completo"))

                .idDatosPersonalCotitular((Integer) rs.getObject("id_datos_personal_cotitular"))
                .documentoCotitular(rs.getString("documento_cotitular"))
                .nombreCotitular(rs.getString("nombre_cotitular"))

                .fechaAperturaCdat(toLocalDate(rs.getDate("fecha_apertura_cdat")))
                .fechaVencimientoCdat(toLocalDate(rs.getDate("fecha_vencimiento_cdat")))
                .fechaUltimaLiquidacion(toLocalDate(rs.getDate("fecha_ultima_liquidacion")))

                .saldoBase(rs.getBigDecimal("saldo_actual_cdat"))
                .tasaNominalAnual(rs.getBigDecimal("tasa_nominal_anual"))

                .aplicaRetencion(rs.getBoolean("retencion_fuente_cdat"))

                .modalidadCdat(rs.getString("modalidad_cdat"))
                .amortizacionDeposito(rs.getString("amortizacion_deposito"))

                .idCuentaAportes((Integer) rs.getObject("id_cuenta_aportes"))
                .codigoCuentaAportes(rs.getString("codigo_cuenta_aportes"))

                .idCuentaAhorro((Integer) rs.getObject("id_cuenta_ahorro"))
                .codigoCuentaAhorro(rs.getString("codigo_cuenta_ahorro"))

                .build(), idCierre);
    }

    public Long crearEncabezado(
            Long idCierre,
            Integer idAgencia,
            LocalDate fechaCorte,
            LocalDate fechaContable,
            Integer anio,
            Integer mes,
            String tipoComprobante,
            String numeroComprobante,
            Integer totalCdats,
            BigDecimal totalCapital,
            BigDecimal totalInteres,
            BigDecimal totalRetencion,
            BigDecimal totalNeto,
            Integer usuarioCreacion
    ) {
        String sql = """
            INSERT INTO cdat.causaciones_mensuales_intereses (
                id_cierre_mensual_cdat,
                id_agencia,
                fecha_corte,
                fecha_contable,
                anio,
                mes,
                tipo_comprobante,
                numero_comprobante,
                total_cdats,
                total_capital,
                total_interes,
                total_retencion,
                total_neto,
                estado,
                usuario_creacion
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'GENERADO', ?)
            RETURNING id_causacion_mensual_interes
        """;

        return jdbc.queryForObject(
                sql,
                Long.class,
                idCierre,
                idAgencia,
                Date.valueOf(fechaCorte),
                Date.valueOf(fechaContable),
                anio,
                mes,
                tipoComprobante,
                numeroComprobante,
                totalCdats,
                totalCapital,
                totalInteres,
                totalRetencion,
                totalNeto,
                usuarioCreacion
        );
    }

    public void insertarDetalle(Long idCausacion, CdatCausacionMensualItemDTO item) {
        String sql = """
            INSERT INTO cdat.causaciones_mensuales_intereses_detalle (
                id_causacion_mensual_interes,
                id_cierre_mensual_cdat_detalle,
                id_cuenta_cdat,
                codigo_cdat,
                id_datos_personal,
                documento,
                nombre_completo,
                saldo_base,
                tasa_nominal_anual,
                dias_causados,
                valor_interes,
                valor_retencion,
                valor_neto,
                aplica_retencion
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbc.update(
                sql,
                idCausacion,
                item.getIdCierreMensualCdatDetalle(),
                item.getIdCuentaCdat(),
                item.getCodigoCdat(),
                item.getIdDatosPersonal(),
                item.getDocumento(),
                item.getNombreCompleto(),
                item.getSaldoBase(),
                item.getTasaNominalAnual(),
                item.getDiasCausados(),
                item.getValorInteres(),
                item.getValorRetencion(),
                item.getValorNeto(),
                item.getAplicaRetencion()
        );
    }

    public List<CdatCausacionMensualPreviewDTO> listar() {
        String sql = """
            SELECT
                id_causacion_mensual_interes,
                id_cierre_mensual_cdat,
                id_agencia,
                fecha_corte,
                fecha_contable,
                anio,
                mes,
                tipo_comprobante,
                numero_comprobante,
                total_cdats,
                total_capital,
                total_interes,
                total_retencion,
                total_neto,
                estado
            FROM cdat.causaciones_mensuales_intereses
            ORDER BY fecha_corte DESC, id_agencia
        """;

        return jdbc.query(sql, (rs, rowNum) -> CdatCausacionMensualPreviewDTO.builder()
                .idCausacionMensualInteres(rs.getLong("id_causacion_mensual_interes"))
                .idCierreMensualCdat(rs.getLong("id_cierre_mensual_cdat"))
                .idAgencia(rs.getInt("id_agencia"))
                .fechaCorte(toLocalDate(rs.getDate("fecha_corte")))
                .fechaContable(toLocalDate(rs.getDate("fecha_contable")))
                .anio(rs.getInt("anio"))
                .mes(rs.getInt("mes"))
                .tipoComprobante(rs.getString("tipo_comprobante"))
                .numeroComprobante(rs.getString("numero_comprobante"))
                .totalCdats(rs.getInt("total_cdats"))
                .totalCapital(rs.getBigDecimal("total_capital"))
                .totalInteres(rs.getBigDecimal("total_interes"))
                .totalRetencion(rs.getBigDecimal("total_retencion"))
                .totalNeto(rs.getBigDecimal("total_neto"))
                .estado(rs.getString("estado"))
                .existeCausacion(true)
                .items(List.of())
                .build());
    }

    public Optional<CdatCausacionMensualPreviewDTO> obtenerPorId(Long idCausacion) {
        String sql = """
            SELECT
                id_causacion_mensual_interes,
                id_cierre_mensual_cdat,
                id_agencia,
                fecha_corte,
                fecha_contable,
                anio,
                mes,
                tipo_comprobante,
                numero_comprobante,
                total_cdats,
                total_capital,
                total_interes,
                total_retencion,
                total_neto,
                estado
            FROM cdat.causaciones_mensuales_intereses
            WHERE id_causacion_mensual_interes = ?
        """;

        List<CdatCausacionMensualPreviewDTO> lista = jdbc.query(
                sql,
                (rs, rowNum) -> CdatCausacionMensualPreviewDTO.builder()
                        .idCausacionMensualInteres(rs.getLong("id_causacion_mensual_interes"))
                        .idCierreMensualCdat(rs.getLong("id_cierre_mensual_cdat"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .fechaCorte(toLocalDate(rs.getDate("fecha_corte")))
                        .fechaContable(toLocalDate(rs.getDate("fecha_contable")))
                        .anio(rs.getInt("anio"))
                        .mes(rs.getInt("mes"))
                        .tipoComprobante(rs.getString("tipo_comprobante"))
                        .numeroComprobante(rs.getString("numero_comprobante"))
                        .totalCdats(rs.getInt("total_cdats"))
                        .totalCapital(rs.getBigDecimal("total_capital"))
                        .totalInteres(rs.getBigDecimal("total_interes"))
                        .totalRetencion(rs.getBigDecimal("total_retencion"))
                        .totalNeto(rs.getBigDecimal("total_neto"))
                        .estado(rs.getString("estado"))
                        .existeCausacion(true)
                        .items(obtenerDetalleCausacion(idCausacion))
                        .build(),
                idCausacion
        );

        return lista.stream().findFirst();
    }

    private List<CdatCausacionMensualItemDTO> obtenerDetalleCausacion(Long idCausacion) {
        String sql = """
            SELECT
                d.id_cierre_mensual_cdat_detalle,
                d.id_cuenta_cdat,
                d.codigo_cdat,
                d.id_datos_personal,
                d.documento,
                d.nombre_completo,
                d.saldo_base,
                d.tasa_nominal_anual,
                d.dias_causados,
                d.valor_interes,
                d.valor_retencion,
                d.valor_neto,
                d.aplica_retencion
            FROM cdat.causaciones_mensuales_intereses_detalle d
            WHERE d.id_causacion_mensual_interes = ?
            ORDER BY d.codigo_cdat
        """;

        return jdbc.query(sql, (rs, rowNum) -> CdatCausacionMensualItemDTO.builder()
                .idCierreMensualCdatDetalle(rs.getLong("id_cierre_mensual_cdat_detalle"))
                .idCuentaCdat(rs.getLong("id_cuenta_cdat"))
                .codigoCdat(rs.getString("codigo_cdat"))
                .idDatosPersonal((Integer) rs.getObject("id_datos_personal"))
                .documento(rs.getString("documento"))
                .nombreCompleto(rs.getString("nombre_completo"))
                .saldoBase(rs.getBigDecimal("saldo_base"))
                .tasaNominalAnual(rs.getBigDecimal("tasa_nominal_anual"))
                .diasCausados(rs.getInt("dias_causados"))
                .valorInteres(rs.getBigDecimal("valor_interes"))
                .valorRetencion(rs.getBigDecimal("valor_retencion"))
                .valorNeto(rs.getBigDecimal("valor_neto"))
                .aplicaRetencion(rs.getBoolean("aplica_retencion"))
                .build(), idCausacion);
    }

    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}