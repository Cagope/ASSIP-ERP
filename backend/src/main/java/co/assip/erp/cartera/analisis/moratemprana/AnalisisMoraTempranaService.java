package co.assip.erp.cartera.analisis.moratemprana;

import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaCosechaDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaDetalleDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaPrimeraMoraDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaResumenDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaSegmentoDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AnalisisMoraTempranaService {

    // =========================================================
    // CONSTANTES
    // =========================================================

    private static final String INDICADOR_TODOS =
            "TODOS";

    private static final String INDICADOR_MORA30_MOB3 =
            "MORA30_MOB3";

    private static final String INDICADOR_MORA30_MOB6 =
            "MORA30_MOB6";

    private static final String INDICADOR_MORA60_MOB6 =
            "MORA60_MOB6";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final AnalisisMoraTempranaRepository repository;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AnalisisMoraTempranaService(
            AnalisisMoraTempranaRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // ÚLTIMO CORTE DISPONIBLE
    // =========================================================

    public LocalDate obtenerUltimoCorteDisponible() {

        return repository
                .obtenerUltimoCorteDisponible()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existen cierres históricos disponibles para el análisis de mora temprana."
                        )
                );
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    public MoraTempranaResumenDTO consultarResumen(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarParametros(
                cosechaDesde,
                cosechaHasta,
                idAgencia,
                idLineaCredito
        );

        validarMadurezRango(
                cosechaDesde,
                cosechaHasta
        );

        return repository
                .consultarResumen(
                        primerDiaMes(cosechaDesde),
                        primerDiaMes(cosechaHasta),
                        idAgencia,
                        idLineaCredito
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existen créditos con madurez mínima MOB 6 para los filtros seleccionados."
                        )
                );
    }

    // =========================================================
    // COSECHAS
    // =========================================================

    public List<MoraTempranaCosechaDTO> consultarCosechas(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarParametros(
                cosechaDesde,
                cosechaHasta,
                idAgencia,
                idLineaCredito
        );

        validarMadurezRango(
                cosechaDesde,
                cosechaHasta
        );

        return repository.consultarCosechas(
                primerDiaMes(cosechaDesde),
                primerDiaMes(cosechaHasta),
                idAgencia,
                idLineaCredito
        );
    }

    // =========================================================
    // AGENCIAS
    // =========================================================

    public List<MoraTempranaSegmentoDTO> consultarPorAgencia(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarParametros(
                cosechaDesde,
                cosechaHasta,
                idAgencia,
                idLineaCredito
        );

        validarMadurezRango(
                cosechaDesde,
                cosechaHasta
        );

        return repository.consultarPorAgencia(
                primerDiaMes(cosechaDesde),
                primerDiaMes(cosechaHasta),
                idAgencia,
                idLineaCredito
        );
    }

    // =========================================================
    // LÍNEAS
    // =========================================================

    public List<MoraTempranaSegmentoDTO> consultarPorLinea(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarParametros(
                cosechaDesde,
                cosechaHasta,
                idAgencia,
                idLineaCredito
        );

        validarMadurezRango(
                cosechaDesde,
                cosechaHasta
        );

        return repository.consultarPorLinea(
                primerDiaMes(cosechaDesde),
                primerDiaMes(cosechaHasta),
                idAgencia,
                idLineaCredito
        );
    }

    // =========================================================
    // PRIMERA MORA
    // =========================================================

    public List<MoraTempranaPrimeraMoraDTO> consultarPrimeraMora(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        validarParametros(
                cosechaDesde,
                cosechaHasta,
                idAgencia,
                idLineaCredito
        );

        validarMadurezRango(
                cosechaDesde,
                cosechaHasta
        );

        return repository.consultarPrimeraMora(
                primerDiaMes(cosechaDesde),
                primerDiaMes(cosechaHasta),
                idAgencia,
                idLineaCredito
        );
    }

    // =========================================================
    // DETALLE
    // =========================================================

    public List<MoraTempranaDetalleDTO> consultarDetalle(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String indicador
    ) {

        validarParametros(
                cosechaDesde,
                cosechaHasta,
                idAgencia,
                idLineaCredito
        );

        validarMadurezRango(
                cosechaDesde,
                cosechaHasta
        );

        String indicadorNormalizado =
                normalizarIndicador(indicador);

        validarIndicador(
                indicadorNormalizado
        );

        return repository.consultarDetalle(
                primerDiaMes(cosechaDesde),
                primerDiaMes(cosechaHasta),
                idAgencia,
                idLineaCredito,
                indicadorNormalizado
        );
    }

    // =========================================================
    // VALIDACIONES GENERALES
    // =========================================================

    private void validarParametros(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        if (cosechaDesde == null) {
            throw new IllegalArgumentException(
                    "La cosecha inicial es obligatoria."
            );
        }

        if (cosechaHasta == null) {
            throw new IllegalArgumentException(
                    "La cosecha final es obligatoria."
            );
        }

        LocalDate desde =
                primerDiaMes(cosechaDesde);

        LocalDate hasta =
                primerDiaMes(cosechaHasta);

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La cosecha inicial no puede ser posterior a la cosecha final."
            );
        }

        if (idAgencia != null
                && idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de agencia no es válido."
            );
        }

        if (idLineaCredito != null
                && idLineaCredito <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de línea de crédito no es válido."
            );
        }
    }

    // =========================================================
    // VALIDACIÓN MADUREZ MOB 6
    // =========================================================

    private void validarMadurezRango(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta
    ) {

        LocalDate ultimoCorte =
                obtenerUltimoCorteDisponible();

        LocalDate ultimoMesMaduro =
                ultimoCorte
                        .minusMonths(6)
                        .withDayOfMonth(1);

        LocalDate desde =
                primerDiaMes(cosechaDesde);

        if (desde.isAfter(ultimoMesMaduro)) {
            throw new IllegalArgumentException(
                    "El rango seleccionado todavía no contiene cosechas con madurez suficiente para evaluar MOB 6."
            );
        }

        /*
         * No rechazamos el rango completo cuando cosechaHasta
         * supera el último mes maduro.
         *
         * El Repository excluye automáticamente las cosechas
         * que todavía no han alcanzado MOB 6.
         *
         * Esto permite que el usuario solicite, por ejemplo,
         * todo el año disponible sin generar falsos ceros
         * en las cosechas recientes.
         */
    }

    // =========================================================
    // INDICADOR
    // =========================================================

    private String normalizarIndicador(
            String indicador
    ) {

        if (indicador == null
                || indicador.isBlank()) {
            return INDICADOR_TODOS;
        }

        return indicador
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private void validarIndicador(
            String indicador
    ) {

        boolean valido =
                INDICADOR_TODOS.equals(indicador)
                        || INDICADOR_MORA30_MOB3.equals(indicador)
                        || INDICADOR_MORA30_MOB6.equals(indicador)
                        || INDICADOR_MORA60_MOB6.equals(indicador);

        if (!valido) {
            throw new IllegalArgumentException(
                    "Indicador de mora temprana no válido: "
                            + indicador
            );
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private LocalDate primerDiaMes(
            LocalDate fecha
    ) {

        return fecha.withDayOfMonth(1);
    }
}