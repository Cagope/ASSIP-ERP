package co.assip.erp.contabilidad.comprobantes.exportacion;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/** Coordina la exportación de comprobantes respetando permisos y alcance de agencias. */
@Service
@RequiredArgsConstructor
public class ComprobantesExportacionService {
    private static final String PERMISO_EXPORTAR = "CONTABILIDAD_EXPORT";
    private static final Integer ID_ROL_CONTADOR = 15;
    private final ComprobantesExportacionRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolService usuarioRolService;
    private final AccessValidator accessValidator;
    private final JdbcTemplate jdbc;

    /** Agrupa las cabeceras, movimientos y controles para construir el Excel. */
    public record DatosExportacion(
            List<ComprobantesExportacionRepository.CabeceraExportacion> comprobantes,
            List<ComprobantesExportacionRepository.MovimientoExportacion> movimientos,
            List<ComprobantesExportacionRepository.ControlExportacion> controles
    ) {}

    /** Exporta todos los comprobantes que coinciden con los filtros y agencias autorizadas. */
    @Transactional(readOnly = true)
    public DatosExportacion exportarConsulta(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String concepto,
            Integer idDatosPersonal
    ) {
        Usuario usuario = validarUsuarioYPermiso();
        List<Integer> agencias = agenciasAutorizadas(usuario.getIdUsuario());
        if (idAgencia != null) {
            validarAgenciaEnAlcance(idAgencia, agencias);
            agencias = List.of(idAgencia);
        }
        List<ComprobantesExportacionRepository.CabeceraExportacion> comprobantes = repository.buscar(
                agencias, tipoComprobante, numeroComprobante,
                fechaDesde, fechaHasta, concepto, idDatosPersonal
        );
        return completarExportacion(comprobantes);
    }

    /** Exporta un comprobante individual, cualquiera sea su origen o estado. */
    @Transactional(readOnly = true)
    public DatosExportacion exportarComprobante(
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante
    ) {
        Usuario usuario = validarUsuarioYPermiso();
        if (idAgencia == null || idAgencia <= 0
                || tipoComprobante == null || tipoComprobante.isBlank()
                || numeroComprobante == null || numeroComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "Agencia, tipo y número del comprobante son obligatorios."
            );
        }
        validarAgenciaEnAlcance(idAgencia, agenciasAutorizadas(usuario.getIdUsuario()));
        List<ComprobantesExportacionRepository.CabeceraExportacion> comprobantes = repository.buscar(
                List.of(idAgencia), tipoComprobante.trim().toUpperCase(),
                numeroComprobante.trim(), null, null, null, null
        );
        if (comprobantes.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el comprobante solicitado.");
        }
        return completarExportacion(comprobantes);
    }

    /** Recupera en conjunto los movimientos y controles de las cabeceras seleccionadas. */
    private DatosExportacion completarExportacion(
            List<ComprobantesExportacionRepository.CabeceraExportacion> comprobantes
    ) {
        if (comprobantes.isEmpty()) {
            return new DatosExportacion(List.of(), List.of(), List.of());
        }
        List<ComprobantesExportacionRepository.MovimientoExportacion> movimientos =
                repository.consultarMovimientos(comprobantes);
        List<ComprobantesExportacionRepository.ControlExportacion> controles =
                repository.consultarControles(movimientos);
        return new DatosExportacion(comprobantes, movimientos, controles);
    }

    /** Comprueba que el usuario esté activo y tenga permiso de exportación. */
    private Usuario validarUsuarioYPermiso() {
        Integer idUsuario = usuarioSesionService.idUsuario();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException(
                        "El usuario autenticado no está activo."
                ));
        accessValidator.validarAcceso(usuario, PERMISO_EXPORTAR);
        return usuario;
    }

    /** Mantiene la misma política de acceso global y agencias de la consulta contable. */
    private List<Integer> agenciasAutorizadas(Integer idUsuario) {
        boolean esContador = usuarioRolService.listarVigentes(idUsuario).stream()
                .anyMatch(asignacion -> asignacion.getRol() != null
                        && Boolean.TRUE.equals(asignacion.getRol().getActivo())
                        && ID_ROL_CONTADOR.equals(asignacion.getRol().getIdRol()));
        if (usuarioSesionService.tieneAccesoTotal() || esContador) {
            return jdbc.queryForList(
                    "SELECT id_agencia FROM general.datos_agencias ORDER BY id_agencia",
                    Integer.class
            );
        }
        return usuarioSesionService.agencias();
    }

    /** Rechaza cualquier agencia que no pertenezca al alcance autorizado. */
    private void validarAgenciaEnAlcance(Integer idAgencia, List<Integer> agencias) {
        if (agencias == null || !agencias.contains(idAgencia)) {
            throw new SecurityException(
                    "No tiene autorización para exportar comprobantes de la agencia indicada."
            );
        }
    }
}
