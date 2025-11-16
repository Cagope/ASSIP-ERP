package co.assip.erp.sarlaft.service;

import co.assip.erp.sarlaft.domain.Alerta;
import co.assip.erp.sarlaft.repository.AlertaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertaService {

    private final AlertaRepository repository;

    public AlertaService(AlertaRepository repository) {
        this.repository = repository;
    }

    public List<Alerta> listar() {
        return repository.findAll();
    }

    public Optional<Alerta> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Alerta guardar(Alerta alerta) {
        return repository.save(alerta);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
