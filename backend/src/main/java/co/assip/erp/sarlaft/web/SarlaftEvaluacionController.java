package co.assip.erp.sarlaft.web;

import co.assip.erp.sarlaft.SarlaftAlertaService;
import co.assip.erp.sarlaft.dto.EvaluacionResultado;
import co.assip.erp.sarlaft.dto.ReglasInput;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sarlaft")
public class SarlaftEvaluacionController {

    private final SarlaftAlertaService sarlaftService;

    public SarlaftEvaluacionController(SarlaftAlertaService sarlaftService) {
        this.sarlaftService = sarlaftService;
    }

    /**
     * 📌 Endpoint de prueba:
     * Recibe un ReglasInput y ejecuta SARLAFT.
     */
    @PostMapping("/evaluar")
    public EvaluacionResultado evaluar(@RequestBody ReglasInput input) {

        System.out.println("====== SARLAFT INPUT ======");
        System.out.println("Persona: " + input.getIdDatosPersonal());
        System.out.println("Agencia: " + input.getIdAgencia());
        System.out.println("Modulo: " + input.getCodigoModulo());
        System.out.println("Accion: " + input.getAccion());
        System.out.println("Monto: " + input.getMonto());
        System.out.println("FechaUltAct: " + input.getFechaUltimaActualizacion());
        System.out.println("F. Nacimiento: " + input.getFechaNacimiento());
        System.out.println("TipoDoc: " + input.getTipoDocumento());
        System.out.println("FormaAhorro: " + input.getCodigoFormaAhorro());

        System.out.println("Ingresos: " + input.getIngresosMensuales());
        System.out.println("Egresos: " + input.getEgresosMensuales());
        System.out.println("Activos: " + input.getTotalActivos());
        System.out.println("Pasivos: " + input.getTotalPasivos());
        System.out.println("============================");

        return sarlaftService.evaluarOperacion(input);
    }

}
