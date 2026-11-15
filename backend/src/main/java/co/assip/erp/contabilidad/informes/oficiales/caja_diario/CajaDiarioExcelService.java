package co.assip.erp.contabilidad.informes.oficiales.caja_diario;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Exporta dos hojas contables con celdas numéricas y formato colombiano. */
@Service
public class CajaDiarioExcelService {
    private void fila(Sheet hoja, int n, String fecha, String codigo, String nombre,
                      BigDecimal debito, BigDecimal credito, CellStyle numero) {
        Row r = hoja.createRow(n);
        r.createCell(0).setCellValue(fecha); r.createCell(1).setCellValue(codigo);
        r.createCell(2).setCellValue(nombre);
        Cell d = r.createCell(3); d.setCellValue(debito.doubleValue()); d.setCellStyle(numero);
        Cell c = r.createCell(4); c.setCellValue(credito.doubleValue()); c.setCellStyle(numero);
    }
    /** Genera detalle diario y resumen desde una sola consulta autorizada. */
    public byte[] generar(CajaDiarioService.Resultado r) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle numero = wb.createCellStyle();
            numero.setDataFormat(wb.createDataFormat().getFormat("#,##0.00;[Red](#,##0.00)"));
            Sheet diario = wb.createSheet("Libro Diario"), resumen = wb.createSheet("Resumen");
            for (Sheet h : new Sheet[]{diario, resumen}) {
                Row info = h.createRow(0);
                info.createCell(0).setCellValue((r.acumuladoAnual() ? "ANUAL" : "MENSUAL")
                    + " | Agencia: " + r.codigoAgencia() + " - " + r.nombreAgencia()
                    + " | Período: " + r.desde() + " a " + r.hasta() + " | Nivel: " + r.nivel());
                Row cab = h.createRow(1); String[] tit = {"Fecha", "Código", "Cuenta", "Débito", "Crédito"};
                for (int i = 0; i < tit.length; i++) cab.createCell(i).setCellValue(tit[i]);
                h.setColumnWidth(0, 4000); h.setColumnWidth(1, 3600); h.setColumnWidth(2, 14500);
                h.setColumnWidth(3, 5500); h.setColumnWidth(4, 5500); h.createFreezePane(0, 2);
            }
            int i = 2;
            LocalDate fechaAnterior = null;
            java.util.Map<LocalDate, CajaDiarioService.TotalDia> totalesPorDia =
                new java.util.LinkedHashMap<>();
            for (var total : r.totalesDias()) totalesPorDia.put(total.fecha(), total);

            CellStyle estiloFecha = wb.createCellStyle();
            estiloFecha.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            estiloFecha.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font fuenteFecha = wb.createFont();
            fuenteFecha.setBold(true);
            estiloFecha.setFont(fuenteFecha);

            CellStyle estiloSubtotal = wb.createCellStyle();
            estiloSubtotal.cloneStyleFrom(numero);
            Font fuenteSubtotal = wb.createFont();
            fuenteSubtotal.setBold(true);
            estiloSubtotal.setFont(fuenteSubtotal);

            for (var x : r.diario()) {
                if (!x.fecha().equals(fechaAnterior)) {
                    if (fechaAnterior != null) {
                        var total = totalesPorDia.get(fechaAnterior);
                        fila(diario, i++, "", "", "TOTAL DEL DÍA " + fechaAnterior,
                             total.debito(), total.credito(), estiloSubtotal);
                    }
                    Row separador = diario.createRow(i++);
                    separador.createCell(0).setCellValue("FECHA: " + x.fecha());
                    for (int col = 0; col < 5; col++) {
                        Cell celda = col == 0 ? separador.getCell(0) : separador.createCell(col);
                        celda.setCellStyle(estiloFecha);
                    }
                    fechaAnterior = x.fecha();
                }
                fila(diario, i++, x.fecha().toString(), x.codigo(), x.nombre(),
                     x.debito(), x.credito(), numero);
            }
            if (fechaAnterior != null) {
                var total = totalesPorDia.get(fechaAnterior);
                fila(diario, i++, "", "", "TOTAL DEL DÍA " + fechaAnterior,
                     total.debito(), total.credito(), estiloSubtotal);
            }
            fila(diario, i, "", "", "TOTAL INFORME",
                 r.totalDebito(), r.totalCredito(), estiloSubtotal);
            i = 2;
            for (var x : r.resumen()) fila(resumen, i++, "", x.codigo(), x.nombre(), x.debito(), x.credito(), numero);
            fila(resumen, i, "", "", "TOTAL INFORME", r.totalDebito(), r.totalCredito(), numero);
            wb.write(out); return out.toByteArray();
        } catch (IOException e) { throw new IllegalStateException("No se pudo generar el Excel de Caja Diario.", e); }
    }
}
