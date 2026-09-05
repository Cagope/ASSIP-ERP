package co.assip.erp.cartera.cierremensual.anexo2;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.assip.erp.cartera.cierremensual.anexo2.dto.DetalleAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.MoraAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.ResumenAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.TrabajoAnexo2DTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Anexo2Service {

    private final Anexo2Repository repository;
    private final Anexo2ConsultaRepository consultaRepository;
    private final CierreMensualRepository cierreRepository;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // PREPARAR / EJECUTAR ANEXO 2
    //
    // Secuencia:
    //
    // 1. validar cierre
    // 2. obtener usuario de sesión
    // 3. iniciar / reiniciar proceso PE
    // 4. crear población PE
    // 5. crear matriz histórica de 40 moras
    // 6. crear variables PE
    // 7. calcular Z, puntaje y calificación
    // 8. calcular default, edad deterioro y PI
    // 9. calcular VEA
    // 10. calcular PDI y pérdida esperada
    // 11. homologar y alinear
    // 12. persistir resultados PE
    // 13. finalizar proceso PE
    //
    // IMPORTANTE:
    //
    // - El cierre general debe estar en estado C.
    // - El usuario NO llega desde el frontend.
    // - El usuario se obtiene de la sesión autenticada.
    // - Si cualquier etapa falla, la transacción completa
    //   realiza rollback.
    // - El proceso PE solamente queda FINALIZADO después
    //   de persistir correctamente todos los resultados.
    //
    // =========================================================

    @Transactional
    public ResultadoPreparacionAnexo2 preparar(
            Integer idCierreCartera
    ) {

        // =====================================================
        // 1. VALIDAR ID DEL CIERRE
        // =====================================================

        validarIdCierre(
                idCierreCartera
        );


        // =====================================================
        // 2. VALIDAR CIERRE GENERAL
        //
        // C = fotografía cerrada en firme.
        // =====================================================

        validarCierreCerrado(
                idCierreCartera
        );


        // =====================================================
        // 3. OBTENER USUARIO DE SESIÓN
        //
        // Nunca se recibe desde el frontend.
        // =====================================================

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        if (idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalStateException(
                    "No fue posible identificar el usuario de sesión."
            );
        }


        // =====================================================
        // 4. VALIDAR EXISTENCIA PARA MOTOR PE
        // =====================================================

        if (!repository.existeCierre(
                idCierreCartera
        )) {

            throw new IllegalArgumentException(
                    "No existe el cierre de cartera: "
                            + idCierreCartera
            );
        }


        // =====================================================
        // 5. INICIAR / REINICIAR PROCESO PE
        //
        // El cierre general permanece C.
        //
        // El proceso interno PE queda PROCESANDO.
        //
        // fk_seguridad_creacion se toma del usuario
        // autenticado.
        // =====================================================

        Integer idPeProceso =
                repository.iniciarProceso(
                        idCierreCartera,
                        idUsuario
                );


        // =====================================================
        // 6. CREAR POBLACIÓN ANEXO 2
        // =====================================================

        int poblacion =
                repository.crearPoblacionTemporal(
                        idCierreCartera
                );

        if (poblacion <= 0) {

            throw new IllegalStateException(
                    "No existen créditos que apliquen al Anexo 2."
            );
        }


        // =====================================================
        // 7. CREAR MATRIZ HISTÓRICA DE MORAS
        // =====================================================

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


        // =====================================================
        // 8. VALIDAR MATRIZ DE MORAS
        // =====================================================

        if (periodos != 40) {

            throw new IllegalStateException(
                    "La matriz PE no contiene 40 periodos. "
                            + "Periodos encontrados: "
                            + periodos
            );
        }

        if (maxPeriodo != 40) {

            throw new IllegalStateException(
                    "El periodo máximo de mora no es 40. "
                            + "Periodo encontrado: "
                            + maxPeriodo
            );
        }

        if (cortesHistoricos != 40) {

            throw new IllegalStateException(
                    "No existen 40 cierres históricos disponibles. "
                            + "Cierres encontrados: "
                            + cortesHistoricos
            );
        }

        if (!corte40Valido) {

            throw new IllegalStateException(
                    "El corte 40 no corresponde al cierre "
                            + "que se está procesando."
            );
        }

        if (moras != morasEsperadas) {

            throw new IllegalStateException(
                    "La cantidad de registros de mora no coincide. "
                            + "Esperados: "
                            + morasEsperadas
                            + ", encontrados: "
                            + moras
            );
        }


        // =====================================================
        // 9. CREAR VARIABLES PE
        // =====================================================

        int variables =
                repository.crearVariablesTemporales();

        if (variables != poblacion) {

            throw new IllegalStateException(
                    "La cantidad de variables PE no coincide "
                            + "con la población. "
                            + "Población: "
                            + poblacion
                            + ", variables: "
                            + variables
            );
        }


        // =====================================================
        // 10. Z, PUNTAJE Y CALIFICACIÓN DEL MODELO
        // =====================================================

        int modelosCalculados =
                repository.calcularZPuntajeCalificacion();

        if (modelosCalculados != poblacion) {

            throw new IllegalStateException(
                    "La cantidad de créditos con modelo calculado "
                            + "no coincide. "
                            + "Población: "
                            + poblacion
                            + ", calculados: "
                            + modelosCalculados
            );
        }


        // =====================================================
        // 11. DEFAULT, EDAD DE DETERIORO Y PI
        // =====================================================

        int deteriorosCalculados =
                repository.calcularDefaultEdadDeterioroYPi();

        if (deteriorosCalculados != poblacion) {

            throw new IllegalStateException(
                    "La cantidad de créditos con deterioro/PI "
                            + "no coincide. "
                            + "Población: "
                            + poblacion
                            + ", calculados: "
                            + deteriorosCalculados
            );
        }


        // =====================================================
        // 12. CALCULAR VEA
        // =====================================================

        int veaCalculados =
                repository.calcularVea(
                        idCierreCartera
                );

        if (veaCalculados != poblacion) {

            throw new IllegalStateException(
                    "La cantidad de créditos con VEA calculado "
                            + "no coincide. "
                            + "Población: "
                            + poblacion
                            + ", calculados: "
                            + veaCalculados
            );
        }


        // =====================================================
        // 13. CALCULAR PDI Y PÉRDIDA ESPERADA
        // =====================================================

        int perdidasCalculadas =
                repository.calcularPdiYPerdida();

        if (perdidasCalculadas != poblacion) {

            throw new IllegalStateException(
                    "La cantidad de créditos con PDI/PE calculada "
                            + "no coincide. "
                            + "Población: "
                            + poblacion
                            + ", calculados: "
                            + perdidasCalculadas
            );
        }


        // =====================================================
        // 14. HOMOLOGACIÓN Y ALINEACIÓN
        // =====================================================

        int homologados =
                repository.calcularHomologacionYAlineacion();

        if (homologados != poblacion) {

            throw new IllegalStateException(
                    "La cantidad de créditos homologados/alineados "
                            + "no coincide. "
                            + "Población: "
                            + poblacion
                            + ", calculados: "
                            + homologados
            );
        }


        // =====================================================
        // 15. PERSISTIR RESULTADOS DEFINITIVOS PE
        //
        // El mismo usuario autenticado se utiliza para la
        // auditoría de los resultados.
        // =====================================================

        int resultadosPersistidos =
                repository.persistirResultadosPe(
                        idCierreCartera,
                        idUsuario
                );

        if (resultadosPersistidos != poblacion) {

            throw new IllegalStateException(
                    "La cantidad de resultados PE persistidos "
                            + "no coincide. "
                            + "Población: "
                            + poblacion
                            + ", persistidos: "
                            + resultadosPersistidos
            );
        }


        // =====================================================
        // 16. FINALIZAR PROCESO PE
        //
        // Solamente llegamos aquí si:
        //
        // - población válida
        // - 40 períodos completos
        // - variables completas
        // - modelo calculado
        // - PI calculada
        // - VEA calculado
        // - PDI calculada
        // - pérdida esperada calculada
        // - homologación completada
        // - resultados persistidos
        //
        // Por tanto el proceso PE queda FINALIZADO.
        //
        // Si este UPDATE falla, @Transactional revierte
        // también toda la ejecución del Anexo 2.
        // =====================================================

        repository.finalizarProceso(
                idPeProceso
        );


        // =====================================================
        // 17. RESULTADO
        // =====================================================

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
                resultadosPersistidos
        );
    }


    // =========================================================
    // VALIDAR CIERRE CERRADO
    //
    // C = fotografía cerrada en firme.
    //
    // Anexo 2 solamente puede ejecutarse después de:
    //
    // - cerrar la fotografía
    // - intereses
    // - seguros
    // - alivios
    // - cálculos previos
    // - Anexo 1
    //
    // =========================================================

    // =========================================================
