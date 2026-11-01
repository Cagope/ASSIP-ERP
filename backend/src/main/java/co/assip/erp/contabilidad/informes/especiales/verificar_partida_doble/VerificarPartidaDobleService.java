package co.assip.erp.contabilidad.informes.especiales.verificar_partida_doble;

import co.assip.erp.contabilidad.informes.especiales.verificar_partida_doble.dto.VerificarPartidaDobleResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificarPartidaDobleService {
    private final VerificarPartidaDobleRepository repository;

    public VerificarPartidaDobleService(VerificarPartidaDobleRepository repository) {
        this.repository = repository;
    }

    /** Verifica todos los comprobantes del período y cuenta los que presentan diferencias. */
    @Transactional(readOnly = true)
    public VerificarPartidaDobleResponseDTO consultar(
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
        int descuadrados = 0;
        for (var fila : filas) {
            debitos = debitos.add(fila.totalDebitos());
            creditos = creditos.add(fila.totalCreditos());
            if (fila.descuadrado()) {
                descuadrados++;
            }
        }
        return new VerificarPartidaDobleResponseDTO(
                idAgencia, fechaInicial, fechaFinal, filas,
                filas.size(), filas.size() - descuadrados, descuadrados,
                debitos, creditos, debitos.subtract(creditos)
        );
    }
}
