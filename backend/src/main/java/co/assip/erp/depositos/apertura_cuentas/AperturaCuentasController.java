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
    //    Recibe:
    //      - idPersona por path
    //      - agencias del usuario (opcional) por header X-Agencias
    // ============================================================
    @GetMapping("/formas/{idPersona}")
    public List<AperturaCuentaItemDTO> listarFormas(
            @PathVariable Integer idPersona,
            @RequestHeader(value = "X-Agencias", required = false) List<Integer> agenciasUsuario
    ) {
        return service.listarFormas(idPersona, agenciasUsuario);
    }

    // ============================================================
    // 🔹 CREAR CUENTA
    // ============================================================
    @PostMapping
    public AperturaCuentasRespuestaDTO crear(@RequestBody AperturaCuentasEntradaDTO dto) {
        return service.crear(dto);
    }
}