// CONSULTAS ANEXO 2
// =========================================================

    @Transactional(readOnly = true)
    public List<Integer> obtenerModelosDelCierre(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        validarCierreCerrado(
                idCierreCartera
        );

        return consultaRepository.obtenerModelosDelCierre(
                idCierreCartera
        );
    }


// =========================================================
// HOJA: RESULTADO
// =========================================================

    @Transactional(readOnly = true)
    public List<DetalleAnexo2DTO> obtenerDetalle(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        validarParametrosConsulta(
                idCierreCartera,
                idModeloPe
        );

        validarCierreCerrado(
                idCierreCartera
        );

        return consultaRepository.obtenerDetalle(
                idCierreCartera,
                idModeloPe
        );
    }


// =========================================================
// HOJA: HOJA_TRABAJO
// =========================================================

    @Transactional(readOnly = true)
    public List<TrabajoAnexo2DTO> obtenerTrabajo(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        validarParametrosConsulta(
                idCierreCartera,
                idModeloPe
        );

        validarCierreCerrado(
                idCierreCartera
        );

        return consultaRepository.obtenerTrabajo(
                idCierreCartera,
                idModeloPe
        );
    }


// =========================================================
// HOJA: MORA
// =========================================================

    @Transactional(readOnly = true)
    public List<MoraAnexo2DTO> obtenerMora(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        validarParametrosConsulta(
                idCierreCartera,
                idModeloPe
        );

        validarCierreCerrado(
                idCierreCartera
        );

        return consultaRepository.obtenerMora(
                idCierreCartera,
                idModeloPe
        );
    }


