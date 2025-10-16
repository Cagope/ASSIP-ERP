import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';

import { DatosPersonalesApi } from '../features/hoja-vida/datos-personales/datos-personales.api';
import { LaboralesApi } from '../features/hoja-vida/laborales/laborales.api';

@Injectable({ providedIn: 'root' })
export class LaboralesExporter {
  private personasApi = inject(DatosPersonalesApi);
  private laboralesApi = inject(LaboralesApi);

  /**
   * Exporta a XLSX la info laboral 1:1, cortada por persona.
   * - Incluye Documento y Nombre de la persona en cada fila.
   * - Solo exporta personas que tengan registro laboral.
   * - Si pasas { q }, filtra por documento/nombre de persona (cliente).
   */
  async exportXlsx(options?: { q?: string | undefined }) {
    const q = (options?.q ?? '').trim().toLowerCase();

    // 1) Personas (acepta array o Page)
    const personasResp: any = await firstValueFrom(this.personasApi.list({ size: 2000 }));
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

    // 3) Por persona: traer laboral → armar filas
    const rows: any[] = [];
    for (const p of personasFiltradas) {
      const id = this.personaId(p);
      if (!id) continue;

      try {
        const laboral = await firstValueFrom(this.laboralesApi.getByPersona(id));
        if (!laboral) continue;

        rows.push({
          PersonaDocumento: this.personaDocumento(p),
          PersonaNombre:    this.personaNombre(p),
          NombreEmpresa:    laboral?.nombreEmpresa ?? '',
          Direccion:        laboral?.direccion ?? '',
          PaisId:           laboral?.idPais ?? '',
          DeptoId:          laboral?.idDepartamento ?? '',
          CiudadId:         laboral?.idCiudad ?? '',
          TelefonoEmpresa:  laboral?.telefonoEmpresa ?? '',
          CelularEmpresa:   laboral?.celularEmpresa ?? '',
          CorreoEmpresa:    laboral?.correoEmpresa ?? '',
          TipoEmpresa:      laboral?.codigoTipoEmpresa ?? '',
          TipoContrato:     laboral?.codigoTipoContrato ?? '',
          Jornada:          laboral?.codigoJornada ?? '',
          NombreContacto:   laboral?.nombreContacto ?? '',
          CelularContacto:  laboral?.celularContacto ?? '',
          FechaVinculacion: laboral?.fechaVinculacion ?? '',
        });
      } catch {
        // si falla, saltamos esa persona
      }
    }

    if (rows.length === 0) {
      alert('No hay información laboral para exportar.');
      return;
    }

    // 4) Workbook
    const ws = XLSX.utils.json_to_sheet(rows);
    (ws as any)['!cols'] = [
      { wch: 18 }, // PersonaDocumento
      { wch: 32 }, // PersonaNombre
      { wch: 36 }, // NombreEmpresa
      { wch: 40 }, // Direccion
      { wch: 8  }, // PaisId
      { wch: 10 }, // DeptoId
      { wch: 10 }, // CiudadId
      { wch: 14 }, // TelefonoEmpresa
      { wch: 16 }, // CelularEmpresa
      { wch: 28 }, // CorreoEmpresa
      { wch: 14 }, // TipoEmpresa
      { wch: 16 }, // TipoContrato
      { wch: 12 }, // Jornada
      { wch: 28 }, // NombreContacto
      { wch: 16 }, // CelularContacto
      { wch: 14 }, // FechaVinculacion
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Laborales');

    const d = new Date();
    const filename = `laborales_${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}.xlsx`;
    XLSX.writeFile(wb, filename);
  }

  // ===== Helpers
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

  private personaDocumento(p: any): string {
    const tipo = (p?.tipoDocumento ?? p?.tipo_documento ?? '').toString().trim();
    const doc  = (p?.documento ?? '').toString().trim();
    return [tipo, doc].filter(Boolean).join(' ');
  }
}
