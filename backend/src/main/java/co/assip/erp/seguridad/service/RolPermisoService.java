package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Rol;
import co.assip.erp.seguridad.domain.RolPermiso;
import co.assip.erp.seguridad.domain.Permiso;
import co.assip.erp.seguridad.repository.RolPermisoRepository;
import co.assip.erp.seguridad.repository.RolRepository;
import co.assip.erp.seguridad.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolPermisoService {

    private final RolPermisoRepository rolPermisoRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    /**
     * Asigna una lista de permisos a un rol.
     */
    @Transactional
    public void asignarPermisos(Integer idRol, List<Integer> idPermisos) {
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id " + idRol));

        // Eliminar permisos existentes
        List<RolPermiso> existentes = rolPermisoRepository.findByRol_IdRol(idRol);
        rolPermisoRepository.deleteAll(existentes);

        // Crear nuevas relaciones
        for (Integer idPermiso : idPermisos) {
            Permiso permiso = permisoRepository.findById(idPermiso)
                    .orElseThrow(() -> new RuntimeException("Permiso no encontrado con id " + idPermiso));

            RolPermiso nuevo = new RolPermiso();
            nuevo.setRol(rol);
            nuevo.setPermiso(permiso);
            rolPermisoRepository.save(nuevo);
        }
    }

    /**
     * Lista los permisos asociados a un rol.
     */
    public List<RolPermiso> listarPorRol(Integer idRol) {
        return rolPermisoRepository.findByRol_IdRol(idRol);
    }

    /**
     * Verifica si un rol tiene un permiso por código.
     */
    public boolean rolTienePermiso(Integer idRol, String codigoPermiso) {
        return rolPermisoRepository.existsByRol_IdRolAndPermiso_Codigo(idRol, codigoPermiso);
    }
}
