package co.assip.erp.depositos.documentos_soporte;

import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteBusquedaDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteCuentaDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteFormDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteGuardarDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteHistoricoDTO;
import co.assip.erp.depositos.documentos_soporte.dto.DocumentosSoporteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentosSoporteService {

    private final DocumentosSoporteRepository repository;

    public List<DocumentosSoporteCuentaDTO> buscarCuentas(
            DocumentosSoporteBusquedaDTO filtros
    ) {

        return repository.buscarCuentas(filtros);

    }

    public DocumentosSoporteFormDTO obtenerDocumentoActivo(
            Integer idCuentaAhorro
    ) {

        return repository.obtenerDocumentoActivo(idCuentaAhorro)
                .orElse(null);

    }

    public List<DocumentosSoporteHistoricoDTO> obtenerHistorico(
            Integer idCuentaAhorro
    ) {

        return repository.obtenerHistorico(idCuentaAhorro);

    }

    public DocumentosSoporteResponseDTO guardar(
            DocumentosSoporteGuardarDTO dto
    ) {

        if (dto.getAccion() == null
                || dto.getAccion().isBlank()) {

            return DocumentosSoporteResponseDTO.builder()
                    .success(false)
                    .message("Debe indicar la acción.")
                    .build();

        }

        DocumentosSoporteCuentaDTO cuenta =
                repository.obtenerDatosCuenta(
                        dto.getIdCuentaAhorro()
                ).orElse(null);

        if (cuenta == null) {

            return DocumentosSoporteResponseDTO.builder()
                    .success(false)
                    .message("No se encontró la cuenta.")
                    .build();

        }

        DocumentosSoporteFormDTO documentoActivo =
                repository.obtenerDocumentoActivo(
                        dto.getIdCuentaAhorro()
                ).orElse(null);

        String accion =
                dto.getAccion().trim().toUpperCase();

        /*
         * =========================================
         * INACTIVAR
         * =========================================
         */
        if ("INACTIVAR".equals(accion)
                || "PERDIDO".equals(accion)
                || "ROBADO".equals(accion)) {

            if (documentoActivo == null) {

                return DocumentosSoporteResponseDTO.builder()
                        .success(false)
                        .message(
                                "La cuenta no tiene documento activo."
                        )
                        .build();

            }

            String nuevoEstado = switch (accion) {
                case "PERDIDO" -> "P";
                case "ROBADO" -> "R";
                default -> "I";
            };

            repository.actualizarEstadoDocumento(
                    documentoActivo.getIdDocumentoSoporte(),
                    nuevoEstado
            );

            return DocumentosSoporteResponseDTO.builder()
                    .success(true)
                    .message("Documento actualizado correctamente.")
                    .idDocumentoSoporte(
                            documentoActivo.getIdDocumentoSoporte()
                    )
                    .build();

        }

        /*
         * =========================================
         * INCLUIR / CAMBIAR
         * =========================================
         */

        if ("INCLUIR".equals(accion)
                && documentoActivo != null) {

            return DocumentosSoporteResponseDTO.builder()
                    .success(false)
                    .message(
                            "La cuenta ya tiene un documento activo."
                    )
                    .build();

        }

        if ("CAMBIAR".equals(accion)
                && documentoActivo == null) {

            return DocumentosSoporteResponseDTO.builder()
                    .success(false)
                    .message(
                            "La cuenta no tiene documento activo."
                    )
                    .build();

        }

        if (dto.getNumeroInicial() == null
                || dto.getNumeroInicial().isBlank()) {

            return DocumentosSoporteResponseDTO.builder()
                    .success(false)
                    .message(
                            "Debe indicar el número inicial."
                    )
                    .build();

        }

        String numeroInicial =
                dto.getNumeroInicial()
                        .trim()
                        .replace(".", "")
                        .replace(",", "");

        if (!numeroInicial.matches("\\d+")) {

            return DocumentosSoporteResponseDTO.builder()
                    .success(false)
                    .message(
                            "El número inicial debe ser numérico."
                    )
                    .build();

        }

        numeroInicial =
                String.format(
                        "%010d",
                        Long.parseLong(numeroInicial)
                );

        long inicial =
                Long.parseLong(numeroInicial);

        long finalNumero =
                inicial
                        + cuenta.getCantidadSoporte()
                        - 1L;

        String numeroFinal =
                String.format(
                        "%010d",
                        finalNumero
                );

        boolean rangoExiste =
                repository.existeRangoActivo(
                        cuenta.getIdFormaAhorro(),
                        cuenta.getDocumentoForma(),
                        numeroInicial,
                        numeroFinal
                );

        if (rangoExiste) {

            return DocumentosSoporteResponseDTO.builder()
                    .success(false)
                    .message(
                            "El rango documental ya existe."
                    )
                    .build();

        }

        /*
         * =========================================
         * CAMBIAR
         * =========================================
         */

        if ("CAMBIAR".equals(accion)) {

            repository.actualizarEstadoDocumento(
                    documentoActivo.getIdDocumentoSoporte(),
                    "I"
            );

        }

        Integer idDocumento =
                repository.guardar(
                        dto,
                        cuenta.getDocumentoForma(),
                        numeroInicial,
                        numeroFinal
                );

        return DocumentosSoporteResponseDTO.builder()
                .success(true)
                .message(
                        "Documento registrado correctamente."
                )
                .idDocumentoSoporte(idDocumento)
                .build();

    }

}