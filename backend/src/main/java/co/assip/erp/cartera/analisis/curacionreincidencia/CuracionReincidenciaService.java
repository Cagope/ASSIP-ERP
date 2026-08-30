package co.assip.erp.cartera.analisis.curacionreincidencia;

import co.assip.erp.cartera.analisis.curacionreincidencia.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CuracionReincidenciaService {

    private static final Set<String> EDADES_VALIDAS =
            Set.of("B", "C", "D", "E");

    private static final Set<String> INDICADORES_DETALLE =
            Set.of(
                    "TODOS",
                    "CURADOS",
                    "ABIERTOS",
                    "CURAS_MADURAS_6M",
                    "REINCIDENTES_6M",
                    "NO_REINCIDENTES_6M"
            );

    private final CuracionReincidenciaRepository repository;

    public CuracionReincidenciaService(
            CuracionReincidenciaRepository repository
    ) {
        this.repository = repository;
    }

    public CuracionReincidenciaControlDTO control() {
        CuracionReincidenciaControlDTO control = repository.control();

        if (control == null || control.getUltimoCorteDisponible() == null) {
            throw new IllegalStateException(
                    "No existen cortes históricos disponibles para Curación y Reincidencia."
            );
        }

        LocalDate ultimoMes =
                control.getUltimoCorteDisponible().withDayOfMonth(1);

        LocalDate primerMes =
                control.getPrimerCorteDisponible() != null
                        ? control.getPrimerCorteDisponible().withDayOfMonth(1)
                        : ultimoMes;

        LocalDate sugeridoDesde = ultimoMes.minusMonths(23);
        if (sugeridoDesde.isBefore(primerMes)) {
            sugeridoDesde = primerMes;
        }

        control.setPeriodoDesdeSugerido(sugeridoDesde);
        control.setPeriodoHastaSugerido(ultimoMes);
        control.setMesesSeguimientoReincidencia(6);

        return control;
    }

    public CuracionReincidenciaResumenDTO resumen(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        Parametros p = parametros(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );

        CuracionReincidenciaResumenDTO dto =
                repository.resumen(
                        p.periodoDesde(),
                        p.periodoHastaExclusivo(),
                        p.idAgencia(),
                        p.idLineaCredito(),
                        p.edadEntrada()
                );

        if (dto != null) {
            dto.setPeriodoDesde(p.periodoDesde());
            dto.setPeriodoHasta(p.periodoHasta());
        }

        return dto;
    }

    public List<CuracionReincidenciaPeriodoDTO> periodos(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        Parametros p = parametros(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );

        return repository.periodos(
                p.periodoDesde(),
                p.periodoHastaExclusivo(),
                p.idAgencia(),
                p.idLineaCredito(),
                p.edadEntrada()
        );
    }

    public List<CuracionReincidenciaSegmentoDTO> agencias(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        Parametros p = parametros(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );

        return repository.agencias(
                p.periodoDesde(),
                p.periodoHastaExclusivo(),
                p.idAgencia(),
                p.idLineaCredito(),
                p.edadEntrada()
        );
    }

    public List<CuracionReincidenciaSegmentoDTO> lineas(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        Parametros p = parametros(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );

        return repository.lineas(
                p.periodoDesde(),
                p.periodoHastaExclusivo(),
                p.idAgencia(),
                p.idLineaCredito(),
                p.edadEntrada()
        );
    }

    public List<CuracionReincidenciaEdadEntradaDTO> edadesEntrada(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        Parametros p = parametros(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );

        return repository.edadesEntrada(
                p.periodoDesde(),
                p.periodoHastaExclusivo(),
                p.idAgencia(),
                p.idLineaCredito(),
                p.edadEntrada()
        );
    }

    public List<CuracionReincidenciaDistribucionCuraDTO> distribucionCura(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        Parametros p = parametros(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );

        return repository.distribucionCura(
                p.periodoDesde(),
                p.periodoHastaExclusivo(),
                p.idAgencia(),
                p.idLineaCredito(),
                p.edadEntrada()
        );
    }

    public List<CuracionReincidenciaPrimeraReincidenciaDTO> primeraReincidencia(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        Parametros p = parametros(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );

        return repository.primeraReincidencia(
                p.periodoDesde(),
                p.periodoHastaExclusivo(),
                p.idAgencia(),
                p.idLineaCredito(),
                p.edadEntrada()
        );
    }

    public List<CuracionReincidenciaDetalleDTO> detalle(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada,
            String indicador
    ) {
        Parametros p = parametros(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );

        String indicadorNormalizado = normalizarIndicador(indicador);

        return repository.detalle(
                p.periodoDesde(),
                p.periodoHastaExclusivo(),
                p.idAgencia(),
                p.idLineaCredito(),
                p.edadEntrada(),
                indicadorNormalizado
        );
    }

    private Parametros parametros(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        CuracionReincidenciaControlDTO control = control();

        LocalDate desde =
                periodoDesde != null
                        ? periodoDesde.withDayOfMonth(1)
                        : control.getPeriodoDesdeSugerido();

        LocalDate hasta =
                periodoHasta != null
                        ? periodoHasta.withDayOfMonth(1)
                        : control.getPeriodoHastaSugerido();

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "El periodo desde no puede ser posterior al periodo hasta."
            );
        }

        LocalDate primerDisponible =
                control.getPrimerCorteDisponible().withDayOfMonth(1);

        LocalDate ultimoDisponible =
                control.getUltimoCorteDisponible().withDayOfMonth(1);

        if (desde.isBefore(primerDisponible)) {
            throw new IllegalArgumentException(
                    "El periodo desde es anterior al primer histórico disponible: "
                            + primerDisponible
            );
        }

        if (hasta.isAfter(ultimoDisponible)) {
            throw new IllegalArgumentException(
                    "El periodo hasta es posterior al último histórico disponible: "
                            + ultimoDisponible
            );
        }

        if (idAgencia != null && idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "El id de agencia debe ser mayor que cero."
            );
        }

        if (idLineaCredito != null && idLineaCredito <= 0) {
            throw new IllegalArgumentException(
                    "El id de línea de crédito debe ser mayor que cero."
            );
        }

        String edad = normalizarEdad(edadEntrada);

        return new Parametros(
                desde,
                hasta,
                hasta.plusMonths(1),
                idAgencia,
                idLineaCredito,
                edad
        );
    }

    private String normalizarEdad(String edadEntrada) {
        if (edadEntrada == null || edadEntrada.isBlank()) {
            return null;
        }

        String edad =
                edadEntrada.trim().toUpperCase(Locale.ROOT);

        if (!EDADES_VALIDAS.contains(edad)) {
            throw new IllegalArgumentException(
                    "Edad de entrada inválida. Valores permitidos: B, C, D, E."
            );
        }

        return edad;
    }

    private String normalizarIndicador(String indicador) {
        String valor =
                indicador == null || indicador.isBlank()
                        ? "TODOS"
                        : indicador.trim().toUpperCase(Locale.ROOT);

        if (!INDICADORES_DETALLE.contains(valor)) {
            throw new IllegalArgumentException(
                    "Indicador de detalle inválido. Valores permitidos: "
                            + String.join(", ", INDICADORES_DETALLE)
            );
        }

        return valor;
    }

    private record Parametros(
            LocalDate periodoDesde,
            LocalDate periodoHasta,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
    }
}
