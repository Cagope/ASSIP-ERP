
package co.assip.erp.cartera.costas_judiciales.administracion;

import co.assip.erp.cartera.costas_judiciales.administracion.dto.CostasJudicialesRequestDTO;
import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.contabilidad.periodos.PeriodosContablesService;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Coordina la previsualización y contabilización de cargos de costas judiciales. */
@Service
@RequiredArgsConstructor
public class AdministracionCostasJudicialesService {
    private final AdministracionCostasJudicialesRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final PeriodosContablesService periodosContablesService;
    private final ConsecutivosComprobantesService consecutivosComprobantesService;
    private final ContabilidadRegistroService contabilidadRegistroService;

    /** Vista previa sin consumir consecutivo ni persistir movimientos financieros. */
    @Transactional
    public VistaPrevia previsualizar(CostasJudicialesRequestDTO solicitud) {
        var contexto = preparar(solicitud);
        String numeroSugerido = consecutivosComprobantesService.obtenerNumeroSugerido(
                contexto.tipoComprobante(), contexto.credito().idAgencia());
        return construirVista(contexto, numeroSugerido);
    }

    /** Confirma comprobante y cargo financiero en una sola transacción con rollback. */
    @Transactional
    public Resultado confirmar(CostasJudicialesRequestDTO solicitud) {
        var contexto = preparar(solicitud);
        Integer idUsuario = usuarioSesionService.idUsuario();
        Integer idAgencia = contexto.credito().idAgencia();
        String numero = consecutivosComprobantesService.generarNumeroDefinitivo(
                contexto.tipoComprobante(), idAgencia, idUsuario);

        Long idCabecera = repository.obtenerCabeceraActiva(contexto.credito().idCredito());
        if (idCabecera == null) {
            idCabecera = repository.crearCabecera(contexto.credito().idCredito(), idUsuario);
        }
        Long idDetalle = repository.registrarCargo(
                idCabecera, solicitud.fechaMovimiento(), solicitud.valor(),
                contexto.tipoComprobante(), numero, solicitud.observacion().trim(), idUsuario);

        var origen = new OrigenComprobanteDTO();
        origen.setOrigenTipo("AUTO");
        origen.setModuloOrigen("CARTERA");
        origen.setProcesoOrigen("COSTAS_JUDICIALES");
        origen.setTablaOrigen("cartera.creditos_costas_judiciales_detalle");
        origen.setIdOrigen(idDetalle);

        contabilidadRegistroService.registrarComprobanteNuevo(
                idAgencia, contexto.tipoComprobante(), numero,
                "Costas judiciales crédito " + contexto.credito().idCredito()
                        + " / " + solicitud.observacion().trim(),
                origen, movimientos(contexto, numero), idUsuario);

        return new Resultado(idDetalle, contexto.tipoComprobante(), numero,
                repository.consultarSaldo(contexto.credito().idCredito()));
    }

    /** Valida datos, agencia, tercero, período y parametrización antes de operar. */
    private Contexto preparar(CostasJudicialesRequestDTO solicitud) {
        if (solicitud == null || solicitud.idCarteraCredito() == null
                || solicitud.fechaMovimiento() == null || solicitud.valor() == null
                || solicitud.valor().signum() <= 0 || solicitud.valor().scale() > 2
                || solicitud.observacion() == null || solicitud.observacion().isBlank()) {
            throw new IllegalArgumentException(
                    "Complete crédito, fecha, valor positivo y descripción.");
        }
        String tipo = normalizarTipo(solicitud.tipoComprobante());
        var credito = repository.bloquearCredito(solicitud.idCarteraCredito());
        usuarioSesionService.validarAgencia(credito.idAgencia());
        periodosContablesService.validarPeriodoAbierto(
                credito.idAgencia(), solicitud.fechaMovimiento());
        if (solicitud.idDatosPersonalTercero() != null
                && !repository.existeTercero(solicitud.idDatosPersonalTercero())) {
            throw new IllegalArgumentException("El tercero seleccionado no existe.");
        }
        var cuentaCargo = repository.resolverCuentaCostas(credito.idCredito());
        var cuentaContrapartida = repository.resolverContrapartida(
                credito.idAgencia(), solicitud.idDatosPersonalTercero());
        if (cuentaCargo == null || cuentaContrapartida == null
                || cuentaCargo.idCatalogoCuenta() == null
                || cuentaContrapartida.idCatalogoCuenta() == null) {
            throw new IllegalStateException(
                    "Falta parametrización contable de costas judiciales.");
        }
        return new Contexto(
                credito, tipo, solicitud,
                cuentaCargo.idCatalogoCuenta(), cuentaCargo.codigoCuenta(),
                cuentaContrapartida.idCatalogoCuenta(),
                cuentaContrapartida.codigoCuenta());
    }

