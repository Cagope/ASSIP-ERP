import { Injectable, inject } from '@angular/core';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';

import { DatosPersonalesApi } from '../features/hoja-vida/datos-personales/datos-personales.api';
import { FinancierosApi, FinancieroResponse } from '../features/hoja-vida/financieros/financieros.api';

@Injectable({ providedIn: 'root' })
export class FinancierosExporter {
  private personasApi = inject(DatosPersonalesApi);
  private financierosApi = inject(FinancierosApi);

  /**
   * Exporta a XLSX los datos financieros, cortados por persona.
   * - Si { idDatosPersonal } está definido, exporta solo ese afiliado.
   * - Si no, exporta filtrando por { q } (documento/nombre de la persona).
   * - Solo incluye personas que SÍ tienen registro financiero.
   * - Ajuste: separa TipoDocumento y Documento en columnas distintas.
   */
  async exportXlsx(options?: { q?: string; idDatosPersonal?: number; afiliadoNombre?: string }) {
    const q = (options?.q ?? '').trim().toLowerCase();
    const singleId = options?.idDatosPersonal && options.idDatosPersonal > 0 ? Number(options.idDatosPersonal) : null;

    const rows: any[] = [];

    if (singleId) {
      // ===== Exportar SOLO un afiliado =====
      const persona = await this.buscarPersona(singleId);
      if (persona) {
        const fin = await this.financieroDePersona(singleId);
        if (fin) rows.push(this.fila(persona, fin, options?.afiliadoNombre));
      }
    } else {
      // ===== Exportar lista con filtro q =====
      const personasResp = await firstValueFrom<any>(this.personasApi.list() as any);
      const personas: any[] = Array.isArray(personasResp)
        ? personasResp
        : (personasResp?.items ?? personasResp?.content ?? []);

      const personasFiltradas = (personas ?? []).filter((p) => this.cumpleFiltro(p, q));

      for (const p of personasFiltradas) {
        const id = this.personaId(p);
        if (!id) continue;
        const fin = await this.financieroDePersona(id);
        if (!fin) continue;
        rows.push(this.fila(p, fin));
      }
    }

    // Workbook
    const ws = XLSX.utils.json_to_sheet(rows);
    (ws as any)['!cols'] = [
      { wch: 10 }, // PersonaTipoDocumento
      { wch: 18 }, // PersonaDocumento
      { wch: 32 }, // PersonaNombre
      { wch: 14 }, // ValorSalario
      { wch: 14 }, // ValorPension
      { wch: 16 }, // IngresosArriendo
      { wch: 18 }, // IngresosComisiones
      { wch: 14 }, // OtrosIngresos
      { wch: 28 }, // ComentarioOtrosIngresos
      { wch: 26 }, // OrigenFondos
      { wch: 14 }, // TotalIngresos
      { wch: 16 }, // EgresosFamiliares
      { wch: 16 }, // EgresosArriendo
      { wch: 16 }, // EgresosCredito
      { wch: 14 }, // OtrosEgresos
      { wch: 28 }, // ComentarioOtrosEgresos
      { wch: 24 }, // RelacionFinanciera
      { wch: 20 }, // DeudaRelacionFinanciera
      { wch: 18 }, // TotalEgresosSinDeuda
      { wch: 14 }, // TotalActivos
      { wch: 14 }, // TotalPasivos
      { wch: 16 }, // TotalPatrimonio
    ];

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Financieros');

    const d = new Date();
    const filename = `financieros_${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}.xlsx`;
    XLSX.writeFile(wb, filename);
  }

  // ===== Helpers de obtención =====
  private async buscarPersona(idDatos: number): Promise<any | null> {
    const maybeGet = (this.personasApi as any)?.get;
    if (typeof maybeGet === 'function') {
      try {
        return await firstValueFrom<any>((maybeGet as Function).call(this.personasApi, idDatos) as any);
      } catch { /* fallback abajo */ }
    }
    try {
      const listResp = await firstValueFrom<any>(this.personasApi.list({ size: 1000 }) as any);
      const arr: any[] = Array.isArray(listResp) ? listResp : (listResp?.items ?? listResp?.content ?? []);
      return (arr ?? []).find((it: any) =>
        [it?.id, it?.idDatosPersonal, it?.id_datos_personales, it?.idDatosPersonales]
          .some((v: any) => Number(v) === idDatos)
      ) ?? null;
    } catch { return null; }
  }

