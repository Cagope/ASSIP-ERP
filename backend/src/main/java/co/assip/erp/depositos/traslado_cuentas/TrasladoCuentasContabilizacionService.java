
package co.assip.erp.depositos.traslado_cuentas;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.consecutivos_comprobantes.ConsecutivosComprobantesService;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;

import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasPreviewDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


// =========================================================
// SERVICIO DE CONTABILIZACIÓN
// TRASLADO INTEGRAL DE CUENTAS ENTRE AGENCIAS
// =========================================================

@Service
@RequiredArgsConstructor
public class TrasladoCuentasContabilizacionService {

 // =====================================================
 // DEPENDENCIAS
 // =====================================================

 private final TrasladoCuentasRepository repository;

 private final ContabilidadRegistroService registro;

 private final ConsecutivosComprobantesService consecutivos;


 // =====================================================
 // ESTRUCTURA DEL COMPROBANTE PREPARADO
 // =====================================================

 public record Preparado(
         Integer agencia,
         String concepto,
         List<MovimientoContableDTO> movimientos
 ) {
 }


 // =====================================================
 // CONSTRUIR MOVIMIENTO CONTABLE
 // =====================================================

 private MovimientoContableDTO movimiento(
         Integer agencia,
         Integer persona,
         Integer cuenta,
         LocalDate fecha,
         BigDecimal debito,
         BigDecimal credito,
         String detalle
 ) {

  MovimientoContableDTO movimiento = new MovimientoContableDTO();

  movimiento.setIdAgencia(agencia);
  movimiento.setIdDatosPersonal(persona);

  movimiento.setIdCatalogoCuenta(cuenta);
  movimiento.setFechaAuxiliar(fecha);

  movimiento.setValorDebito(debito);
  movimiento.setValorCredito(credito);

  movimiento.setValorBase(BigDecimal.ZERO);

  movimiento.setDetalleMovimiento(detalle);

  return movimiento;
 }


 // =====================================================
 // PREPARAR COMPROBANTES CONTABLES
 // =====================================================

