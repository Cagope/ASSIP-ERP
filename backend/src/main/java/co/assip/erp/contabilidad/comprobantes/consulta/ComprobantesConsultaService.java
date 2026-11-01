
package co.assip.erp.contabilidad.comprobantes.consulta;

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

/**
 * Consulta comprobantes contables de todos los módulos y estados.
 * Valida permisos y alcance de agencias; no modifica registros contables.
 */
@Service
@RequiredArgsConstructor
public class ComprobantesConsultaService {
    private static final String PERMISO_CONSULTAR = "CONTABILIDAD_VIEW";
    private static final Integer ID_ROL_CONTADOR = 15;

    private final ComprobantesConsultaRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolService usuarioRolService;
    private final AccessValidator accessValidator;
    private final JdbcTemplate jdbc;

    public record ComprobanteCompleto(
            ComprobantesConsultaRepository.CabeceraComprobante cabecera,
            List<ComprobantesConsultaRepository.MovimientoComprobante> movimientos
    ) {}

    /**
     * Busca comprobantes de todos los orígenes y estados,
     * limitando los resultados a las agencias autorizadas.
     */
    @Transactional(readOnly = true)
    public List<ComprobantesConsultaRepository.ResumenComprobante> buscar(
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
        return repository.buscar(
                agencias,
                tipoComprobante,
                numeroComprobante,
                fechaDesde,
                fechaHasta,
                concepto,
                idDatosPersonal
        );
    }

    /**
     * Recupera cabecera y movimientos de cualquier comprobante registrado.
     * Permite comprobantes manuales, automáticos, activos y reversados.
     * No habilita su edición.
     */
    @Transactional(readOnly = true)
    public ComprobanteCompleto consultar(
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
        validarAgenciaEnAlcance(
                idAgencia,
                agenciasAutorizadas(usuario.getIdUsuario())
        );
        String tipoNormalizado = tipoComprobante.trim().toUpperCase();
        String numeroNormalizado = numeroComprobante.trim();

        var cabecera = repository.consultarCabecera(
                idAgencia,
                tipoNormalizado,
                numeroNormalizado
        ).orElseThrow(() -> new IllegalArgumentException(
                "No se encontró el comprobante solicitado."
        ));

        var movimientos = repository.consultarMovimientos(
                idAgencia,
                tipoNormalizado,
                numeroNormalizado
        );
        return new ComprobanteCompleto(cabecera, movimientos);
    }

    /** Exige usuario activo y permiso de consulta de comprobantes. */
    private Usuario validarUsuarioYPermiso() {
        Integer idUsuario = usuarioSesionService.idUsuario();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException(
                        "El usuario autenticado no está activo."
                ));
        accessValidator.validarAcceso(usuario, PERMISO_CONSULTAR);
        return usuario;
    }

    /** Resuelve acceso global de ADMIN/CONTADOR o agencias de sesión. */
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

    /** Impide consultar agencias fuera del alcance autorizado. */
    private void validarAgenciaEnAlcance(
            Integer idAgencia,
            List<Integer> agencias
    ) {
        if (agencias == null || !agencias.contains(idAgencia)) {
            throw new SecurityException(
                    "No tiene autorización para consultar la agencia indicada."
            );
        }
    }
}
