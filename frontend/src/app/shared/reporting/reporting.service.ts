import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ReportingApi, ReportQueryRequest, ReportResult, ReportMetadata } from './reporting.api';

/**
 * 💼 Servicio central del módulo Reporting.
 * ------------------------------------------------------------
 * Simplifica el acceso a los endpoints del módulo Reporting.
 * Usa Promises (async/await) para un flujo más limpio.
 */
@Injectable({ providedIn: 'root' })
export class ReportingService {
  private readonly api = inject(ReportingApi);

  /** 🔹 Lista las vistas disponibles (reporting_metadata) */
  async listarVistas(): Promise<ReportMetadata[]> {
    try {
      const res = await firstValueFrom(this.api.listarMetadata());
      return res ?? [];
    } catch (err) {
      console.error('❌ Error al listar vistas de reporting:', err);
      return [];
    }
  }

  /** 🔹 Ejecuta un reporte dinámico (vista + filtros) */
  async ejecutarReporte(req: ReportQueryRequest): Promise<ReportResult> {
    try {
      const res = await firstValueFrom(this.api.ejecutarConsulta(req));
      return {
        data: res?.data ?? [],
        total: res?.total ?? (res?.data?.length ?? 0),
        durationMs: res?.durationMs ?? 0
      };
    } catch (err) {
      console.error('❌ Error al ejecutar reporte:', err);
      return { data: [], total: 0 };
    }
  }
}
