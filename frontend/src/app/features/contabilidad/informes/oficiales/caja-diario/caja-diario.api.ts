import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';
import { CajaDiarioFiltro, CajaDiarioFormato, CajaDiarioResultado } from './caja-diario.models';

/** Centraliza consulta y exportación del libro Caja Diario. */
@Injectable({ providedIn: 'root' })
export class CajaDiarioApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/contabilidad/informes/oficiales/caja-diario`;
  consultar(filtro: CajaDiarioFiltro): Observable<CajaDiarioResultado> {
    return this.http.post<CajaDiarioResultado>(`${this.url}/consultar`, filtro);
  }
  pdf(filtro: CajaDiarioFiltro, formato: CajaDiarioFormato): Observable<Blob> {
    return this.http.post(`${this.url}/pdf`, { filtro, formato }, { responseType: 'blob' });
  }
  excel(filtro: CajaDiarioFiltro): Observable<Blob> {
    return this.http.post(`${this.url}/excel`, filtro, { responseType: 'blob' });
  }
}
