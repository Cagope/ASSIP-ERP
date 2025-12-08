package co.assip.erp.depositos.formasagencias;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormasAgenciasService {

    private final FormasAgenciasRepository repository;

    public List<FormaAgenciaDTO> listarPorAgencia(Integer idAgencia) {
        return repository.listarPorAgencia(idAgencia);
    }
}
