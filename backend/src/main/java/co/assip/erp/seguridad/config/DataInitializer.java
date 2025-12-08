package co.assip.erp.seguridad.config;

import co.assip.erp.seguridad.domain.Rol;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.RolRepository;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {

        // 1️⃣ Crear rol ADMIN si no existe
        Rol rolAdmin = rolRepository.findByNombreRol("ADMIN")
                .orElseGet(() -> rolRepository.save(
                        Rol.builder()
                                .nombreRol("ADMIN")
                                .descripcion("Administrador general del sistema")
                                .activo(true)
                                .build()
                ));

        // 2️⃣ Asignar el rol ADMIN al usuario admin/admin1 si existe
        usuarioRepository.findByUsername("admin1").ifPresent(usuario -> {
            if (usuario.getIdRol() == null) {
                usuario.setIdRol(rolAdmin.getIdRol());
                usuarioRepository.save(usuario);
                System.out.println("✅ Rol ADMIN asignado al usuario 'admin1'");
            }
        });

        // También verificar "admin" por compatibilidad
        usuarioRepository.findByUsername("admin").ifPresent(usuario -> {
            if (usuario.getIdRol() == null) {
                usuario.setIdRol(rolAdmin.getIdRol());
                usuarioRepository.save(usuario);
                System.out.println("✅ Rol ADMIN asignado al usuario 'admin'");
            }
        });
    }
}
