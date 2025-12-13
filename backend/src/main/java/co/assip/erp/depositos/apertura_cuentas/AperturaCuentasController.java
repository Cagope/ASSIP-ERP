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
    // 🔹 0. VALIDAR (usado por botón "Gestionar")
    // ============================================================
    @GetMapping("/validar/{idPersona}/{idAgencia}")
    public AperturaCuentasRespuestaDTO validar(
            @PathVariable Integer idPersona,
            @PathVariable Integer idAgencia
    ) {
        System.out.println("🔥 VALIDANDO apertura -> persona=" + idPersona + " agencia=" + idAgencia);
        return service.validarApertura(idPersona, idAgencia);
    }

    // ============================================================
    // 🔹 1. LISTAR FORMAS DISPONIBLES
    //    NO valida, solo retorna formas para el formulario
    // ============================================================
    @GetMapping("/formas/{idPersona}")
    public List<AperturaCuentaItemDTO> listarFormas(@PathVariable Integer idPersona) {

        System.out.println("🔥 listarFormas ejecutándose");
        System.out.println("Persona = " + idPersona);
        System.out.println("---------------------------------------");

        return service.listarFormas(idPersona);
    }

    // ============================================================
    // 🔹 2. CREAR LAS DOS CUENTAS
    // ============================================================
    @PostMapping
    public AperturaCuentasRespuestaDTO crear(@RequestBody AperturaCuentasEntradaDTO dto) {
        System.out.println("🔥 Crear cuentas ejecutándose");
        return service.crear(dto);
    }
}
