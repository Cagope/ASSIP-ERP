package co.assip.erp.depositos.interesmensual_sm;

import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMEntradaDTO;
import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteresMensualSMService {

    private final InteresMensualSMRepository repository;
    private final NamedParameterJdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public List<InteresMensualSMItemDTO> liquidar(InteresMensualSMEntradaDTO input) {

        // =====================================================
        // 1️⃣ Validar forma seleccionada
        // =====================================================
        Map<String, Object> forma = jdbc.queryForMap("""
            SELECT id_forma_ahorro, tiempo_liquidacion
            FROM depositos.formas_ahorro
            WHERE id_forma_ahorro = :formaId
        """, Map.of("formaId", input.getFormaId()));

        Integer tiempo = ((Number) forma.get("tiempo_liquidacion")).intValue();

        if (tiempo != 30) {
            throw new IllegalStateException(
                    "ERROR_VALIDACION|La forma seleccionada NO permite interés mensual. "
                            + "(tiempo_liquidacion = " + tiempo + ")"
            );
        }

        // =====================================================
        // 2️⃣ Ejecutar el cálculo técnico (repository YA devuelve DTO)
        // =====================================================
        List<InteresMensualSMItemDTO> lista = repository.liquidar(input);

        // =====================================================
        // 3️⃣ Filtrar cuentas con valores útiles
        // =====================================================
        return lista.stream()
                .filter(x -> x.getSaldoMinimoMes() != null && x.getSaldoMinimoMes().doubleValue() > 0)
                .filter(x -> x.getInteresBruto() != null && x.getInteresBruto().doubleValue() > 0)
                .toList();
    }
}
