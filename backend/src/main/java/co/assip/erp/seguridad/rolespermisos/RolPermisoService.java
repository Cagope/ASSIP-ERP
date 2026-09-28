package co.assip.erp.seguridad.rolespermisos;

import co.assip.erp.seguridad.permisos.Permiso;
import co.assip.erp.seguridad.permisos.PermisoRepository;
import co.assip.erp.seguridad.roles.Rol;
import co.assip.erp.seguridad.roles.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Servicio encargado de consultar y administrar las asignaciones
 * entre roles y permisos.
 *
 * seguridad.rol_permisos es la fuente oficial para determinar
 * qué permisos tiene asignados cada rol.
 */
@Service
@RequiredArgsConstructor
public class RolPermisoService {

    private final RolPermisoRepository rolPermisoRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    /**
     * Verifica si un rol tiene asignado un permiso activo
     * identificado por su código.
     *
     * @param idRol identificador del rol
     * @param codigoPermiso código funcional del permiso
     * @return true si existe la asignación
     */
    @Transactional(readOnly = true)
    public boolean rolTienePermiso(
            Integer idRol,
            String codigoPermiso
    ) {

        if (idRol == null
                || codigoPermiso == null
                || codigoPermiso.isBlank()) {
            return false;
        }

        return rolPermisoRepository
                .existsByRol_IdRolAndPermiso_Codigo(
                        idRol,
                        codigoPermiso.trim().toUpperCase()
                );
    }

    /**
     * Obtiene los permisos asignados a un rol.
     *
     * @param idRol identificador del rol
     * @return permisos asignados al rol
     */
    @Transactional(readOnly = true)
    public List<Permiso> listarPermisos(Integer idRol) {

        validarRol(idRol);

        return rolPermisoRepository
                .findByRol_IdRol(idRol)
                .stream()
                .map(RolPermiso::getPermiso)
                .toList();
    }

    /**
     * Reemplaza completamente la configuración de permisos de un rol.
     *
     * Solo modifica registros de seguridad.rol_permisos.
     * Los permisos del catálogo nunca son eliminados ni modificados.
     *
     * @param idRol identificador del rol
     * @param idPermisos identificadores de los permisos a asignar
     * @return permisos finalmente asignados
     */
    @Transactional
    public List<Permiso> asignarPermisos(
            Integer idRol,
            List<Integer> idPermisos
    ) {

        Rol rol = validarRol(idRol);

        List<Integer> ids = idPermisos == null
                ? Collections.emptyList()
                : idPermisos.stream()
                .filter(id -> id != null)
                .distinct()
                .toList();

        List<Permiso> permisos = permisoRepository.findAllById(ids);

        if (permisos.size() != ids.size()) {
            throw new IllegalArgumentException(
                    "Uno o más permisos indicados no existen."
            );
        }

        rolPermisoRepository.deleteByRol_IdRol(idRol);
        rolPermisoRepository.flush();

        for (Permiso permiso : permisos) {

            RolPermiso relacion = new RolPermiso();
            relacion.setRol(rol);
            relacion.setPermiso(permiso);

            rolPermisoRepository.save(relacion);
        }

        return permisos;
    }

    /**
     * Valida que el rol exista antes de consultar o modificar
     * sus asignaciones de permisos.
     */
    private Rol validarRol(Integer idRol) {

        if (idRol == null) {
            throw new IllegalArgumentException(
                    "El identificador del rol es obligatorio."
            );
        }

        return rolRepository.findById(idRol)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el rol con id " + idRol
                        )
                );
    }
}