package co.assip.erp.cartera.calculosprevios;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CalculosPreviosService {

    private final CalculosPreviosRepository repository;
    private final CierreMensualRepository cierreRepository;
    private final UsuarioSesionService usuarioSesionService;

    public CalculosPreviosService(
            CalculosPreviosRepository repository,
            CierreMensualRepository cierreRepository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.cierreRepository = cierreRepository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // EJECUTAR CÁLCULOS PREVIOS
    //
    // PROCESO ACTUAL:
    //
    // 1. Validar cierre.
    // 2. Validar estado C.
    // 3. Validar base de cálculos.
    // 4. Calcular días de mora.
    // 5. Calcular edad de mora.
    // 6. Calcular bandera una sola cuota.
    // 7. Calcular bandera reestructurado.
    // 8. Calcular edad de riesgo inicial.
    // 9. Calcular edad de riesgo.
    // 10. Calcular edades de reestructuración.
    // 11. Calcular prorrateo de aportes.
    // 12. Validar cantidades y edades.
    // 13. Limpiar detalle de garantías.
    // 14. Calcular prorrateo de garantías.
    // 15. Consolidar garantías por crédito.
    // 16. Consolidar costas judiciales.
    // 17. Validar cantidades finales.
    //
    // Todavía NO calcula:
    // - VEA
    // - deterioros
    // - edad de PE
    // - edad de homologación
    // - edad contable
    // =========================================================

    public int ejecutar(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 1. RECUPERAR CIERRE
        // =====================================================

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

        // =====================================================
        // 2. VALIDAR ESTADO DEL CIERRE
        // =====================================================

        validarEstadoCerrado(
                cierre
        );

        // =====================================================
        // 3. VALIDAR BASE DE CÁLCULOS
        // =====================================================

        int cantidadResultados =
                repository.contarResultados(
                        idCierreCartera
                );

        if (cantidadResultados <= 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no tiene créditos en la base "
                            + "de cálculos comunes."
            );
        }

        // =====================================================
        // 4. CALCULAR MORA
        // =====================================================

        int cantidadMora =
                repository.calcularMora(
                        idCierreCartera,
                        idUsuario
                );

        if (cantidadMora
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al calcular la mora del cierre "
                            + idCierreCartera
                            + ". Créditos esperados: "
                            + cantidadResultados
                            + ". Créditos actualizados: "
                            + cantidadMora
                            + "."
            );
        }

        // =====================================================
        // 5. VALIDAR EDAD DE MORA
        // =====================================================

        int cantidadSinEdadMora =
                repository.contarSinEdadMora(
                        idCierreCartera
                );

        if (cantidadSinEdadMora > 0) {

            throw new IllegalStateException(
                    "El cálculo de mora no pudo clasificar "
                            + cantidadSinEdadMora
                            + " créditos del cierre "
                            + idCierreCartera
                            + ". Revise la parametrización de "
                            + "cartera.clasificaciones_mora."
            );
        }

        // =====================================================
        // 6. VALIDAR DÍAS DE MORA NEGATIVOS
        // =====================================================

        int cantidadDiasNegativos =
                repository.contarDiasMoraNegativos(
                        idCierreCartera
                );

        if (cantidadDiasNegativos > 0) {

            throw new IllegalStateException(
                    "Se encontraron "
                            + cantidadDiasNegativos
                            + " créditos con días de mora negativos "
                            + "en el cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // 7. CALCULAR BANDERAS COMUNES
        //
        // es_una_sola_cuota:
        // amortizacion_capital = plazo
        //
        // es_reestructurado:
        // credito_reestructurado de la fotografía
        // =====================================================

        int cantidadBanderas =
                repository.calcularBanderas(
                        idCierreCartera,
                        idUsuario
                );

        if (cantidadBanderas
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al calcular las banderas "
                            + "comunes del cierre "
                            + idCierreCartera
                            + ". Créditos esperados: "
                            + cantidadResultados
                            + ". Créditos actualizados: "
                            + cantidadBanderas
                            + "."
            );
        }

        // =====================================================
        // 8. CALCULAR EDADES DE RIESGO
        //
        // F se normaliza a E.
        //
        // edad_riesgo_inicial:
        // se toma de la fotografía.
        //
        // edad_de_riesgo:
        // peor edad entre:
        //
        // - edad_riesgo_inicial
        // - edad_de_riesgo de la fotografía
        // - edad_de_mora calculada
        // =====================================================

        int cantidadEdadesRiesgo =
                repository.calcularEdadesRiesgo(
                        idCierreCartera,
                        idUsuario
                );

        if (cantidadEdadesRiesgo
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al calcular las edades "
                            + "de riesgo del cierre "
                            + idCierreCartera
                            + ". Créditos esperados: "
                            + cantidadResultados
                            + ". Créditos actualizados: "
                            + cantidadEdadesRiesgo
                            + "."
            );
        }

        // =====================================================
        // 9. VALIDAR EDADES DE RIESGO
        //
        // Solamente:
        // A, B, C, D, E
        //
        // No:
        // NULL, vacío, F
        // =====================================================

        int cantidadEdadesInvalidas =
                repository.contarEdadesRiesgoInvalidas(
                        idCierreCartera
                );

        if (cantidadEdadesInvalidas > 0) {

            throw new IllegalStateException(
                    "Se encontraron "
                            + cantidadEdadesInvalidas
                            + " créditos con edades de riesgo "
                            + "inválidas en el cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // 10. CALCULAR EDADES DE REESTRUCTURACIÓN
        //
        // SI NO ES REESTRUCTURADO:
        //
        // edad_reestructuracion_inicial =
        //     edad_riesgo_inicial
        //
        // edad_reestructurado =
        //     edad_de_riesgo
        //
        // SI ES REESTRUCTURADO:
        //
        // se utilizan sus edades propias congeladas
        // en la fotografía.
        //
        // F se normaliza a E.
        // =====================================================

        int cantidadEdadesReestructuracion =
                repository.calcularEdadesReestructuracion(
                        idCierreCartera,
                        idUsuario
                );

        if (cantidadEdadesReestructuracion
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al calcular las edades "
                            + "de reestructuración del cierre "
                            + idCierreCartera
                            + ". Créditos esperados: "
                            + cantidadResultados
                            + ". Créditos actualizados: "
                            + cantidadEdadesReestructuracion
                            + "."
            );
        }

        // =====================================================
        // 11. VALIDAR EDADES DE REESTRUCTURACIÓN
        //
        // Solamente se permiten A-E.
        // =====================================================

        int cantidadReestructuracionInvalidas =
                repository
                        .contarEdadesReestructuracionInvalidas(
                                idCierreCartera
                        );

        if (cantidadReestructuracionInvalidas > 0) {

            throw new IllegalStateException(
                    "Se encontraron "
                            + cantidadReestructuracionInvalidas
                            + " créditos con edades de "
                            + "reestructuración inválidas "
                            + "en el cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // 12. VALIDAR NO REESTRUCTURADOS
        //
        // Todo crédito NO reestructurado debe cumplir:
        //
        // edad_reestructuracion_inicial =
        //     edad_riesgo_inicial
        //
        // edad_reestructurado =
        //     edad_de_riesgo
        // =====================================================

        int cantidadNoReestructuradosInconsistentes =
                repository
                        .contarNoReestructuradosInconsistentes(
                                idCierreCartera
                        );

        if (cantidadNoReestructuradosInconsistentes > 0) {

            throw new IllegalStateException(
                    "Se encontraron "
                            + cantidadNoReestructuradosInconsistentes
                            + " créditos no reestructurados con "
                            + "edades de reestructuración "
                            + "inconsistentes en el cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // 13. CALCULAR PRORRATEO DE APORTES
        //
        // porcentaje_aportes_credito =
        //
        // saldo_capital_credito
        // ----------------------
        // saldo_total_creditos_asociado
        //
        // El porcentaje se almacena así:
        //
        // 60 % = 60.000000
        // 40 % = 40.000000
        //
        // valor_aportes_credito =
        //
        // saldo_aportes_fecha_corte
        // *
        // participación del crédito
        // =====================================================

        int cantidadProrrateoAportes =
                repository.calcularProrrateoAportes(
                        idCierreCartera,
                        idUsuario
                );

        if (cantidadProrrateoAportes
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al calcular el prorrateo "
                            + "de aportes del cierre "
                            + idCierreCartera
                            + ". Créditos esperados: "
                            + cantidadResultados
                            + ". Créditos actualizados: "
                            + cantidadProrrateoAportes
                            + "."
            );
        }

        // =====================================================
        // 14. LIMPIAR DETALLE DE GARANTÍAS
        // =====================================================

        repository.limpiarProrrateoGarantias(
                idCierreCartera
        );


        // =====================================================
        // 15. CALCULAR PRORRATEO DE GARANTÍAS
        // =====================================================

        repository.calcularProrrateoGarantias(
                idCierreCartera,
                idUsuario
        );


        // =====================================================
        // 16. CONSOLIDAR GARANTÍAS POR CRÉDITO
        //
        // IMPORTANTE:
        //
        // El consolidado solamente actualiza los créditos
        // que realmente tienen garantías.
        //
        // Los créditos sin garantía permanecen correctamente
        // con:
        //
        // cantidad_bienes_garantia = 0
        // valor_garantias_total = 0
        // porcentaje_garantias_credito = 0
        // valor_garantias_credito = 0
        //
        // Por tanto, la cantidad retornada por este UPDATE
        // no debe compararse con la población total del cierre.
        // =====================================================

        repository.consolidarProrrateoGarantias(
                idCierreCartera,
                idUsuario
        );


        // =====================================================
        // 17. CONSOLIDAR COSTAS JUDICIALES
        //
        // Las costas judiciales:
        //
        // - NO se causan mensualmente.
        // - NO generan movimientos durante el cierre.
        // - se toman de los movimientos existentes.
        // - solamente se consideran movimientos activos.
        // - solamente se consideran movimientos hasta
        //   la fecha de corte.
        //
        // saldo =
        //     SUM(valor_debito - valor_credito)
        //
        // El resultado queda congelado en:
        //
        // cierres_cartera_resultados.valor_costas_judiciales
        // =====================================================

        int cantidadCostasJudiciales =
                repository.consolidarCostasJudiciales(
                        idCierreCartera,
                        idUsuario
                );

        if (cantidadCostasJudiciales
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al consolidar las costas judiciales "
                            + "del cierre "
                            + idCierreCartera
                            + ". Créditos esperados: "
                            + cantidadResultados
                            + ". Créditos actualizados: "
                            + cantidadCostasJudiciales
                            + "."
            );
        }


        // =====================================================
        // 18. RESULTADO
        // =====================================================

        return cantidadResultados;
    }

    // =========================================================
    // CALCULAR SOLO MORA
    //
    // Se conserva para pruebas individuales.
    // =========================================================

    public int calcularMora(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 1. RECUPERAR CIERRE
        // =====================================================

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

        // =====================================================
        // 2. VALIDAR ESTADO
        // =====================================================

        validarEstadoCerrado(
                cierre
        );

        // =====================================================
        // 3. VALIDAR BASE
        // =====================================================

        int cantidadResultados =
                repository.contarResultados(
                        idCierreCartera
                );

        if (cantidadResultados <= 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no tiene créditos en la base "
                            + "de cálculos comunes."
            );
        }

        // =====================================================
        // 4. CALCULAR MORA
        // =====================================================

        int cantidadActualizada =
                repository.calcularMora(
                        idCierreCartera,
                        idUsuario
                );

        if (cantidadActualizada
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al calcular la mora del cierre "
                            + idCierreCartera
                            + ". Créditos esperados: "
                            + cantidadResultados
                            + ". Créditos actualizados: "
                            + cantidadActualizada
                            + "."
            );
        }

        // =====================================================
        // 5. VALIDAR EDAD DE MORA
        // =====================================================

        int cantidadSinEdadMora =
                repository.contarSinEdadMora(
                        idCierreCartera
                );

        if (cantidadSinEdadMora > 0) {

            throw new IllegalStateException(
                    "El cálculo de mora no pudo clasificar "
                            + cantidadSinEdadMora
                            + " créditos del cierre "
                            + idCierreCartera
                            + ". Revise la parametrización de "
                            + "cartera.clasificaciones_mora."
            );
        }

        // =====================================================
        // 6. VALIDAR DÍAS DE MORA NEGATIVOS
        // =====================================================

        int cantidadDiasNegativos =
                repository.contarDiasMoraNegativos(
                        idCierreCartera
                );

        if (cantidadDiasNegativos > 0) {

            throw new IllegalStateException(
                    "Se encontraron "
                            + cantidadDiasNegativos
                            + " créditos con días de mora negativos "
                            + "en el cierre "
                            + idCierreCartera
                            + "."
            );
        }

        return cantidadActualizada;
    }

    // =========================================================
    // VALIDAR CIERRE CERRADO
    //
    // C = fotografía cerrada en firme.
    //
    // Los cálculos previos definitivos solamente se ejecutan
    // después de cerrar la fotografía.
    // =========================================================

    private void validarEstadoCerrado(
            CierreMensualDTO cierre
    ) {

        String estado =
                cierre.getEstadoCierre();

        if (estado == null
                || !"C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "El cierre "
                            + cierre.getIdCierreCartera()
                            + " debe estar cerrado en firme "
                            + "para ejecutar los cálculos previos."
            );
        }
    }

    // =========================================================
    // VALIDAR ID CIERRE
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
    // CALCULAR SOLO PRORRATEO DE APORTES
    //
    // Permite calcular aportes sobre cierres históricos
    // migrados y procesos de soporte.
    //
    // REQUISITOS:
    // - el cierre debe existir
    // - debe existir la base de resultados
    //
    // IMPORTANTE:
    // Este método NO exige estado C porque se utiliza también
    // para completar cálculos de cierres históricos migrados.
    //
    // NO modifica:
    // - cabecera
    // - fotografía
    // - mora
    // - edades
    // =========================================================

    public int calcularProrrateoAportes(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 1. VALIDAR EXISTENCIA DEL CIERRE
        // =====================================================

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

        // =====================================================
        // 2. VALIDAR BASE DE RESULTADOS
        // =====================================================

        int cantidadResultados =
                repository.contarResultados(
                        idCierreCartera
                );

        if (cantidadResultados <= 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no tiene créditos en la base "
                            + "de cálculos comunes."
            );
        }

        // =====================================================
        // 3. CALCULAR PRORRATEO DE APORTES
        // =====================================================

        int cantidadActualizada =
                repository.calcularProrrateoAportes(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 4. VALIDAR CANTIDAD
        // =====================================================

        if (cantidadActualizada
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al calcular el prorrateo "
                            + "de aportes del cierre "
                            + idCierreCartera
                            + ". Créditos esperados: "
                            + cantidadResultados
                            + ". Créditos actualizados: "
                            + cantidadActualizada
                            + "."
            );
        }

        return cantidadActualizada;
    }
}