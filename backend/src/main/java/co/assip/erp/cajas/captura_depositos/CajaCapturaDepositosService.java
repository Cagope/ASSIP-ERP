package co.assip.erp.cajas.captura_depositos;

import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosChequeDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosCuentaDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosPreviewDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosRequestDTO;
import co.assip.erp.cajas.captura_depositos.dto.CajaCapturaDepositosResponseDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import co.assip.erp.sarlaft.evaluacion.SarlaftAlertaService;
import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;
import co.assip.erp.sarlaft.evaluacion.dto.ReglasInput;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.sarlaft.lavado_activos.LavadoActivosService;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosRequestDTO;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosResponseDTO;

@Service
@RequiredArgsConstructor
public class CajaCapturaDepositosService {

    private static final String MODULO_ORIGEN = "DEPOSITOS";
    private static final String PROCESO_ORIGEN = "CAPTURA_DEPOSITOS";
    private static final String TABLA_ORIGEN = "depositos.cuentas_ahorro";

    private static final String OPERACION_CONSIGNACION = "CONSIGNACION_AHORRO";
    private static final String OPERACION_RETIRO = "RETIRO_AHORRO";

    private static final String NATURALEZA_INGRESO = "INGRESO";
    private static final String NATURALEZA_EGRESO = "EGRESO";

    private static final String MEDIO_EFECTIVO = "EFECTIVO";
    private static final String MEDIO_CHEQUE = "CHEQUE";
    private static final String MEDIO_TRANSFERENCIA = "TRANSFERENCIA";
    private static final String MODULO_CAJAS = "05";
    private static final String MODULO_DEPOSITOS = "02";
    private static final String TARJETA_NO = "N";


    private final CajaCapturaDepositosRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final SarlaftAlertaService sarlaftAlertaService;
    private final DepositosMovimientoService depositosMovimientoService;
    private final LavadoActivosService lavadoActivosService;

    public CajaCapturaDepositosPreviewDTO preview(
            CajaCapturaDepositosRequestDTO request
    ) {
        validarRequestBase(request);

        List<String> errores = new ArrayList<>();

        Long idProvision = validarProvisionAbierta(
                request.getIdCaja(),
                request.getFechaContable()
        );
        validarDepartamentoDepositos();
        validarOperacion(request.getCodigoOperacion());

        CajaCapturaDepositosCuentaDTO cuenta = validarCuenta(request);

        boolean movimientoInteragencia =
                !cuenta.getIdAgencia().equals(
                        request.getIdAgencia()
                );

        String mensajeInteragencia = null;

        if (movimientoInteragencia) {

            mensajeInteragencia =
                    "ATENCIÓN: La cuenta pertenece a la agencia "
                            + cuenta.getCodigoAgencia()
                            + " - "
                            + cuenta.getNombreAgencia()
                            + ". "
                            + "La operación será registrada normalmente y "
                            + "será tratada como movimiento interagencia "
                            + "durante el proceso de cierre diario.";
        }

        String naturaleza = repository.obtenerNaturalezaOperacion(
                request.getCodigoOperacion().trim()
        );

        if (naturaleza == null || naturaleza.trim().isEmpty()) {
            throw new RuntimeException(
                    "La operación de caja no tiene naturaleza configurada."
            );
        }

        BigDecimal valorEfectivo = nvl(request.getValorEfectivo());
        BigDecimal valorCheques = nvl(request.getValorCheques());
        BigDecimal valorTransferencias = nvl(request.getValorTransferencias());

        BigDecimal valorTotal = valorEfectivo
                .add(valorCheques)
                .add(valorTransferencias);

        if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("El valor total del movimiento debe ser mayor a cero.");
        }

        validarDocumentoSoportePreview(request, errores);
        validarChequesPreview(request, valorCheques, errores);

        EvaluacionResultado sarlaft =
                evaluarSarlaftPreview(
                        cuenta,
                        request,
                        usuarioSesionService.idUsuario(),
                        valorTotal
                );

        BigDecimal saldoAnterior = nvl(cuenta.getSaldoActual());
        BigDecimal valorCanje = nvl(cuenta.getValorCanje());
        BigDecimal saldoDisponible = saldoAnterior.subtract(valorCanje);

        BigDecimal saldoFinal = saldoAnterior;

