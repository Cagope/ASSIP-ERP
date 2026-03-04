package co.assip.erp.nomina.desprendible;

import co.assip.erp.nomina.desprendible.dto.DesprendibleConceptoDTO;
import co.assip.erp.nomina.desprendible.dto.DesprendibleEmpleadoDTO;
import co.assip.erp.nomina.desprendible.dto.DesprendibleTotalesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DesprendibleService {

    private final DesprendibleRepository repository;

    public List<DesprendibleRepository.EmpleadoPeriodoRow>
    listarEmpleadosPeriodo(Integer idPeriodo) {
        return repository.listarEmpleadosPeriodo(idPeriodo);
    }

    public DesprendibleEmpleadoDTO generar(Integer idPeriodo, Integer idContrato) {

        var periodo = repository.obtenerPeriodo(idPeriodo);
        var emp = repository.obtenerEmpleadoContrato(idContrato);

        List<DesprendibleRepository.DesprendibleRow> rows =
                repository.obtenerConceptosCerrados(idPeriodo, idContrato);

        List<DesprendibleConceptoDTO> devengados = new ArrayList<>();
        List<DesprendibleConceptoDTO> deducciones = new ArrayList<>();

        BigDecimal totalDev = BigDecimal.ZERO;
        BigDecimal totalDed = BigDecimal.ZERO;

        for (var r : rows) {
            DesprendibleConceptoDTO dto = new DesprendibleConceptoDTO(
                    r.codigoConcepto(),
                    r.nombreConcepto(),
                    r.cantidad(),
                    r.valor()
            );

            if ("DEVENGADO".equalsIgnoreCase(r.tipoConcepto())) {
                devengados.add(dto);
                totalDev = totalDev.add(r.valor());
            } else if ("DEDUCCION".equalsIgnoreCase(r.tipoConcepto())) {
                deducciones.add(dto);
                totalDed = totalDed.add(r.valor());
            }
        }

        DesprendibleTotalesDTO totales = new DesprendibleTotalesDTO(
                totalDev,
                totalDed,
                totalDev.subtract(totalDed)
        );

        return new DesprendibleEmpleadoDTO(
                periodo.idPeriodo(),
                periodo.fechaInicio(),
                periodo.fechaFin(),
                periodo.descripcion(),
                emp.idEmpleado(),
                emp.documento(),
                emp.nombreCompleto(),
                emp.idContrato(),
                emp.salarioBase(),
                emp.cargo(),
                devengados,
                deducciones,
                totales
        );
    }
}