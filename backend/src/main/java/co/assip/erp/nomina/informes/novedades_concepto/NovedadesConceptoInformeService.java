package co.assip.erp.nomina.informes.novedades_concepto;

import co.assip.erp.nomina.informes.novedades_concepto.dto.NovedadesConceptoInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NovedadesConceptoInformeService {

    private final NovedadesConceptoInformeRepository repository;

    public List<NovedadesConceptoInformeDTO> consultar(
            String codigoConcepto,
            Integer idEmpleado,
            Integer idPeriodo,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        return repository.consultar(
                codigoConcepto,
                idEmpleado,
                idPeriodo,
                fechaInicial,
                fechaFinal
        );
    }
}