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
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cartera/catalogos")
@RequiredArgsConstructor
public class CarteraCatalogosController {

    private static final String PERMISO_VIEW = "CARTERA_VIEW";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final CarteraCatalogosService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LÍNEAS DE CRÉDITO
    // =========================================================

    @GetMapping("/lineas-credito")
    public List<CarteraCatalogoDTO> listarLineasCredito() {

        validarAcceso();

        return service.listarLineasCredito();
    }

    @GetMapping("/lineas-credito/detalle")
    public List<LineaCreditoDTO> listarLineasCreditoDetalle() {

        validarAcceso();

        return service.listarLineasCreditoDetalle(true);
    }

    // =========================================================
    // EDADES DE RIESGO
    // =========================================================

    @GetMapping("/edades-riesgo")
    public List<CarteraCatalogoDTO> listarEdadesRiesgo() {

        validarAcceso();

        return service.listarEdadesRiesgo();
    }

    // =========================================================
    // CLASIFICACIONES DE CRÉDITO
    // =========================================================

    @GetMapping("/clasificaciones-credito")
    public List<CarteraCatalogoDTO> listarClasificacionesCredito() {

        validarAcceso();

        return service.listarClasificacionesCredito();
    }

    @GetMapping("/clasificaciones-credito/detalle")
    public List<ClasificacionCreditoDTO> listarClasificacionesCreditoDetalle() {

        validarAcceso();

        return service.listarClasificacionesCreditoDetalle(true);
    }

    // =========================================================
    // DESTINOS ECONÓMICOS
    // =========================================================

    @GetMapping("/destinos-economicos")
    public List<DestinoEconomicoDTO> listarDestinosEconomicos() {

        validarAcceso();

        return service.listarDestinosEconomicosDetalle(true);
    }

    // =========================================================
    // GARANTÍAS DE CRÉDITO
    // =========================================================

    @GetMapping("/garantias-credito")
    public List<CarteraCatalogoDTO> listarGarantiasCredito() {

        validarAcceso();

        return service.listarGarantiasCredito();
    }

    @GetMapping("/garantias-credito/detalle")
    public List<GarantiaCreditoDTO> listarGarantiasCreditoDetalle() {

        validarAcceso();

        return service.listarGarantiasCreditoDetalle(true);
    }

    // =========================================================
    // SUBGARANTÍAS DE CRÉDITO
    // =========================================================

    @GetMapping("/subgarantias")
    public List<SubgarantiaCreditoDTO> listarSubgarantiasCredito() {

        validarAcceso();

        return service.listarSubgarantiasCreditoDetalle(true);
    }

    // =========================================================
    // FONDOS DE GARANTÍAS
    // =========================================================

    @GetMapping("/fondos-garantias")
    public List<FondoGarantiaCatalogoDTO> listarFondosGarantias() {

        validarAcceso();

        return service.listarFondosGarantias();
    }

    // =========================================================
    // ESTADOS DE CARTERA
    // =========================================================

    @GetMapping("/estados-cartera")
    public List<CarteraCatalogoDTO> listarEstadosCartera() {

        validarAcceso();

        return service.listarEstadosCartera();
    }

    // =========================================================
    // ESTADOS JURÍDICOS
    // =========================================================

    @GetMapping("/estados-juridicos")
    public List<CarteraCatalogoDTO> listarEstadosJuridicos() {

        validarAcceso();

        return service.listarEstadosJuridicos();
    }

    // =========================================================
    // FORMAS DE PAGO
    // =========================================================

    @GetMapping("/formas-pago")
    public List<CarteraCatalogoDTO> listarFormasPago() {

        validarAcceso();

        return service.listarFormasPago();
    }

    @GetMapping("/formas-pago/detalle")
    public List<FormaPagoDTO> listarFormasPagoDetalle() {

        validarAcceso();

        return service.listarFormasPagoDetalle(true);
    }

    // =========================================================
    // MODALIDADES DE INTERÉS
    // =========================================================

    @GetMapping("/modalidades-interes")
    public List<ModalidadInteresDTO> listarModalidadesIntereses() {

        validarAcceso();

        return service.listarModalidadesInteresesDetalle(true);
    }

    // =========================================================
    // TIPOS DE CUOTA
    // =========================================================

    @GetMapping("/tipos-cuota")
    public List<TipoCuotaDTO> listarTiposCuotas() {

        validarAcceso();

        return service.listarTiposCuotasDetalle(true);
    }

    // =========================================================
    // CENTRALES DE RIESGO
    // =========================================================

    @GetMapping("/centrales-riesgo")
    public List<CentralRiesgoDTO> listarCentralesRiesgo() {

        validarAcceso();

        return service.listarCentralesRiesgoDetalle(true);
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAcceso() {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                PERMISO_VIEW
        );
    }
}