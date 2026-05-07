package co.assip.erp.depositos.interesmensual_tac;

import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACEntradaDTO;
import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACItemDTO;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
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
public class InteresMensualTACService {

    private final InteresMensualTACRepository repository;
    private final NamedParameterJdbcTemplate jdbc;
    private final DepositosMovimientoService depositosMovimientoService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final InteresMensualTACContabilidadService contabilidadService;

    @Transactional(readOnly = true)
    public List<InteresMensualTACItemDTO> liquidar(InteresMensualTACEntradaDTO input) {

        validarEntradaPreview(input);
        validarNoAplicado(input);
        validarFormaTAC(input);

        List<InteresMensualTACItemDTO> lista = repository.liquidar(input);

        return lista.stream()
                .filter(x -> mayorCero(x.getSaldoActual()))
                .filter(x -> mayorCero(x.getInteresBruto()))
                .toList();
    }

    @Transactional
    public void aplicar(InteresMensualTACEntradaDTO input, Integer idUsuario) {

        validarEntradaAplicacion(input);
        validarNoAplicado(input);
        validarFormaTAC(input);

        List<InteresMensualTACItemDTO> lista = liquidar(input);

        if (lista.isEmpty()) {
            throw new RuntimeException("No hay intereses TAC para aplicar.");
        }

        String tipoComprobante = input.getTipoComprobante();

        String numeroComprobante = consecutivosComprobantesService.generarNumeroDefinitivo(
                tipoComprobante,
                input.getAgenciaId(),
                idUsuario
        );

        input.setNumeroComprobante(numeroComprobante);

        List<DepositosMovimientoDTO> movimientos = lista.stream()
                .flatMap(x -> {
                    List<DepositosMovimientoDTO> list = new java.util.ArrayList<>();

                    if (mayorCero(x.getInteresNeto())) {
                        DepositosMovimientoDTO credito = new DepositosMovimientoDTO();
                        credito.setIdCuentaAhorro(x.getIdCuentaAhorro());
                        credito.setFechaMovimiento(input.getFechaProceso());
                        credito.setTipoComprobante(tipoComprobante);
                        credito.setNumeroComprobante(numeroComprobante);
                        credito.setTipoMovimiento("005");
                        credito.setModulo("02");
                        credito.setValorCredito(nvl(x.getInteresNeto()));
                        credito.setValorDebito(BigDecimal.ZERO);
                        credito.setTarjeta("N");
                        credito.setEstablecimiento("PROC: INTERES MENSUAL TAC");
                        credito.setModuloOrigen("DEPOSITOS");
                        credito.setProcesoOrigen("INTERES_MENSUAL_TAC");
                        list.add(credito);
                    }

                    if (mayorCero(x.getRetencion())) {
                        DepositosMovimientoDTO debito = new DepositosMovimientoDTO();
                        debito.setIdCuentaAhorro(x.getIdCuentaAhorro());
                        debito.setFechaMovimiento(input.getFechaProceso());
                        debito.setTipoComprobante(tipoComprobante);
                        debito.setNumeroComprobante(numeroComprobante);
                        debito.setTipoMovimiento("556");
                        debito.setModulo("02");
                        debito.setValorDebito(nvl(x.getRetencion()));
                        debito.setValorCredito(BigDecimal.ZERO);
                        debito.setTarjeta("N");
                        debito.setEstablecimiento("PROC: RETENCION INTERES TAC");
                        debito.setModuloOrigen("DEPOSITOS");
                        debito.setProcesoOrigen("INTERES_MENSUAL_TAC");
                        list.add(debito);
                    }

                    return list.stream();
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

    private void validarFormaTAC(InteresMensualTACEntradaDTO input) {

        Integer formaId = input.getFormaId();

        if (formaId == null || !(formaId == 7 || formaId == 14)) {
            throw new IllegalStateException(
                    "ERROR_VALIDACION|La forma seleccionada NO es TAC. Solo se permiten 7 y 14."
            );
        }
    }

    private void validarEntradaAplicacion(InteresMensualTACEntradaDTO input) {

        validarEntradaPreview(input);

        if (input.getTipoComprobante() == null || input.getTipoComprobante().isBlank()) {
            throw new RuntimeException("El tipo de comprobante es obligatorio.");
        }
    }

    private void validarEntradaPreview(InteresMensualTACEntradaDTO input) {

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

    private void validarNoAplicado(InteresMensualTACEntradaDTO input) {

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
            throw new RuntimeException("La liquidación de intereses ya fue aplicada para esta agencia, forma y fecha.");
        }
    }

    private void registrarControl(
            InteresMensualTACEntradaDTO input,
            List<InteresMensualTACItemDTO> lista,
            Integer idUsuario
    ) {

        BigDecimal totalBruto = lista.stream()
                .map(x -> nvl(x.getInteresBruto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRetencion = lista.stream()
                .map(x -> nvl(x.getRetencion()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNeto = lista.stream()
                .map(x -> nvl(x.getInteresNeto()))
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
                :totalRetencion,
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
                .addValue("totalInteresBruto", totalBruto)
                .addValue("totalRetencion", totalRetencion)
                .addValue("totalInteresNeto", totalNeto)
                .addValue("idUsuario", idUsuario));
    }

    private void actualizarFechaUltimaLiquidacion(
            InteresMensualTACEntradaDTO input,
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

    private boolean mayorCero(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}