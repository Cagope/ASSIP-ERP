import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InteresDiarioSmApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/depositos/interes-diario-sm`;

  // ==========================================================
  // 🔍 PREVIEW / LIQUIDAR
  // ==========================================================
  liquidar(body: any): Promise<any> {

    return firstValueFrom(
      this.http.post<any>(
        `${this.baseUrl}/liquidar`,
        body
      )
    );
  }

  // ==========================================================
  // ✅ APLICAR
  // ==========================================================
  aplicar(body: any): Promise<any> {

    return firstValueFrom(
      this.http.post<any>(
        `${this.baseUrl}/aplicar`,
        body
      )
    );
  }

  // ==========================================================
  // 🔢 PRÓXIMO COMPROBANTE
  // ==========================================================
  obtenerProximoComprobante(
    idAgencia: number,
    tipoComprobante: string
  ): Promise<any> {

    return firstValueFrom(
      this.http.get<any>(
        `${this.baseUrl}/proximo-comprobante/${idAgencia}/${tipoComprobante}`
      )
    );
  }

  // ==========================================================
  // 📌 ÚLTIMA LIQUIDACIÓN
  // ==========================================================
  obtenerUltimaLiquidacion(
    idAgencia: number,
    idFormaAhorro: number
  ): Promise<any> {

    return firstValueFrom(
      this.http.get<any>(
        `${this.baseUrl}/ultima-liquidacion/${idAgencia}/${idFormaAhorro}`
      )
    );
  }

}
