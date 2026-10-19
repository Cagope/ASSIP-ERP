import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import {
  TransferenciaCuenta, TransferenciaEntrada, TransferenciaFiltro,
  TransferenciaPreview, TransferenciaResultado
} from './transferencia-cuentas.models';

@Injectable({providedIn: 'root'})
export class TransferenciaCuentasApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/depositos/transferencias`;

  buscarCuentas(f: TransferenciaFiltro) {
    let params = new HttpParams();
    Object.entries(f).forEach(([clave, valor]) => {
      if (valor.trim()) params = params.set(clave, valor.trim());
    });
    return this.http.get<TransferenciaCuenta[]>(`${this.base}/buscar-cuentas`, {params});
  }
  consultarCuenta(id: number) {
    return this.http.get<TransferenciaCuenta>(`${this.base}/cuenta/${id}`);
  }
  preview(entrada: TransferenciaEntrada) {
    return this.http.post<TransferenciaPreview>(`${this.base}/preview`, entrada);
  }
  aplicar(entrada: TransferenciaEntrada) {
    return this.http.post<TransferenciaResultado>(`${this.base}/aplicar`, entrada);
  }
}
