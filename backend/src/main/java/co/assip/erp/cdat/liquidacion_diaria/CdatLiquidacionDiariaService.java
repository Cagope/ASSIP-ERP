package co.assip.erp.cdat.liquidacion_diaria;

import co.assip.erp.cdat.cdats.CdatRepository;
import co.assip.erp.cdat.liquidacion_diaria.dto.CdatLiquidacionDiariaEntradaDTO;
import co.assip.erp.cdat.liquidacion_diaria.dto.CdatLiquidacionDiariaItemDTO;
import co.assip.erp.cdat.liquidacion_diaria.dto.CdatLiquidacionDiariaPreviewDTO;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;

@Service
@RequiredArgsConstructor
public class CdatLiquidacionDiariaService {

    private final CdatLiquidacionDiariaRepository repository;
    private final CdatRepository cdatRepository;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final DepositosMovimientoService depositosMovimientoService;

    public CdatLiquidacionDiariaPreviewDTO preview(
            CdatLiquidacionDiariaEntradaDTO input
    ) {
        validarBase(input, false);

        if (repository.existeLiquidacion(input.getIdAgencia(), input.getFechaLiquidacion())) {
            throw new RuntimeException("Ya existe liquidación diaria CDAT para la fecha indicada.");
        }

        List<CdatLiquidacionDiariaItemDTO> items = calcularItems(input);

        if (items.isEmpty()) {
            throw new RuntimeException("No existen CDATs pendientes para liquidar en la fecha seleccionada.");
        }

        List<MovimientoContableDTO> movimientos = armarMovimientos(input, items);

        return armarPreview(items, movimientos);
    }

    @Transactional
    public void aplicar(
            CdatLiquidacionDiariaEntradaDTO input,
            Integer idUsuario
    ) {
        validarBase(input, true);

        if (repository.existeLiquidacion(input.getIdAgencia(), input.getFechaLiquidacion())) {
            throw new RuntimeException("Ya existe liquidación diaria CDAT para la fecha indicada.");
        }

        String numeroComprobante =
                consecutivosComprobantesService.generarNumeroDefinitivo(
                        input.getTipoComprobante(),
                        input.getIdAgencia(),
                        idUsuario
                );

        input.setNumeroComprobante(numeroComprobante);

        List<CdatLiquidacionDiariaItemDTO> items = calcularItems(input);

        if (items.isEmpty()) {
            throw new RuntimeException("No existen CDATs pendientes para aplicar en la fecha seleccionada.");
        }

        List<MovimientoContableDTO> movimientos = armarMovimientos(input, items);

        validarCuadre(movimientos);

        for (CdatLiquidacionDiariaItemDTO item : items) {

            repository.insertarExtractoCdat(
                    item.getIdCuentaCdat(),
                    input.getFechaLiquidacion(),
                    input.getTipoComprobante(),
                    input.getNumeroComprobante(),
                    "005",
                    item.getInteresDiario(),
                    BigDecimal.ZERO,
                    idUsuario
            );

            if (mayorCero(item.getRetencion())) {
                repository.insertarExtractoCdat(
                        item.getIdCuentaCdat(),
                        input.getFechaLiquidacion(),
                        input.getTipoComprobante(),
                        input.getNumeroComprobante(),
                        "056",
                        BigDecimal.ZERO,
                        item.getRetencion(),
                        idUsuario
                );
            }

            LocalDate proximoTraslado = null;

            if (Boolean.TRUE.equals(item.getTrasladarADepositos())
                    && item.getIdCuentaAhorro() != null
                    && mayorCero(item.getValorTrasladoDepositos())) {

                repository.insertarExtractoCdat(
                        item.getIdCuentaCdat(),
                        input.getFechaLiquidacion(),
                        input.getTipoComprobante(),
                        input.getNumeroComprobante(),
                        "055",
                        BigDecimal.ZERO,
                        item.getValorTrasladoDepositos(),
                        idUsuario
                );

                DepositosMovimientoDTO movDeposito = new DepositosMovimientoDTO();

                movDeposito.setIdCuentaAhorro(item.getIdCuentaAhorro());
                movDeposito.setFechaMovimiento(input.getFechaLiquidacion());
                movDeposito.setTipoComprobante(input.getTipoComprobante());
                movDeposito.setNumeroComprobante(input.getNumeroComprobante());

                movDeposito.setTipoMovimiento("774");
                movDeposito.setModulo("04");
                movDeposito.setTarjeta("N");

                movDeposito.setEstablecimiento("INTERES CDAT");

                movDeposito.setValorDebito(BigDecimal.ZERO);
                movDeposito.setValorCredito(item.getValorTrasladoDepositos());

                movDeposito.setModuloOrigen("CDAT");
                movDeposito.setProcesoOrigen("LIQUIDACION_DIARIA_CDAT");
                movDeposito.setIdOrigen(item.getIdCuentaCdat());

                depositosMovimientoService.registrarCredito(
                        movDeposito,
                        idUsuario
                );

                String amortizacion =
                        repository.obtenerAmortizacionCdat(item.getIdCuentaCdat());

                Integer mesesAmortizacion =
                        repository.obtenerMesesAmortizacion(amortizacion);

                proximoTraslado =
                        input.getFechaLiquidacion().plusMonths(mesesAmortizacion);
            }

            LocalDate proximaLiquidacion =
                    input.getFechaLiquidacion().plusMonths(1);

            repository.actualizarFechasCdat(
                    item.getIdCuentaCdat(),
                    input.getFechaLiquidacion(),
                    proximaLiquidacion,
                    proximoTraslado,
                    idUsuario
            );
        }

        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CDAT");
        origen.setProcesoOrigen("LIQUIDACION_DIARIA_CDAT");
        origen.setTablaOrigen("cdat.liquidaciones_diarias_control");
        origen.setIdOrigen(null);

        contabilidadRegistroService.registrarComprobante(
                input.getIdAgencia(),
                input.getTipoComprobante(),
                input.getNumeroComprobante(),
                "Liquidación diaria intereses CDAT " + input.getFechaLiquidacion(),
                origen,
                movimientos,
                idUsuario
        );

        repository.insertarControl(
                input.getIdAgencia(),
                input.getFechaLiquidacion(),
                input.getFechaContable(),
                input.getTipoComprobante(),
                input.getNumeroComprobante(),
                items.size(),
                totalInteres(items),
                totalRetencion(items),
                totalNeto(items),
                totalTrasladado(items),
                totalNoTrasladado(items),
                idUsuario
        );
    }

