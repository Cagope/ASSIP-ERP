package co.assip.erp.cartera.analisis.riesgodeterioro;

import co.assip.erp.cartera.analisis.riesgodeterioro.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalisisRiesgoDeterioroService {

    private final AnalisisRiesgoDeterioroRepository repository;

    public AnalisisRiesgoDeterioroService(
            AnalisisRiesgoDeterioroRepository repository
    ) {
        this.repository = repository;
    }


    // =========================================================
    // CORTES
    // =========================================================

    public List<LocalDate> listarCortes() {

        return repository.listarCortes();
    }


    // =========================================================
    // RESUMEN
    // =========================================================

    public RiesgoDeterioroResumenDTO consultarResumen(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarFechaCorte(
                fechaCorte
        );

        return repository.consultarResumen(
                fechaCorte,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // EDADES
    // =========================================================

    public List<RiesgoDeterioroEdadDTO> consultarEdades(
            LocalDate fechaCorte,
            String tipoEdad,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarFechaCorte(
                fechaCorte
        );

        if (
                tipoEdad == null
                        ||
                        tipoEdad.isBlank()
        ) {
            tipoEdad = "CONTABLE";
        }

        return repository.consultarEdades(
                fechaCorte,
                tipoEdad,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // SEGMENTACIÓN
    // =========================================================

    public List<RiesgoDeterioroSegmentoDTO> consultarSegmentacion(
            LocalDate fechaCorte,
            String dimension,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarFechaCorte(
                fechaCorte
        );

        if (
                dimension == null
                        ||
                        dimension.isBlank()
        ) {
            dimension = "LINEA";
        }

        return repository.consultarSegmentacion(
                fechaCorte,
                dimension,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // EVOLUCIÓN
    // =========================================================

    public List<RiesgoDeterioroEvolucionDTO> consultarEvolucion(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarFechaCorte(
                fechaDesde
        );

        validarFechaCorte(
                fechaHasta
        );

        if (
                fechaDesde.isAfter(
                        fechaHasta
                )
        ) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }

        return repository.consultarEvolucion(
                fechaDesde,
                fechaHasta,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // CONCENTRACIÓN
    // =========================================================

    public List<RiesgoDeterioroConcentracionDTO> consultarConcentracion(
            LocalDate fechaCorte,
            String criterio,
            Integer limite,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarFechaCorte(
                fechaCorte
        );

        if (
                criterio == null
                        ||
                        criterio.isBlank()
        ) {
            criterio = "PERDIDA_ESPERADA";
        }

        return repository.consultarConcentracion(
                fechaCorte,
                criterio,
                limite,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // DETALLE
    // =========================================================

    public List<RiesgoDeterioroDetalleDTO> consultarDetalle(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarFechaCorte(
                fechaCorte
        );

        return repository.consultarDetalle(
                fechaCorte,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarFechaCorte(
            LocalDate fecha
    ) {

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }
    }
}