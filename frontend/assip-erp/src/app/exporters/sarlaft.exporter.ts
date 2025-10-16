import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';

import { DatosPersonalesApi } from '../features/hoja-vida/datos-personales/datos-personales.api';
import { SarlaftApi, SarlaftResponse } from '../features/hoja-vida/sarlaft/sarlaft.api';

@Injectable({ providedIn: 'root' })
export class SarlaftExporter {
  private personasApi = inject(DatosPersonalesApi);
  private sarlaftApi = inject(SarlaftApi);

  /**
   * Exporta SARLAFT por persona (solo quienes tienen registro).
   * - Filtra por q (documento / nombre) opcional.
   */
  async exportXlsx(options?: { q?: string | undefined }) {
    const q = (options?.q ?? '').trim().toLowerCase();

    const personasResp: any = await firstValueFrom(this.personasApi.list({ size: 1000 }));
    const personas: any[] = Array.isArray(personasResp)
      ? personasResp
      : (personasResp?.items ?? personasResp?.content ?? []);

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

    const rows: any[] = [];
    for (const p of personasFiltradas) {
      const id = this.personaId(p);
      if (!id) continue;

      const resp = await firstValueFrom(this.sarlaftApi.getByPersona(id));
      const row: SarlaftResponse | null = resp?.status === 204 ? null : (resp?.body ?? null);
      if (!row) continue;

      const personaTipoDoc = (p?.tipoDocumento ?? p?.tipo_documento ?? '').toString().trim();
      const personaDoc = (p?.documento ?? '').toString().trim();
      const personaNombre = this.personaNombre(p);

      rows.push({
        TipoDocumento: personaTipoDoc,
        Documento: personaDoc,
        PersonaNombre: personaNombre,

        ExoneracionUIAF: this.siNo(row.exoneracion_uiaf),
        FechaExoneracion: row.fecha_exoneracion ?? '',

        AsociadoPEPs: this.siNo(row.asociado_peps),
        TipoPEPs: row.tipo_peps ?? '',
        ObservacionesPEPs: row.observaciones_peps ?? '',
        FechaInicialPEPs: row.fecha_inicial_peps ?? '',
        FechaFinalPEPs: row.fecha_final_peps ?? '',

        FamiliarPEPs: this.siNo(row.familia_peps),
        TipoFamiliarPEPs: row.tipo_familia_peps ?? '',
        CedulaFamiliarPEPs: row.cedula_familia_peps ?? '',
        ParentescoFamiliar: row.codigo_parentesco ?? '',
        NombreFamiliarPEPs: row.nombre_familia_peps ?? '',

        NegocioMonedaExtranjera: this.siNo(row.moneda_extranjera),
        ObsMonedaExtranjera: row.observacion_moneda_extranjera ?? '',

        CuentaExtranjero: this.siNo(row.cuenta_extranjero),
        TipoMonedaExtranjera: row.tipo_moneda_extranjera ?? '',
        NumeroCuentaExtranjero: row.numero_cuenta_extranjero ?? '',
        BancoExtranjero: row.nombre_banco_extranjero ?? '',
        CiudadCuentaExtranjero: row.ciudad_cuenta_extranjero ?? '',
        PaisCuentaExtranjero: row.pais_cuenta_extranjero ?? '',
      });
    }

    const ws = XLSX.utils.json_to_sheet(rows);
    (ws as any)['!cols'] = [
      { wch: 14 }, // TipoDocumento
      { wch: 16 }, // Documento
      { wch: 36 }, // PersonaNombre
      { wch: 14 }, // ExoneracionUIAF
      { wch: 12 }, // FechaExoneracion
      { wch: 12 }, // AsociadoPEPs
      { wch: 8 },  // TipoPEPs
      { wch: 40 }, // ObservacionesPEPs
      { wch: 12 }, // FechaInicialPEPs
      { wch: 12 }, // FechaFinalPEPs
      { wch: 14 }, // FamiliarPEPs
      { wch: 8 },  // TipoFamiliarPEPs
      { wch: 20 }, // CedulaFamiliarPEPs
      { wch: 10 }, // ParentescoFamiliar
      { wch: 30 }, // NombreFamiliarPEPs
      { wch: 18 }, // NegocioMonedaExtranjera
      { wch: 40 }, // ObsMonedaExtranjera
      { wch: 16 }, // CuentaExtranjero
      { wch: 16 }, // TipoMonedaExtranjera
      { wch: 22 }, // NumeroCuentaExtranjero
      { wch: 28 }, // BancoExtranjero
      { wch: 18 }, // CiudadCuentaExtranjero
      { wch: 18 }, // PaisCuentaExtranjero
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'SARLAFT');

    const d = new Date();
    const filename = `sarlaft_${d.getFullYear()}${String(d.getMonth()+1).padStart(2,'0')}${String(d.getDate()).padStart(2,'0')}.xlsx`;
    XLSX.writeFile(wb, filename);
  }

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
  private siNo(v: any): string { return v ? 'Sí' : 'No'; }
}
