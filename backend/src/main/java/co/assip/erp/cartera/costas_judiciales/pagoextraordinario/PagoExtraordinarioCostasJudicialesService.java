package co.assip.erp.cartera.costas_judiciales.pagoextraordinario;

import co.assip.erp.cajas.movimientos.CajasMovimientosRepository;
import co.assip.erp.cartera.pago_cuotas_v1.motor.PagoCuotasMotorRepository;
import co.assip.erp.cartera.costas_judiciales.administracion.AdministracionCostasJudicialesRepository;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.contabilidad.periodos.PeriodosContablesService;
import co.assip.erp.contabilidad.periodos.PeriodosContablesRepository;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.tesoreria.operaciones_pendientes.TesoreriaOperacionesPendientesService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aplica pagos y correcciones de costas judiciales sin alterar cargos originales. */
@Service
@RequiredArgsConstructor
public class PagoExtraordinarioCostasJudicialesService {
    private static final String TIPO = "CC";
    private static final String PROCESO = "PAGO_EXTRAORDINARIO_COSTAS_JUDICIALES";
    private static final String TABLA_ORIGEN = "cartera.creditos_costas_judiciales_detalle";


    private static final String CUENTA_BANCO = "RECAUDO_BANCARIO";
    // Se debe parametrizar el movimiento propio de Costas en Depósitos antes de usar AHORROS.
    private static final String MOVIMIENTO_DEPOSITOS_COSTAS = "772";

    private final PagoExtraordinarioCostasJudicialesRepository repository;
    private final AdministracionCostasJudicialesRepository administracion;
    private final PagoCuotasMotorRepository recaudoRepository;
    private final CajasMovimientosRepository cajasRepository;
    private final DepositosMovimientoService depositosMovimientoService;
    private final TesoreriaOperacionesPendientesService tesoreriaService;
    private final ContabilidadRegistroService contabilidadService;
    private final ConsecutivosComprobantesService consecutivosService;
    private final PeriodosContablesService periodosService;
    private final PeriodosContablesRepository periodosRepository;
    private final UsuarioSesionService sesion;
    private final NamedParameterJdbcTemplate jdbc;

    public enum Forma { CAJA, DEPOSITOS, BANCOS, CORRECCION }

    /** Una fila por forma utilizada; DEPOSITOS permite varias cuentas distintas. */
    public record Medio(Forma forma, BigDecimal valor, Long idCaja, Integer idCuentaAhorro) { }

    public record Solicitud(Integer idCredito, LocalDate fechaMovimiento,
                            List<Medio> medios, String justificacion) { }

    public record Linea(Integer idCatalogoCuenta, String codigoCuenta, String nombreCuenta,
                        BigDecimal debito, BigDecimal credito, String detalle,
                        Integer idTercero, String documentoTercero) { }

    public record VistaPrevia(Integer idCredito, Integer idAgencia, String tipoComprobante,
                              String numeroSugerido, BigDecimal saldoAnterior,
                              BigDecimal totalAplicado, BigDecimal saldoPosterior,
                              List<Linea> lineas) { }

    public record Resultado(Long idDetalleCostas, String tipoComprobante,
                            String numeroComprobante, BigDecimal saldoPosterior,
                            List<Long> movimientosCaja, Long operacionTesoreria) { }

    private record Contexto(PagoExtraordinarioCostasJudicialesRepository.DatosCostas costas,
                            BigDecimal saldo, BigDecimal total, List<Linea> lineas) { }

    /** Consulta saldo sin bloquear ni reservar un consecutivo. */
    @Transactional(readOnly = true)
    public BigDecimal consultarSaldo(Integer idCredito) {
        var seguro = repository.consultarCostas(idCredito).orElseThrow(() ->
                new IllegalArgumentException("El crédito no tiene costas judiciales activas."));
        sesion.validarAgencia(seguro.idAgencia());
        return repository.consultarSaldo(seguro.idCabecera());
    }

