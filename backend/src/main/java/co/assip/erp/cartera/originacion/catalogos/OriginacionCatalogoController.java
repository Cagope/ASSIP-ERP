package co.assip.erp.cartera.originacion.catalogos;

import co.assip.erp.cartera.originacion.catalogos.dto.CentralRiesgoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.ClasificacionCreditoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.DestinoEconomicoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.FormaPagoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.GarantiaCreditoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.LineaCreditoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.ModalidadInteresDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.SubgarantiaCreditoDTO;
import co.assip.erp.cartera.originacion.catalogos.dto.TipoCuotaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/catalogos")
public class OriginacionCatalogoController {

    private final OriginacionCatalogoRepository repository;

    public OriginacionCatalogoController(
            OriginacionCatalogoRepository repository
    ) {
        this.repository = repository;
    }


    // =========================================================
    // LÍNEAS DE CRÉDITO
    // =========================================================

    @GetMapping("/lineas-credito")
    public ResponseEntity<List<LineaCreditoDTO>> listarLineasCredito() {

        return ResponseEntity.ok(
                repository.listarLineasCredito()
        );
    }


    // =========================================================
    // CLASIFICACIONES DE CRÉDITO
    // =========================================================

    @GetMapping("/clasificaciones-credito")
    public ResponseEntity<List<ClasificacionCreditoDTO>> listarClasificacionesCredito() {

        return ResponseEntity.ok(
                repository.listarClasificacionesCredito()
        );
    }


    // =========================================================
    // DESTINOS ECONÓMICOS
    // =========================================================

    @GetMapping("/destinos-economicos")
    public ResponseEntity<List<DestinoEconomicoDTO>> listarDestinosEconomicos() {

        return ResponseEntity.ok(
                repository.listarDestinosEconomicos()
        );
    }


    // =========================================================
    // GARANTÍAS
    // =========================================================

    @GetMapping("/garantias")
    public ResponseEntity<List<GarantiaCreditoDTO>> listarGarantiasCreditos() {

        return ResponseEntity.ok(
                repository.listarGarantiasCreditos()
        );
    }


    // =========================================================
    // SUBGARANTÍAS
    // =========================================================

    @GetMapping("/subgarantias")
    public ResponseEntity<List<SubgarantiaCreditoDTO>> listarSubgarantiasCreditos() {

        return ResponseEntity.ok(
                repository.listarSubgarantiasCreditos()
        );
    }


    // =========================================================
    // FORMAS DE PAGO
    // =========================================================

    @GetMapping("/formas-pago")
    public ResponseEntity<List<FormaPagoDTO>> listarFormasPago() {

        return ResponseEntity.ok(
                repository.listarFormasPago()
        );
    }


    // =========================================================
    // MODALIDADES DE INTERÉS
    // =========================================================

    @GetMapping("/modalidades-interes")
    public ResponseEntity<List<ModalidadInteresDTO>> listarModalidadesIntereses() {

        return ResponseEntity.ok(
                repository.listarModalidadesIntereses()
        );
    }


    // =========================================================
    // TIPOS DE CUOTA
    // =========================================================

    @GetMapping("/tipos-cuota")
    public ResponseEntity<List<TipoCuotaDTO>> listarTiposCuotas() {

        return ResponseEntity.ok(
                repository.listarTiposCuotas()
        );
    }


    // =========================================================
    // CENTRALES DE RIESGO
    // =========================================================

    @GetMapping("/centrales-riesgo")
    public ResponseEntity<List<CentralRiesgoDTO>> listarCentralesRiesgo() {

        return ResponseEntity.ok(
                repository.listarCentralesRiesgo()
        );
    }
}