package co.assip.erp.cartera.analisis.reciprocidadaportes;

import co.assip.erp.cartera.analisis.reciprocidadaportes.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReciprocidadAportesService {

    private final ReciprocidadAportesRepository repository;

    public List<LocalDate> listarCortes() {
        return repository.listarCortes();
    }

    public ReciprocidadAportesResumenDTO obtenerResumen(LocalDate fechaCorte) {
        validarFecha(fechaCorte);
        return repository.obtenerResumen(fechaCorte)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe información de reciprocidad de aportes para el corte " + fechaCorte));
    }

    public List<ReciprocidadAportesPersonaDTO> listarPersonas(LocalDate fechaCorte) {
        validarFecha(fechaCorte);
        return repository.listarPersonas(fechaCorte);
    }

    public List<ReciprocidadAportesDetalleDTO> listarDetalle(LocalDate fechaCorte) {
        validarFecha(fechaCorte);
        return repository.listarDetalle(fechaCorte);
    }

    public List<ReciprocidadAportesDetalleDTO> listarDetallePorPersona(LocalDate fechaCorte, Long idDatosPersonal) {
        validarFecha(fechaCorte);
        validarId(idDatosPersonal, "idDatosPersonal");
        return repository.listarDetallePorPersona(fechaCorte, idDatosPersonal);
    }

    private void validarFecha(LocalDate fechaCorte) {
        if (fechaCorte == null) {
            throw new IllegalArgumentException("La fecha de corte es obligatoria.");
        }
    }

    private void validarId(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(campo + " debe ser mayor que cero.");
        }
    }
}
