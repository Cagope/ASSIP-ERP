package co.assip.erp.seguridad.service.impl;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import co.assip.erp.seguridad.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void cambiarPassword(Long userId, String passwordActual, String passwordNueva) {
        Usuario user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        String hashActual = resolveHash(user);

        if (!passwordEncoder.matches(passwordActual, hashActual)) {
            throw new IllegalArgumentException("La contraseña actual no es válida.");
        }

        String nuevoHash = passwordEncoder.encode(passwordNueva);
        applyNewHash(user, nuevoHash);

        // Si tienes bandera de primer ingreso, se apaga aquí (opcional)
        try {
            user.getClass().getMethod("setDebeCambiarPassword", boolean.class).invoke(user, false);
        } catch (Exception ignored) { /* bandera no existe, continuar */ }

        usuarioRepository.save(user);
    }

    private String resolveHash(Usuario u) {
        try {
            return (String) u.getClass().getMethod("getPasswordHash").invoke(u);
        } catch (Exception e1) {
            try {
                return (String) u.getClass().getMethod("getPassword").invoke(u);
            } catch (Exception e2) {
                throw new IllegalStateException("No se encontró el hash de contraseña en Usuario.");
            }
        }
    }

    private void applyNewHash(Usuario u, String nuevoHash) {
        try {
            u.getClass().getMethod("setPasswordHash", String.class).invoke(u, nuevoHash);
        } catch (Exception e1) {
            try {
                u.getClass().getMethod("setPassword", String.class).invoke(u, nuevoHash);
            } catch (Exception e2) {
                throw new IllegalStateException("No se pudo asignar el nuevo hash en Usuario.");
            }
        }
    }
}
