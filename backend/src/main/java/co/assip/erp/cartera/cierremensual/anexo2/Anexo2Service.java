package co.assip.erp.cartera.cierremensual.anexo2;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.anexo2.dto.DetalleAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.MoraAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.ResumenAnexo2DTO;
import co.assip.erp.cartera.cierremensual.anexo2.dto.TrabajoAnexo2DTO;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // Requisitos:
    //
    // fotografía = C
    // cálculos    = C
    // Anexo 1     = C
    // Anexo 2     = P o E
    //
    // El estado general del cierre permanece P.
    //
    // Al iniciar:
    //
    // estado_anexo2 = E
    //
    // NO se deja en firme desde este método.
    // =========================================================

    @Transactional
    public ResultadoPreparacionAnexo2 preparar(
            Integer idCierreCartera
    ) {

        // =====================================================
        // 1. VALIDAR ID
        // =====================================================

        validarIdCierre(
                idCierreCartera
        );


        // =====================================================
        // 2. OBTENER CIERRE
        // =====================================================

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );


        // =====================================================
        // 3. VALIDAR DEPENDENCIAS
        // =====================================================

        validarDependenciasAnexo2(
                cierre
        );


        // =====================================================
        // 4. VALIDAR QUE ANEXO 2 PUEDA PROCESARSE
        // =====================================================

        validarAnexo2Procesable(
                cierre
        );


        // =====================================================
        // 5. OBTENER USUARIO DE SESIÓN
        // =====================================================

        Integer idUsuario =
                obtenerUsuarioSesion();


        // =====================================================
        // 6. VALIDAR EXISTENCIA DEL CIERRE PARA MOTOR PE
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
        // 7. INICIAR ETAPA ANEXO 2
        //
        // P -> E
        //
        // Si ya estaba E, permite recalcular conservando
        // fecha_anexo2_inicio.
        // =====================================================

        int etapaIniciada =
                cierreRepository.iniciarAnexo2(
                        idCierreCartera,
                        idUsuario
                );

        if (etapaIniciada != 1) {

            throw new IllegalStateException(
                    "No fue posible iniciar la etapa de Anexo 2 "
                            + "del cierre "
                            + idCierreCartera
                            + "."
            );
        }


        // =====================================================
        // 8. INICIAR / REINICIAR PROCESO PE INTERNO
        // =====================================================

        Integer idPeProceso =
                repository.iniciarProceso(
                        idCierreCartera,
                        idUsuario
                );

        if (idPeProceso == null
                || idPeProceso <= 0) {

            throw new IllegalStateException(
                    "No fue posible iniciar el proceso interno "
                            + "de pérdida esperada para el cierre "
                            + idCierreCartera
                            + "."
            );
        }


        // =====================================================
        // 9. CREAR POBLACIÓN ANEXO 2
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
        // 10. CREAR MATRIZ HISTÓRICA DE MORAS
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
        // 11. VALIDAR MATRIZ DE MORAS
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
        // 12. CREAR VARIABLES PE
        // =====================================================

        int variables =
                repository.crearVariablesTemporales();

        validarCantidad(
                "variables PE",
                poblacion,
                variables
        );


        // =====================================================
        // 13. Z, PUNTAJE Y CALIFICACIÓN
        // =====================================================

        int modelosCalculados =
                repository.calcularZPuntajeCalificacion();

        validarCantidad(
                "créditos con modelo calculado",
                poblacion,
                modelosCalculados
        );


        // =====================================================
        // 14. DEFAULT, EDAD DE DETERIORO Y PI
        // =====================================================

        int deteriorosCalculados =
                repository.calcularDefaultEdadDeterioroYPi();

        validarCantidad(
                "créditos con deterioro/PI calculado",
                poblacion,
                deteriorosCalculados
        );


        // =====================================================
        // 15. VEA
        // =====================================================

        int veaCalculados =
                repository.calcularVea(
                        idCierreCartera
                );

        validarCantidad(
                "créditos con VEA calculado",
                poblacion,
                veaCalculados
        );


        // =====================================================
        // 16. PDI Y PÉRDIDA ESPERADA
        // =====================================================

        int perdidasCalculadas =
                repository.calcularPdiYPerdida();

        validarCantidad(
                "créditos con PDI/pérdida esperada calculada",
                poblacion,
                perdidasCalculadas
        );


        // =====================================================
        // 17. HOMOLOGACIÓN Y ALINEACIÓN
        // =====================================================

        int homologados =
                repository.calcularHomologacionYAlineacion();

        validarCantidad(
                "créditos homologados/alineados",
                poblacion,
                homologados
        );


        // =====================================================
        // 18. PERSISTIR RESULTADOS PE
        // =====================================================

        int resultadosPersistidos =
                repository.persistirResultadosPe(
                        idCierreCartera,
                        idUsuario
                );

        validarCantidad(
                "resultados PE persistidos",
                poblacion,
                resultadosPersistidos
        );


        // =====================================================
        // 19. FINALIZAR PROCESO PE INTERNO
        //
        // IMPORTANTE:
        //
        // Esto finaliza el proceso técnico PE.
        //
        // NO cierra todavía estado_anexo2.
        //
        // estado_anexo2 permanece E para revisión,
        // Excel y comparación.
        // =====================================================

        repository.finalizarProceso(
                idPeProceso
        );


        // =====================================================
        // 20. RESULTADO
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
    // CERRAR ANEXO 2 EN FIRME
    //
    // E -> C
    //
    // No modifica todavía estado_cierre general.
    // =========================================================

    @Transactional
    public void cerrar(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarDependenciasAnexo2(
                cierre
        );

        String estadoAnexo2 =
                normalizarEstado(
                        cierre.getEstadoAnexo2()
                );

        if (!"E".equals(
                estadoAnexo2
        )) {

            throw new IllegalStateException(
                    "El Anexo 2 del cierre "
                            + idCierreCartera
                            + " debe estar en proceso "
                            + "antes de cerrarlo en firme."
            );
        }

        Integer idUsuario =
                obtenerUsuarioSesion();

        int actualizados =
                cierreRepository.cerrarAnexo2(
                        idCierreCartera,
                        idUsuario
                );

        if (actualizados != 1) {

            throw new IllegalStateException(
                    "No fue posible cerrar en firme el Anexo 2 "
                            + "del cierre "
                            + idCierreCartera
                            + "."
            );
        }
    }


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

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarAnexo2Consultable(
                cierre
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

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarAnexo2Consultable(
                cierre
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

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarAnexo2Consultable(
                cierre
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

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarAnexo2Consultable(
                cierre
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

        CierreMensualDTO cierre =
                obtenerCierre(
                        idCierreCartera
                );

        validarAnexo2Consultable(
                cierre
        );

        return consultaRepository.obtenerResumen(
                idCierreCartera,
                idModeloPe
        );
    }


    // =========================================================
    // OBTENER CIERRE
    // =========================================================

    private CierreMensualDTO obtenerCierre(
            Integer idCierreCartera
    ) {

        return cierreRepository
                .buscarPorId(
                        idCierreCartera
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el cierre de cartera: "
                                        + idCierreCartera
                        )
                );
    }


    // =========================================================
    // VALIDAR DEPENDENCIAS DEL ANEXO 2
    // =========================================================

    private void validarDependenciasAnexo2(
            CierreMensualDTO cierre
    ) {

        if (!"C".equals(
                normalizarEstado(
                        cierre.getEstadoFotografia()
                )
        )) {

            throw new IllegalStateException(
                    "La fotografía del cierre "
                            + cierre.getIdCierreCartera()
                            + " debe estar cerrada en firme "
                            + "antes de ejecutar el Anexo 2."
            );
        }

        if (!"C".equals(
                normalizarEstado(
                        cierre.getEstadoCalculos()
                )
        )) {

            throw new IllegalStateException(
                    "Los cálculos del cierre "
                            + cierre.getIdCierreCartera()
                            + " deben estar cerrados en firme "
                            + "antes de ejecutar el Anexo 2."
            );
        }

        if (!"C".equals(
                normalizarEstado(
                        cierre.getEstadoAnexo1()
                )
        )) {

            throw new IllegalStateException(
                    "El Anexo 1 del cierre "
                            + cierre.getIdCierreCartera()
                            + " debe estar cerrado en firme "
                            + "antes de ejecutar el Anexo 2."
            );
        }
    }


    // =========================================================
    // VALIDAR ANEXO 2 PROCESABLE
    //
    // P = pendiente
    // E = en proceso / permite recalcular
    // C = cerrado en firme / no permite recalcular
    // =========================================================

    private void validarAnexo2Procesable(
            CierreMensualDTO cierre
    ) {

        String estado =
                normalizarEstado(
                        cierre.getEstadoAnexo2()
                );

        if ("C".equals(
                estado
        )) {

            throw new IllegalStateException(
                    "El Anexo 2 del cierre "
                            + cierre.getIdCierreCartera()
                            + " ya está cerrado en firme "
                            + "y no puede recalcularse."
            );
        }

        if (!"P".equals(estado)
                && !"E".equals(estado)) {

            throw new IllegalStateException(
                    "El estado del Anexo 2 del cierre "
                            + cierre.getIdCierreCartera()
                            + " no permite ejecutar el proceso. "
                            + "Estado actual: "
                            + estado
            );
        }
    }


    // =========================================================
    // VALIDAR ANEXO 2 CONSULTABLE
    //
    // Solo debe existir salida después de haber ejecutado
    // el proceso al menos una vez.
    //
    // E = disponible para revisión
    // C = cerrado en firme
    // =========================================================

    private void validarAnexo2Consultable(
            CierreMensualDTO cierre
    ) {

        validarDependenciasAnexo2(
                cierre
        );

        String estado =
                normalizarEstado(
                        cierre.getEstadoAnexo2()
                );

        if (!"E".equals(estado)
                && !"C".equals(estado)) {

            throw new IllegalStateException(
                    "El Anexo 2 del cierre "
                            + cierre.getIdCierreCartera()
                            + " todavía no ha sido procesado."
            );
        }
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


    // =========================================================
    // VALIDAR CANTIDAD DE RESULTADOS
    // =========================================================

    private void validarCantidad(
            String proceso,
            int esperados,
            int encontrados
    ) {

        if (encontrados != esperados) {

            throw new IllegalStateException(
                    "La cantidad de "
                            + proceso
                            + " no coincide. "
                            + "Esperados: "
                            + esperados
                            + ", encontrados: "
                            + encontrados
            );
        }
    }


    // =========================================================
    // USUARIO DE SESIÓN
    // =========================================================

    private Integer obtenerUsuarioSesion() {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        if (idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalStateException(
                    "No fue posible identificar el usuario de sesión."
            );
        }

        return idUsuario;
    }


    // =========================================================
    // NORMALIZAR ESTADO
    // =========================================================

    private String normalizarEstado(
            String estado
    ) {

        if (estado == null) {
            return "";
        }

        return estado
                .trim()
                .toUpperCase();
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