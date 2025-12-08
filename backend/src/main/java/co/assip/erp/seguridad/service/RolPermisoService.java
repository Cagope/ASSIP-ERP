package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RolPermisoService {

    private final PermisoRepository permisoRepository;

    /**
     * Verifica si un rol tiene un permiso por su código.
     */
    public boolean rolTienePermiso(Integer idRol, String codigoPermiso) {
        return permisoRepository.existsByIdRolAndCodigo(idRol, codigoPermiso);
    }
}
