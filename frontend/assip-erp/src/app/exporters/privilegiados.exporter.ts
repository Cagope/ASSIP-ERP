import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';
import { PrivilegiadosApi, PrivilegiadoListItemDTO } from '../features/general/privilegiados/privilegiados.api';

@Injectable({ providedIn: 'root' })
export class PrivilegiadosExporter {
  private api = inject(PrivilegiadosApi);

  /**
   * Exporta a XLSX la lista de privilegiados.
   * Requiere ?idDirectivo= en la URL. Filtra por q (documento/nombre) igual que el listado.
   */
  async exportXlsx(options?: { q?: string | undefined }) {
    const q = (options?.q ?? '').trim() || undefined;

    // Lee idDirectivo desde el querystring de la URL actual
    const qs = new URLSearchParams(location.search);
    const id = Number(qs.get('idDirectivo'));
    const idDirectivo = Number.isFinite(id) && id > 0 ? id : NaN;
    if (!Number.isFinite(idDirectivo)) {
      throw new Error('Falta idDirectivo en la URL (?idDirectivo=).');
    }

    const data = await firstValueFrom(this.api.list(idDirectivo, q)).catch(() => [] as PrivilegiadoListItemDTO[]);
    const rows = (Array.isArray(data) ? data : (data as any)?.content ?? []) as PrivilegiadoListItemDTO[];

    const json = rows.map(r => ({
      DocumentoDirectivo: r.documentoDirectivo ?? '',
      NombreDirectivo:    r.nombreDirectivo ?? '',
      DocumentoPersona:   r.documentoPersona ?? '',
      NombrePersona:      r.nombrePersona ?? '',
      CodigoParentesco:   r.codigoParentesco ?? '',
      ParentescoNombre:   r.parentescoNombre ?? '',
    }));

    const ws = XLSX.utils.json_to_sheet(json);
    (ws as any)['!cols'] = [
      { wch: 18 }, // DocumentoDirectivo
      { wch: 36 }, // NombreDirectivo
      { wch: 18 }, // DocumentoPersona
      { wch: 36 }, // NombrePersona
      { wch: 12 }, // CodigoParentesco
      { wch: 28 }, // ParentescoNombre
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Privilegiados');

    const d = new Date();
    const filename =
      `privilegiados_${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}.xlsx`;
    XLSX.writeFile(wb, filename);
  }
}
