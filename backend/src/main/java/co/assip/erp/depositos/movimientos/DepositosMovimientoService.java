package co.assip.erp.depositos.movimientos;

import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoResultadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepositosMovimientoService {

    private static final String TARJETA_NO = "N";
    private static final String ESTADO_ACTIVO = "A";

    private final DepositosMovimientoRepository repository;

    @Transactional
    public DepositosMovimientoResultadoDTO registrarDebito(
            DepositosMovimientoDTO dto,
            Integer idUsuario
    ) {
        validarUsuario(idUsuario);
        validarBase(dto);

        BigDecimal valor = nvl(dto.getValorDebito());

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El valor débito debe ser mayor a cero.");
        }

        dto.setValorDebito(valor);
        dto.setValorCredito(BigDecimal.ZERO);

        var cuenta = repository.obtenerCuentaConLock(dto.getIdCuentaAhorro());

        if (cuenta == null) {
            throw new RuntimeException("La cuenta de ahorro no existe.");
        }

        validarCuentaActiva(cuenta.estadoCuenta());

        BigDecimal saldoAnterior = nvl(cuenta.saldoActual());

        if (saldoAnterior.compareTo(valor) < 0) {
            throw new RuntimeException("La cuenta de ahorro no tiene saldo suficiente.");
        }

        BigDecimal saldoNuevo = saldoAnterior.subtract(valor);

        repository.actualizarSaldo(dto.getIdCuentaAhorro(), saldoNuevo, idUsuario);
        repository.insertarExtracto(dto, idUsuario);

        return new DepositosMovimientoResultadoDTO(
                dto.getIdCuentaAhorro(),
                saldoAnterior,
                saldoNuevo
        );
    }

    @Transactional
    public DepositosMovimientoResultadoDTO registrarCredito(
            DepositosMovimientoDTO dto,
            Integer idUsuario
    ) {
        validarUsuario(idUsuario);
        validarBase(dto);

        BigDecimal valor = nvl(dto.getValorCredito());

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El valor crédito debe ser mayor a cero.");
        }

        dto.setValorDebito(BigDecimal.ZERO);
        dto.setValorCredito(valor);

        var cuenta = repository.obtenerCuentaConLock(dto.getIdCuentaAhorro());

        if (cuenta == null) {
            throw new RuntimeException("La cuenta de ahorro no existe.");
        }

        validarCuentaActiva(cuenta.estadoCuenta());

        BigDecimal saldoAnterior = nvl(cuenta.saldoActual());
        BigDecimal saldoNuevo = saldoAnterior.add(valor);

        repository.actualizarSaldo(dto.getIdCuentaAhorro(), saldoNuevo, idUsuario);
        repository.insertarExtracto(dto, idUsuario);

        return new DepositosMovimientoResultadoDTO(
                dto.getIdCuentaAhorro(),
                saldoAnterior,
                saldoNuevo
        );
    }

    @Transactional
    public void registrarCreditosMasivos(
            List<DepositosMovimientoDTO> movimientos,
            Integer idUsuario
    ) {
        registrarMovimientosMasivos(movimientos, idUsuario, true);
    }

    @Transactional
    public void registrarCreditosMasivosSinValidarEstado(
            List<DepositosMovimientoDTO> movimientos,
            Integer idUsuario
    ) {
        registrarMovimientosMasivos(movimientos, idUsuario, false);
    }

    private void registrarMovimientosMasivos(
            List<DepositosMovimientoDTO> movimientos,
            Integer idUsuario,
            boolean validarEstado
    ) {
        validarUsuario(idUsuario);

        if (movimientos == null || movimientos.isEmpty()) {
            throw new RuntimeException("No se recibieron movimientos de depósitos.");
        }

        movimientos.forEach(this::validarBase);
        movimientos.forEach(this::normalizarMovimientoMasivo);

        List<Integer> idsCuentas = movimientos.stream()
                .map(DepositosMovimientoDTO::getIdCuentaAhorro)
                .distinct()
                .toList();

        var cuentas = repository.obtenerCuentasConLock(idsCuentas);

        if (cuentas.size() != idsCuentas.size()) {
            throw new RuntimeException("Una o varias cuentas de ahorro no existen.");
        }

        Map<Integer, BigDecimal> creditosPorCuenta = movimientos.stream()
                .collect(Collectors.groupingBy(
                        DepositosMovimientoDTO::getIdCuentaAhorro,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                m -> nvl(m.getValorCredito()),
                                BigDecimal::add
                        )
                ));

        Map<Integer, BigDecimal> debitosPorCuenta = movimientos.stream()
                .collect(Collectors.groupingBy(
                        DepositosMovimientoDTO::getIdCuentaAhorro,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                m -> nvl(m.getValorDebito()),
                                BigDecimal::add
                        )
                ));

        List<DepositosMovimientoRepository.CuentaSaldoDTO> saldosNuevos = cuentas.stream()
                .map(c -> {
                    if (validarEstado) {
                        validarCuentaActiva(c.estadoCuenta());
                    }

                    BigDecimal saldoAnterior = nvl(c.saldoActual());
                    BigDecimal credito = nvl(creditosPorCuenta.get(c.idCuentaAhorro()));
                    BigDecimal debito = nvl(debitosPorCuenta.get(c.idCuentaAhorro()));

                    BigDecimal saldoNuevo = saldoAnterior.add(credito).subtract(debito);

                    if (saldoNuevo.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException(
                                "La cuenta "
                                        + c.idCuentaAhorro()
                                        + " quedaría con saldo negativo."
                        );
                    }

                    return new DepositosMovimientoRepository.CuentaSaldoDTO(
                            c.idCuentaAhorro(),
                            saldoNuevo,
                            c.estadoCuenta()
                    );
                })
                .toList();

        repository.actualizarSaldoBatch(saldosNuevos, idUsuario);
        repository.insertarExtractosBatch(movimientos, idUsuario);
    }

    private void validarBase(DepositosMovimientoDTO dto) {
        if (dto == null) {
            throw new RuntimeException("No se recibió información del movimiento de depósitos.");
        }

        if (dto.getIdCuentaAhorro() == null) {
            throw new RuntimeException("La cuenta de ahorro es obligatoria.");
        }

        if (dto.getFechaMovimiento() == null) {
            throw new RuntimeException("La fecha del movimiento es obligatoria.");
        }

        if (isBlank(dto.getTipoMovimiento())) {
            throw new RuntimeException("El tipo de movimiento de depósitos es obligatorio.");
        }

        if (isBlank(dto.getModulo())) {
            throw new RuntimeException("El módulo del movimiento de depósitos es obligatorio.");
        }

        dto.setTipoMovimiento(trim(dto.getTipoMovimiento()));
        dto.setModulo(trim(dto.getModulo()));
        dto.setTarjeta(isBlank(dto.getTarjeta()) ? TARJETA_NO : trim(dto.getTarjeta()));

        if (dto.getTipoComprobante() != null) {
            dto.setTipoComprobante(trim(dto.getTipoComprobante()));
        }

        if (dto.getNumeroComprobante() != null) {
            dto.setNumeroComprobante(trim(dto.getNumeroComprobante()));
        }

        if (dto.getEstablecimiento() != null) {
            dto.setEstablecimiento(trim(dto.getEstablecimiento()));
        }
    }

    private void normalizarMovimientoMasivo(DepositosMovimientoDTO movimiento) {
        movimiento.setValorDebito(nvl(movimiento.getValorDebito()));
        movimiento.setValorCredito(nvl(movimiento.getValorCredito()));

        if (movimiento.getValorDebito().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("No se permiten débitos negativos.");
        }

        if (movimiento.getValorCredito().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("No se permiten créditos negativos.");
        }

        if (movimiento.getValorDebito().compareTo(BigDecimal.ZERO) <= 0
                && movimiento.getValorCredito().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Todos los movimientos masivos deben tener valor.");
        }

        if (movimiento.getValorDebito().compareTo(BigDecimal.ZERO) > 0
                && movimiento.getValorCredito().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Un movimiento no puede tener débito y crédito al mismo tiempo.");
        }
    }

    private void validarCuentaActiva(String estadoCuenta) {
        if (!ESTADO_ACTIVO.equalsIgnoreCase(String.valueOf(estadoCuenta).trim())) {
            throw new RuntimeException("La cuenta de ahorro no está activa.");
        }
    }

    private void validarUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            throw new RuntimeException("El usuario del movimiento es obligatorio.");
        }
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