
package co.assip.erp.depositos.transferencias;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasPreviewDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasResultadoDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


// =========================================================
// SERVICIO DE CONTABILIZACIÓN
// TRANSFERENCIA DE FONDOS ENTRE CUENTAS
// =========================================================
//
// Responsabilidades:
//
// 1. Consultar las cuentas contables parametrizadas.
// 2. Preparar los comprobantes contables.
// 3. Construir conceptos y detalles descriptivos.
// 4. Validar el cuadre de cada comprobante.
// 5. Generar los consecutivos definitivos.
// 6. Registrar los comprobantes en Contabilidad.
//
// Este servicio NO registra movimientos de depósitos.
// Esa responsabilidad corresponde a
// TransferenciaCuentasService.
//
// =========================================================

@Service
@RequiredArgsConstructor
public class TransferenciaCuentasContabilidadService {

    // =====================================================
    // DEPENDENCIAS
    // =====================================================

    private final TransferenciaCuentasRepository repository;

    private final ContabilidadRegistroService registro;

    private final ConsecutivosComprobantesService consecutivos;


    // =====================================================
    // ESTRUCTURA DE COMPROBANTE PREPARADO
    // =====================================================

    public record Preparado(
            Integer agencia,
            String tipo,
            String concepto,
            List<MovimientoContableDTO> movimientos
    ) {
    }


    // =====================================================
    // CONSTRUIR MOVIMIENTO CONTABLE
    // =====================================================

    private MovimientoContableDTO construirMovimiento(

            Integer idAgencia,

            Integer idDatosPersonal,

            Integer idCatalogoCuenta,

            LocalDate fecha,

            BigDecimal valorDebito,

            BigDecimal valorCredito,

            String detalle
    ) {

        MovimientoContableDTO movimiento =
                new MovimientoContableDTO();

        movimiento.setIdAgencia(idAgencia);

        movimiento.setIdDatosPersonal(idDatosPersonal);

        movimiento.setIdCatalogoCuenta(idCatalogoCuenta);

        movimiento.setFechaAuxiliar(fecha);

        movimiento.setValorDebito(valorDebito);

        movimiento.setValorCredito(valorCredito);

        movimiento.setValorBase(BigDecimal.ZERO);

        movimiento.setDetalleMovimiento(detalle);

        return movimiento;
    }


    // =====================================================
    // CONSTRUIR CONCEPTO GENERAL DEL COMPROBANTE
    // =====================================================
    //
    // El concepto identifica:
    //
    // - Naturaleza de la operación.
    // - Cuenta origen.
    // - Agencia origen.
    // - Cuenta destino.
    // - Agencia destino.
    // - Motivo indicado por el usuario.
    //
    // =====================================================

    private String construirConcepto(

            TransferenciaCuentasRepository.Cuenta origen,

            TransferenciaCuentasRepository.Cuenta destino,

            String conceptoUsuario
    ) {

        return "TRANSFERENCIA DE FONDOS ENTRE CUENTAS. "
                + "Origen: " + origen.codigo()
                + " (Agencia " + origen.agencia() + "). "
                + "Destino: " + destino.codigo()
                + " (Agencia " + destino.agencia() + "). "
                + "Concepto: " + conceptoUsuario.trim();
    }


    // =====================================================
    // CONSTRUIR DETALLE CONTABLE
    // =====================================================
    //
    // Cada auxiliar debe describir el efecto específico
    // del movimiento.
    //
    // =====================================================

    private String construirDetalle(

            String descripcionMovimiento,

            TransferenciaCuentasRepository.Cuenta origen,

            TransferenciaCuentasRepository.Cuenta destino,

            String conceptoUsuario
    ) {

        return descripcionMovimiento
                + ". Cuenta origen: " + origen.codigo()
                + " - Agencia " + origen.agencia()
                + ". Cuenta destino: " + destino.codigo()
                + " - Agencia " + destino.agencia()
                + ". Motivo: " + conceptoUsuario.trim();
    }


    // =====================================================
    // PREPARAR CONTABILIZACIÓN
    // =====================================================
    //
    // Determina el tipo de comprobante según las agencias:
    //
    // Misma agencia:
    //     Un comprobante NC.
    //
    // Agencias diferentes:
    //     Un comprobante CA en la agencia origen.
    //     Un comprobante CA en la agencia destino.
    //
    // No se registra información en este método.
    //
    // =====================================================

