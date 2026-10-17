
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

import {
  CajaConProvision,
  CajaControlRetiro,
  CajaMovimiento,
  ChequeDisponible,
  CuentaBancariaMovimiento,
  CuentaBancariaRetiro,
  MovimientoFondoPreview,
  MovimientoFondoResponse,
  MovimientoFondoRequest,
  RetiroBancarioPendienteResponse,
  CuentaBancariaTraslado,
  TrasladoBancarioPendienteResponse
} from './movimientos-fondos.models';

@Injectable({
  providedIn: 'root'
})
export class MovimientosFondosApi {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/tesoreria/movimientos-fondos`;

  // =========================================================
  // CUENTAS BANCARIAS PARA CONSIGNACIONES
  // =========================================================

  /**
   * Consulta las cuentas bancarias disponibles
   * para consignaciones de la agencia.
   */
  listarCuentasBancarias(
    idAgencia: number
  ): Observable<CuentaBancariaMovimiento[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<CuentaBancariaMovimiento[]>(
      `${this.baseUrl}/cuentas-bancarias`,
      { params }
    );
  }

  // =========================================================
  // CHEQUES RECIBIDOS DISPONIBLES
  // =========================================================

  /**
   * Consulta los cheques recibidos que están
   * disponibles para consignación bancaria.
   */
  listarCheques(
    idAgencia: number
  ): Observable<ChequeDisponible[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<ChequeDisponible[]>(
      `${this.baseUrl}/cheques`,
      { params }
    );
  }

  // =========================================================
  // CAJAS CON PROVISIÓN PARA CONSIGNACIONES BANCARIAS
  // =========================================================

  /**
   * Consulta las cajas con provisión disponible
   * para realizar consignaciones bancarias.
   */
  listarCajasConProvision(
    idAgencia: number,
    fechaConsignacion: string
  ): Observable<CajaConProvision[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString())
      .set('fechaConsignacion', fechaConsignacion);

    return this.http.get<CajaConProvision[]>(
      `${this.baseUrl}/cajas`,
      { params }
    );
  }

  // =========================================================
  // CATÁLOGO DE CAJAS CONTROL Y OPERATIVAS
  // =========================================================

  /**
   * Consulta las cajas habilitadas para
   * movimientos entre cajas.
   */
  listarCajasMovimiento(
    idAgencia: number
  ): Observable<CajaMovimiento[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<CajaMovimiento[]>(
      `${this.baseUrl}/cajas-movimiento`,
      { params }
    );
  }

  // =========================================================
  // CAJAS CON PROVISIÓN ABIERTA PARA TRASLADOS
  // =========================================================

  /**
   * Consulta las cajas que tienen provisión
   * abierta en la fecha contable indicada.
   */
  listarCajasTrasladoAbiertas(
    idAgencia: number,
    fechaContable: string
  ): Observable<CajaConProvision[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString())
      .set('fechaContable', fechaContable);

    return this.http.get<CajaConProvision[]>(
      `${this.baseUrl}/cajas-traslado-abiertas`,
      { params }
    );
  }

  // =========================================================
  // RETIROS BANCARIOS - CUENTAS HABILITADAS
  // =========================================================

  /**
   * Consulta las cuentas bancarias desde las
   * cuales la agencia puede realizar retiros.
   *
   * La validación de permisos y agencia
   * corresponde al backend.
   */
  listarCuentasRetiro(
    idAgencia: number
  ): Observable<CuentaBancariaRetiro[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<CuentaBancariaRetiro[]>(
      `${this.baseUrl}/cuentas-retiro`,
      { params }
    );
  }

  // =========================================================
  // RETIROS BANCARIOS - CAJAS CONTROL
  // =========================================================

  /**
   * Consulta las cajas CONTROL habilitadas
   * para recibir efectivo de retiros bancarios.
   */
  listarCajasControlRetiro(
    idAgencia: number
  ): Observable<CajaControlRetiro[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<CajaControlRetiro[]>(
      `${this.baseUrl}/cajas-control-retiro`,
      { params }
    );
  }


  // =========================================================
  // TRASLADOS BANCARIOS - CUENTAS DE ORIGEN
  // =========================================================

  /**
   * Consulta las cuentas bancarias habilitadas
   * para trasladar recursos desde la agencia.
   *
   * El backend valida permite_trasladar.
   */
  listarCuentasOrigenTrasladoBancario(
    idAgencia: number
  ): Observable<CuentaBancariaTraslado[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<CuentaBancariaTraslado[]>(
      `${this.baseUrl}/cuentas-origen-traslado-bancario`,
      { params }
    );
  }

  // =========================================================
  // TRASLADOS BANCARIOS - CUENTAS DE DESTINO
  // =========================================================

  /**
   * Consulta las cuentas bancarias habilitadas
   * para recibir recursos en la agencia.
   *
   * El backend valida permite_consignar.
   */
  listarCuentasDestinoTrasladoBancario(
    idAgencia: number
  ): Observable<CuentaBancariaTraslado[]> {

    const params = new HttpParams()
      .set('idAgencia', idAgencia.toString());

    return this.http.get<CuentaBancariaTraslado[]>(
      `${this.baseUrl}/cuentas-destino-traslado-bancario`,
      { params }
    );
  }

  // =========================================================
  // TRASLADOS BANCARIOS - PREPARAR COMPROBANTE
  // =========================================================

  /**
   * Prepara un traslado bancario para autorización.
   *
   * Genera un comprobante TS en estado PENDIENTE.
   *
   * No genera consecutivo definitivo.
   * No contabiliza todavía.
   * No registra movimientos físicos de caja.
   */
  prepararTrasladoBancario(
    request: MovimientoFondoRequest
  ): Observable<TrasladoBancarioPendienteResponse> {

    if (request.tipoMovimiento !== 'TRASLADO_BANCARIO') {
      throw new Error(
        'La solicitud no corresponde a un traslado bancario.'
      );
    }

    return this.http.post<TrasladoBancarioPendienteResponse>(
      `${this.baseUrl}/traslados-bancarios/preparar`,
      request
    );
  }


  // =========================================================
  // VISTA PREVIA CONTABLE
  // =========================================================

  /**
   * Genera la vista previa del movimiento.
   *
   * Admite CONSIGNACION, TRASLADO_CAJAS
   * y RETIRO, según el backend.
   *
   * No registra movimientos definitivos.
   */
  previewContable(
    request: MovimientoFondoRequest
  ): Observable<MovimientoFondoPreview> {

    return this.http.post<MovimientoFondoPreview>(
      `${this.baseUrl}/preview-contable`,
      request
    );
  }

  // =========================================================
  // RETIROS BANCARIOS - PREPARAR COMPROBANTE
  // =========================================================

  /**
   * Prepara un retiro bancario para autorización.
   *
   * El comprobante queda PENDIENTE.
   * No genera consecutivo definitivo,
   * contabilización ni movimiento físico de caja.
   *
   * La contabilización y el ingreso a caja
   * se realizan posteriormente al autorizar.
   */
  prepararRetiro(
    request: MovimientoFondoRequest
  ): Observable<RetiroBancarioPendienteResponse> {

    if (request.tipoMovimiento !== 'RETIRO') {
      throw new Error(
        'La solicitud no corresponde a un retiro bancario.'
      );
    }

    return this.http.post<RetiroBancarioPendienteResponse>(
      `${this.baseUrl}/retiros/preparar`,
      request
    );
  }

  // =========================================================
  // REGISTRO DEFINITIVO
  // =========================================================

  /**
   * Registra definitivamente consignaciones
   * bancarias o traslados entre cajas.
   *
   * Los retiros bancarios NO se registran aquí:
   * deben prepararse y autorizarse.
   */
  aplicarMovimiento(
    request: MovimientoFondoRequest
  ): Observable<MovimientoFondoResponse> {

    const endpoint =
      request.tipoMovimiento === 'CONSIGNACION'
        ? 'consignaciones'
        : request.tipoMovimiento === 'TRASLADO_CAJAS'
          ? 'traslados-cajas'
          : null;

    if (!endpoint) {
      throw new Error(
        'La operación seleccionada todavía no está habilitada para registro definitivo.'
      );
    }

    return this.http.post<MovimientoFondoResponse>(
      `${this.baseUrl}/${endpoint}`,
      request
    );
  }
}
