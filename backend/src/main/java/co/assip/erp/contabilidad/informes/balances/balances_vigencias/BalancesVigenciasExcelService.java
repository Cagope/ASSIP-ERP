package co.assip.erp.contabilidad.informes.balances.balances_vigencias;

import co.assip.erp.contabilidad.informes.balances.balances_vigencias.dto.BalancesVigenciasFilaDTO;
import co.assip.erp.contabilidad.informes.balances.balances_vigencias.dto.BalancesVigenciasRespuestaDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/** Exporta el resumen por clases y el detalle anual sin alterar los saldos calculados. */
@Service
public class BalancesVigenciasExcelService {
    private static final String[] COLUMNAS = {
            "Código", "Cuenta contable", "Saldo anterior", "Débitos", "Créditos", "Saldo final"
    };

    public byte[] generar(BalancesVigenciasRespuestaDTO informe) {
        try (XSSFWorkbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle encabezado = estilo(libro, true, false);
            CellStyle texto = estilo(libro, false, false);
            CellStyle numero = estilo(libro, false, true);
            CellStyle subtotal = estilo(libro, true, true);
            CellStyle rotuloTotal = estilo(libro, true, false);

            // Las clases ya contienen los saldos acumulados; no se suman sus subcuentas.
            List<BalancesVigenciasFilaDTO> clases = informe.filas().stream()
                    .filter(f -> f.nivelCuenta() == 1)
                    .toList();
            crearHoja(libro, "Resumen por clases", informe, clases, encabezado,
                    texto, numero, subtotal, rotuloTotal);
            crearHoja(libro, "Balance detallado", informe, informe.filas(), encabezado,
                    texto, numero, subtotal, rotuloTotal);
            libro.setActiveSheet(0);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible exportar Balances por Vigencias.", e);
        }
    }

    /** Construye una hoja imprimible con filtros sobre datos, no sobre títulos ni totales. */
    private void crearHoja(XSSFWorkbook libro, String nombre,
            BalancesVigenciasRespuestaDTO informe, List<BalancesVigenciasFilaDTO> filas,
            CellStyle encabezado, CellStyle texto, CellStyle numero,
            CellStyle subtotal, CellStyle rotuloTotal) {
        Sheet hoja = libro.createSheet(nombre);
        int filaActual = 0;
        Row titulo = hoja.createRow(filaActual++);
        titulo.createCell(0).setCellValue("BALANCES POR VIGENCIAS - " + informe.ano());
        titulo.getCell(0).setCellStyle(encabezado);
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        Row agencia = hoja.createRow(filaActual++);
        agencia.createCell(0).setCellValue(informe.codigoAgencia() + " - " + informe.nombreAgencia());
        hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));
        Row periodo = hoja.createRow(filaActual++);
        periodo.createCell(0).setCellValue("Del 01/01/" + informe.ano()
                + " al 31/12/" + informe.ano() + " | " + nombre);
        hoja.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));
        filaActual++;
        final int encabezadoFila = filaActual;
        Row cabecera = hoja.createRow(filaActual++);
        for (int i = 0; i < COLUMNAS.length; i++) {
            Cell celda = cabecera.createCell(i);
            celda.setCellValue(COLUMNAS[i]);
            celda.setCellStyle(encabezado);
        }
        for (BalancesVigenciasFilaDTO fila : filas) {
            Row r = hoja.createRow(filaActual++);
            Cell codigo = r.createCell(0);
            codigo.setCellValue(fila.codigoCuenta());
            codigo.setCellStyle(fila.cuentaOperable() ? texto : rotuloTotal);
            Cell cuenta = r.createCell(1);
            cuenta.setCellValue("  ".repeat(Math.max(0, fila.nivelCuenta() - 1))
                    + fila.nombreCuenta());
            cuenta.setCellStyle(fila.cuentaOperable() ? texto : rotuloTotal);
            CellStyle valores = fila.cuentaOperable() ? numero : subtotal;
            importe(r, 2, fila.saldoAnterior(), valores);
            importe(r, 3, fila.valorDebitoMes(), valores);
            importe(r, 4, fila.valorCreditoMes(), valores);
            importe(r, 5, fila.saldoFinal(), valores);
        }
        int ultimaFilaDatos = filaActual - 1;
        Row totales = hoja.createRow(filaActual++);
        totales.createCell(1).setCellValue("TOTALES GENERALES");
        totales.getCell(1).setCellStyle(rotuloTotal);
        importe(totales, 2, informe.totales().saldoAnterior(), subtotal);
        importe(totales, 3, informe.totales().valorDebitoMes(), subtotal);
        importe(totales, 4, informe.totales().valorCreditoMes(), subtotal);
        importe(totales, 5, informe.totales().saldoFinal(), subtotal);
        Row diferencia = hoja.createRow(filaActual);
        diferencia.createCell(1).setCellValue("DIFERENCIA DÉBITOS - CRÉDITOS");
        diferencia.getCell(1).setCellStyle(rotuloTotal);
        importe(diferencia, 3, informe.totales().valorDebitoMes()
                .subtract(informe.totales().valorCreditoMes()), subtotal);

        hoja.createFreezePane(2, encabezadoFila + 1);
        if (ultimaFilaDatos > encabezadoFila) {
            hoja.setAutoFilter(new CellRangeAddress(encabezadoFila, ultimaFilaDatos, 0, 5));
        }
        hoja.setColumnWidth(0, 18 * 256);
        hoja.setColumnWidth(1, 56 * 256);
        for (int i = 2; i < 6; i++) hoja.setColumnWidth(i, 24 * 256);
        hoja.setDisplayGridlines(false);
        hoja.setFitToPage(true);
        PrintSetup impresion = hoja.getPrintSetup();
        impresion.setLandscape(true);
        impresion.setFitWidth((short) 1);
        impresion.setFitHeight((short) 0);
        hoja.setRepeatingRows(new CellRangeAddress(encabezadoFila, encabezadoFila, 0, 5));
    }

    /** Define estilos numéricos reales para permitir sumas y filtros en Excel. */
    private CellStyle estilo(Workbook libro, boolean negrita, boolean numerico) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(negrita);
        estilo.setFont(fuente);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        if (numerico) {
            estilo.setAlignment(HorizontalAlignment.RIGHT);
            estilo.setDataFormat(libro.createDataFormat().getFormat("#,##0.00;[Red](#,##0.00)"));
        }
        return estilo;
    }

    /** Mantiene los importes como celdas numéricas y no como texto formateado. */
    private void importe(Row fila, int columna, BigDecimal valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor == null ? 0 : valor.doubleValue());
        celda.setCellStyle(estilo);
    }
}
