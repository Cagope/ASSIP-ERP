package co.assip.erp.general.web;

import co.assip.erp.general.dto.EmpresaDTOs.EmpresaHeader;
import co.assip.erp.general.dto.EmpresaDTOs.EmpresaResponse;
import co.assip.erp.general.service.EmpresaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/general/empresas")
public class EmpresasController {

    private final EmpresaService service;

    public EmpresasController(EmpresaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<EmpresaResponse>> list() {
        return ResponseEntity.ok(service.listEmpresas());
    }

    @GetMapping("/principal")
    public ResponseEntity<EmpresaResponse> principal() {
        EmpresaResponse dto = service.getEmpresaPrincipal();
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/header")
    public ResponseEntity<EmpresaHeader> header() {
        return ResponseEntity.ok(service.getEmpresaHeader());
    }
}
