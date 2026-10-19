
package co.assip.erp.depositos.traslado_cuentas;

import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;

import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasCuentaDTO;
import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasEntradaDTO;
import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasPreviewDTO;
import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasResultadoDTO;
import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasValidacionDTO;

import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


// =========================================================
// SERVICIO
// TRASLADO INTEGRAL DE CUENTAS ENTRE AGENCIAS
// =========================================================

@Service
@RequiredArgsConstructor
public class TrasladoCuentasService {

 // =====================================================
 // DEPENDENCIAS
 // =====================================================

 private final TrasladoCuentasRepository repo;

 private final TrasladoCuentasContabilizacionService contabilidad;

 private final DepositosMovimientoService movimientos;

 private final UsuarioSesionService sesion;

 private final JdbcTemplate jdbc;


 // =====================================================
 // ESTRUCTURA DE ANÁLISIS
 // =====================================================

 private record Analisis(
         List<TrasladoCuentasRepository.Cuenta> cuentas,
         Map<Integer, Integer> formas,
         List<String> problemas,
         boolean cdat,
         boolean cartera,
         boolean duplicadas
 ) {
 }


 // =====================================================
 // CONSULTAR CUENTAS DEL ASOCIADO
 // =====================================================

 @Transactional(readOnly = true)
 public List<TrasladoCuentasCuentaDTO> cuentas(
         Integer persona
 ) {

  if (persona == null || persona <= 0) {

   throw new IllegalArgumentException(
           "Identifique al asociado."
   );
  }

  return repo.cuentas(persona, false)
          .stream()
          .map(cuenta -> new TrasladoCuentasCuentaDTO(
                  cuenta.idCuentaAhorro(),
                  cuenta.idAgencia(),
                  cuenta.idFormaAhorro(),
                  cuenta.codigoCuenta(),
                  cuenta.codigoForma(),
                  cuenta.nombreForma(),
                  cuenta.estadoCuenta(),
                  cuenta.saldoActual(),
                  cuenta.operativo(),
                  cuenta.operativo()
                          ? ""
                          : "Estado no operativo"
          ))
          .toList();
 }


 // =====================================================
 // VALIDAR DATOS DE ENTRADA
 // =====================================================

 private void validarEntrada(
         TrasladoCuentasEntradaDTO entrada
 ) {

  if (
          entrada == null
                  || entrada.idDatosPersonal() == null
                  || entrada.idDatosPersonal() <= 0
                  || entrada.idAgenciaOrigen() == null
                  || entrada.idAgenciaDestino() == null
                  || entrada.fechaProceso() == null
  ) {

   throw new IllegalArgumentException(
           "Asociado, agencias y fecha son obligatorios."
   );
  }


  // -------------------------------------------------
  // VALIDAR AGENCIAS
  // -------------------------------------------------

  if (
          entrada.idAgenciaOrigen().equals(
                  entrada.idAgenciaDestino()
          )
  ) {

   throw new IllegalArgumentException(
           "Origen y destino deben ser diferentes."
   );
  }

  Set<Integer> agenciasOperativas = Set.of(2, 3);

  if (
          !agenciasOperativas.contains(
                  entrada.idAgenciaOrigen()
          )
                  || !agenciasOperativas.contains(
                  entrada.idAgenciaDestino()
          )
  ) {

   throw new IllegalArgumentException(
           "Solo se permiten traslados entre agencias 2 y 3."
   );
  }


  // -------------------------------------------------
  // VALIDAR COMENTARIO
  // -------------------------------------------------

  if (
          entrada.comentario() == null
                  || entrada.comentario().isBlank()
  ) {

   throw new IllegalArgumentException(
           "El comentario del traslado es obligatorio."
   );
  }

  if (entrada.comentario().length() > 350) {

   throw new IllegalArgumentException(
           "El comentario del traslado supera 350 caracteres."
   );
  }


  // -------------------------------------------------
  // VALIDAR FECHA DE TRABAJO
  // -------------------------------------------------

  if (!LocalDate.now().equals(entrada.fechaProceso())) {

   throw new IllegalArgumentException(
           "El traslado debe registrarse con la fecha "
                   + "de trabajo del servidor."
   );
  }


  // -------------------------------------------------
  // VALIDAR AGENCIA AUTORIZADA
  // -------------------------------------------------

  sesion.validarAgencia(
          entrada.idAgenciaOrigen()
  );
 }


