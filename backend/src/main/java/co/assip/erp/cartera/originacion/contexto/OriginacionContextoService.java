package co.assip.erp.cartera.originacion.contexto;

import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.VectorComportamientoActualService;
import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoDetalleDTO;
import co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto.VectorComportamientoResumenDTO;
import co.assip.erp.cartera.originacion.contexto.dto.OriginacionContextoDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OriginacionContextoService {

    // =========================================================
    // CONSTANTES
    // =========================================================

    /**
     * Valor inicial únicamente para simulación en Originación.
     *
     * No reemplaza la reciprocidad definitiva determinada
     * posteriormente por las condiciones de la operación.
     */
    private static final BigDecimal RECIPROCIDAD_INICIAL_SIMULACION =
            BigDecimal.valueOf(50);


    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final OriginacionContextoRepository repository;

    private final VectorComportamientoActualService
            vectorComportamientoService;

    private final UsuarioSesionService
            usuarioSesionService;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public OriginacionContextoService(
            OriginacionContextoRepository repository,
            VectorComportamientoActualService vectorComportamientoService,
            UsuarioSesionService usuarioSesionService
    ) {

        this.repository =
                repository;

        this.vectorComportamientoService =
                vectorComportamientoService;

        this.usuarioSesionService =
                usuarioSesionService;
    }


    // =========================================================
    // CONTEXTO COMPLETO
    // =========================================================

    public OriginacionContextoDTO consultar(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        validar(
                idDatosPersonal,
                idAgencia
        );


        // =====================================================
        // VECTOR
        // =====================================================

        List<VectorComportamientoResumenDTO>
                vectorResumen =
                vectorComportamientoService
                        .listarResumenPorPersona(
                                idDatosPersonal
                        );


        List<VectorComportamientoDetalleDTO>
                vectorDetalle =
                vectorComportamientoService
                        .listarDetallePorPersona(
                                idDatosPersonal
                        );


        // =====================================================
        // DEPÓSITOS
        // =====================================================

        List<OriginacionContextoDTO.DepositoDTO>
                depositos =
                repository.listarDepositos(
                        idDatosPersonal
                );


        // =====================================================
        // CARTERA ACTUAL
        // =====================================================

        List<OriginacionContextoDTO.CarteraDTO>
                carteraActual =
                construirCarteraActual(
                        vectorResumen,
                        vectorDetalle
                );


        // =====================================================
        // RESPUESTA
        // =====================================================

        OriginacionContextoDTO contexto =
                new OriginacionContextoDTO();


        contexto.setIdDatosPersonal(
                idDatosPersonal
        );


        contexto.setIdAgencia(
                idAgencia
        );


        contexto.setInformacionEconomica(
                repository
                        .buscarInformacionEconomica(
                                idDatosPersonal
                        )
                        .orElse(null)
        );


        contexto.setDepositos(
                depositos
        );


        contexto.setCarteraActual(
                carteraActual
        );


        contexto.setVectorResumen(
                vectorResumen
        );


        contexto.setCodeudasActuales(
                repository.listarCodeudasActuales(
                        idDatosPersonal
                )
        );


        // =====================================================
        // RESUMEN DE CARTERA
        // =====================================================

        contexto.setResumenCartera(
                construirResumenCartera(
                        carteraActual,
                        vectorResumen,
                        vectorDetalle
                )
        );


        // =====================================================
        // RECIPROCIDAD
        // =====================================================

        contexto.setReciprocidad(
                construirReciprocidad(
                        depositos,
                        carteraActual
                )
        );


        return contexto;
    }


    // =========================================================
    // INFORMACIÓN ECONÓMICA
    // =========================================================

    public OriginacionContextoDTO.InformacionEconomicaDTO
    consultarInformacionEconomica(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        validar(
                idDatosPersonal,
                idAgencia
        );

        return repository
                .buscarInformacionEconomica(
                        idDatosPersonal
                )
                .orElse(null);
    }


    // =========================================================
    // DEPÓSITOS
    // =========================================================

    public List<OriginacionContextoDTO.DepositoDTO>
    listarDepositos(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        validar(
                idDatosPersonal,
                idAgencia
        );

        return repository.listarDepositos(
                idDatosPersonal
        );
    }


    // =========================================================
    // CARTERA ACTUAL
    // =========================================================

    public List<OriginacionContextoDTO.CarteraDTO>
    listarCarteraActual(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        validar(
                idDatosPersonal,
                idAgencia
        );


        List<VectorComportamientoResumenDTO>
                resumen =
                vectorComportamientoService
                        .listarResumenPorPersona(
                                idDatosPersonal
                        );


        List<VectorComportamientoDetalleDTO>
                detalle =
                vectorComportamientoService
                        .listarDetallePorPersona(
                                idDatosPersonal
                        );


        return construirCarteraActual(
                resumen,
                detalle
        );
    }


    // =========================================================
    // CARTERA HISTÓRICA
    // =========================================================

    public List<OriginacionContextoDTO.CarteraDTO>
    listarCarteraHistorica(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        validar(
                idDatosPersonal,
                idAgencia
        );

        return repository.listarCarteraHistorica(
                idDatosPersonal
        );
    }


    // =========================================================
    // CODEUDAS ACTUALES
    // =========================================================

    public List<OriginacionContextoDTO.CodeudaDTO>
    listarCodeudasActuales(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        validar(
                idDatosPersonal,
                idAgencia
        );

        return repository.listarCodeudasActuales(
                idDatosPersonal
        );
    }


    // =========================================================
    // CODEUDAS HISTÓRICAS
    // =========================================================

    public List<OriginacionContextoDTO.CodeudaDTO>
    listarCodeudasHistoricas(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        validar(
                idDatosPersonal,
                idAgencia
        );

        return repository.listarCodeudasHistoricas(
                idDatosPersonal
        );
    }


    // =========================================================
    // CONSTRUIR CARTERA ACTUAL
    // =========================================================

    private List<OriginacionContextoDTO.CarteraDTO>
    construirCarteraActual(
            List<VectorComportamientoResumenDTO> resumen,
            List<VectorComportamientoDetalleDTO> detalle
    ) {

        return resumen
                .stream()
                .map(
                        credito -> {

                            VectorComportamientoDetalleDTO actual =
                                    detalle
                                            .stream()
                                            .filter(
                                                    item ->
                                                            credito
                                                                    .getIdCarteraCredito()
                                                                    .equals(
                                                                            item.getIdCarteraCredito()
                                                                    )
                                            )
                                            .filter(
                                                    item ->
                                                            Long.valueOf(1L)
                                                                    .equals(
                                                                            item.getPosicionVector()
                                                                    )
                                            )
                                            .findFirst()
                                            .orElse(null);


                            OriginacionContextoDTO.CarteraDTO dto =
                                    new OriginacionContextoDTO.CarteraDTO();


                            dto.setIdCarteraCredito(
                                    credito.getIdCarteraCredito()
                            );


                            dto.setIdAgencia(
                                    credito.getIdAgencia()
                            );


                            dto.setIdLineaCredito(
                                    credito.getIdLineaCredito()
                            );


                            dto.setCodigoLineaCredito(
                                    credito.getCodigoLineaCredito()
                            );


                            dto.setNombreLineaCredito(
                                    credito.getNombreLineaCredito()
                            );


                            dto.setPagareCartera(
                                    credito.getPagareCartera()
                            );


                            dto.setFechaDesembolso(
                                    credito.getFechaDesembolso()
                            );


                            dto.setSaldoActual(
                                    credito.getSaldoActualMaestro()
                            );


                            if (actual != null) {

                                dto.setDiasMora(
                                        actual.getDiasMora()
                                );

                            } else {

                                dto.setDiasMora(
                                        credito.getMoraUltimoCorte()
                                );
                            }


                            dto.setCodigoEstadoCartera(
                                    credito.getCodigoEstadoCartera()
                            );


                            dto.setNombreEstadoCartera(
                                    credito.getDescripcionEstadoCartera()
                            );


                            dto.setVigente(
                                    Boolean.TRUE
                            );


                            return dto;
                        }
                )
                .toList();
    }


    // =========================================================
    // RESUMEN DE CARTERA
    // =========================================================

    private OriginacionContextoDTO.ResumenCarteraDTO
    construirResumenCartera(
            List<OriginacionContextoDTO.CarteraDTO> carteraActual,
            List<VectorComportamientoResumenDTO> vectorResumen,
            List<VectorComportamientoDetalleDTO> vectorDetalle
    ) {

        OriginacionContextoDTO.ResumenCarteraDTO dto =
                new OriginacionContextoDTO.ResumenCarteraDTO();


        // =====================================================
        // CANTIDAD DE CRÉDITOS
        // =====================================================

        dto.setCantidadCreditos(
                carteraActual.size()
        );


        // =====================================================
        // SALDO TOTAL CARTERA ACTUAL
        // =====================================================

        BigDecimal saldoCarteraActual =
                carteraActual
                        .stream()
                        .map(
                                OriginacionContextoDTO.CarteraDTO::getSaldoActual
                        )
                        .filter(
                                valor -> valor != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        dto.setSaldoCarteraActual(
                saldoCarteraActual
        );


        // =====================================================
        // MORA ACTUAL
        // =====================================================

        Integer moraActual =
                vectorDetalle
                        .stream()
                        .filter(
                                item ->
                                        Long.valueOf(1L)
                                                .equals(
                                                        item.getPosicionVector()
                                                )
                        )
                        .map(
                                VectorComportamientoDetalleDTO::getDiasMora
                        )
                        .filter(
                                dias -> dias != null
                        )
                        .max(
                                Integer::compareTo
                        )
                        .orElse(0);


        dto.setMoraActual(
                moraActual
        );


        // =====================================================
        // MORA MÁXIMA HISTÓRICA
        // =====================================================

        Integer moraMaximaHistorica =
                vectorResumen
                        .stream()
                        .map(
                                VectorComportamientoResumenDTO::getMoraMaxima
                        )
                        .filter(
                                mora -> mora != null
                        )
                        .max(
                                Integer::compareTo
                        )
                        .orElse(0);


        dto.setMoraMaximaHistorica(
                moraMaximaHistorica
        );


        // =====================================================
        // PROMEDIO MORA ÚLTIMOS 12 CIERRES
        // =====================================================
        //
        // El Vector de Comportamiento ya limita el histórico
        // a máximo 12 cierres + ACTUAL.
        //
        // Se excluye ACTUAL y solamente se promedian las
        // posiciones históricas tipo CIERRE.
        // =====================================================

        BigDecimal sumaMoraHistorica =
                vectorDetalle
                        .stream()
                        .filter(
                                item ->
                                        "CIERRE".equalsIgnoreCase(
                                                item.getTipoPosicion()
                                        )
                        )
                        .map(
                                VectorComportamientoDetalleDTO::getDiasMora
                        )
                        .filter(
                                dias -> dias != null
                        )
                        .map(
                                BigDecimal::valueOf
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        long cantidadObservaciones =
                vectorDetalle
                        .stream()
                        .filter(
                                item ->
                                        "CIERRE".equalsIgnoreCase(
                                                item.getTipoPosicion()
                                        )
                        )
                        .map(
                                VectorComportamientoDetalleDTO::getDiasMora
                        )
                        .filter(
                                dias -> dias != null
                        )
                        .count();


        BigDecimal promedioMoraUltimos12Meses =
                BigDecimal.ZERO;


        if (cantidadObservaciones > 0) {

            promedioMoraUltimos12Meses =
                    sumaMoraHistorica.divide(
                            BigDecimal.valueOf(
                                    cantidadObservaciones
                            ),
                            2,
                            RoundingMode.HALF_UP
                    );
        }


        dto.setPromedioMoraUltimos12Meses(
                promedioMoraUltimos12Meses
        );


        // =====================================================
        // RECLASIFICACIÓN POR EVALUACIÓN
        // =====================================================
        //
        // No se infiere desde las edades del cierre.
        //
        // Una edad de riesgo superior a la edad de mora puede
        // provenir de otros procesos del cierre y no permite
        // identificar de forma segura una recalificación por
        // Evaluación de Cartera.
        //
        // Se deja sin informar hasta enlazar la fuente específica
        // de Evaluación de Cartera.
        // =====================================================

        dto.setCantidadCreditosReclasificados(
                null
        );

        dto.setTieneCreditosReclasificados(
                null
        );


        return dto;
    }


    // =========================================================
    // RECIPROCIDAD
    // =========================================================

    private OriginacionContextoDTO.ReciprocidadDTO
    construirReciprocidad(
            List<OriginacionContextoDTO.DepositoDTO> depositos,
            List<OriginacionContextoDTO.CarteraDTO> carteraActual
    ) {

        OriginacionContextoDTO.ReciprocidadDTO dto =
                new OriginacionContextoDTO.ReciprocidadDTO();


        // =====================================================
        // APORTES SOCIALES
        // =====================================================

        BigDecimal saldoAportes =
                depositos
                        .stream()
                        .filter(
                                deposito ->
                                        "01".equals(
                                                deposito.getCodigoForma()
                                        )
                        )
                        .map(
                                OriginacionContextoDTO.DepositoDTO::getSaldo
                        )
                        .filter(
                                saldo -> saldo != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // SALDO CARTERA ACTUAL
        // =====================================================

        BigDecimal saldoCarteraActual =
                carteraActual
                        .stream()
                        .map(
                                OriginacionContextoDTO.CarteraDTO::getSaldoActual
                        )
                        .filter(
                                saldo -> saldo != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // SIMULACIÓN
        // =====================================================

        BigDecimal cupoReciprocidad =
                saldoAportes.multiply(
                        RECIPROCIDAD_INICIAL_SIMULACION
                );


        BigDecimal diferenciaReciprocidad =
                cupoReciprocidad.subtract(
                        saldoCarteraActual
                );


        BigDecimal disponibleReciprocidad =
                diferenciaReciprocidad.signum() > 0
                        ? diferenciaReciprocidad
                        : BigDecimal.ZERO;


        BigDecimal excesoReciprocidad =
                diferenciaReciprocidad.signum() < 0
                        ? diferenciaReciprocidad.abs()
                        : BigDecimal.ZERO;


        dto.setSaldoAportes(
                saldoAportes
        );


        dto.setReciprocidadInicial(
                RECIPROCIDAD_INICIAL_SIMULACION
        );


        dto.setCupoReciprocidad(
                cupoReciprocidad
        );


        dto.setSaldoCarteraActual(
                saldoCarteraActual
        );


        dto.setDiferenciaReciprocidad(
                diferenciaReciprocidad
        );


        dto.setDisponibleReciprocidad(
                disponibleReciprocidad
        );


        dto.setExcesoReciprocidad(
                excesoReciprocidad
        );


        return dto;
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validar(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        if (idDatosPersonal == null
                || idDatosPersonal <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del asociado no es válido."
            );
        }


        if (idAgencia == null
                || idAgencia <= 0) {

            throw new IllegalArgumentException(
                    "La agencia no es válida."
            );
        }


        usuarioSesionService.validarAgencia(
                idAgencia
        );
    }
}