import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SessionService } from '../../core/auth/session.service';

export interface CuentaAutocompleteDTO {
  idCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;
}

@Injectable({ providedIn: 'root' })
export class CuentasApi {

  private readonly base = `${environment.apiUrl}/shared/cuentas`;

  private http = inject(HttpClient);
  private session = inject(SessionService);

  buscar(idAgencia: number, texto: string): Observable<CuentaAutocompleteDTO[]> {

    const params = new HttpParams().set('q', texto);

    const headers = new HttpHeaders()
      .set('X-Agencias', String(idAgencia));

    return this.http.get<CuentaAutocompleteDTO[]>(
      `${this.base}/buscar`,
      { params, headers }
    );
  }

  obtenerPorId(idCuenta: number): Observable<CuentaAutocompleteDTO> {

    // Agencia activa desde sesión (mismo patrón del proyecto)
    const ag = this.session.getAgenciaActiva();
    const idAgencia = ag?.idAgencia;

    let headers = new HttpHeaders();
    if (idAgencia) {
      headers = headers.set('X-Agencias', String(idAgencia));
    }

    return this.http.get<CuentaAutocompleteDTO>(
      `${this.base}/${idCuenta}`,
      { headers }
    );
  }
}
