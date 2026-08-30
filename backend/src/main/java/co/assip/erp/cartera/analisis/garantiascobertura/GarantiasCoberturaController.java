package co.assip.erp.cartera.analisis.garantiascobertura;
import co.assip.erp.cartera.analisis.garantiascobertura.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
@RestController @RequestMapping("/cartera/analisis/garantias-cobertura") @RequiredArgsConstructor
public class GarantiasCoberturaController {
 private final GarantiasCoberturaService service;
 @GetMapping("/control") public Map<String,Object> control(){return Map.of("proceso","Garantías y Cobertura","estado","OK","soloLectura",true);}
 @GetMapping("/cortes") public List<LocalDate> cortes(){return service.listarCortes();}
 @GetMapping("/resumen") public GarantiasCoberturaResumenDTO resumen(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCorte){return service.obtenerResumen(fechaCorte);}
 @GetMapping("/bienes") public List<GarantiasCoberturaBienDTO> bienes(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCorte){return service.listarBienes(fechaCorte);}
 @GetMapping("/bienes/insuficientes") public List<GarantiasCoberturaBienDTO> bienesInsuficientes(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCorte){return service.listarBienesInsuficientes(fechaCorte);}
 @GetMapping("/creditos") public List<GarantiasCoberturaCreditoDTO> creditos(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCorte){return service.listarCreditos(fechaCorte);}
 @GetMapping("/creditos/insuficientes") public List<GarantiasCoberturaCreditoDTO> creditosInsuficientes(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCorte){return service.listarCreditosInsuficientes(fechaCorte);}
 @GetMapping("/detalle") public List<GarantiasCoberturaDetalleDTO> detalle(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCorte){return service.listarDetalle(fechaCorte);}
 @GetMapping("/detalle/bien/{idBien}") public List<GarantiasCoberturaDetalleDTO> detalleBien(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCorte,@PathVariable Long idBien){return service.listarDetallePorBien(fechaCorte,idBien);}
 @GetMapping("/detalle/credito/{idCarteraCredito}") public List<GarantiasCoberturaDetalleDTO> detalleCredito(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaCorte,@PathVariable Long idCarteraCredito){return service.listarDetallePorCredito(fechaCorte,idCarteraCredito);}
}
