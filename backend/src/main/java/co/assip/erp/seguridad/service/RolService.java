package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Rol;
import co.assip.erp.seguridad.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    public Optional<Rol> buscarPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    public Rol guardar(Rol rol) {
        return rolRepository.save(rol);
    }

    public void eliminar(Integer idRol) {
        rolRepository.deleteById(idRol);
    }
}