    public List<Preparado> preparar(

            TransferenciaCuentasRepository.Cuenta origen,

            TransferenciaCuentasRepository.Cuenta destino,

            BigDecimal valor,

            LocalDate fecha,

            String conceptoUsuario
    ) {

        // -------------------------------------------------
        // CONSULTAR PARAMETRIZACIÓN CONTABLE
        // -------------------------------------------------

        var parametrosOrigen =
                repository.parametros(origen);

        var parametrosDestino =
                repository.parametros(destino);


        // -------------------------------------------------
        // VALIDAR CUENTAS CONTABLES DEL PRODUCTO
        // -------------------------------------------------

        repository.catalogo(
                parametrosOrigen.producto(),
                origen.agencia()
        );

        repository.catalogo(
                parametrosDestino.producto(),
                destino.agencia()
        );


        // -------------------------------------------------
        // CONSTRUIR CONCEPTO GENERAL
        // -------------------------------------------------

        String conceptoComprobante =
                construirConcepto(
                        origen,
                        destino,
                        conceptoUsuario
                );


        // -------------------------------------------------
        // DETERMINAR SI INTERVIENEN DOS AGENCIAS
        // -------------------------------------------------

        if (origen.agencia().equals(
                destino.agencia()
        )) {

            return prepararMismaAgencia(

                    origen,

                    destino,

                    parametrosOrigen.producto(),

                    parametrosDestino.producto(),

                    valor,

                    fecha,

                    conceptoUsuario,

                    conceptoComprobante
            );
        }


        // -------------------------------------------------
        // VALIDAR CUENTAS PUENTE INTERAGENCIA
        // -------------------------------------------------

        repository.catalogo(
                parametrosOrigen.interagencia(),
                origen.agencia()
        );

        repository.catalogo(
                parametrosDestino.interagencia(),
                destino.agencia()
        );


        return prepararEntreAgencias(

                origen,

                destino,

                parametrosOrigen.producto(),

                parametrosDestino.producto(),

                parametrosOrigen.interagencia(),

                parametrosDestino.interagencia(),

                valor,

                fecha,

                conceptoUsuario,

                conceptoComprobante
        );
    }


    // =====================================================
    // PREPARAR TRANSFERENCIA EN LA MISMA AGENCIA
    // =====================================================
    //
    // Comprobante NC:
    //
    // DÉBITO:
    //     Cuenta contable del producto origen.
    //
    // CRÉDITO:
    //     Cuenta contable del producto destino.
    //
    // =====================================================

    private List<Preparado> prepararMismaAgencia(

            TransferenciaCuentasRepository.Cuenta origen,

            TransferenciaCuentasRepository.Cuenta destino,

            Integer cuentaContableOrigen,

            Integer cuentaContableDestino,

            BigDecimal valor,

            LocalDate fecha,

            String conceptoUsuario,

            String conceptoComprobante
    ) {

        // -------------------------------------------------
        // DÉBITO - PRODUCTO ORIGEN
        // -------------------------------------------------

        MovimientoContableDTO debitoOrigen =
                construirMovimiento(

                        origen.agencia(),

                        origen.persona(),

                        cuentaContableOrigen,

                        fecha,

                        valor,

                        BigDecimal.ZERO,

                        construirDetalle(
                                "DÉBITO POR TRANSFERENCIA DE FONDOS "
                                        + "DESDE LA CUENTA ORIGEN",
                                origen,
                                destino,
                                conceptoUsuario
                        )
                );


        // -------------------------------------------------
        // CRÉDITO - PRODUCTO DESTINO
        // -------------------------------------------------

        MovimientoContableDTO creditoDestino =
                construirMovimiento(

                        destino.agencia(),

                        destino.persona(),

                        cuentaContableDestino,

                        fecha,

                        BigDecimal.ZERO,

                        valor,

                        construirDetalle(
                                "CRÉDITO POR TRANSFERENCIA DE FONDOS "
                                        + "A LA CUENTA DESTINO",
                                origen,
                                destino,
                                conceptoUsuario
                        )
                );


        // -------------------------------------------------
        // CONSTRUIR COMPROBANTE NC
        // -------------------------------------------------

        Preparado comprobante =
                new Preparado(

                        origen.agencia(),

                        "NC",

                        conceptoComprobante,

                        List.of(
                                debitoOrigen,
                                creditoDestino
                        )
                );

        return List.of(comprobante);
    }


    // =====================================================
    // PREPARAR TRANSFERENCIA ENTRE AGENCIAS
    // =====================================================
    //
    // Se generan dos comprobantes CA.
    //
    // AGENCIA ORIGEN:
    //
    // DÉBITO:
    //     Cuenta contable del producto origen.
    //
    // CRÉDITO:
    //     Cuenta puente interagencia de origen.
    //
    // AGENCIA DESTINO:
    //
    // DÉBITO:
    //     Cuenta puente interagencia de destino.
    //
    // CRÉDITO:
    //     Cuenta contable del producto destino.
    //
    // =====================================================

