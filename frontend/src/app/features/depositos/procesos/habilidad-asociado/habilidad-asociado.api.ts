import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { firstValueFrom } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class HabilidadAsociadoApi {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/depositos/habilidad-asociado`;

  /**
   * Ejecuta el análisis de habilidad / inhabilidad.
   */
  ejecutar(body: any): Promise<any[]> {
    return firstValueFrom(
      this.http.post<any[]>(`${this.base}/ejecutar`, body)
    );
  }

  /**
   * Alias requerido por el componente (this.api.evaluar).
   */
  evaluar(body: any): Promise<any[]> {
    return this.ejecutar(body);
  }

  /**
   * Cambia el estado de la cuenta (A ↔ I).
   */
  actualizarEstado(idCuenta: number, estado: string): Promise<void> {
    return firstValueFrom(
      this.http.put<void>(`${this.base}/estado/${idCuenta}`, { estado })
    );
  }
}
