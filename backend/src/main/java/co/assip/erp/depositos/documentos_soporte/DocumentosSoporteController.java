package co.assip.erp.depositos.documentos_soporte;

import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteBusquedaDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteCuentaDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteFormDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteGuardarDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteHistoricoDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/documentos-soporte")
@RequiredArgsConstructor
public class DocumentosSoporteController {

    private final DocumentosSoporteService service;

    @PostMapping("/buscar-cuentas")
    public List<DocumentosSoporteCuentaDTO> buscarCuentas(
            @RequestBody DocumentosSoporteBusquedaDTO filtros
    ) {

        return service.buscarCuentas(filtros);

    }

    @GetMapping("/activo/{idCuentaAhorro}")
    public DocumentosSoporteFormDTO obtenerDocumentoActivo(
            @PathVariable Integer idCuentaAhorro
    ) {

        return service.obtenerDocumentoActivo(idCuentaAhorro);

    }

    @GetMapping("/historico/{idCuentaAhorro}")
    public List<DocumentosSoporteHistoricoDTO> obtenerHistorico(
            @PathVariable Integer idCuentaAhorro
    ) {

        return service.obtenerHistorico(idCuentaAhorro);

    }

    @PostMapping("/guardar")
    public DocumentosSoporteResponseDTO guardar(
            @RequestBody DocumentosSoporteGuardarDTO dto
    ) {

        return service.guardar(dto);

    }

}