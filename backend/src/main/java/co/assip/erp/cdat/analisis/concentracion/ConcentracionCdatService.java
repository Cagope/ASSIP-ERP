package co.assip.erp.cdat.analisis.concentracion;

import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatDepositanteDTO;
import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatDetalleDTO;
import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatResumenDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConcentracionCdatService {

    private final ConcentracionCdatRepository repository;

    public ConcentracionCdatService(
            ConcentracionCdatRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // CORTES
    // =========================================================

    public List<LocalDate> listarCortes() {
        return repository.cortes();
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    public ConcentracionCdatResumenDTO consultarResumen(
            LocalDate fechaCorte,
            Integer idAgencia
    ) {
        LocalDate corte = resolverCorte(fechaCorte);

        return repository.resumen(
                corte,
                idAgencia
        );
    }

    // =========================================================
    // RANKING
    // =========================================================

    public List<ConcentracionCdatDepositanteDTO> consultarRanking(
            LocalDate fechaCorte,
            Integer idAgencia
    ) {
        LocalDate corte = resolverCorte(fechaCorte);

        return repository.ranking(
                corte,
                idAgencia
        );
    }

    // =========================================================
    // DETALLE DEL DEPOSITANTE
    // =========================================================

    public List<ConcentracionCdatDetalleDTO> consultarDetalle(
            LocalDate fechaCorte,
            Long idDatosPersonal,
            Integer idAgencia
    ) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException(
                    "El id del depositante es obligatorio."
            );
        }

        LocalDate corte = resolverCorte(fechaCorte);

        return repository.detalle(
                corte,
                idDatosPersonal,
                idAgencia
        );
    }

    // =========================================================
    // RESOLUCIÓN Y VALIDACIÓN DEL CORTE
    // =========================================================

    private LocalDate resolverCorte(LocalDate fechaCorte) {

        LocalDate corte = fechaCorte;

        if (corte == null) {
            corte = repository.ultimoCorte();
        }

        if (corte == null) {
            throw new IllegalStateException(
                    "No existen cierres mensuales de CDAT generados."
            );
        }

        if (!repository.existeCorte(corte)) {
            throw new IllegalArgumentException(
                    "No existe un cierre mensual de CDAT generado para la fecha "
                            + corte
                            + "."
            );
        }

        return corte;
    }
}