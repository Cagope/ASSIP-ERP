package co.assip.erp.depositos.interesmensual_sm;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuenta;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuentaRepository;
import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMEntradaDTO;
import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteresMensualSMContabilidadService {

    private final NamedParameterJdbcTemplate jdbc;
    private final PlanCuentaRepository planCuentaRepository;
    private final ContabilidadRegistroService contabilidadRegistroService;

    public void registrarComprobante(
            InteresMensualSMEntradaDTO input,
            List<InteresMensualSMItemDTO> lista,
            Integer idUsuario
    ) {

        Map<String, Object> forma = jdbc.queryForMap("""
            SELECT
                codigo_forma,
                nombre_forma,
                tasa_interes_forma,
                valor_minimo,

                cuenta_gasto,
                cuenta_forma_corto,
                cuenta_retencion_fuente

            FROM depositos.formas_ahorro
            WHERE id_forma_ahorro = :formaId
        """, Map.of("formaId", input.getFormaId()));

        String codigoForma =
                String.valueOf(forma.get("codigo_forma"));

        String nombreForma =
                String.valueOf(forma.get("nombre_forma"));

        BigDecimal tasaInteres =
                (BigDecimal) forma.get("tasa_interes_forma");

        BigDecimal valorMinimo =
                (BigDecimal) forma.get("valor_minimo");

        Integer idCuentaGasto =
                ((Number) forma.get("cuenta_gasto")).intValue();

        Integer idCuentaForma =
                ((Number) forma.get("cuenta_forma_corto")).intValue();

        Integer idCuentaRetencion =
                forma.get("cuenta_retencion_fuente") != null
                        ? ((Number) forma.get("cuenta_retencion_fuente")).intValue()
                        : null;

        PlanCuenta cuentaGasto = planCuentaRepository.findById(idCuentaGasto)
                .orElseThrow(() -> new RuntimeException(
                        "No existe cuenta gasto id: " + idCuentaGasto
                ));

        PlanCuenta cuentaForma = planCuentaRepository.findById(idCuentaForma)
                .orElseThrow(() -> new RuntimeException(
                        "No existe cuenta forma id: " + idCuentaForma
                ));

        PlanCuenta cuentaRetencion = null;

        if (idCuentaRetencion != null) {

            cuentaRetencion = planCuentaRepository.findById(idCuentaRetencion)
                    .orElseThrow(() -> new RuntimeException(
                            "No existe cuenta retención id: "
                                    + idCuentaRetencion
                    ));
        }

        List<MovimientoContableDTO> movimientos = new ArrayList<>();

        for (InteresMensualSMItemDTO item : lista) {

            if (item.getIdDatosPersonal() == null) {

                throw new RuntimeException(
                        "La cuenta "
                                + item.getCodigoCuenta()
                                + " no tiene idDatosPersonal para contabilizar."
                );
            }

            String detalleBase =
                    "LIQ INT MENSUAL SM CTA "
                            + item.getCodigoCuenta();

            // =====================================================
            // DÉBITO GASTO INTERESES
            // =====================================================
            if (mayorCero(item.getInteresBruto())) {

                MovimientoContableDTO debitoGasto =
                        new MovimientoContableDTO();

                debitoGasto.setIdCatalogoCuenta(
                        cuentaGasto.getId()
                );

                debitoGasto.setIdDatosPersonal(
                        item.getIdDatosPersonal()
                );

                debitoGasto.setFechaAuxiliar(
                        input.getFechaProceso()
                );

                debitoGasto.setDetalleMovimiento(
                        detalleBase
                );

                debitoGasto.setValorDebito(
                        nvl(item.getInteresBruto())
                );

                debitoGasto.setValorCredito(
                        BigDecimal.ZERO
                );

                debitoGasto.setValorBase(
                        nvl(item.getInteresBruto())
                );

                movimientos.add(debitoGasto);
            }

            // =====================================================
            // CRÉDITO AHORROS
            // =====================================================
            if (mayorCero(item.getInteresNeto())) {

                MovimientoContableDTO creditoAhorro =
                        new MovimientoContableDTO();

                creditoAhorro.setIdCatalogoCuenta(
                        cuentaForma.getId()
                );

                creditoAhorro.setIdDatosPersonal(
                        item.getIdDatosPersonal()
                );

                creditoAhorro.setFechaAuxiliar(
                        input.getFechaProceso()
                );

                creditoAhorro.setDetalleMovimiento(
                        detalleBase
                );

                creditoAhorro.setValorDebito(
                        BigDecimal.ZERO
                );

                creditoAhorro.setValorCredito(
                        nvl(item.getInteresNeto())
                );

                creditoAhorro.setValorBase(
                        nvl(item.getInteresNeto())
                );

                movimientos.add(creditoAhorro);
            }

            // =====================================================
            // CRÉDITO RETENCIÓN
            // =====================================================
            if (
                    cuentaRetencion != null
                            && mayorCero(item.getRetencion())
            ) {

                MovimientoContableDTO creditoRetencion =
                        new MovimientoContableDTO();

                creditoRetencion.setIdCatalogoCuenta(
                        cuentaRetencion.getId()
                );

                creditoRetencion.setIdDatosPersonal(
                        item.getIdDatosPersonal()
                );

                creditoRetencion.setFechaAuxiliar(
                        input.getFechaProceso()
                );

                creditoRetencion.setDetalleMovimiento(
                        "RET INT MENSUAL SM CTA "
                                + item.getCodigoCuenta()
                );

                creditoRetencion.setValorDebito(
                        BigDecimal.ZERO
                );

                creditoRetencion.setValorCredito(
                        nvl(item.getRetencion())
                );

                creditoRetencion.setValorBase(
                        nvl(item.getRetencion())
                );

                movimientos.add(creditoRetencion);
            }
        }

        if (movimientos.isEmpty()) {
            return;
        }

        String conceptoComprobante =
                "LIQ INT MENSUAL SM "
                        + codigoForma
                        + " "
                        + nombreForma
                        + " TASA "
                        + tasaInteres
                        + "% MIN "
                        + valorMinimo
                        + " "
                        + input.getFechaLiquidacion();

        OrigenComprobanteDTO origen =
                new OrigenComprobanteDTO();

        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("DEPOSITOS");
        origen.setProcesoOrigen("INTERES_MENSUAL_SM");
        origen.setTablaOrigen(
                "depositos.liquidaciones_intereses_control"
        );

        contabilidadRegistroService.registrarComprobante(
                input.getAgenciaId(),
                input.getTipoComprobante(),
                input.getNumeroComprobante(),
                conceptoComprobante,
                origen,
                movimientos,
                idUsuario
        );
    }

    private boolean mayorCero(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}