    private List<CdatLiquidacionDiariaItemDTO> calcularItems(
            CdatLiquidacionDiariaEntradaDTO input
    ) {
        BigDecimal valorBaseRetencion =
                repository.obtenerValorBaseRetencion(input.getIdAgencia());

        BigDecimal porcentajeRetencion =
                repository.obtenerPorcentajeRetencion(input.getIdAgencia());

        List<CdatLiquidacionDiariaItemDTO> base =
                repository.listarCdatsLiquidables(
                        input.getIdAgencia(),
                        input.getFechaLiquidacion()
                );

        List<CdatLiquidacionDiariaItemDTO> resultado = new ArrayList<>();

        BigDecimal diasLiquidar = new BigDecimal("30");

        for (CdatLiquidacionDiariaItemDTO cdat : base) {

            BigDecimal interes =
                    cdat.getCapitalCdat()
                            .multiply(diasLiquidar)
                            .multiply(cdat.getTasaNominalAnual())
                            .divide(new BigDecimal("36000"), 8, RoundingMode.HALF_UP)
                            .setScale(0, RoundingMode.HALF_UP);

            BigDecimal interesDiarioPromedio =
                    interes.divide(diasLiquidar, 8, RoundingMode.HALF_UP);

            BigDecimal retencion = BigDecimal.ZERO;

            if (Boolean.TRUE.equals(cdat.getAplicaRetencion())
                    && interesDiarioPromedio.compareTo(valorBaseRetencion) >= 0) {

                retencion =
                        interes.multiply(porcentajeRetencion)
                                .divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                                .setScale(0, RoundingMode.HALF_UP);
            }

            BigDecimal neto = interes.subtract(retencion);

            boolean trasladar =
                    cdat.getFechaProximoTraslado() != null
                            && cdat.getFechaProximoTraslado().isEqual(input.getFechaLiquidacion());

            resultado.add(CdatLiquidacionDiariaItemDTO.builder()
                    .idCuentaCdat(cdat.getIdCuentaCdat())
                    .idAgencia(cdat.getIdAgencia())
                    .codigoCdat(cdat.getCodigoCdat())
                    .idDatosPersonal(cdat.getIdDatosPersonal())
                    .documento(cdat.getDocumento())
                    .nombreCompleto(cdat.getNombreCompleto())
                    .fechaLiquidacion(input.getFechaLiquidacion())
                    .fechaProximoTraslado(cdat.getFechaProximoTraslado())
                    .plazoMeses(cdat.getPlazoMeses())
                    .capitalCdat(cdat.getCapitalCdat())
                    .tasaNominalAnual(cdat.getTasaNominalAnual())
                    .interesDiario(interes)
                    .retencion(retencion)
                    .interesNeto(neto)
                    .aplicaRetencion(cdat.getAplicaRetencion())
                    .trasladarADepositos(trasladar)
                    .valorTrasladoDepositos(trasladar ? neto : BigDecimal.ZERO)
                    .idCuentaAhorro(cdat.getIdCuentaAhorro())
                    .codigoCuentaAhorro(cdat.getCodigoCuentaAhorro())
                    .codigoFormaAhorro(cdat.getCodigoFormaAhorro())
                    .nombreFormaAhorro(cdat.getNombreFormaAhorro())
                    .build());
        }

        return resultado;
    }

