import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CuentasAhorroApi {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/depositos/cuentas-ahorro`;

  // ============================================================
  // 🟦 LISTAR CUENTAS SEGÚN LAS AGENCIAS DEL USUARIO  ⭐ NUEVO
  // ============================================================
  listarTodas() {
    return this.http.get(`${this.base}`);
  }

  // ============================================================
  // 🟦 LISTAR POR AGENCIA ESPECÍFICA  ⭐ NUEVO
  // ============================================================
  listarPorAgencia(idAgencia: number) {
    return this.http.get(`${this.base}/agencia/${idAgencia}`);
  }

  // ============================================================
  // 🔹 Buscar cuentas usando reporting (vista oficial)
  // ============================================================
  buscarCuentas(req: any) {
    return this.http.post(`${environment.apiUrl}/reporting/query`, req);
  }

  // ============================================================
  // 🔹 Validar reglas antes de crear
  // ============================================================
  validar(data: any) {
    return this.http.post(`${this.base}/validar`, data);
  }

  // ============================================================
  // 🔹 Crear cuenta
  // ============================================================
  crear(data: any) {
    return this.http.post(`${this.base}`, data);
  }

  // ============================================================
  // 🔹 Traer detalle después de crear
  // ============================================================
  detalle(idCuenta: number) {
    return this.http.get(`${this.base}/${idCuenta}`);
  }
}
