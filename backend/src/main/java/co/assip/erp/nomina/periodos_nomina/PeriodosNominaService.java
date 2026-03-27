package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.PeriodoAccionDTO;
import co.assip.erp.nomina.periodos_nomina.dto.PeriodoNominaListDTO;
import co.assip.erp.nomina.novedades_nomina.NovedadesNominaService;
import co.assip.erp.nomina.liquidacion.LiquidacionNominaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PeriodosNominaService {

    private final PeriodosNominaRepository repo;
    private final NovedadesNominaService novedadesService;
    private final LiquidacionNominaService liquidacionService;

    public PeriodosNominaService(
            PeriodosNominaRepository repo,
            NovedadesNominaService novedadesService,
            LiquidacionNominaService liquidacionService
    ) {
        this.repo = repo;
        this.novedadesService = novedadesService;
        this.liquidacionService = liquidacionService;
    }

    // =========================================================
    // LISTAR
    // =========================================================
    public List<PeriodoNominaListDTO> listar(Integer idAgencia, Integer anio) {
        return repo.listar(idAgencia, anio);
    }

    // =========================================================
    // 🔎 PERÍODOS DISPONIBLES PARA CONTABILIZACIÓN
    // =========================================================
    public List<PeriodoNominaListDTO> listarParaContabilizacion() {
        return repo.listarParaContabilizacion();
    }

    // =========================================================
    // ACCIONES DE PERIODO
    // =========================================================
    @Transactional
    public void ejecutarAccion(PeriodoAccionDTO dto, Integer idUsuario) {

        if (dto == null || dto.getIdPeriodo() == null) {
            throw new IllegalArgumentException("ID período es obligatorio");
        }

        String accion = dto.getAccion() != null
                ? dto.getAccion().trim().toUpperCase()
                : "";

        if (accion.isBlank()) {
            throw new IllegalArgumentException("Acción es obligatoria");
        }

        switch (accion) {

            case "ABRIR" -> {
                // 1. Eliminar liquidaciones del período (si existen)
                liquidacionService.eliminarPorPeriodo(dto.getIdPeriodo());

                // 2. Reabrir novedades
                novedadesService.actualizarEstadoPorPeriodo(
                        dto.getIdPeriodo(),
                        "ABIERTO",
                        idUsuario
                );

                // 3. Reabrir período
                repo.cambiarEstado(dto.getIdPeriodo(), "ABIERTO", idUsuario);
            }

            case "CERRAR" -> {
                // 1. Cerrar novedades
                novedadesService.actualizarEstadoPorPeriodo(
                        dto.getIdPeriodo(),
                        "CERRADO",
                        idUsuario
                );

                // 2. Cerrar período
                repo.cambiarEstado(dto.getIdPeriodo(), "CERRADO", idUsuario);
            }

            case "LIQUIDAR" -> {
                // Solo marca el período como liquidado
                repo.marcarLiquidado(dto.getIdPeriodo(), idUsuario);
            }

            case "CONTABILIZAR" -> {
                // Solo marca el período como contabilizado
                repo.marcarContabilizado(dto.getIdPeriodo(), idUsuario);
            }

            default -> throw new IllegalArgumentException(
                    "Acción no soportada: " + accion
            );
        }
    }
}