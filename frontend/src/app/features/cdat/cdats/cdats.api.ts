import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CdatListDTO {
  idCuentaCdat: number;
  codigoCdat: string;
  idAgencia: number;
  idDatosPersonal: number;
  nombreCompleto: string;
  fechaAperturaCdat: string;
  fechaVencimientoCdat: string;
  plazoMeses: number;
  valorAperturaCdat: number;
  saldoActualCdat: number;
  tasaEfectivaAnual: number;
  estadoCdat: string;
  origenCdat: string;
}

export interface CdatFormDTO {
  idCuentaCdat: number | null;
  idAgencia: number | null;
  idProductoCdat: number | null;
  codigoCdat: string | null;
  idDatosPersonal: number | null;
  idDatosPersonalCotitular: number | null;
  fechaAperturaCdat: string;
  fechaVencimientoCdat: string | null;
  plazoMeses: number | null;
  plazoDias: number | null;
  valorAperturaCdat: number | null;
  saldoActualCdat: number | null;
  tasaNominalAnual: number | null;
  tasaEfectivaAnual: number | null;
  tasaNominalMensual: number | null;
  tasaEfectivaMensual: number | null;
  retencionFuenteCdat: boolean | null;
  amortizacionDeposito: string;
  modalidadCdat: string;
  fechaUltimaLiquidacion: string | null;
  fechaProximaLiquidacion: string | null;
  fechaUltimoTrasladoInteres: string | null;
  fechaProximoTrasladoInteres: string | null;
  idCuentaAportes: number | null;
  idCuentaAhorro: number | null;
  cuentaConjunta: string;
  accionConjunta: string | null;
  origenCdat: string;
  idCuentaCdatOrigen: number | null;
  estadoCdat: string | null;
  tipoComprobante: string;
  numeroComprobante: string;
  fechaComprobante: string | null;
  idCaja: number | null;
  valorEfectivo: number;
  valorCheque: number;
  valorDepositos: number;
  valorBanco: number;
  valorTrasladosAgencias: number;
  observacion: string | null;
  beneficiarios?: CdatBeneficiarioDTO[];
  cheques?: CdatChequeDTO[];
  mediosPago?: CdatMediosPagoDTO;
}

export interface CdatAsociadoValidacionDTO {
  idDatosPersonal: number | null;
  documento: string;
  nombreCompleto: string;
  tieneAportesActivos: boolean | null;
  datosActualizados: boolean | null;
  mensajeError: string | null;
}

export interface CdatSaveResponseDTO {
  idCuentaCdat: number;
  codigoCdat: string;
  tipoComprobante: string;
  numeroComprobante: string;
  mensaje: string;
}

export interface CdatAmortizacionDTO {
  codigoAmortizacion: string;
  nombreAmortizacion: string;
  meses: number;
}

export interface CdatBeneficiarioDTO {
  idBeneficiarioCdat?: number | null;
  idCuentaCdat?: number | null;
  idDatosPersonal?: number | null;
  documento: string;
  nombre: string;
  telefono?: string | null;
  tipoParentesco?: string | null;
  estado?: string | null;
}

export interface CdatChequeDTO {
  idChequeCdat?: number | null;
  idCuentaCdat?: number | null;
  codigoBanco: string;
  numeroCheque: string;
  valorCheque: number;
}

export interface CdatDepositoDTO {
  idCuentaAhorro: number;
  codigoCuenta: string;
  saldoDisponible: number;
  valorDebitar: number;
}

export interface CdatBancoDTO {
  idCatalogoCuentaBanco: number;
  codigoCuentaBanco: string;
  nombreCuentaBanco: string;
  valorBanco: number;
}

export interface CdatTrasladoAgenciaDTO {
  idCatalogoCuentaTraslado: number;
  codigoCuentaTraslado: string;
  nombreCuentaTraslado: string;
  valorTraslado: number;
}

export interface CdatMediosPagoDTO {
  idCaja: number | null;
  valorEfectivo: number;
  valorCheques: number;
  valorDepositos: number;
  valorBancos: number;
  cheques: CdatChequeDTO[];
  depositos: CdatDepositoDTO[];
  bancos: CdatBancoDTO[];
  valorTrasladosAgencias: number;
  trasladosAgencias: CdatTrasladoAgenciaDTO[];
}

export interface MovimientoContableDTO {
  idAgencia: number;
  fechaAuxiliar: string;

  idCatalogoCuenta: number;
  codigoCuenta: string | null;
  nombreCuenta: string | null;

  idDatosPersonal: number | null;
  documentoTercero: string | null;
  nombreTercero: string | null;

  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
}

export interface CdatAperturaPreviewDTO {
  movimientos: MovimientoContableDTO[];
  totalDebito: number;
  totalCredito: number;
  diferencia: number;
  cuadrado: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CdatsApi {

  private readonly baseUrl = `${environment.apiUrl}/cdat/cdats`;
  private readonly reportingUrl = `${environment.apiUrl}/reporting/query`;

  constructor(private http: HttpClient) {}

  listar(): Observable<CdatListDTO[]> {
    return this.http.get<CdatListDTO[]>(this.baseUrl);
  }

  obtenerPorId(idCuentaCdat: number): Observable<CdatFormDTO> {
    return this.http.get<CdatFormDTO>(`${this.baseUrl}/${idCuentaCdat}`);
  }

  obtenerProximoCodigo(): Observable<string> {
    return this.http.get(`${this.baseUrl}/proximo-codigo`, {
      responseType: 'text'
    });
  }

  buscarCuentas(req: any): Observable<any> {
    return this.http.post<any>(this.reportingUrl, req);
  }

  validarAsociado(documento: string): Observable<CdatAsociadoValidacionDTO> {
    return this.http.get<CdatAsociadoValidacionDTO>(
      `${this.baseUrl}/validar-asociado/${documento}`
    );
  }

  guardar(dto: CdatFormDTO): Observable<CdatSaveResponseDTO> {
    return this.http.post<CdatSaveResponseDTO>(this.baseUrl, dto);
  }

  listarAmortizaciones(): Observable<CdatAmortizacionDTO[]> {
    return this.http.get<CdatAmortizacionDTO[]>(
      `${environment.apiUrl}/cdat/catalogos/amortizaciones`
    );
  }

  buscarCotitular(documento: string): Observable<CdatAsociadoValidacionDTO> {
    return this.http.get<CdatAsociadoValidacionDTO>(
      `${this.baseUrl}/buscar-cotitular/${documento}`
    );
  }

  obtenerCajasAbiertas(idAgencia: number, fecha: string): Observable<any[]> {
    return this.http.get<any[]>(
      `${environment.apiUrl}/cajas/provisiones/abiertas`,
      {
        params: {
          idAgencia,
          fecha
        }
      }
    );
  }

  previewAperturaContable(dto: CdatFormDTO): Observable<CdatAperturaPreviewDTO> {
    return this.http.post<CdatAperturaPreviewDTO>(
      `${this.baseUrl}/apertura/preview-contable`,
      dto
    );
  }

}
