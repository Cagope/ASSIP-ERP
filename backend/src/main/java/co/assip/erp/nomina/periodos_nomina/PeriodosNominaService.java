package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.PeriodoAccionDTO;
import co.assip.erp.nomina.periodos_nomina.dto.PeriodoNominaListDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeriodosNominaService {

    private final PeriodosNominaRepository repo;

    public PeriodosNominaService(PeriodosNominaRepository repo) {
        this.repo = repo;
    }

    // =========================================================
    // LISTAR
    // =========================================================
    public List<PeriodoNominaListDTO> listar(Integer idAgencia, Integer anio) {
        return repo.listar(idAgencia, anio);
    }

    // =========================================================
    // ACCIONES
    // =========================================================
    public void ejecutarAccion(PeriodoAccionDTO dto, Integer idUsuario) {

        if (dto == null || dto.getIdPeriodo() == null) {
            throw new IllegalArgumentException("ID período es obligatorio");
        }

        String accion =
                dto.getAccion() != null
                        ? dto.getAccion().trim().toUpperCase()
                        : "";

        if (accion.isBlank()) {
            throw new IllegalArgumentException("Acción es obligatoria");
        }

        switch (accion) {

            case "ABRIR" ->
                    repo.cambiarEstado(dto.getIdPeriodo(), "ABIERTO", idUsuario);

            case "CERRAR" ->
                    repo.cambiarEstado(dto.getIdPeriodo(), "CERRADO", idUsuario);

            case "LIQUIDAR" ->
                    repo.marcarLiquidado(dto.getIdPeriodo(), idUsuario);

            case "CONTABILIZAR" ->
                    repo.marcarContabilizado(dto.getIdPeriodo(), idUsuario);

            default ->
                    throw new IllegalArgumentException("Acción no soportada: " + accion);
        }
    }
}
