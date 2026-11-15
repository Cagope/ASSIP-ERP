package co.assip.erp.contabilidad.informes.balances.estado_cuentas;

import co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto.EstadoCuentasRespuestaDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/** Exporta importes numéricos y conserva la jerarquía contable. */
@Service
public class EstadoCuentasExcelService {
    public byte[] generar(EstadoCuentasRespuestaDTO informe) {
        try (XSSFWorkbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Estado de cuentas");
            CellStyle encabezado = estilo(libro, true, false);
            CellStyle texto = estilo(libro, false, false);
            CellStyle numero = estilo(libro, false, true);
            CellStyle total = estilo(libro, true, true);
            int n = 0;
            Row titulo = hoja.createRow(n++);
            titulo.createCell(0).setCellValue("ESTADO DE CUENTAS");
            titulo.getCell(0).setCellStyle(encabezado);
            Row agencia = hoja.createRow(n++);
            agencia.createCell(0).setCellValue(informe.codigoAgencia() + " - " + informe.nombreAgencia());
            Row corte = hoja.createRow(n++);
            corte.createCell(0).setCellValue("Corte: " + informe.fechaCorte()
                    + " | Desde: " + informe.fechaCorte().withDayOfMonth(1)
                    + " | Cuenta: " + (informe.codigoCuentaPrincipal().isBlank()
                    ? "Todas" : informe.codigoCuentaPrincipal()));
            n++;
            Row columnas = hoja.createRow(n++);
            String[] nombres = {"Código", "Cuenta contable", "Saldo anterior",
                    "Débitos del período", "Créditos del período", "Saldo final"};
            for (int i = 0; i < nombres.length; i++) {
                Cell celda = columnas.createCell(i);
                celda.setCellValue(nombres[i]);
                celda.setCellStyle(encabezado);
            }
            for (var fila : informe.filas()) {
                Row r = hoja.createRow(n++);
                r.createCell(0).setCellValue(fila.codigoCuenta());
                r.createCell(1).setCellValue("  ".repeat(Math.max(0, fila.nivelCuenta() - 1))
                        + fila.nombreCuenta());
                for (int i = 0; i < 2; i++) r.getCell(i).setCellStyle(texto);
                importe(r, 2, fila.saldoAnterior(), numero);
                importe(r, 3, fila.valorDebitoMes(), numero);
                importe(r, 4, fila.valorCreditoMes(), numero);
                importe(r, 5, fila.saldoFinal(), numero);
            }
            Row t = hoja.createRow(n++);
            t.createCell(1).setCellValue("TOTALES");
            t.getCell(1).setCellStyle(encabezado);
            importe(t, 2, informe.totales().saldoAnterior(), total);
            importe(t, 3, informe.totales().valorDebitoMes(), total);
            importe(t, 4, informe.totales().valorCreditoMes(), total);
            importe(t, 5, informe.totales().saldoFinal(), total);
            hoja.createFreezePane(2, 5);
            hoja.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(4, n - 2, 0, 5));
            hoja.setColumnWidth(0, 18 * 256);
            hoja.setColumnWidth(1, 48 * 256);
            for (int i = 2; i < 6; i++) hoja.setColumnWidth(i, 23 * 256);
            if (informe.incluirFirmas() && informe.firmas() != null) {
                for (var firma : new EstadoCuentasRespuestaDTO.FirmanteDTO[]{
                        informe.firmas().firmante1(), informe.firmas().firmante2(),
                        informe.firmas().firmante3()}) {
                    if (firma == null) continue;
                    Row r = hoja.createRow(++n);
                    r.createCell(1).setCellValue(firma.nombre() + " - " + firma.titulo()
                            + (firma.tarjetaProfesional() == null ? ""
                            : " - T.P. " + firma.tarjetaProfesional()));
                }
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible exportar el estado de cuentas.", e);
        }
    }

    private CellStyle estilo(Workbook libro, boolean negrita, boolean numerico) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(negrita);
        estilo.setFont(fuente);
        if (numerico) {
            estilo.setAlignment(HorizontalAlignment.RIGHT);
            estilo.setDataFormat(libro.createDataFormat().getFormat("#,##0.00;[Red](#,##0.00)"));
        }
        return estilo;
    }

    private void importe(Row fila, int columna, BigDecimal valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor == null ? 0 : valor.doubleValue());
        celda.setCellStyle(estilo);
    }
}
