package co.assip.erp.shared.referencias.catalogos.nivel_ingreso;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NivelIngresoService {

    private final NivelIngresoRepository repository;

    public NivelIngresoService(NivelIngresoRepository repository) {
        this.repository = repository;
    }

    public List<NivelIngreso> listar() {
        return repository.findAll();
    }
}
