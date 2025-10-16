import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';

import { DatosPersonalesApi } from '../features/hoja-vida/datos-personales/datos-personales.api';
import { DatosFamiliaresApi } from '../features/hoja-vida/datos-familiares/datos-familiares.api';

@Injectable({ providedIn: 'root' })
export class DatosFamiliaresExporter {
  private personasApi = inject(DatosPersonalesApi);
  private familiaresApi = inject(DatosFamiliaresApi);

  /**
   * Exporta a XLSX los datos familiares, cortados por persona.
   * - Incluye Documento y Nombre de la persona en cada fila.
   * - Solo exporta personas que tengan familiares.
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

    // 3) Por persona: traer familiares → armar filas
    const rows: any[] = [];
    for (const p of personasFiltradas) {
      const id = this.personaId(p);
      if (!id) continue;

      const familiares = await firstValueFrom(this.familiaresApi.listByPersona(id));
      if (!familiares || familiares.length === 0) continue;

      const personaNombre = this.personaNombre(p);
      const personaDoc = this.personaDocumento(p);

      for (const f of familiares) {
        rows.push({
          PersonaDocumento: personaDoc,
          PersonaNombre: personaNombre,
          Parentesco: this.nombreParentescoFromItem(f),
          NombreFamiliar: this.nom(f),
          DocumentoFamiliar: this.doc(f),
          FechaNacimiento: f?.fecha_nacimiento ?? '',
          Telefono: this.tel(f),
          Celular: this.cel(f),
          Direccion: this.dir(f),
          Referencia: (f?.referencia_familiar ?? false) ? 'Sí' : '',
        });
      }
    }

    // 4) Workbook
    const ws = XLSX.utils.json_to_sheet(rows);
    (ws as any)['!cols'] = [
      { wch: 18 }, // PersonaDocumento
      { wch: 32 }, // PersonaNombre
      { wch: 18 }, // Parentesco
      { wch: 36 }, // NombreFamiliar
      { wch: 20 }, // DocumentoFamiliar
      { wch: 12 }, // FechaNacimiento
      { wch: 12 }, // Telefono
      { wch: 14 }, // Celular
      { wch: 40 }, // Direccion
      { wch: 10 }, // Referencia
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'DatosFamiliares');

    const d = new Date();
    const filename = `datos_familiares_${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}.xlsx`;
    XLSX.writeFile(wb, filename);
  }

  // ===== Helpers (alineados con tus componentes/print) =====
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

  private codigoPar(it: any): string | null {
    return it?.codigo_parentesco ?? it?.codigoParentesco ?? null;
  }

  private nombreParentescoFromItem(it: any): string {
    const codigo = this.codigoPar(it);
    if (!codigo) return '';
    // Si quieres usar catálogo: inyecta CatalogosApi y mapea a nombre
    return String(codigo);
  }

  private doc(it: any): string {
    return it?.documento_datos_familiar ?? it?.documentoDatosFamiliar ?? '';
  }

  private nom(it: any): string {
    return it?.nombre_datos_familiar ?? it?.nombreDatosFamiliar ?? '';
  }

  private tel(it: any): string {
    return it?.telefono_datos_familiar ?? it?.telefonoDatosFamiliar ?? '';
  }

  private cel(it: any): string {
    return it?.celular_datos_familiar ?? it?.celularDatosFamiliar ?? '';
  }

  private dir(it: any): string {
    return (
      it?.direccion_datos_familiar ??
      it?.direccionDatosFamiliar ??
      it?.direccion_familiar ??
      it?.direccionFamiliar ??
      it?.direccion ??
      ''
    );
  }
}
