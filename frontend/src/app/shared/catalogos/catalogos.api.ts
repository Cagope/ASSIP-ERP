import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { catchError, of, map } from 'rxjs';

export interface CodigoNombreDTO {
  codigo: string | number;
  nombre: string;
}

export interface Departamento {
  idDepartamento: number;
  nombreDepartamento: string;
  codigoDepartamento?: string;
}

export interface Ciudad {
  idCiudad: number;
  nombreCiudad: string;
  idDepartamento: number;
}

export interface CatalogoIdCodigoNombre {
  id: number;
  codigo?: string | null;
  nombre: string;
  activo?: boolean;
}

/**
 * 📚 Servicio unificado de catálogos ASSIP-ERP
 * Usa los endpoints del backend /api/v1/catalogos/*
 */
@Injectable({ providedIn: 'root' })
export class CatalogosApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/catalogos`;

  // ====================================================
  // 🔹 Catálogos básicos (país, depto, ciudad, documento)
  // ====================================================

  listarTiposDocumentos() {
    return this.http.get<any[]>(`${this.baseUrl}/tipos-documentos`).pipe(
      map(items => items.map(d => ({
        codigo: d.codigo,
        nombre: d.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarPaises() {
    return this.http.get<any[]>(`${this.baseUrl}/paises`).pipe(
      map(items => items.map(p => ({
        codigo: p.idpais ?? p.id_pais,
        nombre: p.nombrepais ?? p.nombre_pais
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarDepartamentos() {
    return this.http.get<any[]>(`${this.baseUrl}/departamentos`).pipe(
      map(items => items.map(d => ({
        idDepartamento: d.iddepartamento ?? d.id_departamento,
        nombreDepartamento: d.nombredepartamento ?? d.nombre_departamento,
        codigoDepartamento: d.codigodepartamento ?? d.codigo_departamento
      }))),
      catchError(() => of([] as Departamento[]))
    );
  }

  listarCiudadesPorDepartamento(idDepartamento: number) {
    return this.http
      .get<any[]>(`${this.baseUrl}/departamentos/${idDepartamento}/ciudades`)
      .pipe(
        map(items => items.map(c => ({
          idCiudad: c.idciudad ?? c.id_ciudad,
          nombreCiudad: c.nombreciudad ?? c.nombre_ciudad,
          idDepartamento: c.iddepartamento ?? c.id_departamento
        }))),
        catchError(() => of([] as Ciudad[]))
      );
  }

  listarTodasLasCiudades() {
    return this.http.get<any[]>(`${this.baseUrl}/ciudades`).pipe(
      map(items => items.map(c => ({
        idCiudad: c.idciudad ?? c.id_ciudad,
        nombreCiudad: c.nombreciudad ?? c.nombre_ciudad,
        idDepartamento: c.iddepartamento ?? c.id_departamento
      }))),
      catchError(() => of([] as Ciudad[]))
    );
  }

  // ====================================================
  // 🔹 Catálogos personales
  // ====================================================

  listarGeneros() {
    return this.http.get<any[]>(`${this.baseUrl}/generos`).pipe(
      map(items => items.map(g => ({
        codigo: g.codigo_genero ?? g.codigo,
        nombre: g.nombre_genero ?? g.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarEstadosCiviles() {
    return this.http.get<any[]>(`${this.baseUrl}/estados-civiles`).pipe(
      map(items => items.map(e => ({
        codigo: e.codigo_estado_civil ?? e.codigo,
        nombre: e.nombre_estado_civil ?? e.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarNivelesEscolares() {
    return this.http.get<any[]>(`${this.baseUrl}/niveles-escolares`).pipe(
      map(items => items.map(n => ({
        codigo: n.codigo_escolaridad ?? n.codigo,
        nombre: n.nombre_escolaridad ?? n.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarOcupaciones() {
    return this.http.get<any[]>(`${this.baseUrl}/ocupaciones`).pipe(
      map(items => items.map(o => ({
        codigo: o.codigo_ocupacion ?? o.codigo,
        nombre: o.nombre_ocupacion ?? o.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  // ====================================================
  // 🔹 Catálogos económicos y empresariales
  // ====================================================

  listarSectoresEconomicos() {
    return this.http.get<any[]>(`${this.baseUrl}/sectores-economicos`).pipe(
      map(items => items.map(s => ({
        codigo: s.codigo_sector_economico ?? s.codigo,
        nombre: s.nombre_sector_economico ?? s.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarActividadesSes() {
    return this.http.get<any[]>(`${this.baseUrl}/actividades-economicas/ses`).pipe(
      map(items => items.map(a => ({
        codigo: a.codigo_actividad_ses ?? a.codigo,
        nombre: a.nombre_actividad_ses ?? a.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarActividadesDian() {
    return this.http.get<any[]>(`${this.baseUrl}/actividades-economicas/dian`).pipe(
      map(items => items.map(a => ({
        codigo: a.codigo_actividad_dian ?? a.codigo,
        nombre: a.nombre_actividad_dian ?? a.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarTiposViviendas() {
    return this.http.get<any[]>(`${this.baseUrl}/tipos-viviendas`).pipe(
      map(items => items.map(t => ({
        codigo: t.codigo_tipo_vivienda ?? t.codigo,
        nombre: t.nombre_tipo_vivienda ?? t.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  // ====================================================
  // 🔹 Otros (SARLAFT, etc.)
  // ====================================================

  listarTiposPeps() {
    return this.http.get<any[]>(`${this.baseUrl}/tipos-peps`).pipe(
      map(items => items.map(p => ({
        codigo: p.tipo_peps ?? p.codigo,
        nombre: p.nombre_tipo_peps ?? p.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarParentescos() {
    return this.http.get<any[]>(`${this.baseUrl}/parentescos`).pipe(
      map(items => items.map(p => ({
        codigo: p.codigo_parentesco ?? p.codigo,
        nombre: p.nombre_parentesco ?? p.nombre
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarTiposEmpresas() {
    return this.http.get<any[]>(`${this.baseUrl}/tipos-empresas`).pipe(
      map(items => items.map(t => ({
        codigo: t.codigo_tipo_empresa ?? t.codigo ?? t.id_tipo_empresa,
        nombre: t.nombre_tipo_empresa ?? t.nombre ?? t.descripcion
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarTiposContratos() {
    return this.http.get<any[]>(`${this.baseUrl}/tipos-contratos`).pipe(
      map(items => items.map(t => ({
        codigo: t.codigo_tipo_contrato ?? t.codigo ?? t.id_tipo_contrato,
        nombre: t.nombre_tipo_contrato ?? t.nombre ?? t.descripcion
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarJornadasLaborales() {
    return this.http.get<any[]>(`${this.baseUrl}/jornadas-laborales`).pipe(
      map(items => items.map(t => ({
        codigo: t.codigo_jornada ?? t.codigo ?? t.id_jornada,
        nombre: t.nombre_jornada ?? t.nombre ?? t.descripcion
      }))),
      catchError(() => of([] as CodigoNombreDTO[]))
    );
  }

  listarTiposBienesHojaVida() {
    return this.http.get<any[]>(`${this.baseUrl}/hoja-vida/tipos-bienes`).pipe(
      map(items => items.map(t => ({
        id: t.id,
        codigo: t.codigo,
        nombre: t.nombre,
        activo: t.activo
      }))),
      catchError(() => of([] as CatalogoIdCodigoNombre[]))
    );
  }

  listarTiposInmueblesHojaVida() {
    return this.http.get<any[]>(`${this.baseUrl}/hoja-vida/tipos-inmuebles`).pipe(
      map(items => items.map(t => ({
        id: t.id,
        codigo: t.codigo,
        nombre: t.nombre,
        activo: t.activo
      }))),
      catchError(() => of([] as CatalogoIdCodigoNombre[]))
    );
  }

  listarTiposGravamenesHojaVida() {
    return this.http.get<any[]>(`${this.baseUrl}/hoja-vida/tipos-gravamenes`).pipe(
      map(items => items.map(t => ({
        id: t.id,
        codigo: t.codigo,
        nombre: t.nombre,
        activo: t.activo
      }))),
      catchError(() => of([] as CatalogoIdCodigoNombre[]))
    );
  }

  listarTiposVehiculosHojaVida() {
    return this.http.get<any>(`${this.baseUrl}/hoja-vida/tipos-vehiculos`).pipe(
      map(resp => {
        const items = Array.isArray(resp)
          ? resp
          : (resp?.value ?? []);

        return items.map((t: any) => ({
          id: t.id,
          codigo: t.codigo,
          nombre: t.nombre,
          activo: t.activo
        }));
      }),
      catchError(() => of([] as CatalogoIdCodigoNombre[]))
    );
  }

  listarTiposMaquinariaHojaVida() {
    return this.http.get<any[]>(`${this.baseUrl}/hoja-vida/tipos-maquinaria`).pipe(
      map(items => items.map(t => ({
        id: t.id,
        codigo: t.codigo,
        nombre: t.nombre,
        activo: t.activo
      }))),
      catchError(() => of([] as CatalogoIdCodigoNombre[]))
    );
  }

  listarTiposInversionesHojaVida() {
    return this.http
      .get<any[]>(`${this.baseUrl}/hoja-vida/tipos-inversiones`)
      .pipe(
        map(items => items.map(t => ({
          id: t.id,
          codigo: t.codigo,
          nombre: t.nombre,
          activo: t.activo
        }))),
        catchError(() =>
          of([] as CatalogoIdCodigoNombre[])
        )
      );
  }

  listarTiposZonasInmueblesHojaVida() {
    return this.http.get<CatalogoIdCodigoNombre[]>(
      `${this.baseUrl}/hoja-vida/tipos-zonas-inmuebles`
    );
  }

}
