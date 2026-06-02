package co.assip.erp.cajas.movimientos_interagencia;

import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaChequeDTO;
import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaCuentaDTO;
import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaPreviewDTO;
import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaRequestDTO;
import co.assip.erp.cajas.movimientos_interagencia.dto.MovimientoInteragenciaResponseDTO;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.sarlaft.evaluacion.SarlaftAlertaService;
import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;
import co.assip.erp.sarlaft.evaluacion.dto.ReglasInput;
import co.assip.erp.sarlaft.lavado_activos.LavadoActivosService;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoInteragenciaService {

    private static final String MODULO_ORIGEN = "DEPOSITOS";
    private static final String PROCESO_ORIGEN = "MOVIMIENTO_INTERAGENCIA";
    private static final String TABLA_ORIGEN = "depositos.cuentas_ahorro";

    private static final String OPERACION_CONSIGNACION = "CONSIGNACION_AHORRO";
    private static final String OPERACION_RETIRO = "RETIRO_AHORRO";

    private static final String NATURALEZA_INGRESO = "INGRESO";
    private static final String NATURALEZA_EGRESO = "EGRESO";

    private static final String MEDIO_EFECTIVO = "EFECTIVO";
    private static final String MEDIO_CHEQUE = "CHEQUE";

    private static final String MODULO_CAJAS = "05";
    private static final String TARJETA_NO = "N";

    private final MovimientoInteragenciaRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final SarlaftAlertaService sarlaftAlertaService;
    private final DepositosMovimientoService depositosMovimientoService;
    private final LavadoActivosService lavadoActivosService;

    public List<MovimientoInteragenciaCuentaDTO> buscarCuentas(
            Integer idAgenciaCaja,
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido
    ) {

        if (idAgenciaCaja == null) {
            throw new RuntimeException("La agencia de caja es obligatoria.");
        }

        if (isBlank(documento)
                && isBlank(nombres)
                && isBlank(primerApellido)
                && isBlank(segundoApellido)) {
            throw new RuntimeException("Debe ingresar al menos un criterio de búsqueda.");
        }

        return repository.buscarCuentas(
                idAgenciaCaja,
                documento,
                nombres,
                primerApellido,
                segundoApellido
        );
    }

    public MovimientoInteragenciaPreviewDTO preview(
            MovimientoInteragenciaRequestDTO request
    ) {

        validarRequestBase(request);

        List<String> errores = new ArrayList<>();

        Long idProvision = validarProvisionAbierta(
                request.getIdCaja(),
                request.getFechaContable()
        );

        Long idDepartamento = validarDepartamentoDepositos();
        Long idOperacion = validarOperacion(request.getCodigoOperacion());

        MovimientoInteragenciaCuentaDTO cuenta =
                validarCuentaInteragencia(request);

        String naturaleza =
                repository.obtenerNaturalezaOperacion(
                        request.getCodigoOperacion().trim()
                );

        if (isBlank(naturaleza)) {
            throw new RuntimeException(
                    "La operación de caja no tiene naturaleza configurada."
            );
        }

        BigDecimal valorEfectivo =
                nvl(request.getValorEfectivo());

        BigDecimal valorCheques =
                nvl(request.getValorCheques());

        BigDecimal valorTotal =
                valorEfectivo
                        .add(valorCheques);

        if (OPERACION_RETIRO.equals(request.getCodigoOperacion())
                && valorCheques.compareTo(BigDecimal.ZERO) > 0) {

            errores.add(
                    "Los retiros interagencia solo pueden realizarse en efectivo."
            );
        }

        if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("El valor total del movimiento debe ser mayor a cero.");
        }

        validarDocumentoSoportePreview(
                request,
                errores
        );

        validarChequesPreview(
                request,
                valorCheques,
                errores
        );

        EvaluacionResultado sarlaft =
                evaluarSarlaftPreview(
                        cuenta,
                        request,
                        usuarioSesionService.idUsuario(),
                        valorTotal
                );

        BigDecimal saldoAnterior =
                nvl(cuenta.getSaldoActual());

        BigDecimal valorCanje =
                nvl(cuenta.getValorCanje());

        BigDecimal saldoDisponible =
                saldoAnterior.subtract(valorCanje);

        BigDecimal saldoFinal =
                saldoAnterior;

        if (NATURALEZA_INGRESO.equalsIgnoreCase(naturaleza)) {

            saldoFinal =
                    saldoAnterior.add(valorTotal);

        } else if (NATURALEZA_EGRESO.equalsIgnoreCase(naturaleza)) {

            if (saldoDisponible.compareTo(valorTotal) < 0) {
                errores.add("La cuenta no tiene saldo disponible suficiente para el retiro.");
            }

            saldoFinal =
                    saldoAnterior.subtract(valorTotal);

        } else {
            errores.add("La naturaleza de la operación no es válida.");
        }

        boolean bloqueaSarlaft =
                sarlaft != null
                        && Boolean.TRUE.equals(
                        sarlaft.getBloqueaOperacion()
                );

        return MovimientoInteragenciaPreviewDTO.builder()
                .idCaja(request.getIdCaja())
                .idProvision(idProvision)
                .idAgenciaCaja(request.getIdAgenciaCaja())
                .fechaContable(request.getFechaContable())

                .idCuentaAhorro(cuenta.getIdCuentaAhorro())
                .idAgenciaCuenta(cuenta.getIdAgenciaCuenta())
                .codigoAgenciaCuenta(cuenta.getCodigoAgenciaCuenta())
                .nombreAgenciaCuenta(cuenta.getNombreAgenciaCuenta())

                .codigoCuenta(cuenta.getCodigoCuenta())
                .documento(cuenta.getDocumento())
                .nombreAsociado(cuenta.getNombreAsociado())

                .codigoForma(cuenta.getCodigoForma())
                .nombreForma(cuenta.getNombreForma())

                .codigoOperacion(request.getCodigoOperacion())
                .nombreOperacion(nombreOperacion(request.getCodigoOperacion()))

                .tipoMovimiento(request.getTipoMovimiento())
                .nombreTipoMovimiento(nombreMovimiento(request.getTipoMovimiento()))
                .naturaleza(naturaleza)

                .tipoComprobante(request.getTipoComprobante())
                .numeroComprobante(trim(request.getNumeroComprobante()))

                .valorEfectivo(valorEfectivo)
                .valorCheques(valorCheques)
                .valorTotal(valorTotal)

                .saldoAnterior(saldoAnterior)
                .valorCanje(valorCanje)
                .saldoDisponible(saldoDisponible)
                .saldoFinal(saldoFinal)

                .permiteAplicar(errores.isEmpty() && !bloqueaSarlaft)
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
    public MovimientoInteragenciaResponseDTO aplicar(
            MovimientoInteragenciaRequestDTO request
    ) {

        MovimientoInteragenciaPreviewDTO preview =
                preview(request);

        if (preview.getErrores() != null
                && !preview.getErrores().isEmpty()) {
            throw new RuntimeException(
                    String.join(" | ", preview.getErrores())
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        MovimientoInteragenciaCuentaDTO cuenta =
                repository.obtenerCuenta(
                        request.getIdCuentaAhorro()
                );

        evaluarSarlaftAplicando(
                cuenta,
                request,
                idUsuario,
                preview.getValorTotal()
        );

        Long idDepartamento =
                validarDepartamentoDepositos();

        Long idOperacion =
                validarOperacion(
                        request.getCodigoOperacion()
                );

        List<Long> movimientosCaja =
                new ArrayList<>();

        String referencia =
                preview.getCodigoCuenta();

        String concepto =
                construirConcepto(preview);

        DepositosMovimientoDTO movimientoDeposito =
                new DepositosMovimientoDTO();

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

            Long idMovimiento =
                    repository.insertarMovimientoCaja(
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

            Long idMovimiento =
                    repository.insertarMovimientoCaja(
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

            for (MovimientoInteragenciaChequeDTO cheque : request.getCheques()) {

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

        boolean requiereLavado =
                lavadoActivosService.requiereControlLavado(
                        request.getIdAgenciaCaja(),
                        preview.getValorTotal()
                );

        return MovimientoInteragenciaResponseDTO.builder()
                .idCuentaAhorro(preview.getIdCuentaAhorro())
                .codigoCuenta(preview.getCodigoCuenta())
                .idAgenciaCaja(preview.getIdAgenciaCaja())
                .idAgenciaCuenta(preview.getIdAgenciaCuenta())
                .documento(preview.getDocumento())
                .nombreAsociado(preview.getNombreAsociado())
                .fechaContable(preview.getFechaContable())
                .tipoMovimiento(preview.getTipoMovimiento())
                .naturaleza(preview.getNaturaleza())
                .valorEfectivo(preview.getValorEfectivo())
                .valorCheques(preview.getValorCheques())
                .valorTotal(preview.getValorTotal())
                .saldoAnterior(preview.getSaldoAnterior())
                .saldoFinal(preview.getSaldoFinal())
                .movimientosCaja(movimientosCaja)
                .requiereFormatoLavadoActivos(requiereLavado)
                .mensaje("Movimiento interagencia registrado correctamente.")
                .build();
    }

    private void validarRequestBase(
            MovimientoInteragenciaRequestDTO request
    ) {

        if (request == null) {
            throw new RuntimeException("No se recibió información del movimiento.");
        }

        if (request.getIdCaja() == null) {
            throw new RuntimeException("La caja es obligatoria.");
        }

        if (request.getIdAgenciaCaja() == null) {
            throw new RuntimeException("La agencia de caja es obligatoria.");
        }

        if (request.getFechaContable() == null) {
            throw new RuntimeException("La fecha contable es obligatoria.");
        }

        if (request.getIdCuentaAhorro() == null) {
            throw new RuntimeException("La cuenta de ahorro es obligatoria.");
        }

        if (isBlank(request.getCodigoOperacion())) {
            throw new RuntimeException("La operación de caja es obligatoria.");
        }

        if (!OPERACION_CONSIGNACION.equals(request.getCodigoOperacion())
                && !OPERACION_RETIRO.equals(request.getCodigoOperacion())) {

            throw new RuntimeException(
                    "La operación no está permitida para movimientos interagencia."
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

        Long idProvision =
                repository.obtenerProvisionAbierta(
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

        Long idDepartamento =
                repository.obtenerDepartamentoDepositos();

        if (idDepartamento == null) {
            throw new RuntimeException(
                    "No existe departamento operativo de depósitos/cuentas de ahorro."
            );
        }

        return idDepartamento;
    }

    private Long validarOperacion(
            String codigoOperacion
    ) {

        Long idOperacion =
                repository.obtenerOperacionCaja(
                        codigoOperacion.trim()
                );

        if (idOperacion == null) {
            throw new RuntimeException(
                    "La operación de caja no existe o está inactiva."
            );
        }

        return idOperacion;
    }

    private MovimientoInteragenciaCuentaDTO validarCuentaInteragencia(
            MovimientoInteragenciaRequestDTO request
    ) {

        MovimientoInteragenciaCuentaDTO cuenta =
                repository.obtenerCuenta(
                        request.getIdCuentaAhorro()
                );

        if (cuenta == null) {
            throw new RuntimeException("La cuenta de ahorro no existe.");
        }

        if (cuenta.getIdAgenciaCuenta() == null) {
            throw new RuntimeException("La cuenta no tiene agencia asignada.");
        }

        if (cuenta.getIdAgenciaCuenta().equals(request.getIdAgenciaCaja())) {
            throw new RuntimeException(
                    "La cuenta pertenece a la misma agencia. Use Captura depósitos normal."
            );
        }

        if (Boolean.FALSE.equals(cuenta.getEstadoOperativo())) {
            throw new RuntimeException(cuenta.getMensajeOperativo());
        }

        return cuenta;
    }

    private String construirConcepto(
            MovimientoInteragenciaPreviewDTO preview
    ) {

        return preview.getNombreOperacion()
                + " - Cuenta "
                + preview.getCodigoCuenta()
                + " - "
                + preview.getNombreAsociado();
    }

    private String nombreOperacion(
            String codigoOperacion
    ) {

        if (OPERACION_CONSIGNACION.equals(codigoOperacion)) {
            return "Consignación cuenta de ahorro";
        }

        if (OPERACION_RETIRO.equals(codigoOperacion)) {
            return "Retiro cuenta de ahorro";
        }

        return codigoOperacion;
    }

    private String nombreMovimiento(
            String tipoMovimiento
    ) {

        if ("001".equals(trim(tipoMovimiento))) {
            return "Consignación cuenta de ahorro";
        }

        if ("551".equals(trim(tipoMovimiento))) {
            return "Retiro cuenta de ahorro";
        }

        return "Movimiento cuenta de ahorro";
    }

    private EvaluacionResultado evaluarSarlaftPreview(
            MovimientoInteragenciaCuentaDTO cuenta,
            MovimientoInteragenciaRequestDTO request,
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
            MovimientoInteragenciaCuentaDTO cuenta,
            MovimientoInteragenciaRequestDTO request,
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
            MovimientoInteragenciaCuentaDTO cuenta,
            MovimientoInteragenciaRequestDTO request,
            Integer idUsuario,
            BigDecimal valor
    ) {

        ReglasInput input =
                new ReglasInput();

        input.setIdDatosPersonal(
                cuenta.getIdDatosPersonal()
        );

        input.setIdAgencia(request.getIdAgenciaCaja());
        input.setIdUsuario(idUsuario);
        input.setCodigoModulo(MODULO_CAJAS);
        input.setAccion(trim(request.getTipoMovimiento()));
        input.setCodigoFormaAhorro(cuenta.getCodigoForma());
        input.setMonto(valor.doubleValue());

        return input;
    }

    private void validarDocumentoSoportePreview(
            MovimientoInteragenciaRequestDTO request,
            List<String> errores
    ) {

        String numero =
                request.getNumeroComprobante() == null
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

        boolean valido =
                repository.validarDocumentoSoporteCuenta(
                        request.getIdCuentaAhorro(),
                        numero
                );

        if (!valido) {
            errores.add("El desprendible/libreta u orden no pertenece a la cuenta seleccionada.");
        }

        boolean usado =
                repository.existeDocumentoSoporteUsado(
                        request.getIdCuentaAhorro(),
                        request.getTipoComprobante(),
                        numero
                );

        if (usado) {
            errores.add("El desprendible/libreta u orden ya fue utilizado en un movimiento anterior.");
        }
    }

    private void validarChequesPreview(
            MovimientoInteragenciaRequestDTO request,
            BigDecimal valorCheques,
            List<String> errores
    ) {

        if (valorCheques.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (request.getCheques() == null
                || request.getCheques().isEmpty()) {
            errores.add("Debe capturar el detalle de los cheques recibidos.");
            return;
        }

        BigDecimal totalDetalle =
                BigDecimal.ZERO;

        for (MovimientoInteragenciaChequeDTO cheque : request.getCheques()) {

            if (isBlank(cheque.getCodigoBanco())) {
                errores.add("El banco del cheque es obligatorio.");
            }

            if (isBlank(cheque.getNumeroCheque())) {
                errores.add("El número del cheque es obligatorio.");
            }

            BigDecimal valorCheque =
                    nvl(cheque.getValorCheque());

            if (valorCheque.compareTo(BigDecimal.ZERO) <= 0) {
                errores.add("El valor del cheque debe ser mayor a cero.");
            }

            totalDetalle =
                    totalDetalle.add(valorCheque);
        }

        if (totalDetalle.compareTo(valorCheques) != 0) {
            errores.add("El total del detalle de cheques no coincide con el valor de cheques.");
        }
    }

    private BigDecimal nvl(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String trim(
            String value
    ) {
        return value == null
                ? ""
                : value.trim();
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }
}