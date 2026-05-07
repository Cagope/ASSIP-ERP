package co.assip.erp.depositos.revalorizacion;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuenta;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuentaRepository;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionEntradaDTO;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevalorizacionContabilidadService {

    private final NamedParameterJdbcTemplate jdbc;
    private final PlanCuentaRepository planCuentaRepository;
    private final ContabilidadRegistroService contabilidadRegistroService;

    public void registrarComprobante(
            RevalorizacionEntradaDTO input,
            List<RevalorizacionItemDTO> lista,
            Integer idUsuario
    ) {

        Map<String, Object> forma = jdbc.queryForMap("""
            SELECT
                codigo_forma,
                nombre_forma,
                valor_minimo::numeric AS valor_minimo,
                cuenta_gasto,
                cuenta_forma_corto
            FROM depositos.formas_ahorro
            WHERE id_forma_ahorro = :formaId
        """, Map.of("formaId", input.getFormaId()));

        String codigoForma = String.valueOf(forma.get("codigo_forma"));
        String nombreForma = String.valueOf(forma.get("nombre_forma"));

        BigDecimal valorMinimo =
                (BigDecimal) forma.get("valor_minimo");

        Integer idCuentaGasto =
                ((Number) forma.get("cuenta_gasto")).intValue();

        Integer idCuentaForma =
                ((Number) forma.get("cuenta_forma_corto")).intValue();

        PlanCuenta cuentaGasto = planCuentaRepository.findById(idCuentaGasto)
                .orElseThrow(() -> new RuntimeException(
                        "No existe cuenta gasto id: " + idCuentaGasto
                ));

        PlanCuenta cuentaForma = planCuentaRepository.findById(idCuentaForma)
                .orElseThrow(() -> new RuntimeException(
                        "No existe cuenta forma id: " + idCuentaForma
                ));

        List<MovimientoContableDTO> movimientos = new ArrayList<>();

        for (RevalorizacionItemDTO item : lista) {

            if (item.getIdDatosPersonal() == null) {
                throw new RuntimeException(
                        "La cuenta "
                                + item.getCodigoCuenta()
                                + " no tiene idDatosPersonal para contabilizar."
                );
            }

            String detalleBase =
                    "REVAL APORTES CTA "
                            + item.getCodigoCuenta();

            if (mayorCero(item.getValorRevalorizacion())) {

                MovimientoContableDTO debitoGasto =
                        new MovimientoContableDTO();

                debitoGasto.setIdCatalogoCuenta(cuentaGasto.getId());
                debitoGasto.setIdDatosPersonal(item.getIdDatosPersonal());
                debitoGasto.setFechaAuxiliar(input.getFechaProceso());
                debitoGasto.setDetalleMovimiento(detalleBase);
                debitoGasto.setValorDebito(nvl(item.getValorRevalorizacion()));
                debitoGasto.setValorCredito(BigDecimal.ZERO);
                debitoGasto.setValorBase(nvl(item.getValorRevalorizacion()));

                movimientos.add(debitoGasto);

                MovimientoContableDTO creditoAportes =
                        new MovimientoContableDTO();

                creditoAportes.setIdCatalogoCuenta(cuentaForma.getId());
                creditoAportes.setIdDatosPersonal(item.getIdDatosPersonal());
                creditoAportes.setFechaAuxiliar(input.getFechaProceso());
                creditoAportes.setDetalleMovimiento(detalleBase);
                creditoAportes.setValorDebito(BigDecimal.ZERO);
                creditoAportes.setValorCredito(nvl(item.getValorRevalorizacion()));
                creditoAportes.setValorBase(nvl(item.getValorRevalorizacion()));

                movimientos.add(creditoAportes);
            }
        }

        if (movimientos.isEmpty()) {
            return;
        }

        String conceptoComprobante =
                "REVALORIZACION APORTES "
                        + codigoForma
                        + " "
                        + nombreForma
                        + " TASA "
                        + input.getTasaRevalorizacion()
                        + "% MIN "
                        + valorMinimo
                        + " "
                        + input.getFechaLiquidacion();

        OrigenComprobanteDTO origen =
                new OrigenComprobanteDTO();

        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("DEPOSITOS");
        origen.setProcesoOrigen("REVALORIZACION_APORTES");
        origen.setTablaOrigen("depositos.liquidaciones_intereses_control");

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
        return value == null ? BigDecimal.ZERO : value;
    }
}