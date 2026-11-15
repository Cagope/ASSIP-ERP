package co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual;

import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualFilaDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualFiltroDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualRespuestaDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Compara exclusivamente los saldos de cierre de dos períodos contables seleccionados. */
@Service
@RequiredArgsConstructor
public class BalanceComparadoMensualService {
    private static final String PERMISO_VER = "CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW";
    private static final String PERMISO_CONSOLIDADO = "CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_CONSOLIDADO_VIEW";
    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final BalanceComparadoMensualRepository repository;
    private final UsuarioSesionService sesion;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    private record CuentaClave(String codigo, int nivel) {}

    private static final class Acumulado {
        final String nombre;
        final String naturaleza;
        final boolean operable;
        BigDecimal debito = CERO;
        BigDecimal credito = CERO;

        Acumulado(String nombre, String naturaleza, boolean operable) {
            this.nombre = nombre;
            this.naturaleza = naturaleza;
            this.operable = operable;
        }
    }

    /** Valida acceso, consulta los cierres y construye la comparación por cuenta. */
    @Transactional(readOnly = true)
    public BalanceComparadoMensualRespuestaDTO consultar(BalanceComparadoMensualFiltroDTO filtro) {
        validarFiltro(filtro);
        Usuario usuario = usuarioRepository.findById(sesion.idUsuario())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException("El usuario autenticado no está activo."));
        accessValidator.validarAcceso(usuario, PERMISO_VER);

        BalanceComparadoMensualRepository.Agencia seleccionada = repository.listarAgencias().stream()
                .filter(a -> Objects.equals(a.idAgencia(), filtro.idAgencia()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agencia inexistente."));
        List<BalanceComparadoMensualRepository.Agencia> operativas;
        if ("00".equals(seleccionada.codigo())) {
            accessValidator.validarAcceso(usuario, PERMISO_CONSOLIDADO);
            operativas = repository.listarAgenciasOperativas();
            if (operativas.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No existen agencias operativas para consolidar.");
            }
        } else {
            sesion.validarAgencia(seleccionada.idAgencia());
            operativas = List.of(seleccionada);
        }

        List<Integer> ids = operativas.stream()
                .map(BalanceComparadoMensualRepository.Agencia::idAgencia).toList();
        validarCierres(ids, filtro.anoPeriodo1(), filtro.mesPeriodo1());
        if (!Objects.equals(filtro.anoPeriodo1(), filtro.anoPeriodo2())
                || !Objects.equals(filtro.mesPeriodo1(), filtro.mesPeriodo2())) {
            validarCierres(ids, filtro.anoPeriodo2(), filtro.mesPeriodo2());
        }

        Map<CuentaClave, Acumulado> periodo1 = acumular(repository.consultarCuentas(
                ids, filtro.anoPeriodo1(), filtro.mesPeriodo1(), filtro.nivel(), filtro.soloBalance()));
        Map<CuentaClave, Acumulado> periodo2 = acumular(repository.consultarCuentas(
                ids, filtro.anoPeriodo2(), filtro.mesPeriodo2(), filtro.nivel(), filtro.soloBalance()));

        Map<CuentaClave, Acumulado> union = new LinkedHashMap<>(periodo1);
        periodo2.forEach(union::putIfAbsent);
        List<BalanceComparadoMensualFilaDTO> filas = new ArrayList<>();
        for (Map.Entry<CuentaClave, Acumulado> entrada : union.entrySet()) {
            CuentaClave clave = entrada.getKey();
            Acumulado base = entrada.getValue();
            Acumulado primero = periodo1.get(clave);
            Acumulado segundo = periodo2.get(clave);
            if (primero != null && segundo != null
                    && !primero.naturaleza.equalsIgnoreCase(segundo.naturaleza)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La naturaleza contable cambió entre períodos para la cuenta " + clave.codigo());
            }
            BigDecimal saldo1 = primero == null ? CERO : saldo(primero);
            BigDecimal saldo2 = segundo == null ? CERO : saldo(segundo);
            BigDecimal variacion = saldo2.subtract(saldo1);
            filas.add(new BalanceComparadoMensualFilaDTO(
                    clave.codigo(), base.nombre, clave.nivel(), base.naturaleza, base.operable,
                    saldo1, saldo2, variacion, porcentaje(variacion, saldo1)));
        }
        filas.sort((a, b) -> a.codigoCuenta().compareTo(b.codigoCuenta()));

        // Se suman únicamente las cuentas de nivel 1 para evitar duplicar los saldos de sus hijas.
        List<BalanceComparadoMensualFilaDTO> raices = filas.stream()
                .filter(f -> f.nivelCuenta() != null && f.nivelCuenta() == 1).toList();
        BigDecimal total1 = raices.stream().map(BalanceComparadoMensualFilaDTO::saldoPeriodo1)
                .reduce(CERO, BigDecimal::add);
        BigDecimal total2 = raices.stream().map(BalanceComparadoMensualFilaDTO::saldoPeriodo2)
                .reduce(CERO, BigDecimal::add);
        BigDecimal variacionTotal = total2.subtract(total1);
        var totales = new BalanceComparadoMensualRespuestaDTO.TotalesDTO(
                total1, total2, variacionTotal, porcentaje(variacionTotal, total1));
        var firmas = filtro.incluirFirmas() ? repository.consultarFirmas(seleccionada.idAgencia()) : null;
        return new BalanceComparadoMensualRespuestaDTO(
                seleccionada.idAgencia(), seleccionada.codigo(), seleccionada.nombre(),
                filtro.anoPeriodo1(), filtro.mesPeriodo1(),
                filtro.anoPeriodo2(), filtro.mesPeriodo2(),
                filtro.nivel(), filtro.soloBalance(), filtro.incluirFirmas(),
                List.copyOf(filas), totales, firmas);
    }

    /** Comprueba que ambos meses sean válidos y no correspondan a fechas futuras. */
    private void validarFiltro(BalanceComparadoMensualFiltroDTO filtro) {
        if (filtro == null || filtro.idAgencia() == null || filtro.nivel() == null
                || filtro.nivel() < 1 || filtro.nivel() > 6
                || !periodoValido(filtro.anoPeriodo1(), filtro.mesPeriodo1())
                || !periodoValido(filtro.anoPeriodo2(), filtro.mesPeriodo2())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtros del balance inválidos.");
        }
    }

    private boolean periodoValido(Integer ano, Integer mes) {
        return ano != null && mes != null && ano >= 1900 && ano <= Year.now().getValue()
                && mes >= 1 && mes <= 12 && !YearMonth.of(ano, mes).isAfter(YearMonth.now());
    }

    /** Exige que cada agencia tenga el período cerrado y con saldos consolidados. */
    private void validarCierres(List<Integer> ids, int ano, int mes) {
        var estados = repository.consultarEstados(ids, ano, mes);
        if (estados.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se encontraron todas las agencias del período.");
        }
        for (var estado : estados) {
            if (!"CERRADO".equalsIgnoreCase(estado.estado()) || estado.cantidadSaldos() == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El período " + mes + "/" + ano + " no está cerrado y consolidado para la agencia "
                                + estado.codigoAgencia() + " - " + estado.nombreAgencia());
            }
        }
    }

