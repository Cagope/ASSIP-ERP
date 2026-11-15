package co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual;

import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualFilaDTO;
import co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto.BalanceComparadoMensualRespuestaDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Exporta el balance autorizado a un libro Excel con valores numéricos y formato institucional. */
@Service
public class BalanceComparadoMensualExcelService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final short VERDE = IndexedColors.DARK_GREEN.getIndex();
    private static final short GRIS = IndexedColors.GREY_25_PERCENT.getIndex();

    /** Genera el Excel desde la respuesta de consulta, sin recalcular saldos ni omitir controles de acceso. */
    public byte[] generar(BalanceComparadoMensualRespuestaDTO balance) {
        if (balance == null) throw new IllegalArgumentException("El balance es obligatorio.");
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Balance comparado");
            CellStyle titulo = estilo(libro, true, (short) 14, null, false);
            CellStyle subtitulo = estilo(libro, true, (short) 11, null, false);
            CellStyle texto = estilo(libro, false, (short) 10, null, false);
            CellStyle negrita = estilo(libro, true, (short) 10, null, false);
            CellStyle numero = estilo(libro, false, (short) 10, null, true);
            CellStyle numeroNegrita = estilo(libro, true, (short) 10, null, true);
            CellStyle cabecera = estilo(libro, true, (short) 10, VERDE, false);
            CellStyle agrupador = estilo(libro, true, (short) 10, GRIS, false);
            CellStyle agrupadorNumero = estilo(libro, true, (short) 10, GRIS, true);
            CellStyle porcentaje = estilo(libro, false, (short) 10, null, false);
            porcentaje.setDataFormat(libro.createDataFormat().getFormat("#,##0.00\"%\";[Red](#,##0.00\"%\")"));
            porcentaje.setAlignment(HorizontalAlignment.RIGHT);
            CellStyle porcentajeNegrita = estilo(libro, true, (short) 10, null, false);
            porcentajeNegrita.setDataFormat(porcentaje.getDataFormat());
            porcentajeNegrita.setAlignment(HorizontalAlignment.RIGHT);
            int fila = 0;
            fila = titulo(hoja, fila, "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE", titulo);
            fila = titulo(hoja, fila, "BALANCE COMPARADO MENSUAL", subtitulo);
            String periodo1 = periodo(balance.anoPeriodo1(), balance.mesPeriodo1());
            String periodo2 = periodo(balance.anoPeriodo2(), balance.mesPeriodo2());
            fila = titulo(hoja, fila, "Agencia: " + balance.codigoAgencia() + " - " + balance.nombreAgencia(), negrita);
            fila = titulo(hoja, fila, "Período 1: " + periodo1 + "  |  Período 2: " + periodo2
                    + "  |  Nivel: " + balance.nivel()
                    + "  |  " + (balance.soloBalance() ? "Clases 1, 2 y 3" : "Todas las clases"), texto);
            fila = titulo(hoja, fila, "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), texto);
            fila++;
            int cabeceraFila = fila;
            String[] columnas = {"Código", "Cuenta contable", periodo1, periodo2, "Variación $", "Variación %"};
            Row encabezado = hoja.createRow(fila++);
            for (int i = 0; i < columnas.length; i++) celdaTexto(encabezado, i, columnas[i], cabecera);
            if (balance.filas() != null) {
                for (BalanceComparadoMensualFilaDTO cuenta : balance.filas()) {
                    Row r = hoja.createRow(fila++);
                    boolean grupo = Boolean.FALSE.equals(cuenta.cuentaOperable());
                    CellStyle et = grupo ? agrupador : texto;
                    CellStyle nu = grupo ? agrupadorNumero : numero;
                    celdaTexto(r, 0, cuenta.codigoCuenta(), et);
                    celdaTexto(r, 1, cuenta.nombreCuenta(), et);
                    celdaNumero(r, 2, cuenta.saldoPeriodo1(), nu);
                    celdaNumero(r, 3, cuenta.saldoPeriodo2(), nu);
                    celdaNumero(r, 4, cuenta.variacionValor(), nu);
                    celdaNumero(r, 5, cuenta.variacionPorcentaje(), porcentaje);
                }
            }
            if (balance.totales() != null) {
                BalanceComparadoMensualRespuestaDTO.TotalesDTO t = balance.totales();
                Row r = hoja.createRow(fila++);
                celdaTexto(r, 0, "", negrita);
                celdaTexto(r, 1, "TOTALES", negrita);
                celdaNumero(r, 2, t.saldoPeriodo1(), numeroNegrita);
                celdaNumero(r, 3, t.saldoPeriodo2(), numeroNegrita);
                celdaNumero(r, 4, t.variacionValor(), numeroNegrita);
                celdaNumero(r, 5, t.variacionPorcentaje(), porcentajeNegrita);
            }
            int ultimaFilaDatos = fila - 1;
            if (balance.incluirFirmas() && balance.firmas() != null) {
                fila += 3;
                fila = firmante(hoja, fila, balance.firmas().firmante1(), negrita, texto);
                fila = firmante(hoja, fila, balance.firmas().firmante2(), negrita, texto);
                firmante(hoja, fila, balance.firmas().firmante3(), negrita, texto);
            }
            hoja.setColumnWidth(0, 17 * 256);
            hoja.setColumnWidth(1, 58 * 256);
            for (int i = 2; i < 6; i++) hoja.setColumnWidth(i, 23 * 256);
            hoja.createFreezePane(2, cabeceraFila + 1);
            hoja.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(cabeceraFila, Math.max(cabeceraFila, ultimaFilaDatos), 0, 5));
            hoja.setFitToPage(true);
            hoja.getPrintSetup().setLandscape(true);
            hoja.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
            hoja.setRepeatingRows(new org.apache.poi.ss.util.CellRangeAddress(cabeceraFila, cabeceraFila, 0, 5));
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible generar el Excel del balance comparado mensual.", e);
        }
    }

    /** Formatea cada período sin imponer que los meses sean consecutivos. */
    private String periodo(int ano, int mes) {
        return Month.of(mes).getDisplayName(TextStyle.FULL, COLOMBIA).toUpperCase(COLOMBIA) + " " + ano;
    }

    /** Crea estilos reutilizables; conserva importes como números y no como cadenas formateadas. */
    private CellStyle estilo(Workbook libro, boolean negrita, short tamano, Short fondo, boolean numerico) {
        CellStyle estilo = libro.createCellStyle();
        Font fuente = libro.createFont();
        fuente.setBold(negrita);
        fuente.setFontHeightInPoints(tamano);
        if (fondo != null && fondo == VERDE) fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        if (fondo != null) {
            estilo.setFillForegroundColor(fondo);
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (numerico) {
            estilo.setDataFormat(libro.createDataFormat().getFormat("#,##0.00;[Red](#,##0.00)"));
            estilo.setAlignment(HorizontalAlignment.RIGHT);
        }
        return estilo;
    }

    /** Inserta un renglón institucional de ancho completo. */
    private int titulo(Sheet hoja, int indice, String valor, CellStyle estilo) {
        Row r = hoja.createRow(indice);
        celdaTexto(r, 0, valor, estilo);
        hoja.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(indice, indice, 0, 5));
        return indice + 1;
    }

    /** Escribe textos sin alterar códigos contables con ceros iniciales. */
    private void celdaTexto(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor == null ? "" : valor);
        celda.setCellStyle(estilo);
    }

    /** Escribe valores numéricos editables en Excel y deja en blanco los valores inexistentes. */
    private void celdaNumero(Row fila, int columna, BigDecimal valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        if (valor != null) celda.setCellValue(valor.doubleValue());
        celda.setCellStyle(estilo);
    }

    /** Agrega un firmante parametrizado, cuando existe, sin inventar nombres o tarjetas. */
    private int firmante(Sheet hoja, int fila, BalanceComparadoMensualRespuestaDTO.FirmanteDTO firmante,
                         CellStyle negrita, CellStyle texto) {
        if (firmante == null) return fila;
        fila = titulo(hoja, fila, firmante.nombre(), negrita);
        fila = titulo(hoja, fila, firmante.titulo()
                + (firmante.tarjetaProfesional() == null || firmante.tarjetaProfesional().isBlank()
                ? "" : " - T.P. " + firmante.tarjetaProfesional()), texto);
        return fila + 1;
    }
}
