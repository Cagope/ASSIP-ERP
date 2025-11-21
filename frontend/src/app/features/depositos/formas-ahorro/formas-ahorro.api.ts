import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';

export interface FormaAhorro {
  id?: number;
  codigoForma: string;
  nombreForma: string;
  consecutivoForma?: number | null;
  tipoCaptacion?: string | null;
  tiempoLiquidacion?: number | null;
  cuentaFormaCorto?: number | null;
  cuentaFormaLargo?: number | null;
  cuentaGasto?: number | null;
  cuentaCxpForma?: number | null;
  cuentaGmfForma?: number | null;
  tipoInteresForma?: number | null;
  fechaUltimaLiquidacion?: string | null;
  autorizadoForma?: boolean;
  documentoForma?: string | null;
  periodoGracia?: number | null;
  valorMinimo?: number | null;
  tasaInteresForma?: number | null;
}

@Injectable({ providedIn: 'root' })
export class FormasAhorroApi {

  private readonly base = `${environment.apiUrl}/api/v1/depositos/formas-ahorro`;

  constructor(private http: HttpClient) {}

  listar(): Observable<FormaAhorro[]> {
    return this.http.get<FormaAhorro[]>(this.base);
  }

  crear(data: FormaAhorro): Observable<FormaAhorro> {
    return this.http.post<FormaAhorro>(this.base, data);
  }

  obtener(id: number): Observable<FormaAhorro> {
    return this.http.get<FormaAhorro>(`${this.base}/${id}`);
  }

  actualizar(id: number, data: FormaAhorro): Observable<FormaAhorro> {
    return this.http.put<FormaAhorro>(`${this.base}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
