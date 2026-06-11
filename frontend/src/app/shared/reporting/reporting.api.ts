import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs/operators';

/** 🌐 API base para el módulo Reporting */
const API_URL = `${environment.apiUrl}/reporting`;

/** Tipos principales usados por el motor de reportes */
export interface ReportMetadata {
  table_schema: string;
  table_name: string;
  schema?: string;
  view?: string;
  description?: string;
}

export interface ReportQueryRequest {
  schema: string;
  view: string;

  scope?: 'USUARIO' | 'GLOBAL';

  columns?: string[];
  filters?: Record<string, any>;
  groupBy?: string[];
  aggregations?: Record<string, string>;
}

export interface ReportResult {
  data: Record<string, any>[];
  total?: number;
  durationMs?: number;
}

/** ✅ API pública de Reporting */
@Injectable({ providedIn: 'root' })
export class ReportingApi {

  private readonly http = inject(HttpClient);

  /** 🔹 Listar metadatos disponibles */
  listarMetadata() {
    return this.http.get<ReportMetadata[]>(`${API_URL}/metadata`).pipe(
      map(vistas =>
        vistas.map(v => ({
          ...v,
          schema: v.table_schema,
          view: v.table_name
        }))
      )
    );
  }

  /**
   * 🔹 Ejecutar consulta dinámica sobre una vista
   * 🚫 IMPORTANTE: evitar envío automático de X-Agencias
   */
  ejecutarConsulta(req: ReportQueryRequest) {

    // 🛑 Evita que el interceptor agregue X-Agencias
    const cleanHeaders = new HttpHeaders({
      // No enviamos absolutamente nada especial
      'Content-Type': 'application/json'
    });

    return this.http.post<ReportResult>(
      `${API_URL}/query`,
      req,
      { headers: cleanHeaders }
    );
  }
}