 // =====================================================
 // ANALIZAR CUENTAS Y RESTRICCIONES
 // =====================================================

 private Analisis analizar(
         TrasladoCuentasEntradaDTO entrada,
         boolean bloquear
 ) {

  List<TrasladoCuentasRepository.Cuenta> todas =
          repo.cuentas(
                  entrada.idDatosPersonal(),
                  bloquear
          );

  List<String> problemas = new ArrayList<>();

  Map<Integer, Integer> formas = new HashMap<>();


  // -------------------------------------------------
  // CUENTAS DE LA AGENCIA ORIGEN
  // -------------------------------------------------

  List<TrasladoCuentasRepository.Cuenta> cuentasOrigen =
          todas.stream()
                  .filter(cuenta ->
                          entrada.idAgenciaOrigen().equals(
                                  cuenta.idAgencia()
                          )
                  )
                  .toList();


  // -------------------------------------------------
  // CUENTAS CON SALDO POSITIVO
  // -------------------------------------------------

  List<TrasladoCuentasRepository.Cuenta> cuentasConSaldo =
          cuentasOrigen.stream()
                  .filter(cuenta ->
                          cuenta.saldoActual().signum() > 0
                  )
                  .toList();

  if (cuentasConSaldo.isEmpty()) {

   problemas.add(
           "El asociado no tiene cuentas con saldo "
                   + "en la agencia origen."
   );
  }


  // -------------------------------------------------
  // VALIDAR SALDOS EN OTRAS AGENCIAS
  // -------------------------------------------------

  boolean saldosOtrasAgencias =
          todas.stream()
                  .anyMatch(cuenta ->
                          !entrada.idAgenciaOrigen().equals(
                                  cuenta.idAgencia()
                          )
                                  && cuenta.saldoActual().signum() > 0
                  );

  if (saldosOtrasAgencias) {

   problemas.add(
           "El asociado tiene cuentas con saldo en otra agencia; "
                   + "revise su situación antes de trasladar."
   );
  }


  // -------------------------------------------------
  // VALIDAR ESTADOS DE LAS CUENTAS
  // -------------------------------------------------

  for (TrasladoCuentasRepository.Cuenta cuenta : cuentasConSaldo) {

   if (!cuenta.operativo()) {

    problemas.add(
            "La cuenta "
                    + cuenta.codigoCuenta()
                    + " no está operativa: "
                    + cuenta.estadoCuenta()
    );
   }

   if (!Set.of("A", "I").contains(cuenta.estadoCuenta())) {

    problemas.add(
            "La cuenta "
                    + cuenta.codigoCuenta()
                    + " tiene estado no permitido para traslado."
    );
   }
  }


  // =================================================
  // VALIDAR CDAT DEL ASOCIADO
  // =================================================

  String sqlCdat = """
                SELECT COUNT(*)
                FROM cdat.cuentas_cdats
                WHERE id_datos_personal = ?
                  AND (
                      TRIM(estado_cdat) = 'A'
                      OR COALESCE(saldo_actual_cdat, 0) <> 0
                  )
                """;

  Integer cantidadCdat = jdbc.queryForObject(
          sqlCdat,
          Integer.class,
          entrada.idDatosPersonal()
  );

  boolean tieneCdat =
          cantidadCdat != null && cantidadCdat > 0;

  if (tieneCdat) {

   problemas.add(
           "El asociado tiene CDAT activo o con saldo."
   );
  }


  // =================================================
  // VALIDAR CARTERA DEL ASOCIADO
  // =================================================

  String sqlCartera = """
                SELECT COUNT(*)
                FROM cartera.carteras_creditos
                WHERE id_datos_personal = ?
                  AND (
                      TRIM(codigo_estado_cartera) = 'A'
                      OR COALESCE(saldo_actual, 0) <> 0
                  )
                """;

  Integer cantidadCartera = jdbc.queryForObject(
          sqlCartera,
          Integer.class,
          entrada.idDatosPersonal()
  );

  boolean tieneCartera =
          cantidadCartera != null && cantidadCartera > 0;

  if (tieneCartera) {

   problemas.add(
           "El asociado tiene cartera activa o con saldo."
   );
  }


  // =================================================
  // VALIDAR FORMAS Y DUPLICADOS EN DESTINO
  // =================================================

  boolean duplicadas = false;

  for (TrasladoCuentasRepository.Cuenta cuenta : cuentasConSaldo) {

   try {

    Integer formaDestino = repo.formaDestino(
            cuenta,
            entrada.idAgenciaDestino()
    );

    formas.put(
            cuenta.idCuentaAhorro(),
            formaDestino
    );

    if (
            repo.duplicada(
                    cuenta,
                    entrada.idAgenciaDestino(),
                    formaDestino
            )
    ) {

     duplicadas = true;

     problemas.add(
             "Ya existe en destino la cuenta "
                     + cuenta.codigoCuenta()
                     + " de la forma "
                     + cuenta.codigoForma()
     );
    }

   } catch (IllegalStateException ex) {

    problemas.add(
            ex.getMessage()
    );
   }
  }


  // =================================================
  // RESULTADO DEL ANÁLISIS
  // =================================================

  return new Analisis(
          cuentasConSaldo,
          formas,
          problemas,
          tieneCdat,
          tieneCartera,
          duplicadas
  );
 }


