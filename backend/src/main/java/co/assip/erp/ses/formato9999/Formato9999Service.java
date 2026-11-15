
package co.assip.erp.ses.formato9999;

import co.assip.erp.ses.formato9999.dto.Formato9999DTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Formato9999Service {

    private final Formato9999Repository repository;

    /**
     * Consulta los asociados para el reporte SES.
     *
     * La fecha de entrada se recibe en formato ISO:
     * yyyy-MM-dd.
     *
     * El repositorio devuelve las fechas del reporte
     * en formato ddMMyyyy y agrega los campos
     * CODENTIDAD y FECHACORTE.
     *
     * No modifica información de la base de datos.
     */
    @Transactional(readOnly = true)
    public List<Formato9999DTO> consultar(String fechaCorte) {

        if (fechaCorte == null || fechaCorte.isBlank()) {
            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }

        return repository.consultar(fechaCorte);
    }
}
