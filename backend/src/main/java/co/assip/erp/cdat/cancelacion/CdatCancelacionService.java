package co.assip.erp.cdat.cancelacion;

import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionEntradaDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionItemDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionPreviewDTO;
import co.assip.erp.cdat.cdats.CdatService;
import co.assip.erp.cdat.cdats.dto.CdatSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.assip.erp.cdat.movimientos.CdatMovimientoService;
import co.assip.erp.cdat.movimientos.dto.CdatMovimientoDTO;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.cajas.medios_pago.dto.MedioPagoDepositoDTO;
import co.assip.erp.cajas.medios_pago.dto.MediosPagoDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionFiltroDTO;
import java.util.List;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CdatCancelacionService {

    private final CdatCancelacionRepository repository;
    private final CdatCancelacionContabilizacionService contabilizacionService;
    private final CdatService cdatService;
    private final CdatMovimientoService cdatMovimientoService;
    private final DepositosMovimientoService depositosMovimientoService;

    public CdatCancelacionItemDTO obtenerPorId(Long idCuentaCdat) {

        return repository.obtenerPorId(idCuentaCdat)
                .orElseThrow(() ->
                        new RuntimeException("No se encontró el CDAT."));
    }

    public List<CdatCancelacionItemDTO> buscar(CdatCancelacionFiltroDTO filtro) {
        return repository.buscar(filtro);
    }

    public CdatCancelacionPreviewDTO preview(CdatCancelacionEntradaDTO dto) {

        validar(dto);

        CdatCancelacionItemDTO cdat = obtenerPorId(dto.getIdCuentaCdat());

        return contabilizacionService.preview(dto, cdat);
    }

    @Transactional
    public void aplicar(
            CdatCancelacionEntradaDTO dto,
            Integer idUsuario
    ) {

        validar(dto);

        CdatCancelacionItemDTO cdat = obtenerPorId(dto.getIdCuentaCdat());

        validarDiferenciaMediosPago(dto, cdat);

        contabilizacionService.contabilizar(
                dto,
                cdat,
                idUsuario
        );

        registrarMovimientoCancelacionCdat(dto, cdat, idUsuario);
        registrarMovimientosSalida(dto, cdat, idUsuario);
        registrarMovimientosEntrada(dto, cdat, idUsuario);

        // =========================
        // RENOVACIÓN
        // =========================
        if (mayorCero(dto.getValorRenovacion())) {

            CdatSaveDTO nuevo = construirRenovacion(
                    dto,
                    cdat
            );

            cdatService.guardar(
                    nuevo,
                    idUsuario,
                    cdat.getIdAgencia()
            );
        }

        // =========================
        // CANCELAR CDAT ORIGINAL
        // =========================
        repository.marcarCancelado(
                dto.getIdCuentaCdat(),
                dto.getFechaProceso(),
                idUsuario
        );
    }

    // =========================
    // RENOVACIÓN
    // =========================

    private CdatSaveDTO construirRenovacion(
            CdatCancelacionEntradaDTO dto,
            CdatCancelacionItemDTO cdat
    ) {

        CdatSaveDTO nuevo = new CdatSaveDTO();

        nuevo.setIdAgencia(cdat.getIdAgencia());
        nuevo.setIdDatosPersonal(cdat.getIdDatosPersonal());

        nuevo.setFechaAperturaCdat(dto.getFechaAperturaNuevoCdat());

        nuevo.setPlazoMeses(dto.getPlazoMesesNuevoCdat());
        nuevo.setPlazoDias(dto.getPlazoDiasNuevoCdat());

        nuevo.setValorAperturaCdat(dto.getValorRenovacion());

        nuevo.setTasaNominalAnual(dto.getTasaNominalAnualNuevoCdat());
        nuevo.setTasaEfectivaAnual(dto.getTasaEfectivaAnualNuevoCdat());

        nuevo.setRetencionFuenteCdat(dto.getRetencionFuenteNuevoCdat());

        nuevo.setAmortizacionDeposito(dto.getAmortizacionDepositoNuevoCdat());
        nuevo.setModalidadCdat("V");

        nuevo.setIdCuentaAhorro(dto.getIdCuentaAhorroNuevoCdat());

        nuevo.setOrigenCdat("R");
        nuevo.setIdCuentaCdatOrigen(cdat.getIdCuentaCdat());

        nuevo.setTipoComprobante(dto.getTipoComprobante());
        nuevo.setNumeroComprobante(dto.getNumeroComprobante());
        nuevo.setFechaComprobante(dto.getFechaProceso());

        nuevo.setMediosPago(dto.getMediosPagoEntrada());

        nuevo.setObservacion(dto.getObservacionNuevoCdat());

        return nuevo;
    }

    // =========================
    // VALIDACIONES
    // =========================

    private void validar(CdatCancelacionEntradaDTO dto) {

        if (dto.getIdCuentaCdat() == null) {
            throw new RuntimeException("El CDAT es obligatorio.");
        }

        if (dto.getFechaProceso() == null) {
            throw new RuntimeException("La fecha del proceso es obligatoria.");
        }

        if (dto.getTipoComprobante() == null
                || dto.getTipoComprobante().isBlank()) {

            throw new RuntimeException(
                    "El tipo de comprobante es obligatorio.");
        }

        if (mayorCero(dto.getValorRenovacion())) {

            if (dto.getFechaAperturaNuevoCdat() == null) {
                throw new RuntimeException("La fecha de apertura del nuevo CDAT es obligatoria.");
            }

            if (dto.getFechaVencimientoNuevoCdat() == null) {
                throw new RuntimeException("La fecha de vencimiento del nuevo CDAT es obligatoria.");
            }

            if (dto.getPlazoMesesNuevoCdat() == null
                    || dto.getPlazoMesesNuevoCdat() <= 0) {
                throw new RuntimeException("El plazo en meses del nuevo CDAT es obligatorio.");
            }

            if (dto.getTasaNominalAnualNuevoCdat() == null
                    || dto.getTasaNominalAnualNuevoCdat().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("La tasa nominal anual del nuevo CDAT es obligatoria.");
            }

            if (dto.getAmortizacionDepositoNuevoCdat() == null
                    || dto.getAmortizacionDepositoNuevoCdat().isBlank()) {
                throw new RuntimeException("La amortización del nuevo CDAT es obligatoria.");
            }

            if (dto.getIdCuentaAhorroNuevoCdat() == null) {
                throw new RuntimeException("La cuenta de ahorro para intereses del nuevo CDAT es obligatoria.");
            }
        }

    }

    private boolean mayorCero(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void registrarMovimientoCancelacionCdat(
            CdatCancelacionEntradaDTO dto,
            CdatCancelacionItemDTO cdat,
            Integer idUsuario
    ) {
        CdatMovimientoDTO mov = new CdatMovimientoDTO();

        mov.setIdCuentaCdat(cdat.getIdCuentaCdat());
        mov.setFechaMovimiento(dto.getFechaProceso());
        mov.setTipoComprobante(dto.getTipoComprobante());
        mov.setNumeroComprobante(dto.getNumeroComprobante());
        mov.setTipoMovimiento("551");
        mov.setModulo("11");
        mov.setTarjeta("N");
        mov.setEstablecimiento("CANCELACION CDAT");
        mov.setValorDebito(cdat.getValorCapital());
        mov.setValorCredito(BigDecimal.ZERO);

        mov.setModuloOrigen("CDAT");
        mov.setProcesoOrigen("CANCELACION_CDAT");
        mov.setIdOrigen(cdat.getIdCuentaCdat());

        cdatMovimientoService.registrarDebito(mov, idUsuario);
    }

    private void registrarMovimientosSalida(
            CdatCancelacionEntradaDTO dto,
            CdatCancelacionItemDTO cdat,
            Integer idUsuario
    ) {

        if (dto.getMediosPagoSalida() == null) {
            return;
        }

        if (dto.getMediosPagoSalida().getDepositos() == null) {
            return;
        }

        for (MedioPagoDepositoDTO d :
                dto.getMediosPagoSalida().getDepositos()) {

            if (!mayorCero(d.getValorDebitar())) {
                continue;
            }

            DepositosMovimientoDTO mov = new DepositosMovimientoDTO();

            mov.setIdCuentaAhorro(d.getIdCuentaAhorro());

            mov.setFechaMovimiento(dto.getFechaProceso());

            mov.setTipoComprobante(dto.getTipoComprobante());
            mov.setNumeroComprobante(dto.getNumeroComprobante());

            mov.setTipoMovimiento("008");

            mov.setModulo("11");
            mov.setTarjeta("N");

            mov.setEstablecimiento("CANCELACION CDAT");

            mov.setValorDebito(BigDecimal.ZERO);
            mov.setValorCredito(d.getValorDebitar());

            mov.setModuloOrigen("CDAT");
            mov.setProcesoOrigen("CANCELACION_CDAT");
            mov.setIdOrigen(cdat.getIdCuentaCdat());

            depositosMovimientoService.registrarCredito(
                    mov,
                    idUsuario
            );
        }
    }

    private void registrarMovimientosEntrada(
            CdatCancelacionEntradaDTO dto,
            CdatCancelacionItemDTO cdat,
            Integer idUsuario
    ) {

        if (dto.getMediosPagoEntrada() == null) {
            return;
        }

        if (dto.getMediosPagoEntrada().getDepositos() == null) {
            return;
        }

        for (MedioPagoDepositoDTO d :
                dto.getMediosPagoEntrada().getDepositos()) {

            if (!mayorCero(d.getValorDebitar())) {
                continue;
            }

            DepositosMovimientoDTO mov = new DepositosMovimientoDTO();

            mov.setIdCuentaAhorro(d.getIdCuentaAhorro());

            mov.setFechaMovimiento(dto.getFechaProceso());

            mov.setTipoComprobante(dto.getTipoComprobante());
            mov.setNumeroComprobante(dto.getNumeroComprobante());

            mov.setTipoMovimiento("773");

            mov.setModulo("11");
            mov.setTarjeta("N");

            mov.setEstablecimiento("RENOVACION CDAT");

            mov.setValorDebito(d.getValorDebitar());
            mov.setValorCredito(BigDecimal.ZERO);

            mov.setModuloOrigen("CDAT");
            mov.setProcesoOrigen("RENOVACION_CDAT");
            mov.setIdOrigen(cdat.getIdCuentaCdat());

            depositosMovimientoService.registrarDebito(
                    mov,
                    idUsuario
            );
        }
    }

    private void validarDiferenciaMediosPago(
            CdatCancelacionEntradaDTO dto,
            CdatCancelacionItemDTO cdat
    ) {

        BigDecimal disponible = nvl(cdat.getValorDisponible());
        BigDecimal renovacion = nvl(dto.getValorRenovacion());

        BigDecimal diferencia = renovacion.subtract(disponible);

        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal totalEntrada = totalMediosPago(dto.getMediosPagoEntrada());

            if (totalEntrada.compareTo(diferencia) != 0) {
                throw new RuntimeException(
                        "La diferencia de entrada no coincide con los medios de pago."
                );
            }
        }

        if (diferencia.compareTo(BigDecimal.ZERO) < 0) {

            BigDecimal totalSalida = totalMediosPago(dto.getMediosPagoSalida());

            if (totalSalida.compareTo(diferencia.abs()) != 0) {
                throw new RuntimeException(
                        "La diferencia de salida no coincide con los medios de pago."
                );
            }
        }
    }
    private BigDecimal totalMediosPago(MediosPagoDTO mediosPago) {

        if (mediosPago == null) {
            return BigDecimal.ZERO;
        }

        return nvl(mediosPago.getValorEfectivo())
                .add(nvl(mediosPago.getValorCheques()))
                .add(nvl(mediosPago.getValorDepositos()))
                .add(nvl(mediosPago.getValorBancos()))
                .add(nvl(mediosPago.getValorTrasladosAgencias()));
    }

}