import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CdatCausacionMensualEntradaDTO {
  idAgencia: number | null;
  fechaCorte: string;
  fechaContable: string;

  tipoComprobante: string;
  numeroComprobante: string;

  confirmado: boolean;
}

export interface CdatCausacionMensualItemDTO {

  idCierreMensualCdatDetalle: number;

  idCuentaCdat: number;
  codigoCdat: string;

  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;

  idDatosPersonalCotitular: number | null;
  documentoCotitular: string | null;
  nombreCotitular: string | null;

  fechaAperturaCdat: string;
  fechaVencimientoCdat: string;
  fechaUltimaLiquidacion: string;

  saldoBase: number;
  tasaNominalAnual: number;

  diasCausados: number;

  valorInteres: number;
  valorRetencion: number;
  valorNeto: number;

  aplicaRetencion: boolean;

  modalidadCdat: string;
  amortizacionDeposito: string;

  idCuentaAportes: number | null;
  codigoCuentaAportes: string;

  idCuentaAhorro: number | null;
  codigoCuentaAhorro: string;
}

export interface CdatCausacionMensualPreviewDTO {

  idCausacionMensualInteres: number | null;
  idCierreMensualCdat: number | null;

  idAgencia: number;
  fechaCorte: string;
  fechaContable: string;

  anio: number;
  mes: number;

  tipoComprobante: string;
  numeroComprobante: string;

  totalCdats: number;

  totalCapital: number;
  totalInteres: number;
  totalRetencion: number;
  totalNeto: number;

  existeCausacion: boolean;
  estado: string;

  items: CdatCausacionMensualItemDTO[];
}

@Injectable({
  providedIn: 'root'
})
export class CausacionMensualCdatApi {

  private readonly baseUrl =
    `${environment.apiUrl}/cdat/causacion-mensual-cdat`;

  constructor(private http: HttpClient) {}

  preview(dto: CdatCausacionMensualEntradaDTO):
    Promise<CdatCausacionMensualPreviewDTO> {

    return firstValueFrom(
      this.http.post<CdatCausacionMensualPreviewDTO>(
        `${this.baseUrl}/preview`,
        dto
      )
    );
  }

  aplicar(dto: CdatCausacionMensualEntradaDTO):
    Promise<CdatCausacionMensualPreviewDTO> {

    return firstValueFrom(
      this.http.post<CdatCausacionMensualPreviewDTO>(
        `${this.baseUrl}/aplicar`,
        dto
      )
    );
  }

    listar():
      Promise<CdatCausacionMensualPreviewDTO[]> {

      return firstValueFrom(
        this.http.get<CdatCausacionMensualPreviewDTO[]>(
          `${this.baseUrl}/listar`
        )
      );
    }

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

  }
