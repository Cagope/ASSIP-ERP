package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Rol;
import co.assip.erp.seguridad.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de Roles del sistema.
 * Permite listar, buscar, crear, actualizar y eliminar roles.
 */
@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    /**
     * Listar todos los roles existentes.
     */
    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    /**
     * Buscar un rol por ID.
     */
    public Optional<Rol> buscarPorId(Integer id) {
        return rolRepository.findById(id);
    }

    /**
     * Crear o actualizar un rol.
     */
    public Rol guardar(Rol rol) {
        Optional<Rol> existente = rolRepository.findByNombre(rol.getNombre());

        if (existente.isPresent()) {
            Rol actual = existente.get();
            actual.setDescripcion(rol.getDescripcion());
            actual.setActivo(rol.getActivo() != null ? rol.getActivo() : true);
            return rolRepository.save(actual);
        }

        rol.setActivo(rol.getActivo() != null ? rol.getActivo() : true);
        return rolRepository.save(rol);
    }

    /**
     * Eliminar rol por ID.
     */
    public void eliminar(Integer id) {
        rolRepository.deleteById(id);
    }
}
