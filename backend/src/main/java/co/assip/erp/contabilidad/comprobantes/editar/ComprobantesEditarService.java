package co.assip.erp.contabilidad.comprobantes.editar;

import co.assip.erp.contabilidad.plan_cuentas.PlanCuenta;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuentaRepository;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.auditoria.LogEventoService;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Modifica comprobantes manuales existentes con bloqueo, controles y validación integral. */
@Service
@RequiredArgsConstructor
public class ComprobantesEditarService {

    private static final String PERMISO_EDITAR = "CONTABILIDAD_EDIT";
    private static final Integer ID_ROL_CONTADOR = 15;

    private final ComprobantesEditarRepository repository;
    private final PlanCuentaRepository planCuentaRepository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolService usuarioRolService;
    private final AccessValidator accessValidator;
    private final JdbcTemplate jdbc;
    private final LogEventoService logEventoService;

    public record MovimientoEdicion(
            Integer idAuxiliarContable,
            Integer idCatalogoCuenta,
            Integer idDatosPersonal,
            String detalleMovimiento,
            BigDecimal valorDebito,
            BigDecimal valorCredito,
            BigDecimal valorBase,
            Integer idTiposControl,
            String numeroDocumento
    ) {}

    public record SolicitudEdicion(
            String conceptoComprobante,
            List<MovimientoEdicion> movimientos
    ) {}

    /** Valida y actualiza el comprobante completo sin cambiar su identidad ni fecha contable. */
    @Transactional
    public void editar(Integer agencia, String tipo, String numero,
                       SolicitudEdicion solicitud, HttpServletRequest request) {
        if (agencia == null || agencia <= 0 || tipo == null || tipo.isBlank()
                || numero == null || numero.isBlank() || solicitud == null
                || solicitud.conceptoComprobante() == null
                || solicitud.conceptoComprobante().isBlank()
                || solicitud.movimientos() == null || solicitud.movimientos().size() < 2) {
            throw new IllegalArgumentException("Debe indicar un comprobante y al menos dos movimientos válidos.");
        }
        tipo = tipo.trim().toUpperCase();
        numero = numero.trim();
        Integer usuarioId = usuarioSesionService.idUsuario();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException("El usuario autenticado no está activo."));
        accessValidator.validarAcceso(usuario, PERMISO_EDITAR);
        validarAgencia(usuarioId, agencia);

        var origen = repository.bloquearComprobante(agencia, tipo, numero)
                .orElseThrow(() -> new IllegalArgumentException("No existe el comprobante solicitado."));
        if (!"MANUAL".equals(origen.origenTipo())
                || !"CONTABILIDAD".equals(origen.moduloOrigen())
                || !"COMPROBANTE_MANUAL".equals(origen.procesoOrigen())
                || !"ACTIVO".equals(origen.estadoOrigen())) {
            throw new IllegalStateException("Solo pueden editarse comprobantes manuales activos.");
        }

        var existentes = repository.bloquearMovimientos(agencia, tipo, numero);
        if (existentes.size() < 2 || existentes.stream()
                .anyMatch(m -> !"A".equals(m.estadoMovimiento()))) {
            throw new IllegalStateException("El comprobante no contiene únicamente movimientos activos editables.");
        }
        LocalDate fecha = existentes.get(0).fechaAuxiliar();
        if (fecha == null || existentes.stream().anyMatch(m -> !fecha.equals(m.fechaAuxiliar()))) {
            throw new IllegalStateException("Los movimientos del comprobante tienen fechas contables inconsistentes.");
        }
        String estadoPeriodo = repository.bloquearEstadoPeriodo(agencia, fecha)
                .orElseThrow(() -> new IllegalStateException("El período contable no existe; no puede editarse."));
        if (!"ABIERTO".equals(estadoPeriodo)) {
            throw new IllegalStateException("El período contable está cerrado o no permite modificaciones.");
        }

        Map<Integer, ComprobantesEditarRepository.MovimientoExistente> porId = new HashMap<>();
        existentes.forEach(m -> porId.put(m.idAuxiliarContable(), m));
        Set<Integer> recibidos = new HashSet<>();
        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;
        int linea = 0;
        for (MovimientoEdicion mov : solicitud.movimientos()) {
            linea++;
            if (mov == null || mov.idAuxiliarContable() == null
                    || !porId.containsKey(mov.idAuxiliarContable())
                    || !recibidos.add(mov.idAuxiliarContable())) {
                throw new IllegalArgumentException("La línea " + linea + " no corresponde a un movimiento único del comprobante.");
            }
            validarMovimiento(agencia, mov, linea);
            debitos = debitos.add(valor(mov.valorDebito()));
            creditos = creditos.add(valor(mov.valorCredito()));
        }
        if (recibidos.size() != porId.size()) {
            throw new IllegalArgumentException("Deben enviarse todos los movimientos del comprobante.");
        }
        if (debitos.signum() <= 0 || debitos.compareTo(creditos) != 0) {
            throw new IllegalArgumentException("El comprobante debe conservar débitos y créditos positivos y equilibrados.");
        }

