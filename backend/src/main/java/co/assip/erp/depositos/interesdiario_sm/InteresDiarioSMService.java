package co.assip.erp.depositos.interesdiario_sm;

import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMEntradaDTO;
import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteresDiarioSMService {

    private final InteresDiarioSMRepository repository;
    private final NamedParameterJdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public List<InteresDiarioSMItemDTO> ejecutar(
            InteresDiarioSMEntradaDTO input,
            Integer usuarioId
    ) {

        boolean confirmado = input.getConfirmado() != null && input.getConfirmado();

        // ================================
        // 1️⃣ Leer forma seleccionada
        // ================================
        Map<String, Object> forma = jdbc.queryForMap("""
            SELECT 
                id_forma_ahorro, 
                tiempo_liquidacion,
                fecha_ultima_liquidacion
            FROM depositos.formas_ahorro
            WHERE id_forma_ahorro = :formaId
        """, Map.of("formaId", input.getFormaId()));

        Integer tiempo = ((Number) forma.get("tiempo_liquidacion")).intValue();

        if (tiempo != 1) {
            throw new IllegalStateException(
                    "ERROR_VALIDACION|La forma seleccionada no permite Interés Diario SM. (tiempo_liquidacion = " + tiempo + ")"
            );
        }

        // ================================
        // 2️⃣ Validar fechas
        // ================================
        LocalDate fechaProceso = LocalDate.parse(input.getFechaProceso());
        LocalDate fechaLiquidacion = LocalDate.parse(input.getFechaLiquidacion());

        // ================================
        // 3️⃣ fecha_ultima_liquidacion
        // ================================
        LocalDate fechaUltima = forma.get("fecha_ultima_liquidacion") != null
                ? ((java.sql.Date) forma.get("fecha_ultima_liquidacion")).toLocalDate()
                : null;

        if (fechaUltima == null) {
            throw new IllegalStateException(
                    "ERROR_VALIDACION|La forma seleccionada no tiene fecha_ultima_liquidacion registrada."
            );
        }

        if (!fechaUltima.isBefore(fechaProceso)) {
            throw new IllegalStateException(
                    "ERROR_VALIDACION|La fecha de última liquidación (" + fechaUltima +
                            ") debe ser menor que la fecha de proceso (" + fechaProceso + ")."
            );
        }

        // ================================
        // 4️⃣ Diferencia de días → pedir confirmación
        // ================================
        long dias = ChronoUnit.DAYS.between(fechaUltima, fechaProceso);

        if (!confirmado && dias > 1) {
            throw new IllegalStateException(
                    "CONFIRMAR_DIAS|" + dias + "|" + fechaUltima
            );
        }

        // ================================
        // 5️⃣ Ejecutar proceso técnico
        // ================================
        List<InteresDiarioSMItemDTO> lista = repository.simular(
                input.getAgenciaId(),
                fechaProceso,
                fechaLiquidacion,
                input.getFormaId()
        );

        // ================================
        // 6️⃣ FILTRAR Saldos/intereses en 0 (BigDecimal)
        // ================================
        return lista.stream()
                .filter(x -> x.getSaldoMinimoDia() != null && x.getSaldoMinimoDia().doubleValue() > 0)
                .filter(x -> x.getInteresBruto() != null && x.getInteresBruto().doubleValue() > 0)
                .toList();
    }
}
