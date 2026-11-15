package co.assip.erp.contabilidad.informes.balances.balance_comprobacion;

import co.assip.erp.contabilidad.informes.balances.balance_comprobacion.dto.BalanceComprobacionFilaDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comprobacion.dto.BalanceComprobacionFiltroDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comprobacion.dto.BalanceComprobacionRespuestaDTO;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Construye balances mensuales de solo lectura y restringe el acceso por permiso y agencia. */
@Service
@RequiredArgsConstructor
public class BalanceComprobacionService {
    private static final String PERMISO_VER = "CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW";
    private static final String PERMISO_CONSOLIDADO = "CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_CONSOLIDADO_VIEW";
    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final BalanceComprobacionRepository repository;
    private final UsuarioSesionService sesion;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    private record CuentaClave(String codigo, int nivel) {}
    private static final class Acumulado {
        String nombre;
        String naturaleza;
        boolean operable;
        BigDecimal debitoAnterior = CERO;
        BigDecimal creditoAnterior = CERO;
        BigDecimal debitoActual = CERO;
        BigDecimal creditoActual = CERO;
        Acumulado(String nombre, String naturaleza, boolean operable) {
            this.nombre = nombre;
            this.naturaleza = naturaleza;
            this.operable = operable;
        }
    }

    /** Consulta el balance, verificando permisos, agencias y cierres de ambos períodos. */
    @Transactional(readOnly = true)
    public BalanceComprobacionRespuestaDTO consultar(BalanceComprobacionFiltroDTO filtro) {
        validarFiltro(filtro);
        if (filtro.compararAnoAnterior()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este balance compara el saldo inicial y final del mismo mes; no admite comparación interanual.");
        }
        Usuario usuario = usuarioRepository.findById(sesion.idUsuario())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException("El usuario autenticado no está activo."));
        accessValidator.validarAcceso(usuario, PERMISO_VER);

