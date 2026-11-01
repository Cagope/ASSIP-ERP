import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { FiltrosComprobantes } from '../consulta/comprobantes-consulta.models';

/** Cabecera del comprobante, con totales, origen y auditoría. */
export interface CabeceraExportacion {
  idOrigenComprobante: number;
  idAgencia: number;
  codigoAgencia: string | null;
  nombreAgencia: string | null;
  tipoComprobante: string;
  nombreTipoComprobante: string | null;
  numeroComprobante: string;
  fechaContable: string | null;
  conceptoComprobante: string | null;
  origenTipo: string | null;
  moduloOrigen: string | null;
  procesoOrigen: string | null;
  tablaOrigen: string | null;
  idOrigen: number | null;
  estadoOrigen: string | null;
  totalDebito: number;
  totalCredito: number;
  cantidadMovimientos: number;
  idUsuarioCreacionOrigen: number | null;
  usuarioCreacionOrigen: string | null;
  nombreCreacionOrigen: string | null;
  fechaCreacionOrigen: string | null;
  idUsuarioEdicionOrigen: number | null;
  usuarioEdicionOrigen: string | null;
  nombreEdicionOrigen: string | null;
  fechaEdicionOrigen: string | null;
  idUsuarioCreacionConcepto: number | null;
  usuarioCreacionConcepto: string | null;
  nombreCreacionConcepto: string | null;
  fechaCreacionConcepto: string | null;
  idUsuarioEdicionConcepto: number | null;
  usuarioEdicionConcepto: string | null;
  nombreEdicionConcepto: string | null;
  fechaEdicionConcepto: string | null;
}

/** Movimiento contable individual, sin duplicación por controles documentales. */
export interface MovimientoExportacion {
  idAuxiliarContable: number;
  idAgencia: number;
  tipoComprobante: string;
  numeroComprobante: string;
  idCatalogoCuenta: number | null;
  codigoCuenta: string | null;
  nombreCuenta: string | null;
  idDatosPersonal: number | null;
  documentoTercero: string | null;
  nombreTercero: string | null;
  fechaAuxiliar: string | null;
  detalleMovimiento: string | null;
  estadoMovimiento: string | null;
  valorDebito: number | null;
  valorCredito: number | null;
  valorBase: number | null;
  origenModulo: string | null;
  documentoOrigen: string | null;
  documentoSoporte: string | null;
  idUsuarioCreacion: number | null;
  usuarioCreacion: string | null;
  nombreCreacion: string | null;
  fechaCreacion: string | null;
  idUsuarioEdicion: number | null;
  usuarioEdicion: string | null;
  nombreEdicion: string | null;
  fechaEdicion: string | null;
}

/** Documento de control asociado a un movimiento contable. */
export interface ControlExportacion {
  idControlesAuxiliares: number;
  idAuxiliarContable: number;
  idTiposControl: number | null;
  descripcionTipoControl: string | null;
  pideDocumento: boolean | null;
  numeroDocumento: string | null;
  idUsuarioCreacion: number | null;
  usuarioCreacion: string | null;
  nombreCreacion: string | null;
  fechaCreacion: string | null;
  idUsuarioEdicion: number | null;
  usuarioEdicion: string | null;
  nombreEdicion: string | null;
  fechaEdicion: string | null;
}

/** Respuesta del backend para generar el libro Excel en Angular. */
export interface DatosExportacion {
  comprobantes: CabeceraExportacion[];
  movimientos: MovimientoExportacion[];
  controles: ControlExportacion[];
}

/** Consume los endpoints de exportación; la generación del Excel se realiza por separado. */
@Injectable({ providedIn: 'root' })
export class ComprobantesExportacionApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/contabilidad/comprobantes/exportacion`;

  /** Obtiene todos los comprobantes de los filtros aplicados, sin el límite de pantalla. */
  exportarConsulta(filtros: FiltrosComprobantes): Observable<DatosExportacion> {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(filtros)) {
      if (valor !== undefined && valor !== null && String(valor).trim() !== '') {
        params = params.set(clave, String(valor).trim());
      }
    }
    return this.http.get<DatosExportacion>(`${this.base}/consulta`, { params });
  }

  /** Obtiene la cabecera, los movimientos y los controles de un comprobante. */
  exportarComprobante(idAgencia: number, tipo: string, numero: string): Observable<DatosExportacion> {
    return this.http.get<DatosExportacion>(
      `${this.base}/comprobante/${idAgencia}/${encodeURIComponent(tipo)}/${encodeURIComponent(numero)}`
    );
  }
}
