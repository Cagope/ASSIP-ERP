package co.assip.erp.cartera.analisis.rollforward;

import co.assip.erp.cartera.analisis.rollforward.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RollForwardService {

    private final RollForwardRepository repository;

    public RollForwardService(RollForwardRepository repository) {
        this.repository = repository;
    }

    public List<RollForwardCorteDTO> listarCortes() {
        return repository.listarCortes();
    }

    public List<RollForwardResumenDTO> listarHistoricoResumen() {
        return repository.listarHistoricoResumen();
    }

    public RollForwardResumenDTO obtenerResumen(LocalDate fechaCorte) {
        validarFecha(fechaCorte);
        return repository.buscarResumen(fechaCorte)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe información de Roll Forward para el corte " + fechaCorte));
    }

    public List<RollForwardLineaDTO> listarLineas(LocalDate fechaCorte) {
        validarCorte(fechaCorte);
        return repository.listarLineas(fechaCorte);
    }

    public List<String> listarTiposMovimiento(LocalDate fechaCorte) {
        validarCorte(fechaCorte);
        return repository.listarTiposMovimiento(fechaCorte);
    }

    public List<RollForwardDetalleDTO> listarDetalle(
            LocalDate fechaCorte, String tipoMovimiento, Long idLineaCredito) {

        validarCorte(fechaCorte);

        if (idLineaCredito != null && idLineaCredito <= 0) {
            throw new IllegalArgumentException("El id de línea debe ser mayor que cero.");
        }

        String movimiento = normalizar(tipoMovimiento);
        if (movimiento != null &&
                !repository.listarTiposMovimiento(fechaCorte).contains(movimiento)) {
            throw new IllegalArgumentException(
                    "El tipo de movimiento no existe para el corte seleccionado: " + movimiento);
        }

        return repository.listarDetalle(fechaCorte, movimiento, idLineaCredito);
    }

    private void validarCorte(LocalDate fechaCorte) {
        validarFecha(fechaCorte);
        if (repository.buscarResumen(fechaCorte).isEmpty()) {
            throw new IllegalArgumentException(
                    "No existe información de Roll Forward para el corte " + fechaCorte);
        }
    }

    private void validarFecha(LocalDate fechaCorte) {
        if (fechaCorte == null) {
            throw new IllegalArgumentException("La fecha de corte es obligatoria.");
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }
}