 // =====================================================
 // GENERAR VISTA PREVIA
 // =====================================================

 @Transactional(readOnly = true)
 public TrasladoCuentasPreviewDTO preview(
         TrasladoCuentasEntradaDTO entrada
 ) {

  validarEntrada(entrada);

  Analisis analisis = analizar(
          entrada,
          false
  );

  Map<String, String> persona = repo.persona(
          entrada.idDatosPersonal()
  );


  // -------------------------------------------------
  // CUENTAS A TRASLADAR
  // -------------------------------------------------

  List<TrasladoCuentasCuentaDTO> cuentas =
          analisis.cuentas()
                  .stream()
                  .map(cuenta -> new TrasladoCuentasCuentaDTO(
                          cuenta.idCuentaAhorro(),
                          cuenta.idAgencia(),
                          cuenta.idFormaAhorro(),
                          cuenta.codigoCuenta(),
                          cuenta.codigoForma(),
                          cuenta.nombreForma(),
                          cuenta.estadoCuenta(),
                          cuenta.saldoActual(),
                          cuenta.operativo(),
                          ""
                  ))
                  .toList();


  // -------------------------------------------------
  // VALOR TOTAL DEL TRASLADO
  // -------------------------------------------------

  BigDecimal total = analisis.cuentas()
          .stream()
          .map(TrasladoCuentasRepository.Cuenta::saldoActual)
          .reduce(
                  BigDecimal.ZERO,
                  BigDecimal::add
          );


  // -------------------------------------------------
  // PREPARAR VISTA CONTABLE
  // -------------------------------------------------

  List<TrasladoCuentasPreviewDTO.ComprobantePreview> comprobantes =
          List.of();

  if (analisis.problemas().isEmpty()) {

   try {

    List<TrasladoCuentasContabilizacionService.Preparado> preparados =
            contabilidad.preparar(
                    entrada.idDatosPersonal(),
                    persona.get("documento"),
                    persona.get("nombre"),
                    entrada.idAgenciaOrigen(),
                    entrada.idAgenciaDestino(),
                    entrada.fechaProceso(),
                    analisis.cuentas(),
                    analisis.formas()
            );

    comprobantes = contabilidad.vista(
            preparados
    );

   } catch (IllegalStateException ex) {

    analisis.problemas().add(
            ex.getMessage()
    );
   }
  }


  // -------------------------------------------------
  // RESULTADO DE VALIDACIONES
  // -------------------------------------------------

  TrasladoCuentasValidacionDTO validacion =
          new TrasladoCuentasValidacionDTO(
                  analisis.problemas().isEmpty(),
                  analisis.cdat(),
                  analisis.cartera(),
                  analisis.duplicadas(),
                  List.copyOf(analisis.problemas())
          );


  // -------------------------------------------------
  // RESPUESTA DE VISTA PREVIA
  // -------------------------------------------------

  return new TrasladoCuentasPreviewDTO(
          entrada.idDatosPersonal(),
          persona.get("documento"),
          persona.get("nombre"),
          entrada.idAgenciaOrigen(),
          entrada.idAgenciaDestino(),
          cuentas,
          total,
          validacion,
          comprobantes
  );
 }


