package co.assip.erp.cartera.originacion.asociados;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cartera/originacion/asociados")
public class OriginacionAsociadoController {

    private final OriginacionAsociadoService service;


    public OriginacionAsociadoController(
            OriginacionAsociadoService service
    ) {
        this.service = service;
    }


    // =========================================================
    // BUSCAR
    // =========================================================

    @GetMapping("/buscar")
    public ResponseEntity<List<OriginacionAsociadoDTO>> buscar(

            @RequestParam
            Integer idAgencia,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String documento,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String nombres,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String primerApellido,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String segundoApellido
    ) {

        return ResponseEntity.ok(
                service.buscar(
                        documento,
                        nombres,
                        primerApellido,
                        segundoApellido,
                        idAgencia
                )
        );
    }


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @GetMapping("/{idDatosPersonal}")
    public ResponseEntity<OriginacionAsociadoDTO> buscarPorId(

            @PathVariable
            Integer idDatosPersonal,

            @RequestParam
            Integer idAgencia
    ) {

        return ResponseEntity.ok(
                service.buscarPorId(
                        idDatosPersonal,
                        idAgencia
                )
        );
    }
}