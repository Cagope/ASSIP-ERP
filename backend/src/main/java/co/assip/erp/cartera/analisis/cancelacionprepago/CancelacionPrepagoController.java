package co.assip.erp.cartera.analisis.cancelacionprepago;

import co.assip.erp.cartera.analisis.cancelacionprepago.dto.CancelacionPrepagoCreditoDTO;
import co.assip.erp.cartera.analisis.cancelacionprepago.dto.CancelacionPrepagoDetalleDTO;
import co.assip.erp.cartera.analisis.cancelacionprepago.dto.CancelacionPrepagoLineaDTO;
import co.assip.erp.cartera.analisis.cancelacionprepago.dto.CancelacionPrepagoResumenDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
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

 private static final String PERMISO_VIEW = "CARTERA_ANALISIS_VIEW";

 private final CancelacionPrepagoService service;
 private final UsuarioSesionService usuarioSesionService;
 private final AccessValidator accessValidator;

 // =========================================================
 // CONTROL
 // =========================================================

 @GetMapping("/control")
 public Map<String, Object> control() {

  validarAcceso();

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

  validarAcceso();

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

  validarAcceso();

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

  validarAcceso();

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

  validarAcceso();

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

  validarAcceso();

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

  validarAcceso();

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

  validarAcceso();

  return service.listarHistoriaCredito(
          idCarteraCredito
  );
 }

 // =========================================================
 // SEGURIDAD
 // =========================================================

 private void validarAcceso() {

  Usuario usuarioActual = new Usuario();
  usuarioActual.setIdUsuario(
          usuarioSesionService.idUsuario()
  );

  accessValidator.validarAcceso(
          usuarioActual,
          PERMISO_VIEW
  );
 }
}