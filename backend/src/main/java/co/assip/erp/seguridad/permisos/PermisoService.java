package co.assip.erp.seguridad.permisos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio encargado de administrar el catálogo de permisos del ERP.
 *
 * Un permiso representa una capacidad funcional del sistema.
 * La asignación de permisos a roles se administra exclusivamente
 * mediante seguridad.rol_permisos.
 */
@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;

    /**
     * Retorna todos los permisos registrados.
     *
     * Incluye permisos activos e inactivos para permitir
     * su administración desde el módulo de seguridad.
     */
    @Transactional(readOnly = true)
    public List<Permiso> listar() {
        return permisoRepository.findAll();
    }

    /**
     * Busca un permiso por su identificador.
     *
     * @param id identificador del permiso
     * @return permiso encontrado
     * @throws IllegalArgumentException si el permiso no existe
     */
    @Transactional(readOnly = true)
    public Permiso buscarPorId(Integer id) {
        return permisoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el permiso con id " + id
                        )
                );
    }

    /**
     * Busca un permiso por su código funcional.
     *
     * @param codigo código único del permiso
     * @return permiso encontrado
     * @throws IllegalArgumentException si el permiso no existe
     */
    @Transactional(readOnly = true)
    public Permiso buscarPorCodigo(String codigo) {

        String codigoNormalizado = normalizarCodigo(codigo);

        return permisoRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el permiso " + codigoNormalizado
                        )
                );
    }

    /**
     * Crea un nuevo permiso en el catálogo.
     *
     * El permiso se crea independientemente de los roles.
     * Su asignación posterior se realiza mediante rol_permisos.
     *
     * @param permiso datos del nuevo permiso
     * @return permiso creado
     */
    @Transactional
    public Permiso crear(Permiso permiso) {

        if (permiso == null) {
            throw new IllegalArgumentException(
                    "Los datos del permiso son obligatorios."
            );
        }

        String codigo = normalizarCodigo(permiso.getCodigo());

        permisoRepository.findByCodigo(codigo)
                .ifPresent(existente -> {
                    throw new IllegalArgumentException(
                            "Ya existe un permiso con el código " + codigo
                    );
                });

        permiso.setIdPermiso(null);
        permiso.setCodigo(codigo);

        if (permiso.getActivo() == null) {
            permiso.setActivo(true);
        }

        return permisoRepository.save(permiso);
    }

    /**
     * Actualiza la información de un permiso existente.
     *
     * Esta operación no modifica los roles que tienen asignado
     * el permiso.
     *
     * @param id identificador del permiso
     * @param datos nuevos datos
     * @return permiso actualizado
     */
    @Transactional
    public Permiso actualizar(Integer id, Permiso datos) {

        Permiso actual = buscarPorId(id);

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del permiso son obligatorios."
            );
        }

        String codigo = normalizarCodigo(datos.getCodigo());

        permisoRepository.findByCodigo(codigo)
                .filter(otro -> !otro.getIdPermiso().equals(id))
                .ifPresent(otro -> {
                    throw new IllegalArgumentException(
                            "Ya existe un permiso con el código " + codigo
                    );
                });

        actual.setCodigo(codigo);
        actual.setDescripcion(datos.getDescripcion());

        return permisoRepository.save(actual);
    }

    /**
     * Activa o desactiva un permiso sin eliminarlo físicamente.
     *
     * Las relaciones existentes con roles se conservan para mantener
     * la configuración y trazabilidad de seguridad.
     *
     * @param id identificador del permiso
     * @param activo nuevo estado
     * @return permiso actualizado
     */
    @Transactional
    public Permiso cambiarEstado(Integer id, boolean activo) {

        Permiso permiso = buscarPorId(id);
        permiso.setActivo(activo);

        return permisoRepository.save(permiso);
    }

    /**
     * Normaliza y valida el código funcional de un permiso.
     */
    private String normalizarCodigo(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del permiso es obligatorio."
            );
        }

        return codigo.trim().toUpperCase();
    }
}