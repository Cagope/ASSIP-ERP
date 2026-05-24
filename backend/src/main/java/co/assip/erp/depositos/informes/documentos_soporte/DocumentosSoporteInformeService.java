package co.assip.erp.depositos.informes.documentos_soporte;

import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentoSoporteResumenAgenciaFormaDTO;
import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentosSoporteInformeRequestDTO;
import co.assip.erp.depositos.informes.documentos_soporte.dto.DocumentosSoporteInformeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentosSoporteInformeService {

    private final DocumentosSoporteInformeRepository repository;

    public DocumentosSoporteInformeResponseDTO consultar(
            DocumentosSoporteInformeRequestDTO request
    ) {

        validarRequest(request);

        String agencia =
                normalizarAgencia(request.getAgencia());

        DocumentosSoporteInformeResponseDTO response =
                new DocumentosSoporteInformeResponseDTO();

        response.setResumen(
                repository.resumen(
                        agencia,
                        request.getFechaDesde(),
                        request.getFechaHasta()
                )
        );

        response.setItems(
                repository.consultar(
                        agencia,
                        request.getFechaDesde(),
                        request.getFechaHasta()
                )
        );

        return response;

    }

    public List<DocumentoSoporteResumenAgenciaFormaDTO> resumenPorAgenciaForma(
            DocumentosSoporteInformeRequestDTO request
    ) {

        validarRequest(request);

        return repository.resumenPorAgenciaForma(
                normalizarAgencia(request.getAgencia()),
                request.getFechaDesde(),
                request.getFechaHasta()
        );

    }

    private void validarRequest(
            DocumentosSoporteInformeRequestDTO request
    ) {

        if (request == null) {
            throw new RuntimeException("Debe enviar los filtros del informe.");
        }

        if (request.getFechaDesde() == null
                || request.getFechaDesde().isBlank()) {
            throw new RuntimeException("Debe indicar la fecha inicial.");
        }

        if (request.getFechaHasta() == null
                || request.getFechaHasta().isBlank()) {
            throw new RuntimeException("Debe indicar la fecha final.");
        }

    }

    private String normalizarAgencia(
            String agencia
    ) {

        if (agencia == null || agencia.isBlank()) {
            return "0";
        }

        return agencia.trim();

    }

}