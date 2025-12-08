package co.assip.erp.depositos.interesmensual_tac;

import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACEntradaDTO;
import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InteresMensualTACService {

    private final InteresMensualTACRepository repository;

    @Transactional(readOnly = true)
    public List<InteresMensualTACItemDTO> liquidar(InteresMensualTACEntradaDTO input) {

        Integer formaId = input.getFormaId();

        // Validación
        if (formaId == null || !(formaId == 7 || formaId == 14)) {
            throw new IllegalStateException(
                    "ERROR_VALIDACION|La forma seleccionada NO es TAC. Solo se permiten 7 y 14."
            );
        }

        List<InteresMensualTACItemDTO> lista = repository.liquidar(input);

        // Filtrar cuentas útiles
        return lista.stream()
                .filter(x -> x.getInteresBruto() != null && x.getInteresBruto().doubleValue() > 0)
                .filter(x -> x.getSaldoActual() != null && x.getSaldoActual().doubleValue() > 0)
                .toList();
    }
}
