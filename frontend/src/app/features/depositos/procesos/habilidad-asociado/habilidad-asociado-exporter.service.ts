import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class HabilidadAsociadoExporterService {

  exportarExcel(items: any[], filtros: any): void {
    if (!items || items.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const wb = XLSX.utils.book_new();

    // ============================
    // Separar HÁBILES / INHÁBILES
    // ============================
    const habiles = items.filter(x => (x.resultado || '').toUpperCase() === 'HÁBIL');
    const inHabiles = items.filter(x => (x.resultado || '').toUpperCase() !== 'HÁBIL');

    this.agregarHojaPorZonaYSubzona(wb, 'HÁBILES', habiles);
    this.agregarHojaPorZonaYSubzona(wb, 'INHÁBILES', inHabiles);

    // ============================
    // Nombre del archivo
    // ============================
    const fechaInicio   = filtros.fechaInicio ?? 'sin_fecha_i';
    const fechaFin      = filtros.fechaFin ?? 'sin_fecha_f';
    const codigoAgencia = filtros.agenciaId ?? '00';

    const nombreArchivo =
      `habilidad_asociado_${fechaInicio}_${fechaFin}_${codigoAgencia}.xlsx`;

    XLSX.writeFile(wb, nombreArchivo);
  }

  // ============================================================
  // Construye hoja con:
  //  Zona
  //    Subzona
  //      Detalle
  //      TOTAL Subzona
  //    ...
  //  total zona
  //  TOTAL GENERAL
  // ============================================================
  private agregarHojaPorZonaYSubzona(
    wb: XLSX.WorkBook,
    nombreHoja: string,
    lista: any[]
  ): void {

    const rows: any[][] = [];

    // Si no hay datos, sólo ponemos un mensaje
    if (!lista || lista.length === 0) {
      rows.push([`No hay registros para ${nombreHoja}`]);
      const ws = XLSX.utils.aoa_to_sheet(rows);
      XLSX.utils.book_append_sheet(wb, ws, nombreHoja);
      return;
    }

    const zonasUnicas = Array.from(
      new Set((lista.map(x => x.zona ?? 'SIN_ZONA')))
    );

    let totalGeneral = 0;

    zonasUnicas.forEach(zona => {
      const itemsZona = lista.filter(x => (x.zona ?? 'SIN_ZONA') === zona);
      if (itemsZona.length === 0) {
        return;
      }

      // Encabezado de zona
      rows.push(['Zona', zona]);
      rows.push([]); // línea en blanco

      const subzonas = Array.from(
        new Set((itemsZona.map(x => x.subzona ?? 'SIN_SUBZONA')))
      );

      let totalZona = 0;

      subzonas.forEach(subzona => {
        const itemsSubzona = itemsZona.filter(
          x => (x.subzona ?? 'SIN_SUBZONA') === subzona
        );
        if (itemsSubzona.length === 0) {
          return;
        }

        // Encabezado de subzona
        rows.push(['Subzona', subzona]);
        rows.push([]); // blanco

        // Cabecera de columnas
        rows.push([
          'Documento',
          'Nombre',
          'Edad',
          'Tipo Persona',
          'Saldo Actual',
          'Aportes',
          'Resultado'
        ]);

        // Filas de detalle
        itemsSubzona.forEach(r => {
          rows.push([
            r.documento,
            r.nombre,
            r.edad,
            r.tipoPersona === '1' ? 'Natural' : 'Jurídica',
            Number(r.saldoHoy ?? 0),
            Number(r.totalAportes ?? 0),
            r.resultado
          ]);
        });

        rows.push([]); // blanco

        // TOTAL Subzona
        rows.push([
          'TOTAL',
          subzona,
          '',
          '',
          '',
          '',
          itemsSubzona.length
        ]);

        rows.push([]); // blanco

        totalZona += itemsSubzona.length;
      });

      // total zona
      rows.push([
        'total zona',
        zona,
        '',
        '',
        '',
        '',
        totalZona
      ]);

      rows.push([]); // separación entre zonas

      totalGeneral += totalZona;
    });

    // TOTAL GENERAL al final de la hoja
    rows.push([
      'TOTAL GENERAL',
      '',
      '',
      '',
      '',
      '',
      totalGeneral
    ]);

    const ws = XLSX.utils.aoa_to_sheet(rows);
    XLSX.utils.book_append_sheet(wb, ws, nombreHoja);
  }
}
