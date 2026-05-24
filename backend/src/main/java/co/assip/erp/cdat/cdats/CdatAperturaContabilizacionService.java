package co.assip.erp.cdat.cdats;

import co.assip.erp.cajas.medios_pago.dto.MedioPagoBancoDTO;
import co.assip.erp.cajas.medios_pago.dto.MedioPagoDepositoDTO;
import co.assip.erp.cajas.medios_pago.dto.MedioPagoTrasladoAgenciaDTO;
import co.assip.erp.cajas.medios_pago.dto.MediosPagoDTO;
import co.assip.erp.cdat.cdats.dto.CdatSaveDTO;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.cdat.cdats.dto.CdatAperturaPreviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CdatAperturaContabilizacionService {

    private final CdatRepository repository;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    // =========================
    // CONTABILIZAR REAL
    // =========================
    @Transactional
    public void contabilizarApertura(Long idCuentaCdat, CdatSaveDTO dto, Integer idUsuario) {

        var cdat = repository.obtenerPorId(idCuentaCdat)
                .orElseThrow(() -> new RuntimeException("No se encontró el CDAT."));

        dto.setIdAgencia(cdat.getIdAgencia());
        dto.setIdDatosPersonal(cdat.getIdDatosPersonal());
        dto.setValorAperturaCdat(cdat.getValorAperturaCdat());
        dto.setPlazoMeses(cdat.getPlazoMeses());
        dto.setFechaAperturaCdat(cdat.getFechaAperturaCdat());

        // =========================
        // 🔴 CONCURRENCIA CSC (FOR UPDATE)
        // =========================
        String numeroComprobante = consecutivosComprobantesService.generarNumeroDefinitivo(
                dto.getTipoComprobante(),
                dto.getIdAgencia(),
                idUsuario
        );

        dto.setNumeroComprobante(numeroComprobante);

        // =========================
        // MOVIMIENTOS
        // =========================


        String documentoSoporte = "CDAT " + cdat.getCodigoCdat();

        List<MovimientoContableDTO> movimientos = armarMovimientos(dto, true, documentoSoporte);

        validarCuadre(movimientos);

        // =========================
        // ORIGEN
        // =========================
        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CDAT");
        origen.setProcesoOrigen("APERTURA_CDAT");
        origen.setTablaOrigen("cdat.cuentas_cdats");
        origen.setIdOrigen(idCuentaCdat);

        // =========================
        // REGISTRAR COMPROBANTE
        // =========================
        contabilidadRegistroService.registrarComprobante(
                dto.getIdAgencia(),
                dto.getTipoComprobante(),
                dto.getNumeroComprobante(),
                "Apertura CDAT " + cdat.getCodigoCdat(),
                origen,
                movimientos,
                idUsuario
        );

    }

    // =========================
    // PREVIEW
    // =========================
    public CdatAperturaPreviewDTO previewApertura(CdatSaveDTO dto) {

        List<MovimientoContableDTO> movimientos = armarMovimientos(dto, false, "PREVIEW CDAT");

        List<MovimientoContablePreviewDTO> preview = convertirAPreview(movimientos);

        BigDecimal totalDebito = totalDebito(movimientos);
        BigDecimal totalCredito = totalCredito(movimientos);
        BigDecimal diferencia = totalDebito.subtract(totalCredito);

        return CdatAperturaPreviewDTO.builder()
                .movimientos(preview)
                .totalDebito(totalDebito)
                .totalCredito(totalCredito)
                .diferencia(diferencia)
                .cuadrado(diferencia.compareTo(BigDecimal.ZERO) == 0)
                .build();
    }

    // =========================
    // CORE CONTABLE
    // =========================
    private List<MovimientoContableDTO> armarMovimientos(
            CdatSaveDTO dto,
            boolean exigirComprobante,
            String documentoSoporte
    ) {

        validarDatosBase(dto, exigirComprobante);

        List<MovimientoContableDTO> movimientos = new ArrayList<>();

        Integer idAgencia = dto.getIdAgencia();
        Integer idTercero = dto.getIdDatosPersonal();
        LocalDate fecha = dto.getFechaAperturaCdat();

        MediosPagoDTO mp = dto.getMediosPago();

        if (mp == null) {
            throw new RuntimeException("No se recibieron medios de pago.");
        }

        // =========================
        // DÉBITOS
        // =========================

        if (mayorCero(mp.getValorEfectivo())) {
            Integer cuentaCaja = repository.obtenerCuentaContableCaja(mp.getIdCaja());

            movimientos.add(crearMovimiento(
                    idAgencia, cuentaCaja, idTercero, fecha,
                    "Apertura CDAT - efectivo",
                    mp.getValorEfectivo(), BigDecimal.ZERO,
                    documentoSoporte
            ));
        }

        if (mayorCero(mp.getValorCheques())) {
            Integer cuentaCaja = repository.obtenerCuentaContableCaja(mp.getIdCaja());

            movimientos.add(crearMovimiento(
                    idAgencia, cuentaCaja, idTercero, fecha,
                    "Apertura CDAT - cheques",
                    mp.getValorCheques(), BigDecimal.ZERO,
                    documentoSoporte
            ));
        }

        if (mp.getDepositos() != null) {
            for (MedioPagoDepositoDTO d : mp.getDepositos()) {
                if (!mayorCero(d.getValorDebitar())) continue;

                Integer cuentaDeposito = repository
                        .obtenerCuentaContableFormaAhorroPorCuenta(d.getIdCuentaAhorro());

                movimientos.add(crearMovimiento(
                        idAgencia, cuentaDeposito, idTercero, fecha,
                        "Apertura CDAT - débito cuenta ahorro",
                        d.getValorDebitar(), BigDecimal.ZERO,
                        documentoSoporte
                ));
            }
        }

        if (mp.getBancos() != null) {
            for (MedioPagoBancoDTO b : mp.getBancos()) {
                if (!mayorCero(b.getValorBanco())) continue;

                movimientos.add(crearMovimiento(
                        idAgencia, b.getIdCatalogoCuentaBanco(), idTercero, fecha,
                        "Apertura CDAT - banco",
                        b.getValorBanco(), BigDecimal.ZERO,
                        documentoSoporte
                ));
            }
        }

        if (mp.getTrasladosAgencias() != null) {
            for (MedioPagoTrasladoAgenciaDTO t : mp.getTrasladosAgencias()) {
                if (!mayorCero(t.getValorTraslado())) continue;

                movimientos.add(crearMovimiento(
                        idAgencia, t.getIdCatalogoCuentaTraslado(), idTercero, fecha,
                        "Apertura CDAT - traslado",
                        t.getValorTraslado(), BigDecimal.ZERO,
                        documentoSoporte
                ));
            }
        }

        // =========================
        // CRÉDITO CDAT
        // =========================
        Integer cuentaCdat = repository.obtenerCuentaCapitalCdat(
                idAgencia,
                dto.getPlazoMeses(),
                "APERTURA_CDAT"
        );

        movimientos.add(crearMovimiento(
                idAgencia, cuentaCdat, idTercero, fecha,
                "Apertura CDAT",
                BigDecimal.ZERO, dto.getValorAperturaCdat(),
                documentoSoporte
        ));

        validarCuadre(movimientos);

        return movimientos;
    }

    // =========================
    // HELPERS
    // =========================

    private MovimientoContableDTO crearMovimiento(
            Integer idAgencia,
            Integer idCuenta,
            Integer idTercero,
            LocalDate fecha,
            String detalle,
            BigDecimal debito,
            BigDecimal credito,
            String documentoSoporte
    ) {

        MovimientoContableDTO m = new MovimientoContableDTO();
        m.setIdAgencia(idAgencia);
        m.setIdCatalogoCuenta(idCuenta);
        m.setIdDatosPersonal(idTercero);
        m.setFechaAuxiliar(fecha);
        m.setDetalleMovimiento(detalle);
        m.setValorDebito(nvl(debito));
        m.setValorCredito(nvl(credito));
        m.setDocumentoSoporte(documentoSoporte);
        return m;
    }

    private void validarDatosBase(CdatSaveDTO dto, boolean exigirComprobante) {
        if (dto.getIdAgencia() == null) throw new RuntimeException("Agencia obligatoria");
        if (dto.getIdDatosPersonal() == null) throw new RuntimeException("Titular obligatorio");
        if (dto.getFechaAperturaCdat() == null) throw new RuntimeException("Fecha obligatoria");
        if (!mayorCero(dto.getValorAperturaCdat())) throw new RuntimeException("Valor inválido");
        if (dto.getPlazoMeses() == null || dto.getPlazoMeses() <= 0)
            throw new RuntimeException("Plazo obligatorio");

        if (exigirComprobante) {
            if (dto.getTipoComprobante() == null) throw new RuntimeException("Tipo comprobante obligatorio");
        }
    }

    private void validarCuadre(List<MovimientoContableDTO> movimientos) {
        if (totalDebito(movimientos).compareTo(totalCredito(movimientos)) != 0) {
            throw new RuntimeException("No cuadra el comprobante");
        }
    }

    private BigDecimal totalDebito(List<MovimientoContableDTO> movimientos) {
        return movimientos.stream().map(m -> nvl(m.getValorDebito())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalCredito(List<MovimientoContableDTO> movimientos) {
        return movimientos.stream().map(m -> nvl(m.getValorCredito())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean mayorCero(BigDecimal v) {
        return nvl(v).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private List<MovimientoContablePreviewDTO> convertirAPreview(List<MovimientoContableDTO> movimientos) {

        List<MovimientoContablePreviewDTO> lista = new ArrayList<>();

        for (MovimientoContableDTO m : movimientos) {

            MovimientoContablePreviewDTO p = new MovimientoContablePreviewDTO();

            p.setIdAgencia(m.getIdAgencia());
            p.setFechaAuxiliar(m.getFechaAuxiliar());
            p.setIdCatalogoCuenta(m.getIdCatalogoCuenta());
            p.setIdDatosPersonal(m.getIdDatosPersonal());
            p.setDetalleMovimiento(m.getDetalleMovimiento());
            p.setValorDebito(m.getValorDebito());
            p.setValorCredito(m.getValorCredito());

            var cuenta = repository.obtenerCuentaInfo(m.getIdCatalogoCuenta());
            p.setCodigoCuenta(cuenta.getCodigo());
            p.setNombreCuenta(cuenta.getNombre());

            if (m.getIdDatosPersonal() != null) {
                var t = repository.obtenerTerceroInfo(m.getIdDatosPersonal());
                p.setDocumentoTercero(t.getDocumento());
                p.setNombreTercero(t.getNombre());
            }

            lista.add(p);
        }

        return lista;
    }
}