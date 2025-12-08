import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DepositosCatalogosApi {

  private readonly http = inject(HttpClient);

  // 🔥 RUTA REAL DEL BACKEND
  private readonly base = `${environment.apiUrl}/shared`;

  // ============================================================
  // 1. ESTADOS DE AHORRO
  // ============================================================
  obtenerEstadosAhorro() {
    return this.http.get<any[]>(`${this.base}/estado-ahorro/operativos`);
  }

  // ============================================================
  // 2. TIPOS GMF
  // ============================================================
  obtenerTiposGmf() {
    return this.http.get<any[]>(`${this.base}/tipo-gmf`);
  }
}