 public List<Preparado> preparar(
         Integer persona,
         String documento,
         String nombreCompleto,
         Integer origen,
         Integer destino,
         LocalDate fecha,
         List<TrasladoCuentasRepository.Cuenta> cuentas,
         Map<Integer, Integer> formasDestino
 ) {

  List<MovimientoContableDTO> movimientosSalida =
          new ArrayList<>();

  List<MovimientoContableDTO> movimientosEntrada =
          new ArrayList<>();


  // -------------------------------------------------
  // IDENTIFICACIÓN DE AGENCIAS Y ASOCIADO
  // -------------------------------------------------

  String nombreAgenciaOrigen =
          repository.nombreAgencia(origen);

  String nombreAgenciaDestino =
          repository.nombreAgencia(destino);

  String identificacionAsociado =
          nombreCompleto + ", documento " + documento;


  // -------------------------------------------------
  // CONSTRUIR MOVIMIENTOS POR CUENTA DE AHORRO
  // -------------------------------------------------

  for (TrasladoCuentasRepository.Cuenta cuenta : cuentas) {

   if (cuenta.saldoActual().signum() <= 0) {
    continue;
   }


   // ---------------------------------------------
   // PARÁMETROS DE LA AGENCIA ORIGEN
   // ---------------------------------------------

   var parametrosOrigen = repository.parametros(
           cuenta.idFormaAhorro(),
           cuenta.plazoCuenta()
   );


   // ---------------------------------------------
   // PARÁMETROS DE LA AGENCIA DESTINO
   // ---------------------------------------------

   Integer idFormaDestino =
           formasDestino.get(cuenta.idCuentaAhorro());

   if (idFormaDestino == null) {

    throw new IllegalStateException(
            "No se encontró la forma de ahorro destino "
                    + "para la cuenta "
                    + cuenta.codigoCuenta()
    );
   }

   var parametrosDestino = repository.parametros(
           idFormaDestino,
           cuenta.plazoCuenta()
   );


   // ---------------------------------------------
   // VALIDAR CUENTAS CONTABLES PARAMETRIZADAS
   // ---------------------------------------------

   repository.catalogo(
           parametrosOrigen.producto(),
           origen
   );

   repository.catalogo(
           parametrosOrigen.interagencia(),
           origen
   );

   repository.catalogo(
           parametrosDestino.producto(),
           destino
   );

   repository.catalogo(
           parametrosDestino.interagencia(),
           destino
   );


   // ---------------------------------------------
   // INFORMACIÓN DEL MOVIMIENTO
   // ---------------------------------------------

   BigDecimal valor = cuenta.saldoActual();

   String detalleSalida =
           "Salida por traslado de cuenta de ahorro "
                   + cuenta.codigoCuenta()
                   + " hacia agencia "
                   + nombreAgenciaDestino;

   String detalleEntrada =
           "Recepción por traslado de cuenta de ahorro "
                   + cuenta.codigoCuenta()
                   + " desde agencia "
                   + nombreAgenciaOrigen;


   // ---------------------------------------------
   // AGENCIA ORIGEN
   // DÉBITO: PRODUCTO DE AHORRO
   // CRÉDITO: CUENTA INTERAGENCIA
   // ---------------------------------------------

   movimientosSalida.add(
           movimiento(
                   origen,
                   persona,
                   parametrosOrigen.producto(),
                   fecha,
                   valor,
                   BigDecimal.ZERO,
                   detalleSalida
           )
   );

   movimientosSalida.add(
           movimiento(
                   origen,
                   persona,
                   parametrosOrigen.interagencia(),
                   fecha,
                   BigDecimal.ZERO,
                   valor,
                   detalleSalida
           )
   );


   // ---------------------------------------------
   // AGENCIA DESTINO
   // DÉBITO: CUENTA INTERAGENCIA
   // CRÉDITO: PRODUCTO DE AHORRO
   // ---------------------------------------------

   movimientosEntrada.add(
           movimiento(
                   destino,
                   persona,
                   parametrosDestino.interagencia(),
                   fecha,
                   valor,
                   BigDecimal.ZERO,
                   detalleEntrada
           )
   );

   movimientosEntrada.add(
           movimiento(
                   destino,
                   persona,
                   parametrosDestino.producto(),
                   fecha,
                   BigDecimal.ZERO,
                   valor,
                   detalleEntrada
           )
   );
  }


  // =================================================
  // VALIDAR MOVIMIENTOS
  // =================================================

  if (movimientosSalida.isEmpty()) {

   throw new IllegalStateException(
           "No existen saldos para trasladar."
   );
  }


  // =================================================
  // CONCEPTOS DE COMPROBANTES
  // =================================================

  String conceptoOrigen =
          "Traslado integral de cuentas de ahorro del asociado "
                  + identificacionAsociado
                  + ". Salida de recursos de agencia "
                  + nombreAgenciaOrigen
                  + " hacia agencia "
                  + nombreAgenciaDestino
                  + ".";

  String conceptoDestino =
          "Recepción de recursos por traslado integral "
                  + "de cuentas de ahorro del asociado "
                  + identificacionAsociado
                  + ", provenientes de agencia "
                  + nombreAgenciaOrigen
                  + " hacia agencia "
                  + nombreAgenciaDestino
                  + ".";


  // =================================================
  // DEVOLVER COMPROBANTES PREPARADOS
  // =================================================

  return List.of(

          new Preparado(
                  origen,
                  conceptoOrigen,
                  List.copyOf(movimientosSalida)
          ),

          new Preparado(
                  destino,
                  conceptoDestino,
                  List.copyOf(movimientosEntrada)
          )
  );
 }


 // =====================================================
 // GENERAR VISTA PREVIA CONTABLE
 // =====================================================

