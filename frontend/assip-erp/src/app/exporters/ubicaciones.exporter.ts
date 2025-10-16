import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';

import { DatosPersonalesApi } from '../features/hoja-vida/datos-personales/datos-personales.api';
import { UbicacionesApi, UbicacionResponse } from '../features/hoja-vida/ubicaciones/ubicaciones.api';

@Injectable({ providedIn: 'root' })
export class UbicacionesExporter {
  private personasApi = inject(DatosPersonalesApi);
  private ubicacionesApi = inject(UbicacionesApi);

  /**
   * Exporta a XLSX las ubicaciones, cortadas por persona (1:1).
   * - Incluye Documento y Nombre de la persona en cada fila.
   * - Solo exporta personas que tengan ubicación (200); ignora 204.
   * - Si pasas { q }, filtra por documento/nombre de persona (cliente).
   */
  async exportXlsx(options?: { q?: string | undefined }) {
    const q = (options?.q ?? '').trim().toLowerCase();

    // 1) Personas (acepta array o Page)
    const personasResp: any = await firstValueFrom(this.personasApi.list());
    const personas: any[] = Array.isArray(personasResp)
      ? personasResp
      : (personasResp?.items ?? personasResp?.content ?? []);

    // 2) Filtro por q (igual que en el listado)
    const match = (p: any) => {
      if (!q) return true;
      const full =
        `${p?.nombres ?? ''} ${p?.apellidos ?? ''} ${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''} ${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`
          .replace(/\s+/g, ' ')
          .trim()
          .toLowerCase();
      const doc = String(p?.documento ?? '').toLowerCase();
      return doc.includes(q) || full.includes(q);
    };
    const personasFiltradas = (personas ?? []).filter(match);

    // 3) Por persona: traer ubicación → armar filas
    const rows: any[] = [];
    for (const p of personasFiltradas) {
      const id = this.personaId(p);
      if (!id) continue;

      const resp = await firstValueFrom(this.ubicacionesApi.getByPersona(id).pipe());
      if (!resp || resp.status === 204) continue;
      const r: UbicacionResponse | null = resp.body ?? null;
      if (!r) continue;

      rows.push({
        PersonaDocumento: this.personaDocumento(p),
        PersonaNombre: this.personaNombre(p),
        Direccion: r?.direccion ?? '',
        Barrio: r?.barrio ?? '',
        Telefono: r?.telefono ?? '',
        Celular1: r?.celular_uno ?? '',
        Celular2: r?.celular_dos ?? '',
        Correo: r?.correo ?? '',
        Pais: (r?.nombre_pais ?? r?.id_pais ?? ''),
        Departamento: (r?.nombre_departamento ?? r?.id_departamento ?? ''),
        Ciudad: (r?.nombre_ciudad ?? r?.id_ciudad ?? ''),
        Zona: (r?.nombre_zona ?? ''), // si no viene el nombre, lo dejamos vacío
        SubZona: (r?.nombre_sub_zona ?? r?.id_sub_zona ?? ''),
      });
    }

    // 4) Workbook
    const ws = XLSX.utils.json_to_sheet(rows);
    (ws as any)['!cols'] = [
      { wch: 18 }, // PersonaDocumento
      { wch: 32 }, // PersonaNombre
      { wch: 40 }, // Direccion
      { wch: 28 }, // Barrio
      { wch: 12 }, // Telefono
      { wch: 14 }, // Celular1
      { wch: 14 }, // Celular2
      { wch: 36 }, // Correo
      { wch: 18 }, // Pais
      { wch: 22 }, // Departamento
      { wch: 22 }, // Ciudad
      { wch: 22 }, // Zona
      { wch: 22 }, // SubZona
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Ubicaciones');

    const d = new Date();
    const filename = `ubicaciones_${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}.xlsx`;
    XLSX.writeFile(wb, filename);
  }

  // ===== Helpers =====
  private personaId(p: any): number {
    const candidates = [p?.id, p?.idDatosPersonales, p?.id_datos_personales, p?.idDatosPersonal];
    const found = candidates.find(v => typeof v === 'number' && !Number.isNaN(v) && v > 0);
    return (found ?? 0) as number;
  }

  private personaNombre(p: any): string {
    const nombres = (p?.nombres ?? `${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''}`).toString().trim();
    const apellidos = (p?.apellidos ?? `${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`).toString().trim();
    return `${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim();
  }

  private personaDocumento(p: any): string {
    const tipo = (p?.tipoDocumento ?? p?.tipo_documento ?? '').toString().trim();
    const doc  = (p?.documento ?? '').toString().trim();
    return [tipo, doc].filter(Boolean).join(' ');
  }
}