// =========================================================
// HOJA: RESUMEN
// =========================================================

    @Transactional(readOnly = true)
    public List<ResumenAnexo2DTO> obtenerResumen(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        validarParametrosConsulta(
                idCierreCartera,
                idModeloPe
        );

        validarCierreCerrado(
                idCierreCartera
        );

        return consultaRepository.obtenerResumen(
                idCierreCartera,
                idModeloPe
        );
    }


    // =========================================================
    // VALIDAR PARÁMETROS DE CONSULTA
    // =========================================================

    private void validarParametrosConsulta(
            Integer idCierreCartera,
            Integer idModeloPe
    ) {

        validarIdCierre(
                idCierreCartera
        );

        if (idModeloPe == null
                || idModeloPe <= 0) {

            throw new IllegalArgumentException(
                    "El modelo PE es obligatorio."
            );
        }
    }

    private void validarCierreCerrado(
            Integer idCierreCartera
    ) {

        CierreMensualDTO cierre =
                cierreRepository
                        .buscarPorId(
                                idCierreCartera
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el cierre de cartera: "
                                                + idCierreCartera
                                )
                        );

        String estado =
                cierre.getEstadoCierre();

        if (estado == null
                || !"C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " debe estar cerrado en firme "
                            + "para ejecutar el Anexo 2."
            );
        }
    }


    // =========================================================
    // VALIDAR ID DEL CIERRE
    // =========================================================

    private void validarIdCierre(
            Integer idCierreCartera
    ) {

        if (idCierreCartera == null
                || idCierreCartera <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre de cartera es obligatorio."
            );
        }
    }


    // =========================================================
    // RESULTADO
    // =========================================================

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
            Integer resultadosPersistidos
    ) {
    }
}