import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/* =========================================================
   DTOs
   ========================================================= */

export interface DesprendibleEmpleadoListDTO {
  idContrato: number;
  idEmpleado: number;
  documento: string;
  nombreCompleto: string;
  cargo: string;
  salarioBase: number;
}

/* =========================================================
   API
   ========================================================= */

@Injectable({ providedIn: 'root' })
export class DesprendibleApi {

  private readonly base =
    `${environment.apiUrl}/nomina/desprendible`;

  constructor(private http: HttpClient) {}

  // =========================================================
  // LISTAR EMPLEADOS DEL PERÍODO (CERRADO)
  // =========================================================

  listarEmpleados(
    idPeriodo: number
  ): Observable<DesprendibleEmpleadoListDTO[]> {

    return this.http.get<DesprendibleEmpleadoListDTO[]>(
      `${this.base}/${idPeriodo}/empleados`
    );
  }

  // =========================================================
  // IMPRIMIR DESPRENDIBLE (PDF)
  // =========================================================
  // ✔️ Envía JWT
  // ✔️ Maneja blob
  // ✔️ Abre en nueva pestaña
  // =========================================================

  imprimirPdf(
    idPeriodo: number,
    idContrato: number
  ): void {

    const params = new HttpParams()
      .set('idPeriodo', idPeriodo)
      .set('idContrato', idContrato);

    this.http.get(
      `${this.base}/pdf`,
      {
        params,
        responseType: 'blob'
      }
    ).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        // se puede revocar luego si quieres
        // URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Error generando desprendible', err);
        alert(
          'No se pudo generar el desprendible.\n' +
          'Tu sesión puede haber expirado.'
        );
      }
    });
  }

  // =========================================================
  // GENERAR TODOS LOS DESPRENDIBLES (ZIP)
  // =========================================================
  generarTodosPdf(
    idPeriodo: number,
    filename: string
  ): void {

    this.http.get(
      `${this.base}/pdf/todos`,
      {
        params: { idPeriodo },
        responseType: 'blob'
      }
    ).subscribe({
      next: (blob) => {

        const a = document.createElement('a');
        const url = URL.createObjectURL(blob);

        a.href = url;
        a.download = filename; // 👈 nombre claro + .zip
        a.click();

        URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Error generando ZIP de desprendibles', err);
        alert('No se pudo generar el archivo ZIP.');
      }
    });
  }

}