        if (repository.actualizarConcepto(agencia, tipo, numero,
                solicitud.conceptoComprobante().trim(), usuarioId) != 1) {
            throw new IllegalStateException("No se pudo actualizar el concepto general del comprobante.");
        }
        for (MovimientoEdicion mov : solicitud.movimientos()) {
            if (repository.actualizarMovimiento(agencia, tipo, numero,
                    mov.idAuxiliarContable(), mov.idCatalogoCuenta(), mov.idDatosPersonal(),
                    mov.detalleMovimiento().trim(), valor(mov.valorDebito()),
                    valor(mov.valorCredito()), valor(mov.valorBase()), usuarioId) != 1) {
                throw new IllegalStateException("No se pudo actualizar el movimiento " + mov.idAuxiliarContable());
            }
            List<Integer> controles = repository.listarIdsControles(mov.idAuxiliarContable());
            boolean requiereControl = Boolean.TRUE.equals(planCuentaRepository
                    .findById(mov.idCatalogoCuenta()).orElseThrow().getControlEntradaSalida());
            if (requiereControl) {
                if (controles.size() > 1) {
                    throw new IllegalStateException("El movimiento tiene varios controles; requiere revisión antes de editar.");
                }
                String documento = mov.numeroDocumento() == null ? null : mov.numeroDocumento().trim();
                int afectados = controles.isEmpty()
                        ? repository.insertarControl(mov.idAuxiliarContable(), mov.idTiposControl(), documento, usuarioId)
                        : repository.actualizarControl(controles.get(0), mov.idAuxiliarContable(),
                        mov.idTiposControl(), documento, usuarioId);
                if (afectados != 1) {
                    throw new IllegalStateException("No se pudo guardar el control del movimiento.");
                }
            } else {
                for (Integer idControl : controles) {
                    if (repository.eliminarControl(idControl, mov.idAuxiliarContable()) != 1) {
                        throw new IllegalStateException("No se pudo retirar el control anterior.");
                    }
                }
            }
        }
        logEventoService.registrarEvento(usuarioId, "CONTABILIDAD", "COMPROBANTE_EDITAR",
                "Agencia=" + agencia + "; tipo=" + tipo + "; numero=" + numero
                        + "; origen=MANUAL", request);
    }

    /** Valida cuentas, terceros, importes y controles de cada movimiento. */
    private void validarMovimiento(Integer agencia, MovimientoEdicion mov, int linea) {
        if (mov.idCatalogoCuenta() == null || mov.idDatosPersonal() == null
                || mov.idDatosPersonal() <= 0 || mov.detalleMovimiento() == null
                || mov.detalleMovimiento().isBlank()) {
            throw new IllegalArgumentException("La línea " + linea + " requiere cuenta, tercero y detalle.");
        }
        PlanCuenta cuenta = planCuentaRepository.findById(mov.idCatalogoCuenta())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta de la línea " + linea + " no existe."));
        if (!agencia.equals(cuenta.getIdAgencia()) || !Boolean.TRUE.equals(cuenta.getOperable())) {
            throw new IllegalArgumentException("La cuenta de la línea " + linea + " no es operable en esta agencia.");
        }
        BigDecimal d = valor(mov.valorDebito());
        BigDecimal c = valor(mov.valorCredito());
        if (d.signum() < 0 || c.signum() < 0 || valor(mov.valorBase()).signum() < 0
                || (d.signum() > 0) == (c.signum() > 0)) {
            throw new IllegalArgumentException("La línea " + linea + " debe tener solo débito o solo crédito positivo.");
        }
        boolean requiere = Boolean.TRUE.equals(cuenta.getControlEntradaSalida());
        if (requiere) {
            if (mov.idTiposControl() == null || mov.idTiposControl() <= 0) {
                throw new IllegalArgumentException("La línea " + linea + " requiere tipo de control.");
            }
            Boolean pideDocumento = jdbc.query("""
                    SELECT COALESCE(pide_documento, FALSE)
                    FROM contabilidad.tipos_control_ent_sal
                    WHERE id_tipos_control = ?
                    """, rs -> rs.next() ? rs.getBoolean(1) : null, mov.idTiposControl());
            if (pideDocumento == null) {
                throw new IllegalArgumentException("El tipo de control de la línea " + linea + " no existe.");
            }
            String doc = mov.numeroDocumento();
            if (doc != null && doc.trim().length() > 20) {
                throw new IllegalArgumentException("El documento de la línea " + linea + " supera 20 caracteres.");
            }
            if (pideDocumento && (doc == null || doc.isBlank())) {
                throw new IllegalArgumentException("La línea " + linea + " requiere número de documento.");
            }
        } else if (mov.idTiposControl() != null
                || (mov.numeroDocumento() != null && !mov.numeroDocumento().isBlank())) {
            throw new IllegalArgumentException("La línea " + linea + " no admite control documental.");
        }
    }

    /** Aplica el alcance de agencias existente para ADMIN, CONTADOR y demás usuarios. */
    private void validarAgencia(Integer idUsuario, Integer agencia) {
        if (usuarioSesionService.tieneAccesoTotal()) {
            return;
        }
        boolean contador = usuarioRolService.listarVigentes(idUsuario).stream()
                .anyMatch(a -> a.getRol() != null
                        && Boolean.TRUE.equals(a.getRol().getActivo())
                        && ID_ROL_CONTADOR.equals(a.getRol().getIdRol()));
        if (!contador) {
            usuarioSesionService.validarAgencia(agencia);
        }
    }

    /** Normaliza importes opcionales para las validaciones y actualizaciones. */
    private BigDecimal valor(BigDecimal importe) {
        return importe == null ? BigDecimal.ZERO : importe;
    }
}
