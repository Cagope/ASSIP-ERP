import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CapturaApi {
  private http = inject(HttpClient);

  // ===== BASES =====
  private baseCapturaFinal = `${environment.apiBase}/hoja-vida/captura-finalizar`;

  // ===== CAPTURA FINAL (cierre general) =====
  finalizar(payload: any): Observable<{ idDatosPersonal: number }> {
    return this.http.post<{ idDatosPersonal: number }>(`${this.baseCapturaFinal}`, payload);
  }

  // ===== UPSERTS POR MÓDULO (nested bajo /datos-personales/{id}) =====

  /** Ubicación (1:1): POST /hoja-vida/datos-personales/{idDatosPersonal}/ubicacion */
  upsertUbicacion(idDatosPersonal: number, dto: any): Observable<any> {
    return this.http.post<any>(
      `${environment.apiBase}/hoja-vida/datos-personales/${idDatosPersonal}/ubicacion`,
      dto
    );
  }

  /** Laboral (1:1): POST /hoja-vida/datos-personales/{idDatosPersonal}/laboral */
  upsertLaboral(idDatosPersonal: number, dto: any): Observable<any> {
    return this.http.post<any>(
      `${environment.apiBase}/hoja-vida/datos-personales/${idDatosPersonal}/laboral`,
      dto
    );
  }

  /** Financiero (1:1): POST /hoja-vida/datos-personales/{idDatosPersonal}/financieros */
  upsertFinanciero(idDatosPersonal: number, dto: any): Observable<any> {
    return this.http.post<any>(
      `${environment.apiBase}/hoja-vida/datos-personales/${idDatosPersonal}/financieros`,
      dto
    );
  }

  /** Datos familiares (lista o único): POST /hoja-vida/datos-personales/{idDatosPersonal}/datos-familiares */
  upsertDatosFamiliares(idDatosPersonal: number, dto: any): Observable<any> {
    return this.http.post<any>(
      `${environment.apiBase}/hoja-vida/datos-personales/${idDatosPersonal}/datos-familiares`,
      dto
    );
  }

  /** Referencia personal (1:1): POST /hoja-vida/datos-personales/{idDatosPersonal}/referencias-personales */
  upsertReferenciaPersonal(idDatosPersonal: number, dto: any): Observable<any> {
    return this.http.post<any>(
      `${environment.apiBase}/hoja-vida/datos-personales/${idDatosPersonal}/referencias-personales`,
      dto
    );
  }

  /** SARLAFT (1:1): POST /hoja-vida/datos-personales/{idDatosPersonal}/sarlaft */
  upsertSarlaft(idDatosPersonal: number, dto: any): Observable<any> {
    return this.http.post<any>(
      `${environment.apiBase}/hoja-vida/datos-personales/${idDatosPersonal}/sarlaft`,
      dto
    );
  }

  /** Permisos especiales (1:1): POST /hoja-vida/datos-personales/{idDatosPersonal}/permisos-especiales */
  upsertPermisos(idDatosPersonal: number, dto: any): Observable<any> {
    return this.http.post<any>(
      `${environment.apiBase}/hoja-vida/datos-personales/${idDatosPersonal}/permisos-especiales`,
      dto
    );
  }
}
