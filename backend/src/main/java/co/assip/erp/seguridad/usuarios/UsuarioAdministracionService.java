package co.assip.erp.seguridad.usuarios;

import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.autenticacion.ActivacionUsuarioPdfService;
import co.assip.erp.seguridad.autenticacion.UsuarioToken;
import co.assip.erp.seguridad.autenticacion.UsuarioTokenRepository;
import co.assip.erp.seguridad.autenticacion.UsuarioTokenService;
import co.assip.erp.shared.config.EmpresaConfigService;
import co.assip.erp.general.empresas.EmpresaDTO;
import co.assip.erp.general.agencia.AgenciaRepository;
import co.assip.erp.hojavida.datos_personales.DatosPersonalesRepository;
import co.assip.erp.seguridad.usuarios.dto.UsuarioAdministracionDTO;
import co.assip.erp.seguridad.usuarios.dto.UsuarioListDTO;
import co.assip.erp.seguridad.usuariosagencias.UsuarioAgenciaService;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Coordina la creación administrativa completa de usuarios.
 *
 * Una única transacción comprende:
 * - Creación de la cuenta.
 * - Asignación de roles.
 * - Asignación de agencias autorizadas.
 *
 * Coordina la emisión de códigos sin administrar contraseñas.
 */
@Service
@RequiredArgsConstructor
public class UsuarioAdministracionService {

    private final UsuarioService usuarioService;
    private final UsuarioRolService usuarioRolService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioTokenService usuarioTokenService;
    private final UsuarioTokenRepository usuarioTokenRepository;
    private final ActivacionUsuarioPdfService activacionUsuarioPdfService;
    private final EmpresaConfigService empresaConfigService;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final AgenciaRepository agenciaRepository;

    /**
     * Crea un usuario con sus roles y agencias.
     *
     * Si falla cualquier operación, se revierte
     * toda la transacción.
     */
    @Transactional
    public UsuarioListDTO crearCompleto(
            UsuarioAdministracionDTO datos
    ) {

        validarCreacion(datos);

        Integer idUsuarioActor =
                usuarioSesionService.idUsuario();

        // =====================================================
        // CREAR CUENTA ADMINISTRATIVA
        // =====================================================

        Usuario usuario = new Usuario();

        usuario.setIdDatosPersonal(
                datos.getIdDatosPersonal()
        );

        usuario.setUsername(
                datos.getUsername()
        );

        usuario.setNombreCompleto(
                datos.getNombreCompleto()
        );

        usuario.setEmail(
                datos.getEmail()
        );

        usuario.setActivo(true);

        usuario.setFechaInicioVigencia(
                datos.getFechaInicioVigencia()
        );

        usuario.setFechaFinVigencia(
                datos.getFechaFinVigencia()
        );

        usuario.setIdAgenciaPrincipal(
                datos.getIdAgenciaPrincipal()
        );

        Usuario guardado =
                usuarioService.guardar(usuario);

        Integer idUsuario =
                guardado.getIdUsuario();

        // =====================================================
        // ASIGNAR ROLES
        // =====================================================

        for (Integer idRol : datos.getRoles()) {

            usuarioRolService.asignar(
                    idUsuario,
                    idRol,
                    idUsuarioActor
            );
        }

        // =====================================================
        // ASIGNAR AGENCIAS
        // =====================================================

        usuarioAgenciaService.asignarAgencias(
                idUsuario,
                datos.getAgencias(),
                idUsuarioActor
        );

        // =====================================================
        // RESPUESTA ADMINISTRATIVA
        // =====================================================

        return usuarioService.buscarDtoPorId(
                idUsuario
        );
    }

    /**
     * Emite un código de seguridad y genera su PDF cifrado.
     *
     * Para usuarios sin contraseña genera un código de activación.
     * Para usuarios con contraseña genera un código de recuperación.
     *
     * No modifica los datos administrativos ni la contraseña.
     * Si falla el PDF, se revierte la emisión del código.
     */
    @Transactional
    public byte[] generarDocumentoGestionPassword(Integer idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }

        Usuario usuario = usuarioRepository.findByIdForUpdate(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalStateException(
                    "No se puede generar el documento para un usuario inactivo."
            );
        }

        boolean esActivacion =
                usuario.getPassword() == null
                        || usuario.getPassword().isBlank();
        if (usuario.getIdDatosPersonal() == null) {
            throw new IllegalStateException("El usuario no tiene una persona vinculada.");
        }

