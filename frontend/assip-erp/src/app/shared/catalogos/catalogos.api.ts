// src/app/shared/catalogos/catalogos.api.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs/operators';
import { from, concatMap, catchError, of, filter, take, defaultIfEmpty } from 'rxjs';

export type IdNombreDTO = { id: number; nombre: string };
export type CodigoNombreDTO = { codigo: string; nombre: string };
export type TipoRegimenDTO = {
  codigo: string;
  nombre: string;
  porcentajeRetencion: number | null;
  baseRetencion: boolean;
};

// === Requeridos por DeptoCiudadComponent ===
export type DepartamentoDTO = { id: number; nombre: string };
export type CiudadDTO = { id: number; nombre: string; idDepartamento: number };

@Injectable({ providedIn: 'root' })
export class CatalogosApi {
  private http = inject(HttpClient);
  private base = environment.apiBase;

  // ---------------------------
  // Helper: fallbacks de rutas
  // ---------------------------
  /** Intenta varias rutas en orden y devuelve la PRIMERA que responda OK. */
  private getWithFallbacks<T>(paths: string[], params?: HttpParams) {
    return from(paths).pipe(
      concatMap((p) =>
        this.http.get<T>(`${this.base}${p}`, { params }).pipe(
          // si falla esta ruta, probamos la siguiente
          catchError(() => of(undefined as unknown as T))
        )
      ),
      // solo valores válidos
      filter((v: unknown): v is T => v !== undefined && v !== null),
      take(1),
    );
  }

  // --- Países / Géneros / Estados / etc. (ya existentes)
  paises()               { return this.http.get<IdNombreDTO[]>(`${this.base}/catalogos/paises`); }
  generos()              { return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/generos`); }
  estadosCiviles()       { return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/estados-civiles`); }
  nivelesEscolares()     { return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/niveles-escolares`); }
  ocupaciones()          { return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/ocupaciones`); }
  sectoresEconomicos()   { return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/sectores-economicos`); }
  tiposVivienda()        { return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/tipos-vivienda`); }
  tiposRegimen()         { return this.http.get<TipoRegimenDTO[]>(`${this.base}/catalogos/tipos-regimen`); }
  tiposDocumentos()      { return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/tipos-documentos`); }

  // --- NUEVO: Tipos de PEP (¡sin filtrar, preserva "000", "001", etc.!)
  tiposPeps() {
    type Raw = { tipo_peps: string; nombre_tipo_peps: string };
    return this.http.get<Raw[]>(`${this.base}/catalogos/tipos-peps`).pipe(
      map((rows) => (Array.isArray(rows) ? rows : []).map((r) => ({
        // OJO: no convertimos a número; preservamos ceros a la izquierda
        codigo: (r?.tipo_peps ?? '').toString(),
        nombre: (r?.nombre_tipo_peps ?? '').toString(),
      })))
    );
  }

  // --- NUEVO: Parentescos
  parentescos(q?: string) {
    let params = new HttpParams();
    if (q) params = params.set('q', q);
    return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/parentescos`, { params });
  }

  // --- Departamentos / Ciudades (usados por DeptoCiudadComponent)
  departamentos() {
    return this.http.get<DepartamentoDTO[]>(`${this.base}/catalogos/departamentos`);
  }

  ciudadesPorDepartamento(idDepartamento: number) {
    const params = new HttpParams().set('departamentoId', String(idDepartamento));
    return this.http.get<CiudadDTO[]>(`${this.base}/catalogos/ciudades`, { params });
  }

  // --- Búsqueda actividades SES/DIAN
  actividadesBuscar(fuente: 'SES' | 'DIAN', q: string, limit = 20) {
    const params = new HttpParams()
      .set('fuente', fuente)
      .set('q', q)
      .set('limit', String(limit));
    return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/actividades/buscar`, { params });
  }

  // ============================
  // NUEVOS: Catálogos Laborales
  // ============================

  // Tipos de Empresa - intenta /combo y variantes, pero siempre entrega []
  tiposEmpresas(q?: string) {
    let params = new HttpParams();
    if (q) params = params.set('q', q);
    const paths = [
      '/catalogos/tipos-empresas/combo',
      '/catalogos/tipos-empresas',
      '/catalogos/tipos-empresa',
    ];
    return this.getWithFallbacks<CodigoNombreDTO[]>(paths, params)
      .pipe(defaultIfEmpty([] as CodigoNombreDTO[]));
  }

  // Tipos de Contrato - intenta /combo y variantes, pero siempre entrega []
  tiposContratos(q?: string) {
    let params = new HttpParams();
    if (q) params = params.set('q', q);
    const paths = [
      '/catalogos/tipos-contratos/combo',
      '/catalogos/tipos-contratos',
      '/catalogos/tipos-contrato',
    ];
    return this.getWithFallbacks<CodigoNombreDTO[]>(paths, params)
      .pipe(defaultIfEmpty([] as CodigoNombreDTO[]));
  }

  // Jornadas Laborales - intenta /combo y variantes, pero siempre entrega []
  jornadasLaborales(q?: string) {
    let params = new HttpParams();
    if (q) params = params.set('q', q);
    const paths = [
      '/catalogos/jornadas-laborales/combo',
      '/catalogos/jornadas-laborales',
      '/catalogos/jornadas',
    ];
    return this.getWithFallbacks<CodigoNombreDTO[]>(paths, params)
      .pipe(defaultIfEmpty([] as CodigoNombreDTO[]));
  }

  tiposDirectivos() {
    return this.http.get<CodigoNombreDTO[]>(`${this.base}/catalogos/tipos-directivos`);
  }

}
