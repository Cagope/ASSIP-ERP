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

    public List<Permiso> listar() {
        return permisoRepository.findAll();
    }

    public Optional<Permiso> buscarPorId(Integer id) {
        return permisoRepository.findById(id);
    }

    public List<Permiso> listarPorRol(Integer idRol) {
        return permisoRepository.findByRol_IdRol(idRol);
    }

    public Permiso guardar(Permiso permiso) {
        Optional<Permiso> existente = permisoRepository.findByCodigo(permiso.getCodigo());

        if (existente.isPresent()) {
            Permiso actual = existente.get();
            actual.setDescripcion(permiso.getDescripcion());
            actual.setActivo(permiso.getActivo() != null ? permiso.getActivo() : true);
            actual.setRol(permiso.getRol());
            return permisoRepository.save(actual);
        }

        permiso.setActivo(permiso.getActivo() != null ? permiso.getActivo() : true);
        return permisoRepository.save(permiso);
    }

    public void eliminar(Integer id) {
        permisoRepository.deleteById(id);
    }
}
