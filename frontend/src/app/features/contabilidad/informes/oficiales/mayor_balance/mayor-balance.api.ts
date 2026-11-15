import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import { MayorBalanceFiltro, MayorBalanceFormato, MayorBalanceResultado } from './mayor-balance.models';

/** Todas las solicitudes HTTP de Mayor y Balance. */
@Injectable({ providedIn: 'root' })
export class MayorBalanceApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/informes/oficiales/mayor-balance`;
  consultar(filtro: MayorBalanceFiltro): Observable<MayorBalanceResultado> {
    return this.http.post<MayorBalanceResultado>(`${this.url}/consultar`, filtro);
  }
  pdf(filtro: MayorBalanceFiltro, formato: MayorBalanceFormato): Observable<Blob> {
    return this.http.post(`${this.url}/pdf`, { filtro, formato }, { responseType: 'blob' });
  }
  excel(filtro: MayorBalanceFiltro): Observable<Blob> {
    return this.http.post(`${this.url}/excel`, filtro, { responseType: 'blob' });
  }
}
