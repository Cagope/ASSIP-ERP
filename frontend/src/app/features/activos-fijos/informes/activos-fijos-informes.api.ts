import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { SessionService } from '../../../core/auth/session.service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ActivosFijosInformesApi {

  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);

  private readonly baseUrl = `${environment.apiUrl}/activos-fijos/informes`;

  // ============================================================
  // 🔐 Headers con token JWT
  // ============================================================
  private getHeaders(): HttpHeaders {
    const token = this.session.getToken?.() || (this.session as any).token;
    return new HttpHeaders({
      Authorization: `Bearer ${token}`
    });
  }

  // ============================================================
  // ✅ 0) Catálogo: Tipos de movimientos de activos
  // GET /activos-fijos/informes/activos-movimientos
  // ============================================================
  activosMovimientos(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/activos-movimientos`,
      { headers: this.getHeaders() }
    );
  }

  // ============================================================
  // ✅ 1) Maestro Activos
  // GET /activos-fijos/informes/maestro-activos
  // ============================================================
  maestroActivos(): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.baseUrl}/maestro-activos`,
      { headers: this.getHeaders() }
    );
  }

  // ============================================================
  // ✅ 2) Movimientos / Kardex
  // GET /activos-fijos/informes/movimientos-activos
  // filtros opcionales:
  // - idAgencia
  // - codigoMovimiento
  // - fechaIni (yyyy-MM-dd)
  // - fechaFin (yyyy-MM-dd)
  // - idActivoFijo
  // ============================================================
  movimientosActivos(filtros?: {
    idAgencia?: number | null;
    codigoMovimiento?: string | null;
    fechaIni?: string | null;
    fechaFin?: string | null;
    idActivoFijo?: number | null;
  }): Observable<any[]> {

    let params = new HttpParams();

    if (filtros?.idAgencia != null) {
      params = params.set('idAgencia', filtros.idAgencia);
    }
    if (filtros?.codigoMovimiento) {
      params = params.set('codigoMovimiento', filtros.codigoMovimiento);
    }
    if (filtros?.fechaIni) {
      params = params.set('fechaIni', filtros.fechaIni);
    }
    if (filtros?.fechaFin) {
      params = params.set('fechaFin', filtros.fechaFin);
    }
    if (filtros?.idActivoFijo != null) {
      params = params.set('idActivoFijo', filtros.idActivoFijo);
    }

    return this.http.get<any[]>(
      `${this.baseUrl}/movimientos-activos`,
      { headers: this.getHeaders(), params }
    );
  }

  // ============================================================
  // ✅ 3) Resumen Movimientos (mensual)
  // GET /activos-fijos/informes/resumen-movimientos
  // filtros opcionales:
  // - idAgencia
  // - codigoMovimiento
  // ============================================================
  resumenMovimientos(filtros?: {
    idAgencia?: number | null;
    codigoMovimiento?: string | null;
  }): Observable<any[]> {

    let params = new HttpParams();

    if (filtros?.idAgencia != null) {
      params = params.set('idAgencia', filtros.idAgencia);
    }
    if (filtros?.codigoMovimiento) {
      params = params.set('codigoMovimiento', filtros.codigoMovimiento);
    }

    return this.http.get<any[]>(
      `${this.baseUrl}/resumen-movimientos`,
      { headers: this.getHeaders(), params }
    );
  }
}
