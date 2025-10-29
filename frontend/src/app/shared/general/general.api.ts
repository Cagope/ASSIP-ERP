import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/* ======================================================
   🏢 API: General (Agencias, Zonas, SubZonas, etc.)
   ------------------------------------------------------
   Servicio centralizado para consultar catálogos del
   esquema "general" del backend.
   Incluye Zonas, SubZonas y Agencias.
   ====================================================== */

/** 🏦 Agencia */
export interface AgenciaDTO {
  idAgencia: number;
  nombreAgencia: string;
  direccion?: string;
}

/** 🌍 Zona */
export interface ZonaDTO {
  idZona: number;
  nombreZona: string;
}

/** 📍 SubZona */
export interface SubZonaDTO {
  idSubZona: number;
  nombreSubZona: string;
  idZona?: number;
}

@Injectable({ providedIn: 'root' })
export class GeneralApi {
  private readonly base = `${environment.apiUrl}/general`;

  constructor(private http: HttpClient) {}

  // ======================================================
  // 🏢 AGENCIAS
  // ======================================================

  /** 🔹 Lista todas las agencias activas */
  listarAgencias(): Observable<AgenciaDTO[]> {
    return this.http.get<AgenciaDTO[]>(`${this.base}/agencias`);
  }

  /** 🔹 Obtiene una agencia específica por ID */
  obtenerAgencia(id: number): Observable<AgenciaDTO> {
    return this.http.get<AgenciaDTO>(`${this.base}/agencias/${id}`);
  }

  // ======================================================
  // 🧭 ZONAS
  // ======================================================

  /** 🔹 Lista todas las zonas registradas */
  listarZonas(): Observable<ZonaDTO[]> {
    return this.http.get<ZonaDTO[]>(`${this.base}/zonas`);
  }

  /** 🔹 Obtiene una zona específica por ID */
  obtenerZona(id: number): Observable<ZonaDTO> {
    return this.http.get<ZonaDTO>(`${this.base}/zonas/${id}`);
  }

  // ======================================================
  // 📍 SUBZONAS
  // ======================================================

  /** 🔹 Lista todas las subzonas disponibles */
  listarSubZonas(): Observable<SubZonaDTO[]> {
    return this.http.get<SubZonaDTO[]>(`${this.base}/sub-zonas`);
  }

  /** 🔹 Obtiene una subzona específica por ID */
  obtenerSubZona(id: number): Observable<SubZonaDTO> {
    return this.http.get<SubZonaDTO>(`${this.base}/sub-zonas/${id}`);
  }

  /** 🔹 Lista las subzonas pertenecientes a una zona */
  listarSubZonasPorZona(idZona: number): Observable<SubZonaDTO[]> {
    return this.http.get<SubZonaDTO[]>(`${this.base}/zonas/${idZona}/sub-zonas`);
  }
}
