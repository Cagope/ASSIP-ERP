
package co.assip.erp.contabilidad.informes.especiales.resumen_diario_detallado;

import co.assip.erp.contabilidad.informes.especiales.resumen_diario_detallado.dto.ResumenDiarioDetalladoFilaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Construye el Resumen diario detallado a partir de los auxiliares contables.
 * Agrupa movimientos por fecha y calcula totales sin modificar registros.
 */
@Service
@RequiredArgsConstructor
public class ResumenDiarioDetalladoService {

    private final ResumenDiarioDetalladoRepository repository;

    /**
     * Consulta los movimientos activos del período y calcula los totales diarios
     * y generales. La autorización de usuario y agencia corresponde al controlador.
     */
    @Transactional(readOnly = true)
    public Resultado consultar(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        validarParametros(idAgencia, fechaInicial, fechaFinal);

        List<ResumenDiarioDetalladoFilaDTO> movimientos =
                repository.consultar(idAgencia, fechaInicial, fechaFinal);

        Map<LocalDate, List<ResumenDiarioDetalladoFilaDTO>> movimientosPorFecha =
                new LinkedHashMap<>();

        for (ResumenDiarioDetalladoFilaDTO movimiento : movimientos) {
            movimientosPorFecha
                    .computeIfAbsent(movimiento.fecha(), fecha -> new ArrayList<>())
                    .add(movimiento);
        }

        List<ResumenDia> dias = new ArrayList<>();
        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, List<ResumenDiarioDetalladoFilaDTO>> entrada
                : movimientosPorFecha.entrySet()) {

            BigDecimal debitosDia = BigDecimal.ZERO;
            BigDecimal creditosDia = BigDecimal.ZERO;

            for (ResumenDiarioDetalladoFilaDTO movimiento : entrada.getValue()) {
                debitosDia = debitosDia.add(valor(movimiento.valorDebito()));
                creditosDia = creditosDia.add(valor(movimiento.valorCredito()));
            }

            dias.add(new ResumenDia(
                    entrada.getKey(),
                    List.copyOf(entrada.getValue()),
                    debitosDia,
                    creditosDia,
                    debitosDia.subtract(creditosDia)
            ));

            totalDebitos = totalDebitos.add(debitosDia);
            totalCreditos = totalCreditos.add(creditosDia);
        }

        return new Resultado(
                idAgencia,
                fechaInicial,
                fechaFinal,
                List.copyOf(dias),
                movimientos.size(),
                totalDebitos,
                totalCreditos,
                totalDebitos.subtract(totalCreditos)
        );
    }

    /** Valida la agencia y el orden cronológico de las fechas solicitadas. */
    private void validarParametros(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal
    ) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una agencia válida."
            );
        }

        if (fechaInicial == null || fechaFinal == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha inicial y la fecha final."
            );
        }

        if (fechaInicial.isAfter(fechaFinal)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede superar la fecha final."
            );
        }
    }

    /** Convierte importes nulos en cero para efectuar las sumatorias. */
    private BigDecimal valor(BigDecimal importe) {
        return importe == null ? BigDecimal.ZERO : importe;
    }

    /** Contiene los movimientos y totales contables de una fecha. */
    public record ResumenDia(
            LocalDate fecha,
            List<ResumenDiarioDetalladoFilaDTO> movimientos,
            BigDecimal totalDebitos,
            BigDecimal totalCreditos,
            BigDecimal diferencia
    ) {
    }

    /** Contiene los resultados y totales generales del informe. */
    public record Resultado(
            Integer idAgencia,
            LocalDate fechaInicial,
            LocalDate fechaFinal,
            List<ResumenDia> dias,
            Integer cantidadMovimientos,
            BigDecimal totalDebitos,
            BigDecimal totalCreditos,
            BigDecimal diferencia
    ) {
    }
}
