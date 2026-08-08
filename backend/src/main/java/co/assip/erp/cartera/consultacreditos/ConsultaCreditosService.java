package co.assip.erp.cartera.consultacreditos;

import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoAlivioDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoDetalleDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoEvaluacionDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoExtractoDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoIntegralDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoInteresDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoResumenDTO;
import co.assip.erp.cartera.consultacreditos.dto.ConsultaCreditoSeguroDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de consulta especializada de créditos.
 *
 * Permite consultar:
 *
 * - Todos los créditos de un asociado, incluidos los saldados.
 * - Detalle funcional de un crédito.
 * - Extracto de pagos.
 * - Configuraciones de seguro.
 * - Alivios.
 * - Intereses causados.
 * - Historial de evaluaciones.
 * - Última evaluación de cartera.
 * - Consulta integral del crédito.
 *
 * Este servicio es exclusivamente de lectura.
 */
@Service
@Transactional(readOnly = true)
public class ConsultaCreditosService {

    private final ConsultaCreditosRepository repository;

    public ConsultaCreditosService(
            ConsultaCreditosRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // Créditos del asociado
    // =========================================================

    /**
     * Lista todos los créditos relacionados con el asociado.
     *
     * No se filtra por saldo ni por estado. Por tanto, incluye:
     *
     * - Créditos vigentes.
     * - Créditos con saldo.
     * - Créditos saldados.
     * - Créditos cancelados.
     * - Créditos en mora.
     * - Créditos en cobro jurídico.
     */
    public List<ConsultaCreditoResumenDTO> listarPorPersona(
            Integer idDatosPersonal
    ) {

        validarIdDatosPersonal(
                idDatosPersonal
        );

        return repository.listarPorPersona(
                idDatosPersonal
        );
    }

    // =========================================================
    // Detalle del crédito
    // =========================================================

    /**
     * Busca el detalle funcional del crédito.
     *
     * Si el crédito no existe, responde con HTTP 404.
     */
    public ConsultaCreditoDetalleDTO buscarPorId(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return repository.buscarPorId(
                        idCarteraCredito
                )
                .orElseThrow(
                        () -> creditoNoEncontrado(
                                idCarteraCredito
                        )
                );
    }

    // =========================================================
    // Extracto
    // =========================================================

    /**
     * Lista todos los movimientos registrados en el extracto.
     *
     * El crédito puede estar vigente o saldado.
     */
    public List<ConsultaCreditoExtractoDTO> listarExtracto(
            Integer idCarteraCredito
    ) {

        validarExistenciaCredito(
                idCarteraCredito
        );

        return repository.listarExtracto(
                idCarteraCredito
        );
    }

    // =========================================================
    // Seguros
    // =========================================================

    /**
     * Lista las configuraciones actuales e históricas
     * de seguro relacionadas con el crédito.
     */
    public List<ConsultaCreditoSeguroDTO> listarSeguros(
            Integer idCarteraCredito
    ) {

        validarExistenciaCredito(
                idCarteraCredito
        );

        return repository.listarSeguros(
                idCarteraCredito
        );
    }

    // =========================================================
    // Alivios
    // =========================================================

    /**
     * Lista los alivios y sus períodos registrados
     * para el crédito.
     */
    public List<ConsultaCreditoAlivioDTO> listarAlivios(
            Integer idCarteraCredito
    ) {

        validarExistenciaCredito(
                idCarteraCredito
        );

        return repository.listarAlivios(
                idCarteraCredito
        );
    }

    // =========================================================
    // Intereses causados
    // =========================================================

    /**
     * Lista las causaciones de intereses registradas
     * para el crédito.
     */
    public List<ConsultaCreditoInteresDTO> listarInteresesCausados(
            Integer idCarteraCredito
    ) {

        validarExistenciaCredito(
                idCarteraCredito
        );

        return repository.listarInteresesCausados(
                idCarteraCredito
        );
    }

    // =========================================================
    // Evaluaciones
    // =========================================================

    /**
     * Lista el historial completo de evaluaciones
     * realizadas al crédito.
     */
    public List<ConsultaCreditoEvaluacionDTO> listarEvaluaciones(
            Integer idCarteraCredito
    ) {

        validarExistenciaCredito(
                idCarteraCredito
        );

        return repository.listarEvaluaciones(
                idCarteraCredito
        );
    }

    /**
     * Busca la última evaluación registrada.
     *
     * Se retorna Optional porque un crédito puede no haber sido
     * evaluado todavía.
     */
    public Optional<ConsultaCreditoEvaluacionDTO> buscarUltimaEvaluacion(
            Integer idCarteraCredito
    ) {

        validarExistenciaCredito(
                idCarteraCredito
        );

        return repository.buscarUltimaEvaluacion(
                idCarteraCredito
        );
    }

    // =========================================================
    // Consulta integral
    // =========================================================

    /**
     * Consolida toda la información disponible del crédito.
     *
     * No realiza cálculos ni transforma valores provenientes
     * de las vistas de cartera.
     */
    public ConsultaCreditoIntegralDTO consultarIntegral(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        /*
         * La búsqueda del detalle valida también que el crédito
         * exista. Así se evita ejecutar las demás consultas cuando
         * el identificador no corresponde a un crédito registrado.
         */
        ConsultaCreditoDetalleDTO credito =
                buscarPorId(
                        idCarteraCredito
                );

        ConsultaCreditoIntegralDTO integral =
                new ConsultaCreditoIntegralDTO();

        integral.setCredito(
                credito
        );

        integral.setExtracto(
                repository.listarExtracto(
                        idCarteraCredito
                )
        );

        integral.setSeguros(
                repository.listarSeguros(
                        idCarteraCredito
                )
        );

        integral.setAlivios(
                repository.listarAlivios(
                        idCarteraCredito
                )
        );

        integral.setInteresesCausados(
                repository.listarInteresesCausados(
                        idCarteraCredito
                )
        );

        integral.setEvaluaciones(
                repository.listarEvaluaciones(
                        idCarteraCredito
                )
        );

        integral.setUltimaEvaluacion(
                repository.buscarUltimaEvaluacion(
                                idCarteraCredito
                        )
                        .orElse(null)
        );

        return integral;
    }

    // =========================================================
    // Existencia
    // =========================================================

    public boolean existeCredito(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        return repository.existeCredito(
                idCarteraCredito
        );
    }

    // =========================================================
    // Validaciones internas
    // =========================================================

    private void validarExistenciaCredito(
            Integer idCarteraCredito
    ) {

        validarIdCarteraCredito(
                idCarteraCredito
        );

        if (
                !repository.existeCredito(
                        idCarteraCredito
                )
        ) {
            throw creditoNoEncontrado(
                    idCarteraCredito
            );
        }
    }

    private void validarIdDatosPersonal(
            Integer idDatosPersonal
    ) {

        if (
                idDatosPersonal == null
                        || idDatosPersonal <= 0
        ) {
            throw new IllegalArgumentException(
                    "El idDatosPersonal debe ser mayor que cero."
            );
        }
    }

    private void validarIdCarteraCredito(
            Integer idCarteraCredito
    ) {

        if (
                idCarteraCredito == null
                        || idCarteraCredito <= 0
        ) {
            throw new IllegalArgumentException(
                    "El idCarteraCredito debe ser mayor que cero."
            );
        }
    }

    private ResponseStatusException creditoNoEncontrado(
            Integer idCarteraCredito
    ) {

        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se encontró el crédito con id "
                        + idCarteraCredito
                        + "."
        );
    }
}