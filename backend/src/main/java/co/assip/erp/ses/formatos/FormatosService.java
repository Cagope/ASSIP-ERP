
package co.assip.erp.ses.formatos;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormatosService {

    private final FormatosRepository formatosRepository;

    /**
     * Obtiene el código oficial de un formato SES activo.
     *
     * Identifica el formato mediante:
     * - Módulo de origen.
     * - Nombre funcional del formato.
     *
     * La consulta se realiza en supersolidaria.formatos.
     *
     * No modifica información de la base de datos.
     */
    @Transactional(readOnly = true)
    public String obtenerCodigoFormato(
            String moduloOrigen,
            String nombreFormato
    ) {

        if (moduloOrigen == null || moduloOrigen.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar el módulo de origen del formato SES."
            );
        }

        if (nombreFormato == null || nombreFormato.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar el nombre del formato SES."
            );
        }

        return formatosRepository.consultarCodigoFormato(
                moduloOrigen.trim(),
                nombreFormato.trim()
        ).orElseThrow(() -> new IllegalStateException(
                "No existe un formato SES activo para el módulo '" +
                        moduloOrigen.trim() +
                        "' y el nombre '" +
                        nombreFormato.trim() +
                        "'."
        ));
    }
}
