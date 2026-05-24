package co.assip.erp.cdat.cancelacion;

import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionFiltroDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CdatCancelacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public Optional<CdatCancelacionItemDTO> obtenerPorId(Long idCuentaCdat) {
        String sql = baseSelect() + """
            WHERE c.id_cuenta_cdat = :idCuentaCdat
            LIMIT 1
        """;

        List<CdatCancelacionItemDTO> lista = jdbc.query(
                sql,
                new MapSqlParameterSource("idCuentaCdat", idCuentaCdat),
                (rs, rowNum) -> mapItem(rs)
        );

        return lista.stream().findFirst();
    }

    public List<CdatCancelacionItemDTO> buscar(CdatCancelacionFiltroDTO filtro) {
        String sql = baseSelect() + """
        WHERE c.estado_cdat = 'A'
          AND (CAST(:documento AS TEXT) IS NULL
               OR TRIM(v.documento) ILIKE '%' || TRIM(CAST(:documento AS TEXT)) || '%')

          AND (CAST(:nombres AS TEXT) IS NULL
               OR v.nombres ILIKE '%' || CAST(:nombres AS TEXT) || '%')

          AND (CAST(:primerApellido AS TEXT) IS NULL
               OR v.primer_apellido ILIKE '%' || CAST(:primerApellido AS TEXT) || '%')

          AND (CAST(:segundoApellido AS TEXT) IS NULL
               OR v.segundo_apellido ILIKE '%' || CAST(:segundoApellido AS TEXT) || '%')

          AND (CAST(:codigoCdat AS TEXT) IS NULL
               OR c.codigo_cdat ILIKE '%' || CAST(:codigoCdat AS TEXT) || '%')

        ORDER BY c.fecha_vencimiento_cdat, c.codigo_cdat
        LIMIT 100
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("documento", limpiar(filtro.getDocumento()))
                .addValue("nombres", limpiar(filtro.getNombres()))
                .addValue("primerApellido", limpiar(filtro.getPrimerApellido()))
                .addValue("segundoApellido", limpiar(filtro.getSegundoApellido()))
                .addValue("codigoCdat", limpiar(filtro.getCodigoCdat()));

        return jdbc.query(sql, params, (rs, rowNum) -> mapItem(rs));
    }

    public void marcarCancelado(
            Long idCuentaCdat,
            LocalDate fechaCancelacion,
            Integer idUsuario
    ) {
        int rows = jdbc.update("""
            UPDATE cdat.cuentas_cdats
               SET estado_cdat = 'C',
                   fecha_estado_cdat = :fechaCancelacion,
                   saldo_actual_cdat = 0,
                   fk_seguridad_edicion = :idUsuario,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_cuenta_cdat = :idCuentaCdat
               AND estado_cdat = 'A'
        """, new MapSqlParameterSource()
                .addValue("idCuentaCdat", idCuentaCdat)
                .addValue("fechaCancelacion", fechaCancelacion)
                .addValue("idUsuario", idUsuario));

        if (rows == 0) {
            throw new RuntimeException("El CDAT no existe o no se encuentra activo.");
        }
    }

    private String baseSelect() {
        return """
            SELECT
                c.id_cuenta_cdat,
                c.codigo_cdat,
                c.id_agencia,
                c.id_datos_personal,
                c.estado_cdat,
                c.fecha_apertura_cdat,
                c.fecha_vencimiento_cdat,
                c.fecha_ultima_liquidacion,
                c.plazo_meses,
                c.plazo_dias,
                c.valor_apertura_cdat,
                c.saldo_actual_cdat,
                c.tasa_nominal_anual,
                c.tasa_efectiva_anual,
                c.id_cuenta_ahorro,
                c.retencion_fuente_cdat,
                c.amortizacion_deposito,
                v.documento,
                v.nombre_completo,
                v.primer_apellido,
                v.segundo_apellido,
                v.nombres
            FROM cdat.cuentas_cdats c
            LEFT JOIN LATERAL (
                SELECT
                    hv.documento,
                    hv.primer_apellido,
                    hv.segundo_apellido,
                    hv.nombres,
                    CASE
                        WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                        ELSE TRIM(CONCAT_WS(' ',
                            hv.primer_apellido,
                            hv.segundo_apellido,
                            hv.nombres
                        ))
                    END AS nombre_completo
                FROM reporting.vw_hoja_vida_general_total_extendida hv
                WHERE hv.id_datos_personal = c.id_datos_personal
                LIMIT 1
            ) v ON true
        """;
    }

    private CdatCancelacionItemDTO mapItem(java.sql.ResultSet rs) throws java.sql.SQLException {
        CdatCancelacionItemDTO dto = new CdatCancelacionItemDTO();

        dto.setIdCuentaCdat(rs.getLong("id_cuenta_cdat"));
        dto.setCodigoCdat(rs.getString("codigo_cdat"));
        dto.setIdAgencia(rs.getInt("id_agencia"));
        dto.setIdDatosPersonal(rs.getInt("id_datos_personal"));
        dto.setDocumento(rs.getString("documento"));
        dto.setNombreCompleto(rs.getString("nombre_completo"));
        dto.setEstadoCdat(rs.getString("estado_cdat"));
        dto.setFechaAperturaCdat(rs.getObject("fecha_apertura_cdat", LocalDate.class));
        dto.setFechaVencimientoCdat(rs.getObject("fecha_vencimiento_cdat", LocalDate.class));
        dto.setFechaUltimaLiquidacion(rs.getObject("fecha_ultima_liquidacion", LocalDate.class));
        dto.setPlazoMeses(rs.getInt("plazo_meses"));
        dto.setPlazoDias((Integer) rs.getObject("plazo_dias"));
        dto.setValorCapital(nvl(rs.getBigDecimal("saldo_actual_cdat")));
        dto.setSaldoActualCdat(nvl(rs.getBigDecimal("saldo_actual_cdat")));
        dto.setTasaNominalAnual(nvl(rs.getBigDecimal("tasa_nominal_anual")));
        dto.setTasaEfectivaAnual(nvl(rs.getBigDecimal("tasa_efectiva_anual")));
        dto.setIdCuentaAhorro((Integer) rs.getObject("id_cuenta_ahorro"));
        dto.setRetencionFuenteCdat((Boolean) rs.getObject("retencion_fuente_cdat"));
        dto.setAmortizacionDeposito(rs.getString("amortizacion_deposito"));
        dto.setValorInteresCausado(BigDecimal.ZERO);
        dto.setValorInteresCorriente(BigDecimal.ZERO);
        dto.setValorRetencion(BigDecimal.ZERO);
        dto.setValorDisponible(nvl(rs.getBigDecimal("saldo_actual_cdat")));

        return dto;
    }

    private String limpiar(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public Integer obtenerCuentaConceptoCancelacionCdat(
            Integer idAgencia,
            String tipoMovimiento
    ) {
        String sql = """
        SELECT
            CASE
                WHEN id_catalogo_cuenta_debito IS NOT NULL
                    THEN id_catalogo_cuenta_debito
                WHEN id_catalogo_cuenta_credito IS NOT NULL
                    THEN id_catalogo_cuenta_credito
                ELSE NULL
            END AS cuenta
        FROM cdat.conceptos_contables_cdats
        WHERE codigo_proceso = 'CANCELACION_CDAT'
          AND tipo_movimiento = :tipoMovimiento
          AND activo = TRUE
          AND (id_agencia IS NULL OR id_agencia = :idAgencia)
        ORDER BY id_agencia DESC
        LIMIT 1
    """;

        List<Integer> lista = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("tipoMovimiento", tipoMovimiento),
                (rs, rowNum) -> (Integer) rs.getObject("cuenta")
        );

        if (lista.isEmpty() || lista.get(0) == null) {
            throw new RuntimeException(
                    "No existe configuración contable para cancelación CDAT: "
                            + tipoMovimiento
            );
        }

        return lista.get(0);
    }
}