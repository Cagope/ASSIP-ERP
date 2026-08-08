package co.assip.erp.gerencia.dashboard_cartera;

import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraRequestDTO;
import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class DashboardCarteraService {

    private final DashboardCarteraRepository repository;

    public DashboardCarteraService(
            DashboardCarteraRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // Consulta principal
    // =========================================================

    public DashboardCarteraResponseDTO consultar(
            DashboardCarteraRequestDTO request
    ) {

        DashboardCarteraRequestDTO filtro =
                normalizarFiltro(request);

        validarFiltro(filtro);

        DashboardCarteraResponseDTO respuesta =
                new DashboardCarteraResponseDTO();

        respuesta.setFechaCorte(
                filtro.getFechaCorte()
        );

        respuesta.setFechaDesde(
                filtro.getFechaDesde()
        );

        respuesta.setFechaHasta(
                filtro.getFechaHasta()
        );

        respuesta.setResumen(
                repository.consultarResumen(filtro)
        );

        /*
         * Distribución por edad de riesgo.
         *
         * La clasificación, descripción y orden se toman
         * directamente de:
         *
         * cartera.vw_cartera_creditos_total
         *
         * No se recalcula la edad de riesgo en el servicio
         * ni en el repositorio.
         */
        respuesta.setRiesgos(
                repository.consultarRiesgos(filtro)
        );

        /*
         * Distribución por edad de mora.
         *
         * La clasificación, descripción y orden se toman
         * directamente de:
         *
         * cartera.vw_cartera_creditos_total
         *
         * El filtro edadMora representa el código de la
         * clasificación de mora expuesto por la vista.
         */
        respuesta.setMoras(
                repository.consultarMoras(filtro)
        );

        respuesta.setLineas(
                repository.consultarLineas(filtro)
        );

        respuesta.setAgencias(
                repository.consultarAgencias(filtro)
        );

        respuesta.setRecaudos(
                repository.consultarRecaudos(filtro)
        );

        respuesta.setAlertas(
                repository.consultarAlertas(filtro)
        );

        return respuesta;
    }

    // =========================================================
    // Normalización
    // =========================================================

    private DashboardCarteraRequestDTO normalizarFiltro(
            DashboardCarteraRequestDTO request
    ) {

        DashboardCarteraRequestDTO filtro =
                new DashboardCarteraRequestDTO();

        LocalDate fechaActual =
                LocalDate.now();

        LocalDate fechaCorte =
                request != null
                        && request.getFechaCorte() != null
                        ? request.getFechaCorte()
                        : fechaActual;

        LocalDate fechaDesde =
                request != null
                        && request.getFechaDesde() != null
                        ? request.getFechaDesde()
                        : fechaCorte.withDayOfMonth(1);

        LocalDate fechaHasta =
                request != null
                        && request.getFechaHasta() != null
                        ? request.getFechaHasta()
                        : fechaCorte;

        filtro.setFechaCorte(
                fechaCorte
        );

        filtro.setFechaDesde(
                fechaDesde
        );

        filtro.setFechaHasta(
                fechaHasta
        );

        if (request == null) {
            return filtro;
        }

        filtro.setIdAgencia(
                request.getIdAgencia()
        );

        filtro.setIdLineaCredito(
                request.getIdLineaCredito()
        );

        filtro.setEdadRiesgo(
                normalizarCodigo(
                        request.getEdadRiesgo()
                )
        );

        filtro.setEdadMora(
                normalizarCodigo(
                        request.getEdadMora()
                )
        );

        filtro.setCodigoEstadoCartera(
                normalizarCodigo(
                        request.getCodigoEstadoCartera()
                )
        );

        filtro.setCodigoEstadoJuridico(
                normalizarCodigo(
                        request.getCodigoEstadoJuridico()
                )
        );

        filtro.setCodigoClasificacionCredito(
                normalizarCodigo(
                        request.getCodigoClasificacionCredito()
                )
        );

        filtro.setCodigoGarantiaCredito(
                normalizarCodigo(
                        request.getCodigoGarantiaCredito()
                )
        );

        return filtro;
    }

    private String normalizarTexto(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String texto =
                valor.trim();

        if (texto.isEmpty()) {
            return null;
        }

        return texto;
    }

    private String normalizarCodigo(
            String valor
    ) {

        String texto =
                normalizarTexto(valor);

        if (texto == null) {
            return null;
        }

        return texto.toUpperCase(
                Locale.ROOT
        );
    }

    // =========================================================
    // Validaciones
    // =========================================================

    private void validarFiltro(
            DashboardCarteraRequestDTO filtro
    ) {

        validarFechas(filtro);
        validarIds(filtro);

        validarLongitudCodigo(
                filtro.getEdadRiesgo(),
                "La edad de riesgo"
        );

        validarLongitudCodigo(
                filtro.getEdadMora(),
                "La edad de mora"
        );

        validarLongitudCodigo(
                filtro.getCodigoEstadoCartera(),
                "El código del estado de cartera"
        );

        validarLongitudCodigo(
                filtro.getCodigoEstadoJuridico(),
                "El código del estado jurídico"
        );

        validarLongitudCodigo(
                filtro.getCodigoClasificacionCredito(),
                "El código de clasificación del crédito"
        );

        validarLongitudCodigo(
                filtro.getCodigoGarantiaCredito(),
                "El código de garantía del crédito"
        );
    }

    private void validarFechas(
            DashboardCarteraRequestDTO filtro
    ) {

        LocalDate fechaActual =
                LocalDate.now();

        if (filtro.getFechaCorte() == null) {
            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }

        if (filtro.getFechaDesde() == null) {
            throw new IllegalArgumentException(
                    "La fecha inicial es obligatoria."
            );
        }

        if (filtro.getFechaHasta() == null) {
            throw new IllegalArgumentException(
                    "La fecha final es obligatoria."
            );
        }

        if (filtro.getFechaCorte()
                .isAfter(fechaActual)) {

            throw new IllegalArgumentException(
                    "La fecha de corte no puede ser posterior "
                            + "a la fecha actual."
            );
        }

        if (filtro.getFechaDesde()
                .isAfter(filtro.getFechaHasta())) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior "
                            + "a la fecha final."
            );
        }

        if (filtro.getFechaHasta()
                .isAfter(filtro.getFechaCorte())) {

            throw new IllegalArgumentException(
                    "La fecha final del período no puede ser "
                            + "posterior a la fecha de corte."
            );
        }
    }

    private void validarIds(
            DashboardCarteraRequestDTO filtro
    ) {

        if (filtro.getIdAgencia() != null
                && filtro.getIdAgencia() <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la agencia "
                            + "debe ser mayor que cero."
            );
        }

        if (filtro.getIdLineaCredito() != null
                && filtro.getIdLineaCredito() <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la línea de crédito "
                            + "debe ser mayor que cero."
            );
        }
    }

    private void validarLongitudCodigo(
            String valor,
            String nombreCampo
    ) {

        if (valor == null) {
            return;
        }

        if (valor.length() > 100) {
            throw new IllegalArgumentException(
                    nombreCampo
                            + " no puede superar los 100 caracteres."
            );
        }
    }
}