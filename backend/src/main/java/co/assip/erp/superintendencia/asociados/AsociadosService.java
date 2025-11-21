package co.assip.erp.superintendencia.asociados;

import co.assip.erp.superintendencia.asociados.dto.AsociadoDTO;
import co.assip.erp.superintendencia.asociados.repository.AsociadosRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsociadosService {

    private final AsociadosRepository repo;

    public AsociadosService(AsociadosRepository repo) {
        this.repo = repo;
    }

    public List<AsociadoDTO> consultar(String fechaCorte) {
        return repo.consultar(fechaCorte);
    }
}
