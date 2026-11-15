package co.assip.erp.contabilidad.informes.balances.estado_cuentas;

import co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto.EstadoCuentasFiltroDTO;
import co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto.EstadoCuentasFilaDTO;
import co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto.EstadoCuentasRespuestaDTO;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Estado de cuentas a la fecha, sin ejecutar cierres ni modificar comprobantes. */
@Service
@RequiredArgsConstructor
public class EstadoCuentasService {
    private static final String PERMISO_VER = "CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW";
    private static final String PERMISO_CONSOLIDADO =
            "CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_CONSOLIDADO_VIEW";
    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final EstadoCuentasRepository repository;
    private final UsuarioSesionService sesion;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    private record Clave(String codigo, int nivel) {}

    private static final class Acumulado {
        String nombre;
        String naturaleza;
        boolean operable;
        BigDecimal anteriorDebito = CERO;
        BigDecimal anteriorCredito = CERO;
        BigDecimal actualDebito = CERO;
        BigDecimal actualCredito = CERO;

        Acumulado(String nombre, String naturaleza, boolean operable) {
            this.nombre = nombre;
            this.naturaleza = naturaleza;
            this.operable = operable;
        }
    }

    /** Valida la agencia y sus permisos antes de consultar datos financieros. */
    private List<Integer> agenciasAutorizadas(Integer idAgencia) {
        Usuario usuario = usuarioRepository.findById(sesion.idUsuario())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException("Usuario de sesión inactivo."));
        accessValidator.validarAcceso(usuario, PERMISO_VER);
        var agencia = repository.listarAgencias().stream()
                .filter(a -> Objects.equals(a.idAgencia(), idAgencia))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Agencia inexistente."));
        if ("00".equals(agencia.codigo())) {
            accessValidator.validarAcceso(usuario, PERMISO_CONSOLIDADO);
            List<Integer> ids = repository.listarAgenciasOperativas().stream()
                    .map(EstadoCuentasRepository.Agencia::idAgencia).toList();
            if (ids.isEmpty()) throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No hay agencias operativas para consolidar.");
            return ids;
        }
        sesion.validarAgencia(idAgencia);
        return List.of(idAgencia);
    }

    /** Exige el cierre del mes anterior para disponer de una apertura confiable. */
    private void validarApertura(List<Integer> agencias, LocalDate fecha) {
        LocalDate previo = fecha.withDayOfMonth(1).minusDays(1);
        var estados = repository.consultarEstados(agencias, previo.getYear(), previo.getMonthValue());
        if (estados.size() != agencias.size()) throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Faltan períodos de apertura para algunas agencias.");
        for (var estado : estados) {
            if (!"CERRADO".equalsIgnoreCase(estado.estado()) || estado.cantidadSaldos() == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El mes anterior no está cerrado para la agencia " + estado.codigoAgencia());
            }
        }
    }

    /** Consulta saldos de apertura y movimientos desde el día 1 hasta el corte. */
    @Transactional(readOnly = true)
    public EstadoCuentasRespuestaDTO consultar(EstadoCuentasFiltroDTO filtro) {
        if (filtro == null || filtro.idAgencia() == null || filtro.fechaCorte() == null
                || filtro.fechaCorte().getYear() < 1900
                || filtro.fechaCorte().isAfter(LocalDate.now())
                || filtro.nivel() == null || filtro.nivel() < 1 || filtro.nivel() > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtros inválidos.");
        }
        List<Integer> ids = agenciasAutorizadas(filtro.idAgencia());
        validarApertura(ids, filtro.fechaCorte());
        String prefijo = filtro.codigoCuentaPrincipal() == null
                ? "" : filtro.codigoCuentaPrincipal().trim();
        if (!prefijo.isEmpty() && !prefijo.matches("[0-9]{1,11}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de cuenta inválido.");
        }
        if (!prefijo.isEmpty() && repository.listarCuentasPrincipales(ids).stream()
                .noneMatch(c -> c.codigo().equals(prefijo))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cuenta principal no existe o no es agrupadora.");
        }
        Map<Clave, Acumulado> cuentas = new LinkedHashMap<>();
        for (var cuenta : repository.consultarCuentas(ids, filtro.fechaCorte(), filtro.nivel(), prefijo)) {
            Clave clave = new Clave(cuenta.codigoCuenta(), cuenta.nivelCuenta());
            Acumulado valor = cuentas.computeIfAbsent(clave, k -> new Acumulado(
                    cuenta.nombreCuenta(), cuenta.naturalezaCuenta(), cuenta.cuentaOperable()));
            if (!valor.naturaleza.equals(cuenta.naturalezaCuenta())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Naturaleza contable inconsistente en " + clave.codigo());
            }
            valor.anteriorDebito = valor.anteriorDebito.add(cuenta.debitoAnterior());
            valor.anteriorCredito = valor.anteriorCredito.add(cuenta.creditoAnterior());
            valor.actualDebito = valor.actualDebito.add(cuenta.debitoAcumulado());
            valor.actualCredito = valor.actualCredito.add(cuenta.creditoAcumulado());
        }
        List<EstadoCuentasFilaDTO> filas = new ArrayList<>();
        for (var entrada : cuentas.entrySet()) {
            var clave = entrada.getKey();
            var v = entrada.getValue();
            filas.add(new EstadoCuentasFilaDTO(clave.codigo(), v.nombre, clave.nivel(),
                    v.naturaleza, v.operable,
                    saldo(v.naturaleza, v.anteriorDebito, v.anteriorCredito),
                    v.actualDebito.subtract(v.anteriorDebito),
                    v.actualCredito.subtract(v.anteriorCredito),
                    saldo(v.naturaleza, v.actualDebito, v.actualCredito)));
        }
        filas.sort((x, y) -> x.codigoCuenta().compareTo(y.codigoCuenta()));
        // El total se obtiene del nivel más alto del bloque, nunca de todos los niveles.
        int nivelRaiz = filas.stream().mapToInt(EstadoCuentasFilaDTO::nivelCuenta)
                .min().orElse(1);
        BigDecimal saldoAnterior = CERO, debitos = CERO, creditos = CERO, saldoFinal = CERO;
        for (var fila : filas) {
            if (fila.nivelCuenta() != nivelRaiz) continue;
            saldoAnterior = saldoAnterior.add(fila.saldoAnterior());
            debitos = debitos.add(fila.valorDebitoMes());
            creditos = creditos.add(fila.valorCreditoMes());
            saldoFinal = saldoFinal.add(fila.saldoFinal());
        }
        var agencia = repository.listarAgencias().stream()
                .filter(a -> Objects.equals(a.idAgencia(), filtro.idAgencia()))
                .findFirst().orElseThrow();
        var firmasOriginal = filtro.incluirFirmas()
                ? repository.consultarFirmas(filtro.idAgencia()) : null;
        EstadoCuentasRespuestaDTO.FirmasDTO firmas = firmasOriginal == null ? null
                : new EstadoCuentasRespuestaDTO.FirmasDTO(
                        convertir(firmasOriginal.firmante1()),
                        convertir(firmasOriginal.firmante2()),
                        convertir(firmasOriginal.firmante3()));
        return new EstadoCuentasRespuestaDTO(agencia.idAgencia(), agencia.codigo(),
                agencia.nombre(), filtro.fechaCorte(), prefijo, filtro.nivel(),
                filtro.incluirFirmas(), List.copyOf(filas),
                new EstadoCuentasRespuestaDTO.TotalesDTO(
                        saldoAnterior, debitos, creditos, saldoFinal), firmas);
    }

    private EstadoCuentasRespuestaDTO.FirmanteDTO convertir(
            EstadoCuentasRepository.Firmante firmante) {
        return firmante == null ? null : new EstadoCuentasRespuestaDTO.FirmanteDTO(
                firmante.titulo(), firmante.nombre(), firmante.tarjetaProfesional());
    }

    private BigDecimal saldo(String naturaleza, BigDecimal debito, BigDecimal credito) {
        return "D".equalsIgnoreCase(naturaleza)
                ? debito.subtract(credito) : credito.subtract(debito);
    }

    public record MovimientosCuentaRespuesta(String codigoCuenta, int ano, int mes,
            int pagina, int tamanoPagina, boolean hayMas,
            List<EstadoCuentasRepository.MovimientoCuenta> movimientos) {}

    /** Consulta movimientos paginados dentro del mismo corte, con permisos. */
    @Transactional(readOnly = true)
    public MovimientosCuentaRespuesta consultarMovimientosCuenta(
            Integer idAgencia, LocalDate fechaCorte, String codigoCuenta,
            Integer pagina, Integer tamanoPagina) {
        if (fechaCorte == null || fechaCorte.isAfter(LocalDate.now())
                || codigoCuenta == null || codigoCuenta.isBlank()
                || pagina == null || pagina < 0 || tamanoPagina == null
                || tamanoPagina < 1 || tamanoPagina > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Parámetros de movimientos inválidos.");
        }
        List<Integer> ids = agenciasAutorizadas(idAgencia);
        validarApertura(ids, fechaCorte);
        boolean operable = repository.consultarCuentas(ids, fechaCorte, 6, codigoCuenta.trim())
                .stream().anyMatch(c -> c.codigoCuenta().equals(codigoCuenta.trim())
                        && c.cuentaOperable());
        // Las cuentas operables no figuran en la lista de cuentas principales.
        if (!operable) {
            operable = repository.consultarCuentas(ids, fechaCorte, 6, "")
                    .stream().anyMatch(c -> c.codigoCuenta().equals(codigoCuenta.trim())
                            && c.cuentaOperable());
        }
        if (!operable) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La cuenta no es operable.");
        long offset = (long) pagina * tamanoPagina;
        if (offset > Integer.MAX_VALUE) throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Página fuera de rango.");
        var registros = repository.consultarMovimientosCuenta(ids, codigoCuenta.trim(),
                fechaCorte, tamanoPagina + 1, (int) offset);
        boolean hayMas = registros.size() > tamanoPagina;
        return new MovimientosCuentaRespuesta(codigoCuenta.trim(), fechaCorte.getYear(),
                fechaCorte.getMonthValue(), pagina, tamanoPagina, hayMas,
                hayMas ? List.copyOf(registros.subList(0, tamanoPagina)) : List.copyOf(registros));
    }

    /** Lista cuentas agrupadoras autorizadas para el selector de prefijos. */
    @Transactional(readOnly = true)
    public List<EstadoCuentasRepository.CuentaPrincipal> listarCuentas(Integer idAgencia) {
        return repository.listarCuentasPrincipales(agenciasAutorizadas(idAgencia));
    }
}