    /** Presenta el asiento completo; no crea movimientos ni consume consecutivos. */
    @Transactional(readOnly = true)
    public VistaPrevia previsualizar(Solicitud solicitud) {
        Contexto contexto = preparar(solicitud, false);
        String sugerido = consecutivosService.obtenerNumeroSugerido(TIPO, contexto.costas().idAgencia());
        return new VistaPrevia(contexto.costas().idCredito(), contexto.costas().idAgencia(),
                TIPO, sugerido, contexto.saldo(), contexto.total(),
                contexto.saldo().subtract(contexto.total()), contexto.lineas());
    }

    /** Confirma un solo CC y publica únicamente los componentes que requieren bandejas. */
    @Transactional
    public Resultado confirmar(Solicitud solicitud) {
        Contexto contexto = preparar(solicitud, true);
        Integer usuario = sesion.idUsuario();
        Integer agencia = contexto.costas().idAgencia();
        String numero = consecutivosService.generarNumeroDefinitivo(TIPO, agencia, usuario);
        String concepto = concepto(solicitud, contexto.costas().idCredito());
        Long idDetalle = repository.registrarDisminucion(contexto.costas(),
                solicitud.fechaMovimiento(), contexto.total(), numero, concepto, usuario);
        String detalle = "Pago/corrección costas judiciales crédito " + contexto.costas().idCredito()
                + " / " + concepto;
        List<MovimientoContableDTO> auxiliares = new ArrayList<>();
        for (Linea linea : contexto.lineas()) {
            Integer tercero = linea.idTercero();
            auxiliares.add(auxiliar(agencia, solicitud.fechaMovimiento(), numero,
                    linea.idCatalogoCuenta(), tercero, linea.debito(), linea.credito(), detalle));
        }
        var origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");
        origen.setProcesoOrigen(PROCESO);
        origen.setTablaOrigen(TABLA_ORIGEN);
        origen.setIdOrigen(idDetalle);
        contabilidadService.registrarComprobanteNuevo(agencia, TIPO, numero,
                detalle, origen, auxiliares, usuario);

        List<Long> movimientosCaja = new ArrayList<>();
        BigDecimal bancos = BigDecimal.ZERO;
        List<Medio> ahorros = solicitud.medios().stream()
                .filter(m -> m.forma() == Forma.DEPOSITOS)
                .sorted(Comparator.comparing(Medio::idCuentaAhorro)).toList();
        for (Medio medio : ahorros) {
            var dto = new DepositosMovimientoDTO();
            dto.setIdCuentaAhorro(medio.idCuentaAhorro());
            dto.setFechaMovimiento(solicitud.fechaMovimiento());
            dto.setTipoComprobante(TIPO);
            dto.setNumeroComprobante(numero);
            dto.setTipoMovimiento(MOVIMIENTO_DEPOSITOS_COSTAS);
            dto.setModulo("03");
            dto.setValorDebito(medio.valor());
            dto.setValorCredito(BigDecimal.ZERO);
            dto.setTarjeta("N");
            dto.setModuloOrigen("CARTERA");
            dto.setProcesoOrigen(PROCESO);
            dto.setIdOrigen(idDetalle);
            depositosMovimientoService.registrarDebito(dto, usuario);
        }
        for (Medio medio : solicitud.medios()) {
            if (medio.forma() == Forma.CAJA) {
                movimientosCaja.add(registrarCaja(medio, solicitud.fechaMovimiento(),
                        idDetalle, numero, usuario));
            } else if (medio.forma() == Forma.BANCOS) {
                bancos = bancos.add(medio.valor());
            }
        }
        Long operacionTesoreria = null;
        if (bancos.signum() > 0) {
            operacionTesoreria = tesoreriaService.publicar(agencia, "CARTERA", PROCESO,
                    TABLA_ORIGEN, idDetalle, contexto.costas().idAsociado(),
                    "ENTRADA", solicitud.fechaMovimiento(),
                    "Recaudo bancario costas crédito " + contexto.costas().idCredito()
                            + " / CC " + numero,
                    bancos, usuario, null, null, "RECAUDO_COSTAS_JUDICIALES");
        }
        return new Resultado(idDetalle, TIPO, numero,
                repository.consultarSaldo(contexto.costas().idCabecera()),
                List.copyOf(movimientosCaja), operacionTesoreria);
    }

