
package co.assip.erp.depositos.traslado_cuentas;

import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasCuentaDTO;
import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasEntradaDTO;
import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasPreviewDTO;
import co.assip.erp.depositos.traslado_cuentas.dto.TrasladoCuentasResultadoDTO;

import co.assip.erp.shared.personas.dto.PersonaBusquedaDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


// =========================================================
// CONTROLADOR
// TRASLADO INTEGRAL DE CUENTAS ENTRE AGENCIAS
// =========================================================

@RestController
@RequiredArgsConstructor
@RequestMapping("/depositos/traslado-cuentas")
public class TrasladoCuentasController {

 // =====================================================
 // DEPENDENCIAS
 // =====================================================

 private final TrasladoCuentasService service;

 private final JdbcTemplate jdbc;


 // =====================================================
 // BUSCAR ASOCIADOS
 // =====================================================

 @GetMapping("/buscar-asociados")
 public List<PersonaBusquedaDTO> buscar(
         @RequestParam(defaultValue = "") String documento,
         @RequestParam(defaultValue = "") String nombres,
         @RequestParam(defaultValue = "") String primerApellido,
         @RequestParam(defaultValue = "") String segundoApellido
 ) {

  // -------------------------------------------------
  // NORMALIZAR FILTROS
  // -------------------------------------------------

  String filtroDocumento = documento.trim();

  String filtroNombres = nombres.trim();

  String filtroPrimerApellido = primerApellido.trim();

  String filtroSegundoApellido = segundoApellido.trim();


  // -------------------------------------------------
  // VALIDAR CRITERIOS DE BÚSQUEDA
  // -------------------------------------------------

  if (
          filtroDocumento.isEmpty()
                  && filtroNombres.isEmpty()
                  && filtroPrimerApellido.isEmpty()
                  && filtroSegundoApellido.isEmpty()
  ) {

   return List.of();
  }


  // -------------------------------------------------
  // CONSULTAR ASOCIADOS
  // -------------------------------------------------

  String sql = """
                SELECT
                    dp.id_datos_personal,
                    dp.documento,
                    TRIM(
                        CONCAT_WS(
                            ' ',
                            dp.primer_apellido,
                            dp.segundo_apellido,
                            dp.nombres
                        )
                    ) AS nombre_completo
                FROM hoja_vida.datos_personales dp
                WHERE dp.documento ILIKE ?
                  AND dp.nombres ILIKE ?
                  AND dp.primer_apellido ILIKE ?
                  AND dp.segundo_apellido ILIKE ?
                ORDER BY
                    dp.primer_apellido,
                    dp.segundo_apellido,
                    dp.nombres
                LIMIT 100
                """;


  // -------------------------------------------------
  // EJECUTAR CONSULTA
  // -------------------------------------------------

  return jdbc.query(
          sql,
          (rs, rowNum) -> {

           PersonaBusquedaDTO persona =
                   new PersonaBusquedaDTO();

           persona.setIdDatosPersonal(
                   rs.getLong("id_datos_personal")
           );

           persona.setDocumento(
                   rs.getString("documento")
           );

           persona.setNombreCompleto(
                   rs.getString("nombre_completo")
           );

           return persona;
          },
          "%" + filtroDocumento + "%",
          "%" + filtroNombres + "%",
          "%" + filtroPrimerApellido + "%",
          "%" + filtroSegundoApellido + "%"
  );
 }


 // =====================================================
 // CONSULTAR CUENTAS DEL ASOCIADO
 // =====================================================

 @GetMapping("/cuentas/{idDatosPersonal}")
 public List<TrasladoCuentasCuentaDTO> cuentas(
         @PathVariable Integer idDatosPersonal
 ) {

  return service.cuentas(idDatosPersonal);
 }


 // =====================================================
 // GENERAR VISTA PREVIA DEL TRASLADO
 // =====================================================

 @PostMapping("/preview")
 public TrasladoCuentasPreviewDTO preview(
         @RequestBody TrasladoCuentasEntradaDTO entrada
 ) {

  return service.preview(entrada);
 }


 // =====================================================
 // APLICAR TRASLADO INTEGRAL
 // =====================================================

 @PostMapping("/aplicar")
 public TrasladoCuentasResultadoDTO aplicar(
         @RequestBody TrasladoCuentasEntradaDTO entrada
 ) {

  return service.aplicar(entrada);
 }

}