        if (NATURALEZA_INGRESO.equalsIgnoreCase(naturaleza)) {
            saldoFinal = saldoAnterior.add(valorTotal);

        } else if (NATURALEZA_EGRESO.equalsIgnoreCase(naturaleza)) {

            if (saldoDisponible.compareTo(valorTotal) < 0) {
                errores.add("La cuenta no tiene saldo disponible suficiente para el retiro.");
            }

            saldoFinal = saldoAnterior.subtract(valorTotal);

        } else {
            errores.add("La naturaleza de la operación no es válida.");
        }

        boolean bloqueaSarlaft =
                sarlaft != null
                        && Boolean.TRUE.equals(sarlaft.getBloqueaOperacion());

        return CajaCapturaDepositosPreviewDTO.builder()
                .idCaja(request.getIdCaja())
                .idAgencia(request.getIdAgencia())
                .fechaContable(request.getFechaContable())
                .idProvision(idProvision)
                .idCuentaAhorro(cuenta.getIdCuentaAhorro())
                .codigoCuenta(cuenta.getCodigoCuenta())
                .documento(cuenta.getDocumento())
                .nombreAsociado(cuenta.getNombreAsociado())
                .codigoForma(cuenta.getCodigoForma())
                .nombreForma(cuenta.getNombreForma())

                .movimientoInteragencia(movimientoInteragencia)
                .mensajeInteragencia(mensajeInteragencia)

                .codigoOperacion(request.getCodigoOperacion())
                .nombreOperacion(nombreOperacion(request.getCodigoOperacion()))
                .naturaleza(naturaleza)

                .valorEfectivo(valorEfectivo)
                .valorCheques(valorCheques)
                .valorTransferencias(valorTransferencias)
                .valorTotal(valorTotal)

                .saldoAnterior(saldoAnterior)
                .valorCanje(valorCanje)
                .saldoDisponible(saldoDisponible)
                .saldoFinal(saldoFinal)

                .permiteAplicar(
                        errores.isEmpty()
                                && !bloqueaSarlaft
                )

                .mensaje(
                        errores.isEmpty()
                                ? "Preview generado correctamente."
                                : "El movimiento tiene validaciones pendientes."
                )

