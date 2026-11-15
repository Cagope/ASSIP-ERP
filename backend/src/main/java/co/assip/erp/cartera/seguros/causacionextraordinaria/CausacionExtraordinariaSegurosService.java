package co.assip.erp.cartera.seguros.causacionextraordinaria;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.contabilidad.periodos.PeriodosContablesService;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Previsualiza y registra causaciones extraordinarias de seguros con nota contable NC. */
@Service
@RequiredArgsConstructor
public class CausacionExtraordinariaSegurosService {
    private static final String TIPO_COMPROBANTE = "NC";
    private static final String CONCEPTO_COBRAR = "SEGUROS";
    private static final String CONCEPTO_PAGAR = "SEGUROS_CXP";
    private final CausacionExtraordinariaSegurosRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final PeriodosContablesService periodosContablesService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final ContabilidadRegistroService contabilidadRegistroService;

    /** Sugiere saldo base y valor usando exclusivamente el porcentaje aplicado vigente. */
    @Transactional(readOnly = true)
    public Sugerencia consultarSugerencia(Integer idCredito) {
        var seguro = repository.consultarSeguro(idCredito).orElseThrow(() ->
                new IllegalArgumentException("El crédito no tiene un seguro activo con aseguradora configurada."));
        usuarioSesionService.validarAgencia(seguro.idAgencia());
        validarCredito(seguro);
        BigDecimal base = seguro.saldoActual();
        if (base == null || base.signum() < 0) {
            throw new IllegalStateException("El crédito no tiene un saldo actual válido.");
        }
        return new Sugerencia(seguro.idCredito(), seguro.idCreditoSeguro(), seguro.idAgencia(),
                LocalDate.now(), base, seguro.porcentajeBaseSeguro(),
                seguro.porcentajeExtraprima(), seguro.porcentajeAplicar(),
                calcularValor(base, seguro.porcentajeAplicar()),
                repository.consultarSaldoSeguro(seguro.idCreditoSeguro()));
    }

    /** Construye la vista previa sin consumir el consecutivo ni crear movimientos financieros. */
    @Transactional
    public VistaPrevia previsualizar(Solicitud solicitud) {
        var contexto = preparar(solicitud, false);
        String sugerido = consecutivosComprobantesService.obtenerNumeroSugerido(
                TIPO_COMPROBANTE, contexto.seguro().idAgencia());
        return new VistaPrevia(contexto.seguro().idCredito(), contexto.seguro().idAgencia(),
                TIPO_COMPROBANTE, sugerido, solicitud.fechaMovimiento(),
                solicitud.saldoBase(), contexto.seguro().porcentajeBaseSeguro(),
                contexto.seguro().porcentajeExtraprima(), contexto.seguro().porcentajeAplicar(),
                calcularValor(solicitud.saldoBase(), contexto.seguro().porcentajeAplicar()),
                solicitud.valor(), solicitud.justificacion().trim(),
                lineas(contexto));
    }

    /** Persiste el débito del seguro y su NC en una sola transacción; cualquier fallo revierte ambos. */
    @Transactional
    public Resultado confirmar(Solicitud solicitud) {
        var contexto = preparar(solicitud, true);
        Integer usuario = usuarioSesionService.idUsuario();
        Integer agencia = contexto.seguro().idAgencia();
        String numero = consecutivosComprobantesService.generarNumeroDefinitivo(
                TIPO_COMPROBANTE, agencia, usuario);
        Integer idDetalle = repository.registrarCausacion(
                contexto.seguro(), solicitud.fechaMovimiento(), solicitud.saldoBase(),
                solicitud.valor(), numero, solicitud.justificacion(), usuario);
        var origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");
        origen.setProcesoOrigen("CAUSACION_EXTRAORDINARIA_SEGUROS");
        origen.setTablaOrigen("cartera.creditos_seguros_detalle");
        origen.setIdOrigen(idDetalle.longValue());
        contabilidadRegistroService.registrarComprobanteNuevo(
                agencia, TIPO_COMPROBANTE, numero,
                "Causación extraordinaria de seguros crédito " + contexto.seguro().idCredito()
                        + " / " + solicitud.justificacion().trim(),
                origen, movimientos(contexto, numero), usuario);
        return new Resultado(idDetalle, TIPO_COMPROBANTE, numero,
                repository.consultarSaldoSeguro(contexto.seguro().idCreditoSeguro()));
    }

    /** Valida datos, agencia, período, terceros y cuentas antes de contabilizar. */
    private Contexto preparar(Solicitud solicitud, boolean bloquear) {
        if (solicitud == null || solicitud.idCredito() == null || solicitud.idCredito() <= 0
                || solicitud.fechaMovimiento() == null || solicitud.saldoBase() == null
                || solicitud.saldoBase().signum() < 0 || solicitud.saldoBase().scale() > 2
                || solicitud.valor() == null || solicitud.valor().signum() <= 0
                || solicitud.valor().scale() > 2 || solicitud.justificacion() == null
                || solicitud.justificacion().isBlank()) {
            throw new IllegalArgumentException("Complete crédito, fecha, saldo base, valor positivo y justificación.");
        }
        var seguro = bloquear ? repository.bloquearSeguro(solicitud.idCredito())
                : repository.consultarSeguro(solicitud.idCredito()).orElseThrow(() ->
                new IllegalArgumentException("El crédito no tiene seguro activo con aseguradora configurada."));
        usuarioSesionService.validarAgencia(seguro.idAgencia());
        validarCredito(seguro);
        periodosContablesService.validarPeriodoAbierto(seguro.idAgencia(), solicitud.fechaMovimiento());
        var cobrar = repository.resolverCuenta(seguro.idAgencia(), CONCEPTO_COBRAR);
        var pagar = repository.resolverCuenta(seguro.idAgencia(), CONCEPTO_PAGAR);
        if (!"D".equalsIgnoreCase(cobrar.naturalezaCuenta())
                || !"C".equalsIgnoreCase(pagar.naturalezaCuenta())) {
            throw new IllegalStateException("La naturaleza de las cuentas parametrizadas de seguros es incorrecta.");
        }
        String documentoAsociado = repository.consultarDocumentoTercero(seguro.idAsociado());
        String documentoAseguradora = repository.consultarDocumentoTercero(seguro.idAseguradora());
        return new Contexto(seguro, solicitud, cobrar, pagar,
                documentoAsociado, documentoAseguradora);
    }