    /** Consolida por código y nivel, sin sumar nuevamente cuentas hijas. */
    private Map<CuentaClave, Acumulado> acumular(
            List<BalanceComparadoMensualRepository.CuentaPeriodo> cuentas) {
        Map<CuentaClave, Acumulado> resultado = new LinkedHashMap<>();
        for (var cuenta : cuentas) {
            CuentaClave clave = new CuentaClave(cuenta.codigoCuenta(), cuenta.nivelCuenta());
            Acumulado agregado = resultado.computeIfAbsent(clave,
                    k -> new Acumulado(cuenta.nombreCuenta(), cuenta.naturalezaCuenta(), cuenta.cuentaOperable()));
            if (!agregado.naturaleza.equalsIgnoreCase(cuenta.naturalezaCuenta())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Naturaleza inconsistente entre agencias para la cuenta " + clave.codigo());
            }
            agregado.debito = agregado.debito.add(nuloACero(cuenta.debitoAcumulado()));
            agregado.credito = agregado.credito.add(nuloACero(cuenta.creditoAcumulado()));
        }
        return resultado;
    }

    /** Convierte los acumulados del cierre en saldo según la naturaleza de la cuenta. */
    private BigDecimal saldo(Acumulado cuenta) {
        return "D".equalsIgnoreCase(cuenta.naturaleza)
                ? cuenta.debito.subtract(cuenta.credito)
                : cuenta.credito.subtract(cuenta.debito);
    }

    /** Devuelve nulo cuando la base es cero para evitar un porcentaje indefinido. */
    private BigDecimal porcentaje(BigDecimal variacion, BigDecimal base) {
        if (base.signum() == 0) return null;
        return variacion.multiply(BigDecimal.valueOf(100))
                .divide(base.abs(), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nuloACero(BigDecimal valor) {
        return valor == null ? CERO : valor;
    }
}
