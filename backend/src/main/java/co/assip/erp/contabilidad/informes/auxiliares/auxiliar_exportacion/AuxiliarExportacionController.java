package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_exportacion;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/** Exportación de auxiliares por tercero; reutiliza el permiso de consulta de auxiliares. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/contabilidad/informes/auxiliares/exportacion")
public class AuxiliarExportacionController {
    private final AuxiliarExportacionExcelService excel;
    private final UsuarioSesionService sesion;
    private final UsuarioRepository usuarios;
    private final AccessValidator acceso;

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportar(@RequestParam Integer idAgencia,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaInicial,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fechaFinal,
            @RequestParam(defaultValue="RESUMIDO") String modalidad) {
        var usuario=usuarios.findById(sesion.idUsuario())
            .filter(u->Boolean.TRUE.equals(u.getActivo()))
            .orElseThrow(()->new SecurityException("Usuario inactivo"));
        acceso.validarAcceso(usuario,"CONTABILIDAD_AUXILIAR_CUENTAS_VIEW");
        sesion.validarAgencia(idAgencia);
        if(fechaInicial.isAfter(fechaFinal)) throw new IllegalArgumentException("Rango de fechas inválido");
        if(!modalidad.equals("RESUMIDO")&&!modalidad.equals("DETALLADO"))
            throw new IllegalArgumentException("Modalidad no válida");
        byte[] archivo=excel.generar(idAgencia,fechaInicial,fechaFinal,modalidad.equals("RESUMIDO"));
        String nombre="auxiliar_"+modalidad.toLowerCase()+"_"+fechaInicial+"_"+fechaFinal+".xlsx";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+nombre+"\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(archivo);
    }
}
