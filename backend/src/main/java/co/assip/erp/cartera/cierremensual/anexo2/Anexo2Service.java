package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Anexo2Service {

    private final Anexo2Repository repository;

    @Transactional
    public ResultadoPreparacionAnexo2 preparar(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        // =========================================================
        // VALIDAR CIERRE
        // =========================================================

        if (!repository.existeCierre(idCierreCartera)) {
            throw new IllegalArgumentException(
                    "No existe el cierre de cartera: " + idCierreCartera
            );
        }

        // =========================================================
        // INICIAR / REINICIAR PROCESO PE
        // =========================================================

        Integer idPeProceso =
                repository.iniciarProceso(
                        idCierreCartera,
                        idUsuario
                );

        // =========================================================
        // CREAR POBLACIÓN ANEXO 2
        // =========================================================

        int poblacion =
                repository.crearPoblacionTemporal(
                        idCierreCartera
                );

        if (poblacion <= 0) {
            throw new IllegalStateException(
                    "No existen créditos que apliquen al Anexo 2."
            );
        }

        // =========================================================
        // CREAR MATRIZ HISTÓRICA DE MORAS
        // =========================================================

        long moras =
                repository.crearMorasTemporales(
                        idCierreCartera
                );

        int periodos =
                repository.cantidadPeriodosMora();

        int maxPeriodo =
                repository.maximoPeriodoMora();

        int cortesHistoricos =
                repository.cantidadCortesHistoricos();

        boolean corte40Valido =
                repository.validarCorte40(
                        idCierreCartera
                );

        long morasEsperadas =
                (long) poblacion * 40L;

        // =========================================================
        // VALIDAR MATRIZ DE MORAS
        // =========================================================

        if (periodos != 40) {
            throw new IllegalStateException(
                    "La matriz PE no contiene 40 periodos. " +
                            "Periodos encontrados: " + periodos
            );
        }

        if (maxPeriodo != 40) {
            throw new IllegalStateException(
                    "El periodo máximo de mora no es 40. " +
                            "Periodo encontrado: " + maxPeriodo
            );
        }

        if (cortesHistoricos != 40) {
            throw new IllegalStateException(
                    "No existen 40 cierres históricos disponibles. " +
                            "Cierres encontrados: " + cortesHistoricos
            );
        }

        if (!corte40Valido) {
            throw new IllegalStateException(
                    "El corte 40 no corresponde al cierre que se está procesando."
            );
        }

        if (moras != morasEsperadas) {
            throw new IllegalStateException(
                    "La cantidad de registros de mora no coincide. " +
                            "Esperados: " + morasEsperadas +
                            ", encontrados: " + moras
            );
        }

        // =========================================================
        // CREAR VARIABLES PE
        // =========================================================

        int variables =
                repository.crearVariablesTemporales();

        if (variables != poblacion) {
            throw new IllegalStateException(
                    "La cantidad de variables PE no coincide con la población. " +
                            "Población: " + poblacion +
                            ", variables: " + variables
            );
        }

        // =========================================================
        // Z, PUNTAJE Y CALIFICACIÓN DEL MODELO
        // =========================================================

        int modelosCalculados =
                repository.calcularZPuntajeCalificacion();

        if (modelosCalculados != poblacion) {
            throw new IllegalStateException(
                    "La cantidad de créditos con modelo calculado no coincide. " +
                            "Población: " + poblacion +
                            ", calculados: " + modelosCalculados
            );
        }

        // =========================================================
        // DEFAULT, EDAD DE DETERIORO Y PI
        // =========================================================

        int deteriorosCalculados =
                repository.calcularDefaultEdadDeterioroYPi();

        if (deteriorosCalculados != poblacion) {
            throw new IllegalStateException(
                    "La cantidad de créditos con deterioro/PI no coincide. " +
                            "Población: " + poblacion +
                            ", calculados: " + deteriorosCalculados
            );
        }

        // =========================================================
        // CALCULAR VEA
        // =========================================================

        int veaCalculados =
                repository.calcularVea(
                        idCierreCartera
                );

        if (veaCalculados != poblacion) {
            throw new IllegalStateException(
                    "La cantidad de créditos con VEA calculado no coincide. " +
                            "Población: " + poblacion +
                            ", calculados: " + veaCalculados
            );
        }

        // =========================================================
        // CALCULAR PDI Y PÉRDIDA ESPERADA
        // =========================================================

        int perdidasCalculadas =
                repository.calcularPdiYPerdida();

        if (perdidasCalculadas != poblacion) {
            throw new IllegalStateException(
                    "La cantidad de créditos con PDI/PE calculada no coincide. " +
                            "Población: " + poblacion +
                            ", calculados: " + perdidasCalculadas
            );
        }

        // =========================================================
        // HOMOLOGACIÓN Y ALINEACIÓN
        // =========================================================

        int homologados =
                repository.calcularHomologacionYAlineacion();

        if (homologados != poblacion) {
            throw new IllegalStateException(
                    "La cantidad de créditos homologados/alineados no coincide. " +
                            "Población: " + poblacion +
                            ", calculados: " + homologados
            );
        }

        int resultadosPersistidos =
                repository.persistirResultadosPe(
                        idCierreCartera,
                        idUsuario
                );

        if (resultadosPersistidos != poblacion) {
            throw new IllegalStateException(
                    "La cantidad de resultados PE persistidos no coincide. " +
                            "Población: " + poblacion +
                            ", persistidos: " + resultadosPersistidos
            );
        }

        var muestraVariables =
                repository.consultarVariablesMuestra();

        return new ResultadoPreparacionAnexo2(
                idPeProceso,
                idCierreCartera,
                poblacion,
                moras,
                morasEsperadas,
                periodos,
                maxPeriodo,
                cortesHistoricos,
                corte40Valido,
                variables,
                modelosCalculados,
                deteriorosCalculados,
                veaCalculados,
                perdidasCalculadas,
                homologados,
                muestraVariables
        );
    }

    public record ResultadoPreparacionAnexo2(
            Integer idPeProceso,
            Integer idCierreCartera,
            Integer poblacion,
            Long moras,
            Long morasEsperadas,
            Integer periodos,
            Integer maxPeriodo,
            Integer cortesHistoricos,
            Boolean corte40Valido,
            Integer variables,
            Integer modelosCalculados,
            Integer deteriorosCalculados,
            Integer veaCalculados,
            Integer perdidasCalculadas,
            Integer homologados,
            java.util.List<java.util.Map<String, Object>> muestraVariables
    ) {
    }
}