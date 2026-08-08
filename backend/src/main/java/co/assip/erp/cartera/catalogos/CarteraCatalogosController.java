package co.assip.erp.cartera.catalogos;

import co.assip.erp.cartera.catalogos.dto.CarteraCatalogoDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cartera/catalogos")
public class CarteraCatalogosController {

    private final CarteraCatalogosService service;

    public CarteraCatalogosController(
            CarteraCatalogosService service
    ) {
        this.service = service;
    }

    // =========================================================
    // LÍNEAS DE CRÉDITO
    // =========================================================

    @GetMapping("/lineas-credito")
    public List<CarteraCatalogoDTO> listarLineasCredito() {
        return service.listarLineasCredito();
    }

    // =========================================================
    // EDADES DE RIESGO
    // =========================================================

    @GetMapping("/edades-riesgo")
    public List<CarteraCatalogoDTO> listarEdadesRiesgo() {
        return service.listarEdadesRiesgo();
    }

    // =========================================================
    // CLASIFICACIONES DE CRÉDITO
    // =========================================================

    @GetMapping("/clasificaciones-credito")
    public List<CarteraCatalogoDTO> listarClasificacionesCredito() {
        return service.listarClasificacionesCredito();
    }

    // =========================================================
    // GARANTÍAS DE CRÉDITO
    // =========================================================

    @GetMapping("/garantias-credito")
    public List<CarteraCatalogoDTO> listarGarantiasCredito() {
        return service.listarGarantiasCredito();
    }

    // =========================================================
    // ESTADOS DE CARTERA
    // =========================================================

    @GetMapping("/estados-cartera")
    public List<CarteraCatalogoDTO> listarEstadosCartera() {
        return service.listarEstadosCartera();
    }

    // =========================================================
    // ESTADOS JURÍDICOS
    // =========================================================

    @GetMapping("/estados-juridicos")
    public List<CarteraCatalogoDTO> listarEstadosJuridicos() {
        return service.listarEstadosJuridicos();
    }

    // =========================================================
    // FORMAS DE PAGO
    // =========================================================

    @GetMapping("/formas-pago")
    public List<CarteraCatalogoDTO> listarFormasPago() {
        return service.listarFormasPago();
    }

}