package co.assip.erp.depositos.apertura_cuentas;

import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentaItemDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasEntradaDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasRespuestaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hoja-vida/apertura-cuentas")
public class AperturaCuentasController {

    private final AperturaCuentasService service;

    // ============================================================
    // 🔹 LISTAR FORMAS DISPONIBLES
    //    Solo recibe idPersona. La agencia se toma del JWT en el service.
    // ============================================================
    @GetMapping("/formas/{idPersona}")
    public List<AperturaCuentaItemDTO> listarFormas(
            @PathVariable Integer idPersona
    ) {
        System.out.println("🔥 *** AperturaCuentasController.listarFormas ejecutándose ***");
        System.out.println("🔥 idPersona = " + idPersona);
        System.out.println("==============================================");

        return service.listarFormas(idPersona);
    }

    // ============================================================
    // 🔹 CREAR CUENTA
    // ============================================================
    @PostMapping
    public AperturaCuentasRespuestaDTO crear(@RequestBody AperturaCuentasEntradaDTO dto) {
        return service.crear(dto);
    }
}
