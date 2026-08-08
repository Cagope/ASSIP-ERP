package co.assip.erp.cartera.catalogos;

import co.assip.erp.cartera.catalogos.dto.CarteraCatalogoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CarteraCatalogosService {

    private final CarteraCatalogosRepository repository;

    public CarteraCatalogosService(
            CarteraCatalogosRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // LÍNEAS DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarLineasCredito() {
        return repository.listarLineasCredito();
    }

    // =========================================================
    // EDADES DE RIESGO
    // =========================================================

    public List<CarteraCatalogoDTO> listarEdadesRiesgo() {
        return repository.listarEdadesRiesgo();
    }

    // =========================================================
    // CLASIFICACIONES DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarClasificacionesCredito() {
        return repository.listarClasificacionesCredito();
    }

    // =========================================================
    // GARANTÍAS DE CRÉDITO
    // =========================================================

    public List<CarteraCatalogoDTO> listarGarantiasCredito() {
        return repository.listarGarantiasCredito();
    }

    // =========================================================
    // ESTADOS DE CARTERA
    // =========================================================

    public List<CarteraCatalogoDTO> listarEstadosCartera() {
        return repository.listarEstadosCartera();
    }

    // =========================================================
    // ESTADOS JURÍDICOS
    // =========================================================

    public List<CarteraCatalogoDTO> listarEstadosJuridicos() {
        return repository.listarEstadosJuridicos();
    }

    // =========================================================
    // FORMAS DE PAGO
    // =========================================================

    public List<CarteraCatalogoDTO> listarFormasPago() {
        return repository.listarFormasPago();
    }

}