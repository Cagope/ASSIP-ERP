import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { InformeReversion, InformeReversionFiltro, InformeReversionAsociado } from './informe-reversion.models';

/** Consulta exclusivamente el informe de reversiones, sin ejecutar operaciones financieras. */
@Injectable({ providedIn: 'root' })
export class InformeReversionService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/cartera/informes/reversiones-pagos`;

  /** Envía los filtros con los nombres establecidos por el controlador Java. */
  consultar(filtro: InformeReversionFiltro): Observable<InformeReversion[]> {
    let params = new HttpParams()
      .set('fechaInicial', filtro.fechaInicial)
      .set('fechaFinal', filtro.fechaFinal);
    if (filtro.idAgencia != null) params = params.set('idAgencia', String(filtro.idAgencia));
    if (filtro.asociado?.trim()) params = params.set('asociado', filtro.asociado.trim());
    if (filtro.pagare?.trim()) params = params.set('pagare', filtro.pagare.trim());
    if (filtro.funcionario?.trim()) params = params.set('funcionario', filtro.funcionario.trim());
    return this.http.get<InformeReversion[]>(this.url, { params });
  }
  /** Recupera sugerencias con el mismo permiso y alcance del informe. */
  buscarAsociados(texto: string): Observable<InformeReversionAsociado[]> {
    return this.http.get<InformeReversionAsociado[]>(`${this.url}/asociados`, {
      params: new HttpParams().set('texto', texto.trim())
    });
  }
}

