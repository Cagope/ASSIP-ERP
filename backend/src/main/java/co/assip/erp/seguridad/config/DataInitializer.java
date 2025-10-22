package co.assip.erp.seguridad.config;

import co.assip.erp.seguridad.domain.Rol;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.RolRepository;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializa datos base al arrancar el sistema:
 * - Crea el rol ADMIN si no existe.
 * - Asigna ese rol al usuario "admin" si ya está registrado.
 *
 * Diseñado para ejecutarse una sola vez y mantenerse idempotente.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {

        // 1️⃣ Crear rol ADMIN si no existe
        Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseGet(() -> rolRepository.save(
                        Rol.builder()
                                .nombre("ADMIN")
                                .descripcion("Administrador general del sistema")
                                .activo(true)
                                .build()
                ));

        // 2️⃣ Asignar el rol ADMIN al usuario "admin" si existe y no lo tiene
        usuarioRepository.findByUsername("admin").ifPresent(usuario -> {
            if (usuario.getRol() == null) {
                usuario.setRol(rolAdmin);
                usuarioRepository.save(usuario);
                System.out.println("✅ Rol ADMIN asignado al usuario 'admin'");
            }
        });
    }
}
