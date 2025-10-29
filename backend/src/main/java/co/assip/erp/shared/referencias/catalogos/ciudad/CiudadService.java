package co.assip.erp.shared.referencias.catalogos.ciudad;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CiudadService {

    private final CiudadRepository repository;

    public CiudadService(CiudadRepository repository) {
        this.repository = repository;
    }

    public List<Ciudad> listar() {
        return repository.findAll();
    }
}
