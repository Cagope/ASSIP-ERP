package co.assip.erp.depositos.movimientos.cuentasahorro;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.cuentasahorro.dto.*;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import co.assip.erp.sarlaft.evaluacion.dto.ReglasInput;

import java.math.BigDecimal;
import java.util.List;
import co.assip.erp.sarlaft.evaluacion.SarlaftAlertaService;
import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;

@Service
@RequiredArgsConstructor
public class MovimientoCuentaAhorroService {

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
        validarRequest(request);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        CuentaMovimientoDTO cuenta =
                repository.obtenerCuenta(
                        request.getIdCuentaAhorro()
                );

        if (cuenta == null) {
            throw new RuntimeException("La cuenta de ahorro no existe.");
        }

        if (!cuenta.getIdAgencia().equals(request.getIdAgencia())) {
            throw new RuntimeException("La cuenta no pertenece a la agencia activa.");
        }

        validarCuentaOperativa(cuenta);

        TipoMovimientoDTO tipo =
                repository.obtenerTipoMovimiento(
                        request.getTipoMovimiento()
                );

        if (tipo == null) {
            throw new RuntimeException("El tipo de movimiento no existe o no está permitido para inclusión manual.");
        }

        BigDecimal valor =
                nvl(request.getValorMovimiento());

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El valor del movimiento debe ser mayor a cero.");
        }

        ReglasInput inputSarlaft =
                new ReglasInput();

        inputSarlaft.setIdDatosPersonal(
                cuenta.getIdDatosPersonal().longValue()
        );

        inputSarlaft.setIdAgencia(
                request.getIdAgencia()
        );

        inputSarlaft.setIdUsuario(
                idUsuario
        );

        inputSarlaft.setCodigoModulo("02");
        inputSarlaft.setAccion(request.getTipoMovimiento());
        inputSarlaft.setCodigoFormaAhorro(cuenta.getCodigoForma());
        inputSarlaft.setMonto(valor.doubleValue());

        EvaluacionResultado sarlaft =
                sarlaftAlertaService.evaluarOperacionPreview(
                        inputSarlaft
                );

        BigDecimal saldoActual =
                nvl(cuenta.getSaldoActualCuenta());

        BigDecimal valorEnCanje =
                nvl(cuenta.getValorEnCanje());

        BigDecimal saldoDisponible =
                saldoActual.subtract(valorEnCanje);

        BigDecimal valorGmf =
                BigDecimal.ZERO;

        boolean entrada =
                esEntrada(tipo.getAccionMovimiento());

        boolean salida =
                esSalida(tipo.getAccionMovimiento());

        if (!entrada && !salida) {
            throw new RuntimeException("La acción del tipo de movimiento no está configurada correctamente.");
        }

        BigDecimal saldoFinal;

        if (entrada) {
            saldoFinal =
                    saldoActual.add(valor);
        } else {
            saldoFinal =
                    saldoActual.subtract(valor).subtract(valorGmf);

            if (saldoDisponible.compareTo(valor.add(valorGmf)) < 0) {
                throw new RuntimeException("La cuenta no tiene saldo disponible suficiente para el movimiento.");
            }
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
        MovimientoCuentaPreviewDTO preview = preview(request);

        Integer idUsuario = usuarioSesionService.idUsuario();

        DepositosMovimientoDTO dto = new DepositosMovimientoDTO();
        dto.setIdCuentaAhorro(request.getIdCuentaAhorro());
        dto.setFechaMovimiento(request.getFechaMovimiento());
        dto.setTipoComprobante(request.getTipoComprobante());
        dto.setNumeroComprobante(request.getNumeroComprobante());
        dto.setTipoMovimiento(request.getTipoMovimiento());
        dto.setModulo("02");
        dto.setTarjeta("N");
        dto.setEstablecimiento(request.getDetalle());

        if (esEntrada(preview.getAccionMovimiento())) {
            dto.setValorCredito(preview.getValorMovimiento());
            dto.setValorDebito(BigDecimal.ZERO);

            if (repository.existeMovimientoIgual(
                    request,
                    BigDecimal.ZERO,
                    preview.getValorMovimiento()
            )) {
                throw new RuntimeException("El movimiento ya fue registrado anteriormente.");
            }

            depositosMovimientoService.registrarCredito(dto, idUsuario);

        } else {
            dto.setValorDebito(preview.getValorMovimiento());
            dto.setValorCredito(BigDecimal.ZERO);

            if (repository.existeMovimientoIgual(
                    request,
                    preview.getValorMovimiento(),
                    BigDecimal.ZERO
            )) {
                throw new RuntimeException("El movimiento ya fue registrado anteriormente.");
            }

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
                .tipoComprobante(request.getTipoComprobante())
                .numeroComprobante(request.getNumeroComprobante())
                .mensaje("Movimiento aplicado correctamente.")
                .build();
    }

    private void validarRequest(MovimientoCuentaRequestDTO request) {
        if (request == null) {
            throw new RuntimeException("No se recibió información del movimiento.");
        }

        if (request.getIdCuentaAhorro() == null) {
            throw new RuntimeException("La cuenta de ahorro es obligatoria.");
        }

        if (request.getFechaMovimiento() == null) {
            throw new RuntimeException("La fecha del movimiento es obligatoria.");
        }

        if (request.getTipoMovimiento() == null || request.getTipoMovimiento().isBlank()) {
            throw new RuntimeException("El tipo de movimiento es obligatorio.");
        }

        if (request.getValorMovimiento() == null) {
            throw new RuntimeException("El valor del movimiento es obligatorio.");
        }
    }

    private void validarCuentaOperativa(CuentaMovimientoDTO cuenta) {

        if (Boolean.FALSE.equals(cuenta.getEstadoOperativo())) {
            throw new RuntimeException(cuenta.getMensajeOperativo());
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

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}