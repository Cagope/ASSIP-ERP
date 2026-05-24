package co.assip.erp.depositos.informes.entradas_salidas;

import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasRequestDTO;
import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/depositos/informes/entradas-salidas")
@RequiredArgsConstructor
public class EntradasSalidasController {

    private final EntradasSalidasService service;

    @PostMapping
    public EntradasSalidasResponseDTO consultar(
            @RequestBody EntradasSalidasRequestDTO request
    ) {
        return service.consultar(request);
    }

    @PostMapping("/resumen-forma")
    public EntradasSalidasResponseDTO resumenPorForma(
            @RequestBody EntradasSalidasRequestDTO request
    ) {
        return service.resumenPorForma(request);
    }

    @PostMapping("/detalle")
    public EntradasSalidasResponseDTO detalleMovimientos(
            @RequestBody EntradasSalidasRequestDTO request
    ) {
        return service.detalleMovimientos(request);
    }

}