    /** Normaliza el tipo seleccionado; su existencia se valida al solicitar consecutivo. */
    private String normalizarTipo(String tipo) {
        if (tipo == null || !tipo.trim().matches("[A-Za-z0-9]{2}")) {
            throw new IllegalArgumentException(
                    "Seleccione un tipo de comprobante válido.");
        }
        return tipo.trim().toUpperCase(Locale.ROOT);
    }

    /** Construye dos auxiliares balanceados con el titular y el tercero correspondiente. */
    private List<MovimientoContableDTO> movimientos(Contexto contexto, String numero) {
        var solicitud = contexto.solicitud();
        var credito = contexto.credito();

        MovimientoContableDTO debito = nuevoMovimiento(
                contexto, numero, contexto.idCuentaCargo(),
                Math.toIntExact(credito.idTitular()),
                solicitud.valor(), BigDecimal.ZERO);

        Integer tercero = solicitud.idDatosPersonalTercero() == null
                ? null : Math.toIntExact(solicitud.idDatosPersonalTercero());

        MovimientoContableDTO creditoContable = nuevoMovimiento(
                contexto, numero, contexto.idCuentaContrapartida(),
                tercero, BigDecimal.ZERO, solicitud.valor());

        return List.of(debito, creditoContable);
    }

    /** Prepara una línea del comprobante sin fijar códigos de cuentas en Java. */
    private MovimientoContableDTO nuevoMovimiento(
            Contexto contexto, String numero, Integer idCuenta,
            Integer idTercero, BigDecimal debito, BigDecimal credito) {
        var m = new MovimientoContableDTO();
        m.setIdAgencia(contexto.credito().idAgencia());
        m.setIdCatalogoCuenta(idCuenta);
        m.setIdDatosPersonal(idTercero);
        m.setFechaAuxiliar(contexto.solicitud().fechaMovimiento());
        m.setTipoComprobante(contexto.tipoComprobante());
        m.setNumeroComprobante(numero);
        m.setDetalleMovimiento(contexto.solicitud().observacion().trim());
        m.setValorDebito(debito);
        m.setValorCredito(credito);
        m.setOrigenModulo("CARTERA");
        return m;
    }

    /**
     * Construye la vista previa con nombres de cuentas y documentos reales.
     * Las consultas son descriptivas y no alteran la contabilización.
     */
    private VistaPrevia construirVista(Contexto contexto, String numero) {
        var solicitud = contexto.solicitud();
        var credito = contexto.credito();

        var datosCargo = repository.consultarDatosLinea(
                contexto.idCuentaCargo(),
                credito.idAgencia(),
                credito.idTitular());

        var datosContrapartida = repository.consultarDatosLinea(
                contexto.idCuentaContrapartida(),
                credito.idAgencia(),
                solicitud.idDatosPersonalTercero());

        Linea lineaCargo = new Linea(
                contexto.codigoCuentaCargo(),
                datosCargo.nombreCuenta(),
                datosCargo.documentoTercero(),
                contexto.idCuentaCargo(),
                credito.idTitular(),
                solicitud.valor(),
                BigDecimal.ZERO);

        Linea lineaContrapartida = new Linea(
                contexto.codigoCuentaContrapartida(),
                datosContrapartida.nombreCuenta(),
                datosContrapartida.documentoTercero(),
                contexto.idCuentaContrapartida(),
                solicitud.idDatosPersonalTercero(),
                BigDecimal.ZERO,
                solicitud.valor());

        return new VistaPrevia(
                credito.idCredito(),
                credito.idAgencia(),
                contexto.tipoComprobante(),
                numero,
                solicitud.fechaMovimiento(),
                solicitud.valor(),
                solicitud.idDatosPersonalTercero() != null
                        ? "COSTAS_JUDICIALES_CXP"
                        : "COSTAS_JUDICIALES_AGENCIA",
                List.of(lineaCargo, lineaContrapartida));
    }

    private record Contexto(
            AdministracionCostasJudicialesRepository.Credito credito,
            String tipoComprobante,
            CostasJudicialesRequestDTO solicitud,
            Integer idCuentaCargo,
            String codigoCuentaCargo,
            Integer idCuentaContrapartida,
            String codigoCuentaContrapartida) { }

    public record Linea(
            String codigoCuenta,
            String nombreCuenta,
            String documentoTercero,
            Integer idCatalogoCuenta,
            Long idDatosPersonal,
            BigDecimal debito,
            BigDecimal credito) { }

    public record VistaPrevia(
            Integer idCredito,
            Integer idAgencia,
            String tipoComprobante,
            String numeroSugerido,
            LocalDate fecha,
            BigDecimal valor,
            String modalidad,
            List<Linea> movimientos) { }

    public record Resultado(
            Long idDetalle,
            String tipoComprobante,
            String numeroComprobante,
            BigDecimal saldoCostasJudiciales) { }
}
