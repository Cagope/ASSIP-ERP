package co.assip.erp.cartera.calculosprevios;

import co.assip.erp.cartera.cierremensual.CierreMensualRepository;
import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.assip.erp.cartera.calculosprevios.dto.ResumenEdadMoraDTO;
import co.assip.erp.cartera.calculosprevios.dto.ResumenControlesCalculosDTO;
import co.assip.erp.cartera.calculosprevios.dto.ResumenAportesGarantiasDTO;
import co.assip.erp.cartera.calculosprevios.dto.DetalleCalculosCierreDTO;

import java.util.List;

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
    // 2. Validar fotografía en firme y cálculos abiertos.
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

        validarFotografiaEnFirme(
                cierre
        );

        validarCalculosNoCerrados(
                cierre
        );

// =====================================================
// 3. VALIDAR DEPENDENCIA DEL CIERRE DE DEPÓSITOS
// =====================================================

        validarCierreDepositosEnFirme(
                cierre
        );

        int etapaIniciada =
                cierreRepository.iniciarCalculos(
                        idCierreCartera,
                        idUsuario
                );

        if (etapaIniciada != 1) {

            throw new IllegalStateException(
                    "No fue posible iniciar la etapa de cálculos "
                            + "del cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // VALIDAR BASE DE CÁLCULOS
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
// 2. VALIDAR ESTADO DEL CIERRE
// =====================================================

        validarFotografiaEnFirme(
                cierre
        );

        validarCalculosNoCerrados(
                cierre
        );

// =====================================================
// 3. VALIDAR DEPENDENCIA DEL CIERRE DE DEPÓSITOS
// =====================================================

        validarCierreDepositosEnFirme(
                cierre
        );

        int etapaIniciada =
                cierreRepository.iniciarCalculos(
                        idCierreCartera,
                        idUsuario
                );

        if (etapaIniciada != 1) {

            throw new IllegalStateException(
                    "No fue posible iniciar la etapa de cálculos "
                            + "del cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // VALIDAR BASE DE CÁLCULOS
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
    // =========================================================
    // OBTENER RESUMEN POR CLASIFICACIÓN Y EDAD DE MORA
    //
    // Consulta únicamente resultados ya persistidos.
    // No ejecuta ni modifica cálculos.
    // =========================================================

    @Transactional(readOnly = true)
    public List<ResumenEdadMoraDTO> obtenerResumenEdadMora(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

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
                            + " no tiene resultados de cálculos "
                            + "para consultar."
            );
        }

        // =====================================================
        // 3. CONSULTAR RESUMEN
        // =====================================================

        return repository.obtenerResumenEdadMora(
                idCierreCartera
        );
    }

    // =========================================================
    // OBTENER RESUMEN DE APORTES Y GARANTÍAS
    //
    // Consulta únicamente resultados ya persistidos.
    // No ejecuta ni modifica cálculos.
    // =========================================================

    @Transactional(readOnly = true)
    public ResumenAportesGarantiasDTO obtenerResumenAportesGarantias(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

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
                            + " no tiene resultados de cálculos "
                            + "para consultar."
            );
        }

        // =====================================================
        // 3. CONSULTAR RESUMEN
        // =====================================================

        return repository.obtenerResumenAportesGarantias(
                idCierreCartera
        );
    }

    // =========================================================
    // OBTENER DETALLE COMPLETO DE CÁLCULOS DEL CIERRE
    //
    // Fuente para:
    //
    // - revisión operativa
    // - auditoría del cierre
    // - generación del Excel
    //
    // Consulta únicamente información ya persistida.
    //
    // NO recalcula información.
    // NO modifica el cierre.
    // =========================================================

    @Transactional(readOnly = true)
    public List<DetalleCalculosCierreDTO> obtenerDetalleCalculosCierre(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

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
                            + " no tiene resultados de cálculos "
                            + "para consultar."
            );
        }

        // =====================================================
        // 3. CONSULTAR DETALLE COMPLETO
        // =====================================================

        List<DetalleCalculosCierreDTO> detalle =
                repository.obtenerDetalleCalculosCierre(
                        idCierreCartera
                );

        // =====================================================
        // 4. VALIDAR CANTIDAD
        // =====================================================

        if (detalle.size()
                != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al consultar el detalle "
                            + "de cálculos del cierre "
                            + idCierreCartera
                            + ". Resultados esperados: "
                            + cantidadResultados
                            + ". Filas obtenidas: "
                            + detalle.size()
                            + "."
            );
        }

        // =====================================================
        // 5. RESPUESTA
        // =====================================================

        return detalle;
    }

    // =========================================================
    // OBTENER RESUMEN DE CONTROLES DE CÁLCULOS
    //
    // Consulta únicamente resultados ya persistidos.
    //
    // NO recalcula información.
    // NO modifica el cierre.
    //
    // procesoConsistente = true cuando todos los controles
    // de integridad se encuentran en cero.
    // =========================================================

    @Transactional(readOnly = true)
    public ResumenControlesCalculosDTO obtenerResumenControles(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

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
        // 2. TOTAL DE RESULTADOS
        // =====================================================

        int cantidadResultados =
                repository.contarResultados(
                        idCierreCartera
                );

        if (cantidadResultados <= 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no tiene resultados de cálculos "
                            + "para consultar."
            );
        }

        // =====================================================
        // 3. CONTROLES DE INTEGRIDAD
        // =====================================================

        int sinEdadMora =
                repository.contarSinEdadMora(
                        idCierreCartera
                );

        int diasMoraNegativos =
                repository.contarDiasMoraNegativos(
                        idCierreCartera
                );

        int edadesRiesgoInvalidas =
                repository.contarEdadesRiesgoInvalidas(
                        idCierreCartera
                );

        int edadesReestructuracionInvalidas =
                repository.contarEdadesReestructuracionInvalidas(
                        idCierreCartera
                );

        int noReestructuradosInconsistentes =
                repository.contarNoReestructuradosInconsistentes(
                        idCierreCartera
                );

        // =====================================================
        // 4. INFORMACIÓN DEL CIERRE
        // =====================================================

        int cantidadReestructurados =
                repository.contarReestructurados(
                        idCierreCartera
                );

        int cantidadUnaSolaCuota =
                repository.contarUnaSolaCuota(
                        idCierreCartera
                );

        // =====================================================
        // 5. ESTADO GENERAL DEL CONTROL
        // =====================================================

        boolean procesoConsistente =
                sinEdadMora == 0
                        && diasMoraNegativos == 0
                        && edadesRiesgoInvalidas == 0
                        && edadesReestructuracionInvalidas == 0
                        && noReestructuradosInconsistentes == 0;

        // =====================================================
        // 6. RESPUESTA
        // =====================================================

        return new ResumenControlesCalculosDTO(

                cantidadResultados,

                sinEdadMora,

                diasMoraNegativos,

                edadesRiesgoInvalidas,

                edadesReestructuracionInvalidas,

                noReestructuradosInconsistentes,

                cantidadReestructurados,

                cantidadUnaSolaCuota,

                procesoConsistente
        );
    }

    // =========================================================
    // CERRAR CÁLCULOS EN FIRME
    //
    // REQUISITOS:
    // - fotografía cerrada en firme
    // - cálculos en estado E
    // - base de resultados existente
    // - controles de integridad correctos
    //
    // RESULTADO:
    // estado_calculos = C
    // fecha_calculos_firme = CURRENT_TIMESTAMP
    // =========================================================

    public CierreMensualDTO cerrarCalculos(
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
        // 2. VALIDAR FOTOGRAFÍA EN FIRME
        // =====================================================

        validarFotografiaEnFirme(
                cierre
        );

        // =====================================================
        // 3. VALIDAR ESTADO DE CÁLCULOS
        // =====================================================

        String estadoCalculos =
                cierre.getEstadoCalculos();

        if (estadoCalculos != null
                && "C".equalsIgnoreCase(
                estadoCalculos.trim()
        )) {

            throw new IllegalStateException(
                    "Los cálculos del cierre "
                            + idCierreCartera
                            + " ya se encuentran cerrados en firme."
            );
        }

        if (estadoCalculos == null
                || !"E".equalsIgnoreCase(
                estadoCalculos.trim()
        )) {

            throw new IllegalStateException(
                    "Los cálculos del cierre "
                            + idCierreCartera
                            + " deben ejecutarse antes de "
                            + "cerrarlos en firme."
            );
        }

        // =====================================================
        // 4. VALIDAR BASE DE RESULTADOS
        // =====================================================

        int cantidadResultados =
                repository.contarResultados(
                        idCierreCartera
                );

        if (cantidadResultados <= 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no tiene resultados de cálculos "
                            + "para cerrar en firme."
            );
        }

        // =====================================================
        // 5. VALIDAR CONTROLES DE INTEGRIDAD
        // =====================================================

        int sinEdadMora =
                repository.contarSinEdadMora(
                        idCierreCartera
                );

        int diasMoraNegativos =
                repository.contarDiasMoraNegativos(
                        idCierreCartera
                );

        int edadesRiesgoInvalidas =
                repository.contarEdadesRiesgoInvalidas(
                        idCierreCartera
                );

        int edadesReestructuracionInvalidas =
                repository.contarEdadesReestructuracionInvalidas(
                        idCierreCartera
                );

        int noReestructuradosInconsistentes =
                repository.contarNoReestructuradosInconsistentes(
                        idCierreCartera
                );

        if (sinEdadMora > 0
                || diasMoraNegativos > 0
                || edadesRiesgoInvalidas > 0
                || edadesReestructuracionInvalidas > 0
                || noReestructuradosInconsistentes > 0) {

            throw new IllegalStateException(
                    "Los cálculos del cierre "
                            + idCierreCartera
                            + " presentan inconsistencias y "
                            + "no pueden cerrarse en firme."
            );
        }

        // =====================================================
        // 6. CERRAR ETAPA
        // =====================================================

        int actualizados =
                cierreRepository.cerrarCalculos(
                        idCierreCartera,
                        idUsuario
                );

        if (actualizados != 1) {

            throw new IllegalStateException(
                    "No fue posible cerrar en firme "
                            + "los cálculos del cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // 7. DEVOLVER CABECERA ACTUALIZADA
        // =====================================================

        return cierreRepository
                .buscarPorId(
                        idCierreCartera
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No fue posible recuperar el cierre "
                                        + idCierreCartera
                                        + " después de cerrar los cálculos."
                        )
                );
    }

    // =========================================================
    // VALIDAR FOTOGRAFÍA EN FIRME
    // =========================================================

    private void validarFotografiaEnFirme(
            CierreMensualDTO cierre
    ) {

        String estado =
                cierre.getEstadoFotografia();

        if (estado == null
                || !"C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "La fotografía del cierre "
                            + cierre.getIdCierreCartera()
                            + " debe estar cerrada en firme "
                            + "para ejecutar los cálculos."
            );
        }
    }

    // =========================================================
    // VALIDAR CÁLCULOS NO CERRADOS
    //
    // P = Pendiente
    // E = En proceso
    // C = Cerrados en firme
    // =========================================================

    private void validarCalculosNoCerrados(
            CierreMensualDTO cierre
    ) {

        String estado =
                cierre.getEstadoCalculos();

        if (estado != null
                && "C".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "Los cálculos del cierre "
                            + cierre.getIdCierreCartera()
                            + " ya se encuentran cerrados en firme."
            );
        }
    }

    // =========================================================
    // VALIDAR CIERRE DE DEPÓSITOS EN FIRME
    // =========================================================

    private void validarCierreDepositosEnFirme(
            CierreMensualDTO cierre
    ) {

        boolean disponible =
                repository.existeCierreDepositosEnFirme(
                        cierre.getIdCierreCartera()
                );

        if (!disponible) {

            throw new IllegalStateException(
                    "El cierre de Depósitos del "
                            + cierre.getFechaCorte()
                            + " no se encuentra cerrado en firme. "
                            + "No es posible ejecutar los cálculos "
                            + "de Cartera hasta completar el cierre "
                            + "de Depósitos."
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
