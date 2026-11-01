
package co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes;

import co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes.dto.SecuencialComprobantesFilaDTO;
import co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes.dto.SecuencialComprobantesFiltroDTO;
import co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes.dto.SecuencialComprobantesResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecuencialComprobantesService {
    private final SecuencialComprobantesRepository repository;

    public SecuencialComprobantesService(
            SecuencialComprobantesRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Consulta el secuencial de comprobantes de una agencia.
     * Valida los filtros y consolida los totales del período.
     * La autorización de agencia debe verificarse antes de invocar este método.
     */
    @Transactional(readOnly = true)
    public SecuencialComprobantesResponseDTO consultar(
            SecuencialComprobantesFiltroDTO filtro
    ) {
        validarFiltros(filtro);
        List<SecuencialComprobantesFilaDTO> filas = repository.consultar(
                filtro.idAgencia(),
                filtro.fechaInicial(),
                filtro.fechaFinal()
        );
        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;
        for (SecuencialComprobantesFilaDTO fila : filas) {
            totalDebitos = totalDebitos.add(fila.totalDebitos());
            totalCreditos = totalCreditos.add(fila.totalCreditos());
        }
        return new SecuencialComprobantesResponseDTO(
                filtro.idAgencia(),
                filtro.fechaInicial(),
                filtro.fechaFinal(),
                filas,
                totalDebitos,
                totalCreditos,
                totalDebitos.subtract(totalCreditos)
        );
    }

    /**
     * Valida la agencia y el rango cronológico solicitado.
     * Rechaza filtros incompletos o fechas inconsistentes.
     */
    private void validarFiltros(SecuencialComprobantesFiltroDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException(
                    "Debe proporcionar los filtros del informe."
            );
        }
        if (filtro.idAgencia() == null || filtro.idAgencia() <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una agencia válida."
            );
        }
        LocalDate fechaInicial = filtro.fechaInicial();
        LocalDate fechaFinal = filtro.fechaFinal();
        if (fechaInicial == null || fechaFinal == null) {
            throw new IllegalArgumentException(
                    "Debe indicar las fechas inicial y final."
            );
        }
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }
    }
}
