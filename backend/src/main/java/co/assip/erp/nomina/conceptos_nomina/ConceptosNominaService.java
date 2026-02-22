package co.assip.erp.nomina.conceptos_nomina;

import co.assip.erp.nomina.conceptos_nomina.dto.ConceptoNominaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConceptosNominaService {

    private final ConceptosNominaRepository repository;

    // ============================================================
    // LISTAR
    // ============================================================
    public List<ConceptoNominaDTO> listar() {
        return repository.listar();
    }

    // ============================================================
    // OBTENER
    // ============================================================
    public ConceptoNominaDTO obtener(String codigoConcepto) {
        return repository.obtener(codigoConcepto)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No existe concepto nómina con código: " + codigoConcepto
                        ));
    }

    // ============================================================
    // CREAR
    // ============================================================
    public void crear(ConceptoNominaDTO dto, Integer idUsuario) {
        repository.crear(dto, idUsuario);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    public void actualizar(String codigoConcepto,
                           ConceptoNominaDTO dto,
                           Integer idUsuario) {

        repository.actualizar(codigoConcepto, dto, idUsuario);
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    public void eliminar(String codigoConcepto) {
        repository.eliminar(codigoConcepto);
    }
}