    private List<Preparado> prepararEntreAgencias(

            TransferenciaCuentasRepository.Cuenta origen,

            TransferenciaCuentasRepository.Cuenta destino,

            Integer cuentaContableOrigen,

            Integer cuentaContableDestino,

            Integer cuentaInteragenciaOrigen,

            Integer cuentaInteragenciaDestino,

            BigDecimal valor,

            LocalDate fecha,

            String conceptoUsuario,

            String conceptoComprobante
    ) {

        // =================================================
        // COMPROBANTE CA - AGENCIA ORIGEN
        // =================================================

        // -------------------------------------------------
        // DÉBITO - PRODUCTO ORIGEN
        // -------------------------------------------------

        MovimientoContableDTO debitoOrigen =
                construirMovimiento(

                        origen.agencia(),

                        origen.persona(),

                        cuentaContableOrigen,

                        fecha,

                        valor,

                        BigDecimal.ZERO,

                        construirDetalle(
                                "DÉBITO A LA CUENTA DE AHORRO ORIGEN "
                                        + "POR TRANSFERENCIA A OTRA AGENCIA",
                                origen,
                                destino,
                                conceptoUsuario
                        )
                );


        // -------------------------------------------------
        // CRÉDITO - CUENTA PUENTE ORIGEN
        // -------------------------------------------------

        MovimientoContableDTO creditoInteragenciaOrigen =
                construirMovimiento(

                        origen.agencia(),

                        origen.persona(),

                        cuentaInteragenciaOrigen,

                        fecha,

                        BigDecimal.ZERO,

                        valor,

                        construirDetalle(
                                "CRÉDITO A CUENTA PUENTE INTERAGENCIA "
                                        + "POR FONDOS TRANSFERIDOS A LA AGENCIA "
                                        + destino.agencia(),
                                origen,
                                destino,
                                conceptoUsuario
                        )
                );


        Preparado comprobanteOrigen =
                new Preparado(

                        origen.agencia(),

                        "CA",

                        conceptoComprobante,

                        List.of(
                                debitoOrigen,
                                creditoInteragenciaOrigen
                        )
                );


        // =================================================
        // COMPROBANTE CA - AGENCIA DESTINO
        // =================================================

        // -------------------------------------------------
        // DÉBITO - CUENTA PUENTE DESTINO
        // -------------------------------------------------

        MovimientoContableDTO debitoInteragenciaDestino =
                construirMovimiento(

                        destino.agencia(),

                        destino.persona(),

                        cuentaInteragenciaDestino,

                        fecha,

                        valor,

                        BigDecimal.ZERO,

                        construirDetalle(
                                "DÉBITO A CUENTA PUENTE INTERAGENCIA "
                                        + "POR FONDOS RECIBIDOS DE LA AGENCIA "
                                        + origen.agencia(),
                                origen,
                                destino,
                                conceptoUsuario
                        )
                );


        // -------------------------------------------------
        // CRÉDITO - PRODUCTO DESTINO
        // -------------------------------------------------

        MovimientoContableDTO creditoDestino =
                construirMovimiento(

                        destino.agencia(),

                        destino.persona(),

                        cuentaContableDestino,

                        fecha,

                        BigDecimal.ZERO,

                        valor,

                        construirDetalle(
                                "CRÉDITO A LA CUENTA DESTINO "
                                        + "POR TRANSFERENCIA RECIBIDA DE OTRA AGENCIA",
                                origen,
                                destino,
                                conceptoUsuario
                        )
                );


        Preparado comprobanteDestino =
                new Preparado(

                        destino.agencia(),

                        "CA",

                        conceptoComprobante,

                        List.of(
                                debitoInteragenciaDestino,
                                creditoDestino
                        )
                );


        // -------------------------------------------------
        // RETORNAR AMBOS COMPROBANTES
        // -------------------------------------------------

        return List.of(
                comprobanteOrigen,
                comprobanteDestino
        );
    }


    // =====================================================
    // CONSTRUIR VISTA PREVIA CONTABLE
    // =====================================================
    //
    // Muestra las cuentas contables y sus movimientos.
    //
    // También valida que cada comprobante esté cuadrado.
    //
    // =====================================================