 // =====================================================
 // APLICAR TRASLADO INTEGRAL
 // =====================================================

 @Transactional
 public TrasladoCuentasResultadoDTO aplicar(
         TrasladoCuentasEntradaDTO entrada
 ) {

  validarEntrada(entrada);

  Integer usuario = sesion.idUsuario();


  // -------------------------------------------------
  // BLOQUEAR ASOCIADO
  // -------------------------------------------------

  repo.bloquearAsociado(
          entrada.idDatosPersonal()
  );


  // -------------------------------------------------
  // ANALIZAR CUENTAS CON BLOQUEO
  // -------------------------------------------------

  Analisis analisis = analizar(
          entrada,
          true
  );

  if (!analisis.problemas().isEmpty()) {

   throw new IllegalStateException(
           String.join(
                   "; ",
                   analisis.problemas()
           )
   );
  }


  // -------------------------------------------------
  // IDENTIFICAR ASOCIADO
  // -------------------------------------------------

  Map<String, String> persona = repo.persona(
          entrada.idDatosPersonal()
  );


  // -------------------------------------------------
  // PREPARAR COMPROBANTES CONTABLES
  // -------------------------------------------------

  List<TrasladoCuentasContabilizacionService.Preparado> preparados =
          contabilidad.preparar(
                  entrada.idDatosPersonal(),
                  persona.get("documento"),
                  persona.get("nombre"),
                  entrada.idAgenciaOrigen(),
                  entrada.idAgenciaDestino(),
                  entrada.fechaProceso(),
                  analisis.cuentas(),
                  analisis.formas()
          );


  // -------------------------------------------------
  // VALIDAR CUADRE CONTABLE
  // -------------------------------------------------

  contabilidad.vista(
          preparados
  );


  // -------------------------------------------------
  // REGISTRAR COMPROBANTES CONTABLES
  // -------------------------------------------------

  Map<Integer, String> numeros = contabilidad.registrar(
          preparados,
          entrada.idDatosPersonal(),
          analisis.cuentas().get(0).idCuentaAhorro(),
          usuario
  );


  // =================================================
  // TRASLADAR CUENTAS DE AHORRO
  // =================================================

  List<TrasladoCuentasResultadoDTO.CuentaTrasladada> detalle =
          new ArrayList<>();

  BigDecimal total = BigDecimal.ZERO;


  // -------------------------------------------------
  // PROCESAR CADA CUENTA
  // -------------------------------------------------

  for (TrasladoCuentasRepository.Cuenta cuenta : analisis.cuentas()) {

   // ---------------------------------------------
   // COMENTARIO DE TRAZABILIDAD
   // ---------------------------------------------

   String marca =
           "Traslado "
                   + entrada.fechaProceso()
                   + " de agencia "
                   + entrada.idAgenciaOrigen()
                   + " a "
                   + entrada.idAgenciaDestino()
                   + ". "
                   + entrada.comentario().trim();

   String comentarioDestino = comentario(
           cuenta.comentario(),
           "Recibida desde agencia "
                   + entrada.idAgenciaOrigen()
                   + ". "
                   + marca
   );


   // ---------------------------------------------
   // CREAR CUENTA EN AGENCIA DESTINO
   // ---------------------------------------------

   Integer idCuentaDestino = repo.crearDestino(
           cuenta,
           entrada.idAgenciaDestino(),
           analisis.formas().get(
                   cuenta.idCuentaAhorro()
           ),
           comentarioDestino,
           usuario
   );


   // ---------------------------------------------
   // REGISTRAR DÉBITO EN AGENCIA ORIGEN
   // TIPO DE MOVIMIENTO 771
   // ---------------------------------------------

   DepositosMovimientoDTO salida = movimiento(
           cuenta.idCuentaAhorro(),
           entrada.fechaProceso(),
           "771",
           numeros.get(
                   entrada.idAgenciaOrigen()
           ),
           cuenta.saldoActual(),
           true
   );

   movimientos.registrarDebito(
           salida,
           usuario
   );


   // ---------------------------------------------
   // MARCAR CUENTA ORIGEN COMO TRASLADADA
   // ---------------------------------------------

   repo.marcarTrasladada(
           cuenta.idCuentaAhorro(),
           entrada.fechaProceso(),
           comentario(
                   cuenta.comentario(),
                   marca
           ),
           usuario
   );


   // ---------------------------------------------
   // REGISTRAR CRÉDITO EN AGENCIA DESTINO
   // TIPO DE MOVIMIENTO 221
   // ---------------------------------------------

   DepositosMovimientoDTO movimientoEntrada = movimiento(
           idCuentaDestino,
           entrada.fechaProceso(),
           "221",
           numeros.get(
                   entrada.idAgenciaDestino()
           ),
           cuenta.saldoActual(),
           false
   );

   movimientos.registrarCredito(
           movimientoEntrada,
           usuario
   );


   // ---------------------------------------------
   // REGISTRAR RESULTADO DE LA CUENTA
   // ---------------------------------------------

   detalle.add(
           new TrasladoCuentasResultadoDTO.CuentaTrasladada(
                   cuenta.idCuentaAhorro(),
                   idCuentaDestino,
                   cuenta.codigoCuenta(),
                   cuenta.saldoActual()
           )
   );

   total = total.add(
           cuenta.saldoActual()
   );
  }


  // =================================================
  // COMPROBANTES GENERADOS
  // =================================================

  List<TrasladoCuentasResultadoDTO.ComprobanteGenerado> comprobantes =
          numeros.entrySet()
                  .stream()
                  .map(numero ->
                          new TrasladoCuentasResultadoDTO.ComprobanteGenerado(
                                  numero.getKey(),
                                  "CA",
                                  numero.getValue()
                          )
                  )
                  .toList();


  // =================================================
  // RESPUESTA DEL TRASLADO
  // =================================================

  return new TrasladoCuentasResultadoDTO(
          entrada.idDatosPersonal(),
          entrada.idAgenciaOrigen(),
          entrada.idAgenciaDestino(),
          total,
          detalle.size(),
          List.copyOf(detalle),
          comprobantes
  );
 }


