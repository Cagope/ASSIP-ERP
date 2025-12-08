package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Permiso;
import co.assip.erp.seguridad.repository.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;

    // ============================================================
    // LISTAR TODOS
    // ============================================================
    public List<Permiso> listar() {
        return permisoRepository.findAll();
    }

    // ============================================================
    // BUSCAR POR ID
    // ============================================================
    public Optional<Permiso> buscarPorId(Integer id) {
        return permisoRepository.findById(id);
    }

    // ============================================================
    // LISTAR POR ROL (usando idRol plano)
    // ============================================================
    public List<Permiso> listarPorRol(Integer idRol) {
        return permisoRepository.findByIdRol(idRol);
    }

    // ============================================================
    // GUARDAR / ACTUALIZAR
    // ============================================================
    public Permiso guardar(Permiso permiso) {

        Optional<Permiso> existente = permisoRepository.findByCodigo(permiso.getCodigo());

        if (existente.isPresent()) {
            Permiso actual = existente.get();
            actual.setDescripcion(permiso.getDescripcion());
            actual.setActivo(
                    permiso.getActivo() != null ? permiso.getActivo() : Boolean.TRUE
            );
            // ⬇️ ahora se usa idRol (campo plano)
            actual.setIdRol(permiso.getIdRol());
            return permisoRepository.save(actual);
        }

        // Nuevo permiso
        if (permiso.getActivo() == null) {
            permiso.setActivo(Boolean.TRUE);
        }
        return permisoRepository.save(permiso);
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    public void eliminar(Integer id) {
        permisoRepository.deleteById(id);
    }
}
