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

  // ============================================================
  // 🔹 LISTAR FORMAS HABILITADAS
  //    * El backend NO acepta idAgencia en la URL
  //    * La agencia se toma del JWT en el backend
  // ============================================================
  listarFormas(idDatosPersonal: number, idAgencia: number): Observable<any[]> {

    console.log("🔥 API → Enviando persona/agencia =", idDatosPersonal, idAgencia);

    if (!idAgencia || isNaN(idAgencia)) {
      console.warn("⚠️ API: idAgencia inválida, devolviendo arreglo vacío.");
      return new Observable<any[]>(observer => {
        observer.next([]);
        observer.complete();
      });
    }

    // ⛔ ANTES: /formas/{idDatosPersonal}/{idAgencia}
    // ⛔ Ese endpoint NO existe → 404
    //
    // ✅ AHORA: /formas/{idDatosPersonal}
    //    Coincide con tu @GetMapping("/formas/{idPersona}")
    return this.http.get<any[]>(
      `${this.base}/formas/${idDatosPersonal}`,
      { headers: this.buildHeaders() }
    );
  }

  // ============================================================
  // 🔹 OBTENER SIGUIENTE CÓDIGO
  // ============================================================
  obtenerSiguienteCodigo(idForma: number): Observable<string> {
    return this.http.get(
      `${this.base}/consecutivo/${idForma}`,
      { responseType: 'text', headers: this.buildHeaders() }
    );
  }

  // ============================================================
  // 🔹 FORMAS OPCIONALES (SI SE USAN)
  // ============================================================
  listarFormasOpcionales(): Observable<any[]> {

    const agenciaActiva = this.session.getAgenciaActiva();
    const idAgencia = agenciaActiva?.idAgencia ?? 0;

    return this.http.get<any[]>(
      `${this.base}/formas-ahorro/${idAgencia}`,
      { headers: this.buildHeaders() }
    );
  }

  // ============================================================
  // 🔹 VALIDAR REGLAS DE NEGOCIO ANTES DE ABRIR CUENTAS
  //    /hoja-vida/apertura-cuentas/validar/{idPersona}/{idAgencia}
  // ============================================================
  validar(idPersona: number, idAgencia: number): Observable<any> {

    return this.http.get<any>(
      `${this.base}/validar/${idPersona}/${idAgencia}`,
      { headers: this.buildHeaders() }
    );
  }


  // ============================================================
  // 🔹 CREAR CUENTA
  // ============================================================
  crear(data: any): Observable<any> {
    return this.http.post<any>(
      `${this.base}`,
      data,
      { headers: this.buildHeaders() }
    );
  }
}
