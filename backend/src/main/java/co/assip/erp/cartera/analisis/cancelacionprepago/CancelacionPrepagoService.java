package co.assip.erp.cartera.analisis.cancelacionprepago;

import co.assip.erp.cartera.analisis.cancelacionprepago.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CancelacionPrepagoService {

 private final CancelacionPrepagoRepository repository;

 public List<LocalDate> listarCortes() {
  return repository.listarCortes();
 }

 public CancelacionPrepagoResumenDTO obtenerResumen(
         LocalDate fechaCorte
 ) {
  validarFecha(fechaCorte);

  return repository.obtenerResumen(fechaCorte)
          .orElseThrow(() ->
                  new IllegalArgumentException(
                          "No existe información de cancelación y prepago para el corte "
                                  + fechaCorte
                  )
          );
 }

 public List<CancelacionPrepagoLineaDTO> listarLineas(
         LocalDate fechaCorte
 ) {
  validarFecha(fechaCorte);

  return repository.listarLineas(fechaCorte);
 }

 // =========================================================
 // CRÉDITOS DEL CORTE
 // =========================================================

 public List<CancelacionPrepagoDetalleDTO> listarCreditosPorCorte(
         LocalDate fechaCorte
 ) {
  validarFecha(fechaCorte);

  return repository.listarCreditosPorCorte(fechaCorte);
 }

 public List<CancelacionPrepagoDetalleDTO> listarCreditosPorCorteYClasificacion(
         LocalDate fechaCorte,
         String clasificacion
 ) {
  validarFecha(fechaCorte);
  validarTexto(clasificacion, "clasificación");

  return repository.listarCreditosPorCorteYClasificacion(
          fechaCorte,
          clasificacion.trim()
  );
 }

 // =========================================================
 // CONSOLIDADO HISTÓRICO POR CRÉDITO
 // =========================================================

 public List<CancelacionPrepagoCreditoDTO> listarCreditos() {
  return repository.listarCreditos();
 }

 public List<CancelacionPrepagoCreditoDTO> listarCreditosPorClasificacion(
         String clasificacion
 ) {
  validarTexto(clasificacion, "clasificación");

  return repository.listarCreditosPorClasificacion(
          clasificacion.trim()
  );
 }

 public CancelacionPrepagoCreditoDTO obtenerCredito(
         Long idCarteraCredito
 ) {
  validarId(
          idCarteraCredito,
          "idCarteraCredito"
  );

  return repository.obtenerCredito(idCarteraCredito)
          .orElseThrow(() ->
                  new IllegalArgumentException(
                          "No existe información de cancelación y prepago para el crédito "
                                  + idCarteraCredito
                  )
          );
 }

 // =========================================================
 // DETALLE AUDITABLE
 // =========================================================

 public List<CancelacionPrepagoDetalleDTO> listarDetalle(
         LocalDate fechaCorte
 ) {
  validarFecha(fechaCorte);

  return repository.listarDetalle(fechaCorte);
 }

 public List<CancelacionPrepagoDetalleDTO> listarDetallePorLinea(
         LocalDate fechaCorte,
         Long idLineaCredito
 ) {
  validarFecha(fechaCorte);
  validarId(
          idLineaCredito,
          "idLineaCredito"
  );

  return repository.listarDetallePorLinea(
          fechaCorte,
          idLineaCredito
  );
 }

 public List<CancelacionPrepagoDetalleDTO> listarHistoriaCredito(
         Long idCarteraCredito
 ) {
  validarId(
          idCarteraCredito,
          "idCarteraCredito"
  );

  return repository.listarHistoriaCredito(
          idCarteraCredito
  );
 }

 // =========================================================
 // VALIDACIONES
 // =========================================================

 private void validarFecha(
         LocalDate fechaCorte
 ) {
  if (fechaCorte == null) {
   throw new IllegalArgumentException(
           "La fecha de corte es obligatoria."
   );
  }
 }

 private void validarId(
         Long id,
         String campo
 ) {
  if (id == null || id <= 0) {
   throw new IllegalArgumentException(
           campo + " debe ser mayor que cero."
   );
  }
 }

 private void validarTexto(
         String valor,
         String campo
 ) {
  if (valor == null || valor.isBlank()) {
   throw new IllegalArgumentException(
           "La " + campo + " es obligatoria."
   );
  }
 }
}