    /** Bloquea crédito y ahorros en confirmación, valida saldo y construye las partidas. */
    private Contexto preparar(Solicitud solicitud, boolean bloquear) {
        if (solicitud == null || solicitud.idCredito() == null || solicitud.idCredito() <= 0
                || solicitud.fechaMovimiento() == null || solicitud.medios() == null
                || solicitud.medios().isEmpty()) {
            throw new IllegalArgumentException("Crédito, fecha y formas de disminución son obligatorios.");
        }
        var seguro = bloquear ? repository.bloquearCostas(solicitud.idCredito())
                : repository.consultarCostas(solicitud.idCredito()).orElseThrow(() ->
                new IllegalArgumentException("El crédito no tiene costas judiciales activas."));
        sesion.validarAgencia(seguro.idAgencia());
        if (bloquear) {
            // La confirmación crea o bloquea el período y registra auditoría si corresponde.
            periodosService.validarPeriodoAbierto(seguro.idAgencia(), solicitud.fechaMovimiento());
        } else {
            // La vista previa consulta el estado sin insertar, bloquear ni abrir períodos.
            String estado = periodosRepository.consultarEstado(seguro.idAgencia(),
                    solicitud.fechaMovimiento().getYear(),
                    solicitud.fechaMovimiento().getMonthValue()).orElse("NO_CREADO");
            if (!"ABIERTO".equals(estado) && !"NO_CREADO".equals(estado)) {
                throw new IllegalStateException("El período contable está cerrado. No se permite contabilizar.");
            }
        }
        BigDecimal saldo = repository.consultarSaldo(seguro.idCabecera());
        if (saldo.signum() <= 0) {
            throw new IllegalArgumentException("No hay saldo pendiente de costas judiciales.");
        }
        BigDecimal total = BigDecimal.ZERO;
        Set<String> utilizados = new HashSet<>();
        boolean correccion = false;
        List<Linea> lineas = new ArrayList<>();
        List<Medio> mediosOrdenados = new ArrayList<>(solicitud.medios());
        mediosOrdenados.sort(Comparator.comparing((Medio m) -> m == null || m.forma() != Forma.DEPOSITOS ? 1 : 0)
                .thenComparing(m -> m == null || m.idCuentaAhorro() == null ? 0 : m.idCuentaAhorro()));
        for (Medio medio : mediosOrdenados) {
            if (medio == null || medio.forma() == null || medio.valor() == null
                    || medio.valor().signum() <= 0 || medio.valor().stripTrailingZeros().scale() > 0) {
                throw new IllegalArgumentException("Cada forma debe tener un valor positivo en pesos enteros.");
            }
            String clave = medio.forma() == Forma.DEPOSITOS
                    ? "DEPOSITOS:" + medio.idCuentaAhorro() : medio.forma().name();
            if (!utilizados.add(clave)) {
                throw new IllegalArgumentException("Forma de disminución repetida: " + clave);
            }
            total = total.add(medio.valor());
            Integer cuenta;
            String descripcion;
            switch (medio.forma()) {
                case CAJA -> {
                    if (medio.idCaja() == null || medio.idCaja() <= 0) {
                        throw new IllegalArgumentException("Seleccione una caja válida.");
                    }
                    if (recaudoRepository.obtenerCajaRecaudo(medio.idCaja(), seguro.idAgencia()).isEmpty()) {
                        throw new IllegalArgumentException("La caja no está habilitada para la agencia.");
                    }
                    cuenta = cajasRepository.obtenerCuentaContableCaja(medio.idCaja());
                    descripcion = "CAJA";
                }
                case DEPOSITOS -> {
                    if (medio.idCuentaAhorro() == null || medio.idCuentaAhorro() <= 0) {
                        throw new IllegalArgumentException("Seleccione una cuenta de ahorros válida.");
                    }
                    if (bloquear) {
                        BigDecimal disponible = recaudoRepository.validarCuentaAhorroRecaudoConLock(
                                seguro.idCredito(), medio.idCuentaAhorro());
                        if (disponible.compareTo(medio.valor()) < 0) {
                            throw new IllegalArgumentException("Saldo insuficiente en cuenta de ahorros.");
                        }
                    } else {
                        boolean encontrada = repository.listarCuentasAhorro(seguro.idCredito()).stream()
                                .anyMatch(c -> c.idCuentaAhorro().equals(medio.idCuentaAhorro())
                                        && c.saldoDisponible().compareTo(medio.valor()) >= 0);
                        if (!encontrada) {
                            throw new IllegalArgumentException("Cuenta de ahorros no disponible o sin fondos.");
                        }
                    }
                    cuenta = recaudoRepository.resolverCuentaContableAhorroRecaudo(
                            seguro.idCredito(), medio.idCuentaAhorro());
                    descripcion = "DEPOSITOS";
                }
                case BANCOS -> {
                    cuenta = recaudoRepository.resolverParametroContable(
                            seguro.idAgencia(), CUENTA_BANCO).idCatalogoCuenta();
                    descripcion = "BANCOS / CXC";
                }
                case CORRECCION -> {
                    correccion = true;
                    // Las partidas de corrección se generan separadamente, respetando
                    // las contrapartidas originales verificables y su antigüedad.
                    // No se registra aquí un débito genérico adicional.
                    agregarLineasCorreccion(lineas, seguro, medio.valor());
                    continue;
                }
                default -> throw new IllegalArgumentException("Forma no soportada.");
            }
            if (cuenta == null || cuenta <= 0) {
                throw new IllegalStateException("Falta la cuenta contable de " + descripcion);
            }
            Integer tercero = seguro.idAsociado();
            lineas.add(linea(cuenta, medio.valor(), BigDecimal.ZERO, descripcion, tercero));
        }
        if (total.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El total supera el saldo pendiente de costas judiciales.");
        }
        if (correccion && (solicitud.justificacion() == null || solicitud.justificacion().isBlank())) {
            throw new IllegalArgumentException("La corrección exige una justificación específica.");
        }
        var cobrar = administracion.resolverCuentaCostas(seguro.idCredito());

        lineas.add(linea(cobrar.idCatalogoCuenta(), BigDecimal.ZERO, total,
                "DISMINUCION COSTAS JUDICIALES", seguro.idAsociado()));
        return new Contexto(seguro, saldo, total, List.copyOf(lineas));
    }

