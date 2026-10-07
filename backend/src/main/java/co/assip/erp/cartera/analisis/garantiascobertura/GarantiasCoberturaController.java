package co.assip.erp.cartera.analisis.garantiascobertura;

import co.assip.erp.cartera.analisis.garantiascobertura.dto.*;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cartera/analisis/garantias-cobertura")
@RequiredArgsConstructor
public class GarantiasCoberturaController {

 private static final String PERMISO_VIEW = "CARTERA_ANALISIS_VIEW";

 private final GarantiasCoberturaService service;
 private final UsuarioSesionService usuarioSesionService;
 private final AccessValidator accessValidator;

 // =========================================================
 // CONTROL
 // =========================================================

 @GetMapping("/control")
 public Map<String, Object> control() {

  validarAcceso();

  return Map.of(
          "proceso", "Garantías y Cobertura",
          "estado", "OK",
          "soloLectura", true
  );
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
 public GarantiasCoberturaResumenDTO resumen(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte
 ) {

  validarAcceso();

  return service.obtenerResumen(fechaCorte);
 }

 // =========================================================
 // BIENES
 // =========================================================

 @GetMapping("/bienes")
 public List<GarantiasCoberturaBienDTO> bienes(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte
 ) {

  validarAcceso();

  return service.listarBienes(fechaCorte);
 }

 @GetMapping("/bienes/insuficientes")
 public List<GarantiasCoberturaBienDTO> bienesInsuficientes(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte
 ) {

  validarAcceso();

  return service.listarBienesInsuficientes(fechaCorte);
 }

 // =========================================================
 // CRÉDITOS
 // =========================================================

 @GetMapping("/creditos")
 public List<GarantiasCoberturaCreditoDTO> creditos(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte
 ) {

  validarAcceso();

  return service.listarCreditos(fechaCorte);
 }

 @GetMapping("/creditos/insuficientes")
 public List<GarantiasCoberturaCreditoDTO> creditosInsuficientes(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte
 ) {

  validarAcceso();

  return service.listarCreditosInsuficientes(fechaCorte);
 }

 // =========================================================
 // DETALLE
 // =========================================================

 @GetMapping("/detalle")
 public List<GarantiasCoberturaDetalleDTO> detalle(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte
 ) {

  validarAcceso();

  return service.listarDetalle(fechaCorte);
 }

 @GetMapping("/detalle/bien/{idBien}")
 public List<GarantiasCoberturaDetalleDTO> detalleBien(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte,

         @PathVariable
         Long idBien
 ) {

  validarAcceso();

  return service.listarDetallePorBien(
          fechaCorte,
          idBien
  );
 }

 @GetMapping("/detalle/credito/{idCarteraCredito}")
 public List<GarantiasCoberturaDetalleDTO> detalleCredito(
         @RequestParam
         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
         LocalDate fechaCorte,

         @PathVariable
         Long idCarteraCredito
 ) {

  validarAcceso();

  return service.listarDetallePorCredito(
          fechaCorte,
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