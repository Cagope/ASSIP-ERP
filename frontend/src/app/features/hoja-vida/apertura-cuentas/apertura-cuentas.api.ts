import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SessionService } from '../../../core/auth/session.service';

@Injectable({ providedIn: 'root' })
export class AperturaCuentasApi {

  private readonly base = `${environment.apiUrl}/hoja-vida/apertura-cuentas`;

  constructor(
    private http: HttpClient,
    private session: SessionService
  ) {}

  /** 🔹 Header con agencias reales del usuario */
  private buildHeaders(): HttpHeaders {
    const agencias = this.session.getAgencias()
      ?.map(a => a.idAgencia)
      .join(',') ?? '';

    return new HttpHeaders({
      'X-Agencias': agencias
    });
  }

  /** 🔹 Listar formas habilitadas (reglas ya aplicadas en backend) */
  listarFormas(idDatosPersonal: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.base}/formas/${idDatosPersonal}`,
      { headers: this.buildHeaders() }
    );
  }

  /** 🔹 Obtener próximo código sugerido para aportes */
  obtenerSiguienteCodigo(idForma: number): Observable<string> {
    return this.http.get(
      `${this.base}/consecutivo/${idForma}`,
      { responseType: 'text', headers: this.buildHeaders() }
    );
  }

  /** 🔹 Listar formas de ahorro opcionales (02,03,05,06) */
  listarFormasOpcionales(): Observable<any[]> {

    const agenciaActiva = this.session.getAgenciaActiva();
    const idAgencia = agenciaActiva?.idAgencia ?? 0;

    return this.http.get<any[]>(
      `${this.base}/formas-ahorro/${idAgencia}`,
      { headers: this.buildHeaders() }
    );
  }

  /** 🔹 Crear cuenta (envía agencia del usuario y aplica reglas) */
  crear(data: any): Observable<any> {
    return this.http.post<any>(
      `${this.base}`,
      data,
      { headers: this.buildHeaders() }
    );
  }
}
