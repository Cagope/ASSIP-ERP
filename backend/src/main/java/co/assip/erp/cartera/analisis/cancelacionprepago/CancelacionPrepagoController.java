package co.assip.erp.cartera.analisis.cancelacionprepago;

import co.assip.erp.cartera.analisis.cancelacionprepago.dto.CancelacionPrepagoCreditoDTO;
import co.assip.erp.cartera.analisis.cancelacionprepago.dto.CancelacionPrepagoDetalleDTO;
import co.assip.erp.cartera.analisis.cancelacionprepago.dto.CancelacionPrepagoLineaDTO;
import co.assip.erp.cartera.analisis.cancelacionprepago.dto.CancelacionPrepagoResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartera/analisis/cancelacion-prepago")
@RequiredArgsConstructor
public class CancelacionPrepagoController {

 private final CancelacionPrepagoService service;

 // =========================================================
 // CONTROL
 // =========================================================

 @GetMapping("/control")
 public Map<String, Object> control() {

  Map<String, Object> respuesta = new LinkedHashMap<>();

  respuesta.put("estado", "OK");
  respuesta.put("proceso", "Cancelación y Prepago");
  respuesta.put("soloLectura", true);

  return respuesta;
 }

 // =========================================================
 // CORTES
 // =========================================================

 @GetMapping("/cortes")
 public List<LocalDate> cortes() {
  return service.listarCortes();
 }

 // =========================================================
 // RESUMEN
 // =========================================================

 @GetMapping("/resumen")
 public CancelacionPrepagoResumenDTO resumen(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte
 ) {
  return service.obtenerResumen(fechaCorte);
 }

 // =========================================================
 // LÍNEAS
 // =========================================================

 @GetMapping("/lineas")
 public List<CancelacionPrepagoLineaDTO> lineas(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte
 ) {
  return service.listarLineas(fechaCorte);
 }

 // =========================================================
 // CRÉDITOS DEL CORTE
 // =========================================================

 @GetMapping("/creditos")
 public List<CancelacionPrepagoDetalleDTO> creditos(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte,

         @RequestParam(required = false)
         String clasificacion
 ) {

  if (clasificacion == null || clasificacion.isBlank()) {
   return service.listarCreditosPorCorte(fechaCorte);
  }

  return service.listarCreditosPorCorteYClasificacion(
          fechaCorte,
          clasificacion
  );
 }

 // =========================================================
 // CRÉDITO CONSOLIDADO HISTÓRICO
 // =========================================================

 @GetMapping("/creditos/{idCarteraCredito}")
 public CancelacionPrepagoCreditoDTO credito(
         @PathVariable
         Long idCarteraCredito
 ) {
  return service.obtenerCredito(idCarteraCredito);
 }

 // =========================================================
 // DETALLE DEL CORTE
 // =========================================================

 @GetMapping("/detalle")
 public List<CancelacionPrepagoDetalleDTO> detalle(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte,

         @RequestParam(required = false)
         Long idLineaCredito
 ) {

  if (idLineaCredito == null) {
   return service.listarDetalle(fechaCorte);
  }

  return service.listarDetallePorLinea(
          fechaCorte,
          idLineaCredito
  );
 }

 // =========================================================
 // HISTORIA AUDITABLE DEL CRÉDITO
 // =========================================================

 @GetMapping("/detalle/credito/{idCarteraCredito}")
 public List<CancelacionPrepagoDetalleDTO> historiaCredito(
         @PathVariable
         Long idCarteraCredito
 ) {
  return service.listarHistoriaCredito(
          idCarteraCredito
  );
 }
}