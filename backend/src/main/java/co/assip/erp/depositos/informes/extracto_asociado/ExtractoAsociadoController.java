package co.assip.erp.depositos.informes.extracto_asociado;

import co.assip.erp.depositos.informes.extracto_asociado.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/informes/extracto_asociado")
@RequiredArgsConstructor
public class ExtractoAsociadoController {

    private final ExtractoAsociadoService service;

    @GetMapping("/buscar-asociados")
    public List<ExtractoAsociadoBusquedaDTO> buscarAsociados(
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String codigoCuenta
    ) {

        return service.buscarAsociados(
                documento,
                nombres,
                primerApellido,
                segundoApellido,
                codigoCuenta
        );
    }

    @PostMapping
    public ExtractoAsociadoResponseDTO consultar(
            @RequestBody ExtractoAsociadoRequestDTO request
    ) {

        return service.consultar(request);
    }

}