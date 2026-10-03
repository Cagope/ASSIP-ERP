import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { environment } from '../../../../../environments/environment';

export interface ConsultaCdatsFiltros {
  documento?: string;
  nombres?: string;
  primerApellido?: string;
  segundoApellido?: string;
  codigoCdat?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConsultaCdatsService {

  private readonly base =
    `${environment.apiUrl}/cdat/informes/consulta-cdats`;

  constructor(
    private readonly http: HttpClient
  ) {}

  consultar(
    filtros: ConsultaCdatsFiltros
  ): Promise<any[]> {

    let params = new HttpParams();

    if (filtros.documento?.trim()) {
      params = params.set(
        'documento',
        filtros.documento.trim()
      );
    }

    if (filtros.nombres?.trim()) {
      params = params.set(
        'nombres',
        filtros.nombres.trim()
      );
    }

    if (filtros.primerApellido?.trim()) {
      params = params.set(
        'primerApellido',
        filtros.primerApellido.trim()
      );
    }

    if (filtros.segundoApellido?.trim()) {
      params = params.set(
        'segundoApellido',
        filtros.segundoApellido.trim()
      );
    }

    if (filtros.codigoCdat?.trim()) {
      params = params.set(
        'codigoCdat',
        filtros.codigoCdat.trim()
      );
    }

    return firstValueFrom(
      this.http.get<any[]>(
        this.base,
        { params }
      )
    );
  }

  consultarExtracto(
    codigoCdat: string,
    fechaInicial: string,
    fechaFinal: string
  ): Promise<any[]> {

    const params = new HttpParams()
      .set('fechaInicial', fechaInicial)
      .set('fechaFinal', fechaFinal);

    return firstValueFrom(
      this.http.get<any[]>(
        `${this.base}/${encodeURIComponent(codigoCdat)}/extracto`,
        { params }
      )
    );
  }
}
