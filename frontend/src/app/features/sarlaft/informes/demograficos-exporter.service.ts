import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class DemograficosExporterService {

  exportarListado(fecha: string, registros: any[], stats: any) {

    /* ==========================================================
       1) HOJA DETALLE — TODOS LOS CAMPOS DEL BACKEND
       ========================================================== */
    const detalleSheet = XLSX.utils.json_to_sheet(registros);


    /* ==========================================================
       2) HOJA ESTADÍSTICAS
       - Convertimos cada grupo en una tabla clara
       ========================================================== */

    const estadisticasRows: any[] = [];

    // 🔹 Resumen general
    estadisticasRows.push(
      { Categoria: 'TOTAL ASOCIADOS', Cantidad: stats.total, Valor: '' },
      { Categoria: 'TOTAL APORTES', Cantidad: '', Valor: stats.resumen.totalAportes },
      { Categoria: 'PROMEDIO APORTES', Cantidad: '', Valor: stats.resumen.promedioAportes },
      {},
      { Categoria: '--- DISTRIBUCIÓN POR GÉNERO ---' }
    );

    // 🔹 Género (cantidades y aportes)
    Object.keys(stats.genero).forEach(key => {
      estadisticasRows.push({
        Categoria: key,
        Cantidad: stats.genero[key],
        Valor: stats.aportexGenero[key] || 0
      });
    });

    estadisticasRows.push({}, { Categoria: '--- RANGOS DE EDAD ---' });

    // 🔹 Rangos de edad
    Object.keys(stats.rangosEdad).forEach(key => {
      estadisticasRows.push({
        Categoria: key,
        Cantidad: stats.rangosEdad[key],
        Valor: ''
      });
    });

    estadisticasRows.push({}, { Categoria: '--- TIPO VIVIENDA ---' });

    // 🔹 Tipo vivienda
    Object.keys(stats.tipoVivienda).forEach(key => {
      estadisticasRows.push({
        Categoria: key,
        Cantidad: stats.tipoVivienda[key],
        Valor: ''
      });
    });

    estadisticasRows.push({}, { Categoria: '--- ZONAS ---' });

    // 🔹 Zonas
    Object.keys(stats.zonas).forEach(key => {
      estadisticasRows.push({
        Categoria: key,
        Cantidad: stats.zonas[key],
        Valor: ''
      });
    });

    estadisticasRows.push({}, { Categoria: '--- SUBZONAS ---' });

    // 🔹 Subzonas
    Object.keys(stats.subzonas).forEach(key => {
      estadisticasRows.push({
        Categoria: key,
        Cantidad: stats.subzonas[key],
        Valor: ''
      });
    });

    estadisticasRows.push({}, { Categoria: '--- ESCOLARIDAD ---' });

    // 🔹 Escolaridad
    Object.keys(stats.escolaridad).forEach(key => {
      estadisticasRows.push({
        Categoria: key,
        Cantidad: stats.escolaridad[key],
        Valor: ''
      });
    });

    estadisticasRows.push({}, { Categoria: '--- OCUPACIÓN ---' });

    // 🔹 Ocupación
    Object.keys(stats.ocupacion).forEach(key => {
      estadisticasRows.push({
        Categoria: key,
        Cantidad: stats.ocupacion[key],
        Valor: ''
      });
    });

    estadisticasRows.push({}, { Categoria: '--- MUNICIPIO (Dirección) ---' });

    // 🔹 Municipio
    Object.keys(stats.municipio).forEach(key => {
      estadisticasRows.push({
        Categoria: key,
        Cantidad: stats.municipio[key],
        Valor: ''
      });
    });


    // Crear hoja Excel
    const estadisticasSheet = XLSX.utils.json_to_sheet(estadisticasRows);


    /* ==========================================================
       3) LIBRO FINAL
       ========================================================== */
    const book = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(book, detalleSheet, 'Detalle');
    XLSX.utils.book_append_sheet(book, estadisticasSheet, 'Estadisticas');

    XLSX.writeFile(book, `informe_demografico_${fecha}.xlsx`);
  }
}