    /**
     * Imputa contablemente la corrección a las causaciones originales más recientes.
     * Los débitos por reversión de pagos no constituyen nuevas causaciones.
     * Si falta trazabilidad contable, usa la cuenta CXP parametrizada aprobada.
     * La disminución financiera continúa siendo una sola por la operación completa.
     */
    private void agregarLineasCorreccion(List<Linea> lineas,
            PagoExtraordinarioCostasJudicialesRepository.DatosCostas costas,
            BigDecimal valorCorreccion) {
        Integer cuentaAlternativa = recaudoRepository.resolverParametroContable(
                costas.idAgencia(), "COSTAS_JUDICIALES_CXP").idCatalogoCuenta();
        if (cuentaAlternativa == null || cuentaAlternativa <= 0) {
            throw new IllegalStateException("Falta parametrizar COSTAS_JUDICIALES_CXP.");
        }
        BigDecimal restante = valorCorreccion;
        for (var cargo : repository.listarCargos(costas.idCredito())) {
            if (restante.signum() == 0) break;
            BigDecimal corregido = cargo.valorCorregido() == null
                    ? BigDecimal.ZERO : cargo.valorCorregido();
            BigDecimal disponible = cargo.valor().subtract(corregido);
            if (disponible.signum() <= 0) continue;
            BigDecimal aplicado = restante.min(disponible);
            boolean verificable = cargo.idCuentaContrapartida() != null
                    && cargo.idCuentaContrapartida() > 0;
            Integer cuenta = verificable ? cargo.idCuentaContrapartida() : cuentaAlternativa;
            Integer tercero = verificable ? cargo.idTercero() : costas.idAsociado();
            lineas.add(linea(cuenta, aplicado, BigDecimal.ZERO,
                    verificable ? "CORRECCION COSTAS / CARGO " + cargo.idDetalleCargo()
                            : "CORRECCION COSTAS / PARAMETRIZADA / CARGO " + cargo.idDetalleCargo(),
                    tercero));
            restante = restante.subtract(aplicado);
        }
        // Una causación histórica puede no estar disponible en listarCargos.
        // No se bloquea la corrección global: el remanente usa la cuenta parametrizada.
        if (restante.signum() > 0) {
            lineas.add(linea(cuentaAlternativa, restante, BigDecimal.ZERO,
                    "CORRECCION COSTAS / CUENTA PARAMETRIZADA", costas.idAsociado()));
        }
    }

