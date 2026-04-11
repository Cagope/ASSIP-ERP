package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.PeriodoActivoDetalleDTO;
import org.springframework.stereotype.Service;

@Service
public class PeriodoNominaActivoService {

    private final PeriodosNominaRepository repo;

    public PeriodoNominaActivoService(PeriodosNominaRepository repo) {
        this.repo = repo;
    }

    // =========================================================
    // ID DEL PERÍODO ACTIVO (ABIERTO)
    // =========================================================
    public Integer obtenerPeriodoActivo() {

        Integer idPeriodo = repo.obtenerPeriodoActivoId();

        if (idPeriodo == null) {
            throw new IllegalStateException(
                    "No existe un período de nómina ABIERTO"
            );
        }

        return idPeriodo;
    }

    // =========================================================
// 🔥 DETALLE DEL PERÍODO ACTIVO (FECHAS)
// =========================================================
    public PeriodoActivoDetalleDTO obtenerPeriodoActivoDetalle() {

        Integer idPeriodo = obtenerPeriodoActivo();

        var fechas = repo.obtenerFechas(idPeriodo); // ✅ NOMBRE CORRECTO

        if (fechas == null) {
            throw new IllegalStateException(
                    "No se pudieron obtener las fechas del período activo"
            );
        }

        return new PeriodoActivoDetalleDTO(
                idPeriodo,
                fechas.fechaInicio(),
                fechas.fechaFin()
        );
    }

    public PeriodosNominaRepository.PeriodoActivoInfo obtenerPeriodoActivoInfo() {

        var info = repo.obtenerPeriodoActivoInfo();

        if (info == null) {
            throw new IllegalStateException(
                    "No existe un período de nómina ABIERTO"
            );
        }

        return info;
    }
}