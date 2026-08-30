package co.assip.erp.cartera.cierremensual.hojavida;

import co.assip.erp.cartera.cierremensual.hojavida.bienes.CierreHojaVidaBienesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class CierreHojaVidaCarteraService {

    private final CierreHojaVidaCarteraRepository repository;

    private final CierreHojaVidaBienesService bienesService;


    // =========================================================
    // GENERAR / REGENERAR PRECierre DE HOJA DE VIDA
    //
    // Este proceso es interno.
    //
    // Se ejecuta desde la fotografía mensual de cartera.
    //
    // REGLAS:
    //
    // - Si no existe cabecera HV para la fecha:
    //   se crea en estado P.
    //
    // - Si ya existe en estado P:
    //   se reutiliza la misma cabecera.
    //
    // - Si existe en estado D:
    //   no puede modificarse.
    //
    // - La población de personas se toma exclusivamente
    //   de la fotografía del cierre de cartera recibido.
    //
    // - Los bienes se determinan desde los créditos
    //   fotografiados que requieren garantía real.
    //
    // - Los bienes y sus propietarios se fotografían
    //   mediante CierreHojaVidaBienesService.
    //
    // - Este método NO marca el cierre HV como definitivo.
    //
    // =========================================================

    public ResultadoPrecierreHojaVida generarPrecierre(
            Integer idCierreCartera,
            LocalDate fechaCorte,
            Integer idUsuario
    ) {

        validarParametros(
                idCierreCartera,
                fechaCorte,
                idUsuario
        );


        // =====================================================
        // 1. BUSCAR / CREAR CABECERA
        // =====================================================

        Integer idCierreHojaVida =
                repository
                        .buscarIdPorFecha(
                                fechaCorte
                        )
                        .orElse(null);


        if (idCierreHojaVida == null) {

            idCierreHojaVida =
                    repository.crearCabecera(
                            fechaCorte,
                            idUsuario
                    );

            if (idCierreHojaVida == null
                    || idCierreHojaVida <= 0) {

                throw new IllegalStateException(
                        "No fue posible crear la cabecera "
                                + "del precierre de Hoja de Vida "
                                + "para la fecha "
                                + fechaCorte
                                + "."
                );
            }

        } else {

            // =================================================
            // 2. VALIDAR ESTADO EXISTENTE
            // =================================================

            String estado =
                    repository
                            .buscarEstado(
                                    idCierreHojaVida
                            )
                            .orElse(null);

            if (estado == null) {

                throw new IllegalStateException(
                        "No fue posible determinar "
                                + "el estado del cierre "
                                + "de Hoja de Vida "
                                + idCierreHojaVida
                                + "."
                );
            }

            if (!"P".equalsIgnoreCase(
                    estado.trim()
            )) {

                throw new IllegalStateException(
                        "Existe una fotografía definitiva "
                                + "de Hoja de Vida para la fecha "
                                + fechaCorte
                                + ". No puede regenerarse."
                );
            }


            // =================================================
            // 3. LIMPIAR PERSONAS DEL PRECierre ANTERIOR
            //
            // Los bienes se limpian dentro de bienesService.
            // =================================================

            repository.eliminarPersonas(
                    idCierreHojaVida
            );


            // =================================================
            // 4. PREPARAR CABECERA PARA NUEVA FOTOGRAFÍA
            // =================================================

            int cabeceraPreparada =
                    repository.prepararCabeceraPrecierre(
                            idCierreHojaVida,
                            idUsuario
                    );

            if (cabeceraPreparada != 1) {

                throw new IllegalStateException(
                        "No fue posible preparar la cabecera "
                                + "del precierre de Hoja de Vida "
                                + idCierreHojaVida
                                + "."
                );
            }
        }


        // =====================================================
        // 5. CONTAR PERSONAS ESPERADAS
        // =====================================================

        int cantidadPersonasEsperadas =
                repository.contarPersonasEsperadas(
                        idCierreCartera
                );

        if (cantidadPersonasEsperadas <= 0) {

            throw new IllegalStateException(
                    "No existen personas asociadas a los créditos "
                            + "fotografiados del cierre "
                            + idCierreCartera
                            + "."
            );
        }


        // =====================================================
        // 6. GENERAR FOTOGRAFÍA DE PERSONAS
        // =====================================================

        int cantidadPersonasInsertadas =
                repository.generarPersonas(
                        idCierreHojaVida,
                        idCierreCartera,
                        idUsuario
                );


        // =====================================================
        // 7. CONTAR PERSONAS FOTOGRAFIADAS
        // =====================================================

        int cantidadPersonasFotografiadas =
                repository.contarPersonasFotografiadas(
                        idCierreHojaVida
                );


        // =====================================================
        // 8. VALIDAR PERSONAS
        // =====================================================

        if (cantidadPersonasInsertadas
                != cantidadPersonasFotografiadas) {

            throw new IllegalStateException(
                    "Inconsistencia en la fotografía de personas "
                            + "de Hoja de Vida. Insertadas: "
                            + cantidadPersonasInsertadas
                            + ". Encontradas: "
                            + cantidadPersonasFotografiadas
                            + "."
            );
        }


        if (cantidadPersonasEsperadas
                != cantidadPersonasFotografiadas) {

            throw new IllegalStateException(
                    "La fotografía de Hoja de Vida no contiene "
                            + "todas las personas esperadas del cierre "
                            + idCierreCartera
                            + ". Esperadas: "
                            + cantidadPersonasEsperadas
                            + ". Fotografiada(s): "
                            + cantidadPersonasFotografiadas
                            + "."
            );
        }


        // =====================================================
        // 9. GENERAR FOTOGRAFÍA DE BIENES
        //
        // El service de bienes se responsabiliza de:
        //
        // - limpiar fotografía anterior de bienes
        // - determinar bienes requeridos
        // - fotografiar bienes
        // - fotografiar propietarios
        // - validar cantidades
        //
        // Un cierre puede tener 0 bienes y seguir siendo válido.
        // =====================================================

        CierreHojaVidaBienesService.ResultadoFotografiaBienes
                resultadoBienes =
                bienesService.generar(
                        idCierreHojaVida,
                        idCierreCartera,
                        idUsuario
                );


        if (!resultadoBienes.valido()) {

            throw new IllegalStateException(
                    "La fotografía de bienes de Hoja de Vida "
                            + "no superó las validaciones."
            );
        }


        // =====================================================
        // 10. ACTUALIZAR CABECERA
        //
        // Continúa en estado P.
        // =====================================================

        int cabeceraActualizada =
                repository.actualizarCantidadPersonasPrecierre(
                        idCierreHojaVida,
                        cantidadPersonasFotografiadas,
                        idUsuario
                );

        if (cabeceraActualizada != 1) {

            throw new IllegalStateException(
                    "No fue posible actualizar la cabecera "
                            + "del precierre de Hoja de Vida "
                            + idCierreHojaVida
                            + "."
            );
        }


        // =====================================================
        // 11. RESULTADO
        // =====================================================

        return new ResultadoPrecierreHojaVida(
                idCierreHojaVida,
                idCierreCartera,
                fechaCorte,

                cantidadPersonasEsperadas,
                cantidadPersonasFotografiadas,

                resultadoBienes.cantidadBienesEsperados(),
                resultadoBienes.cantidadBienesFotografiados(),

                resultadoBienes.cantidadRelacionesEsperadas(),
                resultadoBienes.cantidadRelacionesFotografiadas(),

                resultadoBienes.cantidadBienesSinPropietario(),

                "P"
        );
    }


    // =========================================================
    // VALIDAR PRECierre EXISTENTE
    //
    // Valida:
    //
    // - personas
    // - bienes
    // - bienes/personas
    //
    // =========================================================

    @Transactional(readOnly = true)
    public ResultadoValidacionPrecierre validarPrecierre(
            Integer idCierreCartera,
            LocalDate fechaCorte
    ) {

        if (idCierreCartera == null
                || idCierreCartera <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre de cartera es obligatorio."
            );
        }

        if (fechaCorte == null) {

            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }


        // =====================================================
        // 1. LOCALIZAR PRECierre DE HOJA DE VIDA
        // =====================================================

        Integer idCierreHojaVida =
                repository
                        .buscarIdPorFecha(
                                fechaCorte
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No existe fotografía de Hoja de Vida "
                                                + "para la fecha "
                                                + fechaCorte
                                                + "."
                                )
                        );


        // =====================================================
        // 2. ESTADO
        // =====================================================

        String estado =
                repository
                        .buscarEstado(
                                idCierreHojaVida
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No fue posible determinar el estado "
                                                + "de la fotografía de Hoja de Vida."
                                )
                        );


        // =====================================================
        // 3. VALIDAR PERSONAS
        // =====================================================

        int cantidadPersonasEsperadas =
                repository.contarPersonasEsperadas(
                        idCierreCartera
                );


        int cantidadPersonasFotografiadas =
                repository.contarPersonasFotografiadas(
                        idCierreHojaVida
                );


        boolean personasValidas =
                cantidadPersonasEsperadas > 0
                        &&
                        cantidadPersonasEsperadas
                                == cantidadPersonasFotografiadas;


        // =====================================================
        // 4. VALIDAR BIENES
        // =====================================================

        CierreHojaVidaBienesService.ResultadoValidacionBienes
                validacionBienes =
                bienesService.validar(
                        idCierreHojaVida,
                        idCierreCartera
                );


        // =====================================================
        // 5. RESULTADO GENERAL
        // =====================================================

        boolean valido =
                personasValidas
                        &&
                        validacionBienes.valido();


        return new ResultadoValidacionPrecierre(
                idCierreHojaVida,
                idCierreCartera,
                fechaCorte,
                estado,

                cantidadPersonasEsperadas,
                cantidadPersonasFotografiadas,

                validacionBienes.cantidadBienesEsperados(),
                validacionBienes.cantidadBienesFotografiados(),

                validacionBienes.cantidadRelacionesEsperadas(),
                validacionBienes.cantidadRelacionesFotografiadas(),

                validacionBienes.cantidadBienesSinPropietario(),

                valido
        );
    }


    // =========================================================
    // CERRAR FOTOGRAFÍA DE HOJA DE VIDA EN DEFINITIVO
    //
    // P -> D
    //
    // Debe ejecutarse únicamente desde:
    //
    // CierreMensualService.cerrarFotografia(...)
    //
    // Antes de marcar D se valida:
    //
    // - personas
    // - bienes
    // - bienes/personas
    //
    // =========================================================

    public void cerrarDefinitivo(
            Integer idCierreCartera,
            LocalDate fechaCorte,
            Integer idUsuario
    ) {

        validarParametros(
                idCierreCartera,
                fechaCorte,
                idUsuario
        );


        // =====================================================
        // 1. VALIDAR PRECierre COMPLETO
        // =====================================================

        ResultadoValidacionPrecierre validacion =
                validarPrecierre(
                        idCierreCartera,
                        fechaCorte
                );


        if (!validacion.valido()) {

            throw new IllegalStateException(
                    "No es posible cerrar en firme la fotografía "
                            + "de Hoja de Vida. "
                            + "Personas esperadas: "
                            + validacion.cantidadPersonasEsperadas()
                            + ". Personas fotografiadas: "
                            + validacion.cantidadPersonasFotografiadas()
                            + ". Bienes esperados: "
                            + validacion.cantidadBienesEsperados()
                            + ". Bienes fotografiados: "
                            + validacion.cantidadBienesFotografiados()
                            + ". Relaciones bien-persona esperadas: "
                            + validacion.cantidadRelacionesEsperadas()
                            + ". Relaciones bien-persona fotografiadas: "
                            + validacion.cantidadRelacionesFotografiadas()
                            + ". Bienes sin propietario: "
                            + validacion.cantidadBienesSinPropietario()
                            + "."
            );
        }


        // =====================================================
        // 2. VALIDAR ESTADO P
        // =====================================================

        if (!"P".equalsIgnoreCase(
                validacion.estado()
        )) {

            throw new IllegalStateException(
                    "La fotografía de Hoja de Vida para la fecha "
                            + fechaCorte
                            + " no se encuentra en estado P."
            );
        }


        // =====================================================
        // 3. MARCAR DEFINITIVO
        // =====================================================

        int actualizados =
                repository.finalizarDefinitivo(
                        validacion.idCierreHojaVida(),
                        idUsuario
                );


        if (actualizados != 1) {

            throw new IllegalStateException(
                    "No fue posible marcar como definitiva "
                            + "la fotografía de Hoja de Vida "
                            + validacion.idCierreHojaVida()
                            + "."
            );
        }
    }


    // =========================================================
    // VALIDACIONES GENERALES
    // =========================================================

    private void validarParametros(
            Integer idCierreCartera,
            LocalDate fechaCorte,
            Integer idUsuario
    ) {

        if (idCierreCartera == null
                || idCierreCartera <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre de cartera es obligatorio."
            );
        }


        if (fechaCorte == null) {

            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }


        if (idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }
    }


    // =========================================================
    // RESULTADO DEL PRECierre
    // =========================================================

    public record ResultadoPrecierreHojaVida(

            Integer idCierreHojaVida,

            Integer idCierreCartera,

            LocalDate fechaCorte,

            Integer cantidadPersonasEsperadas,

            Integer cantidadPersonasFotografiadas,

            Integer cantidadBienesEsperados,

            Integer cantidadBienesFotografiados,

            Integer cantidadRelacionesEsperadas,

            Integer cantidadRelacionesFotografiadas,

            Integer cantidadBienesSinPropietario,

            String estado
    ) {
    }


    // =========================================================
    // RESULTADO DE VALIDACIÓN
    // =========================================================

    public record ResultadoValidacionPrecierre(

            Integer idCierreHojaVida,

            Integer idCierreCartera,

            LocalDate fechaCorte,

            String estado,

            Integer cantidadPersonasEsperadas,

            Integer cantidadPersonasFotografiadas,

            Integer cantidadBienesEsperados,

            Integer cantidadBienesFotografiados,

            Integer cantidadRelacionesEsperadas,

            Integer cantidadRelacionesFotografiadas,

            Integer cantidadBienesSinPropietario,

            boolean valido
    ) {
    }
}