    /** Impide registrar cargos sobre créditos no vigentes o sin tasa aplicada. */
    private void validarCredito(CausacionExtraordinariaSegurosRepository.DatosSeguro seguro) {
        if (!"A".equalsIgnoreCase(seguro.estadoCredito())) {
            throw new IllegalArgumentException("El crédito no se encuentra activo.");
        }
        if (seguro.porcentajeAplicar() == null || seguro.porcentajeAplicar().signum() <= 0) {
            throw new IllegalArgumentException("El seguro no tiene porcentaje aplicado positivo.");
        }
    }

    /** Redondea al peso, igual que la causación mensual, sin recalcular porcentajes. */
    private BigDecimal calcularValor(BigDecimal saldoBase, BigDecimal porcentajeAplicar) {
        return saldoBase.multiply(porcentajeAplicar)
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
    }

    /** Prepara las dos partidas contables balanceadas con sus terceros correspondientes. */
    private List<MovimientoContableDTO> movimientos(Contexto contexto, String numero) {
        return List.of(
                movimiento(contexto, numero, contexto.cobrar().idCatalogoCuenta(),
                        contexto.seguro().idAsociado(), contexto.solicitud().valor(), BigDecimal.ZERO),
                movimiento(contexto, numero, contexto.pagar().idCatalogoCuenta(),
                        contexto.seguro().idAseguradora(), BigDecimal.ZERO, contexto.solicitud().valor()));
    }

    /** Construye un auxiliar de NC sin exponer identificadores internos al funcionario. */
    private MovimientoContableDTO movimiento(Contexto contexto, String numero, Integer cuenta,
                                             Integer tercero, BigDecimal debito, BigDecimal credito) {
        var m = new MovimientoContableDTO();
        m.setIdAgencia(contexto.seguro().idAgencia());
        m.setIdCatalogoCuenta(cuenta);
        m.setIdDatosPersonal(tercero);
        m.setFechaAuxiliar(contexto.solicitud().fechaMovimiento());
        m.setTipoComprobante(TIPO_COMPROBANTE);
        m.setNumeroComprobante(numero);
        m.setDetalleMovimiento("Causación extraordinaria seguro crédito "
                + contexto.seguro().idCredito() + " / " + contexto.solicitud().justificacion().trim());
        m.setValorDebito(debito);
        m.setValorCredito(credito);
        m.setOrigenModulo("CARTERA");
        return m;
    }

    /** Devuelve las partidas que verá el funcionario antes de confirmar. */
    private List<Linea> lineas(Contexto contexto) {
        return List.of(
                new Linea(contexto.cobrar().codigoCuenta(), contexto.cobrar().nombreCuenta(),
                        contexto.documentoAsociado(), contexto.solicitud().valor(), BigDecimal.ZERO),
                new Linea(contexto.pagar().codigoCuenta(), contexto.pagar().nombreCuenta(),
                        contexto.documentoAseguradora(), BigDecimal.ZERO, contexto.solicitud().valor()));
    }

    private record Contexto(
            CausacionExtraordinariaSegurosRepository.DatosSeguro seguro,
            Solicitud solicitud,
            CausacionExtraordinariaSegurosRepository.CuentaContable cobrar,
            CausacionExtraordinariaSegurosRepository.CuentaContable pagar,
            String documentoAsociado, String documentoAseguradora) { }

    public record Solicitud(Integer idCredito, LocalDate fechaMovimiento,
                            BigDecimal saldoBase, BigDecimal valor, String justificacion) { }

    public record Sugerencia(Integer idCredito, Integer idCreditoSeguro, Integer idAgencia,
                             LocalDate fechaSugerida, BigDecimal saldoBase,
                             BigDecimal porcentajeBaseSeguro, BigDecimal porcentajeExtraprima,
                             BigDecimal porcentajeAplicar, BigDecimal valorSugerido,
                             BigDecimal saldoSeguro) { }

    public record Linea(String codigoCuenta, String nombreCuenta, String documentoTercero,
                        BigDecimal debito, BigDecimal credito) { }

    public record VistaPrevia(Integer idCredito, Integer idAgencia, String tipoComprobante,
                              String numeroSugerido, LocalDate fechaMovimiento, BigDecimal saldoBase,
                              BigDecimal porcentajeBaseSeguro, BigDecimal porcentajeExtraprima,
                              BigDecimal porcentajeAplicar, BigDecimal valorSugerido,
                              BigDecimal valor, String justificacion, List<Linea> movimientos) { }

    public record Resultado(Integer idDetalle, String tipoComprobante,
                            String numeroComprobante, BigDecimal saldoSeguro) { }
}