        var persona = datosPersonalesRepository.findById(usuario.getIdDatosPersonal())
                .orElseThrow(() -> new IllegalStateException("No se encontró la persona vinculada."));
        if (persona.getDocumento() == null || persona.getDocumento().isBlank()) {
            throw new IllegalStateException("La persona no tiene documento de identidad.");
        }

        var roles = usuarioRolService.listarVigentes(idUsuario).stream()
                .map(asignacion -> asignacion.getRol().getNombreRol())
                .distinct().toList();
        if (roles.isEmpty()) {
            throw new IllegalStateException("El usuario no tiene roles vigentes.");
        }

        var agencias = usuarioAgenciaService.obtenerIdAgencias(idUsuario).stream()
                .map(id -> agenciaRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException("Agencia no encontrada: " + id))
                        .getNombreAgencia())
                .distinct().toList();
        if (agencias.isEmpty()) {
            throw new IllegalStateException("El usuario no tiene agencias autorizadas.");
        }

        EmpresaDTO empresa = empresaConfigService.getEmpresa();
        if (empresa == null) {
            throw new IllegalStateException("No existe configuración institucional.");
        }

        Integer actor = usuarioSesionService.idUsuario();

        String tipoToken;
        String codigo;

        if (esActivacion) {
            tipoToken = UsuarioTokenService.TIPO_ACTIVACION;
            codigo = usuarioTokenService.generarTokenActivacion(
                    idUsuario,
                    actor
            );
        } else {
            tipoToken = UsuarioTokenService.TIPO_RECUPERACION;
            codigo = usuarioTokenService.generarTokenRecuperacion(
                    idUsuario,
                    actor
            );
        }

        UsuarioToken token = usuarioTokenRepository
                .findFirstByUsuario_IdUsuarioAndTipoTokenAndActivoTrueAndUtilizadoFalseOrderByFechaCreacionDesc(
                        idUsuario,
                        tipoToken
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el código recién generado."
                        )
                );

        String nit = empresa.getDocumentoEmpresa();
        if (empresa.getDigitoVerificacion() != null
                && !empresa.getDigitoVerificacion().isBlank()) {
            nit = nit + "-" + empresa.getDigitoVerificacion();
        }

        var datosEmpresa = new ActivacionUsuarioPdfService.DatosEmpresa(
                empresa.getRazonSocial(), empresa.getSiglaEmpresa(), nit, "");
        var datosUsuario = new ActivacionUsuarioPdfService.DatosUsuario(
                usuario.getUsername(), usuario.getNombreCompleto(), usuario.getEmail(),
                agencias, roles, usuario.getFechaInicioVigencia(), usuario.getFechaFinVigencia());

        return activacionUsuarioPdfService.generar(
                datosEmpresa, datosUsuario, codigo,
                persona.getDocumento(), token.getFechaExpiracion());
    }

    /**
     * Valida los datos necesarios antes de iniciar
     * la persistencia de la cuenta.
     */
    private void validarCreacion(
            UsuarioAdministracionDTO datos
    ) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios."
            );
        }

        if (datos.getIdUsuario() != null) {
            throw new IllegalArgumentException(
                    "La creación no debe recibir un identificador de usuario."
            );
        }

        if (datos.getIdDatosPersonal() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una persona."
            );
        }

        if (datos.getUsername() == null
                || datos.getUsername().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        if (datos.getRoles() == null
                || datos.getRoles().isEmpty()
                || datos.getRoles().stream().anyMatch(Objects::isNull)
                || datos.getRoles().stream().distinct().count()
                != datos.getRoles().size()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar roles válidos y sin duplicados."
            );
        }

        if (datos.getAgencias() == null
                || datos.getAgencias().isEmpty()
                || datos.getAgencias().stream().anyMatch(Objects::isNull)
                || datos.getAgencias().stream().distinct().count()
                != datos.getAgencias().size()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar agencias válidas y sin duplicados."
            );
        }

        if (datos.getIdAgenciaPrincipal() == null
                || !datos.getAgencias().contains(
                datos.getIdAgenciaPrincipal()
        )) {

            throw new IllegalArgumentException(
                    "La agencia principal debe pertenecer "
                            + "a las agencias autorizadas."
            );
        }

        if (datos.getFechaInicioVigencia() != null
                && datos.getFechaFinVigencia() != null
                && datos.getFechaFinVigencia().isBefore(
                datos.getFechaInicioVigencia()
        )) {

            throw new IllegalArgumentException(
                    "La fecha final de vigencia no puede "
                            + "ser anterior a la fecha inicial."
            );
        }
    }
}