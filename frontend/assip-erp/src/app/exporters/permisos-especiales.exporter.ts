import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';

import { DatosPersonalesApi } from '../features/hoja-vida/datos-personales/datos-personales.api';
import { PermisosEspecialesApi, PermisoEspecialResponse } from '../features/hoja-vida/permisos-especiales/permisos-especiales.api';

@Injectable({ providedIn: 'root' })
export class PermisosEspecialesExporter {
  private personasApi = inject(DatosPersonalesApi);
  private permisosApi = inject(PermisosEspecialesApi);

  /**
   * Exporta a XLSX los permisos especiales (1:1 por persona).
   * - Solo exporta personas que tengan registro de permisos.
   * - Si pasas { q }, filtra por documento/nombre de persona.
   */
  async exportXlsx(options?: { q?: string | undefined }) {
    const q = (options?.q ?? '').trim().toLowerCase();

    // 1) Personas (acepta array o Page)
    const personasResp: any = await firstValueFrom(this.personasApi.list({ size: 1000 }));
    const personas: any[] = Array.isArray(personasResp)
      ? personasResp
      : (personasResp?.items ?? personasResp?.content ?? []);

    // 2) Filtro por q (igual que en el listado)
    const match = (p: any) => {
      if (!q) return true;
      const full = `${p?.nombres ?? ''} ${p?.apellidos ?? ''} ${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''} ${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`
        .replace(/\s+/g, ' ')
        .trim()
        .toLowerCase();
      const doc = String(p?.documento ?? '').toLowerCase();
      return doc.includes(q) || full.includes(q);
    };
    const personasFiltradas = (personas ?? []).filter(match);

    // 3) Por persona: traer permiso → armar filas (si existe)
    const rows: any[] = [];
    for (const p of personasFiltradas) {
      const idDatos = this.personaId(p);
      if (!idDatos) continue;

      const resp: any = await firstValueFrom(this.permisosApi.getByPersona(idDatos)).catch(() => null);
      if (!resp) continue;
      if (resp.status === 204) continue; // sin registro

      const row: PermisoEspecialResponse | null = resp.body ?? null;
      if (!row) continue;

      const personaNombre = this.personaNombre(p);
      const { tipoDoc, documento } = this.personaTipoYDoc(p);

      rows.push({
        PersonaTipoDocumento: tipoDoc,
        PersonaDocumento: documento,
        PersonaNombre: personaNombre,
        RecibeLlamadas: row.recibe_llamadas ? 'Sí' : 'No',
        RecibeSMS: row.recibe_msm ? 'Sí' : 'No',
        RecibeEmails: row.recibe_emails ? 'Sí' : 'No',
        RecibeCartas: row.recibe_cartas ? 'Sí' : 'No',
        RecibeRedesSociales: row.recibe_redes_sociales ? 'Sí' : 'No',
      });
    }

    // 4) Workbook
    const ws = XLSX.utils.json_to_sheet(rows);
    (ws as any)['!cols'] = [
      { wch: 12 }, // PersonaTipoDocumento
      { wch: 18 }, // PersonaDocumento
      { wch: 36 }, // PersonaNombre
      { wch: 16 }, // RecibeLlamadas
      { wch: 12 }, // RecibeSMS
      { wch: 14 }, // RecibeEmails
      { wch: 14 }, // RecibeCartas
      { wch: 22 }, // RecibeRedesSociales
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'PermisosEspeciales');

    const d = new Date();
    const filename =
      `permisos_especiales_${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}.xlsx`;
    XLSX.writeFile(wb, filename);
  }

  // ===== Helpers =====
  private personaId(p: any): number {
    const candidates = [p?.id, p?.idDatosPersonales, p?.id_datos_personales, p?.idDatosPersonal];
    const found = candidates.find((v: any) => typeof v === 'number' && !Number.isNaN(v) && v > 0);
    return (found ?? 0) as number;
  }

  private personaNombre(p: any): string {
    const nombres = (p?.nombres ?? `${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''}`).toString().trim();
    const apellidos = (p?.apellidos ?? `${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`).toString().trim();
    return `${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim();
  }

  private personaTipoYDoc(p: any): { tipoDoc: string; documento: string } {
    const tipo = (p?.tipoDocumento ?? p?.tipo_documento ?? '').toString().trim();
    const doc  = (p?.documento ?? '').toString().trim();
    return { tipoDoc: tipo, documento: doc };
  }
}
