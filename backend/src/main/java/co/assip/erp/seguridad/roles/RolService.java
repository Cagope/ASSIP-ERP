package co.assip.erp.seguridad.roles;

import co.assip.erp.seguridad.roles.dto.RolDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    /**
     * Retorna todos los roles registrados como DTO.
     *
     * Incluye roles activos e inactivos para permitir su administración
     * desde el módulo de seguridad sin exponer entidades Hibernate.
     */
    @Transactional(readOnly = true)
    public List<RolDTO> listarDto() {

        return rolRepository.findAll()
                .stream()
                .map(this::convertirDto)
                .toList();
    }

    /**
     * Busca un rol por su identificador y lo retorna como DTO.
     *
     * @param id identificador del rol
     * @return rol encontrado
     */
    @Transactional(readOnly = true)
    public RolDTO buscarDtoPorId(Integer id) {
        return convertirDto(
                buscarPorId(id)
        );
    }

    /**
     * Retorna todos los roles registrados como entidades.
     *
     * Este método se conserva para uso interno del backend.
     */
    @Transactional(readOnly = true)
    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    /**
     * Busca un rol por su identificador.
     *
     * Este método retorna la entidad porque puede ser utilizada
     * internamente por otros procesos de seguridad.
     *
     * @param id identificador del rol
     * @return rol encontrado
     * @throws IllegalArgumentException si el rol no existe
     */
    @Transactional(readOnly = true)
    public Rol buscarPorId(Integer id) {

        return rolRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el rol con id " + id
                        )
                );
    }

    /**
     * Busca un rol por su nombre.
     *
     * @param nombre nombre del rol
     * @return rol encontrado
     * @throws IllegalArgumentException si el rol no existe
     */
    @Transactional(readOnly = true)
    public Rol buscarPorNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del rol es obligatorio."
            );
        }

        return rolRepository.findByNombreRol(nombre.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el rol " + nombre.trim()
                        )
                );
    }

    /**
     * Crea un nuevo rol de seguridad.
     *
     * El identificador es generado por PostgreSQL y un rol nuevo
     * queda activo por defecto.
     *
     * @param rol datos del nuevo rol
     * @return rol creado
     */
    @Transactional
    public Rol crear(Rol rol) {

        if (rol == null) {
            throw new IllegalArgumentException(
                    "Los datos del rol son obligatorios."
            );
        }

        String nombre =
                normalizarNombre(
                        rol.getNombreRol()
                );

        rolRepository.findByNombreRol(nombre)
                .ifPresent(existente -> {
                    throw new IllegalArgumentException(
                            "Ya existe un rol con el nombre " + nombre
                    );
                });

        rol.setIdRol(null);
        rol.setNombreRol(nombre);

        if (rol.getActivo() == null) {
            rol.setActivo(true);
        }

        return rolRepository.save(rol);
    }

    /**
     * Actualiza la información administrativa de un rol existente.
     *
     * No modifica sus permisos ni los usuarios asociados; esas relaciones
     * se administran desde sus procesos correspondientes.
     *
     * @param id identificador del rol
     * @param datos nuevos datos del rol
     * @return rol actualizado
     */
    @Transactional
    public Rol actualizar(
            Integer id,
            Rol datos
    ) {

        Rol actual =
                buscarPorId(id);

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del rol son obligatorios."
            );
        }

        String nombre =
                normalizarNombre(
                        datos.getNombreRol()
                );

        rolRepository.findByNombreRol(nombre)
                .filter(otro ->
                        !otro.getIdRol().equals(id)
                )
                .ifPresent(otro -> {
                    throw new IllegalArgumentException(
                            "Ya existe un rol con el nombre " + nombre
                    );
                });

        actual.setNombreRol(nombre);
        actual.setDescripcion(
                datos.getDescripcion()
        );

        return rolRepository.save(actual);
    }

    /**
     * Cambia el estado activo/inactivo de un rol.
     *
     * Se conserva el registro para mantener las relaciones y la
     * trazabilidad histórica de seguridad.
     *
     * @param id identificador del rol
     * @param activo nuevo estado
     * @return rol actualizado
     */
    @Transactional
    public Rol cambiarEstado(
            Integer id,
            boolean activo
    ) {

        Rol rol =
                buscarPorId(id);

        rol.setActivo(activo);

        return rolRepository.save(rol);
    }

    /**
     * Convierte una entidad Rol a la representación utilizada
     * por la API REST.
     */
    public RolDTO convertirDto(
            Rol rol
    ) {

        return RolDTO.builder()
                .idRol(rol.getIdRol())
                .nombreRol(rol.getNombreRol())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .build();
    }

    /**
     * Normaliza y valida el nombre utilizado para identificar un rol.
     */
    private String normalizarNombre(
            String nombre
    ) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del rol es obligatorio."
            );
        }

        return nombre
                .trim()
                .toUpperCase();
    }
}