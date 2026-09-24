package co.assip.erp.cartera.catalogos;

import co.assip.erp.cartera.catalogos.dto.CarteraCatalogoDTO;
import co.assip.erp.cartera.catalogos.dto.CentralRiesgoDTO;
import co.assip.erp.cartera.catalogos.dto.ClasificacionCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.DestinoEconomicoDTO;
import co.assip.erp.cartera.catalogos.dto.FondoGarantiaCatalogoDTO;
import co.assip.erp.cartera.catalogos.dto.FormaPagoDTO;
import co.assip.erp.cartera.catalogos.dto.GarantiaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.LineaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.ModalidadInteresDTO;
import co.assip.erp.cartera.catalogos.dto.SubgarantiaCreditoDTO;
import co.assip.erp.cartera.catalogos.dto.TipoCuotaDTO;

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

    @GetMapping("/lineas-credito/detalle")
    public List<LineaCreditoDTO> listarLineasCreditoDetalle() {
        return service.listarLineasCreditoDetalle(true);
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

    @GetMapping("/clasificaciones-credito/detalle")
    public List<ClasificacionCreditoDTO> listarClasificacionesCreditoDetalle() {
        return service.listarClasificacionesCreditoDetalle(true);
    }

    // =========================================================
    // DESTINOS ECONÓMICOS
    // =========================================================

    @GetMapping("/destinos-economicos")
    public List<DestinoEconomicoDTO> listarDestinosEconomicos() {
        return service.listarDestinosEconomicosDetalle(true);
    }

    // =========================================================
    // GARANTÍAS DE CRÉDITO
    // =========================================================

    @GetMapping("/garantias-credito")
    public List<CarteraCatalogoDTO> listarGarantiasCredito() {
        return service.listarGarantiasCredito();
    }

    @GetMapping("/garantias-credito/detalle")
    public List<GarantiaCreditoDTO> listarGarantiasCreditoDetalle() {
        return service.listarGarantiasCreditoDetalle(true);
    }

    // =========================================================
    // SUBGARANTÍAS DE CRÉDITO
    // =========================================================

    @GetMapping("/subgarantias")
    public List<SubgarantiaCreditoDTO> listarSubgarantiasCredito() {
        return service.listarSubgarantiasCreditoDetalle(true);
    }

    // =========================================================
    // FONDOS DE GARANTÍAS
    // =========================================================

    @GetMapping("/fondos-garantias")
    public List<FondoGarantiaCatalogoDTO> listarFondosGarantias() {
        return service.listarFondosGarantias();
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

    @GetMapping("/formas-pago/detalle")
    public List<FormaPagoDTO> listarFormasPagoDetalle() {
        return service.listarFormasPagoDetalle(true);
    }

    // =========================================================
    // MODALIDADES DE INTERÉS
    // =========================================================

    @GetMapping("/modalidades-interes")
    public List<ModalidadInteresDTO> listarModalidadesIntereses() {
        return service.listarModalidadesInteresesDetalle(true);
    }

    // =========================================================
    // TIPOS DE CUOTA
    // =========================================================

    @GetMapping("/tipos-cuota")
    public List<TipoCuotaDTO> listarTiposCuotas() {
        return service.listarTiposCuotasDetalle(true);
    }

    // =========================================================
    // CENTRALES DE RIESGO
    // =========================================================

    @GetMapping("/centrales-riesgo")
    public List<CentralRiesgoDTO> listarCentralesRiesgo() {
        return service.listarCentralesRiesgoDetalle(true);
    }
}