package co.assip.erp.cartera.cierremensual.hojavida.bienes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CierreHojaVidaBienesService {

    private final CierreHojaVidaBienesRepository repository;


    // =========================================================
    // GENERAR / REGENERAR FOTOGRAFÍA DE BIENES
    //
    // Este proceso es interno.
    //
    // Se ejecuta como parte del precierre de Hoja de Vida
    // asociado al cierre mensual de cartera.
    //
    // REGLAS:
    //
    // - elimina primero relaciones bienes-personas
    // - elimina después bienes fotografiados
    // - determina los bienes requeridos por los créditos
    //   del cierre de cartera
    // - fotografía los bienes
    // - fotografía sus propietarios
    // - valida cantidades
    //
    // IMPORTANTE:
    //
    // Un cierre puede tener 0 bienes esperados.
    // Esto NO constituye error.
    //
    // =========================================================

    public ResultadoFotografiaBienes generar(
            Integer idCierreHojaVida,
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        validarParametros(
                idCierreHojaVida,
                idCierreCartera,
                idUsuario
        );


        // =====================================================
        // 1. LIMPIAR RELACIONES BIEN - PERSONA ANTERIORES
        // =====================================================

        repository.eliminarBienesPersonas(
                idCierreHojaVida
        );


        // =====================================================
        // 2. LIMPIAR BIENES ANTERIORES
        // =====================================================

        repository.eliminarBienes(
                idCierreHojaVida
        );


        // =====================================================
        // 3. VALIDAR QUE LA LIMPIEZA QUEDÓ COMPLETA
        // =====================================================

        int bienesDespuesLimpieza =
                repository.contarBienesFotografiados(
                        idCierreHojaVida
                );

        int relacionesDespuesLimpieza =
                repository.contarBienesPersonasFotografiados(
                        idCierreHojaVida
                );

        if (bienesDespuesLimpieza != 0
                || relacionesDespuesLimpieza != 0) {

            throw new IllegalStateException(
                    "No fue posible limpiar completamente "
                            + "la fotografía anterior de bienes "
                            + "del cierre de Hoja de Vida "
                            + idCierreHojaVida
                            + ". Bienes restantes: "
                            + bienesDespuesLimpieza
                            + ". Relaciones restantes: "
                            + relacionesDespuesLimpieza
                            + "."
            );
        }


        // =====================================================
        // 4. CONTAR BIENES ESPERADOS
        // =====================================================

        int cantidadBienesEsperados =
                repository.contarBienesEsperados(
                        idCierreCartera
                );


        // =====================================================
        // 5. SI NO HAY BIENES, EL PROCESO ES VÁLIDO
        // =====================================================

        if (cantidadBienesEsperados == 0) {

            return new ResultadoFotografiaBienes(
                    idCierreHojaVida,
                    idCierreCartera,

                    0,
                    0,

                    0,
                    0,

                    0,

                    true
            );
        }


        // =====================================================
        // 6. GENERAR FOTOGRAFÍA DE BIENES
        // =====================================================

        int cantidadBienesInsertados =
                repository.generarBienes(
                        idCierreHojaVida,
                        idCierreCartera,
                        idUsuario
                );


        // =====================================================
        // 7. CONTAR BIENES FOTOGRAFIADOS
        // =====================================================

        int cantidadBienesFotografiados =
                repository.contarBienesFotografiados(
                        idCierreHojaVida
                );


        // =====================================================
        // 8. VALIDAR BIENES INSERTADOS
        // =====================================================

        if (cantidadBienesInsertados
                != cantidadBienesFotografiados) {

            throw new IllegalStateException(
                    "Inconsistencia en la fotografía de bienes. "
                            + "Bienes insertados: "
                            + cantidadBienesInsertados
                            + ". Bienes encontrados: "
                            + cantidadBienesFotografiados
                            + "."
            );
        }


        if (cantidadBienesEsperados
                != cantidadBienesFotografiados) {

            throw new IllegalStateException(
                    "La fotografía de bienes no contiene "
                            + "todos los bienes esperados del cierre "
                            + idCierreCartera
                            + ". Esperados: "
                            + cantidadBienesEsperados
                            + ". Fotografiada(s): "
                            + cantidadBienesFotografiados
                            + "."
            );
        }


        // =====================================================
        // 9. CONTAR RELACIONES BIEN - PERSONA ESPERADAS
        // =====================================================

        int cantidadRelacionesEsperadas =
                repository.contarBienesPersonasEsperadas(
                        idCierreHojaVida
                );


        // =====================================================
        // 10. GENERAR RELACIONES BIEN - PERSONA
        // =====================================================

        int cantidadRelacionesInsertadas =
                repository.generarBienesPersonas(
                        idCierreHojaVida,
                        idUsuario
                );


        // =====================================================
        // 11. CONTAR RELACIONES FOTOGRAFIADAS
        // =====================================================

        int cantidadRelacionesFotografiadas =
                repository.contarBienesPersonasFotografiados(
                        idCierreHojaVida
                );


        // =====================================================
        // 12. VALIDAR RELACIONES INSERTADAS
        // =====================================================

        if (cantidadRelacionesInsertadas
                != cantidadRelacionesFotografiadas) {

            throw new IllegalStateException(
                    "Inconsistencia en la fotografía de propietarios "
                            + "de los bienes. Relaciones insertadas: "
                            + cantidadRelacionesInsertadas
                            + ". Relaciones encontradas: "
                            + cantidadRelacionesFotografiadas
                            + "."
            );
        }


        if (cantidadRelacionesEsperadas
                != cantidadRelacionesFotografiadas) {

            throw new IllegalStateException(
                    "La fotografía de bienes-personas no contiene "
                            + "todas las relaciones esperadas. "
                            + "Esperadas: "
                            + cantidadRelacionesEsperadas
                            + ". Fotografiada(s): "
                            + cantidadRelacionesFotografiadas
                            + "."
            );
        }


        // =====================================================
        // 13. VALIDAR BIENES SIN PROPIETARIO
        // =====================================================

        int cantidadBienesSinPropietario =
                repository.contarBienesSinPropietario(
                        idCierreHojaVida
                );

        if (cantidadBienesSinPropietario > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + cantidadBienesSinPropietario
                            + " bienes fotografiados sin propietario "
                            + "registrado en Hoja de Vida."
            );
        }


        // =====================================================
        // 14. RESULTADO
        // =====================================================

        return new ResultadoFotografiaBienes(
                idCierreHojaVida,
                idCierreCartera,

                cantidadBienesEsperados,
                cantidadBienesFotografiados,

                cantidadRelacionesEsperadas,
                cantidadRelacionesFotografiadas,

                cantidadBienesSinPropietario,

                true
        );
    }


    // =========================================================
    // VALIDAR FOTOGRAFÍA EXISTENTE DE BIENES
    //
    // No modifica información.
    //
    // Se utilizará posteriormente antes de cerrar en firme
    // la fotografía general de cartera.
    // =========================================================

    @Transactional(readOnly = true)
    public ResultadoValidacionBienes validar(
            Integer idCierreHojaVida,
            Integer idCierreCartera
    ) {

        validarIds(
                idCierreHojaVida,
                idCierreCartera
        );


        // =====================================================
        // 1. BIENES ESPERADOS
        // =====================================================

        int cantidadBienesEsperados =
                repository.contarBienesEsperados(
                        idCierreCartera
                );


        // =====================================================
        // 2. BIENES FOTOGRAFIADOS
        // =====================================================

        int cantidadBienesFotografiados =
                repository.contarBienesFotografiados(
                        idCierreHojaVida
                );


        // =====================================================
        // 3. RELACIONES ESPERADAS
        // =====================================================

        int cantidadRelacionesEsperadas =
                repository.contarBienesPersonasEsperadas(
                        idCierreHojaVida
                );


        // =====================================================
        // 4. RELACIONES FOTOGRAFIADAS
        // =====================================================

        int cantidadRelacionesFotografiadas =
                repository.contarBienesPersonasFotografiados(
                        idCierreHojaVida
                );


        // =====================================================
        // 5. BIENES SIN PROPIETARIO
        // =====================================================

        int cantidadBienesSinPropietario =
                repository.contarBienesSinPropietario(
                        idCierreHojaVida
                );


        // =====================================================
        // 6. RESULTADO GENERAL
        //
        // Si no hay bienes esperados:
        //
        // 0 = 0
        //
        // por tanto el cierre sigue siendo válido.
        // =====================================================

        boolean valido =
                cantidadBienesEsperados
                        == cantidadBienesFotografiados

                        &&

                        cantidadRelacionesEsperadas
                                == cantidadRelacionesFotografiadas

                        &&

                        cantidadBienesSinPropietario
                                == 0;


        return new ResultadoValidacionBienes(
                idCierreHojaVida,
                idCierreCartera,

                cantidadBienesEsperados,
                cantidadBienesFotografiados,

                cantidadRelacionesEsperadas,
                cantidadRelacionesFotografiadas,

                cantidadBienesSinPropietario,

                valido
        );
    }


    // =========================================================
    // VALIDAR PARÁMETROS
    // =========================================================

    private void validarParametros(
            Integer idCierreHojaVida,
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        validarIds(
                idCierreHojaVida,
                idCierreCartera
        );


        if (idUsuario == null
                || idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }
    }


    // =========================================================
    // VALIDAR IDS
    // =========================================================

    private void validarIds(
            Integer idCierreHojaVida,
            Integer idCierreCartera
    ) {

        if (idCierreHojaVida == null
                || idCierreHojaVida <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre de Hoja de Vida "
                            + "es obligatorio."
            );
        }


        if (idCierreCartera == null
                || idCierreCartera <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre de cartera "
                            + "es obligatorio."
            );
        }
    }


    // =========================================================
    // RESULTADO DE GENERACIÓN
    // =========================================================

    public record ResultadoFotografiaBienes(

            Integer idCierreHojaVida,

            Integer idCierreCartera,

            Integer cantidadBienesEsperados,

            Integer cantidadBienesFotografiados,

            Integer cantidadRelacionesEsperadas,

            Integer cantidadRelacionesFotografiadas,

            Integer cantidadBienesSinPropietario,

            boolean valido
    ) {
    }


    // =========================================================
    // RESULTADO DE VALIDACIÓN
    // =========================================================

    public record ResultadoValidacionBienes(

            Integer idCierreHojaVida,

            Integer idCierreCartera,

            Integer cantidadBienesEsperados,

            Integer cantidadBienesFotografiados,

            Integer cantidadRelacionesEsperadas,

            Integer cantidadRelacionesFotografiadas,

            Integer cantidadBienesSinPropietario,

            boolean valido
    ) {
    }
}