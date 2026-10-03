package co.assip.erp.depositos.informes.saldos;

import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteItemDTO;
import co.assip.erp.depositos.informes.saldos.dto.SaldosCorteResumenDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/depositos/informes/saldos")
public class SaldosCorteController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_INFORMES_VIEW";

    private final SaldosCorteService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public SaldosCorteController(
            SaldosCorteService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    @PostMapping("/corte")
    public Map<String, Object> consultar(
            @RequestBody Map<String, Object> filtros
    ) {

        validarAcceso();

        String agencia =
                filtros.getOrDefault("agencia", "0").toString();

        String fechaCorte =
                filtros.get("fechaCorte").toString();

        List<SaldosCorteItemDTO> datos =
                service.consultar(
                        fechaCorte,
                        agencia
                );

        SaldosCorteResumenDTO resumen =
                service.resumen(datos);

        return Map.of(
                "resumen", resumen,
                "items", datos
        );
    }

    @GetMapping("/resumen-agencia")
    public List<Map<String, Object>> resumenPorAgencia(
            @RequestParam String fechaCorte,
            @RequestParam(defaultValue = "0") String agencia
    ) {

        validarAcceso();

        return service.resumenPorAgencia(
                fechaCorte,
                agencia
        );
    }

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