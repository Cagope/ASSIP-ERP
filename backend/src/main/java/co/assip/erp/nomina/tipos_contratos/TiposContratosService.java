package co.assip.erp.nomina.tipos_contratos;

import co.assip.erp.nomina.tipos_contratos.dto.TipoContratoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TiposContratosService {

    private final TiposContratosRepository repository;

    public List<TipoContratoDTO> listar() {
        return repository.listar();
    }
}