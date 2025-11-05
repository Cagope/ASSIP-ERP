import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs/operators'; // 👈 Import necesario

/** 🌐 API base para el módulo Reporting */
const API_URL = `${environment.apiUrl}/reporting`;

/** Tipos principales usados por el motor de reportes */
export interface ReportMetadata {
  /** 🔹 Nombre del esquema (reporting, etc.) */
  table_schema: string;

  /** 🔹 Nombre de la vista en base de datos */
  table_name: string;

  /** 🪶 Alias usados en frontend */
  schema?: string;   // alias de table_schema
  view?: string;     // alias de table_name

  description?: string;
}

export interface ReportQueryRequest {
  schema: string;
  view: string;
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

  /** 🔹 Listar metadatos disponibles en el esquema reporting */
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

  /** 🔹 Ejecutar consulta dinámica sobre una vista */
  ejecutarConsulta(req: ReportQueryRequest) {
    return this.http.post<ReportResult>(`${API_URL}/query`, req);
  }
}