    private List<MovimientoContableDTO> armarMovimientos(
            CdatLiquidacionDiariaEntradaDTO input,
            List<CdatLiquidacionDiariaItemDTO> items
    ) {
        List<MovimientoContableDTO> movimientos = new ArrayList<>();

        Integer idAgencia = input.getIdAgencia();
        LocalDate fecha = input.getFechaContable();

        Integer cuentaGastoInteres =
                repository.obtenerCuentaConcepto(idAgencia, "CAUSACION_INTERES_CDAT");

        Integer cuentaRetencion =
                repository.obtenerCuentaConcepto(idAgencia, "RETENCION_CDAT");

        Integer cuentaInteresCausado =
                repository.obtenerCuentaConcepto(idAgencia, "INTERES_CAUSADO");

        for (CdatLiquidacionDiariaItemDTO item : items) {

            BigDecimal interes = nvl(item.getInteresDiario());
            BigDecimal retencion = nvl(item.getRetencion());
            BigDecimal neto = nvl(item.getInteresNeto());

            if (!mayorCero(interes)) {
                continue;
            }

            String codigoCdat = item.getCodigoCdat();
            Integer idTercero = item.getIdDatosPersonal();

            movimientos.add(crearMovimiento(
                    idAgencia,
                    cuentaGastoInteres,
                    idTercero,
                    fecha,
                    "Liquidación diaria CDAT " + codigoCdat + " - gasto intereses",
                    interes,
                    BigDecimal.ZERO
            ));

            if (mayorCero(retencion)) {
                movimientos.add(crearMovimiento(
                        idAgencia,
                        cuentaRetencion,
                        idTercero,
                        fecha,
                        "Liquidación diaria CDAT " + codigoCdat + " - retención",
                        BigDecimal.ZERO,
                        retencion
                ));
            }

            if (Boolean.TRUE.equals(item.getTrasladarADepositos())) {

                if (item.getIdCuentaAhorro() == null) {
                    throw new RuntimeException(
                            "El CDAT " + codigoCdat + " no tiene cuenta de ahorro para traslado."
                    );
                }

                Integer cuentaDeposito =
                        cdatRepository.obtenerCuentaContableFormaAhorroPorCuenta(
                                item.getIdCuentaAhorro()
                        );

                movimientos.add(crearMovimiento(
                        idAgencia,
                        cuentaDeposito,
                        idTercero,
                        fecha,
                        "Liquidación diaria CDAT " + codigoCdat + " - traslado a depósitos",
                        BigDecimal.ZERO,
                        neto
                ));

            } else {

                movimientos.add(crearMovimiento(
                        idAgencia,
                        cuentaInteresCausado,
                        idTercero,
                        fecha,
                        "Liquidación diaria CDAT " + codigoCdat + " - intereses causados no trasladados",
                        BigDecimal.ZERO,
                        neto
                ));
            }
        }

        validarCuadre(movimientos);

        return movimientos;
    }

