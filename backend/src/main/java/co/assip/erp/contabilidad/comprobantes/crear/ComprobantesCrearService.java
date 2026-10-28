
package co.assip.erp.contabilidad.comprobantes.crear;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.comprobantes.crear.dto.ComprobanteCrearRequestDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.contabilidad.periodos.PeriodosContablesService;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuenta;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuentaRepository;
import co.assip.erp.seguridad.auditoria.LogEventoService;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Crea comprobantes contables manuales.
 *
 * Autoriza la operación, valida agencia, período,
 * cuentas y valores, genera el consecutivo, registra
 * los movimientos, sus controles y la auditoría.
 */
@Service
@RequiredArgsConstructor
public class ComprobantesCrearService {

    private static final String PERMISO_CREAR =
            "CONTABILIDAD_NEW";

    private static final Integer ID_ROL_CONTADOR = 15;

    private final AccessValidator accessValidator;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolService usuarioRolService;

    private final PeriodosContablesService periodosContablesService;
    private final PlanCuentaRepository planCuentaRepository;
    private final ConsecutivosComprobantesService consecutivosService;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final LogEventoService logEventoService;

    /**
     * Crea y contabiliza un comprobante manual.
     *
     * El consecutivo se asigna después de validar
     * la solicitud, las líneas y el período.
     * Toda la operación participa en una transacción.
     *
     * @return número definitivo del comprobante.
     */
    @Transactional
    public String crear(
            ComprobanteCrearRequestDTO solicitud,
            HttpServletRequest request
    ) {
        validarSolicitud(solicitud);

        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException(
                        "El usuario autenticado no está activo."
                ));

        accessValidator.validarAcceso(
                usuario,
                PERMISO_CREAR
        );

        validarAlcanceAgencia(
                idUsuario,
                solicitud.idAgencia()
        );

        List<ContabilidadRegistroService.MovimientoManual> movimientos =
                prepararMovimientos(solicitud);

        /*
         * Bloquea el período dentro de esta transacción.
         * El bloqueo permanece hasta confirmar o revertir
         * la contabilización completa.
         */
        periodosContablesService.validarPeriodoAbierto(
                solicitud.idAgencia(),
                solicitud.fechaContable()
        );

        String tipoComprobante =
                solicitud.tipoComprobante().trim().toUpperCase();

        String numeroComprobante =
                consecutivosService.generarNumeroDefinitivo(
                        tipoComprobante,
                        solicitud.idAgencia(),
                        idUsuario
                );

        OrigenComprobanteDTO origen = construirOrigen();

        contabilidadRegistroService.registrarComprobanteManualNuevo(
                solicitud.idAgencia(),
                tipoComprobante,
                numeroComprobante,
                solicitud.conceptoComprobante().trim(),
                origen,
                movimientos,
                idUsuario
        );

        /*
         * Registra la auditoría después de contabilizar.
         * Si falla la transacción, se revierte la operación.
         */
        logEventoService.registrarEvento(
                idUsuario,
                "CONTABILIDAD",
                "COMPROBANTE_CREAR",
                "Agencia=" + solicitud.idAgencia()
                        + "; tipo=" + tipoComprobante
                        + "; numero=" + numeroComprobante
                        + "; origen=MANUAL",
                request
        );

