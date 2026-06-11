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

 listarFormasAhorro() {
    return this.http.get(
      `${environment.apiUrl}/depositos/formas-ahorro`
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

  // ============================================================
  // 🔹 CUENTAS CONJUNTAS
  // ============================================================
  listarCuentasConjuntas(idCuenta: number) {
    return this.http.get(
      `${this.baseDepositos}/${idCuenta}/cuentas-conjuntas`
    );
  }

  agregarCuentaConjunta(idCuenta: number, data: any) {
    return this.http.post(
      `${this.baseDepositos}/${idCuenta}/cuentas-conjuntas`,
      data
    );
  }

  eliminarCuentaConjunta(idCuenta: number, idCuentaConjunta: number) {
    return this.http.delete(
      `${this.baseDepositos}/${idCuenta}/cuentas-conjuntas/${idCuentaConjunta}`
    );
  }

    // ============================================================
    // 🔹 BENEFICIARIOS
    // ============================================================
    listarBeneficiarios(idCuenta: number) {
      return this.http.get(
        `${this.baseDepositos}/${idCuenta}/beneficiarios`
      );
    }

    agregarBeneficiario(idCuenta: number, data: any) {
      return this.http.post(
        `${this.baseDepositos}/${idCuenta}/beneficiarios`,
        data
      );
    }

    actualizarBeneficiario(
      idCuenta: number,
      idBeneficiario: number,
      data: any
    ) {
      return this.http.put(
        `${this.baseDepositos}/${idCuenta}/beneficiarios/${idBeneficiario}`,
        data
      );
    }

    eliminarBeneficiario(idCuenta: number, idBeneficiario: number) {
      return this.http.delete(
        `${this.baseDepositos}/${idCuenta}/beneficiarios/${idBeneficiario}`
      );
    }

    // ============================================================
    // 🔹 PODERES
    // ============================================================
    listarPoderes(idCuenta: number) {
      return this.http.get(
        `${this.baseDepositos}/${idCuenta}/poderes`
      );
    }

    agregarPoder(idCuenta: number, data: any) {
      return this.http.post(
        `${this.baseDepositos}/${idCuenta}/poderes`,
        data
      );
    }

    actualizarPoder(
      idCuenta: number,
      idPoder: number,
      data: any
    ) {
      return this.http.put(
        `${this.baseDepositos}/${idCuenta}/poderes/${idPoder}`,
        data
      );
    }

    eliminarPoder(idCuenta: number, idPoder: number) {
      return this.http.delete(
        `${this.baseDepositos}/${idCuenta}/poderes/${idPoder}`
      );
    }

}
