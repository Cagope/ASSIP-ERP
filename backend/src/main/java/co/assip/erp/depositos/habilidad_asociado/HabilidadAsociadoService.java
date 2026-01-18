package co.assip.erp.depositos.habilidad_asociado;

import co.assip.erp.depositos.habilidad_asociado.dto.HabilidadAsociadoEntradaDTO;
import co.assip.erp.depositos.habilidad_asociado.dto.HabilidadAsociadoItemDTO;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabilidadAsociadoService {

    private final HabilidadAsociadoRepository repository;
    private final NamedParameterJdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public List<HabilidadAsociadoItemDTO> ejecutar(HabilidadAsociadoEntradaDTO input) {

        if (input.getFechaInicio() == null || input.getFechaFin() == null) {
            throw new IllegalArgumentException("ERROR_VALIDACION|Debe indicar fechaInicio y fechaFin.");
        }

        if (input.getValorMenores() == null ||
                input.getValorMayores() == null ||
                input.getValorJuridicas() == null) {
            throw new IllegalArgumentException("ERROR_VALIDACION|Todos los valores de evaluación son obligatorios.");
        }

        return repository.evaluar(input);
    }

    /**
     * Cambia el estado del asociado entre HÁBIL / INHÁBIL.
     */
    @Transactional
    public void actualizarEstado(Integer idCuentaAhorro, String nuevoEstado) {

        if (idCuentaAhorro == null || idCuentaAhorro <= 0) {
            throw new IllegalArgumentException("ERROR|Debe indicar un idCuentaAhorro válido.");
        }

        if (!"A".equalsIgnoreCase(nuevoEstado) &&
                !"I".equalsIgnoreCase(nuevoEstado)) {
            throw new IllegalArgumentException("ERROR|Estado inválido. Solo se permite 'A' o 'I'.");
        }

        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            throw new RuntimeException("USUARIO_NO_AUTENTICADO");
        }

        repository.actualizarEstado(
                idCuentaAhorro,
                nuevoEstado.toUpperCase(),
                idUsuario
        );
    }

    private BigDecimal num(Object o) {
        return o == null ? BigDecimal.ZERO : new BigDecimal(o.toString());
    }
}