  private async financieroDePersona(idDatos: number): Promise<FinancieroResponse | null> {
    try {
      const resp = await firstValueFrom<any>(this.financierosApi.getByPersona(idDatos) as any);
      const status = resp?.status ?? 200;
      if (status === 204) return null;
      return (resp?.body ?? resp ?? null) as FinancieroResponse | null;
    } catch { return null; }
  }

  private cumpleFiltro(p: any, q: string): boolean {
    if (!q) return true;
    const full =
      `${p?.nombres ?? ''} ${p?.apellidos ?? ''} ${p?.primerNombre ?? ''} ${p?.segundoNombre ?? ''} ${p?.primerApellido ?? ''} ${p?.segundoApellido ?? ''}`
        .replace(/\s+/g, ' ')
        .trim()
        .toLowerCase();
    const doc = String(p?.documento ?? '').toLowerCase();
    return doc.includes(q) || full.includes(q);
  }

  // ===== Helpers de fila XLSX =====
  private fila(p: any, fin: FinancieroResponse, nombreOpcional?: string) {
    const personaNombre = nombreOpcional || this.personaNombre(p);
    const personaTipoDoc = this.personaTipoDocumento(p);
    const personaDoc = this.personaDocumentoNumero(p);

    const totalIngresos =
      (fin.valor_salario ?? 0) + (fin.valor_pension ?? 0) + (fin.ingresos_arriendo ?? 0) +
      (fin.ingresos_comisiones ?? 0) + (fin.otros_ingresos ?? 0);

    const totalEgresosSinDeuda =
      (fin.egresos_familiares ?? 0) + (fin.egresos_arriendo ?? 0) +
      (fin.egresos_credito ?? 0) + (fin.otros_egresos ?? 0);

    const totalPatrimonio = (fin.total_activos ?? 0) - (fin.total_pasivos ?? 0);

    return {
      // Persona
      PersonaTipoDocumento: personaTipoDoc,
      PersonaDocumento: personaDoc,
      PersonaNombre: personaNombre,

      // Ingresos
      ValorSalario: fin.valor_salario ?? 0,
      ValorPension: fin.valor_pension ?? 0,
      IngresosArriendo: fin.ingresos_arriendo ?? 0,
      IngresosComisiones: fin.ingresos_comisiones ?? 0,
      OtrosIngresos: fin.otros_ingresos ?? 0,
      ComentarioOtrosIngresos: (fin.comentario_otros_ingresos ?? '').trim(),
      OrigenFondos: (fin.origen_fondos ?? '').trim(),
      TotalIngresos: totalIngresos,

      // Egresos
      EgresosFamiliares: fin.egresos_familiares ?? 0,
      EgresosArriendo: fin.egresos_arriendo ?? 0,
      EgresosCredito: fin.egresos_credito ?? 0,
      OtrosEgresos: fin.otros_egresos ?? 0,
      ComentarioOtrosEgresos: (fin.comentario_otros_egresos ?? '').trim(),
      RelacionFinanciera: (fin.relacion_financiera ?? '').trim(),
      DeudaRelacionFinanciera: fin.deuda_relacion_financiera ?? 0,
      TotalEgresosSinDeuda: totalEgresosSinDeuda,

      // Patrimonio
      TotalActivos: fin.total_activos ?? 0,
      TotalPasivos: fin.total_pasivos ?? 0,
      TotalPatrimonio: totalPatrimonio,
    };
  }

  // ===== Helpers persona (separados tipo + número) =====
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

  private personaTipoDocumento(p: any): string {
    return (p?.tipoDocumento ?? p?.tipo_documento ?? '').toString().trim();
  }

  private personaDocumentoNumero(p: any): string {
    return String(p?.documento ?? '').toString().trim();
  }
}
