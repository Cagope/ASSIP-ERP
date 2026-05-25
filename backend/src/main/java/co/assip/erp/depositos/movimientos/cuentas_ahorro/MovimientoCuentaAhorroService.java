package co.assip.erp.depositos.movimientos.cuentasahorro;

import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.CuentaMovimientoDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.MovimientoCuentaPreviewDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.MovimientoCuentaRequestDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.MovimientoCuentaResponseDTO;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.TipoMovimientoDTO;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.sarlaft.evaluacion.SarlaftAlertaService;
import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;
import co.assip.erp.sarlaft.evaluacion.dto.ReglasInput;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoCuentaAhorroService {

    private static final String MODULO_DEPOSITOS = "02";
    private static final String TARJETA_NO = "N";

    private final MovimientoCuentaAhorroRepository repository;
    private final DepositosMovimientoService depositosMovimientoService;
    private final SarlaftAlertaService sarlaftAlertaService;
    private final UsuarioSesionService usuarioSesionService;

    public List<CuentaMovimientoDTO> buscarCuentas(
            Integer idAgencia,
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido
    ) {
        return repository.buscarCuentas(
                idAgencia,
                documento,
                nombres,
                primerApellido,
                segundoApellido
        );
    }

    public List<TipoMovimientoDTO> listarTiposMovimiento() {
        return repository.listarTiposMovimiento();
    }

    public MovimientoCuentaPreviewDTO preview(MovimientoCuentaRequestDTO request) {
        validarRequestBase(request);

        Integer idUsuario = usuarioSesionService.idUsuario();

        CuentaMovimientoDTO cuenta = obtenerYValidarCuenta(request);

        TipoMovimientoDTO tipo = obtenerYValidarTipoMovimiento(request);

        BigDecimal valor = validarValorMovimiento(request);

        boolean entrada = esEntrada(tipo.getAccionMovimiento());
        boolean salida = esSalida(tipo.getAccionMovimiento());

        if (!entrada && !salida) {
            throw new RuntimeException("La acción del tipo de movimiento no está configurada correctamente.");
        }

        EvaluacionResultado sarlaft = evaluarSarlaftPreview(
                cuenta,
                request,
                idUsuario,
                valor
        );

        BigDecimal saldoActual = nvl(cuenta.getSaldoActualCuenta());
        BigDecimal valorEnCanje = nvl(cuenta.getValorEnCanje());
        BigDecimal saldoDisponible = saldoActual.subtract(valorEnCanje);
        BigDecimal valorGmf = BigDecimal.ZERO;

        BigDecimal saldoFinal;

        if (entrada) {
            saldoFinal = saldoActual.add(valor);
        } else {
            BigDecimal totalSalida = valor.add(valorGmf);

            if (saldoDisponible.compareTo(totalSalida) < 0) {
                throw new RuntimeException("La cuenta no tiene saldo disponible suficiente para el movimiento.");
            }

            saldoFinal = saldoActual.subtract(totalSalida);
        }

        return MovimientoCuentaPreviewDTO.builder()
                .idCuentaAhorro(cuenta.getIdCuentaAhorro())
                .codigoCuenta(cuenta.getCodigoCuenta())
                .documento(cuenta.getDocumento())
                .nombreAsociado(cuenta.getNombreAsociado())
                .codigoForma(cuenta.getCodigoForma())
                .nombreForma(cuenta.getNombreForma())
                .tipoMovimiento(tipo.getCodigoMovimiento())
                .descripcionMovimiento(tipo.getDescripcion())
                .accionMovimiento(tipo.getAccionMovimiento())
                .fechaMovimiento(request.getFechaMovimiento())
                .saldoActual(saldoActual)
                .valorMovimiento(valor)
                .valorGmf(valorGmf)
                .saldoFinal(saldoFinal)
                .contabilizacionDiaria(tipo.getContabilizacionDiaria())
                .generaGmf(tipo.getGeneraGmf())
                .sarlaft(sarlaft)
                .build();
    }

    public MovimientoCuentaResponseDTO aplicar(MovimientoCuentaRequestDTO request) {
        validarRequestAplicar(request);

        MovimientoCuentaPreviewDTO preview = preview(request);

        Integer idUsuario = usuarioSesionService.idUsuario();

        CuentaMovimientoDTO cuenta = repository.obtenerCuenta(
                request.getIdCuentaAhorro()
        );

        if (cuenta == null) {
            throw new RuntimeException("La cuenta de ahorro no existe.");
        }

        evaluarSarlaftAplicando(
                cuenta,
                request,
                idUsuario,
                preview.getValorMovimiento()
        );

        DepositosMovimientoDTO dto = new DepositosMovimientoDTO();
        dto.setIdCuentaAhorro(request.getIdCuentaAhorro());
        dto.setFechaMovimiento(request.getFechaMovimiento());
        dto.setTipoComprobante(trim(request.getTipoComprobante()));
        dto.setNumeroComprobante(trim(request.getNumeroComprobante()));
        dto.setTipoMovimiento(trim(request.getTipoMovimiento()));
        dto.setModulo(MODULO_DEPOSITOS);
        dto.setTarjeta(TARJETA_NO);
        dto.setEstablecimiento(trim(request.getDetalle()));

        if (esEntrada(preview.getAccionMovimiento())) {
            dto.setValorCredito(preview.getValorMovimiento());
            dto.setValorDebito(BigDecimal.ZERO);

            validarMovimientoDuplicado(
                    request,
                    BigDecimal.ZERO,
                    preview.getValorMovimiento()
            );

            depositosMovimientoService.registrarCredito(dto, idUsuario);

        } else {
            dto.setValorDebito(preview.getValorMovimiento());
            dto.setValorCredito(BigDecimal.ZERO);

            validarMovimientoDuplicado(
                    request,
                    preview.getValorMovimiento(),
                    BigDecimal.ZERO
            );

            depositosMovimientoService.registrarDebito(dto, idUsuario);
        }

        return MovimientoCuentaResponseDTO.builder()
                .idCuentaAhorro(preview.getIdCuentaAhorro())
                .codigoCuenta(preview.getCodigoCuenta())
                .documento(preview.getDocumento())
                .nombreAsociado(preview.getNombreAsociado())
                .tipoMovimiento(preview.getTipoMovimiento())
                .descripcionMovimiento(preview.getDescripcionMovimiento())
                .fechaMovimiento(preview.getFechaMovimiento())
                .valorMovimiento(preview.getValorMovimiento())
                .valorGmf(preview.getValorGmf())
                .saldoAnterior(preview.getSaldoActual())
                .saldoFinal(preview.getSaldoFinal())
                .tipoComprobante(trim(request.getTipoComprobante()))
                .numeroComprobante(trim(request.getNumeroComprobante()))
                .mensaje("Movimiento aplicado correctamente.")
                .build();
    }

    private CuentaMovimientoDTO obtenerYValidarCuenta(
            MovimientoCuentaRequestDTO request
    ) {
        CuentaMovimientoDTO cuenta = repository.obtenerCuenta(
                request.getIdCuentaAhorro()
        );

        if (cuenta == null) {
            throw new RuntimeException("La cuenta de ahorro no existe.");
        }

        validarAgenciaCuenta(request, cuenta);
        validarCuentaOperativa(cuenta);

        return cuenta;
    }

    private TipoMovimientoDTO obtenerYValidarTipoMovimiento(
            MovimientoCuentaRequestDTO request
    ) {
        TipoMovimientoDTO tipo = repository.obtenerTipoMovimiento(
                trim(request.getTipoMovimiento())
        );

        if (tipo == null) {
            throw new RuntimeException("El tipo de movimiento no existe o no está permitido para inclusión manual.");
        }

        return tipo;
    }

    private BigDecimal validarValorMovimiento(
            MovimientoCuentaRequestDTO request
    ) {
        BigDecimal valor = nvl(request.getValorMovimiento());

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El valor del movimiento debe ser mayor a cero.");
        }

        return valor;
    }

    private void validarRequestBase(MovimientoCuentaRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("No se recibió información del movimiento.");
        }

        if (request.getIdAgencia() == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }

        if (request.getIdCuentaAhorro() == null) {
            throw new RuntimeException("La cuenta de ahorro es obligatoria.");
        }

        if (request.getFechaMovimiento() == null) {
            throw new RuntimeException("La fecha del movimiento es obligatoria.");
        }

        if (isBlank(request.getTipoMovimiento())) {
            throw new RuntimeException("El tipo de movimiento es obligatorio.");
        }

        if (request.getValorMovimiento() == null) {
            throw new RuntimeException("El valor del movimiento es obligatorio.");
        }
    }

    private void validarRequestAplicar(MovimientoCuentaRequestDTO request) {
        validarRequestBase(request);

        if (isBlank(request.getTipoComprobante())) {
            throw new RuntimeException("El tipo de comprobante es obligatorio.");
        }

        if (isBlank(request.getNumeroComprobante())) {
            throw new RuntimeException("El número de comprobante es obligatorio.");
        }
    }

    private void validarAgenciaCuenta(
            MovimientoCuentaRequestDTO request,
            CuentaMovimientoDTO cuenta
    ) {
        if (cuenta.getIdAgencia() == null) {
            throw new RuntimeException("La cuenta no tiene agencia configurada.");
        }

        if (!cuenta.getIdAgencia().equals(request.getIdAgencia())) {
            throw new RuntimeException("La cuenta no pertenece a la agencia activa.");
        }
    }

    private void validarCuentaOperativa(CuentaMovimientoDTO cuenta) {
        if (Boolean.FALSE.equals(cuenta.getEstadoOperativo())) {
            throw new RuntimeException(cuenta.getMensajeOperativo());
        }
    }

    private EvaluacionResultado evaluarSarlaftPreview(
            CuentaMovimientoDTO cuenta,
            MovimientoCuentaRequestDTO request,
            Integer idUsuario,
            BigDecimal valor
    ) {
        return sarlaftAlertaService.evaluarOperacionPreview(
                construirInputSarlaft(cuenta, request, idUsuario, valor)
        );
    }

    private EvaluacionResultado evaluarSarlaftAplicando(
            CuentaMovimientoDTO cuenta,
            MovimientoCuentaRequestDTO request,
            Integer idUsuario,
            BigDecimal valor
    ) {
        return sarlaftAlertaService.evaluarOperacion(
                construirInputSarlaft(cuenta, request, idUsuario, valor)
        );
    }

    private ReglasInput construirInputSarlaft(
            CuentaMovimientoDTO cuenta,
            MovimientoCuentaRequestDTO request,
            Integer idUsuario,
            BigDecimal valor
    ) {
        ReglasInput input = new ReglasInput();

        input.setIdDatosPersonal(
                cuenta.getIdDatosPersonal() == null
                        ? null
                        : cuenta.getIdDatosPersonal().longValue()
        );

        input.setIdAgencia(request.getIdAgencia());
        input.setIdUsuario(idUsuario);
        input.setCodigoModulo(MODULO_DEPOSITOS);
        input.setAccion(trim(request.getTipoMovimiento()));
        input.setCodigoFormaAhorro(cuenta.getCodigoForma());
        input.setMonto(valor.doubleValue());

        return input;
    }

    private void validarMovimientoDuplicado(
            MovimientoCuentaRequestDTO request,
            BigDecimal valorDebito,
            BigDecimal valorCredito
    ) {
        if (repository.existeMovimientoIgual(request, valorDebito, valorCredito)) {
            throw new RuntimeException("El movimiento ya fue registrado anteriormente.");
        }
    }

    private boolean esEntrada(String accion) {
        String value = trim(accion).toUpperCase();

        return value.equals("S")
                || value.equals("C")
                || value.equals("CR")
                || value.equals("E")
                || value.equals("+")
                || value.equals("ENTRADA")
                || value.equals("CREDITO");
    }

    private boolean esSalida(String accion) {
        String value = trim(accion).toUpperCase();

        return value.equals("R")
                || value.equals("D")
                || value.equals("DB")
                || value.equals("-")
                || value.equals("SALIDA")
                || value.equals("DEBITO");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}