 // =====================================================
 // CONSERVAR TRAZABILIDAD DE COMENTARIOS
 // =====================================================

 private String comentario(
         String anterior,
         String nuevo
 ) {

  String texto =
          (anterior == null || anterior.isBlank()
                  ? ""
                  : anterior.trim() + " | ")
                  + nuevo;

  if (texto.length() > 500) {

   throw new IllegalStateException(
           "El comentario de la cuenta supera 500 caracteres; "
                   + "debe conservarse la trazabilidad."
   );
  }

  return texto;
 }


 // =====================================================
 // CONSTRUIR MOVIMIENTO DE DEPÓSITOS
 // =====================================================

 private DepositosMovimientoDTO movimiento(
         Integer cuenta,
         LocalDate fecha,
         String tipo,
         String numero,
         BigDecimal valor,
         boolean debito
 ) {

  DepositosMovimientoDTO movimiento =
          new DepositosMovimientoDTO();

  movimiento.setIdCuentaAhorro(
          cuenta
  );

  movimiento.setFechaMovimiento(
          fecha
  );

  movimiento.setTipoComprobante(
          "CA"
  );

  movimiento.setNumeroComprobante(
          numero
  );

  movimiento.setTipoMovimiento(
          tipo
  );

  movimiento.setModulo("02");

  movimiento.setModuloOrigen(
          "DEPOSITOS"
  );

  movimiento.setProcesoOrigen(
          "TRASLADO_CUENTAS"
  );

  movimiento.setIdOrigen(
          cuenta.longValue()
  );

  if (debito) {

   movimiento.setValorDebito(
           valor
   );

  } else {

   movimiento.setValorCredito(
           valor
   );
  }

  return movimiento;
 }

}
