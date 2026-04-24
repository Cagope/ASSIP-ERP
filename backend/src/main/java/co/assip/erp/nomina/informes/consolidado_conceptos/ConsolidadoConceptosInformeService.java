package co.assip.erp.nomina.informes.consolidado_conceptos;

import co.assip.erp.nomina.informes.consolidado_conceptos.dto.ConsolidadoConceptosInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsolidadoConceptosInformeService {

    private final ConsolidadoConceptosInformeRepository repository;

    public List<ConsolidadoConceptosInformeDTO> consultar(
            String codigoConcepto,
            String tipoConcepto,
            Integer idPeriodo,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        return repository.consultar(
                codigoConcepto,
                tipoConcepto,
                idPeriodo,
                fechaInicial,
                fechaFinal
        );
    }
}