        return numeroComprobante;
    }

    /**
     * Valida los campos generales y exige al menos
     * dos movimientos para el comprobante manual.
     */
    private void validarSolicitud(
            ComprobanteCrearRequestDTO solicitud
    ) {
        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La solicitud del comprobante es obligatoria."
            );
        }

        if (solicitud.idAgencia() == null
                || solicitud.idAgencia() <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una agencia válida."
            );
        }

        if (solicitud.tipoComprobante() == null
                || solicitud.tipoComprobante().isBlank()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar el tipo de comprobante."
            );
        }

        if (solicitud.tipoComprobante().trim().length() != 2) {
            throw new IllegalArgumentException(
                    "El tipo de comprobante debe tener dos caracteres."
            );
        }

        if (solicitud.fechaContable() == null) {
            throw new IllegalArgumentException(
                    "La fecha contable es obligatoria."
            );
        }

        if (solicitud.conceptoComprobante() == null
                || solicitud.conceptoComprobante().isBlank()) {
            throw new IllegalArgumentException(
                    "El concepto general es obligatorio."
            );
        }

        if (solicitud.movimientos() == null
                || solicitud.movimientos().size() < 2) {
            throw new IllegalArgumentException(
                    "El comprobante requiere al menos dos movimientos."
            );
        }
    }

    /**
     * Comprueba el alcance de agencia.
     *
     * ADMIN y CONTADOR tienen acceso contable global.
     * Los demás usuarios utilizan las agencias de sesión.
     */
    private void validarAlcanceAgencia(
            Integer idUsuario,
            Integer idAgencia
    ) {
        if (usuarioSesionService.tieneAccesoTotal()) {
            return;
        }

        boolean esContador = usuarioRolService
                .listarVigentes(idUsuario)
                .stream()
                .anyMatch(asignacion ->
                        asignacion.getRol() != null
                                && Boolean.TRUE.equals(
                                asignacion.getRol().getActivo()
                        )
                                && ID_ROL_CONTADOR.equals(
                                asignacion.getRol().getIdRol()
                        )
                );

        if (!esContador) {
            usuarioSesionService.validarAgencia(idAgencia);
        }
    }

    /**
     * Prepara los movimientos manuales con su control.
     *
     * Cada movimiento mantiene asociado idTiposControl
     * y documentoSoporte, evitando listas independientes.
     * Valida también el equilibrio contable.
     */
    private List<ContabilidadRegistroService.MovimientoManual>
    prepararMovimientos(
            ComprobanteCrearRequestDTO solicitud
    ) {
        List<ContabilidadRegistroService.MovimientoManual> movimientos =
                new ArrayList<>();

        BigDecimal totalDebito = BigDecimal.ZERO;
        BigDecimal totalCredito = BigDecimal.ZERO;

        int numeroLinea = 0;

        for (ComprobanteCrearRequestDTO.Detalle detalle
                : solicitud.movimientos()) {

            numeroLinea++;

            if (detalle == null) {
                throw new IllegalArgumentException(
                        "La línea " + numeroLinea + " está vacía."
                );
            }

            validarDetalle(
                    detalle,
                    solicitud.idAgencia(),
                    numeroLinea
            );

            BigDecimal debito =
                    valorOZero(detalle.valorDebito());

            BigDecimal credito =
                    valorOZero(detalle.valorCredito());

            totalDebito = totalDebito.add(debito);
            totalCredito = totalCredito.add(credito);

            MovimientoContableDTO movimiento =
                    new MovimientoContableDTO();

            movimiento.setIdCatalogoCuenta(
                    detalle.idCatalogoCuenta()
            );

            movimiento.setIdDatosPersonal(
                    detalle.idDatosPersonal()
            );

            movimiento.setIdAgencia(
                    solicitud.idAgencia()
            );

            movimiento.setFechaAuxiliar(
                    solicitud.fechaContable()
            );

            movimiento.setDetalleMovimiento(
                    detalle.detalleMovimiento().trim()
            );

            movimiento.setValorDebito(debito);
            movimiento.setValorCredito(credito);

            movimiento.setValorBase(
                    valorOZero(detalle.valorBase())
            );

            String documento = detalle.documentoSoporte();

            movimiento.setDocumentoSoporte(
                    documento == null || documento.isBlank()
                            ? null
                            : documento.trim()
            );

            movimientos.add(
                    new ContabilidadRegistroService.MovimientoManual(
                            movimiento,
                            detalle.idTiposControl()
                    )
            );
        }

        if (totalDebito.signum() <= 0
                || totalCredito.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El comprobante debe tener valores "
                            + "positivos en débito y crédito."
            );
        }

        if (totalDebito.compareTo(totalCredito) != 0) {
            throw new IllegalArgumentException(
                    "El comprobante está descuadrado. "
                            + "Débitos: " + totalDebito
                            + ", créditos: " + totalCredito
            );
        }

        return movimientos;
    }

    /**
     * Valida cuenta, tercero, importes y control.
     *
     * Las cuentas con control requieren idTiposControl.
     * La obligatoriedad del documento depende del tipo
     * y se comprueba en ControlesAuxiliaresRepository.
     */
    private void validarDetalle(
            ComprobanteCrearRequestDTO.Detalle detalle,
            Integer idAgencia,
            int numeroLinea
    ) {
        if (detalle.idCatalogoCuenta() == null) {
            throw new IllegalArgumentException(
                    "La línea " + numeroLinea
                            + " no tiene cuenta contable."
            );
        }

        PlanCuenta cuenta = planCuentaRepository
                .findById(detalle.idCatalogoCuenta())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La cuenta de la línea "
                                + numeroLinea + " no existe."
                ));

        if (!idAgencia.equals(cuenta.getIdAgencia())) {
            throw new IllegalArgumentException(
                    "La cuenta de la línea " + numeroLinea
                            + " no pertenece a la agencia."
            );
        }

        if (!Boolean.TRUE.equals(cuenta.getOperable())) {
            throw new IllegalArgumentException(
                    "La cuenta de la línea " + numeroLinea
                            + " no es operable."
            );
        }

        if (detalle.idDatosPersonal() == null
                || detalle.idDatosPersonal() <= 0) {
            throw new IllegalArgumentException(
                    "La línea " + numeroLinea
                            + " requiere un tercero contable."
            );
        }

        if (detalle.detalleMovimiento() == null
                || detalle.detalleMovimiento().isBlank()) {
            throw new IllegalArgumentException(
                    "La línea " + numeroLinea
                            + " requiere un detalle."
            );
        }

        BigDecimal debito =
                valorOZero(detalle.valorDebito());

        BigDecimal credito =
                valorOZero(detalle.valorCredito());

        BigDecimal base =
                valorOZero(detalle.valorBase());

        if (debito.signum() < 0
                || credito.signum() < 0
                || base.signum() < 0) {
            throw new IllegalArgumentException(
                    "La línea " + numeroLinea
                            + " contiene valores negativos."
            );
        }

        if ((debito.signum() > 0 && credito.signum() > 0)
                || (debito.signum() == 0
                && credito.signum() == 0)) {
            throw new IllegalArgumentException(
                    "La línea " + numeroLinea
                            + " debe tener un débito o un crédito, "
                            + "pero no ambos."
            );
        }

        boolean requiereControl = Boolean.TRUE.equals(
                cuenta.getControlEntradaSalida()
        );

        Integer idTiposControl = detalle.idTiposControl();

        String documento = detalle.documentoSoporte();

        if (requiereControl) {

            if (idTiposControl == null
                    || idTiposControl <= 0) {
                throw new IllegalArgumentException(
                        "La cuenta de la línea " + numeroLinea
                                + " requiere seleccionar "
                                + "un tipo de control."
                );
            }

            if (documento != null
                    && documento.trim().length() > 20) {
                throw new IllegalArgumentException(
                        "El documento soporte de la línea "
                                + numeroLinea
                                + " supera 20 caracteres."
                );
            }

        } else {

            if (idTiposControl != null) {
                throw new IllegalArgumentException(
                        "La cuenta de la línea " + numeroLinea
                                + " no requiere tipo de control."
                );
            }

            if (documento != null
                    && !documento.isBlank()) {
                throw new IllegalArgumentException(
                        "La cuenta de la línea " + numeroLinea
                                + " no requiere documento soporte."
                );
            }
        }
    }

    /**
     * Normaliza los importes opcionales sin alterar
     * la precisión decimal proporcionada.
     */
    private BigDecimal valorOZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    /**
     * Identifica el comprobante como creación manual
     * dentro de la trazabilidad contable existente.
     */
    private OrigenComprobanteDTO construirOrigen() {
        OrigenComprobanteDTO origen =
                new OrigenComprobanteDTO();

        origen.setOrigenTipo("MANUAL");
        origen.setModuloOrigen("CONTABILIDAD");
        origen.setProcesoOrigen("COMPROBANTE_MANUAL");

        return origen;
    }
}