    private MovimientoContableDTO crearMovimiento(
            Integer idAgencia,
            Integer idCuenta,
            Integer idTercero,
            LocalDate fecha,
            String detalle,
            BigDecimal debito,
            BigDecimal credito
    ) {
        MovimientoContableDTO m = new MovimientoContableDTO();
        m.setIdAgencia(idAgencia);
        m.setIdCatalogoCuenta(idCuenta);
        m.setIdDatosPersonal(idTercero);
        m.setFechaAuxiliar(fecha);
        m.setDetalleMovimiento(detalle);
        m.setValorDebito(nvl(debito));
        m.setValorCredito(nvl(credito));
        m.setDocumentoSoporte("LIQ-DIARIA-CDAT");

        return m;
    }

    private CdatLiquidacionDiariaPreviewDTO armarPreview(
            List<CdatLiquidacionDiariaItemDTO> items,
            List<MovimientoContableDTO> movimientos
    ) {
        List<MovimientoContablePreviewDTO> preview = new ArrayList<>();

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

            preview.add(p);
        }

        BigDecimal totalDebito = totalDebito(movimientos);
        BigDecimal totalCredito = totalCredito(movimientos);
        BigDecimal diferencia = totalDebito.subtract(totalCredito);

        return CdatLiquidacionDiariaPreviewDTO.builder()
                .totalCdats(items.size())
                .totalInteres(totalInteres(items))
                .totalRetencion(totalRetencion(items))
                .totalNeto(totalNeto(items))
                .totalTrasladadoDepositos(totalTrasladado(items))
                .totalNoTrasladado(totalNoTrasladado(items))
                .totalDebito(totalDebito)
                .totalCredito(totalCredito)
                .diferenciaContable(diferencia)
                .cuadrado(diferencia.compareTo(BigDecimal.ZERO) == 0)
                .items(items)
                .movimientosContables(preview)
                .build();
    }

    private void validarBase(
            CdatLiquidacionDiariaEntradaDTO input,
            boolean exigirComprobante
    ) {
        if (input.getIdAgencia() == null) {
            throw new RuntimeException("Agencia obligatoria.");
        }

        if (input.getFechaLiquidacion() == null) {
            throw new RuntimeException("Fecha de liquidación obligatoria.");
        }

        if (input.getFechaContable() == null) {
            throw new RuntimeException("Fecha contable obligatoria.");
        }

        if (exigirComprobante &&
                (input.getTipoComprobante() == null || input.getTipoComprobante().isBlank())) {
            throw new RuntimeException("Tipo de comprobante obligatorio.");
        }
    }

    private void validarCuadre(List<MovimientoContableDTO> movimientos) {
        if (totalDebito(movimientos).compareTo(totalCredito(movimientos)) != 0) {
            throw new RuntimeException("No cuadra el comprobante de liquidación diaria CDAT.");
        }
    }

    private BigDecimal totalInteres(List<CdatLiquidacionDiariaItemDTO> items) {
        return items.stream()
                .map(i -> nvl(i.getInteresDiario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalRetencion(List<CdatLiquidacionDiariaItemDTO> items) {
        return items.stream()
                .map(i -> nvl(i.getRetencion()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalNeto(List<CdatLiquidacionDiariaItemDTO> items) {
        return items.stream()
                .map(i -> nvl(i.getInteresNeto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalTrasladado(List<CdatLiquidacionDiariaItemDTO> items) {
        return items.stream()
                .map(i -> Boolean.TRUE.equals(i.getTrasladarADepositos())
                        ? nvl(i.getValorTrasladoDepositos())
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalNoTrasladado(List<CdatLiquidacionDiariaItemDTO> items) {
        return totalNeto(items).subtract(totalTrasladado(items));
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

    private boolean mayorCero(BigDecimal value) {
        return nvl(value).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}