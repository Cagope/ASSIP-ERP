package co.assip.erp.contabilidad.informes.oficiales.mayor_balance;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/** Libro Excel con columnas numéricas y los tres grupos de saldos del Mayor. */
@Service
public class MayorBalanceExcelService {
    private static void fila(Sheet hoja, int numeroFila, String codigo, String nombre,
                             BigDecimal[] valores, CellStyle numerico, CellStyle texto) {
        Row r = hoja.createRow(numeroFila);
        Cell c0 = r.createCell(0); c0.setCellValue(codigo); c0.setCellStyle(texto);
        Cell c1 = r.createCell(1); c1.setCellValue(nombre); c1.setCellStyle(texto);
        for (int i=0; i<6; i++) {
            Cell c = r.createCell(i+2); c.setCellValue(valores[i].doubleValue()); c.setCellStyle(numerico);
        }
    }
    public byte[] generar(MayorBalanceService.Resultado resultado) {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet("Mayor y Balance");
            CellStyle encabezado = wb.createCellStyle();
            Font negrita = wb.createFont(); negrita.setBold(true); encabezado.setFont(negrita);
            encabezado.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle numero = wb.createCellStyle();
            numero.setDataFormat(wb.createDataFormat().getFormat("#,##0.00;[Red](#,##0.00)"));
            numero.setAlignment(HorizontalAlignment.RIGHT);
            CellStyle total = wb.createCellStyle(); total.cloneStyleFrom(numero); total.setFont(negrita);
            Row titulo = hoja.createRow(0);
            titulo.createCell(0).setCellValue("LIBRO MAYOR Y BALANCE - " + (resultado.acumuladoAnual() ? "ANUAL" : "MENSUAL"));
            hoja.createRow(1).createCell(0).setCellValue(resultado.codigoAgencia() + " - " + resultado.nombreAgencia());
            hoja.createRow(2).createCell(0).setCellValue("Desde: " + resultado.desde() + "  Hasta: " + resultado.hasta() + "  Nivel: 3");
            Row grupos = hoja.createRow(4);
            String[] nombres = {"Código","Cuenta","SALDO ANTERIOR","","MOVIMIENTO DEL PERÍODO","","NUEVO SALDO",""};
            for (int i=0;i<nombres.length;i++) {Cell c=grupos.createCell(i);c.setCellValue(nombres[i]);c.setCellStyle(encabezado);}
            Row cab = hoja.createRow(5);
            String[] sub = {"Código","Cuenta","Débito","Crédito","Débito","Crédito","Débito","Crédito"};
            for (int i=0;i<sub.length;i++) {Cell c=cab.createCell(i);c.setCellValue(sub[i]);c.setCellStyle(encabezado);}
            int i=6;
            for (var x: resultado.filas()) fila(hoja,i++,x.codigo(),x.nombre(),new BigDecimal[]{
                x.debitoAnterior(),x.creditoAnterior(),x.debitoMovimiento(),x.creditoMovimiento(),
                x.debitoSaldo(),x.creditoSaldo()},numero,wb.createCellStyle());
            var t=resultado.totales();
            fila(hoja,i,"","TOTAL",new BigDecimal[]{t.debitoAnterior(),t.creditoAnterior(),
                t.debitoMovimiento(),t.creditoMovimiento(),t.debitoSaldo(),t.creditoSaldo()},total,encabezado);
            hoja.setColumnWidth(0,4200);hoja.setColumnWidth(1,14500);
            for(int c=2;c<8;c++) hoja.setColumnWidth(c,5200);
            hoja.createFreezePane(2,6); hoja.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(5,i,0,7));
            hoja.getPrintSetup().setLandscape(true);hoja.getPrintSetup().setPaperSize(PrintSetup.LETTER_PAPERSIZE);
            hoja.setFitToPage(true);hoja.getPrintSetup().setFitWidth((short)1);
            wb.write(out);return out.toByteArray();
        } catch(IOException e) {throw new IllegalStateException("No se pudo generar Excel de Mayor y Balance.",e);}
    }
}