 public List<TrasladoCuentasPreviewDTO.ComprobantePreview> vista(
         List<Preparado> preparados
 ) {

  List<TrasladoCuentasPreviewDTO.ComprobantePreview> resultado =
          new ArrayList<>();


  // -------------------------------------------------
  // PROCESAR CADA COMPROBANTE
  // -------------------------------------------------

  for (Preparado preparado : preparados) {

   BigDecimal totalDebito = BigDecimal.ZERO;

   BigDecimal totalCredito = BigDecimal.ZERO;

   List<TrasladoCuentasPreviewDTO.MovimientoPreview> lineas =
           new ArrayList<>();


   // ---------------------------------------------
   // DETALLE DE MOVIMIENTOS
   // ---------------------------------------------

   for (MovimientoContableDTO movimiento : preparado.movimientos()) {

    String[] catalogo = repository.catalogo(
            movimiento.getIdCatalogoCuenta(),
            preparado.agencia()
    );

    lineas.add(
            new TrasladoCuentasPreviewDTO.MovimientoPreview(
                    catalogo[0],
                    catalogo[1],
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


   // ---------------------------------------------
   // VALIDAR CUADRE DEL COMPROBANTE
   // ---------------------------------------------

   if (
           totalDebito.signum() <= 0
                   || totalDebito.compareTo(totalCredito) != 0
   ) {

    throw new IllegalStateException(
            "Comprobante no cuadrado para agencia "
                    + preparado.agencia()
    );
   }


   // ---------------------------------------------
   // CONSTRUIR VISTA PREVIA
   // ---------------------------------------------

   resultado.add(
           new TrasladoCuentasPreviewDTO.ComprobantePreview(
                   preparado.agencia(),
                   repository.nombreAgencia(preparado.agencia()),
                   "CA",
                   preparado.concepto(),
                   List.copyOf(lineas),
                   totalDebito,
                   totalCredito
           )
   );
  }

  return List.copyOf(resultado);
 }


 // =====================================================
 // REGISTRAR COMPROBANTES DEFINITIVOS
 // =====================================================

 @Transactional(propagation = Propagation.MANDATORY)
 public Map<Integer, String> registrar(
         List<Preparado> preparados,
         Integer persona,
         Integer idCuentaOrigen,
         Integer usuario
 ) {

  Map<Integer, String> numeros = new LinkedHashMap<>();


  // -------------------------------------------------
  // VALIDAR CUADRE ANTES DE REGISTRAR
  // -------------------------------------------------

  vista(preparados);


  // -------------------------------------------------
  // GENERAR COMPROBANTES POR AGENCIA
  // -------------------------------------------------

  for (Preparado preparado : preparados) {

   String numero = consecutivos.generarNumeroDefinitivo(
           "CA",
           preparado.agencia(),
           usuario
   );


   // ---------------------------------------------
   // IDENTIFICAR ORIGEN DEL COMPROBANTE
   // ---------------------------------------------

   OrigenComprobanteDTO origenComprobante =
           new OrigenComprobanteDTO();

   origenComprobante.setOrigenTipo("AUTO");

   origenComprobante.setModuloOrigen("DEPOSITOS");

   origenComprobante.setProcesoOrigen("TRASLADO_CUENTAS");

   origenComprobante.setTablaOrigen(
           "depositos.cuentas_ahorro"
   );

   origenComprobante.setIdOrigen(
           idCuentaOrigen.longValue()
   );


   // ---------------------------------------------
   // REGISTRAR COMPROBANTE
   // ---------------------------------------------

   registro.registrarComprobante(
           preparado.agencia(),
           "CA",
           numero,
           preparado.concepto(),
           origenComprobante,
           preparado.movimientos(),
           usuario
   );


   // ---------------------------------------------
   // GUARDAR NÚMERO GENERADO
   // ---------------------------------------------

   numeros.put(
           preparado.agencia(),
           numero
   );
  }

  return numeros;
 }
}
