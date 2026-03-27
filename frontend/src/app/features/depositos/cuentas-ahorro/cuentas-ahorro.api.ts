import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CuentasAhorroApi {

  private readonly http = inject(HttpClient);

  // 👉 endpoint shared nuevo
  private readonly baseShared =
    `${environment.apiUrl}/shared/cuentas_ahorro`;

  // 👉 endpoint depósitos (lo dejamos intacto)
  private readonly baseDepositos =
    `${environment.apiUrl}/depositos/cuentas-ahorro`;

  // ============================================================
  // 🟩 NUEVO → CUENTAS OPERATIVAS POR PERSONA
  // ============================================================
  listarPorPersona(idDatosPersonal: number) {
    return this.http.get(
      `${this.baseShared}/${idDatosPersonal}`
    );
  }

  // ============================================================
  // 🟦 LISTAR CUENTAS (depósitos)
  // ============================================================
  listarTodas() {
    return this.http.get(`${this.baseDepositos}`);
  }

  listarPorAgencia(idAgencia: number) {
    return this.http.get(
      `${this.baseDepositos}/agencia/${idAgencia}`
    );
  }

  // ============================================================
  // 🔹 REPORTING
  // ============================================================
  buscarCuentas(req: any) {
    return this.http.post(
      `${environment.apiUrl}/reporting/query`,
      req
    );
  }

  // ============================================================
  // 🔹 CRUD depósitos
  // ============================================================
  validar(data: any) {
    return this.http.post(`${this.baseDepositos}/validar`, data);
  }

  crear(data: any) {
    return this.http.post(`${this.baseDepositos}`, data);
  }

  detalle(idCuenta: number) {
    return this.http.get(
      `${this.baseDepositos}/${idCuenta}`
    );
  }
}
