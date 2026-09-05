package co.assip.erp.cartera.analisis.cancelacionprepago;

import co.assip.erp.cartera.analisis.cancelacionprepago.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CancelacionPrepagoRepository {

 private final JdbcTemplate jdbc;

 public List<LocalDate> listarCortes() {
  return jdbc.query(
          """
          SELECT fecha_corte
          FROM cartera.vw_cancelacion_prepago_resumen
          ORDER BY fecha_corte DESC
          """,
          (r, n) -> r.getObject("fecha_corte", LocalDate.class)
  );
 }

 public Optional<CancelacionPrepagoResumenDTO> obtenerResumen(LocalDate fechaCorte) {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_resumen
          WHERE fecha_corte = ?
          """,
          this::mapResumen,
          fechaCorte
  ).stream().findFirst();
 }

 public List<CancelacionPrepagoLineaDTO> listarLineas(LocalDate fechaCorte) {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_lineas
          WHERE fecha_corte = ?
          ORDER BY
              saldo_expuesto DESC,
              codigo_linea_credito
          """,
          this::mapLinea,
          fechaCorte
  );
 }

 // =========================================================
 // CRÉDITOS DEL CORTE
 // =========================================================

 public List<CancelacionPrepagoDetalleDTO> listarCreditosPorCorte(
         LocalDate fechaCorte
 ) {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_detalle
          WHERE fecha_corte = ?
          ORDER BY
              CASE clasificacion_salida
                  WHEN 'PREPAGO' THEN 0
                  WHEN 'CANCELACION POSTERIOR AL VENCIMIENTO' THEN 1
                  WHEN 'CANCELACION NORMAL' THEN 2
                  WHEN 'AUSENCIA TEMPORAL' THEN 3
                  ELSE 4
              END,
              saldo_corte_anterior DESC,
              id_cartera_credito
          """,
          this::mapDetalle,
          fechaCorte
  );
 }

 public List<CancelacionPrepagoDetalleDTO> listarCreditosPorCorteYClasificacion(
         LocalDate fechaCorte,
         String clasificacion
 ) {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_detalle
          WHERE fecha_corte = ?
            AND clasificacion_salida = ?
          ORDER BY
              saldo_corte_anterior DESC,
              id_cartera_credito
          """,
          this::mapDetalle,
          fechaCorte,
          clasificacion
  );
 }

 // =========================================================
 // CONSOLIDADO HISTÓRICO POR CRÉDITO
 // =========================================================

 public List<CancelacionPrepagoCreditoDTO> listarCreditos() {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_creditos
          ORDER BY
              CASE clasificacion_salida
                  WHEN 'PREPAGO' THEN 0
                  WHEN 'CANCELACION POSTERIOR AL VENCIMIENTO' THEN 1
                  WHEN 'CANCELACION NORMAL' THEN 2
                  ELSE 3
              END,
              saldo_corte_anterior DESC,
              id_cartera_credito
          """,
          this::mapCredito
  );
 }

 public List<CancelacionPrepagoCreditoDTO> listarCreditosPorClasificacion(
         String clasificacion
 ) {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_creditos
          WHERE clasificacion_salida = ?
          ORDER BY
              saldo_corte_anterior DESC,
              id_cartera_credito
          """,
          this::mapCredito,
          clasificacion
  );
 }

 public Optional<CancelacionPrepagoCreditoDTO> obtenerCredito(
         Long idCarteraCredito
 ) {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_creditos
          WHERE id_cartera_credito = ?
          """,
          this::mapCredito,
          idCarteraCredito
  ).stream().findFirst();
 }

 // =========================================================
 // DETALLE AUDITABLE
 // =========================================================

 public List<CancelacionPrepagoDetalleDTO> listarDetalle(
         LocalDate fechaCorte
 ) {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_detalle
          WHERE fecha_corte = ?
          ORDER BY
              CASE clasificacion_salida
                  WHEN 'PREPAGO' THEN 0
                  WHEN 'CANCELACION POSTERIOR AL VENCIMIENTO' THEN 1
                  WHEN 'CANCELACION NORMAL' THEN 2
                  WHEN 'AUSENCIA TEMPORAL' THEN 3
                  ELSE 4
              END,
              saldo_corte_anterior DESC,
              id_cartera_credito
          """,
          this::mapDetalle,
          fechaCorte
  );
 }

 public List<CancelacionPrepagoDetalleDTO> listarDetallePorLinea(
         LocalDate fechaCorte,
         Long idLineaCredito
 ) {
  return jdbc.query(
          """
          WITH detalle_corte AS MATERIALIZED (
              SELECT *
              FROM cartera.vw_cancelacion_prepago_detalle
              WHERE fecha_corte = ?
          )
          SELECT *
          FROM detalle_corte
          WHERE id_linea_credito = ?
          ORDER BY
              saldo_corte_anterior DESC,
              id_cartera_credito
          """,
          this::mapDetalle,
          fechaCorte,
          idLineaCredito
  );
 }

 public List<CancelacionPrepagoDetalleDTO> listarHistoriaCredito(
         Long idCarteraCredito
 ) {
  return jdbc.query(
          """
          SELECT *
          FROM cartera.vw_cancelacion_prepago_detalle
          WHERE id_cartera_credito = ?
          ORDER BY fecha_corte
          """,
          this::mapDetalle,
          idCarteraCredito
  );
 }

 // =========================================================
 // MAPPERS
 // =========================================================

 private CancelacionPrepagoResumenDTO mapResumen(
         ResultSet r,
         int n
 ) throws SQLException {

  return new CancelacionPrepagoResumenDTO(
          i(r, "id_cierre_cartera"),
          d(r, "fecha_corte"),
          d(r, "fecha_corte_siguiente"),
          l(r, "creditos_expuestos"),
          l(r, "personas_expuestas"),
          l(r, "permanencias"),
          l(r, "ausencias_temporales"),
          l(r, "prepagos"),
          l(r, "cancelaciones_normales"),
          l(r, "cancelaciones_posteriores"),
          l(r, "salidas_definitivas"),
          r.getBigDecimal("tasa_permanencia"),
          r.getBigDecimal("tasa_salida_definitiva"),
          r.getBigDecimal("tasa_prepago_poblacion"),
          r.getBigDecimal("participacion_prepago_cancelaciones"),
          r.getBigDecimal("saldo_expuesto"),
          r.getBigDecimal("saldo_previo_prepagos"),
          r.getBigDecimal("saldo_previo_salidas_definitivas"),
          r.getBigDecimal("velocidad_amortizacion_promedio"),
          r.getBigDecimal("mediana_velocidad_amortizacion"),
          l(r, "amortizaciones_aceleradas"),
          l(r, "aumentos_saldo"),
          r.getBigDecimal("porcentaje_amortizacion_acelerada"),
          r.getBigDecimal("vida_contractual_promedio_dias"),
          r.getBigDecimal("vida_efectiva_promedio_dias"),
          r.getBigDecimal("porcentaje_vida_consumida_promedio"),
          r.getBigDecimal("anticipacion_promedio_prepago_dias"),
          l(r, "prepagos_31_90_dias"),
          l(r, "prepagos_3_6_meses"),
          l(r, "prepagos_6_12_meses"),
          l(r, "prepagos_1_2_anios"),
          l(r, "prepagos_2_5_anios"),
          l(r, "prepagos_mas_5_anios")
  );
 }

 private CancelacionPrepagoLineaDTO mapLinea(
         ResultSet r,
         int n
 ) throws SQLException {

  return new CancelacionPrepagoLineaDTO(
          i(r, "id_cierre_cartera"),
          d(r, "fecha_corte"),
          d(r, "fecha_corte_siguiente"),
          l(r, "id_linea_credito"),
          r.getString("codigo_linea_credito"),
          r.getString("nombre_linea_credito"),
          l(r, "creditos_expuestos"),
          l(r, "permanencias"),
          l(r, "ausencias_temporales"),
          l(r, "prepagos"),
          l(r, "cancelaciones_normales"),
          l(r, "cancelaciones_posteriores"),
          l(r, "salidas_definitivas"),
          r.getBigDecimal("tasa_permanencia"),
          r.getBigDecimal("tasa_salida_definitiva"),
          r.getBigDecimal("tasa_prepago_poblacion"),
          r.getBigDecimal("participacion_prepago_cancelaciones"),
          r.getBigDecimal("saldo_expuesto"),
          r.getBigDecimal("saldo_previo_prepagos"),
          r.getBigDecimal("saldo_previo_salidas_definitivas"),
          r.getBigDecimal("velocidad_amortizacion_promedio"),
          r.getBigDecimal("mediana_velocidad_amortizacion"),
          l(r, "amortizaciones_aceleradas"),
          l(r, "aumentos_saldo"),
          r.getBigDecimal("porcentaje_amortizacion_acelerada"),
          r.getBigDecimal("vida_contractual_promedio_dias"),
          r.getBigDecimal("vida_efectiva_promedio_dias"),
          r.getBigDecimal("porcentaje_vida_consumida_promedio"),
          r.getBigDecimal("anticipacion_promedio_prepago_dias")
  );
 }

 private CancelacionPrepagoCreditoDTO mapCredito(
         ResultSet r,
         int n
 ) throws SQLException {

  return new CancelacionPrepagoCreditoDTO(
          l(r, "id_cartera_credito"),
          i(r, "id_agencia"),
          l(r, "id_linea_credito"),
          r.getString("codigo_linea_credito"),
          r.getString("nombre_linea_credito"),
          r.getString("pagare_cartera"),
          l(r, "id_datos_personal"),
          r.getString("tipo_documento"),
          r.getString("documento"),
          r.getString("nombre_completo"),
          r.getString("codigo_clasificacion_credito"),
          r.getString("descripcion_clasificacion_credito"),
          r.getString("codigo_destino_economico"),
          r.getString("descripcion_destino_economico"),
          r.getBigDecimal("valor_inicial_credito"),
          r.getBigDecimal("valor_desembolsado"),
          d(r, "fecha_desembolso"),
          d(r, "fecha_final"),
          i(r, "plazo"),
          r.getString("codigo_forma_pago"),
          r.getString("codigo_tipo_cuota"),
          i(r, "amortizacion_capital"),
          r.getBigDecimal("valor_cuota"),
          d(r, "primer_corte_observado"),
          d(r, "ultimo_corte_evaluable"),
          l(r, "cantidad_transiciones"),
          l(r, "cantidad_permanencias"),
          l(r, "cantidad_ausencias_temporales"),
          l(r, "periodos_amortizacion_acelerada"),
          l(r, "periodos_aumento_saldo"),
          r.getBigDecimal("velocidad_amortizacion_promedio"),
          r.getBigDecimal("maxima_reduccion_porcentual"),
          d(r, "ultimo_corte_transicion"),
          d(r, "fecha_corte_siguiente"),
          r.getBigDecimal("saldo_corte_anterior"),
          r.getBigDecimal("saldo_corte_siguiente"),
          r.getBigDecimal("reduccion_saldo"),
          r.getBigDecimal("porcentaje_reduccion_saldo"),
          r.getBigDecimal("mediana_reduccion_linea"),
          r.getBigDecimal("percentil_75_reduccion_linea"),
          r.getString("comportamiento_amortizacion"),
          d(r, "ultima_fecha_aparicion"),
          i(r, "dias_anticipacion"),
          i(r, "dias_vida_contractual"),
          i(r, "dias_vida_efectiva"),
          r.getBigDecimal("porcentaje_vida_consumida"),
          r.getString("clasificacion_salida"),
          r.getString("rango_anticipacion"),
          i(r, "dias_mora"),
          r.getString("edad_contable_resultado"),
          r.getBigDecimal("deterioro_capital"),
          r.getBigDecimal("deterioro_intereses"),
          r.getBigDecimal("deterioro_otros"),
          r.getBigDecimal("deterioro_total")
  );
 }

 private CancelacionPrepagoDetalleDTO mapDetalle(
         ResultSet r,
         int n
 ) throws SQLException {

  return new CancelacionPrepagoDetalleDTO(
          i(r, "id_cierre_cartera"),
          d(r, "fecha_corte"),
          d(r, "fecha_corte_siguiente"),
          i(r, "id_cierre_cartera_credito"),
          i(r, "id_cierre_cartera_siguiente"),
          i(r, "id_cierre_cartera_credito_siguiente"),
          l(r, "id_cartera_credito"),
          i(r, "id_agencia"),
          l(r, "id_linea_credito"),
          r.getString("codigo_linea_credito"),
          r.getString("nombre_linea_credito"),
          r.getString("pagare_cartera"),
          l(r, "id_datos_personal"),
          r.getString("tipo_documento"),
          r.getString("documento"),
          r.getString("nombre_completo"),
          r.getString("codigo_clasificacion_credito"),
          r.getString("descripcion_clasificacion_credito"),
          r.getString("codigo_destino_economico"),
          r.getString("descripcion_destino_economico"),
          r.getBigDecimal("valor_inicial_credito"),
          r.getBigDecimal("valor_desembolsado"),
          d(r, "fecha_desembolso"),
          d(r, "fecha_final"),
          i(r, "plazo"),
          r.getString("codigo_forma_pago"),
          r.getString("codigo_tipo_cuota"),
          i(r, "amortizacion_capital"),
          r.getBigDecimal("valor_cuota"),
          i(r, "altura_cuota"),
          r.getBigDecimal("saldo_corte_anterior"),
          r.getBigDecimal("saldo_corte_siguiente"),
          r.getBigDecimal("reduccion_saldo"),
          r.getBigDecimal("porcentaje_reduccion_saldo"),
          r.getBigDecimal("mediana_reduccion_linea"),
          r.getBigDecimal("percentil_75_reduccion_linea"),
          r.getString("comportamiento_amortizacion"),
          b(r, "permanece"),
          b(r, "ausencia_temporal"),
          b(r, "salida_definitiva"),
          d(r, "ultima_fecha_aparicion"),
          i(r, "dias_anticipacion"),
          i(r, "dias_vida_contractual"),
          i(r, "dias_vida_efectiva"),
          r.getBigDecimal("porcentaje_vida_consumida"),
          r.getString("clasificacion_salida"),
          r.getString("rango_anticipacion"),
          i(r, "dias_mora"),
          r.getString("edad_contable_resultado"),
          r.getString("codigo_estado_cartera"),
          r.getString("descripcion_estado_cartera"),
          r.getBigDecimal("deterioro_capital"),
          r.getBigDecimal("deterioro_intereses"),
          r.getBigDecimal("deterioro_otros"),
          r.getBigDecimal("deterioro_total")
  );
 }

 // =========================================================
 // HELPERS JDBC
 // =========================================================

 private Long l(
         ResultSet r,
         String columna
 ) throws SQLException {
  long valor = r.getLong(columna);
  return r.wasNull() ? null : valor;
 }

 private Integer i(
         ResultSet r,
         String columna
 ) throws SQLException {
  int valor = r.getInt(columna);
  return r.wasNull() ? null : valor;
 }

 private Boolean b(
         ResultSet r,
         String columna
 ) throws SQLException {
  boolean valor = r.getBoolean(columna);
  return r.wasNull() ? null : valor;
 }

 private LocalDate d(
         ResultSet r,
         String columna
 ) throws SQLException {
  return r.getObject(columna, LocalDate.class);
 }
}