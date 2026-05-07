package co.assip.erp.depositos.revalorizacion;

import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionEntradaDTO;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevalorizacionService {

    private final RevalorizacionRepository repository;
    private final NamedParameterJdbcTemplate jdbc;
    private final DepositosMovimientoService depositosMovimientoService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final RevalorizacionContabilidadService contabilidadService;

    @Transactional(readOnly = true)
    public List<RevalorizacionItemDTO> liquidar(RevalorizacionEntradaDTO input) {

        validarEntradaPreview(input);
        validarNoAplicado(input);
        validarFormaRevalorizacion(input);

        List<RevalorizacionItemDTO> lista = repository.liquidar(input);

        return lista.stream()
                .filter(x -> mayorCero(x.getSaldoActual()))
                .filter(x -> mayorCero(x.getValorRevalorizacion()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RevalorizacionItemDTO> ejecutar(RevalorizacionEntradaDTO input) {
        return liquidar(input);
    }

    @Transactional
    public void aplicar(RevalorizacionEntradaDTO input, Integer idUsuario) {

        validarEntradaAplicacion(input);
        validarNoAplicado(input);
        validarFormaRevalorizacion(input);

        List<RevalorizacionItemDTO> lista = liquidar(input);

        if (lista.isEmpty()) {
            throw new RuntimeException("No hay revalorización para aplicar.");
        }

        String tipoComprobante = input.getTipoComprobante();

        String numeroComprobante = consecutivosComprobantesService.generarNumeroDefinitivo(
                tipoComprobante,
                input.getAgenciaId(),
                idUsuario
        );

        input.setNumeroComprobante(numeroComprobante);

        String tipoMovimientoRevalorizacion = obtenerTipoMovimientoRevalorizacion();

        List<DepositosMovimientoDTO> movimientos = lista.stream()
                .filter(x -> mayorCero(x.getValorRevalorizacion()))
                .map(x -> {
                    DepositosMovimientoDTO credito = new DepositosMovimientoDTO();

                    credito.setIdCuentaAhorro(x.getIdCuentaAhorro());
                    credito.setFechaMovimiento(input.getFechaProceso());
                    credito.setTipoComprobante(tipoComprobante);
                    credito.setNumeroComprobante(numeroComprobante);
                    credito.setTipoMovimiento(tipoMovimientoRevalorizacion);
                    credito.setModulo("02");
                    credito.setValorCredito(nvl(x.getValorRevalorizacion()));
                    credito.setValorDebito(BigDecimal.ZERO);
                    credito.setTarjeta("N");
                    credito.setEstablecimiento("PROC: REVALORIZACION APORTES");
                    credito.setModuloOrigen("DEPOSITOS");
                    credito.setProcesoOrigen("REVALORIZACION_APORTES");

                    return credito;
                })
                .toList();

        depositosMovimientoService.registrarCreditosMasivosSinValidarEstado(
                movimientos,
                idUsuario
        );

        contabilidadService.registrarComprobante(
                input,
                lista,
                idUsuario
        );

        registrarControl(input, lista, idUsuario);
        actualizarFechaUltimaLiquidacion(input, idUsuario);
    }

    private void validarFormaRevalorizacion(RevalorizacionEntradaDTO input) {

        Map<String, Object> forma = jdbc.queryForMap("""
            SELECT id_forma_ahorro, tiempo_liquidacion
            FROM depositos.formas_ahorro
            WHERE id_forma_ahorro = :formaId
        """, Map.of("formaId", input.getFormaId()));

        Integer tiempo = ((Number) forma.get("tiempo_liquidacion")).intValue();

        if (tiempo != 360) {
            throw new IllegalStateException(
                    "ERROR_VALIDACION|La forma seleccionada NO permite revalorización. "
                            + "(tiempo_liquidacion = " + tiempo + ")"
            );
        }
    }

    private void validarEntradaAplicacion(RevalorizacionEntradaDTO input) {

        validarEntradaPreview(input);

        if (input.getTipoComprobante() == null || input.getTipoComprobante().isBlank()) {
            throw new RuntimeException("El tipo de comprobante es obligatorio.");
        }
    }

    private void validarEntradaPreview(RevalorizacionEntradaDTO input) {

        if (input.getAgenciaId() == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }

        if (input.getFormaId() == null) {
            throw new RuntimeException("La forma de ahorro es obligatoria.");
        }

        if (input.getFechaProceso() == null) {
            throw new RuntimeException("La fecha de proceso es obligatoria.");
        }

        if (input.getFechaLiquidacion() == null) {
            throw new RuntimeException("La fecha de liquidación es obligatoria.");
        }
    }

    private void validarNoAplicado(RevalorizacionEntradaDTO input) {

        Integer count = jdbc.queryForObject("""
            SELECT COUNT(1)
            FROM depositos.liquidaciones_intereses_control
            WHERE id_agencia = :idAgencia
              AND id_forma_ahorro = :idFormaAhorro
              AND fecha_liquidacion = :fechaLiquidacion
              AND reversado = false
        """, new MapSqlParameterSource()
                        .addValue("idAgencia", input.getAgenciaId())
                        .addValue("idFormaAhorro", input.getFormaId())
                        .addValue("fechaLiquidacion", input.getFechaLiquidacion()),
                Integer.class
        );

        if (count != null && count > 0) {
            throw new RuntimeException("La revalorización ya fue aplicada para esta agencia, forma y fecha.");
        }
    }

    private void registrarControl(
            RevalorizacionEntradaDTO input,
            List<RevalorizacionItemDTO> lista,
            Integer idUsuario
    ) {

        BigDecimal totalRevalorizacion = lista.stream()
                .map(x -> nvl(x.getValorRevalorizacion()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        jdbc.update("""
            INSERT INTO depositos.liquidaciones_intereses_control (
                id_agencia,
                id_forma_ahorro,
                fecha_proceso,
                fecha_liquidacion,
                tipo_comprobante,
                numero_comprobante,
                cantidad_cuentas,
                total_interes_bruto,
                total_retencion,
                total_interes_neto,
                reversado,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            ) VALUES (
                :idAgencia,
                :idFormaAhorro,
                :fechaProceso,
                :fechaLiquidacion,
                :tipoComprobante,
                :numeroComprobante,
                :cantidadCuentas,
                :totalInteresBruto,
                0,
                :totalInteresNeto,
                false,
                :idUsuario,
                :idUsuario
            )
        """, new MapSqlParameterSource()
                .addValue("idAgencia", input.getAgenciaId())
                .addValue("idFormaAhorro", input.getFormaId())
                .addValue("fechaProceso", input.getFechaProceso())
                .addValue("fechaLiquidacion", input.getFechaLiquidacion())
                .addValue("tipoComprobante", input.getTipoComprobante())
                .addValue("numeroComprobante", input.getNumeroComprobante())
                .addValue("cantidadCuentas", lista.size())
                .addValue("totalInteresBruto", totalRevalorizacion)
                .addValue("totalInteresNeto", totalRevalorizacion)
                .addValue("idUsuario", idUsuario));
    }

    private void actualizarFechaUltimaLiquidacion(
            RevalorizacionEntradaDTO input,
            Integer idUsuario
    ) {
        jdbc.update("""
            UPDATE depositos.formas_ahorro
               SET fecha_ultima_liquidacion = :fechaLiquidacion,
                   fk_seguridad_edicion = :idUsuario,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_forma_ahorro = :idFormaAhorro
        """, new MapSqlParameterSource()
                .addValue("fechaLiquidacion", input.getFechaLiquidacion())
                .addValue("idUsuario", idUsuario)
                .addValue("idFormaAhorro", input.getFormaId()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerUltimaLiquidacion(
            Integer idAgencia,
            Integer idFormaAhorro
    ) {

        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT
                fecha_proceso,
                fecha_liquidacion,
                tipo_comprobante,
                numero_comprobante,
                cantidad_cuentas,
                total_interes_bruto,
                total_retencion,
                total_interes_neto
            FROM depositos.liquidaciones_intereses_control
            WHERE id_agencia = :idAgencia
              AND id_forma_ahorro = :idFormaAhorro
              AND reversado = false
            ORDER BY fecha_liquidacion DESC,
                     id_control DESC
            LIMIT 1
        """, new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia)
                .addValue("idFormaAhorro", idFormaAhorro));

        if (rows.isEmpty()) {
            return Map.of();
        }

        return rows.get(0);
    }

    private String obtenerTipoMovimientoRevalorizacion() {
        return "008";
    }

    private boolean mayorCero(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}