    public List<TransferenciaCuentasPreviewDTO.Comprobante> vista(
            List<Preparado> preparados
    ) {

        List<TransferenciaCuentasPreviewDTO.Comprobante> respuesta =
                new ArrayList<>();


        for (Preparado preparado : preparados) {

            BigDecimal totalDebito =
                    BigDecimal.ZERO;

            BigDecimal totalCredito =
                    BigDecimal.ZERO;

            List<TransferenciaCuentasPreviewDTO.Movimiento> movimientosVista =
                    new ArrayList<>();


            // -------------------------------------------------
            // CONSULTAR CATÁLOGO Y CONSTRUIR MOVIMIENTOS
            // -------------------------------------------------

            for (MovimientoContableDTO movimiento :
                    preparado.movimientos()) {

                String[] cuentaContable =
                        repository.catalogo(
                                movimiento.getIdCatalogoCuenta(),
                                preparado.agencia()
                        );


                movimientosVista.add(

                        new TransferenciaCuentasPreviewDTO.Movimiento(

                                cuentaContable[0],

                                cuentaContable[1],

                                movimiento.getDetalleMovimiento(),

                                movimiento.getValorDebito(),

                                movimiento.getValorCredito()
                        )
                );


                totalDebito = totalDebito.add(
                        movimiento.getValorDebito()
                );

                totalCredito = totalCredito.add(
                        movimiento.getValorCredito()
                );
            }


            // -------------------------------------------------
            // VALIDAR CUADRE DEL COMPROBANTE
            // -------------------------------------------------

            if (
                    totalDebito.signum() <= 0
                            || totalDebito.compareTo(
                            totalCredito
                    ) != 0
            ) {
                throw new IllegalStateException(
                        "El comprobante contable de la agencia "
                                + preparado.agencia()
                                + " no se encuentra cuadrado."
                );
            }


            // -------------------------------------------------
            // CONSTRUIR COMPROBANTE PARA VISTA PREVIA
            // -------------------------------------------------

            respuesta.add(

                    new TransferenciaCuentasPreviewDTO.Comprobante(

                            preparado.agencia(),

                            repository.nombreAgencia(
                                    preparado.agencia()
                            ),

                            preparado.tipo(),

                            preparado.concepto(),

                            List.copyOf(
                                    movimientosVista
                            ),

                            totalDebito,

                            totalCredito
                    )
            );
        }


        return List.copyOf(respuesta);
    }


    // =====================================================
    // REGISTRAR COMPROBANTES CONTABLES
    // =====================================================
    //
    // Este método debe ejecutarse dentro de la transacción
    // iniciada por TransferenciaCuentasService.aplicar().
    //
    // No inicia una transacción independiente.
    //
    // =====================================================

    @Transactional(propagation = Propagation.MANDATORY)
    public List<TransferenciaCuentasResultadoDTO.Comprobante> registrar(

            List<Preparado> preparados,

            Integer idCuentaOrigen,

            Integer idUsuario
    ) {

        // -------------------------------------------------
        // VALIDAR CUADRE ANTES DE REGISTRAR
        // -------------------------------------------------

        vista(preparados);


        List<TransferenciaCuentasResultadoDTO.Comprobante> comprobantes =
                new ArrayList<>();


        // -------------------------------------------------
        // REGISTRAR CADA COMPROBANTE
        // -------------------------------------------------

        for (Preparado preparado : preparados) {

            // ---------------------------------------------
            // GENERAR CONSECUTIVO DEFINITIVO
            // ---------------------------------------------

            String numeroComprobante =
                    consecutivos.generarNumeroDefinitivo(

                            preparado.tipo(),

                            preparado.agencia(),

                            idUsuario
                    );


            // ---------------------------------------------
            // CONSTRUIR TRAZABILIDAD DE ORIGEN
            // ---------------------------------------------

            OrigenComprobanteDTO origen =
                    new OrigenComprobanteDTO();

            origen.setOrigenTipo(
                    "AUTO"
            );

            origen.setModuloOrigen(
                    "DEPOSITOS"
            );

            origen.setProcesoOrigen(
                    "TRANSFERENCIA_CUENTAS"
            );

            origen.setTablaOrigen(
                    "depositos.cuentas_ahorro"
            );

            origen.setIdOrigen(
                    idCuentaOrigen.longValue()
            );


            // ---------------------------------------------
            // REGISTRAR COMPROBANTE CONTABLE
            // ---------------------------------------------

            registro.registrarComprobante(

                    preparado.agencia(),

                    preparado.tipo(),

                    numeroComprobante,

                    preparado.concepto(),

                    origen,

                    preparado.movimientos(),

                    idUsuario
            );


            // ---------------------------------------------
            // CONSOLIDAR RESULTADO
            // ---------------------------------------------

            comprobantes.add(

                    new TransferenciaCuentasResultadoDTO.Comprobante(

                            preparado.agencia(),

                            preparado.tipo(),

                            numeroComprobante
                    )
            );
        }


        return List.copyOf(comprobantes);
    }

}
