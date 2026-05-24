package co.assip.erp.depositos.informes.documentos_soporte;

import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentoSoporteResumenAgenciaFormaDTO;
import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentosSoporteInformeRequestDTO;
import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentosSoporteInformeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/informes/documentos-soporte")
@RequiredArgsConstructor
public class DocumentosSoporteInformeController {

    private final DocumentosSoporteInformeService service;

    @PostMapping
    public DocumentosSoporteInformeResponseDTO consultar(
            @RequestBody DocumentosSoporteInformeRequestDTO request
    ) {

        return service.consultar(request);

    }

    @PostMapping("/resumen-agencia-forma")
    public List<DocumentoSoporteResumenAgenciaFormaDTO> resumenPorAgenciaForma(
            @RequestBody DocumentosSoporteInformeRequestDTO request
    ) {

        return service.resumenPorAgenciaForma(request);

    }

}