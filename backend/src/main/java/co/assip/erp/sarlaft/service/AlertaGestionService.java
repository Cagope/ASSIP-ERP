package co.assip.erp.sarlaft.service;

import co.assip.erp.sarlaft.domain.AlertaGestion;
import co.assip.erp.sarlaft.repository.AlertaGestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertaGestionService {

    private final AlertaGestionRepository repository;

    public AlertaGestionService(AlertaGestionRepository repository) {
        this.repository = repository;
    }

    public List<AlertaGestion> listar() {
        return repository.findAll();
    }

    public Optional<AlertaGestion> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public AlertaGestion guardar(AlertaGestion gestion) {
        return repository.save(gestion);
    }
}
