package co.assip.erp.nomina.informes.novedades_empleado;

import co.assip.erp.nomina.informes.novedades_empleado.dto.NovedadesEmpleadoInformeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NovedadesEmpleadoInformeService {

    private final NovedadesEmpleadoInformeRepository repository;

    public List<NovedadesEmpleadoInformeDTO> consultar(
            String documento,
            Integer idEmpleado,
            Integer idPeriodo,
            String codigoConcepto,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        return repository.consultar(
                documento,
                idEmpleado,
                idPeriodo,
                codigoConcepto,
                fechaInicial,
                fechaFinal
        );
    }
}