package co.assip.erp.shared.referencias.catalogos.departamento;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartamentoService {
    private final DepartamentoRepository repository;

    public DepartamentoService(DepartamentoRepository repository) {
        this.repository = repository;
    }

    public List<Departamento> listar() {
        return repository.findAll();
    }
}