                .sarlaft(sarlaft)
                .errores(errores)
                .build();
    }

    @Transactional
    public CajaCapturaDepositosResponseDTO aplicar(
            CajaCapturaDepositosRequestDTO request
    ) {

        CajaCapturaDepositosPreviewDTO preview = preview(request);

        if (preview.getErrores() != null && !preview.getErrores().isEmpty()) {
            throw new RuntimeException(
                    String.join(" | ", preview.getErrores())
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        CajaCapturaDepositosCuentaDTO cuenta =
                repository.obtenerCuenta(
                        request.getIdCuentaAhorro()
                );

        evaluarSarlaftAplicando(
                cuenta,
                request,
                idUsuario,
                preview.getValorTotal()
        );

        Long idDepartamento = validarDepartamentoDepositos();
        Long idOperacion = validarOperacion(request.getCodigoOperacion());
        List<Long> movimientosCaja = new ArrayList<>();

        String referencia = preview.getCodigoCuenta();

        String concepto = construirConcepto(preview);

        DepositosMovimientoDTO movimientoDeposito = new DepositosMovimientoDTO();

        movimientoDeposito.setIdCuentaAhorro(
                Math.toIntExact(
                        request.getIdCuentaAhorro()
                )
        );

        movimientoDeposito.setFechaMovimiento(request.getFechaContable());
        movimientoDeposito.setTipoComprobante(request.getTipoComprobante());
        movimientoDeposito.setNumeroComprobante(request.getNumeroComprobante());
        movimientoDeposito.setTipoMovimiento(request.getTipoMovimiento());
        movimientoDeposito.setModulo(MODULO_CAJAS);
        movimientoDeposito.setTarjeta(TARJETA_NO);
        movimientoDeposito.setEstablecimiento(concepto);

        if (NATURALEZA_INGRESO.equalsIgnoreCase(preview.getNaturaleza())) {
            movimientoDeposito.setValorCredito(preview.getValorTotal());
            movimientoDeposito.setValorDebito(BigDecimal.ZERO);

            depositosMovimientoService.registrarCredito(
                    movimientoDeposito,
                    idUsuario
            );
        } else {
            movimientoDeposito.setValorDebito(preview.getValorTotal());
            movimientoDeposito.setValorCredito(BigDecimal.ZERO);

            depositosMovimientoService.registrarDebito(
                    movimientoDeposito,
                    idUsuario
            );
        }

        if (preview.getValorEfectivo().compareTo(BigDecimal.ZERO) > 0) {

            Long idMovimiento = repository.insertarMovimientoCaja(
                    preview.getIdProvision(),
                    request.getIdCaja(),
                    idDepartamento,
                    idOperacion,
                    MODULO_ORIGEN,
                    PROCESO_ORIGEN,
                    TABLA_ORIGEN,
                    request.getIdCuentaAhorro(),
                    request.getTipoComprobante(),
                    request.getNumeroComprobante(),
                    referencia,
                    preview.getNaturaleza(),
                    MEDIO_EFECTIVO,
                    preview.getValorEfectivo(),
                    concepto,
                    idUsuario
            );

            movimientosCaja.add(idMovimiento);
        }

        if (preview.getValorCheques().compareTo(BigDecimal.ZERO) > 0) {

            Long idMovimiento = repository.insertarMovimientoCaja(
                    preview.getIdProvision(),
                    request.getIdCaja(),
                    idDepartamento,
                    idOperacion,
                    MODULO_ORIGEN,
                    PROCESO_ORIGEN,
                    TABLA_ORIGEN,
                    request.getIdCuentaAhorro(),
                    request.getTipoComprobante(),
                    request.getNumeroComprobante(),
                    referencia,
                    preview.getNaturaleza(),
                    MEDIO_CHEQUE,
                    preview.getValorCheques(),
                    concepto,
                    idUsuario
            );

            movimientosCaja.add(idMovimiento);

            for (CajaCapturaDepositosChequeDTO cheque : request.getCheques()) {

                repository.insertarChequeRecibido(
                        idMovimiento,
                        request.getIdCaja(),
                        cheque.getCodigoBanco(),
                        cheque.getNumeroCheque(),
                        nvl(cheque.getValorCheque()),
                        request.getNumeroComprobante(),
                        referencia,
                        MODULO_ORIGEN,
                        PROCESO_ORIGEN,
                        TABLA_ORIGEN,
                        request.getIdCuentaAhorro(),
                        concepto,
                        idUsuario
                );

                repository.insertarCanjeCuentaAhorro(
                        request.getIdCuentaAhorro(),
                        request.getFechaContable(),
                        nvl(cheque.getValorCheque()),
                        cheque.getNumeroCheque(),
                        idUsuario
                );
            }
        }

        if (preview.getValorTransferencias().compareTo(BigDecimal.ZERO) > 0) {

            Long idMovimiento = repository.insertarMovimientoCaja(
                    preview.getIdProvision(),
                    request.getIdCaja(),
                    idDepartamento,
                    idOperacion,
                    MODULO_ORIGEN,
                    PROCESO_ORIGEN,
                    TABLA_ORIGEN,
                    request.getIdCuentaAhorro(),
                    request.getTipoComprobante(),
                    request.getNumeroComprobante(),
                    referencia,
                    preview.getNaturaleza(),
                    MEDIO_TRANSFERENCIA,
                    preview.getValorTransferencias(),
                    concepto,
                    idUsuario
            );

            movimientosCaja.add(idMovimiento);
        }

        boolean requiereLavado =
                lavadoActivosService.requiereControlLavado(
                        request.getIdAgencia(),
                        preview.getValorTotal()
                );

        return CajaCapturaDepositosResponseDTO.builder()
                .idCuentaAhorro(preview.getIdCuentaAhorro())
                .codigoCuenta(preview.getCodigoCuenta())
                .documento(preview.getDocumento())
                .nombreAsociado(preview.getNombreAsociado())
                .fechaContable(preview.getFechaContable())
                .valorTotal(preview.getValorTotal())
                .saldoAnterior(preview.getSaldoAnterior())
                .saldoFinal(preview.getSaldoFinal())
                .movimientosCaja(movimientosCaja)
                .mensaje("Movimiento de caja registrado correctamente.")
                .requiereFormatoLavadoActivos(requiereLavado)
                .build();
    }

    private void validarRequestBase(
            CajaCapturaDepositosRequestDTO request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "No se recibió información del movimiento."
            );
        }

        if (request.getIdCaja() == null) {
            throw new RuntimeException(
                    "La caja es obligatoria."
            );
        }

        if (request.getIdAgencia() == null) {
            throw new RuntimeException(
                    "La agencia es obligatoria."
            );
        }

        if (request.getFechaContable() == null) {
            throw new RuntimeException(
                    "La fecha contable es obligatoria."
            );
        }

        if (request.getIdCuentaAhorro() == null) {
            throw new RuntimeException(
                    "La cuenta de ahorro es obligatoria."
            );
        }

        if (isBlank(request.getCodigoOperacion())) {
            throw new RuntimeException(
                    "La operación de caja es obligatoria."
            );
        }

        if (!OPERACION_CONSIGNACION.equals(request.getCodigoOperacion())
                && !OPERACION_RETIRO.equals(request.getCodigoOperacion())) {

            throw new RuntimeException(
                    "La operación no está permitida para captura de depósitos."
            );
        }

        if (isBlank(request.getNumeroComprobante())) {
            throw new RuntimeException(
                    "El número del desprendible/libreta u orden es obligatorio."
            );
        }
    }

    private Long validarProvisionAbierta(
            Long idCaja,
            LocalDate fechaContable
    ) {

        Long idProvision = repository.obtenerProvisionAbierta(
                idCaja,
                fechaContable
        );

        if (idProvision == null) {
            throw new RuntimeException(
                    "La caja no tiene provisión abierta para la fecha contable actual."
            );
        }

        return idProvision;
    }

    private Long validarDepartamentoDepositos() {

        Long idDepartamento = repository.obtenerDepartamentoDepositos();

        if (idDepartamento == null) {
            throw new RuntimeException(
                    "No existe departamento operativo de depósitos/cuentas de ahorro."
            );
        }

        return idDepartamento;
    }

    private Long validarOperacion(String codigoOperacion) {

        Long idOperacion = repository.obtenerOperacionCaja(
                codigoOperacion.trim()
        );

        if (idOperacion == null) {
            throw new RuntimeException(
                    "La operación de caja no existe o está inactiva."
            );
        }

        return idOperacion;
    }

    private CajaCapturaDepositosCuentaDTO validarCuenta(
            CajaCapturaDepositosRequestDTO request
    ) {

        CajaCapturaDepositosCuentaDTO cuenta =
                repository.obtenerCuenta(
                        request.getIdCuentaAhorro()
                );

        if (cuenta == null) {
            throw new RuntimeException(
                    "La cuenta de ahorro no existe."
            );
        }

        if (cuenta.getIdAgencia() == null) {

            throw new RuntimeException(
                    "La cuenta no tiene agencia asociada."
            );
        }

        if (Boolean.FALSE.equals(cuenta.getEstadoOperativo())) {
            throw new RuntimeException(
                    cuenta.getMensajeOperativo()
            );
        }

        if (Boolean.FALSE.equals(cuenta.getEstadoOperativo())) {
            throw new RuntimeException(
                    cuenta.getMensajeOperativo()
            );
        }

        return cuenta;
    }

    private String construirConcepto(
            CajaCapturaDepositosPreviewDTO preview
    ) {

        return preview.getNombreOperacion()
                + " - Cuenta "
                + preview.getCodigoCuenta()
                + " - "
                + preview.getNombreAsociado();
    }

    private String nombreOperacion(String codigoOperacion) {

        if (OPERACION_CONSIGNACION.equals(codigoOperacion)) {
            return "Consignación cuenta de ahorro";
        }

        if (OPERACION_RETIRO.equals(codigoOperacion)) {
            return "Retiro cuenta de ahorro";
        }

        return codigoOperacion;
    }

    private EvaluacionResultado evaluarSarlaftPreview(
            CajaCapturaDepositosCuentaDTO cuenta,
            CajaCapturaDepositosRequestDTO request,
            Integer idUsuario,
            BigDecimal valor
    ) {
        return sarlaftAlertaService.evaluarOperacionPreview(
                construirInputSarlaft(
                        cuenta,
                        request,
                        idUsuario,
                        valor
                )
        );
    }

    private EvaluacionResultado evaluarSarlaftAplicando(
            CajaCapturaDepositosCuentaDTO cuenta,
            CajaCapturaDepositosRequestDTO request,
            Integer idUsuario,
            BigDecimal valor
    ) {
        return sarlaftAlertaService.evaluarOperacion(
                construirInputSarlaft(
                        cuenta,
                        request,
                        idUsuario,
                        valor
                )
        );
    }

    private ReglasInput construirInputSarlaft(
            CajaCapturaDepositosCuentaDTO cuenta,
            CajaCapturaDepositosRequestDTO request,
            Integer idUsuario,
            BigDecimal valor
    ) {
        ReglasInput input = new ReglasInput();

        input.setIdDatosPersonal(
                cuenta.getIdDatosPersonal()
        );

        input.setIdAgencia(request.getIdAgencia());
        input.setIdUsuario(idUsuario);
        input.setCodigoModulo(MODULO_CAJAS);
        input.setAccion(trim(request.getTipoMovimiento()));
        input.setCodigoFormaAhorro(cuenta.getCodigoForma());
        input.setMonto(valor.doubleValue());

        return input;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }

    private void validarDocumentoSoportePreview(
            CajaCapturaDepositosRequestDTO request,
            List<String> errores
    ) {
        String numero = request.getNumeroComprobante() == null
                ? ""
                : request.getNumeroComprobante().trim();

        if (numero.isEmpty()) {
            errores.add("El número del desprendible/libreta u orden es obligatorio.");
            return;
        }

        if (!numero.matches("\\d+")) {
            errores.add("El número del desprendible/libreta u orden debe ser numérico.");
            return;
        }

        boolean valido = repository.validarDocumentoSoporteCuenta(
                request.getIdCuentaAhorro(),
                numero
        );

        if (!valido) {
            errores.add("El desprendible/libreta u orden no pertenece a la cuenta seleccionada.");
        }

        boolean usado = repository.existeDocumentoSoporteUsado(
                request.getIdCuentaAhorro(),
                request.getTipoComprobante(),
                numero
        );

        if (usado) {
            errores.add("El desprendible/libreta u orden ya fue utilizado en un movimiento anterior.");
        }

    }

    private void validarChequesPreview(
            CajaCapturaDepositosRequestDTO request,
            BigDecimal valorCheques,
            List<String> errores
    ) {
        if (valorCheques.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (request.getCheques() == null || request.getCheques().isEmpty()) {
            errores.add("Debe capturar el detalle de los cheques recibidos.");
            return;
        }

        BigDecimal totalDetalle = BigDecimal.ZERO;

        for (CajaCapturaDepositosChequeDTO cheque : request.getCheques()) {

            if (isBlank(cheque.getCodigoBanco())) {
                errores.add("El banco del cheque es obligatorio.");
            }

            if (isBlank(cheque.getNumeroCheque())) {
                errores.add("El número del cheque es obligatorio.");
            }

            BigDecimal valorCheque = nvl(cheque.getValorCheque());

            if (valorCheque.compareTo(BigDecimal.ZERO) <= 0) {
                errores.add("El valor del cheque debe ser mayor a cero.");
            }

            totalDetalle = totalDetalle.add(valorCheque);
        }

        if (totalDetalle.compareTo(valorCheques) != 0) {
            errores.add("El total del detalle de cheques no coincide con el valor de cheques.");
        }
    }

    private LavadoActivosRequestDTO construirRequestLavadoActivos(
            CajaCapturaDepositosRequestDTO request,
            CajaCapturaDepositosPreviewDTO preview,
            CajaCapturaDepositosCuentaDTO cuenta
    ) {
        return LavadoActivosRequestDTO.builder()
                .modulo(MODULO_CAJAS)
                .proceso(PROCESO_ORIGEN)
                .idOrigen(request.getIdCuentaAhorro())

                .fechaTransaccion(request.getFechaContable())
                .fechaContable(request.getFechaContable())

                .tipoTransaccion(preview.getNaturaleza())
                .valorTransaccion(preview.getValorTotal())

                .idAgencia(request.getIdAgencia())
                .codigoAgencia(cuenta.getCodigoAgencia())
                .nombreAgencia(cuenta.getNombreAgencia())

                .idDatosPersonal(cuenta.getIdDatosPersonal())
                .documento(cuenta.getDocumento())
                .nombreCompleto(cuenta.getNombreAsociado())

                .codigoProducto(cuenta.getCodigoForma())
                .descripcionProducto(cuenta.getNombreForma())

                .numeroProducto(cuenta.getCodigoCuenta())
                .numeroComprobante(request.getNumeroComprobante())

                .tipoDocumentoRealiza(null)
                .documentoRealiza(cuenta.getDocumento())
                .nombreBeneficiario(cuenta.getNombreAsociado())

                .build();
    }
}