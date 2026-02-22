package co.assip.erp.nomina.busqueda;

import co.assip.erp.nomina.busqueda.dto.EmpleadoBusquedaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NominaBusquedaService {

    private final NominaBusquedaRepository repo;

    public List<EmpleadoBusquedaDTO> buscarEmpleados(String q, Integer limit) {

        int lim = (limit == null || limit <= 0) ? 20 : limit;

        return repo.buscarEmpleados(q, lim);
    }

    public EmpleadoBusquedaDTO obtener(Integer idEmpleado) {
        return repo.obtenerPorId(idEmpleado);
    }
}
