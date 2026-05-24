package co.assip.erp.cdat.cancelacion;

import co.assip.erp.cajas.medios_pago.dto.MedioPagoBancoDTO;
import co.assip.erp.cajas.medios_pago.dto.MedioPagoDepositoDTO;
import co.assip.erp.cajas.medios_pago.dto.MedioPagoTrasladoAgenciaDTO;
import co.assip.erp.cajas.medios_pago.dto.MediosPagoDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionEntradaDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionItemDTO;
import co.assip.erp.cdat.cancelacion.dto.CdatCancelacionPreviewDTO;
import co.assip.erp.cdat.cdats.CdatRepository;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CdatCancelacionContabilizacionService {

    private final CdatRepository cdatRepository;
    private final CdatCancelacionRepository cancelacionRepository;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    public CdatCancelacionPreviewDTO preview(
            CdatCancelacionEntradaDTO input,
            CdatCancelacionItemDTO cdat
    ) {
        List<MovimientoContableDTO> movimientos =
                armarMovimientos(input, cdat, false);

        List<MovimientoContablePreviewDTO> preview =
                convertirAPreview(movimientos);

        BigDecimal totalDebito = totalDebito(movimientos);
        BigDecimal totalCredito = totalCredito(movimientos);
        BigDecimal diferencia = totalDebito.subtract(totalCredito);

        return CdatCancelacionPreviewDTO.builder()
                .cdat(cdat)
                .valorCapital(cdat.getValorCapital())
                .valorInteresCausado(cdat.getValorInteresCausado())
                .valorInteresCorriente(cdat.getValorInteresCorriente())
                .valorRetencion(cdat.getValorRetencion())
                .valorDisponible(cdat.getValorDisponible())
                .valorRenovacion(nvl(input.getValorRenovacion()))
                .valorDiferencia(calcularDiferencia(cdat, input))
                .tipoOperacionDiferencia(calcularTipoOperacion(cdat, input))
                .movimientosContables(preview)
                .totalDebito(totalDebito)
                .totalCredito(totalCredito)
                .diferenciaContable(diferencia)
                .cuadrado(diferencia.compareTo(BigDecimal.ZERO) == 0)
                .build();
    }

    @Transactional
    public void contabilizar(
            CdatCancelacionEntradaDTO input,
            CdatCancelacionItemDTO cdat,
            Integer idUsuario
    ) {
        String numeroComprobante =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        input.getTipoComprobante(),
                        cdat.getIdAgencia(),
                        idUsuario
                );

        input.setNumeroComprobante(numeroComprobante);

        List<MovimientoContableDTO> movimientos =
                armarMovimientos(input, cdat, true);

        validarCuadre(movimientos);

        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CDAT");
        origen.setProcesoOrigen("CANCELACION_CDAT");
        origen.setTablaOrigen("cdat.cuentas_cdats");
        origen.setIdOrigen(cdat.getIdCuentaCdat());

        String concepto =
                "Cancelación CDAT "
                        + cdat.getCodigoCdat()
                        + " Doc "
                        + cdat.getDocumento()
                        + " Plazo "
                        + cdat.getPlazoMeses()
                        + " meses";

        contabilidadRegistroService.registrarComprobante(
                cdat.getIdAgencia(),
                input.getTipoComprobante(),
                input.getNumeroComprobante(),
                concepto,
                origen,
                movimientos,
                idUsuario
        );
    }

    private List<MovimientoContableDTO> armarMovimientos(
            CdatCancelacionEntradaDTO input,
            CdatCancelacionItemDTO cdat,
            boolean exigirComprobante
    ) {
        validarDatosBase(input, cdat, exigirComprobante);

        List<MovimientoContableDTO> movimientos = new ArrayList<>();

        Integer idAgencia = cdat.getIdAgencia();
        Integer idTercero = cdat.getIdDatosPersonal();
        LocalDate fecha = input.getFechaProceso();
        String documentoSoporte = "CDAT " + cdat.getCodigoCdat();

        BigDecimal capital = nvl(cdat.getValorCapital());
        BigDecimal interesCausado = nvl(cdat.getValorInteresCausado());
        BigDecimal interesCorriente = nvl(cdat.getValorInteresCorriente());
        BigDecimal retencion = nvl(cdat.getValorRetencion());
        BigDecimal renovacion = nvl(input.getValorRenovacion());
        BigDecimal disponible = nvl(cdat.getValorDisponible());

        BigDecimal diferencia = disponible.subtract(renovacion);

        if (mayorCero(capital)) {
            Integer cuentaCapital = cdatRepository.obtenerCuentaCapitalCdat(
                    idAgencia,
                    cdat.getPlazoMeses(),
                    "APERTURA_CDAT"
            );

            movimientos.add(crearMovimiento(
                    idAgencia,
                    cuentaCapital,
                    idTercero,
                    fecha,
                    "Cancelación CDAT - capital título anterior",
                    capital,
                    BigDecimal.ZERO,
                    documentoSoporte
            ));
        }

        if (mayorCero(interesCausado)) {
            Integer cuentaInteresCausado =
                    cancelacionRepository.obtenerCuentaConceptoCancelacionCdat(
                            idAgencia,
                            "INTERES_CAUSADO"
                    );

            movimientos.add(crearMovimiento(
                    idAgencia,
                    cuentaInteresCausado,
                    idTercero,
                    fecha,
                    "Cancelación CDAT - intereses causados",
                    interesCausado,
                    BigDecimal.ZERO,
                    documentoSoporte
            ));
        }

        if (mayorCero(interesCorriente)) {
            Integer cuentaGastoInteres =
                    cancelacionRepository.obtenerCuentaConceptoCancelacionCdat(
                            idAgencia,
                            "CAUSACION_INTERES_CDAT"
                    );

            movimientos.add(crearMovimiento(
                    idAgencia,
                    cuentaGastoInteres,
                    idTercero,
                    fecha,
                    "Cancelación CDAT - intereses corrientes",
                    interesCorriente,
                    BigDecimal.ZERO,
                    documentoSoporte
            ));
        }

        if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal valorEntrada = diferencia.abs();

            validarTotalMediosPago(
                    input.getMediosPagoEntrada(),
                    valorEntrada,
                    "entrada"
            );

            agregarMediosPago(
                    movimientos,
                    input.getMediosPagoEntrada(),
                    idAgencia,
                    idTercero,
                    fecha,
                    documentoSoporte,
                    "Renovación CDAT - entrada adicional",
                    true
            );
        }

        if (mayorCero(retencion)) {
            Integer cuentaRetencion =
                    cancelacionRepository.obtenerCuentaConceptoCancelacionCdat(
                            idAgencia,
                            "RETENCION_CDAT"
                    );

            movimientos.add(crearMovimiento(
                    idAgencia,
                    cuentaRetencion,
                    idTercero,
                    fecha,
                    "Cancelación CDAT - retención en la fuente",
                    BigDecimal.ZERO,
                    retencion,
                    documentoSoporte
            ));
        }

        if (mayorCero(renovacion)) {
            Integer cuentaNuevoCdat = cdatRepository.obtenerCuentaCapitalCdat(
                    idAgencia,
                    input.getPlazoMesesNuevoCdat() != null
                            ? input.getPlazoMesesNuevoCdat()
                            : cdat.getPlazoMeses(),
                    "APERTURA_CDAT"
            );

            movimientos.add(crearMovimiento(
                    idAgencia,
                    cuentaNuevoCdat,
                    idTercero,
                    fecha,
                    "Renovación CDAT - nuevo título",
                    BigDecimal.ZERO,
                    renovacion,
                    documentoSoporte
            ));
        }

        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal valorSalida = diferencia;

            validarTotalMediosPago(
                    input.getMediosPagoSalida(),
                    valorSalida,
                    "salida"
            );

            agregarMediosPago(
                    movimientos,
                    input.getMediosPagoSalida(),
                    idAgencia,
                    idTercero,
                    fecha,
                    documentoSoporte,
                    "Cancelación CDAT - salida al cliente",
                    false
            );
        }

        validarCuadre(movimientos);

        return movimientos;
    }

    private void agregarMediosPago(
            List<MovimientoContableDTO> movimientos,
            MediosPagoDTO mp,
            Integer idAgencia,
            Integer idTercero,
            LocalDate fecha,
            String documentoSoporte,
            String prefijoDetalle,
            boolean esDebito
    ) {
        if (mp == null) {
            throw new RuntimeException("No se recibieron medios de pago.");
        }

        if (mayorCero(mp.getValorEfectivo())) {
            Integer cuentaCaja = cdatRepository.obtenerCuentaContableCaja(mp.getIdCaja());

            movimientos.add(crearMovimientoPorNaturaleza(
                    idAgencia,
                    cuentaCaja,
                    idTercero,
                    fecha,
                    prefijoDetalle + " - efectivo",
                    mp.getValorEfectivo(),
                    documentoSoporte,
                    esDebito
            ));
        }

        if (mayorCero(mp.getValorCheques())) {
            Integer cuentaCaja = cdatRepository.obtenerCuentaContableCaja(mp.getIdCaja());

            movimientos.add(crearMovimientoPorNaturaleza(
                    idAgencia,
                    cuentaCaja,
                    idTercero,
                    fecha,
                    prefijoDetalle + " - cheques",
                    mp.getValorCheques(),
                    documentoSoporte,
                    esDebito
            ));
        }

        if (mp.getDepositos() != null) {
            for (MedioPagoDepositoDTO d : mp.getDepositos()) {
                if (!mayorCero(d.getValorDebitar())) continue;

                Integer cuentaDeposito =
                        cdatRepository.obtenerCuentaContableFormaAhorroPorCuenta(
                                d.getIdCuentaAhorro()
                        );

                movimientos.add(crearMovimientoPorNaturaleza(
                        idAgencia,
                        cuentaDeposito,
                        idTercero,
                        fecha,
                        prefijoDetalle + " - cuenta ahorro",
                        d.getValorDebitar(),
                        documentoSoporte,
                        esDebito
                ));
            }
        }

        if (mp.getBancos() != null) {
            for (MedioPagoBancoDTO b : mp.getBancos()) {
                if (!mayorCero(b.getValorBanco())) continue;

                movimientos.add(crearMovimientoPorNaturaleza(
                        idAgencia,
                        b.getIdCatalogoCuentaBanco(),
                        idTercero,
                        fecha,
                        prefijoDetalle + " - banco",
                        b.getValorBanco(),
                        documentoSoporte,
                        esDebito
                ));
            }
        }

        if (mp.getTrasladosAgencias() != null) {
            for (MedioPagoTrasladoAgenciaDTO t : mp.getTrasladosAgencias()) {
                if (!mayorCero(t.getValorTraslado())) continue;

                movimientos.add(crearMovimientoPorNaturaleza(
                        idAgencia,
                        t.getIdCatalogoCuentaTraslado(),
                        idTercero,
                        fecha,
                        prefijoDetalle + " - traslado agencia",
                        t.getValorTraslado(),
                        documentoSoporte,
                        esDebito
                ));
            }
        }
    }

    private void validarTotalMediosPago(
            MediosPagoDTO mp,
            BigDecimal valorEsperado,
            String tipo
    ) {
        if (mp == null) {
            throw new RuntimeException("Debe registrar medios de pago de " + tipo + ".");
        }

        BigDecimal total = nvl(mp.getValorEfectivo())
                .add(nvl(mp.getValorCheques()))
                .add(nvl(mp.getValorDepositos()))
                .add(nvl(mp.getValorBancos()))
                .add(nvl(mp.getValorTrasladosAgencias()));

        if (total.compareTo(valorEsperado) != 0) {
            throw new RuntimeException(
                    "El total de medios de pago de "
                            + tipo
                            + " no coincide. Esperado: "
                            + valorEsperado
                            + ", recibido: "
                            + total
            );
        }
    }

    private MovimientoContableDTO crearMovimientoPorNaturaleza(
            Integer idAgencia,
            Integer idCuenta,
            Integer idTercero,
            LocalDate fecha,
            String detalle,
            BigDecimal valor,
            String documentoSoporte,
            boolean esDebito
    ) {
        return crearMovimiento(
                idAgencia,
                idCuenta,
                idTercero,
                fecha,
                detalle,
                esDebito ? valor : BigDecimal.ZERO,
                esDebito ? BigDecimal.ZERO : valor,
                documentoSoporte
        );
    }

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
        if (idCuenta == null) {
            throw new RuntimeException("Cuenta contable no configurada para: " + detalle);
        }

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

    private void validarDatosBase(
            CdatCancelacionEntradaDTO input,
            CdatCancelacionItemDTO cdat,
            boolean exigirComprobante
    ) {
        if (cdat.getIdAgencia() == null) {
            throw new RuntimeException("Agencia obligatoria.");
        }

        if (cdat.getIdDatosPersonal() == null) {
            throw new RuntimeException("Titular obligatorio.");
        }

        if (input.getFechaProceso() == null) {
            throw new RuntimeException("Fecha de proceso obligatoria.");
        }

        if (exigirComprobante &&
                (input.getTipoComprobante() == null || input.getTipoComprobante().isBlank())) {
            throw new RuntimeException("Tipo comprobante obligatorio.");
        }
    }

    private void validarCuadre(List<MovimientoContableDTO> movimientos) {
        if (totalDebito(movimientos).compareTo(totalCredito(movimientos)) != 0) {
            throw new RuntimeException("No cuadra el comprobante de cancelación CDAT.");
        }
    }

    private BigDecimal totalDebito(List<MovimientoContableDTO> movimientos) {
        return movimientos.stream()
                .map(m -> nvl(m.getValorDebito()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalCredito(List<MovimientoContableDTO> movimientos) {
        return movimientos.stream()
                .map(m -> nvl(m.getValorCredito()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularDiferencia(
            CdatCancelacionItemDTO cdat,
            CdatCancelacionEntradaDTO input
    ) {
        return nvl(cdat.getValorDisponible())
                .subtract(nvl(input.getValorRenovacion()))
                .abs();
    }

    private String calcularTipoOperacion(
            CdatCancelacionItemDTO cdat,
            CdatCancelacionEntradaDTO input
    ) {
        BigDecimal diferencia =
                nvl(input.getValorRenovacion())
                        .subtract(nvl(cdat.getValorDisponible()));

        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            return "ENTRADA";
        }

        if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
            return "SALIDA";
        }

        return "NINGUNA";
    }

    private List<MovimientoContablePreviewDTO> convertirAPreview(
            List<MovimientoContableDTO> movimientos
    ) {
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

            var cuenta = cdatRepository.obtenerCuentaInfo(m.getIdCatalogoCuenta());
            p.setCodigoCuenta(cuenta.getCodigo());
            p.setNombreCuenta(cuenta.getNombre());

            if (m.getIdDatosPersonal() != null) {
                var tercero = cdatRepository.obtenerTerceroInfo(m.getIdDatosPersonal());
                p.setDocumentoTercero(tercero.getDocumento());
                p.setNombreTercero(tercero.getNombre());
            }

            lista.add(p);
        }

        return lista;
    }

    private boolean mayorCero(BigDecimal v) {
        return nvl(v).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}