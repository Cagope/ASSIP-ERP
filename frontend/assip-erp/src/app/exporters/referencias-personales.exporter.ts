import * as XLSX from 'xlsx';
import { Injectable, inject } from '@angular/core';
import { of, firstValueFrom, catchError } from 'rxjs';

import { DatosPersonalesApi } from '../features/hoja-vida/datos-personales/datos-personales.api';
import { ReferenciasPersonalesApi, ReferenciaPersonal } from '../features/hoja-vida/referencias-personales/referencias-personales.api';

type PersonaItem = {
  id?: number;
  idDatosPersonales?: number;
  id_datos_personales?: number;
  idDatosPersonal?: number;
  documento?: string;
  primerNombre?: string;
  segundoNombre?: string;
  primerApellido?: string;
  segundoApellido?: string;
  nombres?: string;
  apellidos?: string;
  [k: string]: any;
};

@Injectable({ providedIn: 'root' })
export class ReferenciasPersonalesExporter {
  private personasApi = inject(DatosPersonalesApi);
  private refApi = inject(ReferenciasPersonalesApi);

  private personaId(p: PersonaItem): number {
    const candidates = [
      (p as any)?.id,
      (p as any)?.idDatosPersonales,
      (p as any)?.id_datos_personales,
      (p as any)?.idDatosPersonal,
    ];
    const found = candidates.find(v => typeof v === 'number' && !Number.isNaN(v) && v > 0);
    return (found ?? 0) as number;
  }

  private nombreCompleto(p: PersonaItem): string {
    const nombres = (p.nombres ?? `${p.primerNombre ?? ''} ${p.segundoNombre ?? ''}`).toString().trim();
    const apellidos = (p.apellidos ?? `${p.primerApellido ?? ''} ${p.segundoApellido ?? ''}`).toString().trim();
    return `${nombres} ${apellidos}`.replace(/\s+/g, ' ').trim();
  }

  async exportXlsx(opts: { q?: string } = {}): Promise<void> {
    // 1) Personas (filtradas si aplica)
    const resp: any = await firstValueFrom(
      this.personasApi.list({ q: opts.q || undefined, page: 0, size: 1000, sort: 'idDatosPersonal,desc' })
        .pipe(catchError(() => of(null)))
    );

    const personas: PersonaItem[] = Array.isArray(resp)
      ? resp
      : (resp?.content ?? resp?.items ?? []);

    // 2) Por cada persona, única referencia (si existe)
    const rows: any[] = [];
    for (const p of personas) {
      const id = this.personaId(p);
      if (!id) continue;

      const ref: ReferenciaPersonal | null = await firstValueFrom(
        this.refApi.getByPersona(id).pipe(catchError(() => of(null)))
      );

      if (!ref) continue;

      rows.push({
        Documento: String(p.documento ?? '').trim(),
        Afiliado: this.nombreCompleto(p),
        'Nombre referencia': ref.nombre_referencia_personal ?? '',
        Dirección: ref.direccion_referencia_personal ?? '',
        Teléfono: ref.telefono_referencia_personal ?? '',
        Celular: ref.celular_referencia_personal ?? '',
        DepartamentoId: ref.id_departamento ?? '',
        CiudadId: ref.id_ciudad ?? '',
      });
    }

    // 3) XLSX
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(rows);
    XLSX.utils.book_append_sheet(wb, ws, 'Referencias');

    // 4) Descargar
    const date = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-');
    XLSX.writeFile(wb, `referencias-personales_${date}.xlsx`);
  }
}
