import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';

import { DirectivosApi, DirectivoListItemDTO } from '../features/general/directivos/directivos.api';
import { CatalogosApi } from '../shared/catalogos/catalogos.api';

@Injectable({ providedIn: 'root' })
export class DirectivosExporter {
  private directivosApi = inject(DirectivosApi);
  private catalogosApi = inject(CatalogosApi);

  /**
   * Exporta a XLSX el listado de directivos (misma lógica que el list).
   * - Respeta filtro q (documento o nombre).
   * - Intenta traducir el código de tipo de directivo a su nombre (catálogo).
   * - Fechas formateadas yyyy-mm-dd.
   */
  async exportXlsx(options?: { q?: string | undefined }) {
    const q = (options?.q ?? '').trim();

    // 1) Catálogo tipos (código → nombre)
    const tipos = await firstValueFrom(this.catalogosApi.tiposDirectivos().pipe());
    const tipoMap = new Map<string, string>(
      (tipos ?? []).map(t => [t.codigo, t.nombre])
    );

    // 2) Cargar lista (acepta array o Page)
    const resp: any = await firstValueFrom(this.directivosApi.list(q));
    const rows: DirectivoListItemDTO[] = Array.isArray(resp)
      ? resp
      : (resp?.content ?? resp?.items ?? []);

    // 3) Armar filas XLSX
    const out = rows.map(r => {
      const tipoNombre = tipoMap.get(r.codigoTipoDirectivo) ?? r.codigoTipoDirectivo;
      return {
        Documento: r.documento ?? '',
        Nombre: r.nombrePersona ?? '',
        TipoDirectivo: `${r.codigoTipoDirectivo ?? ''} - ${tipoNombre ?? ''}`.trim(),
        Calidad: this.nomCalidad(r.calidadDirectivo),
        Estado: this.nomEstado(r.estadoDirectivo),
        ActaAsamblea: (r as any).actaAsamblea ?? '—',         // por si lo tienes en list/print
        FechaAsamblea: this.toYMD((r as any).fechaAsamblea),  // yyyy-mm-dd o '—'
        ResolucionSES: (r as any).resolucionSes ?? '—',
        FechaResolucion: this.toYMD(r.fechaResolucion as any),
        FechaRetiro: this.toYMD(r.fechaRetiro as any),
        PeriodosVigencia: r.periodosVigencia ?? 0,
      };
    });

    // 4) Hoja y libro
    const ws = XLSX.utils.json_to_sheet(out);
    (ws as any)['!cols'] = [
      { wch: 16 }, // Documento
      { wch: 36 }, // Nombre
      { wch: 28 }, // TipoDirectivo
      { wch: 12 }, // Calidad
      { wch: 12 }, // Estado
      { wch: 16 }, // ActaAsamblea
      { wch: 14 }, // FechaAsamblea
      { wch: 16 }, // ResolucionSES
      { wch: 14 }, // FechaResolucion
      { wch: 14 }, // FechaRetiro
      { wch: 10 }, // PeriodosVigencia
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Directivos');

    const d = new Date();
    const filename = `directivos_${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}.xlsx`;
    XLSX.writeFile(wb, filename);
  }

  // ===== Helpers =====
  private nomCalidad(v?: string) { return v === '1' ? 'Principal' : 'Suplente'; }
  private nomEstado(v?: string) { return v === '1' ? 'Nombrado' : v === '2' ? 'Retirado' : 'Excluido'; }

  /** Acepta Date | string | null/undefined y devuelve yyyy-mm-dd o '—' */
  private toYMD(v: any): string {
    if (!v) return '—';
    const d = v instanceof Date ? v : new Date(v);
    if (isNaN(d.getTime())) return '—';
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
