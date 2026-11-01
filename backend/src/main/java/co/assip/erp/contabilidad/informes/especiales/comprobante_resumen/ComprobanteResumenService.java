package co.assip.erp.contabilidad.informes.especiales.comprobante_resumen;

import co.assip.erp.contabilidad.informes.especiales.comprobante_resumen.dto.ComprobanteResumenResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComprobanteResumenService {
    private final ComprobanteResumenRepository repository;

    /** Valida el período y obtiene el consolidado con sus totales generales. */
    @Transactional(readOnly = true)
    public ComprobanteResumenResponseDTO consultar(
            Integer idAgencia, LocalDate fechaInicial, LocalDate fechaFinal
    ) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una agencia válida.");
        }
        if (fechaInicial == null || fechaFinal == null || fechaInicial.isAfter(fechaFinal)) {
            throw new IllegalArgumentException("Debe indicar un rango de fechas válido.");
        }
        var filas = repository.consultar(idAgencia, fechaInicial, fechaFinal);
        BigDecimal debitos = BigDecimal.ZERO;
        BigDecimal creditos = BigDecimal.ZERO;
        for (var fila : filas) {
            debitos = debitos.add(fila.totalDebitos());
            creditos = creditos.add(fila.totalCreditos());
        }
        return new ComprobanteResumenResponseDTO(
                idAgencia, fechaInicial, fechaFinal, filas, filas.size(), debitos, creditos);
    }
}
