package co.assip.erp.cdat.informes.estadisticos_cdat;

import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatRequestDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatDetalleDTO;
import co.assip.erp.cdat.informes.estadisticos_cdat.dto.EstadisticosCdatDetalleRequestDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadisticosCdatService {

    private final EstadisticosCdatRepository repository;

    public EstadisticosCdatResponseDTO consultar(
            EstadisticosCdatRequestDTO request
    ) {

        LocalDate fechaCorte =
                request.getFechaCorteActual() == null
                        || request.getFechaCorteActual().isBlank()
                        ? LocalDate.now()
                        : LocalDate.parse(request.getFechaCorteActual());

        return EstadisticosCdatResponseDTO.builder()
                .resumen(repository.obtenerResumen(fechaCorte))
                .rangos(repository.obtenerRangos(fechaCorte))
                .amortizacion(repository.obtenerAmortizacion(fechaCorte))
                .plazos(repository.obtenerPlazos(fechaCorte))
                .plazosDetalle(repository.obtenerPlazosDetalle(fechaCorte))
                .tasas(repository.obtenerTasas(fechaCorte))
                .tasasDetalle(repository.obtenerTasasDetalle(fechaCorte))
                .build();
    }

    public List<EstadisticosCdatDetalleDTO> detalle(
            EstadisticosCdatDetalleRequestDTO request
    ) {

        LocalDate fechaCorte =
                request.getFechaCorte() == null
                        || request.getFechaCorte().isBlank()
                        ? LocalDate.now()
                        : LocalDate.parse(request.getFechaCorte());

        return repository.obtenerDetalle(
                fechaCorte,
                request.getTipoBloque(),
                request.getConcepto()
        );
    }
}