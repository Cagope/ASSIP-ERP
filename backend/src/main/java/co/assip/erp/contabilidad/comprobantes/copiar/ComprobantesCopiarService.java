package co.assip.erp.contabilidad.comprobantes.copiar;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.comprobantes.consulta.ComprobantesConsultaRepository;
import co.assip.erp.contabilidad.comprobantes.consulta.ComprobantesConsultaService;
import co.assip.erp.contabilidad.comprobantes.copiar.dto.ComprobanteCopiarRequestDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Copia comprobantes sin alterar el origen ni permitir editar sus movimientos. */
@Service
@RequiredArgsConstructor
public class ComprobantesCopiarService {
    private static final String PERMISO_COPIAR = "CONTABILIDAD_COPY";
    private static final Integer ID_ROL_CONTADOR = 15;
    private final ComprobantesConsultaService consultaService;
    private final ComprobantesConsultaRepository consultaRepository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolService usuarioRolService;
    private final AccessValidator accessValidator;
    private final PlanCuentaRepository planCuentaRepository;
    private final PeriodosContablesService periodosContablesService;
    private final ConsecutivosComprobantesService consecutivosService;
    private final ContabilidadRegistroService registroService;
    private final LogEventoService logEventoService;

    /** Valida origen, destino y período, y registra la copia en una transacción. */
    @Transactional
    public String copiar(ComprobanteCopiarRequestDTO solicitud, HttpServletRequest request) {
        validarSolicitud(solicitud);
        Integer idUsuario = usuarioSesionService.idUsuario();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException("El usuario autenticado no está activo."));
        accessValidator.validarAcceso(usuario, PERMISO_COPIAR);
        validarAgenciaDestino(idUsuario, solicitud.idAgenciaDestino());
        // La consulta existente autoriza el origen, incluso si su período está cerrado.
        var origen = consultaService.consultar(
                solicitud.idAgenciaOrigen(),
                solicitud.tipoComprobanteOrigen(),
                solicitud.numeroComprobanteOrigen()
        );
        List<ContabilidadRegistroService.MovimientoManual> movimientos =
                prepararMovimientos(origen.movimientos(), solicitud);
        // El período de destino se crea si falta y queda bloqueado hasta el commit.
        periodosContablesService.validarPeriodoAbierto(
                solicitud.idAgenciaDestino(), solicitud.fechaContableDestino()
        );
        String tipoDestino = solicitud.tipoComprobanteDestino().trim().toUpperCase();
        String numero = consecutivosService.generarNumeroDefinitivo(
                tipoDestino, solicitud.idAgenciaDestino(), idUsuario
        );
        OrigenComprobanteDTO nuevoOrigen = new OrigenComprobanteDTO();
        nuevoOrigen.setOrigenTipo("MANUAL");
        nuevoOrigen.setModuloOrigen("CONTABILIDAD");
        nuevoOrigen.setProcesoOrigen("COMPROBANTE_MANUAL");
        registroService.registrarComprobanteManualNuevo(
                solicitud.idAgenciaDestino(), tipoDestino, numero,
                solicitud.conceptoComprobanteDestino().trim(),
                nuevoOrigen, movimientos, idUsuario
        );
        logEventoService.registrarEvento(
                idUsuario, "CONTABILIDAD", "COMPROBANTE_COPIAR",
                "origen=" + solicitud.idAgenciaOrigen() + "/"
                        + solicitud.tipoComprobanteOrigen() + "/"
                        + solicitud.numeroComprobanteOrigen()
                        + "; destino=" + solicitud.idAgenciaDestino() + "/"
                        + tipoDestino + "/" + numero,
                request
        );
        return numero;
    }

    /** Exige identificación completa del origen y datos válidos del destino. */
    private void validarSolicitud(ComprobanteCopiarRequestDTO s) {
        if (s == null || s.idAgenciaOrigen() == null || s.idAgenciaOrigen() <= 0
                || s.tipoComprobanteOrigen() == null || s.tipoComprobanteOrigen().isBlank()
                || s.numeroComprobanteOrigen() == null || s.numeroComprobanteOrigen().isBlank()) {
            throw new IllegalArgumentException("Debe identificar el comprobante de origen.");
        }
        if (s.idAgenciaDestino() == null || s.idAgenciaDestino() <= 0
                || s.tipoComprobanteDestino() == null
                || s.tipoComprobanteDestino().trim().length() != 2
                || s.fechaContableDestino() == null
                || s.conceptoComprobanteDestino() == null
                || s.conceptoComprobanteDestino().isBlank()) {
            throw new IllegalArgumentException("Debe indicar agencia, tipo, fecha y concepto del destino.");
        }
    }

    /** Aplica las mismas reglas de alcance de agencia que la creación manual. */
    private void validarAgenciaDestino(Integer idUsuario, Integer idAgencia) {
        if (usuarioSesionService.tieneAccesoTotal()) return;
        boolean contador = usuarioRolService.listarVigentes(idUsuario).stream()
                .anyMatch(a -> a.getRol() != null
                        && Boolean.TRUE.equals(a.getRol().getActivo())
                        && ID_ROL_CONTADOR.equals(a.getRol().getIdRol()));
        if (!contador) usuarioSesionService.validarAgencia(idAgencia);
    }

    /** Reconstruye las líneas sin reutilizar IDs de auxiliares ni cuentas de otra agencia. */
    private List<ContabilidadRegistroService.MovimientoManual> prepararMovimientos(
            List<ComprobantesConsultaRepository.MovimientoComprobante> origen,
            ComprobanteCopiarRequestDTO solicitud
    ) {
        if (origen == null || origen.isEmpty()) {
            throw new IllegalArgumentException("El comprobante origen no contiene movimientos.");
        }
        List<ContabilidadRegistroService.MovimientoManual> resultado = new ArrayList<>();
        Set<Integer> auxiliares = new HashSet<>();
        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;
        int linea = 0;
        for (var m : origen) {
            linea++;
            if (!auxiliares.add(m.idAuxiliarContable())) {
                throw new IllegalArgumentException("El movimiento " + linea
                        + " tiene varios controles asociados; no puede copiarse sin alterar su estructura.");
            }
            if (m.codigoCuenta() == null || m.codigoCuenta().isBlank()) {
                throw new IllegalArgumentException("La cuenta de la línea " + linea + " no existe.");
            }
            PlanCuenta cuenta = planCuentaRepository.findByIdAgenciaAndCodigoCuenta(
                    solicitud.idAgenciaDestino(), m.codigoCuenta()
            ).orElseThrow(() -> new IllegalArgumentException(
                    "La cuenta " + m.codigoCuenta() + " no existe en la agencia destino."
            ));
            if (!Boolean.TRUE.equals(cuenta.getOperable())) {
                throw new IllegalArgumentException("La cuenta " + m.codigoCuenta()
                        + " no es operable en la agencia destino.");
            }
            if (m.idDatosPersonal() == null || m.idDatosPersonal() <= 0
                    || m.detalleMovimiento() == null || m.detalleMovimiento().isBlank()) {
                throw new IllegalArgumentException("La línea " + linea
                        + " no tiene tercero o detalle válido.");
            }
            BigDecimal debito = cero(m.valorDebito());
            BigDecimal credito = cero(m.valorCredito());
            BigDecimal base = cero(m.valorBase());
            if (debito.signum() < 0 || credito.signum() < 0 || base.signum() < 0
                    || (debito.signum() > 0 && credito.signum() > 0)
                    || (debito.signum() == 0 && credito.signum() == 0)) {
                throw new IllegalArgumentException("La línea " + linea + " tiene importes inválidos.");
            }
            boolean requiereControl = Boolean.TRUE.equals(cuenta.getControlEntradaSalida());
            if (requiereControl != (m.idTiposControl() != null)) {
                throw new IllegalArgumentException("El control documental de la cuenta "
                        + m.codigoCuenta() + " no es compatible con el destino.");
            }
            if (!requiereControl && m.numeroDocumento() != null && !m.numeroDocumento().isBlank()) {
                throw new IllegalArgumentException("La línea " + linea + " tiene un documento no permitido.");
            }
            if (requiereControl && (m.idTiposControl() <= 0
                    || (m.numeroDocumento() != null && m.numeroDocumento().length() > 20))) {
                throw new IllegalArgumentException("Control documental inválido en la línea " + linea + ".");
            }
            MovimientoContableDTO nuevo = new MovimientoContableDTO();
            nuevo.setIdAgencia(solicitud.idAgenciaDestino());
            nuevo.setIdCatalogoCuenta(cuenta.getId());
            nuevo.setIdDatosPersonal(m.idDatosPersonal());
            nuevo.setFechaAuxiliar(solicitud.fechaContableDestino());
            nuevo.setDetalleMovimiento(m.detalleMovimiento());
            nuevo.setValorDebito(debito);
            nuevo.setValorCredito(credito);
            nuevo.setValorBase(base);
            nuevo.setDocumentoSoporte(m.numeroDocumento());
            resultado.add(new ContabilidadRegistroService.MovimientoManual(nuevo, m.idTiposControl()));
            debitos = debitos.add(debito);
            creditos = creditos.add(credito);
        }
        if (resultado.size() < 2 || debitos.signum() <= 0 || creditos.signum() <= 0
                || debitos.compareTo(creditos) != 0) {
            throw new IllegalArgumentException("El comprobante de origen no está cuadrado o tiene menos de dos movimientos.");
        }
        return resultado;
    }

    /** Normaliza importes opcionales sin redondear valores. */
    private BigDecimal cero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
