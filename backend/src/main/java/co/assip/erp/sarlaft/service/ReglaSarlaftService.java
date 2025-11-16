package co.assip.erp.sarlaft.service;

import co.assip.erp.sarlaft.domain.ReglaSarlaft;
import co.assip.erp.sarlaft.repository.ReglaSarlaftRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReglaSarlaftService {

    private final ReglaSarlaftRepository repository;

    public ReglaSarlaftService(ReglaSarlaftRepository repository) {
        this.repository = repository;
    }

    public List<ReglaSarlaft> listar() {
        return repository.findAll();
    }

    public Optional<ReglaSarlaft> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public ReglaSarlaft guardar(ReglaSarlaft regla) {
        return repository.save(regla);
    }

    public ReglaSarlaft buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }
}