        List<BalanceComprobacionRepository.Agencia> agencias = repository.listarAgencias();
        BalanceComprobacionRepository.Agencia seleccionada = agencias.stream()
                .filter(a -> Objects.equals(a.idAgencia(), filtro.idAgencia()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agencia inexistente."));
        boolean consolidado = "00".equals(seleccionada.codigo());
        List<BalanceComprobacionRepository.Agencia> operativas;
        if (consolidado) {
            accessValidator.validarAcceso(usuario, PERMISO_CONSOLIDADO);
            operativas = repository.listarAgenciasOperativas();
            if (operativas.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "No existen agencias operativas para consolidar.");
            }
            // El permiso de consolidado es explícito y habilita información de todas las agencias.
        } else {
            sesion.validarAgencia(seleccionada.idAgencia());
            operativas = List.of(seleccionada);
        }
        List<Integer> ids = operativas.stream().map(BalanceComprobacionRepository.Agencia::idAgencia).toList();
        validarCierres(ids, filtro.ano(), filtro.mes());
        if (filtro.compararAnoAnterior()) {
            validarCierres(ids, filtro.ano() - 1, filtro.mes());
        }
        Map<CuentaClave, Acumulado> actual = acumular(repository.consultarCuentas(
                ids, filtro.ano(), filtro.mes(), filtro.nivel(), filtro.soloBalance()));
        Map<CuentaClave, Acumulado> anterior = filtro.compararAnoAnterior()
                ? acumular(repository.consultarCuentas(ids, filtro.ano() - 1, filtro.mes(),
                filtro.nivel(), filtro.soloBalance()))
                : Map.of();

        Map<CuentaClave, Acumulado> union = new LinkedHashMap<>(actual);
        anterior.forEach((clave, valor) -> union.putIfAbsent(clave, valor));
        List<BalanceComprobacionFilaDTO> filas = new ArrayList<>();
        for (Map.Entry<CuentaClave, Acumulado> entrada : union.entrySet()) {
            CuentaClave clave = entrada.getKey();
            Acumulado base = entrada.getValue();
            Acumulado hoy = actual.get(clave);
            Acumulado previo = anterior.get(clave);
            if (hoy != null && previo != null && !hoy.naturaleza.equals(previo.naturaleza)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La naturaleza contable cambió entre períodos para la cuenta " + clave.codigo());
            }
            BigDecimal saldoInicial = hoy == null ? CERO : saldo(base.naturaleza, hoy.debitoAnterior, hoy.creditoAnterior);
            BigDecimal debitos = hoy == null ? CERO : hoy.debitoActual.subtract(hoy.debitoAnterior);
            BigDecimal creditos = hoy == null ? CERO : hoy.creditoActual.subtract(hoy.creditoAnterior);
            BigDecimal saldoFinal = hoy == null ? CERO : saldo(base.naturaleza, hoy.debitoActual, hoy.creditoActual);
            BigDecimal saldoComparado = previo == null ? CERO : saldo(base.naturaleza, previo.debitoActual, previo.creditoActual);
            BigDecimal variacion = filtro.compararAnoAnterior() ? saldoFinal.subtract(saldoComparado) : null;
            filas.add(new BalanceComprobacionFilaDTO(
                    clave.codigo(), base.nombre, clave.nivel(), base.naturaleza, base.operable,
                    saldoInicial, debitos, creditos, saldoFinal,
                    filtro.compararAnoAnterior() ? saldoComparado : null,
                    variacion, filtro.compararAnoAnterior() ? porcentaje(variacion, saldoComparado) : null));
        }
        filas.sort((a, b) -> a.codigoCuenta().compareTo(b.codigoCuenta()));

        // Solo las cuentas de nivel 1 forman los totales globales: los demás niveles ya están acumulados.
        List<BalanceComprobacionFilaDTO> raiz = filas.stream()
                .filter(f -> f.nivelCuenta() == 1).toList();
        BigDecimal saldoAnterior = sumar(raiz, 0);
        BigDecimal debitoMes = sumar(raiz, 1);
        BigDecimal creditoMes = sumar(raiz, 2);
        BigDecimal saldoFinal = sumar(raiz, 3);
        BigDecimal saldoComparado = filtro.compararAnoAnterior() ? sumar(raiz, 4) : null;
        BigDecimal variacion = filtro.compararAnoAnterior() ? saldoFinal.subtract(saldoComparado) : null;
        var totales = new BalanceComprobacionRespuestaDTO.TotalesDTO(
                saldoAnterior, debitoMes, creditoMes, saldoFinal, saldoComparado,
                variacion, filtro.compararAnoAnterior() ? porcentaje(variacion, saldoComparado) : null);
        // Resúmenes independientes del nivel visual y de la opción solo balance.
        Map<CuentaClave, Acumulado> cuentasResumen = acumular(
                repository.consultarCuentasResumen(ids, filtro.ano(), filtro.mes()));
        var indicadores = calcularIndicadores(cuentasResumen);
        var resumen = calcularResumen(cuentasResumen);
        var firmas = filtro.incluirFirmas() ? repository.consultarFirmas(seleccionada.idAgencia()) : null;
        return new BalanceComprobacionRespuestaDTO(
                seleccionada.idAgencia(), seleccionada.codigo(), seleccionada.nombre(),
                filtro.ano(), filtro.mes(), filtro.nivel(), filtro.soloBalance(),
                filtro.compararAnoAnterior(), filtro.compararAnoAnterior() ? filtro.ano() - 1 : null,
                filtro.compararAnoAnterior() ? filtro.mes() : null,
                filtro.incluirFirmas(), List.copyOf(filas), totales, firmas, indicadores, resumen);
    }

    /** Resultado paginado de movimientos, sin recalcular saldos del cierre. */
    public record MovimientosCuentaRespuesta(
            String codigoCuenta,
            int ano,
            int mes,
            int pagina,
            int tamanoPagina,
            boolean hayMas,
            List<BalanceComprobacionRepository.MovimientoCuenta> movimientos
    ) {}

    /**
     * Consulta movimientos de una cuenta operable de un período cerrado.
     * Reutiliza los permisos y el alcance de agencias del balance; no modifica saldos.
     */
    @Transactional(readOnly = true)
    public MovimientosCuentaRespuesta consultarMovimientosCuenta(
            Integer idAgencia,
            Integer ano,
            Integer mes,
            String codigoCuenta,
            Integer pagina,
            Integer tamanoPagina
    ) {
        if (idAgencia == null || ano == null || mes == null
                || ano < 1900 || ano > Year.now().getValue()
                || mes < 1 || mes > 12
                || codigoCuenta == null || codigoCuenta.isBlank()
                || pagina == null || pagina < 0
                || tamanoPagina == null || tamanoPagina < 1 || tamanoPagina > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Parámetros de movimientos inválidos.");
        }

        Usuario usuario = usuarioRepository.findById(sesion.idUsuario())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException("El usuario autenticado no está activo."));
        accessValidator.validarAcceso(usuario, PERMISO_VER);

        var seleccionada = repository.listarAgencias().stream()
                .filter(a -> Objects.equals(a.idAgencia(), idAgencia))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Agencia inexistente."));