    /** Obtiene la descripción contable desde el catálogo de la cuenta existente. */
    private Linea linea(Integer cuenta, BigDecimal debito, BigDecimal credito,
                        String detalle, Integer idTercero) {
        var datos = jdbc.query("""
                SELECT codigo_cuenta, nombre_cuenta
                FROM contabilidad.catalogo_cuentas
                WHERE id_catalogo_cuenta = :cuenta AND cuenta_operable = TRUE
                """, new MapSqlParameterSource("cuenta", cuenta),
                (rs, i) -> new String[] { rs.getString(1), rs.getString(2) });
        if (datos.size() != 1) {
            throw new IllegalStateException("Cuenta contable inexistente o no operable: " + cuenta);
        }
        String documento = idTercero == null ? "" : jdbc.queryForObject("""
                SELECT documento FROM hoja_vida.datos_personales
                WHERE id_datos_personal = :tercero
                """, new MapSqlParameterSource("tercero", idTercero), String.class);
        return new Linea(cuenta, datos.get(0)[0], datos.get(0)[1],
                debito, credito, detalle, idTercero, documento);
    }

    /** Construye el auxiliar con el tercero propio de cada partida. */
    private MovimientoContableDTO auxiliar(Integer agencia, LocalDate fecha, String numero,
                                           Integer cuenta, Integer tercero, BigDecimal debito,
                                           BigDecimal credito, String detalle) {
        var m = new MovimientoContableDTO();
        m.setIdAgencia(agencia);
        m.setIdCatalogoCuenta(cuenta);
        m.setIdDatosPersonal(tercero);
        m.setFechaAuxiliar(fecha);
        m.setTipoComprobante(TIPO);
        m.setNumeroComprobante(numero);
        m.setDetalleMovimiento(detalle);
        m.setValorDebito(debito);
        m.setValorCredito(credito);
        m.setOrigenModulo("CARTERA");
        return m;
    }

    /** Publica el efectivo como ingreso pendiente con origen propio de Costas. */
    private Long registrarCaja(Medio medio, LocalDate fecha, Long idDetalle,
                               String numero, Integer usuario) {
        Long provision = cajasRepository.obtenerProvisionAbierta(medio.idCaja(), fecha);
        Long departamento = cajasRepository.obtenerIdDepartamento("12");
        Long operacion = cajasRepository.obtenerIdOperacion("PAGO_COSTAS_JUDICIALES");
        if (provision == null || departamento == null || operacion == null) {
            throw new IllegalStateException("Falta provisión abierta, departamento 12 u operación PAGO_COSTAS_JUDICIALES en Caja.");
        }
        return cajasRepository.insertarMovimientoCaja(provision, medio.idCaja(), fecha,
                departamento, operacion, "CARTERA", PROCESO, TABLA_ORIGEN, idDetalle,
                null, "INGRESO", "EFECTIVO", medio.valor(), "Recaudo costas CC " + numero,
                "PENDIENTE", TIPO, numero, usuario);
    }

    /** Conserva un concepto legible dentro del límite de observación del seguro. */
    private String concepto(Solicitud solicitud, Integer credito) {
        String justificacion = solicitud.justificacion() == null ? "" : solicitud.justificacion().trim();
        String texto = "PAGO Y/O CORRECCION COSTAS CREDITO " + credito
                + (justificacion.isEmpty() ? "" : " / " + justificacion);
        if (texto.length() > 350) {
            throw new IllegalArgumentException("El concepto excede los 400 caracteres permitidos.");
        }
        return texto;
    }
}
