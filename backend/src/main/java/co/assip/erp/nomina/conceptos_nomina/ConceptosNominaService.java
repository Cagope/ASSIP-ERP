package co.assip.erp.nomina.conceptos_nomina;

import co.assip.erp.nomina.conceptos_nomina.dto.ConceptoNominaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConceptosNominaService {

    private final ConceptosNominaRepository repository;

    public List<ConceptoNominaDTO> listar() {
        return repository.listar();
    }

    public ConceptoNominaDTO obtener(String codigo) {
        return repository.obtener(codigo)
                .orElseThrow(() -> new RuntimeException("No existe concepto nómina con código: " + codigo));
    }

    public void crear(ConceptoNominaDTO dto, Integer idUsuario) {
        repository.crear(dto, idUsuario);
    }

    public void actualizar(String codigo, ConceptoNominaDTO dto, Integer idUsuario) {
        repository.actualizar(codigo, dto, idUsuario);
    }

    public void eliminar(String codigo) {
        repository.eliminar(codigo);
    }
}
