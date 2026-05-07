import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { catchError, map, of } from 'rxjs';

export interface AgenciaDTO {
  idAgencia: number;
  nombreAgencia: string;
}

export interface FormaAhorroDTO {

  idFormaAhorro: number;

  codigoForma: string;
  nombreForma: string;

  fechaUltimaLiquidacion?: string | null;

  tasaInteresForma?: number;
  valorMinimo?: number;

  tiempoLiquidacion?: string | number;

  idCuentaGasto?: number;
  codigoCuentaGasto?: string;
  nombreCuentaGasto?: string;

  idCuentaFormaCorto?: number;
  codigoCuentaFormaCorto?: string;
  nombreCuentaFormaCorto?: string;

  idCuentaRetencionFuente?: number | null;
  codigoCuentaRetencionFuente?: string | null;
  nombreCuentaRetencionFuente?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AgenciaFormaApi {

  private readonly http = inject(HttpClient);
  private readonly baseGeneral = `${environment.apiUrl}/general`;

  listarAgencias() {
    return this.http
      .get<any[]>(`${this.baseGeneral}/agencias`)
      .pipe(
        map(items =>
          items.map(item => ({
            idAgencia: item.idAgencia ?? item.id_agencia,
            nombreAgencia: item.nombreAgencia ?? item.nombre_agencia
          }))
        ),
        catchError(() => of([] as AgenciaDTO[]))
      );
  }

  listarFormasPorAgencia(idAgencia: number) {
    return this.http
      .get<any[]>(`${environment.apiUrl}/depositos/formasagencias/${idAgencia}`)
      .pipe(
        map(items =>
          items.map(item => ({
            idFormaAhorro: item.idFormaAhorro ?? item.id_forma_ahorro,
            codigoForma: item.codigoForma ?? item.codigo_forma,
            nombreForma: item.nombreForma ?? item.nombre_forma,

            fechaUltimaLiquidacion:
              item.fechaUltimaLiquidacion ?? item.fecha_ultima_liquidacion,

            tasaInteresForma:
              item.tasaInteresForma ?? item.tasa_interes_forma,

            valorMinimo:
              item.valorMinimo ?? item.valor_minimo,

            tiempoLiquidacion:
              item.tiempoLiquidacion ?? item.tiempo_liquidacion,

            idCuentaGasto:
              item.idCuentaGasto ?? item.id_cuenta_gasto,

            codigoCuentaGasto:
              item.codigoCuentaGasto ?? item.codigo_cuenta_gasto,

            nombreCuentaGasto:
              item.nombreCuentaGasto ?? item.nombre_cuenta_gasto,

            idCuentaFormaCorto:
              item.idCuentaFormaCorto ?? item.id_cuenta_forma_corto,

            codigoCuentaFormaCorto:
              item.codigoCuentaFormaCorto ?? item.codigo_cuenta_forma_corto,

            nombreCuentaFormaCorto:
              item.nombreCuentaFormaCorto ?? item.nombre_cuenta_forma_corto,

            idCuentaRetencionFuente:
              item.idCuentaRetencionFuente ?? item.id_cuenta_retencion_fuente,

            codigoCuentaRetencionFuente:
              item.codigoCuentaRetencionFuente ?? item.codigo_cuenta_retencion_fuente,

            nombreCuentaRetencionFuente:
              item.nombreCuentaRetencionFuente ?? item.nombre_cuenta_retencion_fuente
          }))
        ),
        catchError(() => of([] as FormaAhorroDTO[]))
      );
  }
}
