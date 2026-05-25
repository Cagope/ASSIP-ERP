import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CdatCierreMensualEntradaDTO {
  idAgencia: number | null;
  fechaCorte: string;
  confirmado: boolean;
}

export interface CdatCierreMensualItemDTO {
  idCuentaCdat: number;
  codigoCdat: string;
  idAgencia: number;

  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;
  idDatosPersonalCotitular: number | null;
  documentoCotitular: string | null;
  nombreCotitular: string | null;
  fechaAperturaCdat: string;
  fechaVencimientoCdat: string;
  fechaUltimaLiquidacion: string;
  fechaProximaLiquidacion: string;
  fechaUltimoTrasladoInteres: string;
  fechaProximoTrasladoInteres: string;

  plazoMeses: number;
  plazoDias: number;

  valorAperturaCdat: number;
  saldoActualCdat: number;

  tasaNominalAnual: number;
  tasaEfectivaAnual: number;
  tasaNominalMensual: number;
  tasaEfectivaMensual: number;

  retencionFuenteCdat: boolean;

  amortizacionDeposito: string;
  modalidadCdat: string;

  idCuentaAportes: number | null;
  codigoCuentaAportes: string;
  idCuentaAhorro: number | null;
  codigoCuentaAhorro: string;

  cuentaConjunta: boolean;
  accionConjunta: string | null;

  origenCdat: string;
  idCuentaCdatOrigen: number | null;

  estadoCdat: string;
}

export interface CdatCierreMensualPreviewDTO {
  idCierreMensualCdat: number | null;

  idAgencia: number;
  fechaCorte: string;

  anio: number;
  mes: number;

  totalCdats: number;

  totalValorApertura: number;
  totalCapital: number;

  totalCapitalAportes: number;
  totalCapitalAhorros: number;

  existeCierre: boolean;

  estado: string;
  observacion: string | null;

  items: CdatCierreMensualItemDTO[];
}

@Injectable({
  providedIn: 'root'
})
export class CierreMensualCdatApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cdat/cierre-mensual-cdat`;

  constructor(private http: HttpClient) {}

  preview(dto: CdatCierreMensualEntradaDTO): Promise<CdatCierreMensualPreviewDTO> {
    return firstValueFrom(
      this.http.post<CdatCierreMensualPreviewDTO>(
        `${this.baseUrl}/preview`,
        dto
      )
    );
  }

  aplicar(dto: CdatCierreMensualEntradaDTO): Promise<CdatCierreMensualPreviewDTO> {
    return firstValueFrom(
      this.http.post<CdatCierreMensualPreviewDTO>(
        `${this.baseUrl}/aplicar`,
        dto
      )
    );
  }

  listar(): Promise<CdatCierreMensualPreviewDTO[]> {
    return firstValueFrom(
      this.http.get<CdatCierreMensualPreviewDTO[]>(
        `${this.baseUrl}/listar`
      )
    );
  }

  obtenerPorId(idCierre: number): Promise<CdatCierreMensualPreviewDTO> {
    return firstValueFrom(
      this.http.get<CdatCierreMensualPreviewDTO>(
        `${this.baseUrl}/${idCierre}`
      )
    );
  }

  eliminar(idCierre: number): Promise<void> {
    return firstValueFrom(
      this.http.delete<void>(
        `${this.baseUrl}/${idCierre}`
      )
    );
  }

}
