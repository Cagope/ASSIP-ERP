package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de usuarios del módulo de seguridad.
 * Encapsula la lógica de negocio para listar, buscar, guardar y eliminar usuarios.
 * - Cifra contraseñas con BCrypt.
 * - Valida duplicados.
 * - Facilita mantenimiento y reutilización.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Listar todos los usuarios (incluye su rol cargado por EAGER).
     */
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    /**
     * Buscar usuario por username.
     */
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Crear o actualizar un usuario.
     * Si es nuevo, cifra la contraseña antes de guardar.
     * Si ya existe, actualiza los demás campos.
     */
    public Usuario guardar(Usuario usuario) {
        Optional<Usuario> existente = usuarioRepository.findByUsername(usuario.getUsername());

        if (existente.isPresent()) {
            Usuario actual = existente.get();

            // 🔒 Cifrar nueva contraseña si se envía
            if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
                actual.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }

            // 🔄 Actualizar datos generales
            actual.setNombreCompleto(usuario.getNombreCompleto());
            actual.setEmail(usuario.getEmail());
            actual.setActivo(usuario.getActivo() != null ? usuario.getActivo() : true);
            actual.setRol(usuario.getRol());

            return usuarioRepository.save(actual);
        }

        // 🆕 Nuevo usuario → cifrar contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setActivo(usuario.getActivo() != null ? usuario.getActivo() : true);
        return usuarioRepository.save(usuario);
    }

    /**
     * Eliminar usuario por ID (eliminación física).
     * Puede cambiarse a lógica según políticas del sistema.
     */
    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);
    }
}
