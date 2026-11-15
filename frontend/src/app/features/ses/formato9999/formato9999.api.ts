
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';

/**
 * Información del asociado para el reporte SES.
 */
export interface AsociadoSES {
  codigoEntidad: string;
  fechaCorte: string;

  tipoIdentificacion: string;
  numeroIdentificacion: string;
  digitoVerificacion: string | null;

  primerApellido: string;
  segundoApellido: string;
  nombres: string;
  tipoPersona: string;

  fechaIngreso: string | null;
  rolAsociado: number;
  activo: number;

  telefono: string | null;
  celular: string | null;
  direccion: string | null;
  codigoMunicipio: string | null;
  email: string | null;

  actividadEconomica: string | null;
  sectorEconomico: number | null;
  ocupacion: number | null;

  empleado: number | null;
  tipoContrato: number | null;
  jornadaLaboral: number | null;

  nivelEscolaridad: number | null;
  estrato: number | null;
  nivelIngresos: number | null;

  fechaNacimiento: string | null;
  genero: number | null;
  estadoCivil: number | null;
  mujerCabezaFamilia: number | null;

  fechaRetiro: string | null;
  asistioAsamblea: number | null;

  saldoAportes: number | null;
}

/**
 * Respuesta de la configuración del formato SES.
 */
export interface FormatoAsociadosSES {
  codigoFormato: string;
}

@Injectable({
  providedIn: 'root'
})
export class Formato9999Api {

  private readonly http = inject(HttpClient);

  private readonly baseUrl =
    `${environment.apiUrl}/ses/asociados`;

  private readonly formatosUrl =
    `${environment.apiUrl}/ses/formatos`;

  /**
   * Consulta los asociados a una fecha de corte.
   *
   * La fecha se envía en formato yyyy-MM-dd.
   * El backend devuelve las fechas del reporte
   * en formato ddMMyyyy.
   */
  consultar(fechaCorte: string): Observable<AsociadoSES[]> {

    const params = new HttpParams()
      .set('fechaCorte', fechaCorte);

    return this.http.get<AsociadoSES[]>(
      this.baseUrl,
      { params }
    );
  }

  /**
   * Obtiene el código oficial del formato Asociados.
   *
   * Utiliza la API centralizada de formatos SES,
   * que consulta supersolidaria.formatos.
   *
   * No se fija el código del formato en Angular.
   */
  consultarFormato(): Observable<FormatoAsociadosSES> {

    const params = new HttpParams()
      .set('modulo', 'HOJA_VIDA')
      .set('nombre', 'ASOCIADOS');

    return this.http.get<FormatoAsociadosSES>(
      this.formatosUrl,
      { params }
    );
  }
}