        List<Integer> ids;
        if ("00".equals(seleccionada.codigo())) {
            accessValidator.validarAcceso(usuario, PERMISO_CONSOLIDADO);
            ids = repository.listarAgenciasOperativas().stream()
                    .map(BalanceComprobacionRepository.Agencia::idAgencia)
                    .toList();
            if (ids.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "No existen agencias operativas para consolidar.");
            }
        } else {
            sesion.validarAgencia(seleccionada.idAgencia());
            ids = List.of(seleccionada.idAgencia());
        }

        validarCierres(ids, ano, mes);
        String codigo = codigoCuenta.trim();
        boolean cuentaValida = repository.consultarCuentas(ids, ano, mes, 6, false)
                .stream()
                .anyMatch(c -> c.codigoCuenta().equals(codigo) && c.cuentaOperable());
        if (!cuentaValida) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cuenta no es operable o no pertenece al balance del período.");
        }

        long desplazamiento = (long) pagina * tamanoPagina;
        if (desplazamiento > Integer.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Página de movimientos fuera de rango.");
        }
        var registros = repository.consultarMovimientosCuenta(
                ids, codigo, ano, mes, tamanoPagina + 1, (int) desplazamiento);
        boolean hayMas = registros.size() > tamanoPagina;
        var movimientos = hayMas
                ? List.copyOf(registros.subList(0, tamanoPagina))
                : List.copyOf(registros);
        return new MovimientosCuentaRespuesta(
                codigo, ano, mes, pagina, tamanoPagina, hayMas, movimientos);
    }

    /**
     * Calcula los seis indicadores a partir de las cuentas raíz.
     * No suma cuentas hijas sobre sus agrupadoras.
     */
    private BalanceComprobacionRespuestaDTO.IndicadoresFinancierosDTO calcularIndicadores(
            Map<CuentaClave, Acumulado> cuentas) {

        var activo = indicadorClase(cuentas, "1");
        var pasivo = indicadorClase(cuentas, "2");
        var patrimonio = indicadorClase(cuentas, "3");
        var ingresos = indicadorClase(cuentas, "4");

        var costos = indicadorClase(cuentas, "5");
        var gastos = indicadorClase(cuentas, "6");

        var costosGastos = new BalanceComprobacionRespuestaDTO.IndicadorDTO(
                costos.valorMes().add(gastos.valorMes()),
                costos.valorAcumulado().add(gastos.valorAcumulado()));

        var gananciasPerdidas = new BalanceComprobacionRespuestaDTO.IndicadorDTO(
                ingresos.valorMes().subtract(costosGastos.valorMes()),
                ingresos.valorAcumulado().subtract(costosGastos.valorAcumulado()));

        return new BalanceComprobacionRespuestaDTO.IndicadoresFinancierosDTO(
                activo, pasivo, patrimonio, ingresos, costosGastos, gananciasPerdidas);
    }

    /**
     * Obtiene la variación del mes y el saldo acumulado de una clase.
     * Los importes respetan la naturaleza contable configurada.
     */
    private BalanceComprobacionRespuestaDTO.IndicadorDTO indicadorClase(
            Map<CuentaClave, Acumulado> cuentas, String codigo) {

        Acumulado cuenta = cuentas.get(new CuentaClave(codigo, 1));
        if (cuenta == null) {
            return new BalanceComprobacionRespuestaDTO.IndicadorDTO(CERO, CERO);
        }

        BigDecimal anterior = saldo(
                cuenta.naturaleza, cuenta.debitoAnterior, cuenta.creditoAnterior);
        BigDecimal actual = saldo(
                cuenta.naturaleza, cuenta.debitoActual, cuenta.creditoActual);

        return new BalanceComprobacionRespuestaDTO.IndicadorDTO(
                actual.subtract(anterior), actual);
    }

    /**
     * Totaliza movimientos contables exclusivamente desde cuentas operables.
     * Evita duplicar los importes registrados en cuentas agrupadoras.
     */
    private BalanceComprobacionRespuestaDTO.ResumenFinancieroDTO calcularResumen(
            Map<CuentaClave, Acumulado> cuentas) {

        BigDecimal debitosAnteriores = CERO;
        BigDecimal creditosAnteriores = CERO;
        BigDecimal debitosActuales = CERO;
        BigDecimal creditosActuales = CERO;

        for (Acumulado cuenta : cuentas.values()) {
            if (!cuenta.operable) continue;

            debitosAnteriores = debitosAnteriores.add(cuenta.debitoAnterior);
            creditosAnteriores = creditosAnteriores.add(cuenta.creditoAnterior);
            debitosActuales = debitosActuales.add(cuenta.debitoActual);
            creditosActuales = creditosActuales.add(cuenta.creditoActual);
        }

        BigDecimal debitosMes = debitosActuales.subtract(debitosAnteriores);
        BigDecimal creditosMes = creditosActuales.subtract(creditosAnteriores);

        var anterior = new BalanceComprobacionRespuestaDTO.MovimientoResumenDTO(
                debitosAnteriores, creditosAnteriores);

        var movimientos = new BalanceComprobacionRespuestaDTO.MovimientoResumenDTO(
                debitosMes, creditosMes);

        var finalMes = new BalanceComprobacionRespuestaDTO.MovimientoResumenDTO(
                debitosActuales, creditosActuales);

        return new BalanceComprobacionRespuestaDTO.ResumenFinancieroDTO(
                anterior,
                movimientos,
                finalMes,
                debitosMes.subtract(creditosMes),
                debitosActuales.subtract(creditosActuales));
    }

    /** Rechaza parámetros inválidos antes de consultar información financiera. */
    private void validarFiltro(BalanceComprobacionFiltroDTO filtro) {
        if (filtro == null || filtro.idAgencia() == null || filtro.ano() == null
                || filtro.mes() == null || filtro.nivel() == null
                || filtro.ano() < 1900 || filtro.ano() > Year.now().getValue()
                || filtro.mes() < 1 || filtro.mes() > 12
                || filtro.nivel() < 1 || filtro.nivel() > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtros del balance inválidos.");
        }
    }

    /** Exige cierres y saldos para todas las agencias incluidas en el informe. */
    private void validarCierres(List<Integer> ids, int ano, int mes) {
        var estados = repository.consultarEstados(ids, ano, mes);
        if (estados.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se encontraron todas las agencias del período.");
        }
        for (var estado : estados) {
            if (!"CERRADO".equalsIgnoreCase(estado.estado()) || estado.cantidadSaldos() == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El período " + mes + "/" + ano + " no está cerrado y consolidado para la agencia "
                                + estado.codigoAgencia() + " - " + estado.nombreAgencia());
            }
        }
    }

    /** Consolida por código contable, nunca por identificador de cuenta de cada agencia. */
    private Map<CuentaClave, Acumulado> acumular(List<BalanceComprobacionRepository.CuentaPeriodo> cuentas) {
        Map<CuentaClave, Acumulado> resultado = new LinkedHashMap<>();
        for (var cuenta : cuentas) {
            CuentaClave clave = new CuentaClave(cuenta.codigoCuenta(), cuenta.nivelCuenta());
            Acumulado agregado = resultado.computeIfAbsent(clave,
                    k -> new Acumulado(cuenta.nombreCuenta(), cuenta.naturalezaCuenta(), cuenta.cuentaOperable()));
            if (!agregado.naturaleza.equals(cuenta.naturalezaCuenta())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Naturaleza inconsistente entre agencias para la cuenta " + clave.codigo());
            }
            agregado.debitoAnterior = agregado.debitoAnterior.add(nuloACero(cuenta.debitoAnterior()));
            agregado.creditoAnterior = agregado.creditoAnterior.add(nuloACero(cuenta.creditoAnterior()));
            agregado.debitoActual = agregado.debitoActual.add(nuloACero(cuenta.debitoAcumulado()));
            agregado.creditoActual = agregado.creditoActual.add(nuloACero(cuenta.creditoAcumulado()));
        }
        return resultado;
    }

    /** Calcula el saldo de una cuenta respetando su naturaleza. */
    private BigDecimal saldo(String naturaleza, BigDecimal debito, BigDecimal credito) {
        return "D".equalsIgnoreCase(naturaleza) ? debito.subtract(credito) : credito.subtract(debito);
    }

    /** Expresa variación porcentual sobre el valor absoluto de la base histórica. */
    private BigDecimal porcentaje(BigDecimal variacion, BigDecimal base) {
        if (base == null || base.signum() == 0) return null;
        return variacion.multiply(BigDecimal.valueOf(100))
                .divide(base.abs(), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nuloACero(BigDecimal valor) { return valor == null ? CERO : valor; }

    /** Totaliza únicamente las raíces, evitando duplicar niveles jerárquicos. */
    private BigDecimal sumar(List<BalanceComprobacionFilaDTO> filas, int columna) {
        BigDecimal total = CERO;
        for (var fila : filas) {
            BigDecimal valor = switch (columna) {
                case 0 -> fila.saldoAnterior();
                case 1 -> fila.valorDebitoMes();
                case 2 -> fila.valorCreditoMes();
                case 3 -> fila.saldoFinal();
                case 4 -> fila.saldoMismoMesAnoAnterior();
                default -> CERO;
            };
            total = total.add(nuloACero(valor));
        }
        return total;
    }
}
