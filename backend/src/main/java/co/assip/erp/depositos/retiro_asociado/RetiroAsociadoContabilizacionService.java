package co.assip.erp.depositos.retiro_asociado;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetiroAsociadoContabilizacionService {

    private final ContabilidadRegistroService contabilidadRegistroService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;

    public record DatosContabilizacion(
            Integer idCuentaAhorro, Integer idAgencia, Integer idDatosPersonal,
            Integer idCuentaProducto, Integer idCuentaContrapartida, Integer idCuentaCaja,
            BigDecimal valorCaja, BigDecimal valorCxp, BigDecimal valor,
            String tipoComprobante, LocalDate fechaProceso,
            Integer idAgenciaOperacion, Integer idInteragenciaPropietaria,
            Integer idInteragenciaOperacion
    ) {}

    /** Registra una sola vez cada comprobante consolidado por agencia y tipo. */
    @Transactional(propagation = Propagation.MANDATORY)
    public java.util.Map<ClaveComprobante, String> contabilizarConsolidado(
            List<ComprobantePreparado> comprobantes, Integer idDatosPersonal,
            Integer idCuentaOrigen, Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0 || idDatosPersonal == null
                || idCuentaOrigen == null || comprobantes == null || comprobantes.isEmpty()) {
            throw new IllegalArgumentException("Datos incompletos para registrar el retiro integral.");
        }
        java.util.Map<ClaveComprobante, String> numeros = new java.util.LinkedHashMap<>();
        for (ComprobantePreparado c : comprobantes) {
            DatosContabilizacion origen = new DatosContabilizacion(
                    idCuentaOrigen, c.idAgencia(), idDatosPersonal,
                    null, null, null, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, c.tipoComprobante(), null, c.idAgencia(), null, null);
            String numero = registrar(origen, idUsuario, c.idAgencia(),
                    c.tipoComprobante(), c.concepto(), c.movimientos());
            numeros.put(new ClaveComprobante(c.idAgencia(), c.tipoComprobante()), numero);
        }
        return java.util.Collections.unmodifiableMap(numeros);
    }

    public record ClaveComprobante(Integer idAgencia, String tipoComprobante) {}

    /** Agrupa por agencia/tipo, sin mezclar agencias y sin crear consecutivos. */
    public List<ComprobantePreparado> consolidar(List<ComprobantePreparado> preparados) {
        java.util.Map<ClaveComprobante, List<MovimientoContableDTO>> grupos = new java.util.LinkedHashMap<>();
        java.util.Map<ClaveComprobante, String> conceptos = new java.util.LinkedHashMap<>();
        for (ComprobantePreparado preparado : preparados) {
            ClaveComprobante clave = new ClaveComprobante(preparado.idAgencia(), preparado.tipoComprobante());
            grupos.computeIfAbsent(clave, k -> new ArrayList<>()).addAll(preparado.movimientos());
            conceptos.putIfAbsent(clave, preparado.concepto());
        }
        List<ComprobantePreparado> resultado = new ArrayList<>();
        for (var grupo : grupos.entrySet()) {
            // Consolida las contrapartidas repetidas, manteniendo las líneas de producto.
            List<MovimientoContableDTO> movimientos = new ArrayList<>();
            java.util.Map<Integer, MovimientoContableDTO> creditos = new java.util.LinkedHashMap<>();
            for (MovimientoContableDTO m : grupo.getValue()) {
                if (m.getValorDebito().signum() == 0 && m.getValorCredito().signum() > 0) {
                    MovimientoContableDTO anterior = creditos.get(m.getIdCatalogoCuenta());
                    if (anterior == null) {
                        creditos.put(m.getIdCatalogoCuenta(), m);
                    } else {
                        anterior.setValorCredito(anterior.getValorCredito().add(m.getValorCredito()));
                    }
                } else {
                    movimientos.add(m);
                }
            }
            movimientos.addAll(creditos.values());
            resultado.add(new ComprobantePreparado(grupo.getKey().idAgencia(),
                    grupo.getKey().tipoComprobante(), conceptos.get(grupo.getKey()),
                    List.copyOf(movimientos)));
        }
        return List.copyOf(resultado);
    }

    public record ComprobantePreparado(Integer idAgencia, String tipoComprobante,
                                       String concepto, List<MovimientoContableDTO> movimientos) {}

    public record ContextoConcepto(String nombre, String documento, String tipoRetiro,
                                   String acta, LocalDate fechaActa, String productos,
                                   Integer agenciaOperacion, String nombreAgenciaOperacion,
                                   java.util.Map<Integer, String> nombresAgencias) {}

    /** Descripción definitiva por comprobante consolidado; se usa también en la vista previa. */
    public List<ComprobantePreparado> describirComprobantes(
            List<ComprobantePreparado> comprobantes, ContextoConcepto contexto) {
        boolean tieneTraslado = comprobantes.stream()
                .anyMatch(c -> "CA".equals(c.tipoComprobante()));
        java.time.format.DateTimeFormatter fecha =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<ComprobantePreparado> resultado = new ArrayList<>();
        for (ComprobantePreparado c : comprobantes) {
            BigDecimal total = c.movimientos().stream()
                    .map(MovimientoContableDTO::getValorDebito)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String accion = switch (c.tipoComprobante()) {
                case "CA" -> "Cancelación de productos y traslado interagencias para pago por Caja en "
                        + contexto.nombreAgenciaOperacion();
                case "NC" -> "Cancelación de productos y constitución de cuenta por pagar en Tesorería";
                case "CE" -> tieneTraslado && c.idAgencia().equals(contexto.agenciaOperacion())
                        ? "Pago por Caja de recursos trasladados desde otra agencia"
                        : "Pago por Caja de productos del asociado";
                default -> throw new IllegalStateException("Tipo contable no previsto para el retiro: "
                        + c.tipoComprobante());
            };
            String concepto = "Retiro " + contexto.tipoRetiro() + " de " + contexto.nombre()
                    + ", documento " + contexto.documento()
                    + ", acta " + contexto.acta() + " del " + contexto.fechaActa().format(fecha)
                    + ". " + accion + ". Productos: " + contexto.productos()
                    + ". Agencia " + contexto.nombresAgencias().get(c.idAgencia())
                    + ". Valor comprobante $" + total.toPlainString() + ".";
            resultado.add(new ComprobantePreparado(c.idAgencia(), c.tipoComprobante(),
                    concepto, c.movimientos()));
        }
        return List.copyOf(resultado);
    }



    /** Mismo motor para vista previa y registro; no genera consecutivos ni persiste. */
    public List<ComprobantePreparado> preparar(DatosContabilizacion d) {
        validar(d, 1);
        boolean interagencia = d.valorCaja().signum() > 0
                && !d.idAgencia().equals(d.idAgenciaOperacion());
        String soporte = limitar("RETIRO-" + d.idCuentaAhorro(), 20);
        List<ComprobantePreparado> resultado = new ArrayList<>();

        // La obligación por pagar se reconoce siempre mediante NC.
        if (d.valorCxp().signum() > 0) {
            List<MovimientoContableDTO> movimientosCxp = List.of(
                    movimiento(d, d.idAgencia(), d.idCuentaProducto(), d.valorCxp(),
                            BigDecimal.ZERO, "Retiro integral - cancelación producto", soporte),
                    movimiento(d, d.idAgencia(), d.idCuentaContrapartida(), BigDecimal.ZERO,
                            d.valorCxp(), "Retiro integral - CXP", soporte));
            resultado.add(new ComprobantePreparado(d.idAgencia(), "NC",
                    "Retiro integral asociado - cuenta por pagar", movimientosCxp));
        }

        if (d.valorCaja().signum() > 0) {
            // CA registra la obligación entre agencias; CE registra el pago en la agencia de Caja.
            List<MovimientoContableDTO> movimientosOrigen = List.of(
                    movimiento(d, d.idAgencia(), d.idCuentaProducto(), d.valorCaja(),
                            BigDecimal.ZERO, "Retiro integral - cancelación producto", soporte),
                    movimiento(d, d.idAgencia(),
                            interagencia ? d.idInteragenciaPropietaria() : d.idCuentaCaja(),
                            BigDecimal.ZERO, d.valorCaja(),
                            interagencia ? "Retiro integral - puente interagencia" : "Retiro integral - Caja", soporte));
            resultado.add(new ComprobantePreparado(d.idAgencia(), interagencia ? "CA" : "CE",
                    "Retiro integral asociado - pago por Caja", movimientosOrigen));
            if (interagencia) {
                List<MovimientoContableDTO> movimientosOperacion = List.of(
                        movimiento(d, d.idAgenciaOperacion(), d.idInteragenciaOperacion(),
                                d.valorCaja(), BigDecimal.ZERO, "Retiro integral - puente interagencia", soporte),
                        movimiento(d, d.idAgenciaOperacion(), d.idCuentaCaja(),
                                BigDecimal.ZERO, d.valorCaja(), "Retiro integral - efectivo Caja", soporte));
                resultado.add(new ComprobantePreparado(d.idAgenciaOperacion(), "CE",
                        "Retiro integral asociado - pago interagencia", movimientosOperacion));
            }
        }
        return List.copyOf(resultado);
    }

    private String registrar(DatosContabilizacion d, Integer usuario, Integer agencia,
                             String tipo, String concepto, List<MovimientoContableDTO> movimientos) {
        BigDecimal debitos = movimientos.stream().map(MovimientoContableDTO::getValorDebito)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditos = movimientos.stream().map(MovimientoContableDTO::getValorCredito)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debitos.signum() <= 0 || debitos.compareTo(creditos) != 0) {
            throw new IllegalStateException("El comprobante del retiro no está cuadrado.");
        }
        String numero = consecutivosComprobantesService.generarNumeroDefinitivo(tipo, agencia, usuario);
        if (numero == null || numero.isBlank()) {
            throw new IllegalStateException("No se generó consecutivo contable del retiro.");
        }
        OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("DEPOSITOS");
        origen.setProcesoOrigen("RETIRO_ASOCIADO");
        origen.setTablaOrigen("depositos.cuentas_ahorro");
        origen.setIdOrigen(d.idCuentaAhorro().longValue());
        contabilidadRegistroService.registrarComprobante(agencia, tipo, numero,
                concepto, origen, movimientos, usuario);
        return numero;
    }

    private MovimientoContableDTO movimiento(DatosContabilizacion d, Integer agencia,
                                             Integer cuenta, BigDecimal debito, BigDecimal credito,
                                             String detalle, String soporte) {
        MovimientoContableDTO m = new MovimientoContableDTO();
        m.setIdAgencia(agencia);
        m.setIdCatalogoCuenta(cuenta);
        m.setIdDatosPersonal(d.idDatosPersonal());
        m.setFechaAuxiliar(d.fechaProceso());
        m.setDetalleMovimiento(limitar(detalle, 100));
        m.setValorDebito(debito);
        m.setValorCredito(credito);
        m.setValorBase(BigDecimal.ZERO);
        m.setDocumentoSoporte(soporte);
        return m;
    }

    private String limitar(String valor, int longitud) {
        return valor.length() <= longitud ? valor : valor.substring(0, longitud);
    }

    private void validar(DatosContabilizacion d, Integer usuario) {
        if (d == null || usuario == null || usuario <= 0 || d.idCuentaAhorro() == null
                || d.idCuentaAhorro() <= 0 || d.idDatosPersonal() == null || d.idDatosPersonal() <= 0
                || d.idAgencia() == null || d.idAgencia() <= 0 || d.idAgenciaOperacion() == null
                || d.idAgenciaOperacion() <= 0 || d.fechaProceso() == null) {
            throw new IllegalArgumentException("Identificación incompleta del retiro.");
        }
        if (d.valor() == null || d.valor().signum() <= 0 || d.valorCaja() == null
                || d.valorCxp() == null || d.valorCaja().signum() < 0 || d.valorCxp().signum() < 0
                || d.valorCaja().add(d.valorCxp()).compareTo(d.valor()) != 0) {
            throw new IllegalArgumentException("La distribución Caja/CXP no cuadra con el retiro.");
        }
        // El tipo definitivo se determina por cada porción en preparar(): NC, CA o CE.
        if (!"CE".equals(d.tipoComprobante()) && !"NC".equals(d.tipoComprobante())) {
            throw new IllegalArgumentException("Tipo de comprobante incorrecto para el retiro.");
        }
        if (d.idCuentaProducto() == null || d.idCuentaProducto() <= 0
                || (d.valorCxp().signum() > 0 && (d.idCuentaContrapartida() == null
                || d.idCuentaContrapartida() <= 0 || d.idCuentaProducto().equals(d.idCuentaContrapartida())))
                || (d.valorCaja().signum() > 0 && (d.idCuentaCaja() == null || d.idCuentaCaja() <= 0))) {
            throw new IllegalStateException("Parametrización contable incompleta del retiro.");
        }
        if (d.valorCaja().signum() > 0 && !d.idAgencia().equals(d.idAgenciaOperacion())) {
            if (d.idInteragenciaPropietaria() == null || d.idInteragenciaPropietaria() <= 0
                    || d.idInteragenciaOperacion() == null || d.idInteragenciaOperacion() <= 0) {
                throw new IllegalStateException("Faltan las cuentas puente interagencia.");
            }
        }
    }
}
