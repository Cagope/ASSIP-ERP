package co.assip.erp.depositos.informes.gmf_semanal;

import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalRequestDTO;
import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/depositos/informes/gmf-semanal")
@RequiredArgsConstructor
public class GmfSemanalController {

    private final GmfSemanalService service;

    @PostMapping
    public ResponseEntity<GmfSemanalResponseDTO> consultar(
            @RequestBody GmfSemanalRequestDTO request
    ) {

        GmfSemanalResponseDTO response =
                service.consultar(request);

        return ResponseEntity.ok(response);
    }
}