
package co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual;

import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualFilaDTO;
import co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto.BalanceDetalladoMensualRespuestaDTO;
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

/** Exporta el balance autorizado a Excel, conservando la hoja principal y agregando resúmenes. */
@Service
public class BalanceDetalladoMensualExcelService {
    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final short VERDE = IndexedColors.DARK_GREEN.getIndex();
    private static final short GRIS = IndexedColors.GREY_25_PERCENT.getIndex();

    /** Genera el libro con el balance original y dos hojas financieras independientes. */
    public byte[] generar(BalanceDetalladoMensualRespuestaDTO balance) {
        if (balance == null) throw new IllegalArgumentException("El balance es obligatorio.");
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Balance mensual");
            boolean comparativo = balance.compararAnoAnterior();
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
            fila = titulo(hoja, fila, "BALANCE DETALLADO MENSUAL" + (comparativo ? " - COMPARATIVO INTERANUAL" : ""), subtitulo);
            String mes = Month.of(balance.mes()).getDisplayName(TextStyle.FULL, COLOMBIA).toUpperCase(COLOMBIA);
            fila = titulo(hoja, fila, "Agencia: " + balance.codigoAgencia() + " - " + balance.nombreAgencia(), negrita);
            fila = titulo(hoja, fila, "Período: " + mes + " " + balance.ano()
                    + (comparativo ? " / " + mes + " " + balance.anoComparativo() : "")
                    + "     Nivel: " + balance.nivel()
                    + "     " + (balance.soloBalance() ? "Clases 1, 2 y 3" : "Todas las clases"), texto);
            fila = titulo(hoja, fila, "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), texto);
            fila++;
            int cabeceraFila = fila;
            String[] columnas = comparativo
                    ? new String[]{"Código", "Cuenta contable", "Saldo año anterior", "Saldo actual", "Variación $", "Variación %"}
                    : new String[]{"Código", "Cuenta contable", "Saldo anterior", "Débitos mes", "Créditos mes", "Saldo final"};
            Row encabezado = hoja.createRow(fila++);
            for (int i = 0; i < columnas.length; i++) celdaTexto(encabezado, i, columnas[i], cabecera);
            if (balance.filas() != null) {
                for (BalanceDetalladoMensualFilaDTO cuenta : balance.filas()) {
                    Row r = hoja.createRow(fila++);
                    boolean grupo = Boolean.FALSE.equals(cuenta.cuentaOperable());
                    CellStyle et = grupo ? agrupador : texto;
                    CellStyle nu = grupo ? agrupadorNumero : numero;
                    celdaTexto(r, 0, cuenta.codigoCuenta(), et);
                    celdaTexto(r, 1, cuenta.nombreCuenta(), et);
                    if (comparativo) {
                        celdaNumero(r, 2, cuenta.saldoMismoMesAnoAnterior(), nu);
                        celdaNumero(r, 3, cuenta.saldoFinal(), nu);
                        celdaNumero(r, 4, cuenta.variacionValor(), nu);
                        celdaNumero(r, 5, cuenta.variacionPorcentaje(), porcentaje);
                    } else {
                        celdaNumero(r, 2, cuenta.saldoAnterior(), nu);
                        celdaNumero(r, 3, cuenta.valorDebitoMes(), nu);
                        celdaNumero(r, 4, cuenta.valorCreditoMes(), nu);
                        celdaNumero(r, 5, cuenta.saldoFinal(), nu);
                    }
                }
            }
            if (balance.totales() != null) {
                BalanceDetalladoMensualRespuestaDTO.TotalesDTO t = balance.totales();
                Row r = hoja.createRow(fila++);
                celdaTexto(r, 0, "", negrita);
                celdaTexto(r, 1, "TOTALES", negrita);
                if (comparativo) {
                    celdaNumero(r, 2, t.saldoMismoMesAnoAnterior(), numeroNegrita);
                    celdaNumero(r, 3, t.saldoFinal(), numeroNegrita);
                    celdaNumero(r, 4, t.variacionValor(), numeroNegrita);
                    celdaNumero(r, 5, t.variacionPorcentaje(), porcentajeNegrita);
                } else {
                    celdaNumero(r, 2, t.saldoAnterior(), numeroNegrita);
                    celdaNumero(r, 3, t.valorDebitoMes(), numeroNegrita);
                    celdaNumero(r, 4, t.valorCreditoMes(), numeroNegrita);
                    celdaNumero(r, 5, t.saldoFinal(), numeroNegrita);
                }
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

            agregarHojaIndicadores(libro, balance, titulo, subtitulo, texto,
                    negrita, numero, numeroNegrita, cabecera);
            agregarHojaResumen(libro, balance, titulo, subtitulo, texto,
                    negrita, numero, numeroNegrita, cabecera);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible generar el Excel del balance detallado mensual.", e);
        }
    }

    /**
     * Crea la hoja de indicadores sin modificar la hoja Balance mensual.
     * Utiliza exclusivamente los importes calculados por el servicio.
     */
    private void agregarHojaIndicadores(
            Workbook libro,
            BalanceDetalladoMensualRespuestaDTO balance,
            CellStyle tituloEstilo,
            CellStyle subtitulo,
            CellStyle texto,
            CellStyle negrita,
            CellStyle numero,
            CellStyle numeroNegrita,
            CellStyle cabecera) {

        Sheet hoja = libro.createSheet("Indicadores financieros");
        int fila = encabezadoResumen(hoja, balance, "INDICADORES FINANCIEROS",
                tituloEstilo, subtitulo, texto, negrita);

        Row encabezado = hoja.createRow(fila++);
        celdaTexto(encabezado, 0, "Concepto", cabecera);
        celdaTexto(encabezado, 1, "Del mes", cabecera);
        celdaTexto(encabezado, 2, "Acumulado", cabecera);

        var indicadores = balance.indicadoresFinancieros();
        if (indicadores != null) {
            fila = filaIndicador(hoja, fila, "Activo", indicadores.activo(), texto, numero);
            fila = filaIndicador(hoja, fila, "Pasivo", indicadores.pasivo(), texto, numero);
            fila = filaIndicador(hoja, fila, "Patrimonio", indicadores.patrimonio(), texto, numero);
            fila = filaIndicador(hoja, fila, "Ingresos", indicadores.ingresos(), texto, numero);
            fila = filaIndicador(hoja, fila, "Costos y gastos", indicadores.costosGastos(), texto, numero);
            filaIndicador(hoja, fila, "Ganancias o pérdidas",
                    indicadores.gananciasPerdidas(), negrita, numeroNegrita);
        }

        configurarHojaResumen(hoja);
    }

    /**
     * Crea la hoja de control de débitos y créditos.
     * Presenta las diferencias reales sin modificar los valores originales.
     */
    private void agregarHojaResumen(
            Workbook libro,
            BalanceDetalladoMensualRespuestaDTO balance,
            CellStyle tituloEstilo,
            CellStyle subtitulo,
            CellStyle texto,
            CellStyle negrita,
            CellStyle numero,
            CellStyle numeroNegrita,
            CellStyle cabecera) {

        Sheet hoja = libro.createSheet("Resumen financiero");
        int fila = encabezadoResumen(hoja, balance, "RESUMEN FINANCIERO",
                tituloEstilo, subtitulo, texto, negrita);

        Row encabezado = hoja.createRow(fila++);
        celdaTexto(encabezado, 0, "Concepto", cabecera);
        celdaTexto(encabezado, 1, "Débitos", cabecera);
        celdaTexto(encabezado, 2, "Créditos", cabecera);

        var resumen = balance.resumenFinanciero();
        if (resumen != null) {
            fila = filaMovimiento(hoja, fila, "Acumulado anterior",
                    resumen.acumuladoAnterior(), texto, numero);
            fila = filaMovimiento(hoja, fila, "Movimientos del mes",
                    resumen.movimientosMes(), texto, numero);
            fila = filaMovimiento(hoja, fila, "Acumulado final",
                    resumen.acumuladoFinal(), negrita, numeroNegrita);

            fila++;
            Row diferenciaMes = hoja.createRow(fila++);
            celdaTexto(diferenciaMes, 0, "Diferencia del mes", negrita);
            celdaNumero(diferenciaMes, 1, resumen.diferenciaMes(), numeroNegrita);

            Row diferenciaAcumulada = hoja.createRow(fila);
            celdaTexto(diferenciaAcumulada, 0, "Diferencia acumulada", negrita);
            celdaNumero(diferenciaAcumulada, 1,
                    resumen.diferenciaAcumulada(), numeroNegrita);
        }

        configurarHojaResumen(hoja);
    }

    /** Presenta la identificación del informe en cada hoja financiera. */
    private int encabezadoResumen(
            Sheet hoja,
            BalanceDetalladoMensualRespuestaDTO balance,
            String nombreInforme,
            CellStyle tituloEstilo,
            CellStyle subtitulo,
            CellStyle texto,
            CellStyle negrita) {

        int fila = 0;
        fila = tituloResumen(hoja, fila,
                "COOPERATIVA DE AHORRO Y CREDITO DEL VALLE DE SAN JOSE",
                tituloEstilo);
        fila = tituloResumen(hoja, fila, nombreInforme, subtitulo);
        fila = tituloResumen(hoja, fila,
                "Agencia: " + balance.codigoAgencia() + " - " + balance.nombreAgencia(),
                negrita);

        String mes = Month.of(balance.mes())
                .getDisplayName(TextStyle.FULL, COLOMBIA)
                .toUpperCase(COLOMBIA);
        fila = tituloResumen(hoja, fila,
                "Período: " + mes + " " + balance.ano(), texto);
        fila = tituloResumen(hoja, fila,
                "Generado: " + LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                texto);
        return fila + 1;
    }

    /** Escribe una fila de indicador con valores numéricos editables. */
    private int filaIndicador(
            Sheet hoja,
            int indice,
            String concepto,
            BalanceDetalladoMensualRespuestaDTO.IndicadorDTO indicador,
            CellStyle texto,
            CellStyle numero) {

        Row fila = hoja.createRow(indice);
        celdaTexto(fila, 0, concepto, texto);
        if (indicador != null) {
            celdaNumero(fila, 1, indicador.valorMes(), numero);
            celdaNumero(fila, 2, indicador.valorAcumulado(), numero);
        }
        return indice + 1;
    }

    /** Escribe una fila del control financiero con débitos y créditos separados. */
    private int filaMovimiento(
            Sheet hoja,
            int indice,
            String concepto,
            BalanceDetalladoMensualRespuestaDTO.MovimientoResumenDTO movimiento,
            CellStyle texto,
            CellStyle numero) {

        Row fila = hoja.createRow(indice);
        celdaTexto(fila, 0, concepto, texto);
        if (movimiento != null) {
            celdaNumero(fila, 1, movimiento.debitos(), numero);
            celdaNumero(fila, 2, movimiento.creditos(), numero);
        }
        return indice + 1;
    }

    /** Configura exclusivamente las nuevas hojas para consulta e impresión. */
    private void configurarHojaResumen(Sheet hoja) {
        hoja.setColumnWidth(0, 38 * 256);
        hoja.setColumnWidth(1, 25 * 256);
        hoja.setColumnWidth(2, 25 * 256);
        hoja.createFreezePane(0, 7);
        hoja.setFitToPage(true);
        hoja.getPrintSetup().setLandscape(true);
        hoja.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        hoja.getPrintSetup().setFitWidth((short) 1);
        hoja.getPrintSetup().setFitHeight((short) 1);
    }

    /** Inserta un encabezado fusionado en las tres columnas de las nuevas hojas. */
    private int tituloResumen(Sheet hoja, int indice, String valor, CellStyle estilo) {
        Row fila = hoja.createRow(indice);
        celdaTexto(fila, 0, valor, estilo);
        hoja.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                indice, indice, 0, 2));
        return indice + 1;
    }

    /** Crea estilos reutilizables; conserva importes como números y no como cadenas. */
    private CellStyle estilo(Workbook libro, boolean negrita, short tamano,
                             Short fondo, boolean numerico) {
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

    /** Inserta un renglón institucional de ancho completo en la hoja original. */
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

    /** Escribe valores numéricos editables y deja en blanco los inexistentes. */
    private void celdaNumero(Row fila, int columna, BigDecimal valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        if (valor != null) celda.setCellValue(valor.doubleValue());
        celda.setCellStyle(estilo);
    }

    /** Agrega un firmante parametrizado sin inventar nombres ni tarjetas. */
    private int firmante(Sheet hoja, int fila,
                         BalanceDetalladoMensualRespuestaDTO.FirmanteDTO firmante,
                         CellStyle negrita, CellStyle texto) {
        if (firmante == null) return fila;
        fila = titulo(hoja, fila, firmante.nombre(), negrita);
        fila = titulo(hoja, fila, firmante.titulo()
                + (firmante.tarjetaProfesional() == null || firmante.tarjetaProfesional().isBlank()
                ? "" : " - T.P. " + firmante.tarjetaProfesional()), texto);
        return fila + 1;
    }
}
